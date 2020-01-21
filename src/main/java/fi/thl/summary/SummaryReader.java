package fi.thl.summary;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fi.thl.summary.model.*;
import fi.thl.summary.model.SummaryDimension.TotalMode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.thl.pivot.model.Label;

/**
 * <p>
 * SummaryReaders parses a summary definition XML and constructs a summary
 * object based on the configurion presented. SummaryReader does not validate
 * the XML document it reads nor enforce complience witha schema. If the
 * document contains properties that SummaryReader does not regonice they are
 * skipped.
 * </p>
 * 
 * <p>
 * SummaryReader is intended to be used on per summary definition. Please do not
 * read multiple documents with single instace.
 * </p>
 * 
 * @author aleksiyrttiaho
 *
 */
public class SummaryReader {

    private static final String SECTION_ELEMENT = "section";
    private static final String ROW_ELEMENT = "row";
    private static final String GRID_ELEMENT = "grid";
    private static final String DEFAULT_STAGE = ":all:";
    private static final String STAGE_ELEMENT = "stage";
    private static final String DIMENSION_ELEMENT = "dim";
    private static final String LANGUAGE_ELEMENT = "lang";
    private static final String BOOLEAN_YES = "yes";

    private static final String SUPPRESSION_ATTRIBUTE = "suppression";
    private static final String HIGHLIGHT_ATTRIBUTE = "highlight";

    private static final List<String> textPresentations = ImmutableList.of("subtitle", "subtitle1", "subtitle2", "subtitle3", "info");
    private static final List<String> dataPresentations = ImmutableList.of("line", "list", "bar", "gauge", "pie", "radar", "map");
    private static final List<String> tablePresentations = ImmutableList.of("table");

    private static final List<String> stageDefinitionElements = ImmutableList.of("stage", "stageMulti");
    private static final List<String> itemDefinitionElements = ImmutableList.of("item", "itemMulti");

    private Summary summary;
    private Document document;
    private Node root;
    private boolean isDocumentParsed;

    public SummaryReader() {
        this.summary = new Summary();
    }

    /**
     * Returns the parsed summary. Should be used after the contents have been
     * read using the read-method
     * 
     * @return
     */
    public Summary getSummary() {
        return summary;
    }

    public void read(InputStream in) throws SummaryException {
        assertOnlyOneDocumentRead();
        try {
            this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);

            root = document.getElementsByTagName("summary").item(0);

            parseSummaryAttributes();
            parseSupportedLanguages();

            summary.setFactTable(document.getElementsByTagName("fact").item(0).getTextContent());

            summary.setSubject(label(root, "subject"));
            summary.setTitle(label(root, "title"));
            summary.setLink(label(root, "link"));
            summary.setNote(label(root, "note"));

            parseFilters();
            parsePresentations();
            parseValues();

            summary.setSource(documentAsString());
        } catch (Exception e) {
            throw new SummaryException(e.getMessage(), e);
        }
    }

    private String documentAsString()
            throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.transform(new DOMSource(document), result);
        return writer.toString();
    }

    private void assertOnlyOneDocumentRead() {
        if (isDocumentParsed) {
            throw new IllegalStateException("Attempt to read multiple documents using the same SummaryReader intance");
        }
        isDocumentParsed = true;
    }

    private void parseValues() {
        for (Node node : iterator(root, "value")) {
            Value value = new Value();
            value.setId(attribute(node, "id").toLowerCase());
            for(Node bind : iterator(node, "bind")) {
                Selection s = new Selection();
                s.setDimension(attribute(bind, "dim"));
                s.addItem(bind.getTextContent().toLowerCase());
                s.addDefaultItem(bind.getTextContent().toLowerCase());
                value.addFilter(s);
            }
            for(Selection s : parseFilter(summary, node, "filterref")) {
                value.addFilter(s);
            }
            for(Selection s : parseFilter(summary, node, "measureref")) {
                value.addFilter(s);
            }
            
            summary.addValue(value);
        }
    }

    private void parseFilters() {
        for (Node node : iterator(root, "select")) {

            Selection selection = new Selection();
            selection.setId(attribute(node, "id").toLowerCase());
            selection.setVisible(!BOOLEAN_YES.equals(attribute(node, "hidden")));
            selection.setMultiple(BOOLEAN_YES.equals(attribute(node, "multi")));
            selection.setSearchable(BOOLEAN_YES.equals(attribute(node, "search")));

            selection.setLabelMode(Selection.LabelMode.fromString(attribute(node, "label")));
            selection.setSelectionMode(Selection.SelectionMode.fromString(attribute(node, "include_descendants")));
            
            
            selection.setDimension(firstChildNode(node, DIMENSION_ELEMENT).getTextContent());

            for (Node item : iterator(node, "item")) {
                selection.addItem(item.getTextContent());
            }
            for (Node set : iterator(node, "set")) {
                selection.addSet(set.getTextContent());
            }
            for (Node stage : iterator(node, STAGE_ELEMENT)) {
                selection.addStage(stage.getTextContent());
            }
            for (Node def : iterator(node, "default")) {
                selection.addDefaultItem(def.getTextContent());
            }

            summary.addSelection(selection);
        }
    }

    private void parsePresentations() {
        boolean gridFound = false;
        Section gridSection = new Section();

        // Grid based summary description
        gridFound = parseGrid(gridSection, gridFound);

        // Non-grid based summary description
        if (!gridFound) {
            Section row = new Section();
            Section block = new Section();

            row.addChild(block);
            gridSection.addChild(row);
            parsePresentationInSection(root, block);
        }

        summary.setSections(gridSection.getChildren());
    }

    private boolean parseGrid(Section gridSection, boolean gridFound) {
        for (Node grid : iterator(root, GRID_ELEMENT)) {
            gridFound = true;
            for (Node row : iterator(grid, ROW_ELEMENT)) {
                Section rowSection = new Section();
                gridSection.addChild(rowSection);
                for (Node column : iterator(row, SECTION_ELEMENT)) {
                    Section columnSection = new Section();
                    columnSection.setWidth(attribute(column, "columns"));
                    parsePresentationInSection(column, columnSection);
                    rowSection.addChild(columnSection);
                }
            }
        }
        return gridFound;
    }

    private void parsePresentationInSection(Node block, Section section) {
        List<Presentation> group = Lists.newArrayList();
        for (Node node : iterator(block, "presentation")) {
            Presentation p = createPresentationParserFor(node).parse();
            if (p.isFirst() || !group.isEmpty()) {
                group.add(p);
            }
            if (p.isLast()) {
                for (Presentation member : group) {
                    member.setGroupSize(group.size());
                }
                group.clear();
            }
            section.addPresentation(p);

            // Still added to summary to limit code changes required
            summary.addPresentation(p);
        }
    }

    private PresentationParser createPresentationParserFor(final Node node) {
        for (Node child : iterator(node.getChildNodes())) {
            if ("type".equals(child.getNodeName())) {
                // Type content has been handled as case-insensitive so we
                // have to convert the type definition to lower case to
                // ensure backwards compatibility
                String type = child.getTextContent().toLowerCase();
                if (textPresentations.contains(type)) {
                    return new TextPresentationParser(node);
                } else if (dataPresentations.contains(type)) {
                    return new DataPresentationParser(summary, node);
                } else if (tablePresentations.contains(type)) {
                    return new TablePresentationParser(summary, node);
                } else {
                    return new SentinelPresentationParser("Unsupported presentation " + type);
                }
            }
        }

        return new SentinelPresentationParser("Undefined type for presentation");
    }

    private static Label label(Node parent, String nodeName) {
        Label label = new Label();
        for (Node node : iterator(parent, nodeName)) {
            label.setValue(attribute(node, LANGUAGE_ELEMENT), node.getTextContent());
        }
        return label;
    }

    private void parseSupportedLanguages() {
        for (Node node : iterator(document.getElementsByTagName(LANGUAGE_ELEMENT))) {
            summary.supportLanguage(node.getTextContent());
            if (BOOLEAN_YES.equals(attribute(node, "define"))) {
                summary.setItemLanguage(node.getTextContent());
            }
        }
    }

    private static Iterable<Node> iterator(Node parent, String nodeName) {
        List<Node> nodes = Lists.newArrayList();
        for (Node child : iterator(parent.getChildNodes())) {
            if (nodeName.equals(child.getNodeName())) {
                nodes.add(child);
            }
        }
        return nodes;
    }

    private static Iterable<Node> iterator(final NodeList nodeList) {
        return new NodeListIterator(nodeList);
    }

    private void parseSummaryAttributes() {

        Node id = root.getAttributes().getNamedItem("id");
        if (null != id) {
            summary.setId(id.getTextContent());
        }

        summary.setId(attribute(root, "id"));
        if ("ref".equals(attribute(root, "scheme"))) {
            summary.setScheme(Summary.Scheme.Reference);
        }
        summary.setSpecificationVersion(attribute(root, "spec"));
        summary.setDrillEnabled(booleanAttribute(root, "drill"));
    }

    private static boolean booleanAttribute(Node node, String attribute) {
        return "yes".equalsIgnoreCase(attribute(node, attribute));
    }

    private static Node firstChildNode(Node parent, String nodeName) {
        for (Node child : iterator(parent, nodeName)) {
            return child;
        }
        return null;
    }

    private static String attribute(Node node, String attribute) {
        if (null == node) {
            return null;
        }
        Node attributeNode = node.getAttributes().getNamedItem(attribute);
        if (null == attributeNode) {
            return null;
        }
        return attributeNode.getTextContent();
    }
    

    private static List<Selection> parseFilter(Summary summary, Node node, String type) {
        Set<String> ids = Sets.newHashSet();
        List<Selection> filters = Lists.newArrayList();
        for (Node n : iterator(node, type)) {
            if (ids.contains(n.getTextContent().toLowerCase())) {
                throw new IllegalStateException(n.getTextContent() + " defined multiple times");
            }
            ids.add(n.getTextContent().toLowerCase());
            filters.add(summary.getSelection(n.getTextContent().toLowerCase()));
        }
        return filters;
    }


    private static interface PresentationParser {

        Presentation parse();

    }

    private static final class SentinelPresentationParser implements PresentationParser {

        private String msg;

        public SentinelPresentationParser(String message) {
            this.msg = message;
        }

        @Override
        public Presentation parse() {
            TextPresentation p = new TextPresentation();
            p.setId("error");
            p.setType("error");
            Label content = new Label();
            content.setValue("en", msg);
            p.setContent(content);
            return p;
        }
    }

    private static class TablePresentationParser extends DataPresentationParser {

        private static final String FILTER_STAGE = ":filter:";

        public TablePresentationParser(Summary summary, Node node) {
            super(summary, node);
        }

        @Override
        public Presentation parse() {
            TablePresentation p = new TablePresentation();

            p.setSuppress(Presentation.SuppressMode.fromString(attribute(node, SUPPRESSION_ATTRIBUTE)));
            p.setHighlight(Presentation.HighlightMode.fromString(attribute(node, HIGHLIGHT_ATTRIBUTE)));

            // We have to parse filters before dimensions as dimension
            // may have an unspecified stage in which case if filter
            // for the same dimension is present then all nodes in dimensions
            // should be shown i.e. :all: -stage is used and if filter is
            // present
            // the filtered values hould be shown i.e. :filter: -stage should be
            // used
            super.parseFilter(p, "filterref");
            super.parseFilter(p, "measureref");

            for (Node row : iterator(node, "rows")) {
                addRow(p, row);
            }
            for (Node row : iterator(node, "rowsMulti")) {
                addRow(p, row);
            }
            for (Node col : iterator(node, "columns")) {
                addColumn(p, col);
            }
            for (Node col : iterator(node, "columnsMulti")) {
                addColumn(p, col);
            }

            return parse(p);
        }

        private void addColumn(TablePresentation p, Node col) {
            Node dim = firstChildNode(col, DIMENSION_ELEMENT);
            final SummaryItem item;
            if (null != dim) {
                item = p.addColumn(dim.getTextContent(), getStage(p, col), showTotal(dim));
            } else {
                item = p.addColumn(listMeasures(col));
            }
            align(col, item);
        }

        private void addRow(TablePresentation p, Node row) {
            Node dim = firstChildNode(row, DIMENSION_ELEMENT);
            final SummaryItem item;
            if (null != dim) {
                item = p.addRow(dim.getTextContent(), getStage(p, row), showTotal(dim));
            } else {
                item = p.addRow(listMeasures(row));
            }
            align(row, item);
        }

        private TotalMode showTotal(Node dim) {
            try {
                return TotalMode.valueOf(attribute(dim, "total").toUpperCase());
            }
            catch (NullPointerException e) {
                return TotalMode.NO;
            }
            catch (IllegalArgumentException e) {
                return TotalMode.NO;
            }
        }

        private void align(Node col, final SummaryItem item) {
            String valueAlign = attribute(col, "aligndata");
            String headerAlign = attribute(col, "alignheader");
            String allAlign = attribute(col, "alignall");
            item.align(valueAlign, headerAlign, allAlign);
        }

        private SummaryStage getStage(TablePresentation p, Node row) {
            Node stageNode = firstChildNode(row, STAGE_ELEMENT);
            if (stageNode == null) {
                if (super.hasFilterForDimension(p, firstChildNode(row, DIMENSION_ELEMENT))) {
                    return new SummaryStage(SummaryStage.Type.STAGE, FILTER_STAGE);
                } else {
                    return new SummaryStage(SummaryStage.Type.STAGE, DEFAULT_STAGE);
                }
            } else {
                return new SummaryStage(SummaryStage.Type.STAGE, stageNode.getTextContent());
            }
        }

    }

    private static class DataPresentationParser implements PresentationParser {

        private static final String ATTRIBUTE_SORT = "sort";
        private static final String LEGENDLESS_ATTRIBUTE = "legendless";
        protected final Node node;
        private final Summary summary;

        private DataPresentationParser(Summary summary, Node node) {
            this.summary = summary;
            this.node = node;
        }

        @Override
        public Presentation parse() {
            DataPresentation p = new DataPresentation();
            p.setId(attribute(node, "id"));
            parseFilter(p, "filterref");
            parseFilter(p, "measureref");

            for (Node emphasizeNode : iterator(node, "emphasize")) {
                if (null != emphasizeNode) {
                    p.addEmphasize(emphasizeNode.getTextContent());
                }
            }

            return parse(p);
        }

        protected Presentation parse(DataPresentation p) {

            p.setSuppress(Presentation.SuppressMode.fromString(attribute(node, SUPPRESSION_ATTRIBUTE)));
            Node typeNode = firstChildNode(node, "type");
            // As it happens, old version was case insensitive so we have to
            // make this case insensitive as well.
            String type = typeNode.getTextContent().toLowerCase();
            type = refineBarTypeAttributes(typeNode, type);
            p.setType(type);
            if (null != attribute(typeNode, ATTRIBUTE_SORT)) {
                p.setSortMode(Presentation.SortMode.valueOf(attribute(typeNode, ATTRIBUTE_SORT).toLowerCase()));
            }
            if (null != attribute(typeNode, LEGENDLESS_ATTRIBUTE)) {
                p.setLegendless(Presentation.Legendless.fromString(attribute(typeNode, LEGENDLESS_ATTRIBUTE)));
            }
            p.setShowConfidenceInterval(BOOLEAN_YES.equals(attribute(typeNode, "ci")));
            p.setShowSampleSize(BOOLEAN_YES.equals(attribute(typeNode, "n")));
            p.setMin(parseIntAttribute(typeNode, "min"));
            p.setMax(parseIntAttribute(typeNode, "max"));
            p.setPalette(attribute(typeNode, "palette"));
            p.setGeometry(attribute(typeNode, "geometry"));
            List<SummaryStage> stages = new ArrayList<>();
            parseDimensions(p, stages, DIMENSION_ELEMENT, "dimMulti");
            List<String> dimensions = new ArrayList<>();
            for (SummaryStage stage : stages) {
                dimensions.add(stage.getDimensionId());
            }

            if (dimensions.size() != stages.size()) {
                return new SentinelPresentationParser("Number of dimensions and stages do not match").parse();
            }

            for (int i = 0; i < dimensions.size(); ++i) {
                p.addDimension(dimensions.get(i), stages.get(i));
            }

            List<MeasureItem> measures = listMeasures(node);
            p.addMeasures(measures);

            p.setFirst("begin".equals(attribute(node, "group")));
            p.setLast("end".equals(attribute(node, "group")));

            MeasureItem widthMeasure = extractWidthMeasure(node);
            if (widthMeasure != null) {
                p.setWidthMeasure(widthMeasure);
            }

            Node ruleNode = firstChildNode(node, "rule");
            if(null != ruleNode) {
                Rule rule = new Rule();
                rule.setExpression(ruleNode.getTextContent());
                p.setRule(rule);
            }

            return p;
        }

        private MeasureItem extractWidthMeasure(Node node) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                Node child = children.item(i);
                if ("widthmeasure".equals(child.getNodeName())) {
                    return new MeasureItem(MeasureItem.Type.LABEL, child.getTextContent());
                } else if ("widthref".equals(child.getNodeName())) {
                    return new MeasureItem(MeasureItem.Type.REFERENCE, child.getTextContent());
                } 
            }
            return null;
        }

        private Integer parseIntAttribute(Node node, String a) {
            Integer value = null;
            String valueString = attribute(node, a);
            if (null != valueString && valueString.matches("^\\d+$")) {
                value = Integer.parseInt(valueString);
            }
            return value;
        }

        /**
         * Goes through child nodes finding dimension definition and their
         * corresponding stage definitions. The stage definition may be missing
         * so we have to check if there is one and fall back on a default stage
         * if no stage definition is found.
         *
         */
        private void parseDimensions(DataPresentation p, List<SummaryStage> stages, String... dimensionNodeNames) {
            for (String nodeName : dimensionNodeNames) {
                for (Node dimension : iterator(node, nodeName)) {
                    List<String> items = new ArrayList<>();
                    SummaryStage.Type type = SummaryStage.Type.STAGE;
                    Node candidate = getNextElementNode(dimension);
                    while (candidate != null) {
                        if (itemDefinitionElements.contains(candidate.getNodeName())) {
                            items.add(candidate.getTextContent());
                            candidate = getNextElementNode(candidate);
                            type = SummaryStage.Type.ITEMS;
                            continue;
                        }
                        if (stageDefinitionElements.contains(candidate.getNodeName())) {
                            items.add(candidate.getTextContent());
                            candidate = getNextElementNode(candidate);
                            continue;
                        }
                        break;
                    }
                    if (items.isEmpty()) {
                        boolean hasFilter = hasFilterForDimension(p, dimension);
                        items.add(hasFilter ? ":filter:" : DEFAULT_STAGE);
                    };
                    stages.add(new SummaryStage(type, items, dimension.getTextContent()));
                }
            }
        }

        /**
         * Finds the next element node on the same level as the dimension node.
         * This is done to prevent false negatives when the next node should be
         * a text node, a comment node or an enitity node - none of which we are
         * interested in. See {@link Node} constants.
         * 
         * @param dimension
         * @return
         */
        private Node getNextElementNode(Node dimension) {
            Node next = dimension.getNextSibling();
            while (null != next) {
                if (next.getNodeType() == Node.ELEMENT_NODE) {
                    return next;
                }
                next = next.getNextSibling();
            }
            return null;
        }

        private boolean hasFilterForDimension(DataPresentation p, Node dimension) {
            boolean hasFilter = false;
            for (Selection s : p.getFilters()) {
                if (s.getDimension().equals(dimension.getTextContent())) {
                    hasFilter = true;
                    break;
                }
            }
            return hasFilter;
        }

        /**
         * Expands the type definition of a bar-presentation
         * 
         * @param typeNode
         * @param type
         * @return
         */
        private String refineBarTypeAttributes(Node typeNode, String type) {
            if ("bar".equals(type)) {

                if (!"horizontal".equals(attribute(typeNode, "orientation"))) {
                    type = "column";
                }

                if ("stack".equals(attribute(typeNode, "multi"))) {
                    type += "stacked";
                }

                if ("stack100".equals(attribute(typeNode, "multi"))) {
                    type += "stacked100";
                }
            }
            return type;
        }

        private void parseFilter(DataPresentation p, String type) {
            for(Selection s : SummaryReader.parseFilter(summary, node, type)) {
                p.addFilter(s);
            }
        }

    }

    /**
     * Parses various presentation elements that only contains different
     * language versions of some textual content. Expects
     * 
     * <code><pre>
     *  <TEXT_TYPE>
     *      <content lang="XX"> TEXT </content>
     *      ...
     *  </TEXT_TYPE>
     * </pre></code>
     * 
     * 
     * @author aleksiyrttiaho
     *
     */
    private static final class TextPresentationParser implements PresentationParser {
        private Node node;

        public TextPresentationParser(Node node) {
            this.node = node;
        }

        @Override
        public Presentation parse() {
            TextPresentation p = new TextPresentation();
            p.setId(attribute(node, "id"));
            p.setType(firstChildNode(node, "type").getTextContent().toLowerCase());
            p.setContent(label(node, "content"));

            Node ruleNode = firstChildNode(node, "rule");
            if(null != ruleNode) {
                Rule rule = new Rule();
                rule.setExpression(ruleNode.getTextContent());
                p.setRule(rule);
            }

            return p;
        }
    }

    /**
     * Utility wrapper that implements Iterator and Iterable interfaces over
     * NodeList. This allows Nodelists to be iterated in modern loop constructs
     * and to be used with Guava collections.
     * 
     * @author aleksiyrttiaho
     *
     */
    private static final class NodeListIterator implements Iterable<Node>, Iterator<Node> {
        private final NodeList nodeList;
        int i = 0;

        private NodeListIterator(NodeList nodeList) {
            this.nodeList = nodeList;
        }

        @Override
        public boolean hasNext() {
            return i < nodeList.getLength();
        }

        @Override
        public Node next() {
            return nodeList.item(i++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Node> iterator() {
            return this;
        }
    }

    private static List<MeasureItem> listMeasures(Node parent) {
        List<MeasureItem> measures = Lists.newArrayList();
        NodeList children = parent.getChildNodes();
        MeasureItem widthMeasure = null;
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if ("measureref".equals(child.getNodeName())) {
                measures.add(new MeasureItem(MeasureItem.Type.REFERENCE, child.getTextContent()));
            } else if ("measure".equals(child.getNodeName())) {
                measures.add(new MeasureItem(MeasureItem.Type.LABEL, child.getTextContent()));
            } else if ("widthref".equals(child.getNodeName())) {
                widthMeasure = new MeasureItem(MeasureItem.Type.REFERENCE, child.getTextContent());
            } else if ("widthmeasure".equals(child.getNodeName())) {
                widthMeasure = new MeasureItem(MeasureItem.Type.LABEL, child.getTextContent());
            }
        }
        if (widthMeasure != null) {
            measures.add(widthMeasure);
        }
        return measures;
    }

}
