package fi.thl.pivot.summary.model;

public abstract class AbstractSummaryItem implements SummaryItem {

    private String headerAlign;
    private String valueAlign;

    public String getHeaderAlign() {
        return headerAlign;
    }

    public String getValueAlign() {
        return valueAlign;
    }

    @Override
    public void align(String valueAlign, String headerAlign, String allAlign) {
        if (null != allAlign) {
            valueAlign = allAlign;
            headerAlign = allAlign;
        }
        if (!isValidAlign(valueAlign)) {
            valueAlign = "center";
        }
        if (!isValidAlign(headerAlign)) {
            headerAlign = "center";
        }
        this.valueAlign = (valueAlign == null ? "center" : valueAlign);
        this.headerAlign = (headerAlign == null ? "center" : headerAlign);
    }

    private boolean isValidAlign(String align) {
        return ("right".equalsIgnoreCase(align) || "center".equalsIgnoreCase(align) || "left".equals(align));
    }
}
