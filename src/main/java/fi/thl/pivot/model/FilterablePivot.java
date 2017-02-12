package fi.thl.pivot.model;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fi.thl.pivot.util.Functions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;

public class FilterablePivot extends AbstractPivotForwarder {

    private static final Logger LOG = Logger.getLogger(FilterablePivot.class);

    private static interface HeaderCallback {
        DimensionNode getHeaderAt(int level, int index);
    }

    private List<PivotLevel> filteredRows = null;
    private List<PivotLevel> filteredColumns = null;

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

        // Start with empty coordinate indices
        rowIndices = new IntArrayList();
        columnIndices = new IntArrayList();

        // Prepare headers for filteration
        include(new ColumnStrategy(), 0, 0);
        include(new RowStrategy(), 0, 0);

        filter(filters);
    }

    private void filter(List<Predicate<PivotCell>> filters) {
        IntLinkedOpenHashSet included = new IntLinkedOpenHashSet();
        for (int column = getColumnCount() - 1; column >= 0; --column) {
            boolean columnIncluded = false;
            for (int row = getRowCount() - 1; row >= 0; --row) {

                PivotCell cell = getCellAt(row, column);

                // Row and column should be included
                // if no filters apply to the current cell

                boolean includeCell = true;
                for (Predicate<PivotCell> filter : filters) {
                    if (filter.apply(cell)) {
                        includeCell = false;
                        break;
                    }
                }

                if (includeCell) {
                    columnIncluded = true;
                    included.add(row);
                }
                
            }
            if (!columnIncluded) {
                columnIndices.remove(column);
            }
        }

        for (int rem = rowIndices.size() - 1; rem >= 0; --rem) {
            if (!included.contains(rem)) {
                rowIndices.remove(rem);
            }
        }
    }

    private int include(IncludeStrategy strategy, int level, int index) {
        if (level == strategy.size() - 1) {
            return includeLeafLevel(strategy, level, index);
        } else {
            return includeLevel(strategy, level, index);
        }
    }

    private int includeLevel(IncludeStrategy strategy, int level, int index) {
        for (@SuppressWarnings("unused")
        DimensionNode node : strategy.get(level)) {
            if (!shouldFilter(strategy, level, index)) {
                index = include(strategy, level + 1, index);
            } else {
                index += strategy.getRepetitionFactory(level);
            }
        }
        return index;
    }

    private int includeLeafLevel(IncludeStrategy strategy, int level, int index) {
        int levelIndex = 0;
        int levelSize = strategy.get(level).size();
        if(strategy.get(level).isTotalIncluded()) {
            levelSize -= 1;
        }
        for (@SuppressWarnings("unused")
        DimensionNode node : strategy.get(level)) {
            if(++levelIndex <= levelSize && !shouldFilter(strategy, level, index)) {
                strategy.add(index);
            }
            ++index;
        }
        return index;
    }

    private boolean shouldFilter(IncludeStrategy strategy, int level,
            int i) {
        for (int a = 0; a < level; ++a) {
            DimensionNode aNode = strategy.getNode(a, i);
            DimensionNode aLastNode = strategy.get(a).getLastNode();

            for (int b = a + 1; b < level + 1; ++b) {
                DimensionNode bNode = strategy.getNode(b, i);
   
                if(!aNode.getDimension().equals(bNode.getDimension())) {
                    continue;
                }
                
                DimensionNode bLastNode = strategy.get(b).getLastNode();
                if (aLastNode == bLastNode
                        && aLastNode.getSurrogateId() == aNode.getSurrogateId()
                        && bLastNode.getSurrogateId() != bNode.getSurrogateId()) {
                    return true;
                }
                
                if (!aNode.ancestorOf(bNode) && !bNode.ancestorOf(aNode)) {
                    return true;
                }
            }
        }
        return false;
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

    private static abstract class IncludeStrategy {

        abstract List<PivotLevel> getLevels();

        abstract void add(int index);

        abstract DimensionNode getNode(int level, int index);

        public PivotLevel get(int level) {
            return getLevels().get(level);
        }

        public int getRepetitionFactory(int i) {
            return get(i).getRepetitionFactor(getLevels(), i + 1);
        }

        public int size() {
            return getLevels().size();
        }
    }

    private class RowStrategy extends IncludeStrategy {

        @Override
        public List<PivotLevel> getLevels() {
            return rows;
        }

        @Override
        public DimensionNode getNode(int level, int index) {
            return delegate.getRowAt(level, index);
        }

        @Override
        void add(int index) {
            rowIndices.add(index);
        }

    }

    private class ColumnStrategy extends  IncludeStrategy {

        @Override
        public List<PivotLevel> getLevels() {
            return columns;
        }

        @Override
        public DimensionNode getNode(int level, int index) {
            return delegate.getColumnAt(level, index);
        }
        
        @Override
        void add(int index) {
            columnIndices.add(index);
        }

    }

}
