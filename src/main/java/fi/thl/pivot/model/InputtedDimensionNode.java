package fi.thl.pivot.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InputtedDimensionNode implements IDimensionNode {

    private final IDimensionNode delegate;
    private static final Label label = new Label();
    static {
        label.setValue("fi", "Yhteensä");
        label.setValue("sv", "Totalt");
        label.setValue("en", "Total");
    }

    private final int level;
    private final boolean showName;

    public InputtedDimensionNode(IDimensionNode delegate, int level, boolean showName) {
        this.delegate = delegate;
        this.level = level;
        this.showName = showName;
    }

    public int getLevelNumber() {
        return level;
    }


    private IDimensionNode getDelegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return "InputtedDimensionNode{" +
                "delegate=" + delegate +
                '}';
    }

    @Override
    public boolean isMeasure() {
        return delegate.isMeasure();
    }

    @Override
    public Set<Map.Entry<String, Label>> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

    @Override
    public Label getLabel() {
       // NOTE: Not delegated!
        if(showName) {
            return delegate.getLabel();
        } else {
            return label;
        }
    }

    @Override
    public Dimension getDimension() {
        return delegate.getDimension();
    }

    @Override
    public boolean isRootLevelNode() {
        return delegate.isRootLevelNode();
    }

    @Override
    public IDimensionNode getParent() {
        return delegate.getParent();
    }

    @Override
    public Collection<IDimensionNode> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public IDimensionNode getFirstChild() {
        return delegate.getFirstChild();
    }

    @Override
    public void sortChildren() {
        delegate.sortChildren();
    }

    @Override
    public void setParent(IDimensionNode node) {
        delegate.setParent(node);
    }

    @Override
    public void addChild(IDimensionNode node) {
        delegate.addChild(node);
    }

    @Override
    public void removeChild(IDimensionNode node) {
        delegate.removeChild(node);
    }

    @Override
    public boolean ancestorOf(IDimensionNode node) {
        return delegate.ancestorOf(node);
    }

    @Override
    public DimensionLevel getLevel() {
        return delegate.getLevel();
    }

    @Override
    public void setSort(String language, Long sort) {
        delegate.setSort(language, sort);
    }

    @Override
    public Long getSort() {
        return delegate.getSort();
    }

    @Override
    public String getCode() {
        return delegate.getCode();
    }

    @Override
    public void setCode(String code) {
        delegate.setCode(code);
    }

    @Override
    public int getDecimals() {
        return delegate.getDecimals();
    }

    @Override
    public void setDecimals(int decimals) {
        delegate.setDecimals(decimals);
    }

    @Override
    public void setProperty(String predicate, String language, String value) {
        delegate.setProperty(predicate, language, value);
    }

    @Override
    public void setSurrogateId(Integer surrogateId) {
        delegate.setSurrogateId(surrogateId);
    }

    @Override
    public int getSurrogateId() {
        return delegate.getSurrogateId();
    }

    @Override
    public boolean descendentOf(IDimensionNode parentCandidate) {
        return delegate.descendentOf(parentCandidate);
    }

    @Override
    public void setPassword(String value) {
        delegate.setPassword(value);
    }

    @Override
    public boolean canAccess() {
        return delegate.canAccess();
    }

    @Override
    public void setReference(String key) {
        delegate.setReference(key);
    }

    @Override
    public String getReference() {
        return delegate.getReference();
    }

    @Override
    public void addEdge(String value, IDimensionNode dimensionNode) {
        delegate.addEdge(value, dimensionNode);
    }

    @Override
    public IDimensionNode getConfidenceUpperLimitNode() {
        return delegate.getConfidenceUpperLimitNode();
    }

    @Override
    public IDimensionNode getConfidenceLowerLimitNode() {
        return delegate.getConfidenceLowerLimitNode();
    }

    @Override
    public IDimensionNode getSampleSizeNode() {
        return delegate.getSampleSizeNode();
    }

    @Override
    public boolean isDecimalsSet() {
        return delegate.isDecimalsSet();
    }

    @Override
    public int determineDecimals(String value) {
        return delegate.determineDecimals(value);
    }

    @Override
    public Label getProperty(String property) {
        return delegate.getProperty(property);
    }

    @Override
    public Limits getLimits() {
        return delegate.getLimits();
    }

    @Override
    public void setLimits(Limits limits) {
        delegate.setLimits(limits);
    }

    @Override
    public void hide() {
        delegate.hide();
    }

    @Override
    public boolean isHidden() {
        return delegate.isHidden();
    }

    @Override
    public List<String> getPasswords() {
        return delegate.getPasswords();
    }

    @Override
    public int compareTo(IDimensionNode o) {
        return delegate.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if (obj instanceof IDimensionNode) {
            return delegate.getSurrogateId() == ((IDimensionNode) obj).getSurrogateId();
        } else {
            return false;
        }
    }
}
