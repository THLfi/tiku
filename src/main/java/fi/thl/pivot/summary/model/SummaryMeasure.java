package fi.thl.pivot.summary.model;

import java.util.Set;

import com.google.common.collect.Sets;

public class SummaryMeasure extends AbstractSummaryItem implements SummaryItem{

    private Set<MeasureItem> measures = Sets.newLinkedHashSet();

    public Set<MeasureItem> getMeasures() {
        return measures;
    }

    public void addMeasure(MeasureItem measure) {
        this.measures.add(measure);
    }

    @Override
    public String toString() {
        return "SummaryMeasure [measures=" + measures + "]";
    }

}
