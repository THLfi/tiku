package fi.thl.pivot.summary.model;

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
    private TotalMode totalMode;
    public enum TotalMode {
        NO,
        YES,
        HIGHLIGHT;

        public boolean includeTotal() {
            return this != NO;
        }
    }

    protected SummaryDimension(String dimension, SummaryStage stage) {
        this(dimension, stage, TotalMode.NO);
    }

    protected SummaryDimension(String dimension, SummaryStage stage, TotalMode includeTotal) {
        this.dimension = dimension;
        this.stage = stage;
        this.totalMode = includeTotal;
    }

    public String getDimension() {
        return dimension;
    }

    public SummaryStage getStage() {
        return stage;
    }

    public boolean includeTotal() {
        return totalMode.includeTotal();
    }

    public TotalMode getTotalMode() {
		return totalMode;
    }

    @Override
    public String toString() {
        return "SummaryDimension [dimension=" + dimension + ", stage=" + stage + ", includeTotal=" + totalMode + "]";
    }

}
