package fi.thl.pivot.model;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import fi.thl.pivot.util.Constants;

/**
 * Represents a single dimension e.g. Time, Region, etc. Each dimension is used
 * as a potential classifier in a dataset.
 * 
 * Dimension is used as defined in the Hydra specification.
 * 
 * 
 * @author aleksiyrttiaho
 *
 */
public class Dimension {

    private final String id;
    private final Label label;
    private final DimensionLevel rootLevel;
    private final Map<Integer, IDimensionNode> nodes;
    private boolean isMeasure;
    private int hashCode;

    public Dimension(String id, Label label, Label rootLabel) {
        Preconditions.checkNotNull(id, "Dimension must have a non-null identifier");
        Preconditions.checkArgument(!id.trim().isEmpty(), "Dimension must have a non-empty identifier");
        Preconditions.checkNotNull(label, "Dimension must have a non-null label");

        this.id = id;
        this.label = label;
        this.rootLevel = new DimensionLevel("root", this, 0);
        this.nodes = Maps.newHashMap();
        rootLevel.createNode("all-" + id, rootLabel, null);
        this.isMeasure = Constants.MEASURE.equals(id);
    }

    public boolean isMeasure() {
        return isMeasure;
    }

    void putNode(IDimensionNode node) {
        nodes.put(node.getSurrogateId(), node);
    }

    public IDimensionNode getNode(String id) {
        DimensionLevel level = rootLevel;
        while (level != null) {
            for (IDimensionNode n : level.getNodes()) {
                if (n.getReference().equals(id)) {
                    return n;
                }
            }
            level = level.getChildLevel();
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public Label getLabel() {
        return label;
    }

    public DimensionLevel getRootLevel() {
        return rootLevel;
    }

    public IDimensionNode getRootNode() {
        return getRootLevel().getNodes().get(0);
    }

    @Override
    public String toString() {
        return "Dimension [id=" + id + ", label=" + label + "]";
    }

    public DimensionLevel getLevel(String stage) {
        DimensionLevel level = getRootLevel();
        while (level != null) {
            if (level.getId().equalsIgnoreCase(stage)) {
                return level;
            }
            level = level.getChildLevel();
        }
        return null;
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = id.hashCode();
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dimension other = (Dimension) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
