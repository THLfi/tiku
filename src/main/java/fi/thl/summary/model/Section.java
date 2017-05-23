package fi.thl.summary.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Provides abstraction over positioning features of summary descriptions.
 * Description constist of grid element that may contain rows and blocks/sections.
 * This allows the summary to contain multiple columns with multiple presentations
 * within each block
 * 
 * @author aleksiyrttiaho
 *
 */
public class Section {

    private List<Section> children = new ArrayList<>();
    private List<Presentation> presentations = new ArrayList<>();
    private String width;

    public void addChild(Section section) {
        children.add(section);
    }

    public void addPresentation(Presentation presentation) {
        presentations.add(presentation);
    }

    public List<Section> getChildren() {
        return children;
    }

    public List<Presentation> getPresentations() {
        return presentations;
    }

    public void setWidth(String attribute) {
        if (attribute != null && attribute.matches("^((1[012])|([0-9))$")) {
            this.width = attribute;
        }
    }

    public String getWidth() {
        return width;
    }
}
