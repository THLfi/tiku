package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Selection;

public class HydraLabel extends Label {

    private List<Selection> selections;
    private Label delegate;

    public HydraLabel(Label delegate, List<Selection> selections) {
        this.delegate = delegate;
        this.selections = selections;
    }

    @Override
    public String getValue(String language) {
        String content = delegate.getValue(language);
        for (Selection s : selections) {
            String placeholder = String.format("\\$\\$%s.value\\$\\$", s.getId());
            List<String> labels = new ArrayList<>();
            for (DimensionNode node : ((HydraFilter) s).getSelected()) {
                labels.add(node.getLabel().getValue(language));
            }
            content = content.replaceAll(placeholder, Joiner.on(", ").join(labels));
        }
        return content;
    }
}
