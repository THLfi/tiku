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

    public SummaryItem addColumn(String dimension, SummaryStage stage, boolean includeTotal) {
        SummaryDimension dim = new SummaryDimension(dimension, stage, includeTotal);
        columns.add(dim);
        return dim;
    }

    public SummaryItem addColumn(List<MeasureItem> measures) {
        SummaryMeasure item = createMeasure(measures);
        columns.add(item);
        return item;
    }

    public SummaryItem addRow(String dimension, SummaryStage stage, boolean includeTotal) {
        SummaryDimension dim = new SummaryDimension(dimension, stage, includeTotal);
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
