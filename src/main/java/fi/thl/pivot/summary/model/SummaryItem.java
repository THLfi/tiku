package fi.thl.pivot.summary.model;

public interface SummaryItem {

    String getHeaderAlign();

    String getValueAlign();

    void align(String valueAlign, String headerAlign, String allAlign);

}
