package fi.thl.pivot.export;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.ui.Model;

import com.google.common.base.Joiner;

import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.pivot.model.Pivot;
import fi.thl.pivot.model.PivotCell;
import fi.thl.pivot.model.PivotLevel;
import fi.thl.pivot.util.ThreadRole;

public class JsonStatExporter {

    private static final Logger LOG = Logger.getLogger(JsonStatExporter.class);
    private static final Pattern NUMBER = Pattern.compile("^-?\\d+([,\\.]\\d+)?$");
    
    public void export(Model model, OutputStream out) throws IOException {
        PrintWriter writer = null;
        Map<String, Object> params = model.asMap();
        try {
            writer = new PrintWriter(new BufferedOutputStream(out));
            if (isSet(params, "jsonp")) {
                writer.println("thl.pivot.fromJsonStat(");
            }

            writer.println("{");
            exportMetadata(writer, params);
            writer.println("}");

            if (isSet(params, "jsonp")) {
                writer.println(");");
            }
            writer.flush();
        } finally {
            close(writer);
        }

    }

    private boolean isSet(Map<String, Object> model, String attribute) {
        return Boolean.TRUE.equals(model.get(attribute));
    }

    private void exportMetadata(PrintWriter writer, Map<String, Object> model) {
        Pivot pivot = (Pivot) model.get("pivot");
        Label label = (Label) model.get("cubeLabel");

        List<String> identifiers = new ArrayList<>();
        List<Integer> sizes = new ArrayList<>();
        for (PivotLevel level : pivot.getRows()) {
            String id = findUniqueId(identifiers, level);
            identifiers.add(quote(escape(id)));
            sizes.add(level.size());
        }
        for (PivotLevel level : pivot.getColumns()) {
            String id = findUniqueId(identifiers, level);
            identifiers.add(quote(escape(id)));
            sizes.add(level.size());
        }
        boolean showValueTypes = isSet(model, "showCi") || isSet(model, "showSampleSize");
        if (showValueTypes) {
            identifiers.add("\"tiku_vtype\"");
            sizes.add(4);
        }

        writer.println("\t\"dataset\": {");
        writer.println("\t\t\"version\": \"2.0\",");
        writer.println("\t\t\"class\": \"dataset\",");
        writer.println(String.format("\t\t\"label\": \"%s\",", escape(label.getValue(ThreadRole.getLanguage()))));

        writer.println("\t\t\"dimension\": {");

        exportDimensionIdentifiers(writer, identifiers);
        exportDimensionSizes(writer, sizes);
        int i = 0;
        i = exportLevels(writer, model, identifiers, i, pivot.getRows());
        i = exportLevels(writer, model, identifiers, i, pivot.getColumns());
        if (showValueTypes) {
            writer.println(",\"tiku_vtype\": { \"category\": { \"index\": { \"v\": 0, \"ci_lower\": 1, \"ci_upper\": 2, \"n\": 3 } } }");
        }
        writer.print("\n\t\t}");

        exportValues(writer, pivot, showValueTypes);

        writer.println("\t}");
    }

    private String findUniqueId(List<String> identifiers, PivotLevel level) {
        String id = level.getDimension().getId();
        if (identifiers.contains(quote(escape(id)))) {
            String originalId = id;
            int i = 1;
            while (identifiers.contains(quote(escape(id)))) {
                id = originalId + i;
            }
        }
        return id;
    }

    private void exportValues(PrintWriter writer, Pivot pivot, boolean showValueTypes) {
        int count = 0;
        for (PivotCell cell : pivot) {
            int index = cell.getPosition();
            if (showValueTypes) {
                index *= 4;
            }
            String value = cell.getValue();
            if (value != null) {
                if (count > 0) {
                    writer.write(",\n\t\t\t");
                } else {
                    writer.println(",\n\t\t\"value\": {");
                    writer.print("\t\t\t");
                }
                printValue(writer, index, value);
                if (showValueTypes) {
                    writer.print(",");
                    printValue(writer, index + 1, cell.getConfidenceLowerLimit());

                    writer.print(",");
                    printValue(writer, index + 2, cell.getConfidenceUpperLimit());

                    writer.print(",");
                    printValue(writer, index + 3, cell.getSampleSize());
                }
                ++count;
            }
        }
        if (count > 0) {
            writer.println("\n\t\t}");
        } else {
            writer.println(",\n\t\t\"value\": []");
        }
    }

    private void printValue(PrintWriter writer, int i, String value) {
        // All values are handled as text to preserve accuracy of 
        // values e.g. if handled as numbers then 0,0 would be reduced 0 
        
//        if(NUMBER.matcher(value).matches()) {
//            writer.write(String.format("\"%d\": %s", i, value.replace(",", ".")));
//        } else {
            writer.write(String.format("\"%d\": \"%s\"", i, value.replace("\"", "\"\"")));
//        }
    }

    private int exportLevels(PrintWriter writer, Map<String, Object> model, List<String> identifiers, int index, List<PivotLevel> levels) {
        for (PivotLevel level : levels) {
            if (level.size() == 0) {
                continue;
            }
            writer.println(String.format(",\n\t\t\t%s : {", identifiers.get(index++)));
            writer.println("\t\t\t\t\"category\": {");
            writer.print("\t\t\t\t\t\"index\": {");
            List<String> nodes = new ArrayList<>();
            int i = 0;
            if (isSet(model, "surrogate")) {
                for (DimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\t\t\t\t\t\t\"%s\": %d", node.getSurrogateId(), i++));
                }
            } else {
                for (DimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\t\t\t\t\t\t\"%s\": %d", escape(node.getId()), i++));
                }
            }
            writer.print("\t\t\t\t\t");
            writer.println(Joiner.on(",").join(nodes));
            writer.println("\t\t\t\t\t},");

            writer.print("\t\t\t\t\t\"label\": {");
            nodes = new ArrayList<>();
            i = 0;
            if (isSet(model, "surrogate")) {
                for (DimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\t\t\t\t\t\t\"%s\": \"%s\"", node.getSurrogateId(), escape(node.getLabel().getValue(ThreadRole.getLanguage()))));

                }
            } else {
                for (DimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\t\t\t\t\t\t\"%s\": \"%s\"", escape(node.getId()), escape(node.getLabel().getValue(ThreadRole.getLanguage()))));
                }
            }

            writer.print("\t\t\t\t\t");
            writer.println(Joiner.on(",").join(nodes));
            writer.println("\t\t\t\t\t}");

            writer.print("\t\t\t\t}\n\t\t\t}");
        }
        return index;
    }

    private void exportDimensionSizes(PrintWriter writer, List<Integer> sizes) {
        writer.println(",");
        writer.println("\t\t\t\"size\": [");
        writer.print("\t\t\t\t");
        writer.println(Joiner.on(",").join(sizes));
        writer.print("\t\t\t]");
    }

    private void exportDimensionIdentifiers(PrintWriter writer, List<String> identifiers) {
        writer.println("\t\t\t\"id\": [");
        writer.print("\t\t\t\t");
        writer.println(Joiner.on(",").join(identifiers));
        writer.print("\t\t\t]");
    }

    private String quote(String value) {
        return String.format("\"%s\"", value);
    }

    private String escape(String value) {
        if (null == value) {
            return "";
        }
        return value.replaceAll("\"", "\\\"");
    }

    private void close(PrintWriter writer) {
        try {
            if (null != writer) {
                writer.close();
            }
        } catch (Exception e) {
            LOG.warn("Could not close output stream");
        }
    }
}
