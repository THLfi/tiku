package fi.thl.pivot.web.tools;

import com.google.common.base.Predicate;

import fi.thl.pivot.model.PivotCell;

public final class FilterZeroOrEmpty implements Predicate<PivotCell> {

    /**
     * Function that returns true if and only if a given input string represents
     * a number that is equal to zero.
     *
     */
    @Override
    public boolean apply(PivotCell input) {
        if (null == input) {
            return true;
        }
        if(null == input.getValue()) {
            return true;
        }
        if (input.getValue().isEmpty()) {
            return true;
        }
        if ("..".equals(input.getValue())) {
            return true;
        }
        return input.getValue().matches("^0*([,\\.]0*)?$");

    }
}