package fi.thl.summary.model.hydra;

import com.google.common.base.Function;

import fi.thl.pivot.model.DimensionNode;

final class NodeToId implements Function<DimensionNode, Integer> {
    @Override
    public Integer apply(DimensionNode dn) {
        if (dn != null) {
            return dn.getSurrogateId();
        } else {
            return 0;
        }
    }
}