package fi.thl.summary.model.hydra;

import java.util.List;

import fi.thl.pivot.model.IDimensionNode;
import fi.thl.summary.model.SummaryItem;

public interface Extension extends SummaryItem {

    List<IDimensionNode> getNodes();
    String getDimension();
    List<IDimensionNode> getNodes(String stage);
}
