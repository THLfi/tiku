package fi.thl.pivot.summary.model;

import java.util.List;

import com.google.common.collect.Lists;

import fi.thl.pivot.summary.model.SummaryDimension.TotalMode;

public class TablePresentation extends DataPresentation {

    private List<SummaryItem> columns = Lists.newArrayList();
    private List<SummaryItem> rows = Lists.newArrayList();

    public List<SummaryItem> getColumns() {
        return columns;
    }

    public List<SummaryItem> getRows() {
        return rows;
    }

    public SummaryItem addColumn(String dimension, SummaryStage stage, TotalMode totalMode) {
        SummaryDimension dim = new SummaryDimension(dimension, stage, totalMode);
        columns.add(dim);
        return dim;
    }

    public SummaryItem addColumn(List<MeasureItem> measures) {
        SummaryMeasure item = createMeasure(measures);
        columns.add(item);
        return item;
    }

    public SummaryItem addRow(String dimension, SummaryStage stage, TotalMode totalMode) {
        SummaryDimension dim = new SummaryDimension(dimension, stage, totalMode);
        rows.add(dim);
        return dim;
    }

    public SummaryItem addRow(List<MeasureItem> measures) {
        SummaryMeasure item = createMeasure(measures);
        rows.add(item);
        return item;
    }

    private SummaryMeasure createMeasure(List<MeasureItem> measures) {
        SummaryMeasure m = new SummaryMeasure();
        for (MeasureItem measure : measures) {
            m.addMeasure(measure);
        }
        return m;
    }

}
