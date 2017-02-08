package fi.thl.pivot.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import fi.thl.pivot.util.Functions;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class FilterablePivot extends AbstractPivotForwarder {

    private static final Logger LOG = Logger.getLogger(FilterablePivot.class);

    private static interface HeaderCallback {
        DimensionNode getHeaderAt(int level, int index);
    }

    private List<PivotLevel> filteredRows = null;
    private List<PivotLevel> filteredColumns = null;
    
    private long totalTimeSpent;
    private IntList rowIndices;
    private IntList columnIndices;
    private List<PivotLevel> rows;
    private List<PivotLevel> columns;

    public FilterablePivot(Pivot delegate) {
        super(delegate);
        this.rowIndices = Functions.listUpto(delegate.getRowCount());
        this.columnIndices = Functions.listUpto(delegate.getColumnCount());
        this.rows = delegate.getRows();
        this.columns = delegate.getColumns();
    }

    @Override
    public boolean isFirstColumn(int column) {
        return isColumn(column, 0);
    }

    @Override
    public boolean isColumn(int column, int targetColumn) {
        return columnIndices.indexOf(column) == targetColumn;
    }

    @Override
    public int getColumnNumber(int column) {
        return delegate.getColumnNumber(columnIndices.indexOf(column));
    }

    @Override
    public PivotCell getCellAt(int row, int column) {
        return new PivotCellForwarder(super.getCellAt(rowIndices.get(row), columnIndices.get(column)), row, column);
    }

    @Override
    public DimensionNode getRowAt(int level, int row) {
        return super.getRowAt(level, rowIndices.get(row));
    }

    @Override
    public DimensionNode getColumnAt(int level, int column) {
        return super.getColumnAt(level, columnIndices.get(column));
    }

    @Override
    public int getRowCount() {
        return rowIndices.size();
    }

    @Override
    public int getColumnCount() {
        return columnIndices.size();
    }

    public void applyFilter(Predicate<PivotCell> filter) {
        Preconditions.checkNotNull(filter, "Applied filter must not be null");
        applyFilters(ImmutableList.of(filter));
    }

    public void applyFilters(List<Predicate<PivotCell>> filters) {
        LOG.debug("Applying filters " + filters + " table size [" + rowIndices.size() + ", " + columnIndices.size() + "]");
        filterHiearachy();
        if (filters.isEmpty()) {
            return;
        }
        // Initially all rows and columns are filtered
        // Rows and columns are only shown if exists one
        // or more cells in that column or row where
        // the cell is not filtered.
        //
        // Note that the method may be called more
        // than once
        IntSet filteredRows = Functions.setUpto(getRowCount());
        IntSet filteredColumns = Functions.setUpto(getColumnCount());
 
        // goes through the whole multidimensional table
        // and applies the filter for each cell
        applyFiltersForEachCell(filters, filteredRows, filteredColumns);
        
        updateFilteredHeaderCounts(filteredRows, filteredColumns);

        filteredRows = null;
        filteredColumns = null;
    }

    private void updateFilteredHeaderCounts(IntCollection filteredRows, IntCollection filteredColumns) {
        // Update row indices and row count to match the
        // number of shown rows af filteration
        IntLinkedOpenHashSet r = (IntLinkedOpenHashSet) filteredRows;
        while(!r.isEmpty()) {
            int row = rowIndices.removeInt(r.removeLastInt());
            for(int column = 0; column < delegate.getColumnCount(); ++column) {
                delegate.filterCellAt(row, column);
            }
        }

        // Update column indices and column count to match the
        // number of shown rows af filteration
        IntLinkedOpenHashSet c = (IntLinkedOpenHashSet) filteredColumns;
        while(!c.isEmpty()) {
           int column = columnIndices.removeInt(c.removeLastInt());
            for(int row = 0; row < delegate.getRowCount(); ++row) {
                delegate.filterCellAt(row, column);
            }
        }
    }

    @Override
    public List<PivotLevel> getColumns() {
        if (null == filteredColumns) {
            filteredColumns = Collections.unmodifiableList(filter(columns, getColumnCount(), new HeaderCallback() {
                @Override
                public DimensionNode getHeaderAt(int level, int index) {
                    return getColumnAt(level, index);
                }
            }));
        }
        return filteredColumns;
    }

    @Override
    public List<PivotLevel> getRows() {
        if (null == filteredRows) {
            filteredRows = Collections.unmodifiableList(filter(rows, getRowCount(), new HeaderCallback() {
                @Override
                public DimensionNode getHeaderAt(int level, int index) {
                    return getRowAt(level, index);
                }
            }));
        }
        return filteredRows;
    }

    private List<PivotLevel> filter(final List<PivotLevel> filterable, int max, HeaderCallback cb) {
        List<PivotLevel> filtered = Lists.newArrayList();
        for (int i = 0; i < filterable.size(); ++i) {
            filtered.add(new PivotLevel(filterable.get(i)));
            List<DimensionNode> retainable = Lists.newArrayList();
            for (int j = 0; j < max; ++j) {
                DimensionNode n = cb.getHeaderAt(i, j);
                retainable.add(n);
            }
            filtered.get(i).retainAll(retainable);
        }
        return filtered;
    }

    /**
     * Traverses the dataset and removes all rows and columns where filter
     * returns true for each cell in row or column. The filter collections lists
     * all row and column indices that should not be visible after filter has
     * been applied
     * 
     * @param filter
     *            predicate that returns true if cell should be filtered out
     * @param filteredRows
     *            hidden row indices
     * @param filteredColumns
     *            hidden column indices
     */
    private void applyFiltersForEachCell(List<Predicate<PivotCell>> filter, IntSet filteredRows, IntSet filteredColumns) {
        if (columnIndices.size() == 0) {
            applyFiltersForSingleDimensionCubes(filter, rowIndices.size(), true, filteredRows);
        } else if (rowIndices.size() == 0) {
            applyFiltersForSingleDimensionCubes(filter, columnIndices.size(), false, filteredColumns);
        } else {
            applyFiltersForAllCells(filter, filteredRows, filteredColumns);
        }
    }

    private void applyFiltersForAllCells(List<Predicate<PivotCell>> filters, IntSet filteredRows, IntSet filteredColumns) {
        long i = 0L;
        for (int column = 0; column < columnIndices.size(); ++column) {
            boolean isColumnIncluded = false;
            for (int row = 0; row < rowIndices.size(); ++row) {
                PivotCell cell = getCellAt(row, column);
                for (Predicate<PivotCell> filter : filters) {
                    if (!filter.apply(cell)) {
                        filteredRows.remove(row);
                        if(!isColumnIncluded) {
                            filteredColumns.remove(column);
                            isColumnIncluded = true;
                        }
                        break;
                    }
                }
                if (++i % 100000 == 0) {
                    LOG.debug("Filter applied to " + i + " cells / " + (columnIndices.size() * rowIndices.size()));
                    LOG.debug(totalTimeSpent);
                }
            }
        }
    }

    private void applyFiltersForSingleDimensionCubes(List<Predicate<PivotCell>> filters, int max, boolean isRow, IntSet nodes) {
        PivotCellImpl cell = new PivotCellImpl("..");
        for (int index = 0; index < max; ++index) {
            if(isRow) {
                cell.setRowNumber(index);
                cell.setColumnNumber(0);
            } else {
                cell.setRowNumber(0);
                cell.setColumnNumber(index);
            }
            for (Predicate<PivotCell> filter : filters) {
                if (!filter.apply(cell)) {
                    nodes.remove(index);
                    break;
                }
            }
        }
    }

    public void filterHiearachy() {
        updateFilteredHeaderCounts(filterHieararchyInRows(), filterHieararchyInColumns());
        filteredRows = null;
        filteredColumns = null;
    }

    private IntSet filterHieararchyInRows() {
        IntSet newFilteredRows = new IntLinkedOpenHashSet();
        List<PivotLevel> someRows = getRows();

        Multimap<Dimension, Integer> dims = determineDimensionInRow(someRows);
        Map<Dimension, Collection<Integer>> asMap = dims.asMap();
        if (asMap.size() != someRows.size()) {
            for (Integer i = 0; i < rowIndices.size(); ++i) {
                boolean filtered = determineIfRowShouldBeFiltered(asMap, i);
                if (filtered) {
                    newFilteredRows.add(rowIndices.get(i));
                }
            }
        }
        return newFilteredRows;
    }

    private IntSet filterHieararchyInColumns() {
        IntSet newFilteredColumns = new IntLinkedOpenHashSet();
        List<PivotLevel> someColumns = getColumns();

        Multimap<Dimension, Integer> dims = determineDimensionInColumn(someColumns);
        Map<Dimension, Collection<Integer>> asMap = dims.asMap();
        if (asMap.size() != someColumns.size()) {
            for (Integer i = 0; i < columnIndices.size(); ++i) {
                boolean filtered = determineIfColumnShouldBeFiltered(asMap, i);
                if (filtered) {
                    newFilteredColumns.add(columnIndices.get(i));
                }
            }
        }
        return newFilteredColumns;
    }

    private boolean determineIfRowShouldBeFiltered(Map<Dimension, Collection<Integer>> asMap, int i) {
        for (Map.Entry<Dimension, Collection<Integer>> e : asMap.entrySet()) {
            List<Integer> l = new ArrayList<>(e.getValue());
            for (int a = 0; a < l.size() - 1; ++a) {
                for (int b = a + 1; b < l.size(); ++b) {
                    if (sameNodeUsedTwiceInRows(i, a, b)) {
                        return true;
                    }
                    if (invalidRowHiearachy(i, a, b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean invalidRowHiearachy(int i, int a, int b) {
        return !getRowAt(a, i).ancestorOf(getRowAt(b, i)) && !getRowAt(b, i).ancestorOf(getRowAt(a, i));
    }

    private boolean sameNodeUsedTwiceInRows(int i, int a, int b) {
        return rows.get(a).getLastNode().getSurrogateId() == getRowAt(a, i).getSurrogateId() 
                && rows.get(b).getLastNode().getSurrogateId() != getRowAt(b, i).getSurrogateId()
                && rows.get(a).getLastNode() == rows.get(b).getLastNode();
    }

    private boolean determineIfColumnShouldBeFiltered(Map<Dimension, Collection<Integer>> asMap, int i) {
        for (Map.Entry<Dimension, Collection<Integer>> e : asMap.entrySet()) {
            List<Integer> l = new ArrayList<>(e.getValue());
            for (int a = 0; a < l.size() - 1; ++a) {
                for (int b = a + 1; b < l.size(); ++b) {
                    if (sameNodeUsedTwiceInColumns(i, a, b)) {
                        return true;
                    }
                    if (invalidColumnHiearachy(i, a, b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean invalidColumnHiearachy(int i, int a, int b) {
        return !getColumnAt(a, i).ancestorOf(getColumnAt(b, i)) && !getColumnAt(b, i).ancestorOf(getColumnAt(a, i));
    }

    private boolean sameNodeUsedTwiceInColumns(int i, int a, int b) {
        return columns.get(a).getLastNode() == getColumnAt(a, i) 
                && columns.get(b).getLastNode() != (getColumnAt(b, i))
                && rows.get(a).getLastNode() == rows.get(b).getLastNode();
    }

    private Multimap<Dimension, Integer> determineDimensionInRow(List<PivotLevel> rows) {
        Multimap<Dimension, Integer> dims = ArrayListMultimap.create();
        if (rowIndices.size() > 1) {
            for (int i = 0; i < rows.size(); ++i) {
                DimensionNode rowHeader = getRowAt(i, 0);
                dims.put(rowHeader.getDimension(), i);
            }
        }
        return dims;
    }

    private Multimap<Dimension, Integer> determineDimensionInColumn(List<PivotLevel> column) {
        Multimap<Dimension, Integer> dims = ArrayListMultimap.create();
        if (columnIndices.size() > 1) {
            for (int i = 0; i < column.size(); ++i) {
                DimensionNode columnHeader = getColumnAt(i, 0);
                dims.put(columnHeader.getDimension(), i);
            }
        }
        return dims;
    }

}
