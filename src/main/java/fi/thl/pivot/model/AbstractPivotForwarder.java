package fi.thl.pivot.model;

import java.util.Iterator;
import java.util.List;

public class AbstractPivotForwarder implements Pivot {

    Pivot delegate;

    protected AbstractPivotForwarder(Pivot delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<PivotLevel> getColumns() {
        return delegate.getColumns();
    }

    @Override
    public List<PivotLevel> getRows() {
        return delegate.getRows();
    }

    @Override
    public int getColumnCount() {
        return delegate.getColumnCount();
    }

    @Override
    public Iterator<PivotCell> iterator() {
        return new PivotIterator(this, getRowCount(), getColumnCount());
    }

    @Override
    public int getRowCount() {
        return delegate.getRowCount();
    }

    @Override
    public PivotCell getCellAt(int row, int column) {
        return new PivotCellForwarder(delegate.getCellAt(row, column), row, column);
    }

    @Override
    public IDimensionNode getColumnAt(int level, int column) {
        return delegate.getColumnAt(level, column);
    }

    @Override
    public IDimensionNode getRowAt(int level, int row) {
        return delegate.getRowAt(level, row);
    }

    @Override
    public boolean isFirstColumn(int column) {
        return delegate.isFirstColumn(column);
    }

    @Override
    public boolean isColumn(int column, int targetColumn) {
        return delegate.isColumn(column, targetColumn);
    }

    @Override
    public int getColumnNumber(int column) {
        return delegate.getColumnNumber(column);
    }
    
    @Override
    public void filterCellAt(int row, int column) {
        delegate.filterCellAt(row, column);
    }

}
