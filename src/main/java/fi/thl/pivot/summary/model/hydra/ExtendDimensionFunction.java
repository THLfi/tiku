package fi.thl.pivot.summary.model.hydra;

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
import fi.thl.pivot.model.IDimensionNode;
import fi.thl.pivot.summary.model.Summary;
import fi.thl.pivot.summary.model.SummaryDimension;
import fi.thl.pivot.summary.model.SummaryItem;
import fi.thl.pivot.summary.model.SummaryMeasure;
import fi.thl.pivot.summary.model.SummaryStage;

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
                return createDrilledDimensionExtension(dim);
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
                HydraFilter s = summary.getSelectionByDimension(dim.getDimension());
                return new DimensionExtension(source, dim, IncludeDescendants.apply(s));
            } else if (":all:".equals(stage.getStage())) {
                return new DimensionExtension(source, dim, allNodesIn(dim));
            } else {
                List<IDimensionNode> stageNodes = findNodes(dim.getDimension(), stage.getStage());
                List<IDimensionNode> totalNodes = Lists.newArrayList();
                if(dim.includeTotal()) {
                    extendParentLevel(stageNodes, totalNodes);
                }
                return new DimensionExtension(source, dim, stageNodes, totalNodes);
            }
        } else {
            return new DimensionExtension(source, dim, findNodes(dim.getDimension(), stage.getItems()));
        }
    }

    private void extendParentLevel(List<IDimensionNode> nodes, List<IDimensionNode> totalNodes) {
        if(!nodes.isEmpty()) {
        IDimensionNode parent = nodes.get(0).getParent();
        if(parent != null) {
            totalNodes.addAll(nodes.get(0).getParent().getLevel().getNodes());
        }
        }
    }

    private List<IDimensionNode> drilledNodesIn(final SummaryDimension dim) {
        List<IDimensionNode> nodes = Lists.newArrayList();
        for (IDimensionNode drilled : summary.getDrilledNodes(dim.getDimension())) {
            nodes.addAll(drilled.getChildren());
            if (dim.includeTotal()) {
                nodes.add(drilled);
            }
        }

        return nodes;
    }

    private SummaryItem createDrilledDimensionExtension(SummaryDimension dim) {
        List<IDimensionNode> childNodes = Lists.newArrayList();
        List<IDimensionNode> totalNodes = Lists.newArrayList();
        for (IDimensionNode drilled : summary.getDrilledNodes(dim.getDimension())) {
            childNodes.addAll(drilled.getChildren());
            if (dim.includeTotal()) {
                totalNodes.add(drilled);
            }
        }
        return new DimensionExtension(source, dim, childNodes, totalNodes);
    }

    private List<IDimensionNode> allNodesIn(final SummaryDimension dim) {
        List<IDimensionNode> nodes = new ArrayList<>();
        for (Dimension dimension : source.getDimensions()) {
            if (dimension.getId().equals(dim.getDimension())) {
                addDepthFirst(nodes, Lists.newArrayList(dimension.getRootNode()));
            }
        }
        return nodes;
    }

    private void addDepthFirst(List<IDimensionNode> nodes, Collection<IDimensionNode> candidates) {
        for (IDimensionNode c : candidates) {
            nodes.add(c);
            addDepthFirst(nodes, c.getChildren());
        }

    }

    private List<IDimensionNode> findNodes(String dimension, String stage) {
        for (Dimension dim : source.getDimensionsAndMeasures()) {
            if (dim.getId().equals(dimension)) {
                DimensionLevel level = dim.getRootLevel();
                while (level != null && !level.getId().equals(stage)) {
                    level = level.getChildLevel();
                }
                if (null != level) {
                    List<IDimensionNode> nodes = Lists.newArrayList(level.getNodes());
                   
                    return nodes;
                } else {
                    break;
                }
            }
        }
        return Collections.emptyList();
    }

    private List<IDimensionNode> findNodes(String dim, List<String> items) {
        List<IDimensionNode> nodes = new ArrayList<>();
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

    private void findNodesByName(Dimension dimension, List<String> items, List<IDimensionNode> nodes) {
        DimensionLevel level = dimension.getRootLevel();
        List<String> itemsNotFound = new ArrayList<>(items);
        while (level != null) {
            for (IDimensionNode node : level.getNodes()) {
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