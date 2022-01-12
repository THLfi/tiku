package fi.thl.pivot.web;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fi.thl.pivot.exception.SameDimensionAsRowAndColumnException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.google.common.collect.Maps;

import fi.thl.pivot.datasource.AmorDao;
import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.datasource.LogSource;
import fi.thl.pivot.exception.CubeAccessDeniedException;
import fi.thl.pivot.exception.CubeNotFoundException;
import fi.thl.pivot.exception.UserNotAuthenticatedException;
import fi.thl.pivot.util.ThreadRole;

/**
 * Provides common features for all controllers including exception handling and
 * login functionality
 * 
 * @author aleksiyrttiaho
 *
 */
public abstract class AbstractController {

    private static final Logger LOG = Logger.getLogger(AbstractController.class);

    @Autowired
    protected AmorDao amorDao;

    @Autowired
    protected FreeMarkerConfig freemarker;

    @Autowired
    private AccessTokens accessToken;

    @Autowired
    protected LogSource logSource;

    @Autowired
    protected ApplicationContext ctx;
    
    /**
     * Provides a friendly error messages when content is not found
     * 
     * @param resp
     * @return
     */
    @ExceptionHandler(CubeNotFoundException.class)
    public ModelAndView handleCubeNotFound(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        ModelAndView model = new ModelAndView("error");
        model.addObject("lang", ThreadRole.getLanguage());
        model.addObject("uiLanguage", ThreadRole.getLanguage());
        model.addObject("status", HttpServletResponse.SC_NOT_FOUND);
        return model;
    }

    /**
     * Provides a friendly error messages when content is not sensible
     *
     * @param resp
     * @return
     */
    @ExceptionHandler(SameDimensionAsRowAndColumnException.class)
    public ModelAndView handleSameDimensionAsRowsAndColumn(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ModelAndView model = new ModelAndView("error");
        model.addObject("lang", ThreadRole.getLanguage());
        model.addObject("uiLanguage", ThreadRole.getLanguage());
        model.addObject("status", -1000);
        return model;
    }

    @ExceptionHandler(CubeAccessDeniedException.class)
    public ModelAndView handleCubeAccessDenied(CubeAccessDeniedException e, HttpServletResponse resp) {
        LOG.warn("SECURITY: " + e.getMessage());
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ModelAndView model = new ModelAndView("error");
        model.addObject("lang", ThreadRole.getLanguage());
        model.addObject("uiLanguage", ThreadRole.getLanguage());
        model.addObject("status", HttpServletResponse.SC_FORBIDDEN);
        return model;
    }

    /**
     * Provides a friendly error messages when content is not found
     * 
     * @param resp
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e, HttpServletResponse resp) {
        LOG.error("Could not render view", e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ModelAndView model = new ModelAndView("error");
        model.addObject("lang", ThreadRole.getLanguage());
        model.addObject("uiLanguage", ThreadRole.getLanguage());
        model.addObject("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return model;
    }

    /**
     * 
     * Displays the login form and necessary error messages related to the login
     * process
     * 
     * @param resp
     * @param e
     * @return
     */
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ModelAndView handleUserNotAuthenticated(HttpServletResponse resp, UserNotAuthenticatedException e) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ModelAndView model = new ModelAndView("login");
        model.addObject("lang", ThreadRole.getLanguage());
        model.addObject("uiLanguage", ThreadRole.getLanguage());
        model.addObject("authenticationFailed", e.isAuthenticationFailed());
        model.addObject("cubeLabel", e.getSource().getName());
        model.addObject("csrf", accessToken.getCsrf());

        return model;
    }
    
    @ModelAttribute
    public void setBuildVersion(Model model, RedirectAttributes r) {
        if(null != ctx) {
            try {
                Properties p = (Properties)ctx.getBean("build-version");
                model.addAttribute("buildVersion", p.getProperty("build.version"));
                model.addAttribute("buildTimestamp", p.getProperty("build.timestamp"));
            } catch (Exception e) {
                LOG.warn("Could not set build version " + e.getMessage());;
            }
        }
    }

    /**
     * Attemps to log the user in to a specific cube in environment. The
     * password the user provides must match at least one password found in the
     * hydra metadata
     * 
     * @param env
     * @param cube
     * @param password
     */
    protected void login(String env, String cube, String password, HttpServletRequest request) {
        LOG.debug("SECURITY User attempting to login to cube " + cube);
        HydraSource source = amorDao.loadSource(env, cube);
        if (null != source) {
            loadMetadata(source);
            if (source.isProtectedWith(password)) {
                recreateSession(request);
                LOG.info("SECURITY User logged into " + env + "/" + cube + ", session: " + request.getSession().getId());
                request.getSession().setAttribute(sessionAttributeName(env, cube), password);
            } else {
                LOG.warn("SECURITY User provided an incorrect password while logging into " + env + "/" + cube + ", session: " + request.getSession().getId());
                throw new UserNotAuthenticatedException(source, true);
            }
        } else {
            LOG.warn("Source not found " + cube);
        }
    }

    /**
     * Logs the user out of each cube and environment
     */
    protected void logout(HttpSession session) {
        LOG.info("SECURITY user logged out, " + session.getId());
        session.invalidate();
    }

    /**
     * A Cube may be password protected. This is detected when there is at least
     * one password defined in the hydra metadata. If password is required then
     * user must first log in to the cube before accessing the data. Each
     * environment and cube is protected with their own credentials. If user has
     * to access multiple cubes then they have to provide passwords for each
     * cube and environment.
     * 
     * @param env
     * @param cube
     * @param model
     * @param source
     * @throws CubeAccessDeniedException
     */

    protected void checkLoginRequirements(AbstractRequest request, Model model, HydraSource source) {
        checkLoginRequirements(request, model, source, request.getCube());
    }

    protected void checkLoginRequirements(AbstractRequest request, Model model, HydraSource source, String cubeId) {
    	HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
        if (source.isProtected()) {
            Object pwdAttribute = session.getAttribute(sessionAttributeName(request.getEnv(), cubeId));
            if (pwdAttribute == null) {
                LOG.debug("SECURITY Users is not autheticated to access cube " + request.getEnv() + "/" + cubeId);
                throw new UserNotAuthenticatedException(source);
            }
            if (!source.isProtectedWith((String) pwdAttribute)) {
                LOG.debug("SECURITY Users is not autheticated to access cube, unsupported password " + request.getEnv() + "/" + cubeId);
                throw new UserNotAuthenticatedException(source);
            }

            checkIfCubeIsAccessible(request, source);

            LOG.info("SECURITY User is accessing protected cube " + request.getEnv() + "/" + cubeId + ", session: " + session.getId());
            model.addAttribute("requireLogin", true);
            String pwd = (String) pwdAttribute;
            
            ThreadRole.setRole(new Role(source.isMasterPassword(pwd)? Role.Type.Master : Role.Type.Regular, pwd));
        } else {
            checkIfCubeIsAccessible(request, source);
            LOG.debug("SECURITY Users is accessing an open cube");
            model.addAttribute("requireLogin", false);
        }
    }

    private void checkIfCubeIsAccessible(AbstractRequest request, HydraSource source) {
        if (source.isCubeAccessDenied() && request instanceof CubeRequest) {
            LOG.debug("SECURITY Attempt to access data with token " + request.toDataUrl());
            if (!accessToken.canAccess(request.toDataUrl())) {
                throw new CubeAccessDeniedException(request.getEnv(), request.getCube());
            }
        }
    }

    protected void loadMetadata(HydraSource source) {
        if (!source.isMetadataLoaded()) {
            source.loadMetadata();
        }
        LOG.debug("metadata loaded");
    }

    protected final String sessionAttributeName(String env, String cube) {
        return env + "/" + cube;
    }

    protected String getCsrf() {
        return accessToken.getCsrf();
    }

    protected void validateCsrf(String csrf) {
        if (csrf == null || !accessToken.getCsrf().equals(csrf)) {
            throw new IllegalArgumentException("Invalid csrf token");
        }
    }

    protected boolean isExternalAddress(String remoteIp) {
        InetAddress address;
        InetAddress localhost;
        try {
            address = InetAddress.getByName(remoteIp);
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOG.error("Failed to test ip-address", e);
            return true;
        }
        boolean isLocalAddress = address.isSiteLocalAddress() || address.isLoopbackAddress() || address.equals(localhost);
        return !isLocalAddress;
    }

    protected void recreateSession(HttpServletRequest request) {
    	HttpSession session = request.getSession();
        Map<String, Object> sessionAttributes = Maps.newHashMap();
        Enumeration<String> sessionKeys = session.getAttributeNames();
        while (sessionKeys.hasMoreElements()) {
            String key = sessionKeys.nextElement();
            sessionAttributes.put(key , session.getAttribute(key));
        }
        session.invalidate();
    	session = request.getSession(true);
        for (Map.Entry<String, Object> sessionAttribute : sessionAttributes.entrySet()) {
            session.setAttribute(sessionAttribute.getKey(), sessionAttribute.getValue());
        }
    }

}
