package fi.thl.pivot.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fi.thl.pivot.util.Functions;

public class OrderablePivot extends AbstractPivotForwarder {

    public static enum SortBy {
        Row, Column
    }

    public static enum SortMode {
        Ascending, Descending
    }

    private final Logger logger = LoggerFactory.getLogger(OrderablePivot.class);

    private int sortIndex;
    private SortMode sortMode;

    private List<Integer> columnOrder;
    private List<Integer> rowOrder;

    public OrderablePivot(Pivot delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public boolean isFirstColumn(int column) {
        return isColumn(column, 0);
    }

    @Override
    public boolean isColumn(int column, int targetColumn) {
        return columnOrder.indexOf(column) == targetColumn;
    }

    @Override
    public int getColumnNumber(int column) {
        return delegate.getColumnNumber(columnOrder.get(column));
    }

    @Override
    public PivotCell getCellAt(int row, int column) {
        return new PivotCellForwarder(delegate.getCellAt(rowOrder.get(row), columnOrder.get(column)), row, column);
    }

    @Override
    public IDimensionNode getRowAt(int level, int row) {
        return super.getRowAt(level, rowOrder.get(row));
    }

    @Override
    public IDimensionNode getColumnAt(int level, int column) {
        return super.getColumnAt(level, columnOrder.get(column));
    }

    public void sortBy(final int sortIndex, SortBy sortBy, SortMode sortMode) {
        logger.debug(String.format("Sorting pivot by column : %s , %d ascending: %s", sortBy, sortIndex, sortMode));
        Preconditions.checkNotNull("Missing sortBy argument", sortBy);
        Preconditions.checkNotNull("Missing sortMode argument", sortMode);

        this.sortIndex = sortIndex;
        this.sortMode = sortMode;

        clearCurrentSortOrder();
        List<Integer> newColumnOrder = Lists.newArrayList(Functions.setUpto(getColumnCount()));
        List<Integer> newRowOrder = Lists.newArrayList(Functions.setUpto(getRowCount()));
        applySort(sortBy, newColumnOrder, newRowOrder);
        setSortOrder(newColumnOrder, newRowOrder);

    }

    private void setSortOrder(List<Integer> newColumnOrder, List<Integer> newRowOrder) {
        this.rowOrder = newRowOrder;
        this.columnOrder = newColumnOrder;
    }

    private void applySort(SortBy sortBy, List<Integer> newColumnOrder, List<Integer> newRowOrder) {
        switch (sortBy) {
        case Column:
            sortByColumn(newRowOrder);
            break;
        case Row:
            sortByRow(newColumnOrder);
            break;
        }
    }

    private void clearCurrentSortOrder() {
        this.columnOrder = null;
        this.rowOrder = null;
    }

    private void sortByRow(List<Integer> columnOrder) {
        Collections.sort(columnOrder, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                PivotCell c1 = delegate.getCellAt(sortIndex, o1);
                PivotCell c2 = delegate.getCellAt(sortIndex, o2);
                return c1.compareTo(c2);
            }
        });
        if (SortMode.Descending.equals(sortMode)) {
            Collections.reverse(columnOrder);
        }
    }

    private void sortByColumn(List<Integer> rowOrder) {
        Collections.sort(rowOrder, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                PivotCell c1 = delegate.getCellAt(o1, sortIndex);
                PivotCell c2 = delegate.getCellAt(o2, sortIndex);
                return c1.compareTo(c2);
            }
        });
        if (SortMode.Descending.equals(sortMode)) {
            Collections.reverse(rowOrder);
        }
    }

}
