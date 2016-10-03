package fi.thl.pivot.web.tools;

import com.google.common.base.Predicate;

import fi.thl.pivot.model.PivotCell;

public final class FilterZero implements Predicate<PivotCell> {

    /**
     * Function that returns true if and only if a given input string represents
     * a number that is equal to zero.
     *
     */
    @Override
    public boolean apply(PivotCell input) {
        if (null == input) {
            return false;
        }
        if(null == input.getValue()) {
            return false;
        }
        if (input.getValue().isEmpty()) {
            return false;
        }
        if ("..".equals(input.getValue())) {
            return false;
        }
        return input.getValue().matches("^0*(,0*)?$");

    }
}