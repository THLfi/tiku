package fi.thl.pivot.model;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Query {

    private Multimap<String, DimensionNode> dimension = ArrayListMultimap.create();
    private Set<DimensionNode> levelNodes = Sets.newHashSet();
    private List<List<DimensionNode>> rows = Lists.newArrayList();
    private List<List<DimensionNode>> columns = Lists.newArrayList();
    private Set<Integer> subsetRows = Sets.newHashSet();
    private Set<Integer> subsetColumns = Sets.newHashSet();

    public void addRowNode(List<List<DimensionNode>> levels, List<Boolean> subset) {
        int i = 0;
        for (List<DimensionNode> level : levels) {
            rows.add(level);
            if (subset.get(i)) {
                useRowAsSubset(i);
            }
            for (DimensionNode node : level) {
                dimension.put(node.getDimension().getId(), node);
                if (!subset.get(i)) {
                    dimension.putAll(node.getDimension().getId(), node.getChildren());
                }
            }
            ++i;
        }
    }

    public void addColumnNode(List<List<DimensionNode>> levels, List<Boolean> subset) {
        int i = 0;
        for (List<DimensionNode> level : levels) {
            columns.add(level);
            if (subset.get(i)) {
                useColumnAsSubset(i);
            }
            for (DimensionNode node : level) {
                dimension.put(node.getDimension().getId(), node);
                if (!subset.get(i)) {
                    dimension.putAll(node.getDimension().getId(), node.getChildren());
                }
            }
            ++i;
        }

    }

    public Multimap<String, DimensionNode> getNodesPerDimension() {
        return dimension;
    }

    public List<PivotLevel> getRows() {
        return getLevels(rows, subsetRows);
    }

    public List<PivotLevel> getColumns() {
        return getLevels(columns, subsetColumns);
    }

    private List<PivotLevel> getLevels(List<List<DimensionNode>> nodeLevels, Set<Integer> subsets) {
        List<PivotLevel> levels = Lists.newArrayList();
        int i = 0;
        for (List<DimensionNode> nodes : nodeLevels) {
            PivotLevel level = new PivotLevel();
            levels.add(level);
            if (nodes.size() > 1) {
                level.add(nodes);
            } else if (subsets.contains(i)) {
                level.add(nodes);
                level.setAsSubsetLevel();
            } else {
                handleSingleNodeInLevel(level, nodes);
            }
            ++i;
        }
        return levels;
    }

    private void handleSingleNodeInLevel(PivotLevel level, List<DimensionNode> nodes) {
        for (DimensionNode dn : nodes) {
            if (dn.canAccess()) {
                if (levelNodes.contains(dn)) {
                    addAllNodesInLevel(dn, level);
                } else {
                    addDirectDescendants(dn, level);
                }
            }
        }
    }

    private void addDirectDescendants(DimensionNode dn, PivotLevel level) {
        level.add(dn.getChildren());
        level.add(dn);
    }

    private void addAllNodesInLevel(DimensionNode dn, PivotLevel level) {
        for (DimensionNode d : dn.getLevel().getNodes()) {
            level.add(d.getChildren());
        }
        level.add(dn.getLevel().getNodes());
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
