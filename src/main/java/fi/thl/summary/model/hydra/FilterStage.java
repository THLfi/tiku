package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Summary.Scheme;

public class FilterStage {

    private Label label;
    private List<DimensionNode> options;
    private List<DimensionNode> selected = new ArrayList<>();
    private FilterStage parent;
    private Collection<String> defaultItem;
    private String itemLanguage;
    private Scheme scheme;
    private String id;

    public FilterStage(Label label, List<DimensionNode> options) {
        this.label = label;
        this.options = options;
    }

    public void setParent(FilterStage parent) {
        this.parent = parent;
    }

    /**
     * Returns all options in the stage. If a parent stage exists all options
     * that are not descdents of the selected parent option are filtered out
     */
    public Collection<DimensionNode> getOptions() {
        if (null == parent) {
            return this.options;
        }
        final List<DimensionNode> parentOption = parent.getSelected();
        return Collections2.filter(this.options, new Predicate<DimensionNode>() {

            @Override
            public boolean apply(DimensionNode option) {
                for (DimensionNode p : parentOption) {
                    if (option.descendentOf(p)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public Label getLabel() {
        return this.label;
    }

    public List<DimensionNode> getSelected() {
        if (null == selected) {
            select(null);
        }
        if (null != parent) {
            for (Iterator<DimensionNode> it = selected.iterator(); it.hasNext();) {
                boolean isDescendent = false;
                DimensionNode s = it.next();
                for (DimensionNode p : parent.getSelected()) {

                    if (p.ancestorOf(s)) {
                        isDescendent = true;
                        break;
                    }
                }
                if (!isDescendent) {
                    it.remove();
                }
            }
            if (selected.isEmpty()) {
                return parent.getSelected();
            }
        }
        return selected;
    }

    public void select(String candidateId) {
        if (null != candidateId) {
            for (DimensionNode option : getOptions()) {
                if (String.valueOf(option.getSurrogateId()).equals(candidateId)) {
                    selected.add(option);
                    return;
                }
            }
        }

        if (null != defaultItem && !defaultItem.isEmpty()) {
            
            if (defaultItem.contains(":last:")) {
                List<DimensionNode> opt = Lists.newArrayList(getOptions());
                selected.add(opt.get(opt.size() - 1));
                return;
            }

            if (defaultItem.contains(":first:")) {
                List<DimensionNode> opt = Lists.newArrayList(getOptions());
                selected.add(opt.get(0));
                return;
            }

            for (DimensionNode option : options) {
                if (option == null) {

                } else if (Scheme.Reference.equals(scheme)) {
                    if (defaultItem.contains(option.getReference().toLowerCase())) {
                        selected.add(option);
                        if(selected.size() == defaultItem.size()) {
                            return;
                        }
                    }
                } else {
                    if (defaultItem.contains(option.getLabel().getValue(itemLanguage).toLowerCase())) {
                        selected.add(option);
                        if(selected.size() == defaultItem.size()) {
                            return;
                        }
                    }
                }
            }
        }

        if(parent == null) {
            List<DimensionNode> opt = new ArrayList<>(getOptions());
            selected.add(opt.get(0));
        }

    }

    public Collection<String> getDefaultItem() {
        return defaultItem;
    }

    public void setDefaultItem(Collection<String> defaultItem) {
        this.defaultItem = defaultItem;
    }

    public String getItemLanguage() {
        return itemLanguage;
    }

    public void setItemLanguage(String itemLanguage) {
        this.itemLanguage = itemLanguage;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
