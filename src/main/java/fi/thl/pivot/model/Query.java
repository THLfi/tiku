package fi.thl.pivot.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Query {

    private Multimap<String, IDimensionNode> dimension = ArrayListMultimap.create();
    private Set<IDimensionNode> levelNodes = Sets.newHashSet();
    private List<List<IDimensionNode>> rows = Lists.newArrayList();
    private List<List<IDimensionNode>> columns = Lists.newArrayList();
    private Set<Integer> subsetRows = Sets.newHashSet();
    private Set<Integer> subsetColumns = Sets.newHashSet();

    public void addRowNode(List<List<IDimensionNode>> levels, List<Boolean> subset) {
        int i = 0;
        for (List<IDimensionNode> level : levels) {
            rows.add(level);
            if (subset.get(i)) {
                useRowAsSubset(i);
            }
            for (IDimensionNode node : level) {
                dimension.put(node.getDimension().getId(), node);
                if (!subset.get(i)) {
                    dimension.putAll(node.getDimension().getId(), node.getChildren());
                }
            }
            ++i;
        }
    }

    public void addColumnNode(List<List<IDimensionNode>> levels, List<Boolean> subset) {
        int i = 0;
        for (List<IDimensionNode> level : levels) {
            columns.add(level);
            if (subset.get(i)) {
                useColumnAsSubset(i);
            }
            for (IDimensionNode node : level) {
                dimension.put(node.getDimension().getId(), node);
                if (!subset.get(i)) {
                    dimension.putAll(node.getDimension().getId(), node.getChildren());
                }
            }
            ++i;
        }

    }

    public Multimap<String, IDimensionNode> getNodesPerDimension() {
        return dimension;
    }

    public List<PivotLevel> getRows() {
        return getLevels(rows, subsetRows);
    }

    public List<PivotLevel> getColumns() {
        return getLevels(columns, subsetColumns);
    }

    private List<PivotLevel> getLevels(List<List<IDimensionNode>> nodeLevels, Set<Integer> subsets) {
        List<PivotLevel> levels = Lists.newArrayList();
        int i = 0;
        for (List<IDimensionNode> nodes : nodeLevels) {
            PivotLevel level = new PivotLevel();
            levels.add(level);
            if (nodes.size() > 1) {
                level.add(nodes);
            } else if (subsets.contains(i)) {
                level.add(nodes);
                level.setAsSubsetLevel();
            } else {
                handleSingleNodeInLevel(level, nodes, i);
            }
            ++i;
        }
        return levels;
    }

    private void handleSingleNodeInLevel(PivotLevel level, List<IDimensionNode> nodes, int index) {
        for (IDimensionNode dn : nodes) {
            if (dn.canAccess()) {
                if (levelNodes.contains(dn)) {
                    addAllNodesInLevel(dn, level);
                } else {
                    addDirectDescendants(dn, level, index);
                }
            }
        }
    }

    private void addDirectDescendants(IDimensionNode dn, PivotLevel level, int index) {
        level.add(dn.getChildren().stream().filter(x -> !x.isHidden() || !x.isMeasure()).collect(Collectors.toList()));
        level.add(new InputtedDimensionNode(dn, index, true));
    }

    private void addAllNodesInLevel(IDimensionNode dn, PivotLevel level) {
        for (IDimensionNode d : dn.getLevel().getNodes()) {
            level.add(d.getChildren().stream().filter(x -> !x.isHidden() || !x.isMeasure()).collect(Collectors.toList()));
        }
        level.add(dn.getLevel().getNodes().stream().filter(x -> !x.isHidden() || !x.isMeasure()).collect(Collectors.toList()));
    }

    public boolean isRowSubset(int i) {
        return subsetRows.contains(i);
    }

    public boolean isColumnSubset(int i) {
        return subsetColumns.contains(i);
    }
    
    public boolean isRowLevel(int i) {
        return rows.get(i).size() == 1 && levelNodes.contains(rows.get(i).get(0));
    }

    public boolean isColumnLevel(int i) {
        return columns.get(i).size() == 1 && levelNodes.contains(columns.get(i).get(0));
    }


    public void useRowAsSubset(int i) {
        subsetRows.add(i);
    }

    public void useColumnAsSubset(int i) {
        subsetColumns.add(i);
    }

}
