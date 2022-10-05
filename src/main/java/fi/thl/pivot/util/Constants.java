package fi.thl.pivot.util;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public final class Constants {

    private Constants() {
    }

    public static final String DIMENSION_SEPARATOR = "-";
    public static final String SUBSET_SEPARATOR = ".";
    public static final String DEPRECATED_DIMENSION_SEPARATOR = "/";
    public static final String DEPRECATED_SUBSET_SEPARATOR = ";";
    public static final String LEVEL_IDENTIFIER = "L";
    public static final String MEASURE = "measure";
    public static final List<String> VALID_ENVIRONMENTS = Collections.unmodifiableList(Lists.newArrayList("deve", "test", "prod", "beta"));

    public static final String CONFIDENCE_INTERVAL_UPPER_LIMIT  = "ci_upper"; 
    public static final String CONFIDENCE_INTERVAL_LOWER_LIMIT  = "ci_lower";
    public static final String SAMPLE_SIZE  = "n";

    public static final String DEFAULT_FIRST_ITEM = ":first:";
    public static final String DEFAULT_FIRST_ITEMS_START = ":first";
    public static final String DEFAULT_LAST_ITEM = ":last:";
    public static final String DEFAULT_LAST_ITEMS_START = ":last";
}
