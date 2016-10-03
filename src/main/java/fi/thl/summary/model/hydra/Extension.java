package fi.thl.summary.model.hydra;

import java.util.List;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.SummaryItem;

public interface Extension extends SummaryItem {

    List<DimensionNode> getNodes();
    String getDimension();
}
