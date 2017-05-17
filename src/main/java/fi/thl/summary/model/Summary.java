package fi.thl.summary.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fi.thl.pivot.model.Label;

/**
 * 
 * Provides a object representation of a summary specification document as
 * defined in the Hydra 4.x specification
 * 
 * @author aleksiyrttiaho
 *
 */
public class Summary {

    public static enum Scheme {
        Reference, Name
    }

    private String id;
    private Scheme scheme = Scheme.Name;
    private String specificationVersion;
    private boolean isDrillEnabled;
    private List<String> supportedLanguages = Lists.newArrayList();
    private List<Presentation> presentations = Lists.newArrayList();
    private Map<String, Selection> selections = Maps.newLinkedHashMap();

    private Label subject;
    private Label title;
    private Label link;
    private Label note;
    private String factTable;
    private String itemLanguage;
    private String source;
    private List<Section> sections;
    
    private Map<String, Value> values = Maps.newHashMap();

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpecificationVersion() {
        return specificationVersion;
    }

    public void setSpecificationVersion(String specificationVersion) {
        this.specificationVersion = specificationVersion;
    }

    public boolean isDrillEnabled() {
        return isDrillEnabled;
    }

    public void setDrillEnabled(boolean isDrillEnabled) {
        this.isDrillEnabled = isDrillEnabled;
    }

    public Collection<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void supportLanguage(String language) {
        if (!supportedLanguages.contains(language)) {
            supportedLanguages.add(language);
        }
    }

    public Label getSubject() {
        return subject;
    }

    public void setSubject(Label subject) {
        this.subject = subject;
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        this.title = title;
    }

    public Label getLink() {
        return link;
    }

    public void setLink(Label link) {
        this.link = link;
    }

    public Label getNote() {
        return note;
    }

    public void setNote(Label note) {
        this.note = note;
    }

    public void addPresentation(Presentation newPresentation) {
        presentations.add(newPresentation);
    }

    public List<Presentation> getPresentations() {
        return presentations;
    }

    public void addSelection(Selection selection) {
        selections.put(selection.getId(), selection);
    }

    public Collection<Selection> getSelections() {
        return selections.values();
    }

    public Selection getSelection(String id) {
        return selections.get(id);
    }

    public String getFactTable() {
        return this.factTable;
    }

    public void setFactTable(String factTable) {
        this.factTable = factTable;
    }

    public String getItemLanguage() {
        return itemLanguage;
    }

    public void setItemLanguage(String itemLanguage) {
        this.itemLanguage = itemLanguage;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String documentAsString) {
        this.source = documentAsString;
    }

    public void setSections(List<Section> children) {
        this.sections = children;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void addValue(Value value) {
        this.values.put(value.getId(), value);
    }
    
    public Value getValue(String id) {
        return this.values.get(id);
    }

}
