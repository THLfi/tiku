package fi.thl.pivot.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Preconditions;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.web.tools.FindNodes;

/**
 * Provides common functionality for all cube controllers including
 * ModelAttribute mapping of request parameters to POJO and cube service
 * building.
 * 
 * @author aleksiyrttiaho
 *
 */
public abstract class AbstractCubeController extends AbstractController {

    private static final String FACT_PREFIX = "fact_";
    private final Logger logger = LoggerFactory.getLogger(AbstractCubeController.class);

    /*
     * Handles common parameters and creates a POJO out of them so that handling
     * parameters in done in DRYer way.
     */
    @ModelAttribute
    public CubeRequest createRequest(@PathVariable String subject, @PathVariable String hydra, @PathVariable String locale, @PathVariable String cube,
            @PathVariable String env, @RequestParam(value = "run_id", required = false, defaultValue = "latest") String runId,
            @RequestParam(value = "row", required = false, defaultValue = "") List<String> rowHeaders,
            @RequestParam(value = "column", required = false, defaultValue = "") List<String> columnHeaders,
            @RequestParam(value = "filter", required = false, defaultValue = "") List<String> filterValues,
            @RequestParam(value = "measure", required = false, defaultValue = "") List<String> measureValues,
            @RequestParam(value = "search", required = false, defaultValue = "su") String searchType, 
            @RequestParam(required = false) String fo,
            @RequestParam(required = false) String fz, 
            @RequestParam(value = "sort", required = false) String sortNode,
            @RequestParam(value = "mode", required = false, defaultValue = "desc") String sortMode,
            @RequestParam(value = "sc", required = false, defaultValue = "") String showCodes,
            @RequestParam(value = "ci", required=false) String ci, 
            @RequestParam(value = "n", required=false) String n) {
        CubeRequest cr = new CubeRequest();

        cr.setSubject(subject);
        cr.setHydra(hydra);
        cr.setRunId(runId);
        cr.setCube(FACT_PREFIX + cube);
        cr.setEnv(env);

        cr.setRowHeaders(rowHeaders);
        cr.setColumnHeaders(columnHeaders);
        cr.setMeasureValues(measureValues);
        cr.setFilterValues(filterValues);
        cr.setFilterZeroes(fz);
        cr.setFilterEmptyCells(fo);
        cr.setSortMode(sortMode);
        cr.setSortNode(sortNode);
        cr.setSearchType(searchType);
        cr.setShowCodes(showCodes);
        cr.setLocale(locale);
        cr.setCi(ci);
        cr.setN(n);

        return cr;
    }

    protected FindNodes.SearchType resolveSearchType(String searchType) {
        if ("id".equals(searchType)) {
            return FindNodes.SearchType.IDENTIFIER;
        } else {
            return FindNodes.SearchType.SURROGATE;
        }
    }

    protected CubeService createCube(CubeRequest cubeRequest, FindNodes.SearchType searchType, Model model) {
        return createCube(cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest, searchType, model);
    }

    @Monitored
    protected CubeService createCube(String env, String cube, CubeRequest cubeRequest, FindNodes.SearchType searchType, Model model) {

        Preconditions.checkNotNull(cubeRequest.getRowHeaders());
        Preconditions.checkNotNull(cubeRequest.getColumnHeaders());

        CubeService service = new CubeService(amorDao.loadSource(env, cube), cubeRequest);

        service.setLocale(cubeRequest.getLocale());

        service.setSortIndex(parseSortIndex(cubeRequest.getSortNode()));
        service.setSortedByColumns(parseSortTarget(cubeRequest.getSortNode()));
        service.setAscendingOrder("asc".equals(cubeRequest.getSortMode()));

        service.setRowHeaders(cubeRequest.getRowHeaders());
        service.setColumnHeaders(cubeRequest.getColumnHeaders());
        service.setFilterValues(cubeRequest.getFilterValues());
        service.setMeasureValues(cubeRequest.getMeasureValues());
        service.setZeroValuesFiltered(null != cubeRequest.getFilterZeroes());
        service.setEmptyValuesFiltered(null != cubeRequest.getFilterEmptyValues());

        service.setSearchType(searchType);

        model.addAttribute("lang", cubeRequest.getLocale().getLanguage());
        logger.debug("Creating cube");

        if (null != service.getSource()) {
            logger.debug("Found cube " + cube);
            loadMetadata(service.getSource());
            checkLoginRequirements(cubeRequest, model, service.getSource());
            service.createCube();
            service.assignModelAttributes(model, cubeRequest);
            logger.debug("redirecting to user interface");
        }
        return service;
    }

    protected boolean parseSortTarget(String sortNode) {
        return null != sortNode && sortNode.startsWith("c");
    }

    protected int parseSortIndex(String sortNode) {
        return null != sortNode && sortNode.matches("^[cr]\\d+$") ? Integer.parseInt(sortNode.substring(1)) : -1;
    }
}
