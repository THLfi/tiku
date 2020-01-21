package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

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
    private List<IDimensionNode> totalNodes;
    private SummaryDimension delegate;
    private HydraSource source;

    DimensionExtension(HydraSource source, SummaryDimension d, List<IDimensionNode> baseNodes, List<IDimensionNode> totalNodes) {
        super(d.getDimension(), d.getStage(), d.getTotalMode());
        this.delegate = d;
        this.source = source;
        combineBaseAndTotalNodes(baseNodes, totalNodes);
        this.align(d.getValueAlign(), d.getHeaderAlign(), null);
    }

    DimensionExtension(HydraSource source, SummaryDimension d, List<IDimensionNode> nodes) {
        this(source, d, nodes, null);
    }

    public List<IDimensionNode> getNodes() {
        return nodes;
    }
    
    public List<IDimensionNode> getTotalHighlightNodes() {
        if (getTotalMode() == TotalMode.HIGHLIGHT) {
            return totalNodes;
        } else {
            return null;
        }
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

    private void combineBaseAndTotalNodes(List<IDimensionNode> baseNodes, List<IDimensionNode> totalNodes) {
        this.nodes = Lists.newArrayList();
        nodes.addAll(baseNodes);
        Collections.sort(nodes);

        if (totalNodes != null) {
            Collections.sort(totalNodes);
            this.totalNodes = totalNodes;
            nodes.addAll(totalNodes);
        } else {
            addRootLevelNodesAsTotal(nodes);
        }
    }

    private void addRootLevelNodesAsTotal(List<IDimensionNode> nodes) {
        this.totalNodes = Lists.newArrayList();

        for (IDimensionNode node : Lists.newArrayList(nodes)) {
            if (node.isRootLevelNode()) {
                // assign total status
                totalNodes.add(node);
                // move to the end of the list
                nodes.remove(node);
                nodes.add(node);
            }
        }
    }
}