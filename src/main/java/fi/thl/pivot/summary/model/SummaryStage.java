package fi.thl.pivot.summary.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SummaryStage {

    public static enum Type {
        STAGE, ITEMS
    }

    private Type type;
    private List<String> items;
    private String dimensionId;

    public SummaryStage(Type type, String... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Cannot create summary presentation with no items");
        }
        this.type = type;
        this.items = new ArrayList<>();
        for (String i : items) {
            this.items.add(i);
        }
    }

    public SummaryStage(Type type, List<String> items, String dimensionId) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create summary presentation with no items");
        }
        this.type = type;
        this.items = new ArrayList<>(items);
        this.dimensionId = dimensionId;
    }

    public Type getType() {
        return type;
    }

    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getStage() {
        return items.get(0);
    }

    public boolean isLogical() {
        return Type.STAGE.equals(type) && getStage().startsWith(":");
    }

    public String getDimensionId() {
        return this.dimensionId;
    }

    @Override
    public String toString() {
        return "SummaryStage [type=" + type + ", items=" + items + ", dimensionId=" + dimensionId + "]";
    }
    

}
