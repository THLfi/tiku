package fi.thl.summary.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a presentation in Amor specification that is generated using the
 * underlying data cube. The presentation describes which elements should be
 * used as data axis and which filters should be applied when generating a query
 * to the Pivot API.
 * 
 * @author aleksiyrttiaho
 *
 */
public class DataPresentation implements Presentation {

    private String id;
    private String type;
    private String palette;
    private List<SummaryItem> dimensions = new ArrayList<>();
    private SummaryMeasure measures = new SummaryMeasure();
    private List<Selection> filter = new ArrayList<>();
    private Presentation.SortMode sortMode = SortMode.none;

    private Integer min, max;
    private int groupSize = 1;
    private boolean isFirst, isLast;
    private SuppressMode suppress;
    private boolean showConfidenceInterval;
    private boolean showSampleSize;
    private List<String> emphasize = new ArrayList<>();
    private String geometry;

    private Rule rule;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addDimension(String dimension, SummaryStage stage) {
        dimensions.add(new SummaryDimension(dimension, stage));
    }

    public List<SummaryItem> getDimensions() {
        return dimensions;
    }

    public void addFilter(Selection selection) {
        filter.add(selection);
    }

    public List<Selection> getFilters() {
        return filter;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    public void addMeasures(List<MeasureItem> measure) {
        for (MeasureItem mi : measure) {
            measures.addMeasure(mi);
        }
    }

    public SummaryMeasure getMeasures() {
        return measures;
    }

    /**
     * @return true if at least one measure has been defined for this
     *         presentation
     */
    public boolean hasMeasures() {
        return !measures.getMeasures().isEmpty();
    }

    public Presentation.SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(Presentation.SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public void setSuppress(SuppressMode suppress) {
        this.suppress = suppress;
    }

    public SuppressMode getSuppress() {
        return this.suppress;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getPalette() {
        return palette;
    }

    public void setPalette(String colorScheme) {
        this.palette = colorScheme;
    }

    public void setShowConfidenceInterval(boolean showConfidenceInterval) {
        this.showConfidenceInterval = showConfidenceInterval;
    }

    public void setShowSampleSize(boolean showSampleSize) {
        this.showSampleSize = showSampleSize;
    }

    public boolean getShowConfidenceInterval() {
        return showConfidenceInterval;
    }

    public boolean isShowSampleSize() {
        return showSampleSize;
    }

    public List<String> getEmphasize() {
        return emphasize;
    }

    public void addEmphasize(String id) {
        this.emphasize.add(id);
    }

    public void setGeometry(String attribute) {
        this.geometry = attribute;
    }

    public String getGeometry() {
        return geometry;
    }


    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public Rule getRule() {
        return rule;
    }
}
