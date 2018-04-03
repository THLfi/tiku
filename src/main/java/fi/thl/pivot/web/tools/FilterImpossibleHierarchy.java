package fi.thl.pivot.web.tools;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fi.thl.pivot.model.*;

public final class FilterImpossibleHierarchy implements Predicate<PivotCell> {

    private Multimap<String, IDimensionNode> nodesByDim;
    private List<PivotLevel> columns;
    private List<PivotLevel> rows;
    private Pivot pivot;

    public FilterImpossibleHierarchy(Pivot pivot) {
        nodesByDim = HashMultimap.create();
        this.columns = pivot.getColumns();
        this.rows = pivot.getRows();
        this.pivot = pivot;
    }

    /**
     * Function that returns true if and only if a given input has an impossible
     * hierarchy where the same dimension is used two or more times and nodes in
     * the hierarchy are of incompatible branches non-empty
     *
     */
    @Override
    public boolean apply(PivotCell input) {
        return checkRowNodes(input) || checkColumnNodes(input);
    }

    private boolean checkColumnNodes(PivotCell input) {
        nodesByDim.clear();
        for (int i = 0; i < columns.size(); ++i) {
            IDimensionNode dn = pivot.getColumnAt(i, input.getColumnNumber());
            nodesByDim.put(dn.getDimension().getId(), dn);
        }
        return checkNodesByDimension(nodesByDim);
    }

    private boolean checkRowNodes(PivotCell input) {
        nodesByDim.clear();
        for (int i = 0; i < rows.size(); ++i) {
            IDimensionNode dn = pivot.getRowAt(i, input.getRowNumber());
            nodesByDim.put(dn.getDimension().getId(), dn);
        }
        return checkNodesByDimension(nodesByDim);
    }

    private boolean checkNodesByDimension(Multimap<String, IDimensionNode> nodesByDim) {
        for (Map.Entry<String, Collection<IDimensionNode>> e : nodesByDim.asMap().entrySet()) {
            if (e.getValue().size() > 1 && checkAncestryPairWise(e.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAncestryPairWise(Collection<IDimensionNode> e) {
        for (IDimensionNode a : e) {
            for (IDimensionNode b : e) {
                if (!(a.ancestorOf(b) || b.ancestorOf(a))) {
                    return true;
                }
            }
        }
        return false;
    }
}