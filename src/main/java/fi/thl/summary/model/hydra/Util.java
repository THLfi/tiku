package fi.thl.summary.model.hydra;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;

final class Util {

    static final List<Selection> extendFilters(final HydraSource source, final Summary summary, List<Selection> filters) {
        return Lists.transform(filters, new Function<Selection, Selection>() {

            @Override
            public Selection apply(final Selection s) {
                return new HydraFilter(source, summary, s);
            }
        });
    }
}
