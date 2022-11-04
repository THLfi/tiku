package fi.thl.pivot.summary.model.hydra;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import fi.thl.pivot.model.IDimensionNode;
import fi.thl.pivot.summary.model.Presentation.SortMode;
import fi.thl.pivot.summary.model.Presentation.SuppressMode;
import fi.thl.pivot.util.PivotUtils;

public class UrlBuilder {

    private StringBuilder sb = new StringBuilder();
    private String parameterName = "row";
    private final Joiner joiner = Joiner.on(".");
    private final Logger logger = LoggerFactory.getLogger(UrlBuilder.class);

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
            List<Integer> joined = Lists.transform(nodes, new NodeToId());
            String packedParams = PivotUtils.packAndZip(joined);
            logger.debug("Dimension nodes length before packing: " + joiner.join(joined).length());
            logger.debug("Dimension nodes length after packing: " + packedParams.length());

            sb
            .append("&")
            .append(parameterName)
            .append("=")
            .append(dimensionId)
            .append("-")
            .append(packedParams)
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

    public String getParameterName() {
        return parameterName;
    }
}
