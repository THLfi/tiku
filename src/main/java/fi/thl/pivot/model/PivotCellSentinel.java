package fi.thl.pivot.model;

import java.util.List;

public class PivotCellSentinel implements PivotCell {

    private int row;
    private int column;

    public PivotCellSentinel(int row, int column) {
        this.row = row;
        this.column = column;
    }
    
    @Override
    public int compareTo(PivotCell o) {
        if(o instanceof PivotCellSentinel) {
            return 0;
        }
        return -1;
    }

    @Override
    public String getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRowNumber() {
        return row;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    @Override
    public boolean isNumber() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getNumberValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setIndices(List<List<Integer>> indices) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IDimensionNode getMeasure() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getConfidenceLowerLimit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getConfidenceUpperLimit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSampleSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPosition() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getI18nValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int determineDecimals() {
        // TODO Auto-generated method stub
        return 0;
    }
}
