package fi.thl.summary.model;

/**
 * 
 * Represents a single level in the dimension tree in Hydra 4+ specification
 * 
 * @author aleksiyrttiaho
 *
 */
public class SummaryDimension extends AbstractSummaryItem implements SummaryItem {

    private final String dimension;
    private final SummaryStage stage;
    private boolean includeTotal;

    protected SummaryDimension(String dimension, SummaryStage stage) {
        this(dimension, stage, false);
    }

    protected SummaryDimension(String dimension, SummaryStage stage, boolean includeTotal) {
        this.dimension = dimension;
        this.stage = stage;
        this.includeTotal = includeTotal;
    }

    public String getDimension() {
        return dimension;
    }

    public SummaryStage getStage() {
        return stage;
    }

    public boolean includeTotal() {
        return includeTotal;
    }

    @Override
    public String toString() {
        return "SummaryDimension [dimension=" + dimension + ", stage=" + stage + ", includeTotal=" + includeTotal + "]";
    }

}