package fi.thl.pivot.summary.model.hydra;

import com.google.common.base.Function;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.IDimensionNode;

final class NodeToId implements Function<IDimensionNode, Integer> {
    @Override
    public Integer apply(IDimensionNode dn) {
        if (dn != null) {
            return dn.getSurrogateId();
        } else {
            return 0;
        }
    }
}