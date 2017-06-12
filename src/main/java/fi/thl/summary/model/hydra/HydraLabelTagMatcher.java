package fi.thl.summary.model.hydra;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HydraLabelTagMatcher {

    private static final Pattern tags = Pattern.compile("\\$\\$(([^\\.\\_]+)(_([^\\.]+))?)\\.([^\\$]+)\\$\\$");
    private Matcher matcher;

    HydraLabelTagMatcher(String content) {
        matcher = tags.matcher(content);
    }

    boolean find() {
        return matcher.find();
    }

    String getTag() {
        return matcher.group(0);
    }
    
    String getIdentifier() {
        return matcher.group(2);
    }
    
    String getFullIdentifier() {
        return matcher.group(1);
    }

    String getStage() {
        return matcher.group(4);
    }

    String getProperty() {
        return matcher.group(5);
    }
}
