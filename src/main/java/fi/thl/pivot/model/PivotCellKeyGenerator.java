package fi.thl.pivot.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This object is responsible for generating a series of keys that is used to
 * access {@link Dataset} to populate {@link PivotCell} values. The intent is to
 * provide an optimized version of key generation that minimized object creation
 * and expensive comparison operations.
 *
 * Optimizations took time per request from 16 microsends to 3 microseconds on a
 * i5 processor
 * 
 * @author aleksiyrttiaho
 * @author tapiosaarivaara
 *
 */
class PivotCellKeyGenerator {

    private final Pivot pivot;

    /**
     * Index of the last row - not the number of rows!
     */
    private final int rows;

    /**
     * Index of the last column - not the number of columns!
     */
    private final int columns;

    /**
     * The total amount of rows, columns and constants - provides a maximum size
     * for the key. The key may be smaller though
     */
    private final int total;

    private final DimensionNode[] constants;
    private final DimensionNode[] dimensions;

    private final int[] key;
    private final int[] fullKey;

    private int[] k;
    /**
     * Represents the index of the last id that has been inserted in to key.
     * This is used to keep track of the head of the array list so that
     * insertion is O(1)
     */
    private int lastKeyIndex = 0;

    /**
     * Represents the index of the last dimension that has been inserted in to
     * dimension. This is used to keep track of the head of the array list so
     * that insertion is O(1)
     */
    private int lastDimensionIndex = 0;

    /**
     * Represent a measure attached to the current cell. We have to keep track
     * of the measure here so that we don't have to go through all the keys
     * again when we assign the measure to the cell
     */
    private DimensionNode measure;

    public PivotCellKeyGenerator(Pivot pivot, Collection<DimensionNode> constants) {
        this.pivot = pivot;
        this.rows = pivot.getRows().size();
        this.columns = pivot.getColumns().size();

        // as rows and columns point to the last index
        // and not the count we have to add 2
        this.total = rows + columns + constants.size();

        this.constants = constants.toArray(new DimensionNode[constants.size()]);
        this.dimensions = new DimensionNode[total];
        this.key = new int[total];
        this.fullKey = new int[rows + columns];
    }

    public DimensionNode getMeasure() {
        return measure;
    }

    /**
     * Generates a hash code based on cell header nodes and filter nodes. The
     * hash code is not perfect and may cause collision on very large data sets
     */
    public SortedSet<Integer> createCellKey(int row, int column) {
        clear();
        addRows(row);
        addColumns(column, rows);
        k = Arrays.copyOf(key, lastKeyIndex);
        addConstants();
        return generateSortedKey();
    }

    private SortedSet<Integer> generateSortedKey() {
        SortedSet<Integer> sortedKey = new TreeSet<>();
        for (int i = 0; i < lastKeyIndex; ++i) {
            sortedKey.add(key[i]);
        }
        return sortedKey;
    }

    /**
     * Returns the part of the cell key that descibes the row and column nodes
     * used to describe the cell. This is required if pivot cell is to be used
     * in a situation where the dataset is described as a sparse matrixs
     * 
     * @return
     */
    public int[] getKey() {
        return k;
    }

    public int[] getFullKey() {
        return Arrays.copyOf(fullKey, fullKey.length);
    }

    /**
     * Iterates over each row header level and adds the node to the cell key
     * 
     * @param row
     */
    private void addRows(int row) {
        for (int i = 0; i < rows; ++i) {
            DimensionNode node = pivot.getRowAt(i, row);
            addNode(node, i);
        }
    }

    /**
     * Iterates over each column header level and adds the node to the cell key
     * 
     * @param row
     */
    private void addColumns(int column, int offset) {
        for (int i = 0; i < columns; ++i) {
            DimensionNode node = pivot.getColumnAt(i, column);
            addNode(node, i + offset);
        }
    }

    /**
     * Inserts a node to key if the key is the most specific node of a dimension
     * tree. If the same dimension is already present in the key then current
     * node is replaced with a more specific one.
     * 
     * @param node
     */
    private void addNode(final DimensionNode node, int position) {
        final int id = node.getSurrogateId();
        final int oldNodeIndex = putDimIfAbsent(node);
        fullKey[position] = node.getSurrogateId();
        if (oldNodeIndex >= 0) {
            final DimensionNode oldNode = dimensions[oldNodeIndex];
            if (node.ancestorOf(oldNode)) {
                // A more specific node is found from the same
                // dimension tree, skip
                return;
            } else {
                // A more generic node is found from the same
                // dimension tree. Find the old node and
                // replace it with the more specific node
                dimensions[oldNodeIndex] = node;
                final int oldId = oldNode.getSurrogateId();
                for (int i = 0; i < lastKeyIndex; ++i) {
                    if (key[i] == oldId) {
                        key[i] = id;
                        if (node.isMeasure()) {
                            measure = node;
                        }
                        return;
                    }
                }
                throw new IllegalStateException("Could not find old node to replace in key");
            }
        } else {
            // Append the id as the last element in the list
            key[lastKeyIndex++] = id;
        }
    }

    // Check if constants contain the same dimension multiple times
    // current implementation relies on that not being the case
    /**
     * Add all contanst dimensions to the key that are not present in the key
     */
    private void addConstants() {
        outer: for (int j = 0; j < constants.length; ++j) {
            DimensionNode node = constants[j];
            Dimension nd = node.getDimension();

            // Check if constant dimension is already
            // present in the key and skip the dimension
            // if it is
            for (int i = 0; i < lastDimensionIndex; ++i) {
                DimensionNode d = dimensions[i];
                if (d.getDimension().getId().equals(nd.getId())) {
                    // Note that the continue is targetted to
                    // the outer loop that loops through constants!
                    continue outer;
                }
            }

            // Dimension was not yet found so let's
            // append it to the key
            key[lastKeyIndex++] = node.getSurrogateId();
            if (nd.isMeasure()) {
                measure = node;
            }
        }
    }

    private int putDimIfAbsent(DimensionNode node) {
        Dimension nodeDimension = node.getDimension();
        for (int i = 0; i < lastDimensionIndex; ++i) {
            DimensionNode n = dimensions[i];
            if (n.getDimension() == nodeDimension) {
                return i;
            }
        }
        dimensions[lastDimensionIndex++] = node;
        if (nodeDimension.isMeasure()) {
            measure = node;
        }
        return -1;
    }

    /**
     * Reset the key generator to initial state so that key is generated
     * correctly.
     * 
     * Note that we don't clear the dimension or key arrays as we are only
     * concerned about their last used indices.
     */
    private void clear() {
        lastKeyIndex = 0;
        lastDimensionIndex = 0;
        measure = null;
    }

}