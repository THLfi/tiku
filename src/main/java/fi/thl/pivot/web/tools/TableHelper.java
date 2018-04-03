package fi.thl.pivot.web.tools;

import fi.thl.pivot.model.IDimensionNode;
import fi.thl.pivot.model.Pivot;

public class TableHelper {

    private Pivot pivot;

    public TableHelper(Pivot pivot) {
        this.pivot = pivot;
    }

    /**
     * Counts the number of columns where the column header
     * and all its parents keeps the same.
     * @param level
     * @param column
     * @return
     */
    public int getColumnSpanAt(int level, int column) {
        IDimensionNode[] levels = new IDimensionNode[level + 1];
        for (int l = 0; l < levels.length; ++l) {
            levels[l] = pivot.getColumnAt(l, column);
        }
        int i = column + 1;
        while (i < pivot.getColumnCount()) {
            for (int l = 0; l < levels.length; ++l) {
                if (levels[l].getSurrogateId() != pivot.getColumnAt (l, i).getSurrogateId()) {
                    return i - column;
                };
            }
            ++i;
        }
        return i - column;
    }

    public int getRowSpanAt(int level, int row) {
        IDimensionNode[] levels = new IDimensionNode[level + 1];
        for (int l = 0; l < levels.length; ++l) {
            levels[l] = pivot.getRowAt(l, row);
        }
        int i = row + 1;
        while (i < pivot.getRowCount()) {
            for (int l = 0; l < levels.length; ++l) {
                // WARNING: Assumption of identity
                if (levels[l].getSurrogateId() != pivot.getRowAt(l, i).getSurrogateId()) {
                    return i - row;
                };
            }
            ++i;
        }
        return i - row;
    }
}
