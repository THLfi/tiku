package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryDimension;
import fi.thl.summary.model.SummaryItem;
import fi.thl.summary.model.SummaryMeasure;
import fi.thl.summary.model.SummaryStage;

final class ExtendDimensionFunction implements Function<SummaryItem, SummaryItem> {

    private HydraSource source;
    private HydraSummary summary;

    public ExtendDimensionFunction(HydraSummary summary, HydraSource source) {
        this.source = source;
        this.summary = summary;
    }

    @Override
    public SummaryItem apply(final SummaryItem d) {
        if (d instanceof SummaryDimension) {
            final SummaryDimension dim = (SummaryDimension) d;
            if (summary.isDrilled(((SummaryDimension) d).getDimension())) {
                return new DimensionExtension(source, dim, drilledNodesIn(dim));
            } else {
                return extendStage(dim);
            }
        } else {
            return new MeasureExtension(summary, source, (SummaryMeasure) d);
        }
    }

    private SummaryItem extendStage(final SummaryDimension dim) {
        SummaryStage stage = dim.getStage();
        if (SummaryStage.Type.STAGE.equals(stage.getType())) {
            if (":filter:".equals(stage.getStage())) {
                return new DimensionExtension(source, dim, Lists.newArrayList(summary.getSelectionByDimension(dim.getDimension())));
            } else if (":all:".equals(stage.getStage())) {
                return new DimensionExtension(source, dim, allNodesIn(dim));
            } else {
                return new DimensionExtension(source, dim, findNodes(dim.getDimension(), stage.getStage(), dim.includeTotal()));
            }
        } else {
            return new DimensionExtension(source, dim, findNodes(dim.getDimension(), stage.getItems()));
        }
    }

    private List<DimensionNode> drilledNodesIn(final SummaryDimension dim) {
        List<DimensionNode> nodes = Lists.newArrayList();
        for (DimensionNode drilled : summary.getDrilledNodes(dim.getDimension())) {
            nodes.addAll(drilled.getChildren());
            if (dim.includeTotal()) {
                nodes.add(drilled);
            }
        }

        return nodes;
    }

    private List<DimensionNode> allNodesIn(final SummaryDimension dim) {
        List<DimensionNode> nodes = new ArrayList<>();
        for (Dimension dimension : source.getDimensions()) {
            if (dimension.getId().equals(dim.getDimension())) {
                addDepthFirst(nodes, Lists.newArrayList(dimension.getRootNode()));
            }
        }
        return nodes;
    }

    private void addDepthFirst(List<DimensionNode> nodes, Collection<DimensionNode> candidates) {
        for (DimensionNode c : candidates) {
            nodes.add(c);
            addDepthFirst(nodes, c.getChildren());
        }

    }

    private List<DimensionNode> findNodes(String dimension, String stage, boolean includeTotal) {
        for (Dimension dim : source.getDimensionsAndMeasures()) {
            if (dim.getId().equals(dimension)) {
                DimensionLevel level = dim.getRootLevel();
                while (level != null && !level.getId().equals(stage)) {
                    level = level.getChildLevel();
                }
                if (null != level) {
                    List<DimensionNode> nodes = Lists.newArrayList(level.getNodes());
                    if (includeTotal) {
                        nodes.add(level.getNodes().get(0).getParent());
                    }
                    return nodes;
                } else {
                    break;
                }
            }
        }
        return Collections.emptyList();
    }

    private List<DimensionNode> findNodes(String dim, List<String> items) {
        List<DimensionNode> nodes = new ArrayList<>();
        for (Dimension dimension : source.getDimensionsAndMeasures()) {
            if (dimension.getId().equals(dim)) {
                if (Summary.Scheme.Reference.equals(summary.getScheme())) {
                    for (String i : items) {
                        nodes.add(dimension.getNode(i));
                    }
                } else {
                    findNodesByName(dimension, items, nodes);
                }
                break;
            }
        }
        return nodes;
    }

    private void findNodesByName(Dimension dimension, List<String> items, List<DimensionNode> nodes) {
        DimensionLevel level = dimension.getRootLevel();
        List<String> itemsNotFound = new ArrayList<>(items);
        while (level != null) {
            for (DimensionNode node : level.getNodes()) {
                for (Iterator<String> i = items.iterator(); i.hasNext();) {
                    String item = i.next();
                    if (node.getLabel().getValue(summary.getItemLanguage()).equals(item)) {
                        nodes.add(node);
                        if (itemsNotFound.isEmpty()) {
                            return;
                        }
                        break;
                    }
                }
            }
            level = level.getChildLevel();
        }
    }

}