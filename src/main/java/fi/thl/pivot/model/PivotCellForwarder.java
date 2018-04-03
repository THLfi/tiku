package fi.thl.pivot.model;

import java.util.List;

public class PivotCellForwarder implements PivotCell {

    private PivotCell delegate;
    private int row;
    private int column;

    public PivotCellForwarder(PivotCell cellAt, int row, int column) {
        this.delegate = cellAt;
        this.row = row;
        this.column = column;

    }

    public String getValue() {
        return delegate.getValue();
    }

    public int getRowNumber() {
        return row;
    }

    public int getActualRowNumber() {
        return delegate.getRowNumber();
    }

    public int getColumnNumber() {
        return column;
    }

    public int getActualColumnNumber() {
        return delegate.getRowNumber();
    }

    public boolean isNumber() {
        return delegate.isNumber();
    }

    public double getNumberValue() {
        return delegate.getNumberValue();
    }

    public void setIndices(List<List<Integer>> indices) {
        delegate.setIndices(indices);
    }

    public IDimensionNode getMeasure() {
        return delegate.getMeasure();
    }

    public String getConfidenceLowerLimit() {
        return delegate.getConfidenceLowerLimit();
    }

    public String getConfidenceUpperLimit() {
        return delegate.getConfidenceUpperLimit();
    }

    public String getSampleSize() {
        return delegate.getSampleSize();
    }

    public int getPosition() {
        return delegate.getPosition();
    }

    public int compareTo(PivotCell o) {
        return delegate.compareTo(o);
    }

    public String getI18nValue() {
        return delegate.getI18nValue();
    }

    @Override
    public int determineDecimals() {
        return delegate.determineDecimals();
    }

}
