package fi.thl.pivot.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.export.JsonStatExporter;

/**
 * Controller for API requests (non-UI) of pivot
 * 
 * @author aleksiyrttiaho
 *
 */
@Controller
@RequestMapping("/{env}/{locale}/{subject}/{hydra}")
public class CubeApiController extends AbstractCubeController {

    private static final Logger LOG = Logger.getLogger(CubeApiController.class);

    @RequestMapping(value = "/fact_{cube}.js", produces = "text/javascript")
    public String displayCubeAsJsonStatP(@ModelAttribute CubeRequest cubeRequest, Model model, HttpServletResponse resp) {
        LOG.debug(String.format("ACCESS JSON-STAT cube requested %s %s %s", cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest.toString()));

        CubeService cs = createCube(cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest, resolveSearchType(cubeRequest.getSearchType()), model);
        if (cs.isCubeCreated()) {
            logSource.logDisplayEvent(cubeRequest.getEnv(), cubeRequest.getCube(), cs, "json");
            model.addAttribute("surrogate", "su".equals(cubeRequest.getSearchType()));
            model.addAttribute("jsonp", true);
            resp.setContentType("text/javascript");
            resp.setCharacterEncoding("utf-8");
            return "cube.jsonstat";
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("cube", cubeRequest.getCube());
            return "no-cube-found";
        }
    }

    @Monitored
    @RequestMapping(value = "/fact_{cube}.json", produces = "application/json")
    public void displayCubeAsJsonStat(@ModelAttribute CubeRequest cubeRequest, Model model, HttpServletResponse resp, OutputStream out) throws IOException {
        LOG.debug(String.format("ACCESS JSON-STAT cube requested %s %s %s", cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest.toString()));

        try {
            CubeService cs = createCube(cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest, resolveSearchType(cubeRequest.getSearchType()), model);

            if (cs.isCubeCreated()) {
                logSource.logDisplayEvent(cubeRequest.getCube(), cubeRequest.getEnv(), cs, "json");
                model.addAttribute("jsonp", false);
                model.addAttribute("surrogate", "su".equals(cubeRequest.getSearchType()));
                // return "cube.jsonstat";
                resp.setContentType("application/json");
                resp.setCharacterEncoding("utf-8");
                new JsonStatExporter().export(model, out);
            } else {
                LOG.debug("Cube is not created");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            LOG.debug("Cube access is forbidden", e);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

        }
    }

    @Monitored
    @RequestMapping(value = "/fact_{cube}.dimensions.json", produces = "text/javascript")
    public String displayDimensionsAsJson(@ModelAttribute CubeRequest cubeRequest, Model model, HttpServletResponse resp) {
        LOG.debug(String.format("ACCESS Cube dimensions requested %s %s", cubeRequest.getEnv(), cubeRequest.getCube()));

        HydraSource source = amorDao.loadSource(cubeRequest.getEnv(), cubeRequest.getCube());
        if (null != source) {
            loadMetadata(source);
            checkLoginRequirements(cubeRequest, model, source);
            model.addAttribute("dimensions", source.getDimensionsAndMeasures());
            model.addAttribute("lang", cubeRequest.getLocale());
            return "cube.dimensions";
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("cube", cubeRequest.getCube());
            return "no-cube-found";
        }
    }

}
