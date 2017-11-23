package fi.thl.summary.model.hydra;

import java.util.List;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Rule;
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

        StringBuilder part = new StringBuilder();
        StringBuilder whole = new StringBuilder();

        char prev = ' ';
        boolean inTextExpression = false;
        boolean inValueExpression = false;
        for(char c : content.toCharArray()) {
            switch(c) {
                case '%':
                    if(prev == '%') {
                        if(inTextExpression) {
                            evaluateExpression(part, whole, "");
                        }  else {
                            whole.append(part);
                            part.setLength(0);
                        }
                        inTextExpression = !inTextExpression;
                        prev = ' ';
                    } else {
                        prev = c;
                    }
                    break;
                case '$':
                    if(prev == '$') {
                        if(inValueExpression) {
                            evaluateExpression(part, whole, "$$");
                        } else {
                            whole.append(part);
                            part.setLength(0);
                        }
                        inValueExpression = !inValueExpression;
                        prev = ' ';
                    } else {
                        prev = c;
                    }
                    break;
                default:
                    if(prev == '%') {
                        part.append(prev);
                    }
                    else if(prev == '$') {
                        part.append(prev);
                    }
                    part.append(c);
                    prev = c;
            }
        }
        whole.append(part.toString());
        return solve(whole.toString(), language);
    }

    private void evaluateExpression(StringBuilder part, StringBuilder whole, String delimiter) {
        String[] p = part.toString().split("\\?\\?");

        if(p.length == 2) {
            Rule r = new Rule();
            r.setExpression(p[1]);
            HydraRule hr = new HydraRule(r, summary);
            if(hr.eval()) {
                whole.append(delimiter + p[0] + delimiter);
            }
        } else {
            whole.append(delimiter + p[0] + delimiter);
        }
        part.setLength(0);
    }

    private String solve(String content, String language) {
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
