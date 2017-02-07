package fi.thl.pivot.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import fi.thl.pivot.util.Constants;
import fi.thl.pivot.util.ThreadRole;

/**
 * Holds metadata and hierarchy of each node in a dimension
 * 
 * @author aleksiyrttiaho
 * 
 */
public class DimensionNode implements Comparable<DimensionNode> {

    private static final String DEFAULT = "default";
    private final DimensionLevel level;
    private String id;
    private final Label label;
    private final List<DimensionNode> children;
    private DimensionNode parent;

    private Map<String, Long> sort = new HashMap<>();
    private String code;
    private int decimals = -1;
    private Map<String, Label> properties = new HashMap<>();
    private int surrogateId;
    private int hashCode;
    private List<String> passwords = new ArrayList<>();
    private String reference;
    private Map<String, DimensionNode> edges = new HashMap<>();

    public DimensionNode(DimensionLevel level, String id, Label label) {
        Preconditions.checkNotNull(id, "Dimension node must have a non-null identifier");
        Preconditions.checkArgument(!id.trim().isEmpty(), "Dimension node must have a non-empty identifier");
        Preconditions.checkNotNull(label, "Dimension node must have a non-null label");
        Preconditions.checkNotNull(level, "Dimension node must be attached to a level");
        this.level = level;
        this.id = id;
        this.label = label;
        children = new ArrayList<>();
    }

    public boolean isMeasure() {
        return level.getDimension().isMeasure();
    }

    public Set<Map.Entry<String, Label>> getProperties() {
        return properties.entrySet();
    }

    public String getId() {
        return id;
    }

    // FIXME: Ids should be permanent but currently root level nodes
    // are automatically created and this is the only way the id can
    // be set.
    public void setId(String id) {
        this.id = id;
    }

    public Label getLabel() {
        return label;
    }

    public Dimension getDimension() {
        return level.getDimension();
    }

    public boolean isRootLevelNode() {
        return getDimension().getRootLevel().getNodes().contains(this);
    }

    public DimensionNode getParent() {
        return parent;
    }

    public Collection<DimensionNode> getChildren() {
        final boolean canAccess = canAccess();
        Collections.sort(children);
        return Collections2.filter(children, new Predicate<DimensionNode>() {

            @Override
            public boolean apply(DimensionNode input) {
                if (!canAccess) {
                    return false;
                }
                if (input.passwords.isEmpty()) {
                    return true;
                }
                if (ThreadRole.getRole() != null && ThreadRole.getRole().matches(input.passwords)) {
                    return true;
                }
                return false;
            }
        });
    }

    public DimensionNode getFirstChild() {
        return new ArrayList<>(getChildren()).get(0);
    }

    public void sortChildren(Comparator<DimensionNode> comparator) {
        Collections.sort(children, comparator);
    }

    void setParent(final DimensionNode node) {
        if (null != parent) {
            parent.removeChild(this);
        }
        parent = node;
        if (null != parent) {
            parent.addChild(this);
        }
    }

    protected void addChild(DimensionNode node) {
        this.children.add(node);
    }

    protected void removeChild(DimensionNode node) {
        this.children.remove(node);
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = id.hashCode();
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        DimensionNode other = (DimensionNode) obj;
        return surrogateId == other.surrogateId;
    }

    @Override
    public String toString() {
        return "DimensionNode [id=" + id + ", label=" + label + ", surrogateId=" + surrogateId + "]";
    }

    public boolean ancestorOf(DimensionNode node) {
        if (node == this || node.equals(this)) {
            return true;
        }
        DimensionNode aParent = node.parent;
        while (aParent != null) {
            if (aParent == this || aParent.equals(this)) {
                return true;
            }
            aParent = aParent.parent;
        }
        return false;
    }

    public DimensionLevel getLevel() {
        return this.level;
    }

    public void setSort(String language, Long sort) {
        if(null == language || language.length() == 0) {
            language=DEFAULT;
        }
        this.sort.put(language, sort);
    }

    public Long getSort() {
        Long s = sort.get(ThreadRole.getLanguage());
        if(null == s) {
            s = sort.get("fi");
        }
        if(null == s) {
            s = sort.get(DEFAULT);
        }
        return null == s ? 0 : s;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public void setProperty(String predicate, String language, String value) {
        if (properties.containsKey(predicate)) {
            properties.get(predicate).setValue(language, value);
        } else {
            Label property = new Label();
            label.setValue(language, value);
            properties.put(predicate, property);
        }
    }

    @Override
    public int compareTo(DimensionNode o) {
        return getSort().compareTo(o.getSort());
    }

    public void setSurrogateId(Integer surrogateId) {
        this.surrogateId = surrogateId;
    }

    public int getSurrogateId() {
        return surrogateId;
    }

    /**
     * Determines if this node is the child of the candidate.
     * 
     * @param parentCandidate
     *            possible parent of the node
     * @return
     *         <li>true if parent candidate is the parent of this node in any
     *         degree
     *         <li>false if parent canditate is this node or non ancestor
     */
    public boolean descendentOf(DimensionNode parentCandidate) {
        if (null == this.parent) {
            return false;
        }
        if (this.parent.getSurrogateId() == parentCandidate.getSurrogateId()) {
            return true;
        }
        return this.parent.descendentOf(parentCandidate);
    }

    /**
     * Adds a new password for the node. Node may contains multiple passwords if
     * multiple different roles may access the node or it's descendents.
     * 
     * @param value
     */
    public void setPassword(String value) {
        this.passwords.add(value);
    }

    /**
     * User can access the dimension node if it is not password protected or the
     * user has provided a password that matches any of the passwords defined
     * for the current node or it's ancestors
     * 
     * @return
     */
    public boolean canAccess() {
        if (!ThreadRole.isAuthenticated()) {
            return true;
        }
        if (this.passwords.isEmpty()) {
            if (this.parent == null) {
                return true;
            } else {
                return parent.canAccess();
            }
        }
        return ThreadRole.getRole().matches(passwords);
    }

    public void setReference(String key) {
        this.reference = key;
    }

    public String getReference() {
        return reference;
    }

    public void addEdge(String value, DimensionNode dimensionNode) {
        this.edges.put(value, dimensionNode);
    }

    public DimensionNode getConfidenceUpperLimitNode() {
        return edges.get(Constants.CONFIDENCE_INTERVAL_UPPER_LIMIT);
    }

    public DimensionNode getConfidenceLowerLimitNode() {
        return edges.get(Constants.CONFIDENCE_INTERVAL_LOWER_LIMIT);
    }

    public DimensionNode getSampleSizeNode() {
        return edges.get(Constants.SAMPLE_SIZE);
    }

    public boolean isDecimalsSet() {
       return 0 <= decimals;
    }
    /**
     * If no decimals metadata is provided then accuracy of 
     * value must be determined dynamically
     * @return
     */
    public int determineDecimals(String value) {
        if (isDecimalsSet()) {
            return decimals;
        } else {
            int separatorIndex = value.replace(",",".").indexOf('.');
            return separatorIndex >= 0 ? value.length() - separatorIndex - 1: 0;
        }
    }

}
