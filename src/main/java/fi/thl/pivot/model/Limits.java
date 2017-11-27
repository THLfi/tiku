package fi.thl.pivot.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Joiner;

public class Limits {

    private DimensionNode limitMeasure;
    private Map<Integer, Double> limits = new TreeMap<>();
    private Map<Integer, Double> area = new TreeMap<>();
    private Map<Integer, Label> labels = new TreeMap<>();

    private boolean isAscendingLimitOrder = true;
    private boolean isLimitLowerOrEqualTo = true;

    /**
     * Provides a reference to a measure that
     * contain limit values of a meaasure.
     * @return
     */
    public DimensionNode getLimitMeasure() {
        return limitMeasure;
    }

    public void setLimitMeasure(DimensionNode dimensionNode) {
        this.limitMeasure = dimensionNode;
    }

    /**
     * Provides the limits for measure values that
     * define i.e. the quintile, quartile or some other
     * division of measure values. These may used to
     * differentiate or color measure values in different
     * visualizations.The limits are listed in natural
     * order by the limit number
     * @return
     */
    public Collection<Double> getLimits() {
        return limits.values();
    }

    /**
     * Adds a new limit to the set of limits (see. {@link getLimits}). Limits
     * are added in natural order of {@link order}.
     * If the same order is added multiple times then
     * the latest limit value is applied. If order numbers
     * are skipped then the limits are compacted e.g.
     * orders 1, 2, 4 are equivalent to orders 0,1,2
     * 
     * @param order
     * @param limit
     */
    public void setLimit(int order, double limit) {
        this.limits.put(order, limit);
    }

    public void setLimitOrder(String value) {
        if (null != value) {
            isAscendingLimitOrder = !("desc".equalsIgnoreCase(value));
        }
    }

    public boolean isAscendingOrder() {
        return isAscendingLimitOrder;
    }

    public void setLimitBound(String value) {
        if (null != value) {
            isLimitLowerOrEqualTo = !("upper".equalsIgnoreCase(value));
        }
    }

    public boolean isLessThanOrEqualTo() {
        return isLimitLowerOrEqualTo;
    }

    @Override
    public String toString() {
        return Joiner.on(',').join(getLimits());
    }

    public void setLabel(int index, String language, String value) {
        if(!labels.containsKey(index)) {
            labels.put(index, new Label());
        }
        labels.get(index).setValue(language, value);
    }

    public Label getLabel(int index) {
        return labels.get(index);
    }

    public Collection<Label> getLabels() {
        return labels.values();
    }
    public Collection<Double> getAreas() { return area.values(); }


    public void setLimitArea(int order, double area) {
        this.area.put(order, area);
    }
}
