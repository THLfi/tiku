package fi.thl.pivot.model;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * Provides implementation for i18n strings for the model
 * 
 * @author aleksiyrttiaho
 * 
 */
public class Label {

    private static final String DEFAULT = "default";
    private final Map<String, String> values = Maps.newHashMap();

    /**
     * Sets the value of the label in a given language.
     * 
     * @param language
     *            ISO-639-1 language code
     * @param value
     *            value
     */
    public void setValue(String language, String value) {
        if(null == language) {
            values.put(DEFAULT, value);
        } else {
            values.put(language, value);
        }
    }

    /**
     * Gets the value of the label in a given language
     * 
     * @param language
     *            ISO-639-1 language code
     * @return value
     */
    public String getValue(String language) {
        if (values.containsKey(language)) {
            return values.get(language);
        } else {
            if (values.containsKey(DEFAULT)) {
                return values.get(DEFAULT);
            }
            return "n/a";
        }
    }

    /**
     * Factory method for creating a label with a single preset value
     * 
     * @param language
     *            ISO-639-1 language code
     * @param value
     *            label value
     * @return new Label instance where getValue(language) = value
     */
    public static Label create(String language, String value) {
        Label label = new Label();
        label.setValue(language, value);
        return label;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
