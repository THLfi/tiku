package fi.thl.pivot.web;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.exception.CubeAccessDeniedException;
import fi.thl.pivot.exception.CubeNotFoundException;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.web.tools.FindNodes;
import fi.thl.pivot.web.tools.FindNodes.SearchType;

@Controller
@RequestMapping("/{env}/{locale}/{subject}/{hydra}/fact_{cube}")
public class CubeMetaController extends AbstractCubeController {

    private static final Logger LOG = Logger.getLogger(CubeController.class);

    @Monitored
    @RequestMapping(value = "/{id}", produces = "application/javascript;charset=UTF-8", method = RequestMethod.GET)
    public String displayMetaData(@PathVariable String cube, @PathVariable String id, @ModelAttribute CubeRequest cubeRequest, HttpServletRequest request, HttpServletResponse resp, Model model)
            throws CubeNotFoundException, CubeAccessDeniedException {
        LOG.info(String.format("ACCESS HTML cube requested %s %s %s", cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest.toString()));
        
        LOG.debug(String.format("ACCESS Cube dimensions requested %s %s", cubeRequest.getEnv(), cubeRequest.getCube()));

        HydraSource source = amorDao.loadSource(cubeRequest.getEnv(), cubeRequest.getCube());
        if (null != source) {
            loadMetadata(source);
            checkLoginRequirements(cubeRequest, model, source);
            FindNodes fn = new FindNodes(source, SearchType.SURROGATE);
           
            try {
                model.addAttribute("nodes", fn.apply(id));
            } catch (IllegalArgumentException e) {
                LOG.warn("Could not find nodes for metadata with numeric id" + id);
                model.addAttribute("nodes", new ArrayList<DimensionNode>());
            }
            model.addAttribute("lang", cubeRequest.getLocale());
            model.addAttribute("env", cubeRequest.getEnv());
            model.addAttribute("lang", cubeRequest.getLocale().getLanguage());
            model.addAttribute("hydra", cubeRequest.getHydra());
            model.addAttribute("subject", cubeRequest.getSubject());
            model.addAttribute("server", request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : ":" + request.getServerPort()));
            model.addAttribute("cube", "fact_" + cube);
            
            
            resp.setContentType("application/javascript");
            return "node-meta";
        } else {
            throw new CubeNotFoundException();
        }
        
       
    }

}
