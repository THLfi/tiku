package fi.thl.pivot.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fi.thl.pivot.util.Constants;
import fi.thl.pivot.util.CumulativeStopWatch;

public class ModifiablePivot implements Pivot {

    private static final boolean ASSERT_ENABLED = ModifiablePivot.class.desiredAssertionStatus();
    private static final Logger LOG = Logger.getLogger(ModifiablePivot.class);

    private PivotCellSentinel sentinel = new PivotCellSentinel(-1, -1);

    private List<PivotLevel> columns = Lists.newArrayList();
    private List<PivotLevel> rows = Lists.newArrayList();

    /**
     * Cell Cache is a simple optimization that prevents creation of the same
     * PivotCell object each time the cell is accessed
     */
    private Map<Object, PivotCell> cellCache = Maps.newHashMap();

    private Set<DimensionNode> constants = Sets.newLinkedHashSet();

    int columnCount;
    int rowCount;

    private boolean filtersApplied;
    private Dataset dataset;
    private int fullColumnCount;

    private CumulativeStopWatch sw = new CumulativeStopWatch();
    private DimensionNode defaultMeasure;
    private List<DimensionNode> filterNodes;
    private PivotCellKeyGenerator cellKeyGenerator;

    public ModifiablePivot(Dataset dataset) {
        this.dataset = dataset;
    }

    public void logTimeSpent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(sw.prettyPrint());
        }
    }

    @Override
    public Iterator<PivotCell> iterator() {
        return new PivotIterator(this, rowCount, columnCount);
    }

    /**
     * Returns a collection of all columns (filtered and non-filtered) in the
     * table. There may be 0..n column levels that may have 1..n column nodes
     */
    @Override
    public List<PivotLevel> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    @Override
    public List<PivotLevel> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void appendColumn(PivotLevel nodes) {
        Preconditions.checkNotNull(nodes, "Cannot add null as column");
        Preconditions.checkState(!filtersApplied, "Cannot add column after filters have been applied");
        columns.add(nodes);

        columnCount = calculateHeaderCount(columnCount, nodes);
        fullColumnCount = columnCount;
    }

    public void appendRow(PivotLevel nodes) {
        Preconditions.checkNotNull(nodes, "Cannot add null as row");
        Preconditions.checkState(!filtersApplied, "Cannot add column after filters have been applied");
        rows.add(nodes);

        rowCount = calculateHeaderCount(rowCount, nodes);
    }

    public void appendConstant(DimensionNode node) {
        Preconditions.checkNotNull(node, "Cannot add null as constant");
        constants.add(node);
    }

    /**
     * Returns the number of columns in table after filteration
     */
    @Override
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Returns the number of rows after filteration
     */
    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public void filterCellAt(int row, int column) {
        int cacheKey = createCacheKey(row, column);
        cellCache.remove(cacheKey);
    }

    @Override
    public PivotCell getCellAt(int row, int column) {

        int cacheKey = createCacheKey(row, column);
        if (cellCache.containsKey(cacheKey)) {
            return cellCache.get(cacheKey);
        }
        if (ASSERT_ENABLED) {
            checkRowBounds(row);
            checkColumnBounds(column);
        }
        if (null == cellKeyGenerator) {
            this.cellKeyGenerator = new PivotCellKeyGenerator(this, constants);
        }
        SortedSet<Integer> key = cellKeyGenerator.createCellKey(row, column);
        String datum = dataset.getWithIds(key);
        if (null != datum) {
            PivotCellImpl cell = null;
            DimensionNode measure = cellKeyGenerator.getMeasure();

            cell = new PivotCellImpl(datum);
            cell.setKey(cellKeyGenerator.getKey());
            cell.setFullKey(cellKeyGenerator.getFullKey());
            cell.setMeasure(measure);
            cell.setRowNumber(row);
            cell.setColumnNumber(column);

            setConfidenceInterval(key, measure, cell);
            setSampleSize(key, measure, cell);

            cellCache.put(cacheKey, cell);
            return cell;
        } else {
            // PivotCell cell = new PivotCellSentinel(row, column);
            // cellCache.put(cacheKey, cell);
            // return cell;
            cellCache.put(cacheKey, sentinel);
            return sentinel;
        }
    }

    private int createCacheKey(int row, int column) {
        int cacheKey = row * fullColumnCount + column;
        return cacheKey;
    }

    private void setSampleSize(SortedSet<Integer> key, DimensionNode measure, PivotCellImpl cell) {
        if (null != measure && measure.getSampleSizeNode() != null) {
            cell.setSampleSize(getValueWithModifiedKey(key, measure.getSurrogateId(),
                    measure.getSampleSizeNode().getSurrogateId()));
        }
    }

    private void setConfidenceInterval(SortedSet<Integer> key, DimensionNode measure, PivotCellImpl cell) {
        if (null != measure && measure.getConfidenceLowerLimitNode() != null) {
            cell.setConfidenceLowerLimit(getValueWithModifiedKey(key, measure.getSurrogateId(),
                    measure.getConfidenceLowerLimitNode().getSurrogateId()));
            cell.setConfidenceUpperLimit(
                    getValueWithModifiedKey(key, measure.getConfidenceLowerLimitNode().getSurrogateId(),
                            measure.getConfidenceUpperLimitNode().getSurrogateId()));
            key.remove(measure.getConfidenceUpperLimitNode().getSurrogateId());
            key.add(measure.getSurrogateId());
        }
    }

    private String getValueWithModifiedKey(SortedSet<Integer> key, Integer a, Integer b) {
        key.remove(a);
        key.add(b);
        return dataset.getWithIds(key);
    }

    @Override
    public DimensionNode getColumnAt(int level, int column) {
        if (ASSERT_ENABLED) {
            Preconditions.checkArgument(level >= 0 && level < columns.size(),
                    String.format("Column level is out of bounds %d / %d", level, columns.size()));
            checkColumnBounds(column);
        }
        return getNodeAt(columns, level, column);
    }

    @Override
    public DimensionNode getRowAt(int level, int row) {
        if (ASSERT_ENABLED) {
            Preconditions.checkArgument(level >= 0 && level < rows.size(),
                    String.format("Row level is out of bounds %d / %d", level, rows.size()));
            checkRowBounds(row);
        }
        return getNodeAt(rows, level, row);
    }

    /**
     * 
     * Determines which header should be applied for a given cell. This method
     * is called separately for row and column headers.
     * 
     * The header is selected by removing the effect of header repetition by
     * dividing the index by the number of times each level is repeated and then
     * using modulo arithmetic to ensure we get the correct elementary index.
     * 
     * The levels are processed in reverse order as each sub-level is repeated
     * for each value in respective parent level.
     * 
     * @param element
     *            index of row or column being processes
     * @param nodes
     *            Levels and members of row or column headers
     * @param key
     *            List of row and column headers for requested cell
     */
    private DimensionNode getNodeAt(List<PivotLevel> nodes, int level, int element) {
        return nodes.get(level).getElement(nodes, level, element);
    }

    /**
     * Used to increment the current row or column count by calculating how many
     * times headers are repeated in a given level.
     * 
     * This method must be called in reverse order in regards to the levels
     * depth
     * 
     * @param currentCount
     *            current number of rows or columns
     * @param nodes
     *            members of the current level
     * @param modified
     * @return new number of rows or colums
     */
    private int calculateHeaderCount(int currentCount, PivotLevel nodes) {
        if (currentCount == 0) {
            return nodes.size();
        } else {
            currentCount = currentCount * nodes.size();
            return currentCount;
        }
    }

    private void checkRowBounds(int row) {
        Preconditions.checkArgument(row >= 0 && row < rowCount,
                String.format("Row index out of bounds %d / %d", row, rowCount));
    }

    private void checkColumnBounds(int column) {
        Preconditions.checkArgument(column >= 0 && column < columnCount,
                String.format("Column index out of bounds %d / %d", column, columnCount));
    }

    public void setDefaultMeasure(DimensionNode defaultMeasure) {
        this.defaultMeasure = defaultMeasure;
    }

    public DimensionNode getDefaultMeasure() {
        if (null != defaultMeasure) {
            return defaultMeasure;
        }
        for (PivotLevel l : getRows()) {
            if (Constants.MEASURE.equals(l.getDimension().getId())) {
                return null;
            }
        }
        for (PivotLevel l : getColumns()) {
            if (Constants.MEASURE.equals(l.getDimension().getId())) {
                return null;
            }
        }
        for (DimensionNode l : filterNodes) {
            if (Constants.MEASURE.equals(l.getDimension().getId())) {
                return l;
            }
        }
        return null;
    }

    public void setFilterNodes(List<DimensionNode> filterNodes) {
        this.filterNodes = filterNodes;
    }

    @Override
    public boolean isFirstColumn(int column) {
        return isColumn(column, 0);
    }

    @Override
    public boolean isColumn(int column, int targetColumn) {
        return column == targetColumn;
    }

    @Override
    public int getColumnNumber(int column) {
        return column;
    }

}
