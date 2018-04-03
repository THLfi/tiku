package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.IDimensionNode;
import fi.thl.summary.model.SummaryDimension;
import fi.thl.summary.model.SummaryStage;

/**
 * Extends Dimension object by allowing the user to access the nodes in the
 * selected level
 * 
 * @author aleksiyrttiaho
 *
 */
public class DimensionExtension extends SummaryDimension implements Extension {

    private List<IDimensionNode> nodes;
    private SummaryDimension delegate;
    private HydraSource source;

    DimensionExtension(HydraSource source, SummaryDimension d, List<IDimensionNode> nodes) {
        super(d.getDimension(), d.getStage());
        this.delegate = d;
        this.source = source;
        Collections.sort(nodes);
        this.nodes = nodes;
        this.align(d.getValueAlign(), d.getHeaderAlign(), null);
    }

    public List<IDimensionNode> getNodes() {
        return nodes;
    }
    
    public List<IDimensionNode> getNodes(String stage) {
        DimensionLevel level = source.getDimension(delegate.getDimension()).getLevel(stage);
        return level == null ? Collections.emptyList() : level.getNodes();
    }

    public List<DimensionLevel> getLevels() {
        SummaryStage stage = delegate.getStage();
        if(SummaryStage.Type.STAGE.equals(stage.getType())) {
            Dimension dim = source.getDimension(stage.getDimensionId());
            List<DimensionLevel> levels = new ArrayList<>();
            for(String s : stage.getItems()) {
                 levels.add(dim.getLevel(s));
            }
            return levels;
        }
        return Collections.emptyList();
    }
}