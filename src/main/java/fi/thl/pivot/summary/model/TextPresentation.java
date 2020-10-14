package fi.thl.pivot.summary.model;

import fi.thl.pivot.model.Label;

public class TextPresentation implements Presentation {

    private String id;
    private Label content;
    private String type;

    private int groupSize;
    private boolean isFirst, isLast;
    private Rule rule;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public Label getContent() {
        return content;
    }

    public void setContent(Label content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    @Override
    public SortMode getSortMode() {
        return SortMode.none;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public Rule getRule() {
        return rule;
    }
}
