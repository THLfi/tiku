package fi.thl.pivot.web.tools;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import fi.thl.pivot.model.IDimensionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.util.Constants;
import fi.thl.pivot.util.IntegerListPacker;

/**
 * Converts a identifier set to corresponding set of dimension nodes. The
 * identifier is structured as dimension, dash and dot separated list of
 * identifiers
 * 
 * &lt;dimension&gt;-&lt;id<sub>1</sub>&gt;.&lt;id<sub>2</sub>&gt;.&lt;...&gt;
 * .&lt;id<sub>n</sub>&gt;
 * 
 * If {@link SearchType.SURROGATE} is used then the identifiers should be non
 * negative integers. if {@link SearchType.IDENTIFIER} is used then the actual
 * identifiers in the cube are used. Please note that cube identifiers may
 * contain characters that will cause the program to malfunction.
 * 
 * @author aleksiyrttiaho
 *
 */
public final class FindNodes implements Function<String, List<IDimensionNode>> {

    public static enum SearchType {
        SURROGATE, IDENTIFIER, URI
    }

    private final Logger logger = LoggerFactory.getLogger(FindNodes.class);

    private final HydraSource source;
    private final SearchType searchType;

    public FindNodes(HydraSource source, SearchType searchType) {
        this.source = source;
        this.searchType = searchType;
    }

    public List<IDimensionNode> apply(String identifier) {
        List<IDimensionNode> nodes = Lists.newArrayList();

        if (SearchType.SURROGATE.equals(searchType) && isLevelIdentifier(identifier)) {
            useAllNodesInLevel(identifier, nodes);
        } else {
            convertIdentifiersToNodes(identifier, nodes);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(identifier + " => " + nodes);
        }
        return nodes.isEmpty() ? null : nodes;
    }

    private void useAllNodesInLevel(String identifier, List<IDimensionNode> nodes) {
        String nodeId = identifier.substring(dimensionIdentifierIndex(identifier) + 1, identifier.length() - 1);
        IDimensionNode node = findNodeUsingSelectedSearchType(nodeId);
        if (null != node && node.canAccess()) {
            nodes.addAll(node.getLevel().getNodes().stream().filter(x -> !x.isHidden() || !x.isMeasure()).collect(Collectors.toList()));
        }
        Collections.sort(nodes);
    }

    private boolean isLevelIdentifier(String identifier) {
        return identifier.endsWith(Constants.LEVEL_IDENTIFIER);
    }

    private void convertIdentifiersToNodes(String identifier, List<IDimensionNode> nodes) {
        for (String id : asId(identifier)) {
            IDimensionNode node = findNodeUsingSelectedSearchType(id);
            if (node == null) {
                logger.warn(String.format("Attempt to load %s with id %s FAILED", identifier, id));
            } else if (!node.canAccess()) {
                logger.warn(String.format("Attempt to load %s with id %s DENIED as user cannot access it", identifier, id));
            } else {
                nodes.add(node);
            }
        }
    }

    private IDimensionNode findNodeUsingSelectedSearchType(String id) {
        IDimensionNode node;

        if (SearchType.SURROGATE.equals(this.searchType)) {
            node = source.resolve(id);
        } else if (SearchType.IDENTIFIER.equals(this.searchType)) {
            node = source.getNode(id);
        } else {
            throw new UnsupportedOperationException("URI id is not yet supported");
        }
        return node;
    }

    private String[] asId(String identifier) {

        int idx = dimensionIdentifierIndex(identifier);
        int dotIndex = identifier.indexOf(".");
        String ids;

        // Check if url params have been packed or not. If only one dimension node is set as parameter the packed length is 12(original 6).
        if (dotIndex == identifier.length() - 1 && (dotIndex - idx) > 7) {
            String str = identifier.substring(idx + 1, identifier.length() - 1);
            List<Integer> unpacked = IntegerListPacker.unzipAndUnpack(str);
            ids = unpacked.stream().map(String::valueOf).collect(Collectors.joining("."));
        } else {
            // extract node identifiers
            ids = identifier.substring(idx + 1);
        }

        // If identifier contains subset then split node identifiers
        // from identifier
        if (ids.contains(Constants.DEPRECATED_SUBSET_SEPARATOR)) {
            return ids.split(Constants.DEPRECATED_SUBSET_SEPARATOR);
        }
        // We need to add the escapre as - is reserved charater in regex
        return ids.split("\\" + Constants.SUBSET_SEPARATOR);
    }

    private int dimensionIdentifierIndex(String identifier) {
        // check split point for dimenion-ids
        int idx = identifier.indexOf(Constants.DIMENSION_SEPARATOR);
        if (idx < 0) {
            // For backwards compatibility
            idx = identifier.indexOf(Constants.DEPRECATED_DIMENSION_SEPARATOR);
        }
        return idx;
    }
}