package fi.thl.pivot.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

public class PivotLevel implements Iterable<IDimensionNode> {

    private static final boolean ASSERT_ENABLED = PivotLevel.class.desiredAssertionStatus();
    
    private static final Logger LOG = Logger.getLogger(PivotLevel.class);
    private Dimension dimension;
    private List<IDimensionNode> nodes = Lists.newArrayList();

    boolean isSubsetLevel;
    private boolean includeTotal;
    private IDimensionNode selectedNode;
    private int repetitionFactor;

    public PivotLevel() {
    }

    /**
     * Constructs a shallow copy of a pivot level. All changes to the nodes in
     * level will be reflected in this copy. Changes to the actual level will
     * not be refleted
     * 
     * @param copy
     */
    public PivotLevel(PivotLevel copy) {
        dimension = copy.dimension;
        nodes.addAll(copy.nodes);
        includeTotal = copy.isTotalIncluded();
        selectedNode = copy.getSelectedNode();
        isSubsetLevel = copy.isSubsetLevel();
    }

    /**
     * Determines if the level is a subset level. The level should be a subset
     * level if the level is created in a {@link Query} where each node in the
     * level is enumerated.
     */
    public boolean isSubsetLevel() {
        return isSubsetLevel;
    }

    public void setAsSubsetLevel() {
        isSubsetLevel = true;
    }

    public void add(Collection<IDimensionNode> someNodes) {
        this.nodes.addAll(someNodes);
        if (null == dimension && !this.nodes.isEmpty()) {
            dimension = this.nodes.get(0).getDimension();
        }
    }

    public void add(IDimensionNode node) {
        if (null == dimension) {
            dimension = node.getDimension();
        }
        this.nodes.add(node);
    }

    public List<IDimensionNode> getNodes() {
        return nodes;
    }

    @Override
    public Iterator<IDimensionNode> iterator() {
        return nodes.iterator();
    }

    public int size() {
        return nodes.size();
    }

    public void retainAll(List<IDimensionNode> retainable) {
        nodes.retainAll(retainable);
    }

    public IDimensionNode get(int i) {
        if(ASSERT_ENABLED) {
            if (i < 0 || i >= nodes.size()) {
                LOG.warn(String.format("User attempted to access node in %d but only %d nodes available", i, nodes.size()));
                return null;
            }
        }
        return nodes.get(i);
    }

    public IDimensionNode getLastNode() {
        return get(size() - 1);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void sort() {
        Collections.sort(nodes);
    }

    @Override
    public String toString() {
        return "PivotLevel [dimension=" + dimension.getId() + ", nodes=" + nodes + "]";
    }

    public void setIncludesTotal(boolean total) {
        this.includeTotal = total;
    }

    public Boolean isTotalIncluded() {
        return includeTotal;
    }

    public IDimensionNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(IDimensionNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public int getRepetitionFactor(List<PivotLevel> levels, int level) {
        if (this.repetitionFactor == 0) {
            if (levels.size() - 1 == level) {
                this.repetitionFactor = 1;
            } else {
                PivotLevel parent = levels.get(level + 1);
                this.repetitionFactor = parent.size() * parent.getRepetitionFactor(levels, level + 1);
            }
        }
        return this.repetitionFactor;
    }

    public IDimensionNode getElement(List<PivotLevel> levels, int level, int element) {
        int repetitionFactor = getRepetitionFactor(levels, level);
        return nodes.get((element / repetitionFactor) % nodes.size());
    }

}
