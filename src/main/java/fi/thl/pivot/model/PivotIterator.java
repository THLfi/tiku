package fi.thl.pivot.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * PivotIterator is used to iterate over a multidimensional table and generate
 * {@link PivotCell} instance for all cells in table
 * 
 * @author aleksiyrttiaho
 *
 */
final class PivotIterator implements Iterator<PivotCell> {

    private final Pivot pivot;
    private int columnCount;
    private int rowCount;

    private int currentRow = 0;
    private int currentColumn = 0;
    
    private List<List<Integer>> indices = new ArrayList<>(); 
    
    PivotIterator(Pivot pivot, int rowCount, int columnCount) {
        this.pivot = pivot;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        addNodesToIndex();
    }

    @Override
    public boolean hasNext() {
        return currentRow < rowCount && currentColumn < columnCount;
    }

    @Override
    public PivotCell next() {
        PivotCell cell = this.pivot.getCellAt(currentRow, currentColumn);
        cell.setIndices(indices);
        if (++currentColumn == columnCount) {
            ++currentRow;
            currentColumn = 0;
        }
        return cell;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
   
    /**
     * Nodes are added to an index to help json stat generation of sparse
     * matrices
     */
    private void addNodesToIndex() {
        for (PivotLevel l : pivot.getRows()) {
            addNodesToIndex(l);
        }
        for (PivotLevel l : pivot.getColumns()) {
            addNodesToIndex(l);
        }
    }

    private void addNodesToIndex(PivotLevel l) {
        List<Integer> index = new ArrayList<>();
        for (DimensionNode n : l.getNodes()) {
            index.add(n.getSurrogateId());
        }
        indices.add(index);
    }
}