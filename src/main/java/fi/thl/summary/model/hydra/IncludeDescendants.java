package fi.thl.summary.model.hydra;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.Selection;

public class IncludeDescendants {

    public static List<DimensionNode> apply(Selection s) {
        Set<DimensionNode> set = Sets.newLinkedHashSet();
        List<DimensionNode> selected = ((HydraFilter) s).getSelected();
        switch (s.getSelectionMode()) {
        case directDescendants:
            for (DimensionNode node : selected) {
                set.add(node);
                set.addAll(node.getChildren());
            }
            return Lists.newArrayList(set);
        case allDescendants:
            addRecursive(selected, set);
            return Lists.newArrayList(set);
        default:
            return selected;
        }

    }

    private static void addRecursive(Collection<DimensionNode> selected, Collection<DimensionNode> nodes) {
        for (DimensionNode node : selected) {
            nodes.add(node);
            addRecursive(node.getChildren(), nodes);
        }
    }

}
