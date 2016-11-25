package fi.thl.pivot.model;

import java.util.Set;
import java.util.TreeSet;

public class NamedView implements Comparable<NamedView> {

    private int id;
    private String url;
    private Label label = new Label();
    private boolean isDefault = false;
    private Set<String> defaultForPassword = new TreeSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public int compareTo(NamedView o) {
        if (this.id < o.id) {
            return -1;
        } else if (this.id > o.id) {
            return 1;
        }
        return 0;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isDefaultForPassword(String password) {
        return defaultForPassword.contains(password);
    }

    public void setDefaultForPassword(String defaultForPassword) {
        this.defaultForPassword.add(defaultForPassword);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
