package fi.thl.pivot.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

import fi.thl.pivot.annotation.Monitored;
import fi.thl.pivot.exception.CubeNotFoundException;
import fi.thl.pivot.export.PdfExporter;
import fi.thl.pivot.web.tools.MessageSourceWrapper;

@Controller
public class CubePdfController extends AbstractCubeController {

    private static final int MAXIMUM_COLUMNS_IN_TABLE = 10;
    private static final Logger LOG = Logger.getLogger(CubePdfController.class);

    @Autowired
    private MessageSource messageSource;

    /**
     * <p>
     * Generates a PDF version of the cube. The PDF version is printed on a A4
     * sheet in landscape mode Table is split into multiple pages vertically and
     * all headers are printed on top of each page.
     * </p>
     * 
     * <p>
     * To achieve horizontal table splitting to multiple pages a maximum column
     * number per page is defined as {@link MAXIMUM_COLUMNS_IN_TABLE}. The
     * number of row levels is taken into account. The table is split into N
     * tables, where N is the (number of columns / maximum columns in table).
     * Each of these tables are printed after each other.
     * </p>
     * 
     * <p>
     * Each page contains a header where the name of the cube and the page
     * number is display. Each page contains a footer containing copyright
     * notices.
     * </p>
     * 
     * 
     * @param env
     * @param cube
     * @param cubeRequest
     * @param sc
     * @param searchType
     * @param model
     * @param resp
     * @throws CubeNotFoundException
     * @throws IOException
     */
    @Monitored
    @RequestMapping(value = "/{env}/{locale}/{subject}/{hydra}/fact_{cube}.pdf", headers="Accept=*/*",  produces = "application/pdf")
    public void displayCubeAsPDF(@ModelAttribute CubeRequest cubeRequest, Model model, HttpServletResponse resp) throws CubeNotFoundException, IOException {
        LOG.debug(String.format("PDF cube requested %s %s %s", cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest.toString()));

        CubeService cs = createCube(cubeRequest.getEnv(), cubeRequest.getCube(), cubeRequest, resolveSearchType(cubeRequest.getSearchType()), model);
        if (cs.isCubeCreated()) {
            logSource.logDisplayEvent(cubeRequest.getCube(), cubeRequest.getEnv(), cs, "pdf");
            resp.setContentType("application/pdf");

            model.addAttribute("env", cubeRequest.getEnv());
            model.addAttribute("cube", cubeRequest.getCube());
            model.addAttribute("rowParams", cubeRequest.getRowHeaders());
            model.addAttribute("colParams", cubeRequest.getColumnHeaders());

            model.addAttribute("isOpenData", cs.getSource().isOpenData());
            model.addAttribute("updated", cs.getSource().getRunDate());
            model.addAttribute("sc", cubeRequest.getShowCodes());

            model.addAttribute("tableBounds", calculateSplitTableBounds(cs));
            model.addAttribute("messageSource", new MessageSourceWrapper(messageSource, "fi"));

            new PdfExporter(cubeRequest.getUiLanguage(), freemarker).export(model, resp.getOutputStream());
        } else {
            throw new CubeNotFoundException();
        }

    }

    /**
     * Wide tables do not fit into a A4 sheet. Flying saucer cannot split wide
     * tables horizontally so we must do that ourselves. As a solution we
     * calculate column bounds that determine how many columns can be shown in a
     * single table. The overflowing columns will be displayed in a separate
     * table after the first table has been completely written.
     * 
     * @param cs
     * @return List of column indices that should be used to split a wide table
     *         in multiple narrower tables
     */
    private List<Integer[]> calculateSplitTableBounds(CubeService cs) {
        int columnsInTable = MAXIMUM_COLUMNS_IN_TABLE - cs.getPivot().getRows().size();
        int numberOfSubtables = cs.getPivot().getColumnCount() / columnsInTable;
        List<Integer[]> tableBounds = Lists.newArrayList();
        for (int i = 0; i <= numberOfSubtables; ++i) {
            tableBounds.add(new Integer[] { i * columnsInTable, Math.min((i + 1) * columnsInTable, cs.getPivot().getColumnCount()) });
        }
        return tableBounds;
    }

}
