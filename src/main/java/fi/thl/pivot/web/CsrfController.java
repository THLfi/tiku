package fi.thl.pivot.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CsrfController extends AbstractController {

    private static final Logger LOG = Logger.getLogger(CsrfController.class);

    /**
     * End point to get csrf token bound to current session. Adds support for api-login from internet.
     * @return csrf token bound to current session.
     */
    @RequestMapping(value = "/csrf", produces = "text/plain", method = RequestMethod.GET)
    @ResponseBody
    public String getCsrf(HttpSession session, HttpServletRequest request) {
        LOG.debug(String.format("RETRIEVE CSRF OF SESSION %s [%s]", session.getId(), request.getRemoteAddr()));
        return getCsrf();
    }

}
