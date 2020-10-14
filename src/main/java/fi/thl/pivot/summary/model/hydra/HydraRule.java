package fi.thl.pivot.summary.model.hydra;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import fi.thl.pivot.summary.model.Rule;
import fi.thl.pivot.summary.model.Selection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HydraRule {


    private final HydraSummary summary;
    private final Rule rule;

    public HydraRule(Rule rule, HydraSummary summary) {
        this.rule = rule;
        this.summary = summary;
    }

    public boolean eval() {
        if(null == rule) {
            return true;
        }
        String expression = rule.getExpression();
        if(null == expression) {
            return true;
        }

        expression = expression.trim();
        if(expression.isEmpty()) {
            return true;
        }

        if(expression.equalsIgnoreCase("true")) {
            return true;
        }

        if(expression.equalsIgnoreCase("false")) {
            return false;
        }

        expression = solve(expression);

        if(expression.matches("^[0-9\\-+*%/<>=&.|() !]*$")) {
            try {
                ScriptEngineManager sem = new ScriptEngineManager();
                ScriptEngine se = sem.getEngineByName("JavaScript");
                Object result = se.eval(expression);
                return (Boolean) result;
            } catch (Exception e) {
                System.out.println("Invalid expression " + expression);
                e.printStackTrace();
            }
        } else {
            System.out.println("Illegal expression" + expression);
        }
        return false;
    }


    public String solve(String content) {
        TagMatcher m = new TagMatcher(content);
        while(m.find()) {
            Selection select = summary.getSelection(m.getIdentifier());
            if(null != select) {
                //...
            } else {
                String v = summary.getValueOf(m.getIdentifier());
                String regex = m.getTag().replace("\\.","\\\\.");
                if(v == null) {
                    content = content.replaceAll(regex, "..");
                } else {
                    content = content.replaceAll(regex, v).replaceAll(",",".").replaceAll("\\s*}", "");
                }
            }
        }
        return content;
    }



    private static class TagMatcher {

        private static final Pattern tags = Pattern.compile("(([a-zA-Z][^\\.\\_]+)(_([^\\.]+))?)\\.value");
        private Matcher matcher;

        TagMatcher(String content) {
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
            return "value";
        }
    }

}
