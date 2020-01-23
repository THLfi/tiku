package fi.thl.summary.model.hydra;

import java.util.List;
import java.util.Set;

import fi.thl.pivot.model.IDimensionNode;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.summary.model.MeasureItem;
import fi.thl.summary.model.SummaryMeasure;

public class MeasureExtension extends SummaryMeasure implements Extension {

    private static final Logger LOG = Logger.getLogger(MeasureExtension.class);
    
    private final HydraSource source;
    private final SummaryMeasure delegate;
    private final HydraSummary summary;

    MeasureExtension(HydraSummary summary, HydraSource source, SummaryMeasure d) {
        this.source = source;
        this.delegate = d;
        this.summary = summary;

        this.align(d.getValueAlign(), d.getHeaderAlign(), null);
    }

    public Set<MeasureItem> getMeasures() {
        return delegate.getMeasures();
    }

    @Override
    public String getDimension() {
        return "measure";
    }

    public void addMeasure(MeasureItem measure) {
        delegate.addMeasure(measure);
    }

    public List<IDimensionNode> getNodes() {
        List<IDimensionNode> nodes = Lists.newArrayList();
        findMatchingNodesFromEachLevel(summary.getItemLanguage(), nodes);

        return nodes;
    }
 
    private void findMatchingNodesFromEachLevel(String language, List<IDimensionNode> nodes) {
        for (MeasureItem mi : delegate.getMeasures()) {
            if (MeasureItem.Type.LABEL.equals(mi.getType())) {
                IDimensionNode node = findNode(language, mi.getCode());
                if (null != node && !nodes.contains(node)) {
                    nodes.add(node);
                }
            } else {
                List<IDimensionNode> nodesToAdd = ((HydraFilter) summary.getSelection(mi.getCode())).getSelected();
                for (IDimensionNode node : nodesToAdd) {
                    if (!nodes.contains(node)) {
                        nodes.add(node);
                    }
                }
            }
        }
    }

    public IDimensionNode findNode(String language, String item) {
        for (fi.thl.pivot.model.Dimension dim : source.getMeasures()) {
            DimensionLevel level = dim.getRootLevel();
            while (!leafLevelReached(level)) {
                IDimensionNode node = findMatchingNodes(language, level, item);
                if (null != node) {
                    return node;
                }
                level = level.getChildLevel();
            }
        }
        return null;
    }

    private IDimensionNode findMatchingNodes(String language, DimensionLevel level, String item) {
        DimensionLevel iterableLevel = level;
        do {
            for (IDimensionNode node : iterableLevel.getNodes()) {
                if (item.equals(node.getLabel().getValue(language))) {
                    return node;
                }
            }
            iterableLevel = iterableLevel.getChildLevel();
        } while (iterableLevel != null);
        LOG.warn("Could not find item from source " + item);
        return null;
        
    }

    private boolean leafLevelReached(DimensionLevel level) {
        return level == null;
    }

    @Override
    public List<IDimensionNode> getNodes(String stage) {
        // TODO Auto-generated method stub
        return null;
    }

}
