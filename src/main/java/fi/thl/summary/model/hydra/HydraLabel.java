package fi.thl.summary.model.hydra;

import java.util.List;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Selection;

public class HydraLabel extends Label {
    
    private Label delegate;
    private HydraSummary summary;

    public HydraLabel(Label delegate, HydraSummary summary) {
        this.delegate = delegate;
        this.summary = summary;
    }

    @Override
    public String getValue(String language) {
        String content = delegate.getValue(language);
        HydraLabelTagMatcher m = new HydraLabelTagMatcher(content);
        while(m.find()) {
         
            Selection select = summary.getSelection(m.getIdentifier());
            if(null != select) {
                content = replaceFilterReference(language, content, m, select);
            } else {
                String v = summary.getValueOf(m.getIdentifier());
                String regex = m.getTag().replace("$", "\\$");
                if(v == null) {
                    content = content.replaceAll(regex, "..");
                } else {
                    content = content.replaceAll(regex, v);
                }
            }
        }
        return content;
    }

    private String replaceFilterReference(String language, String content, HydraLabelTagMatcher m,
            Selection select) {
        StringBuilder label = new StringBuilder();
        boolean first = true;
        String regex = m.getTag().replace("$", "\\$");
        for(DimensionNode node : selectNodes(m, select)) {
            if(!first) {
                label.append(", ");
            }
            if("value".equals(m.getProperty())) {
                label.append(node.getLabel().getValue(language));
            } else if ("code".equals(m.getProperty())) {
                String code = node.getCode();
                label.append(code == null ? ".." : code);
            } else {
                Label p = node.getProperty(m.getProperty());
                if(null == p) {
                    label.append("");
                } else {
                    label.append(p.getValue(language));
                }
            }
            first = false;
        }
        content = content.replaceAll(regex, label.toString());
        return content;
    }

    private List<DimensionNode> selectNodes(HydraLabelTagMatcher m, Selection select) {
        List<DimensionNode> selected = null;
        if(null == m.getStage()) {
            selected = ((HydraFilter) select).getSelected();
        } else {
            selected = ((HydraFilter) select).getSelected(m.getStage());
        }
        return selected;
    }
}
