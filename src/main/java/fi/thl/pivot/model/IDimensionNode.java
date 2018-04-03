package fi.thl.pivot.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDimensionNode  extends Comparable<IDimensionNode>{
    boolean isMeasure();

    Set<Map.Entry<String, Label>> getProperties();

    String getId();

    // FIXME: Ids should be permanent but currently root level nodes
    // are automatically created and this is the only way the id can
    // be set.
    void setId(String id);

    Label getLabel();

    Dimension getDimension();

    boolean isRootLevelNode();

    IDimensionNode getParent();

    Collection<IDimensionNode> getChildren();

    IDimensionNode getFirstChild();

    void sortChildren();

    void setParent(IDimensionNode node);

    void addChild(IDimensionNode node);

    void removeChild(IDimensionNode node);

    boolean ancestorOf(IDimensionNode node);

    DimensionLevel getLevel();

    void setSort(String language, Long sort);

    Long getSort();

    String getCode();

    void setCode(String code);

    int getDecimals();

    void setDecimals(int decimals);

    void setProperty(String predicate, String language, String value);

    void setSurrogateId(Integer surrogateId);

    int getSurrogateId();

    boolean descendentOf(IDimensionNode parentCandidate);

    void setPassword(String value);

    boolean canAccess();

    void setReference(String key);

    String getReference();

    void addEdge(String value, IDimensionNode dimensionNode);

    IDimensionNode getConfidenceUpperLimitNode();

    IDimensionNode getConfidenceLowerLimitNode();

    IDimensionNode getSampleSizeNode();

    boolean isDecimalsSet();

    int determineDecimals(String value);

    Label getProperty(String property);

    Limits getLimits();

    void setLimits(Limits limits);

    void hide();

    boolean isHidden();

    List<String> getPasswords();
}
