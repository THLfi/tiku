package fi.thl.pivot.export;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fi.thl.pivot.model.IDimensionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.Label;

public class DimensionTreeExporter {

    private final Logger logger = LoggerFactory.getLogger(DimensionTreeExporter.class);

    public void export(Model model, OutputStream out) throws IOException {
        PrintWriter writer = null;
        Map<String, Object> params = model.asMap();
        try {
            writer = new PrintWriter(new BufferedOutputStream(out, 8192));
            // if (isSet(params, "jsonp")) {
            writer.println("thl.pivot.loadDimensions(");
            // }

            writer.println('[');
            exportMetadata(writer, params);
            writer.println(']');

            // if (isSet(params, "jsonp")) {
            writer.println(");");
            // }
            writer.flush();
        } finally {
            close(writer);
        }

    }

    private void exportMetadata(PrintWriter writer, Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        Collection<Dimension> dimensions = (Collection<Dimension>) params.get("dimensions");
        String language = ((Locale) params.get("lang")).getLanguage();
        boolean first = true;
        for (Dimension dimension : dimensions) {
            exportDimension(writer, dimension, first, language);
            first = false;
        }

    }

    private void exportDimension(PrintWriter writer, Dimension dimension, boolean first, String language) {
        if (!first) {
            writer.println(',');
        }
        writer.println('{');
        boolean isCommaNeeded = false;
        isCommaNeeded = attribute(writer, "id", dimension.getId(), isCommaNeeded);
        isCommaNeeded = attribute(writer, "label", label(dimension.getLabel().getValue(language)), isCommaNeeded);
        writer.println(",\n\t\"children\": [");
        boolean firstNode = true;
        for (IDimensionNode node : dimension.getRootLevel().getNodes()) {
            firstNode = exportNode(writer, node, firstNode, language);
        }
        writer.print("]}");
        first = false;
    }

    private boolean exportNode(PrintWriter writer, IDimensionNode node, boolean first, String language) {
        if(node.isHidden() && node.isMeasure()) {
            return first;
        }

        if (!first) {
            writer.println(',');
        }
        writer.println('{');
        boolean isCommaNeeded = false;
        isCommaNeeded = attribute(writer, "id", node.getId(), isCommaNeeded);
        isCommaNeeded = attribute(writer, "sid", node.getSurrogateId(), isCommaNeeded);
        isCommaNeeded = attribute(writer, "label", label(node.getLabel().getValue(language)), isCommaNeeded);
        isCommaNeeded = attribute(writer, "stage", node.getLevel().getId(), isCommaNeeded);
        isCommaNeeded = attribute(writer, "code", node.getCode(), isCommaNeeded);
        isCommaNeeded = attribute(writer, "sort", node.getSort(), isCommaNeeded);
        if(node.isDecimalsSet()) {
            isCommaNeeded = attribute(writer, "decimals", node.getDecimals(), isCommaNeeded);
        }
        isCommaNeeded = attribute(writer, "uri", node.getReference(), isCommaNeeded);

        Set<Entry<String, Label>> properties = node.getProperties();
        if (properties != null && !properties.isEmpty()) {
            writer.println(",\n\t\"properties\": {");
            boolean isPropertyCommaNeeded = false;
            for (Map.Entry<String, Label> p : properties) {
                isPropertyCommaNeeded = attribute(writer, p.getKey(), p.getValue().getValue(language),
                        isPropertyCommaNeeded);
            }
            writer.print("\n\t}");
        }

        Collection<IDimensionNode> children = node.getChildren();
        if (!children.isEmpty()) {
            writer.println(",\n\t\"children\": [");
            boolean firstNode = true;
            for (IDimensionNode child : children) {
                exportNode(writer, child, firstNode, language);
                firstNode = false;
            }
            writer.print(']');
        } else {
            writer.println(",\n\t\"children\": []");
        }
        writer.print("\n}");
        return false;
    }

    private boolean attribute(PrintWriter writer, String attribute, String value, boolean isCommaNeeded) {
        if (null != value) {
            if (isCommaNeeded) {
                writer.println(',');
            }
            writer.print('\t');
            writer.print('"');
            writer.print(attribute);
            writer.print('"');
            writer.print(':');
            writer.print('"');
            escape(writer, value);
            writer.print('"');
            return true;
        }
        return isCommaNeeded;
    }

    private boolean attribute(PrintWriter writer, String attribute, Number value, boolean isCommaNeeded) {
        if (null != value) {
            if (isCommaNeeded) {
                writer.println(',');
            }
            writer.print('\t');
            writer.print('"');
            writer.print(attribute);
            writer.print('"');
            writer.print(':');
            writer.print(value);
            return true;
        }
        return isCommaNeeded;
    }

    private String label(String label) {
        return label == null ? "n/a" : label;
    }

    private void escape(PrintWriter writer, String value) {
        if (null == value) {
            return;
        }
        for(char c : value.toCharArray()) {
            switch(c) {
            case '"':
                writer.print('\\');
                writer.print('"');
                break;
            case '\n':
            case '\r':
                writer.print('\\');
                writer.print('n');
                break;
            default:
                writer.print(c);
                break;
            }
        }
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
