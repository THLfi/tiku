package fi.thl.pivot.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

/**
 * Utility view the time spent on rendering a given template
 * 
 * @author aleksiyrttiaho
 *
 */
public class LoggingFreeMarkerView extends FreeMarkerView {

    @Override
    protected void doRender(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            super.doRender(model, request, response);
        } finally {
            watch.stop();
            Logger.getLogger(getClass()).debug(
                    "View rendered: " + watch.shortSummary());
        }
    }
}