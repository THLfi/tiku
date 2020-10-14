package fi.thl.pivot.summary.model.hydra;

import java.util.List;

import fi.thl.pivot.model.IDimensionNode;
import fi.thl.pivot.summary.model.SummaryItem;

public interface Extension extends SummaryItem {

    List<IDimensionNode> getNodes();
    String getDimension();
    List<IDimensionNode> getNodes(String stage);
}
