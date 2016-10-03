package fi.thl.pivot;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.log4j.Logger;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Pivot;
import fi.thl.pivot.model.PivotCell;
import fi.thl.pivot.model.PivotLevel;

/**
 * Simple pivot rendered that provides a csv output with N row and M column
 * headers. This is create to provide a simple result that can be used for
 * testing expected and actual outputs.
 * 
 * @author aleksiyrttiaho
 *
 */
public class TextView {

    private final Logger log = Logger.getLogger(getClass());

    private final Pivot pivot;
    private final String language;

    public TextView(Pivot pivot) {
        this(pivot, "en");
    }

    public TextView(Pivot pivot, String language) {
        this.pivot = pivot;
        this.language = language;
    }

    public void display(OutputStream out) {
        final PrintWriter writer = new PrintWriter(out, true);
        columnHeaders(writer);
        int lastRowNum = -1;
        Iterator<PivotCell> pi = pivot.iterator();
        while (pi.hasNext()) {
            PivotCell cell = pi.next();
            if (null == cell) {
                throw new IllegalStateException("Null cell detected");
            }
            log.trace(cell.getRowNumber() + ":" + cell.getColumnNumber());

            if (lastRowNum != cell.getRowNumber()) {
                if (lastRowNum >= 0) {
                    writer.println();
                }
                lastRowNum = cell.getRowNumber();
                for (int level = 0; level < pivot.getRows().size(); ++level) {
                    appendNodeLabel(writer,
                            pivot.getRowAt(level, cell.getRowNumber()));
                }
            }

            String value = cell.getValue();
            writer.print(value != null ? value : "");
            writer.print(";");
        }
        writer.flush();

    }

    private void columnHeaders(PrintWriter writer) {

        int[] nodesInLevel = calculateNumberOfNodesInEachLevel();
        int index = 0;

        // Create a header row for each column header level
        for (PivotLevel cl : pivot.getColumns()) {
            // Prepend an empty cell for each row header level
            for (int rowLevel = 0; rowLevel < pivot.getRows().size(); ++rowLevel) {
                writer.print(";");
            }

            for (DimensionNode node : cl) {

                // If last column header level, we can simply add the
                // node
                if (index + 1 == nodesInLevel.length) {
                    appendNodeLabel(writer, node);
                } else {
                    // When there is at least one level to go
                    // the column header node must be repeated for each
                    // sublevel node
                    for (int i = index + 1; i < nodesInLevel.length; ++i) {
                        for (int j = 0; j < nodesInLevel[i]; ++j) {
                            appendNodeLabel(writer, node);
                        }
                    }
                }
            }
            writer.println();
            ++index;
        }
    }

    private void appendNodeLabel(PrintWriter writer, DimensionNode node) {
        writer.print(nodeLabel(node));
        writer.print(";");
    }

    private String nodeLabel(DimensionNode node) {
        return node != null ? node.getLabel().getValue(language) : "";
    }

    private int[] calculateNumberOfNodesInEachLevel() {
        int index = 0;
        int[] nodesInLevel = new int[pivot.getColumns().size()];
        for (PivotLevel level : pivot.getColumns()) {
            nodesInLevel[index++] = level.size();
        }
        return nodesInLevel;
    }

}
