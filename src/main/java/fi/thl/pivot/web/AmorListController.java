package fi.thl.pivot.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.datasource.AmorDao;
import fi.thl.pivot.model.Report;
import fi.thl.pivot.model.Report.ReportType;
import fi.thl.pivot.model.Tuple;

/**
 * Provides user the capability of browsing cubes and summaries.
 * 
 * @author aleksiyrttiaho
 *
 */
@Controller
public class AmorListController {

    private static final Logger LOG = Logger.getLogger(AmorListController.class);

    @Autowired
    private AmorDao dao;
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @RequestMapping("/")
    @Monitored
    public String listAvailableEnvinroments() {
        return "env-list";
    }

    @RequestMapping("/{env}")
    @Monitored
    public String listAvailableReports(@PathVariable String env, Model model) {
        Preconditions.checkNotNull(model, "No model injected when calling amor list controller");
        Preconditions.checkNotNull(dao, "No dao injected when calling amor list controller ");

        if ("favicon".equals(env)) {
            return "error";
        }

        model.addAttribute("reports", dao.listReports(env));

        return "amor-list";
    }

    private Date getLatestUpdate(Collection<Report> reports) {
        Date updated = null;
        Calendar c1 = Calendar.getInstance();
        for (Report r : reports) {
            if (null == updated) {
                updated = r.getAdded();
                c1.setTime(updated);
            } else {
                Calendar c2 = Calendar.getInstance();
                c2.setTime(r.getAdded());
                if (c2.after(c1)) {
                    updated = r.getAdded();
                    c1.setTime(updated);
                }
            }
        }
        if (null == updated) {
            return new Date();
        }
        return updated;
    }

    @Monitored
    @RequestMapping("/{env}/{locale:[a-z][a-z]}/{subject}")
    public String listReportsInSubject(@PathVariable String env, @PathVariable final String subject, Model model) {
        model.addAttribute("showRestrictedView", true);
        model.addAttribute("reports", listReportsInSubject(env, subject));
        return "amor-list";
    }

    private Collection<Report> listReportsInSubject(String env, final String subject) {
        return Collections2.filter(dao.listReports(env, subject), new Predicate<Report>() {

            @Override
            public boolean apply(Report input) {
                return subject.equals(input.getSubject());
            }
        });
    }

    @RequestMapping(value = "/{env}/api/{subject}.json", produces = "application/json;charset=UTF-8")
    @Monitored
    public ResponseEntity<String> listAvailableReportsAsJson(@PathVariable String env,
            @PathVariable final String subject, Model model,
            HttpServletResponse response) {
        Preconditions.checkNotNull(model, "No model injected when calling amor list controller");
        Preconditions.checkNotNull(dao, "No dao injected when calling amor list controller ");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Collection<Report> reports = filterLatest(listReportsInSubject(env, subject));

        return createJsonStatCollection(env, subject, reports, subject);
    }

    @Monitored
    @RequestMapping("/{env}/{locale}/{subject}/{hydra}")
    public String listReportsInHydra(@PathVariable String env, @PathVariable final String subject,
            @PathVariable final String hydra, Model model) {
        model.addAttribute("showRestrictedView", true);
        model.addAttribute("reports", Collections2.filter(dao.listReports(env, subject), new Predicate<Report>() {

            @Override
            public boolean apply(Report input) {
                return subject.equals(input.getSubject()) && hydra.equals(input.getHydra());
            }
        }));
        return "amor-list";
    }

    @Monitored
    @RequestMapping(value = "/{env}/api/{subject}/{hydra}.json", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> listReportsInHydraJson(@PathVariable String env, @PathVariable final String subject,
            @PathVariable final String hydra,
            Model model, HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Collection<Report> reports = filterLatest(listReportsInHydra(env, subject, hydra));
        return createJsonStatCollection(env, subject, reports, hydra);
    }

    private Collection<Report> listReportsInHydra(String env, final String subject, final String hydra) {
        return Collections2.filter(dao.listReports(env, subject), new Predicate<Report>() {

            @Override
            public boolean apply(Report input) {
                return subject.equals(input.getSubject()) && hydra.equals(input.getHydra());
            }
        });
    }

    @RequestMapping("/{env}/api/latest")
    @Monitored
    public ResponseEntity<String> getLatestVersion(@PathVariable String env, @RequestParam String subject,
            @RequestParam String hydra,
            @RequestParam String fact) {
        try {
            Preconditions.checkNotNull(dao,

                    "No dao injected when calling amor list controller ");
            Report r = dao.loadLatestReport(env, subject, hydra, fact);

            if (null == r) {
                LOG.error(String.format("Failed to show latest version of cube (%s, %s, %s, %s) - not found", env,
                        subject, hydra, fact));
                return new ResponseEntity<String>("{\"error\":\"Not found\"}", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<String>(String.format(
                        "{\"subject\":\"%s\", \"hydra\":\"%s\", \"fact\":\"%s\", \"runid\":\"%s\", \"added\": \"%s\"}",
                        r.getSubject(), r.getHydra(), r.getFact(), r.getRunId(), df.format(r.getAdded())),
                        HttpStatus.OK);
            }
        } catch (Exception e) {
            LOG.error(
                    String.format("Failed to show latest version of cube (%s, %s, %s, %s)", env, subject, hydra, fact),
                    e);
            return new ResponseEntity<String>(
                    String.format("{\"error\":\"%s\"}", e.getMessage().replaceAll("\"", "\\\"")),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Collection<Report> filterLatest(Collection<Report> reports) {
        List<Report> filter = new ArrayList<>();
        final Set<String> closed = new HashSet<>();
        for (Report input : reports) {
            if (input.getType() == Report.ReportType.SUMMARY) {
                continue;
            }
            String canonicalName = String.format("%s.%s.%s", input.getSubject(), input.getHydra(), input.getFact());
            if (closed.contains(canonicalName)) {
                continue;
            }
            closed.add(canonicalName);
            filter.add(input);
        }
        return filter;
    }

    private ResponseEntity<String> createJsonStatCollection(String env, final String subject,
            Collection<Report> reports, String label) {
        JSONObject collection = new JSONObject();
        collection.put("version", "2.0");
        collection.put("class", "collection");
        collection.put("label", label);
        collection.put("updated", new SimpleDateFormat("yyyy-MM-dd").format(getLatestUpdate(reports)));

        JSONObject link = new JSONObject();
        JSONArray items = new JSONArray();

        for (Report report : reports) {
            if (report.getType() == ReportType.SUMMARY) {
                continue;
            }
            boolean isOpenData = false;
            boolean isCubeAccessDenied = false;

            if (dao.isProtected(env, report.getFact(), report.getRunId())) {
                continue;
            }
            List<Tuple> metadata = dao.loadCubeMetadata(env, report.getFact(), report.getRunId());
            for (Tuple t : metadata) {
                if (t.predicate.equals("opendata") && t.object.equals("1")) {
                    isOpenData = true;
                }
                if(t.predicate.equals("deny") && t.object.equals("1")) {
                    isCubeAccessDenied = true;
                }
            }
            if(isCubeAccessDenied) {
                continue;
            }

            for (Tuple t : metadata) {
                if (t.predicate.equals("name")) {
                    JSONObject r = new JSONObject();
                    r.put("class", "dataset");
                    r.put("href", String.format("https://sampo.thl.fi/pivot/%s/%s/%s/%s/%s.json", env, t.lang,
                            report.getSubject(), report.getHydra(),
                            report.getFact()));
                    items.put(r);
                    r.put("label", t.object);
                    JSONArray note = new JSONArray();
                    if (isOpenData) {
                        note.put("© Terveyden ja hyvinvoinnin laitos 2016, Creative Commons BY 4.0");
                    } else {
                        note.put("© Terveyden ja hyvinvoinnin laitos 2016");
                    }
                }
            }
        }
        link.put("item", items);
        collection.put("link", link);

        return new ResponseEntity<>(collection.toString(), HttpStatus.OK);
    }

}
