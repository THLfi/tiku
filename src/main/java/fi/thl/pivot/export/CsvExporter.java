package fi.thl.pivot.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.thl.pivot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * <p>
 * Provides a CSV exporter based on {@link Pivot} using superCSV. The CSV is
 * formatted using ; as column separator and " as quotation mark. The CSV has
 * dimensions as headers where each {@link PivotLevel} creates a new column. A
 * dimension may occur multile times as a header. Each row contains a single
 * value.
 * <p>
 * And empty CSV will have a single header name val and no values
 * <p>
 * This class is intended to be used as a throwaway class.
 * 
 * @author aleksiyrttiaho
 *
 */
public class CsvExporter {

    private static final String EMPTY_COLUMN = "";
    private final Logger logger = LoggerFactory.getLogger(CsvExporter.class);
    private int columnLevelCount;
    private int rowLevelCount;
    private Pivot pivot;
    private String lang;
    private List<String> columns = new ArrayList<>();
    private CsvListWriter writer;

    public void export(Model model, OutputStream out) throws IOException {
        try {
            initializeCsvExporter(model);
            createCsvWriter(out);
            boolean showCodes = model.asMap().containsKey("sc");
            writeHeader(showCodes);
            writeValues(showCodes);
        } finally {
            closeCsvWriter();
        }
    }

    private void initializeCsvExporter(Model model) {
        Map<String, ?> params = model.asMap();
        lang = (String) params.get("lang");
        pivot = (Pivot) params.get("pivot");
        columnLevelCount = pivot.getColumns().size();
        rowLevelCount = pivot.getRows().size();
        columns.clear();
    }

    private void createCsvWriter(OutputStream out) {
        writer = new CsvListWriter(new OutputStreamWriter(out), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
    }

    private void writeHeader(boolean showCodes) throws IOException {
        addHeaders(pivot.getColumns(), showCodes);
        addHeaders(pivot.getRows(), showCodes);
        columns.add("val");
        writer.write(columns);
    }

    private void addHeaders(List<PivotLevel> levels, boolean showCodes) {
        for (PivotLevel level : levels) {
            if(showCodes) {
                columns.add(level.getDimension().getLabel().getValue(lang));
            }
            columns.add(level.getDimension().getLabel().getValue(lang));
        }
    }

    private void writeValues(boolean showCodes) throws IOException {
        int rowNum = -1;
        int colNum = 0;
        for (PivotCell cell : pivot) {
            columns.clear();
            if (pivot.isFirstColumn(cell.getColumnNumber())) {
                rowNum++;
                colNum = 0;
            }
            valueRow(rowNum, colNum, cell, showCodes);
            writer.write(columns);
            colNum++;
        }
    }

    private void valueRow(int rowNum, int colNum, PivotCell cell, boolean showCodes) {
        for (int i = 0; i < columnLevelCount; ++i) {
            IDimensionNode node = pivot.getColumnAt(i, colNum);
            if (showCodes) {
                columns.add(node.getCode());
            }
            columns.add(node.getLabel().getValue(lang));

        }
        for (int i = 0; i < rowLevelCount; ++i) {
            IDimensionNode node = pivot.getRowAt(i, rowNum);
            if (showCodes) {
                columns.add(node.getCode());
            }
            columns.add(node.getLabel().getValue(lang));
        }

        if (null == cell.getValue()) {
            columns.add(EMPTY_COLUMN);
        } else {
            columns.add(cell.getValue().replace(",", "."));
        }
    }

    private void closeCsvWriter() {
        try {
            writer.close();
        } catch (Exception e) {
            logger.warn("Could not close CSV writer", e);
        }
    }

}
