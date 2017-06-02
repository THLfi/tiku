package fi.thl.summary.model.hydra;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Selection;

public class HydraLabel extends Label {

    private static final Pattern tags = Pattern.compile("\\$\\$([^\\.]+)\\.([^\\$]+)\\$\\$");
    
    
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("\\$\\$([^\\.]+)\\.([^\\$]+)\\$\\$");
        Matcher m  = pattern.matcher("$$lorem.value$$ a adfavasdf asdfa $$ipsum.meta:description$$");
        while(m.find()) {
            System.out.println(m.group(1) + " => " + m.group(2));
        }
    }
    
    private Label delegate;
    private HydraSummary summary;

    public HydraLabel(Label delegate, HydraSummary summary) {
        this.delegate = delegate;
        this.summary = summary;
    }

    @Override
    public String getValue(String language) {
        String content = delegate.getValue(language);
        
        Matcher m = tags.matcher(content);
        while(m.find()) {
            String id = m.group(1);
            String property = m.group(2);
            String regex = m.group(0).replace("$", "\\$");
            
            Selection select = summary.getSelection(id);
            if(null != select) {
                content = replaceFilterReference(language, content, regex, property, select);
            } else {
                String v = summary.getValueOf(id);
                if(v == null) {
                    content = content.replaceAll(regex, "..");
                } else {
                    content = content.replaceAll(regex, v);
                }
            }
        }
        
        return content;
    }

    private String replaceFilterReference(String language, String content, String regex, String property,
            Selection select) {
        StringBuilder label = new StringBuilder();
        boolean first = true;
        for(DimensionNode node : ((HydraFilter) select).getSelected()) {
            if(!first) {
                label.append(", ");
            }
            if("value".equals(property)) {
                label.append(node.getLabel().getValue(language));
            } else if ("code".equals(property)) {
                String code = node.getCode();
                label.append(code == null ? ".." : code);
            } else {
                Label p = node.getProperty(property);
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
}
