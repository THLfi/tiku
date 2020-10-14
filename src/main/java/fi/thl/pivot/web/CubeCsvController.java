package fi.thl.pivot.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.exception.CubeNotFoundException;
import fi.thl.pivot.export.CsvExporter;

@Controller
public class CubeCsvController extends AbstractCubeController {

    private final Logger logger = LoggerFactory.getLogger(CubeCsvController.class);

    @Monitored
    @RequestMapping(value = "/{env}/{locale}/{subject}/{hydra}/fact_{cube}.csv", headers="Accept=*/*", produces = "text/csv;charset=UTF-8")
    public void displayCubeAsCSV(@ModelAttribute CubeRequest cubeRequest, Model model, HttpServletResponse resp) throws IOException {
    	logger.debug(String.format("ACCESS CSV cube requested %s %s %s ", cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest.toString()));

        CubeService cs = createCube(cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest, resolveSearchType(cubeRequest.getSearchType()), model);
        if (cs.isCubeCreated()) {
            logSource.logDisplayEvent(cubeRequest.getCube(), cubeRequest.getEnv(), cs, "csv");

            //resp.setHeader("Content-disposition", "attachment; filename=" + cubeRequest.getCube() + ".csv");

            model.addAttribute("cube", cubeRequest.getCube());
            model.addAttribute("updated", cs.getSource().getRunDate());
            model.addAttribute("isOpenData", cs.getSource().isOpenData());
            
            if(cubeRequest.getShowCodes().length() > 0) {
                model.addAttribute("sc", cubeRequest.getShowCodes());
            }

            new CsvExporter().export(model, resp.getOutputStream());

        } else {
            throw new CubeNotFoundException();
        }
    }

}
