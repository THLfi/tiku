package fi.thl.pivot.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

/**
 * This controller is intended to provide backwards compatibility with in the
 * alpha versions of this application. Currently TEAViisari application relies
 * on this version of the API
 * 
 * @author aleksiyrttiaho
 *
 */
@Controller
@RequestMapping("/{env}/api")
public class DeprecatedApiController {

    @ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
    @RequestMapping(value = "/{cube:.+}/dimensions", produces = "application/javascript")
    public String getDimensionsAsJson(WebRequest req, @PathVariable("env") String env, @PathVariable("cube") String cube, Model model) {
        String[] cubeParts = cube.split("\\.");
        String lang = req.getParameter("l");
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(env);
        sb.append("/").append(lang == null ? "fi" : lang);
        sb.append("/").append(cubeParts[0]);
        sb.append("/").append(cubeParts[1]);
        sb.append("/").append(cubeParts[2]);
        sb.append(".dimensions.json?");

        Escaper escaper = UrlEscapers.urlFragmentEscaper();
        for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            for (String v : e.getValue()) {
                sb.append(escaper.escape(e.getKey())).append("=").append(escaper.escape(v)).append("&");
            }
        }

        return "redirect:" + sb.toString();
    }

    @ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
    @RequestMapping(value = "/{cube:.+}/cube", produces = "application/javascript")
    public String getDataAsJsonStat(WebRequest req, @PathVariable("env") String env, @PathVariable("cube") String cube, Model model) {
        String[] cubeParts = cube.split("\\.");
        String lang = req.getParameter("l");
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(env);
        sb.append("/").append(lang == null ? "fi" : lang);
        sb.append("/").append(cubeParts[0]);
        sb.append("/").append(cubeParts[1]);
        sb.append("/").append(cubeParts[2]);
        sb.append(".js?");

        Escaper escaper = UrlEscapers.urlFragmentEscaper();
        for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            for (String v : e.getValue()) {
                sb.append(escaper.escape(e.getKey())).append("=").append(escaper.escape(v)).append("&");
            }
        }
        sb.append("&search=id");

        return "redirect:" + sb.toString();
    }
}
