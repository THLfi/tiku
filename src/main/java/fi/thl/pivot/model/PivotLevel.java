package fi.thl.pivot.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

public class PivotLevel implements Iterable<DimensionNode> {

    private static final Logger LOG = Logger.getLogger(PivotLevel.class);
    private Dimension dimension;
    private List<DimensionNode> nodes = Lists.newArrayList();

    boolean isSubsetLevel;
    private boolean includeTotal;
    private DimensionNode selectedNode;

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

    public void add(Collection<DimensionNode> someNodes) {
        this.nodes.addAll(someNodes);
        if (null == dimension && !this.nodes.isEmpty()) {
            dimension = this.nodes.get(0).getDimension();
        }
    }

    public void add(DimensionNode node) {
        if (null == dimension) {
            dimension = node.getDimension();
        }
        this.nodes.add(node);
    }

    public List<DimensionNode> getNodes() {
        return nodes;
    }

    @Override
    public Iterator<DimensionNode> iterator() {
        return nodes.iterator();
    }

    public int size() {
        return nodes.size();
    }

    public void retainAll(List<DimensionNode> retainable) {
        nodes.retainAll(retainable);
    }

    public DimensionNode get(int i) {
        if (i < 0 || i >= nodes.size()) {
            LOG.warn(String.format("User attempted to access node in %d but only %d nodes available", i, nodes.size()));
            return null;
        }
        return nodes.get(i);
    }

    public DimensionNode getLastNode() {
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
    
    public DimensionNode getSelectedNode() {
        return selectedNode;
    }
    
    public void setSelectedNode(DimensionNode selectedNode) {
        this.selectedNode = selectedNode;
    }

}
