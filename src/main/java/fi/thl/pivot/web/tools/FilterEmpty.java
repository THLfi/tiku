package fi.thl.pivot.web.tools;

import com.google.common.base.Predicate;

import fi.thl.pivot.model.PivotCell;

public final class FilterEmpty implements Predicate<PivotCell> {

    /**
     * Function that returns true if and only if a given input string is
     * non-empty
     *
     */
    @Override
    public boolean apply(PivotCell input) {
        boolean filtered = null == input ||
                null == input.getValue() ||
                input.getValue().isEmpty() ||
                "..".equals(input.getValue());
        return filtered;

    }
}