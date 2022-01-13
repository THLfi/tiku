package fi.thl.pivot.web;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import fi.thl.pivot.model.IDimensionNode;
import fi.thl.pivot.web.tools.NonceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.exception.CubeNotFoundException;
import fi.thl.pivot.model.Report;
import fi.thl.pivot.summary.model.Presentation;
import fi.thl.pivot.summary.model.Selection;
import fi.thl.pivot.summary.model.Summary;
import fi.thl.pivot.summary.model.hydra.HydraDataPresentation;
import fi.thl.pivot.summary.model.hydra.HydraFilter;
import fi.thl.pivot.summary.model.hydra.HydraSummary;
import fi.thl.pivot.summary.model.hydra.HydraTablePresentation;

@Controller
@RequestMapping("/{env}/{locale}/{subject}/{hydra}/summary_{summaryId}")
public class SummaryController extends AbstractController {

    private final Logger logger = LoggerFactory.getLogger(SummaryController.class);

    public static class SummaryRequest extends AbstractRequest {

        private String geometry;

        public String getSummaryUrl() {
            return getSummaryUrl(locale.getLanguage());
        }

        public String getSummaryUrl(String language) {
            return getSummaryUrl(language, cube);
        }

        public String getSummaryUrlWithId(String id) {
            return getSummaryUrl(locale.getLanguage(), id);
        }

        public String getSummaryUrl(String language, String id) {
            return String.format("%s/%s/%s/%s/summary_%s", env, language, subject, hydra, id);
        }

        public void setGeometry(String geo) {
            this.geometry = geo;
        }

        public String getGeometry() {
            return geometry;
        }
    }

    @Autowired
    private AccessTokens tokens;

    @ModelAttribute
    public SummaryRequest getSummaryRequest(@PathVariable String env, @PathVariable String locale,
            @PathVariable String subject, @PathVariable String hydra,
            @PathVariable String summaryId, @RequestParam(value ="geo", required=false) String geo,
            @RequestParam(value = "run_id", required = false, defaultValue = "latest") String runId) {
        SummaryRequest sr = new SummaryRequest();
        sr.setEnv(env);
        sr.setLocale(locale);
        sr.setSubject(subject);
        sr.setHydra(hydra);
        sr.setCube(summaryId);
        sr.setRunId(runId);
        sr.setGeometry(geo);
        return sr;
    }

    @RequestMapping(value = "", produces = "text/html;charset=UTF-8", method = RequestMethod.POST)
    public String loginToCube(@ModelAttribute SummaryRequest summaryRequest, HttpServletRequest request, @RequestParam String password, @RequestParam(required = false) String csrf) {
        if (isExternalAddress(request.getRemoteAddr()) || csrf != null) {
        	validateCsrf(csrf);
        }
        Summary summary = amorDao.loadSummary(summaryRequest.getEnv(), summaryRequest.getCube());
        login(summaryRequest.getEnv(),
                amorDao.replaceFactInIdentifier(summaryRequest.getCube(), summary.getFactTable()), password, request);
        return "redirect:/" + summaryRequest.getSummaryUrl();
    }

    @RequestMapping(value = "/logout", produces = "text/html;charset=UTF-8")
    public String loginToCube(@ModelAttribute SummaryRequest summaryRequest, HttpServletRequest request) {
        logout(request.getSession());
        return "redirect:/" + summaryRequest.getSummaryUrl();
    }

    @Monitored
    @RequestMapping(value = "", produces = "text/html;charset=UTF-8", method = RequestMethod.GET)
    public String displaySummary(@ModelAttribute SummaryRequest summaryRequest, WebRequest request, Model model, HttpServletRequest servletRequest)
            throws CubeNotFoundException {

        logger.info("ACCESS Rendering summary " + summaryRequest.getCube());

        // Load summary and hydra definitions for the current summary
        // If the source does not exists then display 404 for the user
        Summary summary = amorDao.loadSummary(summaryRequest.getEnv(), summaryRequest.getCube());
        final String cubeId = amorDao.replaceFactInIdentifier(summaryRequest.getCube(), summary.getFactTable());
        HydraSource source = amorDao.loadSource(summaryRequest.getEnv(), cubeId);

        if (null == source) {
            logger.debug("Cube not found for summary " + cubeId);
            throw new CubeNotFoundException();
        }
        loadMetadata(source);

        model.addAttribute("supportedLanguages", source.getLanguages());
        model.addAttribute("cubeRequest", summaryRequest);

        checkLoginRequirements(summaryRequest, model, source, cubeId);
        HydraSummary hSummary = constructHydraSummary(request, summary, source);
        hSummary.setGeometry(summaryRequest.getGeometry());
        List<IDimensionNode> drillNodes = determineDrillNodes(request, summary, hSummary);

        addAccessTokenForSummaryIfCubeAccessIsDenied(source, hSummary);

        // Set model attributes for the template engine
        model.addAttribute("drillNodes", drillNodes);
        model.addAttribute("lang", summaryRequest.getUiLanguage());
        model.addAttribute("factName", source.getName());
        model.addAttribute("summary", hSummary);
        model.addAttribute("env", summaryRequest.getEnv());
        model.addAttribute("summaryId", summaryRequest.getCube());
        model.addAttribute("cubeId", cubeId);
        model.addAttribute("runDate", source.getRunDate());
        model.addAttribute("isOpenData", source.isOpenData());
        model.addAttribute("factTable", summary.getFactTable());
        model.addAttribute("uiLanguage", summaryRequest.getLocale());
        model.addAttribute("contactInformation", source.getContactInformation());
        model.addAttribute("cspNonce", NonceGenerator.getNonce());

        model.addAttribute("reports",
                listSummariesBasedOnTheSameSubject(summaryRequest.getEnv(), summaryRequest.getCube(), source));

        logger.debug("Sending user to template summary with summary " + summaryRequest.getCube());

        return "summary";
    }

    @Monitored
    @RequestMapping(value = "/source", produces = "text/xml;charset=UTF-8", method = RequestMethod.GET)
    public @ResponseBody String displaySource(@ModelAttribute SummaryRequest summaryRequest, WebRequest request,
            Model model)
                    throws CubeNotFoundException {

        if ("deve".equals(summaryRequest.getEnv()) || "test".equals(summaryRequest.getEnv())) {
            logger.info("ACCESS Displaying source of summary " + summaryRequest.getCube());

            // Load summary and hydra definitions for the current summary
            // If the source does not exists then display 404 for the user
            Summary summary = amorDao.loadSummary(summaryRequest.getEnv(), summaryRequest.getCube());
            return summary.getSource();
        } else {
            return "";
        }
    }

    private void addAccessTokenForSummaryIfCubeAccessIsDenied(HydraSource source, HydraSummary hSummary) {
        if (source.isCubeAccessDenied()) {
            for (Presentation p : hSummary.getPresentations()) {
                if (p instanceof HydraDataPresentation) {
                    HydraDataPresentation hdp = (HydraDataPresentation) p;
                    tokens.putToken(hdp.getDataUrl());
                } else if (p instanceof HydraTablePresentation) {
                    HydraTablePresentation hdp = (HydraTablePresentation) p;
                    tokens.putToken(hdp.getDataUrl());
                }
            }
        }
    }

    private HydraSummary constructHydraSummary(WebRequest request, Summary summary, HydraSource source) {
        HydraSummary hSummary = new HydraSummary(summary, source);
        for (Selection s : hSummary.getSelections()) {
            HydraFilter f = (HydraFilter) s;
            for (int i = 0; i < f.getFilterStages().size(); ++i) {
                String[] parameters = request.getParameterValues(s.getId() + "_" + i);
                if (null != parameters) {
                    f.select(i, parameters);
                } else {
                    f.select(i, null);
                }
            }
        }
        return hSummary;
    }

    private List<IDimensionNode> determineDrillNodes(WebRequest request, Summary summary, HydraSummary hSummary) {
        List<IDimensionNode> drillNodes = Lists.newArrayList();
        if (summary.isDrillEnabled()) {
            for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
                if (isDrillParameter(parameter)) {
                    IDimensionNode node = hSummary.drillTo(drillDimension(parameter), drillNode(parameter));
                    drillNodes.add(node);
                    break;
                }
            }
        }
        return drillNodes;
    }

    private Collection<Report> listSummariesBasedOnTheSameSubject(String env, String summaryId, HydraSource source) {
        final Set<String> reports = Sets.newHashSet();
        return Collections2.filter(
                amorDao.listReports(env, summaryId.substring(0, summaryId.indexOf(".")), source.getRunid()),
                new Predicate<Report>() {

                    @Override
                    public boolean apply(Report report) {
                        String id = String.format("%s.%s.%s", report.getHydra(), report.getFact(),
                                report.getType().toString());
                        if (reports.contains(id)) {
                            return false;
                        } else {
                            reports.add(id);
                            return true;
                        }
                    }
                });
    }

    private boolean isDrillParameter(Map.Entry<String, String[]> parameter) {
        return parameter.getKey().startsWith("drill-") && parameter.getValue().length == 1;
    }

    private String drillNode(Map.Entry<String, String[]> parameter) {
        return parameter.getValue()[0];
    }

    private String drillDimension(Map.Entry<String, String[]> parameter) {
        return parameter.getKey().substring(6, parameter.getKey().length());
    }

}
