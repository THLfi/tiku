package fi.thl.pivot.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.exception.CubeNotFoundException;
import fi.thl.pivot.export.XlsxExporter;

@Controller
public class CubeXlsxController extends AbstractCubeController {

    private static final Logger LOG = Logger.getLogger(CubeXlsxController.class);

    @Autowired
    private MessageSource messageSource;

    @Monitored
    @RequestMapping(value = "/{env}/{locale}/{subject}/{hydra}/fact_{cube}.xlsx", headers="Accept=*/*", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void displayCubeAsXLSX(@ModelAttribute CubeRequest cubeRequest, Model model, HttpServletResponse resp) throws CubeNotFoundException, IOException {
        LOG.debug(String.format("XLSX cube requested %s %s %s", cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest.toString()));

        if(cubeRequest.getRowHeaders().isEmpty() || cubeRequest.getColumnHeaders().isEmpty()) {
            throw new RuntimeException("Invalid selection");
        }
        CubeService cs = createCube(cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest, resolveSearchType(cubeRequest.getSearchType()), model);
        if (cs.isCubeCreated()) {
            logSource.logDisplayEvent(cubeRequest.getCube(), cubeRequest.getEnv(), cs, "xlsx");
            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-disposition", "attachment; filename=" + cubeRequest.getCube() + ".xlsx");
            model.addAttribute("cube", cubeRequest.getCube());
            model.addAttribute("updated", cs.getSource().getRunDate());
            model.addAttribute("isOpenData", cs.getSource().isOpenData());
            if(cubeRequest.getShowCodes().length() > 0) {
                model.addAttribute("sc", cubeRequest.getShowCodes());
            }
            
            new XlsxExporter(cubeRequest.getUiLanguage(), messageSource).export(model, resp.getOutputStream());
        } else {
            throw new CubeNotFoundException();
        }
    }
}
