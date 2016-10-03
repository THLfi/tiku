package fi.thl.summary.model.hydra;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.MeasureItem;
import fi.thl.summary.model.SummaryMeasure;

public class MeasureExtension extends SummaryMeasure implements Extension {

    private final HydraSource source;
    private final SummaryMeasure delegate;
    private final HydraSummary summary;

    MeasureExtension(HydraSummary summary, HydraSource source, SummaryMeasure d) {
        this.source = source;
        this.delegate = d;
        this.summary = summary;
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

    public List<DimensionNode> getNodes() {
        List<DimensionNode> nodes = Lists.newArrayList();
        findMatchingNodesFromEachLevel(summary.getItemLanguage(), nodes);
        return nodes;
    }

    private void findMatchingNodesFromEachLevel(String language, List<DimensionNode> nodes) {
        for (MeasureItem mi : delegate.getMeasures()) {
            if (MeasureItem.Type.LABEL.equals(mi.getType())) {
                DimensionNode node = findNode(language, mi.getCode());
                if (null != node) {
                    nodes.add(node);
                }
            } else {
                nodes.addAll(((HydraFilter) summary.getSelection(mi.getCode())).getSelected());
            }
        }

    }

    private DimensionNode findNode(String language, String item) {
        for (fi.thl.pivot.model.Dimension dim : source.getMeasures()) {
            DimensionLevel level = dim.getRootLevel();
            while (!leafLevelReached(level)) {
                DimensionNode node = findMatchingNodes(language, level, item);
                if (null != node) {
                    return node;
                }
                level = level.getChildLevel();
            }
        }
        return null;
    }

    private DimensionNode findMatchingNodes(String language, DimensionLevel level, String item) {
        DimensionLevel iterableLevel = level;
        do {
            for (DimensionNode node : iterableLevel.getNodes()) {
                if (item.equals(node.getLabel().getValue(language))) {
                    return node;
                }
            }
            iterableLevel = iterableLevel.getChildLevel();
        } while (iterableLevel != null);
        throw new IllegalStateException("Could not find item from source " + item);
    }

    private boolean leafLevelReached(DimensionLevel level) {
        return level == null;
    }

}
