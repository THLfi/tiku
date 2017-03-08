package fi.thl.pivot.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DimensionLevel {

    private final Map<String, DimensionNode> nodeMap = Maps.newHashMap();
    private final List<DimensionNode> nodes = Lists.newArrayList();

    public static DimensionLevel SENTINEL = new DimensionLevel("nil",null,Integer.MAX_VALUE);
    
    private final Dimension dimension;
    private DimensionLevel parent;
    private DimensionLevel child;
    private String id;
    private Label label;
    private int index;

    private boolean sorted = false;

    DimensionLevel(String id, Dimension dimension, int index) {
        this.id = id;
        this.dimension = dimension;
        this.label = new Label();
        this.index = index;
    }

    public void addNodes(DimensionNode root) {
        nodes.add(root);
        sorted = false;
    }

    public List<DimensionNode> getNodes() {
        if (!sorted) {
            Collections.sort(nodes);
            sorted = true;
        }

        return Lists.newArrayList(Collections2.filter(nodes, new Predicate<DimensionNode>() {
            @Override
            public boolean apply(DimensionNode input) {
                return input.canAccess();
            }
        }));
    }

    public DimensionLevel getParentLevel() {
        return parent;
    }

    public DimensionLevel getChildLevel() {
        return child;
    }

    public DimensionLevel addLevel(String id, Label label) {
        if(null == this.child) {
            child = new DimensionLevel(id, dimension, this.index + 1);
            child.parent = this;
        }  
        return child;
    }

    public DimensionNode createNode(String id, Label label, DimensionNode parent) {
        Preconditions.checkArgument(!nodeMap.containsKey(id), "A node with id " + id + " already exists");
        nodeMap.put(id, new DimensionNode(this, id, label));
        nodes.add(nodeMap.get(id));
        nodeMap.get(id).setParent(parent);
        dimension.putNode(nodeMap.get(id));
        return nodeMap.get(id);
    }

    public DimensionLevel addLevel(String dimensionLevel) {
        DimensionLevel child =  addLevel(dimensionLevel, Label.create("fi", dimensionLevel));
        return child;
    }

    public String getId() {
        return id;
    }

    public Label getLabel() {
        return label;
    }

    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        return "DimensionLevel [id=" + id + ", label=" + label + "]";
    }

    public int getIndex() {
        return index;
    }
}
