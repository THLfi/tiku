package fi.thl.pivot.summary.model;

/**
 * Represent a presentation in Amor summary specification. A presentation is a
 * element shown to the user that contains either static or dynamic content
 *
 * @author aleksiyrttiaho
 *
 */
public interface Presentation {

    public static enum SortMode {
        none, desc, asc
    }
    
    public static enum SuppressMode {
        none, empty, zero, all;

        public static SuppressMode fromString(String attribute) {
            if(null == attribute) {
                return none;
            }
            if("no".equalsIgnoreCase(attribute)) {
                return none;
            }
            if("yes".equalsIgnoreCase(attribute)) {
                return all;
            }
            if("empty".equalsIgnoreCase(attribute)) {
                return empty;
            }
            if("zero".equalsIgnoreCase(attribute)) {
                return zero;
            }
            return none;
        }
    }
    public static enum HighlightMode {
        none, zeros, negatives, zeros_and_negatives;

        public static HighlightMode fromString(String attribute) {
            if(null == attribute) {
                return none;
            }
            if("zeros".equalsIgnoreCase(attribute)) {
                return zeros;
            }
            if("negatives".equalsIgnoreCase(attribute)) {
                return negatives;
            }
            if("zeros_and_negatives".equalsIgnoreCase(attribute)) {
                return zeros_and_negatives;
            }           
            return none;
        }
    }

    public static enum Legendless {
        no, yes, nonEmphasized;

        public static Legendless fromString(String attribute) {
            if(null == attribute) {
                return no;
            }
            if("yes".equalsIgnoreCase(attribute)) {
                return yes;
            }
            if("non-emphasized".equalsIgnoreCase(attribute)) {
                return nonEmphasized;
            }
            return no;
        }
    }

    /**
     * 
     * @return the type of the presentation as defined in the summary
     *         configuration
     */
    String getType();

    boolean isFirst();

    boolean isLast();

    int getGroupSize();

    void setGroupSize(int size);

    SortMode getSortMode();

    String getId();

    Rule getRule();

}
