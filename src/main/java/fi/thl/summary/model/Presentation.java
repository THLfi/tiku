package fi.thl.summary.model;

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

}
