package fi.thl.pivot.web;

import java.util.List;

/**
 * Represent common parameters that are passed to different cube requests
 * 
 * @author aleksiyrttiaho
 *
 */
public class CubeRequest extends AbstractRequest {

    private static final String CUBE_PREFIX = "fact_";
    private List<String> columnHeaders;
    private List<String> filterValues;
    private List<String> measureValues;

    private String fo;
    private String fz;
    private String ci;
    private String n;
    private String sortNode;
    private String sortMode = "desc";
    private String searchType = "su";

    private List<String> rowHeaders;
    private String showCodes;

    public List<String> getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(List<String> rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    public List<String> getColumnHeaders() {
        return columnHeaders;
    }

    @Override
    public String toString() {
        return "CubeRequest [rowHeaders=" + rowHeaders + ", columnHeaders=" + columnHeaders + ", filterValues=" + filterValues + ", measureValues="
                + measureValues + ", fo=" + fo + ", fz=" + fz + ", sortNode=" + sortNode + ", sortMode=" + sortMode + ", searchType=" + searchType + ", locale="
                + locale + "]";
    }

    public void setColumnHeaders(List<String> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public List<String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(List<String> filterValues) {
        this.filterValues = filterValues;
    }

    public List<String> getMeasureValues() {
        return measureValues;
    }

    public void setMeasureValues(List<String> measureValues) {
        this.measureValues = measureValues;
    }

    public String getFilterEmptyValues() {
        return fo;
    }

    public void setFilterEmptyCells(String fo) {
        this.fo = fo;
    }

    public String getFilterZeroes() {
        return fz;
    }

    public void setFilterZeroes(String fz) {
        this.fz = fz;
    }

    public String getSortNode() {
        return sortNode;
    }

    public void setSortNode(String sortNode) {
        this.sortNode = sortNode;
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getCubeUrl() {
        return getCubeUrl(locale.getLanguage());
    }

    public String getCubeUrl(String language) {
        StringBuilder sb = new StringBuilder();
        sb.append(env).append("/");
        sb.append(language).append("/");
        sb.append(subject).append("/");
        sb.append(hydra).append("/");
        sb.append(cube).append("?");
        if (!"latest".equals(runId)) {
            sb.append("&amp;runid=").append(runId);
        }
        for (String row : rowHeaders) {
            sb.append("&amp;row=").append(row);
        }
        for (String column : columnHeaders) {
            sb.append("&amp;column=").append(column);
        }
        for (String filter : filterValues) {
            sb.append("&amp;filter=").append(filter);
        }
        if (null != getFilterEmptyValues()) {
            sb.append("&amp;fo");
        }
        if (null != getFilterZeroes()) {
            sb.append("&amp;fz");
        }
        if("1".equals(getShowCodes())) {
            sb.append("&amp;sc");
        }
        if (null != sortNode) {
            sb.append("&amp;sort=").append(sortNode);
            sb.append("&amp;mode=").append(sortMode);
        }
        if (!"su".equals(searchType)) {
            sb.append("&amp;search=").append(searchType);
        }
        return sb.toString();
    }

    public String getDimensionsUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(env).append("/");
        sb.append(locale).append("/");
        sb.append(subject).append("/");
        sb.append(hydra).append("/");
        sb.append(cube).append(".dimensions.json");
        if (!"latest".equals(runId)) {
            sb.append("?runid=").append(runId);
        }
        return sb.toString();
    }

    public void setShowCodes(String showCodes) {
        this.showCodes = showCodes;
    }

    public String getShowCodes() {
        return showCodes;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getN() {
        return n;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getCi() {
        return ci;
    }

    @Override
    public void setCube(String cube) {
        if (cube.startsWith(CUBE_PREFIX)) {
            super.setCube(cube);
        } else {
            super.setCube(CUBE_PREFIX + cube);
        }
    }

    @Override
    public String toDataUrl() {
        StringBuilder sb = new StringBuilder();
        for (String row : rowHeaders) {
            sb.append(String.format("&amp;row=%s", row));
        }
        for (String column : columnHeaders) {
            sb.append(String.format("&amp;column=%s", column));
        }
        for (String filter : filterValues) {
            sb.append(String.format("&amp;filter=%s", filter));
        }
        if (fo != null) {
            sb.append("&amp;fo");
        }
        if (fz != null) {
            sb.append("&amp;fz");
        }
        if(ci != null) {
            sb.append("&amp;ci");
        }
        if(n != null) {
            sb.append("&amp;n");
        }
        return sb.toString();
    }

}
