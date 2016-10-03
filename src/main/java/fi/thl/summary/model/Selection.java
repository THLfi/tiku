package fi.thl.summary.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Selection {

    private String id;
    private String dimension;
    private List<String> items = new ArrayList<>();
    private List<String> stages = new ArrayList<>();
    private List<String> sets = new ArrayList<>();
    private Set<String> defaultItems = new TreeSet<>();

    private boolean completeDimension = true;
    private boolean visible = true;
    private boolean multiple;
    private boolean searchable;

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public void addItem(String item) {
        completeDimension = false;
        items.add(item);
    }

    public void addStage(String stage) {
        completeDimension = false;
        stages.add(stage);
    }

    public void addDefaultItem(String item) {
        if (null != item) {
            this.defaultItems.add(item.toLowerCase());
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDimension() {
        return dimension;
    }

    public List<String> getItems() {
        return items;
    }

    public List<String> getStages() {
        return stages;
    }

    public Collection<String> getDefaultItem() {
        return defaultItems;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void addSet(String set) {
        completeDimension = false;
        this.sets.add(set);
    }

    public List<String> getSets() {
        return sets;
    }

    @Override
    public String toString() {
        return "Selection [id=" + id + ", dimension=" + dimension + "]";
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean getMultiple() {
        return multiple;
    }

    public boolean getSearchable() {
        return searchable;
    }

    public boolean getIsCompleteDimension() {
        return completeDimension;
    }
}
