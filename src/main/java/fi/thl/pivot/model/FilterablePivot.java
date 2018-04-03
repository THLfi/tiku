package fi.thl.pivot.model;

import java.util.*;
import java.util.stream.Collectors;

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

    private  interface HeaderCallback {
        IDimensionNode getHeaderAt(int level, int index);
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
        return delegate.getColumnNumber(column);
    }

    @Override
    public PivotCell getCellAt(int row, int column) {
        return new PivotCellForwarder(super.getCellAt(rowIndices.get(row), columnIndices.get(column)), row, column);
    }

    @Override
    public IDimensionNode getRowAt(int level, int row) {
        return super.getRowAt(level, rowIndices.get(row));
    }

    @Override
    public IDimensionNode getColumnAt(int level, int column) {
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
        if (level >= strategy.size() - 1) {
            return includeLeafLevel(strategy, level, index);
        } else {
            return includeLevel(strategy, level, index);
        }
    }

    private int includeLevel(IncludeStrategy strategy, int level, int index) {
        outer: for (@SuppressWarnings("unused")
                IDimensionNode node : strategy.get(level)) {
            if (!shouldFilter(strategy, level, index)) {
                index = include(strategy, level + 1, index);
            } else {
                index += strategy.getRepetitionFactory(level);
            }
        }
        return index;
    }

    private Collection<String> labels(Collection<IDimensionNode> nodes) {
        return nodes.stream().map(x->x.getLabel().getValue("fi")).collect(Collectors.toList());
    }

    private int includeLeafLevel(IncludeStrategy strategy, int level, int index) {
        if (strategy.size() == 0) {
            strategy.add(0);
            return 0;
        }

        int levelSize = strategy.get(level).size();

        outer:
        for (int i = 0; i < levelSize; ++i) {
            IDimensionNode node = strategy.getNode(level, index);
            if (!shouldFilter(strategy, level, index)) {
                if(!shouldFilterTotalRow(strategy, level, index, node)) {
                    strategy.add(index);
                }
            }

            ++index;
        }


        return index;
    }

    private boolean shouldFilterTotalRow(IncludeStrategy strategy, int level, int index, IDimensionNode node) {

        boolean shouldInclude = false;
        List<IDimensionNode> parents = getParentNodes(strategy, index, level, node);
        parents.add(node);

        for(int i = 0; i < parents.size() - 1; ++i) {
            IDimensionNode parent = parents.get(i);
            if(parent instanceof InputtedDimensionNode) {
                for (int j = i + 1; j < parents.size(); ++j) {
                    IDimensionNode child = parents.get(j);
                    if(parent.getDimension().equals(child.getDimension())) {
                        if(!(child instanceof InputtedDimensionNode)) {
                            // Case: If parent node is a total node then
                            // child must also be a total node
                            return true;
                        }
                        if(child.getSurrogateId() != parent.getSurrogateId()) {
                            // Case: If parent is total node then
                            // child must represent the same node as parent
                            return true;
                        }
                    }
                }
            }
        }
        if(node instanceof InputtedDimensionNode && ((InputtedDimensionNode) node).getLevelNumber() != 0) {
            // Case: We are only interested in the total and nothing but the total
            return true;
        }
        return shouldInclude;
    }

    private List<IDimensionNode> getParentNodes(IncludeStrategy strategy, int index, int level, IDimensionNode node) {
        List<IDimensionNode> parentNodes = new ArrayList<>();
        for(int j = 0; j < level; ++j) {
            IDimensionNode parent = strategy.getNode(j, index);
            parentNodes.add(parent);

        }
        return parentNodes;
    }

    private boolean shouldFilter(IncludeStrategy strategy, int level,
            int i) {
        
        for (int a = 0; a < level; ++a) {
            IDimensionNode aNode = strategy.getNode(a, i);
            IDimensionNode aLastNode = strategy.getLastNode(a);

            for (int b = a + 1; b < level + 1; ++b) {

                IDimensionNode bLastNode = strategy.getLastNode(b);
                // We rely on implementation detail here. 
                // This phase took 3.6 % of execution time
                // when changed from safe equals check to 
                // unsafe reference check. Can reduce execution
                // time 1 second or more when loop is executed
                // multiple times. 
                if (aNode.getDimension() != bLastNode.getDimension()) {
                    continue;
                }

                IDimensionNode bNode = strategy.getNode(b, i);

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
                public IDimensionNode getHeaderAt(int level, int index) {
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
                public IDimensionNode getHeaderAt(int level, int index) {
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
            List<IDimensionNode> retainable = Lists.newArrayList();
            for (int j = 0; j < max; ++j) {
                IDimensionNode n = cb.getHeaderAt(i, j);
                retainable.add(n);
            }
            filtered.get(i).retainAll(retainable);
        }
        return filtered;
    }

    private static abstract class IncludeStrategy {

        protected long hitCount = 0;
        
        private Map<Integer, IDimensionNode> lastNodes = new HashMap<>();

        abstract List<PivotLevel> getLevels();

        abstract void add(int index);

        abstract IDimensionNode getNode(int level, int index);

        public PivotLevel get(int level) {
            return getLevels().get(level);
        }

        public IDimensionNode getLastNode(int level) {
            if(!lastNodes.containsKey(level)) {
                lastNodes.put(level, get(level).getLastNode());
            }
            return lastNodes.get(level);
        }

        public int getRepetitionFactory(int i) {
            if (i >= getLevels().size()) {
                return 1;
            }
            return get(i).getRepetitionFactor(getLevels(), i + 1);
        }

        public int size() {
            return getLevels().size();
        }
        
        public long getHitCount() {
            return hitCount;
        }
    }

    private class RowStrategy extends IncludeStrategy {

        @Override
        public List<PivotLevel> getLevels() {
            return rows;
        }

        @Override
        public IDimensionNode getNode(int level, int index) {
            hitCount++;
            return delegate.getRowAt(level, index);
        }

        @Override
        void add(int index) {
            rowIndices.add(index);
        }

    }

    private class ColumnStrategy extends IncludeStrategy {

        @Override
        public List<PivotLevel> getLevels() {
            return columns;
        }

        @Override
        public IDimensionNode getNode(int level, int index) {
            hitCount++;
            return delegate.getColumnAt(level, index);
        }

        @Override
        void add(int index) {
            columnIndices.add(index);
        }

    }

}
