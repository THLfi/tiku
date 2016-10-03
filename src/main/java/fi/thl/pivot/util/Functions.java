package fi.thl.pivot.util;

import java.util.Set;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

public class Functions {

    private Functions() {
    };

    /**
     * @param maxValue
     *            maximum value of range
     * @return a contiguous modifiable set of integer between 0 and maxvalue
     */
    public static Set<Integer> upto(int maxValue) {
        // The range must be inserted to a new collection
        // as contiguous set is immutable and thus prevents
        // filtering in place. We could also construct new
        // sets in applyFilterForEach-method but that would
        // require us to use output parameters
        return Sets.newLinkedHashSet(ContiguousSet.create(Range.open(-1, maxValue), DiscreteDomain.integers()));
    }

}
