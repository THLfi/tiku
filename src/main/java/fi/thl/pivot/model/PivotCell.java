package fi.thl.pivot.model;

import java.util.List;

public interface PivotCell extends Comparable<PivotCell> {

    String getValue();

    int getRowNumber();

    int getColumnNumber();

    boolean isNumber();

    double getNumberValue();

    void setIndices(List<List<Integer>> indices);

    DimensionNode getMeasure();

    String getConfidenceLowerLimit();

    String getConfidenceUpperLimit();

    String getSampleSize();

    int getPosition();

    String getI18nValue();
}
