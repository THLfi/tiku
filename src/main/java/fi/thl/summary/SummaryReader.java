package fi.thl.summary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.thl.pivot.model.Label;
import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.MeasureItem;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryStage;
import fi.thl.summary.model.TablePresentation;
import fi.thl.summary.model.TextPresentation;

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

    private static final String DEFAULT_STAGE = ":all:";
    private static final String STAGE_ELEMENT = "stage";
    private static final String DIMENSION_ELEMENT = "dim";
    private static final String LANGUAGE_ELEMENT = "lang";
    private static final String BOOLEAN_YES = "yes";

    private static final String SUPPRESSION_ATTRIBUTE = "suppression";

    private static final List<String> textPresentations = ImmutableList.of("subtitle", "info");
    private static final List<String> dataPresentations = ImmutableList.of("line", "bar", "gauge", "pie");
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

        } catch (Exception e) {
            throw new SummaryException(e.getMessage(), e);
        }
    }

    private void assertOnlyOneDocumentRead() {
        if (isDocumentParsed) {
            throw new IllegalStateException("Attempt to read multiple documents using the same SummaryReader intance");
        }
        isDocumentParsed = true;
    }

    private void parseFilters() {
        for (Node node : iterator(root, "select")) {

            Selection selection = new Selection();
            selection.setId(attribute(node, "id").toLowerCase());
            selection.setVisible(!BOOLEAN_YES.equals(attribute(node, "hidden")));
            selection.setMultiple(BOOLEAN_YES.equals(attribute(node, "multi")));
            selection.setSearchable(BOOLEAN_YES.equals(attribute(node, "search")));
            
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
        List<Presentation> group = Lists.newArrayList();
        for (Node node : iterator(root, "presentation")) {
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

            p.setSuppress(BOOLEAN_YES.equals(attribute(node, SUPPRESSION_ATTRIBUTE)));

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
            if (null != dim) {
                p.addColumn(dim.getTextContent(), getStage(p, col), showTotal(dim));
            } else {
                p.addColumn(listMeasures(col));
            }
        }

        private void addRow(TablePresentation p, Node row) {
            Node dim = firstChildNode(row, DIMENSION_ELEMENT);
            if (null != dim) {
                p.addRow(dim.getTextContent(), getStage(p, row), showTotal(dim));
            } else {
                p.addRow(listMeasures(row));
            }
        }

        private boolean showTotal(Node dim) {
            return BOOLEAN_YES.equals(attribute(dim, "total"));
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

            p.setSuppress(BOOLEAN_YES.equals(attribute(node, SUPPRESSION_ATTRIBUTE)));

            Node typeNode = firstChildNode(node, "type");
            // As it happens, old version was case insensitive so we have to
            // make this case insensitive as well.
            String type = typeNode.getTextContent().toLowerCase();
            type = refineBarTypeAttributes(typeNode, type);
            p.setType(type);
            if (null != attribute(typeNode, ATTRIBUTE_SORT)) {
                p.setSortMode(Presentation.SortMode.valueOf(attribute(typeNode, ATTRIBUTE_SORT).toLowerCase()));
            }
            p.setShowConfidenceInterval(BOOLEAN_YES.equals(attribute(typeNode, "ci")));
            p.setShowSampleSize(BOOLEAN_YES.equals(attribute(typeNode, "n")));
            p.setMin(parseIntAttribute(typeNode, "min"));
            p.setMax(parseIntAttribute(typeNode, "max"));
            p.setPalette(attribute(typeNode, "palette"));

            List<SummaryStage> stages = new ArrayList<>();
            parseDimensions(p, stages, DIMENSION_ELEMENT, "dimMulti");
            List<String> dimensions = new ArrayList<>();
            for(SummaryStage stage : stages) {
                dimensions.add(stage.getDimensionId());
            }

            if (dimensions.size() != stages.size()) {
                return new SentinelPresentationParser("Number of dimensions and stages do not match").parse();
            }

            for (int i = 0; i < dimensions.size(); ++i) {
                p.addDimension(dimensions.get(i), stages.get(i));
            }

            p.addMeasures(listMeasures(node));

            p.setFirst("begin".equals(attribute(node, "group")));
            p.setLast("end".equals(attribute(node, "group")));

            return p;
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
         * @param dimensionsAndStages
         * @param dimensionNodeNames
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
                            break;
                        }
                        break;
                    }
                    if (items.isEmpty()) {
                        boolean hasFilter = hasFilterForDimension(p, dimension);
                        items.add(hasFilter ? ":filter:" : DEFAULT_STAGE);
                    }
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
            Set<String> filters = Sets.newHashSet();
            for (Node n : iterator(node, type)) {
                if (filters.contains(n.getTextContent().toLowerCase())) {
                    throw new IllegalStateException(n.getTextContent() + " defined multiple times");
                }
                filters.add(n.getTextContent().toLowerCase());
                p.addFilter(summary.getSelection(n.getTextContent().toLowerCase()));
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
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if ("measureref".equals(child.getNodeName())) {
                measures.add(new MeasureItem(MeasureItem.Type.REFERENCE, child.getTextContent()));
            } else if ("measure".equals(child.getNodeName())) {
                measures.add(new MeasureItem(MeasureItem.Type.LABEL, child.getTextContent()));
            }
        }
        return measures;
    }

}
