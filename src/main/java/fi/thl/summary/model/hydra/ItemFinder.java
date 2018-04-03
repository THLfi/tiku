package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.IDimensionNode;
import fi.thl.summary.model.Summary;

public class ItemFinder {

    private Summary summary;

    public ItemFinder(Summary summary) {
        this.summary = summary;
    }
    
    public List<IDimensionNode> findItems(List<String> items, HydraSource source) {
        List<IDimensionNode> result = new ArrayList<>();
        if (Summary.Scheme.Reference.equals(summary.getScheme())) {
            for (String item : items) {
                result.add(source.findNodeByRef(item));
            }
        } else {
            for (String item : items) {
                result.add(source.findNodeByName(item, summary.getItemLanguage()));
            }
        }
        return result;
    }

    public List<IDimensionNode> findItems(List<String> items, Dimension dim) {
        List<IDimensionNode> result = new ArrayList<>();
        if (Summary.Scheme.Reference.equals(summary.getScheme())) {
            for (String item : items) {
                result.add(dim.getNode(item));
            }
        } else {
            findItemsByName(items, dim, result);
        }
        return result;
    }

    private void findItemsByName(List<String> items, Dimension dim, List<IDimensionNode> result) {
        DimensionLevel level = dim.getRootLevel();
        List<String> notFound = Lists.newArrayList(items);
        while (level != null && !notFound.isEmpty()) {
            for (IDimensionNode node : level.getNodes()) {
                for (Iterator<String> it = notFound.iterator(); it.hasNext();) {
                    String s = it.next();
                    if (s.equals(node.getLabel().getValue(summary.getItemLanguage()))) {
                        result.add(node);
                        it.remove();
                    }
                }
            }
            level = level.getChildLevel();
        }
    }
}
