package fi.thl.pivot.model;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import fi.thl.pivot.util.Constants;
import fi.thl.pivot.util.ThreadRole;

/**
 * Holds metadata and hierarchy of each node in a dimension
 * 
 * @author aleksiyrttiaho
 * 
 */
public class DimensionNode implements IDimensionNode {

    private static final Collator COLLATOR = Collator.getInstance(new Locale("fi"));
    private static final String DEFAULT = "default";
    private final DimensionLevel level;
    private String id;
    private final Label label;
    private final List<IDimensionNode> children;
    private IDimensionNode parent;

    private Map<String, Long> sort = new HashMap<>();
    private String code;
    private int decimals = -1;
    private Map<String, Label> properties = new HashMap<>();
    private int surrogateId;
    private int hashCode;
    private List<String> passwords = new ArrayList<>();
    private String reference;
    private Map<String, IDimensionNode> edges = new HashMap<>();

    private Limits limits;
    private boolean hidden;

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

    @Override
    public boolean isMeasure() {
        return level.getDimension().isMeasure();
    }

    @Override
    public Set<Map.Entry<String, Label>> getProperties() {
        return properties.entrySet();
    }

    @Override
    public String getId() {
        return id;
    }

    // FIXME: Ids should be permanent but currently root level nodes
    // are automatically created and this is the only way the id can
    // be set.
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public Dimension getDimension() {
        return level.getDimension();
    }

    @Override
    public boolean isRootLevelNode() {
        return getDimension().getRootLevel().getNodes().contains(this);
    }

    @Override
    public IDimensionNode getParent() {
        return parent;
    }

    @Override
    public Collection<IDimensionNode> getChildren() {
        final boolean canAccess = canAccess();
        return children.stream().sorted().filter(new Predicate<IDimensionNode>() {

            @Override
            public boolean apply(IDimensionNode input) {
                if (!canAccess) {
                    return false;
                }
                if (input.getPasswords().isEmpty()) {
                    return true;
                }
                if (ThreadRole.getRole() != null && ThreadRole.getRole().matches(input.getPasswords())) {
                    return true;
                }
                return false;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public IDimensionNode getFirstChild() {
        return new ArrayList<>(getChildren()).get(0);
    }

    @Override
    public void sortChildren() {
        Collections.sort(children);
    }

    @Override
    public void setParent(final IDimensionNode node) {
        if (null != parent) {
            parent.removeChild(this);
        }
        parent = node;
        if (null != parent) {
            parent.addChild(this);
        }
    }

    @Override
    public void addChild(IDimensionNode node) {
        this.children.add(node);
    }

    @Override
    public void removeChild(IDimensionNode node) {
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
        IDimensionNode other = (IDimensionNode) obj;
        return surrogateId == other.getSurrogateId();
    }

    @Override
    public String toString() {
        return "DimensionNode [id=" + id + ", label=" + label + ", surrogateId=" + surrogateId + "]";
    }

    @Override
    public boolean ancestorOf(IDimensionNode node) {
        if (node.equals(this)) {
            return true;
        }
        if(node.getSurrogateId() == getSurrogateId()) {
            // FIXME: This is needed because of a bug in equals.
            return true;
        }
        IDimensionNode aParent = node.getParent();
        while (aParent != null) {
            if (aParent == this || aParent.equals(this)) {
                return true;
            }
            aParent = aParent.getParent();
        }
        return false;
    }

    @Override
    public List<String> getPasswords() {
        return passwords;
    }

    @Override
    public DimensionLevel getLevel() {
        return this.level;
    }

    @Override
    public void setSort(String language, Long sort) {
        if (null == language || language.length() == 0) {
            language = DEFAULT;
        }
        this.sort.put(language, sort);
    }

    @Override
    public Long getSort() {
        Long s = sort.get(ThreadRole.getLanguage());
        if (null == s) {
            s = sort.get("fi");
        }
        if (null == s) {
            s = sort.get(DEFAULT);
        }
        return null == s ? 0 : s;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public int getDecimals() {
        return decimals;
    }

    @Override
    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    @Override
    public void setProperty(String predicate, String language, String value) {
        if (properties.containsKey(predicate)) {
            properties.get(predicate).setValue(language, value);
        } else {
            Label property = new Label();
            property.setValue(language, value);
            properties.put(predicate, property);
        }

    }

    @Override
    public int compareTo(IDimensionNode o) {
        if(this.hidden && !o.isHidden()) {
            return 1;
        }
        if(!this.hidden && o.isHidden()) {
            return -1;
        }
        if (this.sort.isEmpty()) {
            String s1 = getLabel().getValue(ThreadRole.getLanguage());
            String s2 = o.getLabel().getValue(ThreadRole.getLanguage());
            return COLLATOR.compare(s1, s2);
        } else {
            return getSort().compareTo(o.getSort());
        }
    }

    @Override
    public void setSurrogateId(Integer surrogateId) {
        this.surrogateId = surrogateId;
    }

    @Override
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
    @Override
    public boolean descendentOf(IDimensionNode parentCandidate) {
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
    @Override
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
    @Override
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

    @Override
    public void setReference(String key) {
        this.reference = key;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void addEdge(String value, IDimensionNode dimensionNode) {
        this.edges.put(value, dimensionNode);
    }

    @Override
    public IDimensionNode getConfidenceUpperLimitNode() {
        return edges.get(Constants.CONFIDENCE_INTERVAL_UPPER_LIMIT);
    }

    @Override
    public IDimensionNode getConfidenceLowerLimitNode() {
        return edges.get(Constants.CONFIDENCE_INTERVAL_LOWER_LIMIT);
    }

    @Override
    public IDimensionNode getSampleSizeNode() {
        return edges.get(Constants.SAMPLE_SIZE);
    }

    @Override
    public boolean isDecimalsSet() {
        return 0 <= decimals;
    }

    /**
     * If no decimals metadata is provided then accuracy of
     * value must be determined dynamically
     * @return
     */
    @Override
    public int determineDecimals(String value) {
        if (isDecimalsSet()) {
            return decimals;
        } else {
            int separatorIndex = value.replace(",", ".").indexOf('.');
            return separatorIndex >= 0 ? value.length() - separatorIndex - 1 : 0;
        }
    }

    @Override
    public Label getProperty(String property) {
        if ("code".equals(property)) {
            Label l = new Label();
            l.setValue("fi", getCode());
            return l;
        } else if ("label".equals(property)) {
            return getLabel();
        } else {
            return properties.get(property);
        }
    }

    @Override
    public Limits getLimits() {
        return limits;
    }

    @Override
    public void setLimits(Limits limits) {
        this.limits = limits;
    }

    @Override
    public void hide() {
        this.hidden = true;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }
}
