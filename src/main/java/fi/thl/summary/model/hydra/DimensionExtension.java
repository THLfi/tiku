package fi.thl.summary.model.hydra;

import java.util.Collections;
import java.util.List;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.SummaryDimension;

/**
 * Extends Dimension object by allowing the user to access the nodes in the
 * selected level
 * 
 * @author aleksiyrttiaho
 *
 */
public class DimensionExtension extends SummaryDimension implements Extension {

    private List<DimensionNode> nodes;

    DimensionExtension(HydraSource source, SummaryDimension d, List<DimensionNode> nodes) {
        super(d.getDimension(), d.getStage());
        Collections.sort(nodes);
        this.nodes = nodes;
        this.align(d.getValueAlign(), d.getHeaderAlign(), null);
    }

    public List<DimensionNode> getNodes() {
        return nodes;
    }

}