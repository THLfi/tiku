package fi.thl.summary.model;

import java.util.List;

import com.google.common.collect.Lists;

public class TablePresentation extends DataPresentation {

    private List<SummaryItem> columns = Lists.newArrayList();
    private List<SummaryItem> rows = Lists.newArrayList();

    public List<SummaryItem> getColumns() {
        return columns;
    }

    public List<SummaryItem> getRows() {
        return rows;
    }

    public void addColumn(String dimension, SummaryStage stage, boolean includeTotal) {
        columns.add(new SummaryDimension(dimension, stage, includeTotal));
    }

    public void addColumn(List<MeasureItem> measures) {
        columns.add(createMeasure(measures));
    }

    public void addRow(String dimension, SummaryStage stage, boolean includeTotal) {
        rows.add(new SummaryDimension(dimension, stage, includeTotal));
    }

    public void addRow(List<MeasureItem> measures) {
        rows.add(createMeasure(measures));
    }

    private SummaryMeasure createMeasure(List<MeasureItem> measures) {
        SummaryMeasure m = new SummaryMeasure();
        for (MeasureItem measure : measures) {
            m.addMeasure(measure);
        }
        return m;
    }
}
