package fi.thl.pivot.export;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import fi.thl.pivot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.google.common.base.Joiner;

import fi.thl.pivot.util.ThreadRole;

public class JsonStatExporter {

    private final Logger logger = LoggerFactory.getLogger(JsonStatExporter.class);
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

        writer.println("\"dataset\": {");
        writer.println("\"version\": \"2.0\",");
        writer.println("\"class\": \"dataset\",");
        writer.println(String.format("\"label\": \"%s\",", escape(label.getValue(ThreadRole.getLanguage()))));

        writer.println("\"dimension\": {");

        exportDimensionIdentifiers(writer, identifiers);
        exportDimensionSizes(writer, sizes);
        int i = 0;
        i = exportLevels(writer, model, identifiers, i, pivot.getRows());
        i = exportLevels(writer, model, identifiers, i, pivot.getColumns());
        if (showValueTypes) {
            writer.println(
                    ",\"tiku_vtype\": { \"category\": { \"index\": { \"v\": 0, \"ci_lower\": 1, \"ci_upper\": 2, \"n\": 3 } } }");
        }
        writer.print("\n}");

        exportValues(writer, pivot, showValueTypes);

        writer.println("}");
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
                    writer.write(",\n");
                } else {
                    writer.println(",\n\"value\": {");
                    writer.print("");
                }
                printValue(writer, index, value);
                if (showValueTypes) {

                    if (null != cell.getConfidenceLowerLimit()) {
                        writer.print(",");
                        printValue(writer, index + 1, cell.getConfidenceLowerLimit());
                    }

                    if (null != cell.getConfidenceUpperLimit()) {
                        writer.print(",");
                        printValue(writer, index + 2, cell.getConfidenceUpperLimit());
                    }

                    if (null != cell.getSampleSize()) {
                        writer.print(",");
                        printValue(writer, index + 3, cell.getSampleSize());
                    }
                }
                ++count;
            }
        }
        if (count > 0) {
            writer.println("\n}");
        } else {
            writer.println(",\n\"value\": []");
        }
    }

    private void printValue(PrintWriter writer, int i, String value) {
        // All values are handled as text to preserve accuracy of
        // values e.g. if handled as numbers then 0,0 would be reduced 0

        // if(NUMBER.matcher(value).matches()) {
        // writer.write(String.format("\"%d\": %s", i, value.replace(",", ".")));
        // } else {
        writer.write(String.format("\"%d\": \"%s\"", i, value.replace("\"", "\\\"")));
        // }
    }

    private int exportLevels(PrintWriter writer, Map<String, Object> model, List<String> identifiers, int index,
            List<PivotLevel> levels) {
        for (PivotLevel level : levels) {
            if (level.size() == 0) {
                continue;
            }
            writer.println(String.format(",\n%s : {", identifiers.get(index++)));
            writer.println("\"category\": {");
            writer.print("\"index\": {");
            List<String> nodes = new ArrayList<>();
            int i = 0;
            if (isSet(model, "surrogate")) {
                for (IDimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\"%s\": %d", node.getSurrogateId(), i++));
                }
            } else {
                for (IDimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\"%s\": %d", escape(node.getId()), i++));
                }
            }
            writer.print("");
            writer.println(Joiner.on(",").join(nodes));
            writer.println("},");

            writer.print("\"label\": {");
            nodes = new ArrayList<>();
            i = 0;
            if (isSet(model, "surrogate")) {
                for (IDimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\"%s\": \"%s\"", node.getSurrogateId(),
                            escape(node.getLabel().getValue(ThreadRole.getLanguage()))));

                }
            } else {
                for (IDimensionNode node : level.getNodes()) {
                    nodes.add(String.format("\n\"%s\": \"%s\"", escape(node.getId()),
                            escape(node.getLabel().getValue(ThreadRole.getLanguage()))));
                }
            }

            writer.print("");
            writer.println(Joiner.on(",").join(nodes));
            writer.println("}");

            writer.print("}\n}");
        }
        return index;
    }

    private void exportDimensionSizes(PrintWriter writer, List<Integer> sizes) {
        writer.println(",");
        writer.println("\"size\": [");
        writer.print("");
        writer.println(Joiner.on(",").join(sizes));
        writer.print("]");
    }

    private void exportDimensionIdentifiers(PrintWriter writer, List<String> identifiers) {
        writer.println("\"id\": [");
        writer.print("");
        writer.println(Joiner.on(",").join(identifiers));
        writer.print("]");
    }

    private String quote(String value) {
        return String.format("\"%s\"", value);
    }

    private String escape(String value) {
        if (null == value) {
            return "";
        }
        return value.replaceAll("\\\"", "\\\\\\\"");
    }

    private void close(PrintWriter writer) {
        try {
            if (null != writer) {
                writer.close();
            }
        } catch (Exception e) {
            logger.warn("Could not close output stream");
        }
    }
}
