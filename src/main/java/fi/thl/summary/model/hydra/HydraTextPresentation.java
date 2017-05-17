package fi.thl.summary.model.hydra;

import java.util.List;

import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.TextPresentation;

public class HydraTextPresentation extends TextPresentation {

    private HydraSummary summary;
    private TextPresentation p;

    public HydraTextPresentation(TextPresentation p, HydraSummary summary) {
        this.p = p;
        this.summary = summary;
    }

    public void setId(String id) {
        p.setId(id);
    }

    public String getId() {
        return p.getId();
    }

    public Label getContent() {
        return new HydraLabel(p.getContent(), summary);
    }

    public String getContent(String language) {
        return getContent().getValue(language);
    }

    public void setContent(Label content) {
        p.setContent(content);
    }

    public String getType() {
        return p.getType();
    }

    public void setType(String type) {
        p.setType(type);
    }

    public int getGroupSize() {
        return p.getGroupSize();
    }

    public void setGroupSize(int groupSize) {
        p.setGroupSize(groupSize);
    }

    public boolean isFirst() {
        return p.isFirst();
    }

    public void setFirst(boolean isFirst) {
        p.setFirst(isFirst);
    }

    public boolean isLast() {
        return p.isLast();
    }

    public void setLast(boolean isLast) {
        p.setLast(isLast);
    }

    public SortMode getSortMode() {
        return p.getSortMode();
    }

}
