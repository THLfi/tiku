package fi.thl.summary.model.hydra;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import fi.thl.pivot.model.IDimensionNode;
import fi.thl.summary.model.Presentation.SortMode;
import fi.thl.summary.model.Presentation.SuppressMode;

public class UrlBuilder {

    private StringBuilder sb = new StringBuilder();
    private String parameterName = "row";
    private final Joiner joiner = Joiner.on(".");

    public String toString() {
        return sb.toString();
    }

    public void addRows() {
        this.parameterName = "row";
    }

    public void addColumns() {
        this.parameterName = "column";
    }

    public void addFilters() {
        this.parameterName = "filter";
    }

    public void addParameter(String dimensionId, List<IDimensionNode> nodes) {
        if (!nodes.isEmpty()) {
            sb
                    .append("&")
                    .append(parameterName)
                    .append("=")
                    .append(dimensionId)
                    .append("-")
                    .append(joiner.join(Lists.transform(nodes, new NodeToId())))
                    .append(".");
        }
    }

    public void sort(SortMode sortMode) {
        sb.append("&mode=").append(sortMode.toString());
        sb.append("&sort=c0");
    }

    public void suppress(SuppressMode suppressMode) {
        if(null == suppressMode || SuppressMode.none.equals(suppressMode)) {
            
        } else if (SuppressMode.empty.equals(suppressMode)) {
            sb.append("&fo");
        } else if (SuppressMode.zero.equals(suppressMode)) {
            sb.append("&fz");
        } else if (SuppressMode.all.equals(suppressMode)) {
            sb.append("&fo&fz");
        }
    }

    public void showConfidenceInterval() {
        sb.append("&ci");
    }

    public void showSampleSize() {
        sb.append("&n");
    }
}
