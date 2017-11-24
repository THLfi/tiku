package fi.thl.pivot.web;

import java.util.*;

import fi.thl.pivot.exception.SameDimensionAsRowAndColumnException;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.ListDocumentImpl;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.Dataset;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.FilterablePivot;
import fi.thl.pivot.model.ModifiablePivot;
import fi.thl.pivot.model.OrderablePivot;
import fi.thl.pivot.model.Pivot;
import fi.thl.pivot.model.PivotCell;
import fi.thl.pivot.model.PivotLevel;
import fi.thl.pivot.model.Query;
import fi.thl.pivot.util.Constants;
import fi.thl.pivot.web.tools.FilterBuilder;
import fi.thl.pivot.web.tools.FilterEmpty;
import fi.thl.pivot.web.tools.FilterZero;
import fi.thl.pivot.web.tools.FilterZeroOrEmpty;
import fi.thl.pivot.web.tools.FindNodes;
import fi.thl.pivot.web.tools.FindNodes.SearchType;
import fi.thl.pivot.web.tools.SpanCounter;
import fi.thl.pivot.web.tools.TableHelper;

public class CubeService {

    private final class FirstElementInList implements Function<List<DimensionNode>, DimensionNode> {
        @Override
        public DimensionNode apply(List<DimensionNode> input) {
            return input.get(0);
        }
    }

    private static final Logger LOG = Logger.getLogger(CubeService.class);

    private List<String> rowHeaders;
    private List<String> columnHeaders;
    private List<String> filterValues;
    private List<String> measureValues;
    private boolean isZeroValuesFiltered;
    private boolean isEmptyValuesFiltered;

    private FindNodes.SearchType searchType;

    private boolean isSortedByColumns;
    private boolean isAscendingOrder;
    private int sortIndex = -1;

    private Locale locale;

    private HydraSource source;

    private List<DimensionNode> filterNodes;

    private Pivot pivot;

    private boolean cubeCreated;

    private List<List<DimensionNode>> rowNodes;
    private List<List<DimensionNode>> columnNodes;
    private List<Boolean> rowNodesModified = new ArrayList<>();
    private List<Boolean> columnNodesModified = new ArrayList<>();

    private boolean isMultipleMeasuresShown;

    private Query query;

    private boolean defaultMeasureUsed;

    private DimensionNode defaultMeasure;

    private CubeRequest request;

    public CubeService(HydraSource source, CubeRequest cr) {
        this.source = source;
        this.request = cr;
    }

    public void createCube() {
        StopWatch sw = new StopWatch();

        this.query = new Query();

        sw.start("Construct cube definition");

        assignDefaultRowsAndColumns();
        final List<List<DimensionNode>> headerNodes = determineHeaderNodes();
        checkIfSameDimensionUsedBothInRowsAndColumns();

        this.filterNodes = Lists.newArrayList(determineFilterNodes());
        if (!isMeasureDefined(headerNodes)) {
            assignDefaultMeasureIfNonSpecified(headerNodes);
        }
        final List<DimensionNode> filter = new FilterBuilder(source, headerNodes, filterNodes).asFilter();
        sw.stop();

        sw.start("Load data");
        Dataset dataSet = source.loadSubset(query, filter, null != request.getCi() || null != request.getN());
        sw.stop();

        LOG.debug("data loaded");
     
        sw.start("Create pivot");
        ModifiablePivot mPivot = new ModifiablePivot(dataSet);
        if (this.defaultMeasureUsed) {
            mPivot.setDefaultMeasure(defaultMeasure);
        }
        mPivot.setFilterNodes(filterNodes);
        determineDimensions(mPivot, filter);
        sw.stop();

        
        sw.start("apply filters");
        FilterablePivot fPivot = new FilterablePivot(mPivot);
    
        applyFilters(fPivot);
        
        this.pivot = fPivot;
        sw.stop();

        if (sortIndex >= 0) {
            sw.start("sort");
            OrderablePivot oPivot = new OrderablePivot(pivot);
            oPivot.sortBy(sortIndex, isSortedByColumns ? OrderablePivot.SortBy.Column : OrderablePivot.SortBy.Row,
                    isAscendingOrder ? OrderablePivot.SortMode.Ascending : OrderablePivot.SortMode.Descending);
            pivot = oPivot;
            sw.stop();
        }

        LOG.debug(String.format("Cube pivot created (%d, %s)", pivot.getRowCount(), pivot.getColumnCount()));
        LOG.debug(sw.prettyPrint());
        this.cubeCreated = true;
    }

    private void checkIfSameDimensionUsedBothInRowsAndColumns() {
        final Set<String> closed = new HashSet<>();
        addRowDimensions(closed);
        failIfRowDimensionInColumns(closed);
    }

    private void failIfRowDimensionInColumns(Set<String> closed) {
        for(List<DimensionNode> list : columnNodes) {
            for(DimensionNode node : list) {
                if(closed.contains(node.getDimension().getId())) {
                    throw new SameDimensionAsRowAndColumnException();
                }
                break; // First node is only needed
            }
        }
    }

    private void addRowDimensions(Set<String> closed) {
        for(List<DimensionNode> list : rowNodes) {
            for (DimensionNode node : list) {
                closed.add(node.getDimension().getId());
                break; // First node is only needed
            }
        }
    }

    public boolean isCubeCreated() {
        return cubeCreated;
    }

    public List<List<DimensionNode>> getRowNodes() {
        return rowNodes;
    }

    public List<List<DimensionNode>> getColumnNodes() {
        return columnNodes;
    }

    public List<List<DimensionNode>> getFilterNodes() {
        List<List<DimensionNode>> nodes = Lists.newArrayList();
        nodes.add(filterNodes);
        return nodes;
    }

    public boolean isMultipleMeasuresShown() {
        return isMultipleMeasuresShown;
    }

    public List<String> getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(List<String> rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    public List<String> getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(List<String> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public List<String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(List<String> filterValues) {
        this.filterValues = filterValues;
    }

    public List<String> getMeasureValues() {
        return measureValues;
    }

    public void setMeasureValues(List<String> measureValues) {
        this.measureValues = measureValues;
    }

    public boolean isZeroValuesFiltered() {
        return isZeroValuesFiltered;
    }

    public void setZeroValuesFiltered(boolean isZeroValuesFiltered) {
        this.isZeroValuesFiltered = isZeroValuesFiltered;
    }

    public boolean isEmptyValuesFiltered() {
        return isEmptyValuesFiltered;
    }

    public void setEmptyValuesFiltered(boolean isEmptyValuesFiltered) {
        this.isEmptyValuesFiltered = isEmptyValuesFiltered;
    }

    public boolean isSortedByColumns() {
        return isSortedByColumns;
    }

    public void setSortedByColumns(boolean isSortedByColumns) {
        this.isSortedByColumns = isSortedByColumns;
    }

    public boolean isAscendingOrder() {
        return isAscendingOrder;
    }

    public void setAscendingOrder(boolean isAscendingOrder) {
        this.isAscendingOrder = isAscendingOrder;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setSearchType(FindNodes.SearchType searchType) {
        this.searchType = searchType;
    }

    public void assignModelAttributes(Model model, CubeRequest cubeRequest) {
        model.addAttribute("cubeLabel", source.getName());
        model.addAttribute("filters", filterNodes);
        model.addAttribute("measures", asNodes(source, measureValues, null));
        model.addAttribute("colHeaders", columnHeaders);
        model.addAttribute("rowHeaders", rowHeaders);
        model.addAttribute("pivot", pivot);
        model.addAttribute("tableHelper", new TableHelper(pivot));
        model.addAttribute("rowspan", new SpanCounter());
        model.addAttribute("dimensions", source.getDimensionsAndMeasures());
        model.addAttribute("isDefaultMeasureUsed", this.defaultMeasureUsed);
        model.addAttribute("query", query);
        model.addAttribute("multipleMeasuresShown", isMultipleMeasuresShown());
        model.addAttribute("sortByColumn", sortIndex >= 0 && isSortedByColumns);
        model.addAttribute("sortByRow", sortIndex >= 0 && !isSortedByColumns);
        model.addAttribute("sortIndex", sortIndex);

        model.addAttribute("supportedLanguages", source.getLanguages());
        model.addAttribute("cubeRequest", cubeRequest);
        model.addAttribute("lang", cubeRequest.getLocale().getLanguage());
        model.addAttribute("showCi", null != cubeRequest.getCi());
        model.addAttribute("showSampleSize", null != cubeRequest.getN());

    }

    public HydraSource getSource() {
        return source;
    }

    public Pivot getPivot() {
        return pivot;
    }

    private List<List<DimensionNode>> determineHeaderNodes() {
        this.rowNodes = selectRows();
        this.columnNodes = selectColumns();
        final List<List<DimensionNode>> headerNodes = Lists.newArrayList(rowNodes);
        headerNodes.addAll(columnNodes);
        return headerNodes;
    }

    private List<DimensionNode> determineFilterNodes() {
        List<DimensionNode> newFilterNodes = new ArrayList<>();
        for(List<DimensionNode> nodes : asNodes(source, filterValues, null)) {
            newFilterNodes.addAll(nodes);
        }
        newFilterNodes.addAll(Lists.transform(asNodes(source, measureValues, null), new FirstElementInList()));
        return newFilterNodes;
    }

    private void assignDefaultMeasureIfNonSpecified(final List<List<DimensionNode>> headerNodes) {
        this.defaultMeasureUsed = true;
        for (Dimension d : source.getMeasures()) {
            DimensionNode dn = d.getRootNode();
            while (!dn.getChildren().isEmpty()) {
                dn = dn.getFirstChild();
            }
            this.defaultMeasure = dn;
            this.filterNodes.add(dn);
            break;
        }
    }

    private void assignDefaultRowsAndColumns() {
        if (rowHeaders.isEmpty() && columnHeaders.isEmpty()) {
            for (Dimension d : source.getDimensions()) {
                rowHeaders.add(d.getId() + Constants.DIMENSION_SEPARATOR + d.getRootNode().getSurrogateId());
                break;
            }
            int i = 0;
            for (Dimension d : source.getDimensions()) {
                if (i++ == 1) {
                    columnHeaders.add(d.getId() + Constants.DIMENSION_SEPARATOR + d.getRootNode().getSurrogateId());
                    break;
                }
            }
        }
    }

    private boolean isMeasureDefined(List<List<DimensionNode>> headerNodes) {
        for (DimensionNode dn : filterNodes) {
            if (dn.getDimension().isMeasure()) {
                return true;
            }
        }
        for (List<DimensionNode> dns : headerNodes) {
            for (DimensionNode dn : dns) {
                if (dn.getDimension().isMeasure()) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void determineDimensions(ModifiablePivot mPivot, final List<DimensionNode> filter) {
        int i = 0;
        for (PivotLevel column : query.getColumns()) {
            setLevelProperties(i, column, columnHeaders, columnNodesModified);
            mPivot.appendColumn(column);
            i++;
        }
        i = 0;
        for (PivotLevel row : query.getRows()) {
            setLevelProperties(i, row, rowHeaders, rowNodesModified);
            mPivot.appendRow(row);
            i++;
        }

        for (DimensionNode constant : filter) {
            mPivot.appendConstant(constant);
        }
    }

    private void setLevelProperties(int i, PivotLevel level, List<String> headers, List<Boolean> modified) {
        if(null != modified && SearchType.SURROGATE.equals(searchType)) {
            level.setIncludesTotal(modified.get(i));
        }
        if (SearchType.IDENTIFIER.equals(searchType) || headers.get(i).contains(".") || headers.get(i).contains(";")) {
            level.setSelectedNode(level.getLastNode());
        } else {
            for (DimensionNode node : level.getNodes()) {
                if (String.valueOf(node.getSurrogateId()).equals(headers.get(i).replace("L", "").replace(node.getDimension().getId() + "-", ""))) {
                    level.setSelectedNode(node);
                }
            }
        }
        if (level.getSelectedNode() == null) {
            throw new IllegalStateException(headers.get(i).replace("L", "") + " not found");
        }
    }

    private void applyFilters(FilterablePivot fPivot) {
        LOG.debug(String.format("Constructed pivot (%d, %s)", fPivot.getRowCount(), fPivot.getColumnCount()));

        List<Predicate<PivotCell>> filters = new ArrayList<>();
        // filters.add(new FilterImpossibleHierarchy(fPivot));
        // LOG.debug(String.format("Filtered pivot ImpossibleHierarchy (%d,
        // %s)", fPivot.getRowCount(), fPivot.getColumnCount()));

        if (isEmptyValuesFiltered && isZeroValuesFiltered) {
            filters.add(new FilterZeroOrEmpty());
            LOG.debug(String.format("Filtered pivot zero or empty (%d, %s)", fPivot.getRowCount(), fPivot.getColumnCount()));
        } else if (isZeroValuesFiltered) {
            filters.add(new FilterZero());
            LOG.debug(String.format("Filtered pivot zero (%d, %s)", fPivot.getRowCount(), fPivot.getColumnCount()));
        } else if (isEmptyValuesFiltered) {
            filters.add(new FilterEmpty());
            LOG.debug(String.format("Filtered pivot empty (%d, %s)", fPivot.getRowCount(), fPivot.getColumnCount()));
        }

        fPivot.applyFilters(filters);

    }

    private List<List<DimensionNode>> selectRows() {
        final List<List<DimensionNode>> newRowNodes = asNodes(source, rowHeaders, rowNodesModified);
        query.addRowNode(newRowNodes, selectSubsets(rowHeaders));
        checkIfMultipleMeasuresAreShown(newRowNodes);
        return newRowNodes;
    }

    private List<List<DimensionNode>> selectColumns() {
        final List<List<DimensionNode>> newColumnNodes = asNodes(source, columnHeaders, columnNodesModified);
        query.addColumnNode(newColumnNodes, selectSubsets(columnHeaders));
        checkIfMultipleMeasuresAreShown(newColumnNodes);
        return newColumnNodes;
    }

    /**
     * Check if a node list contains measure nodes. If so then there may be two
     * or more measures selected in the cube in which case we may want to
     * present the user a different variation of the user interace
     * 
     * @param rowNodes
     */
    private void checkIfMultipleMeasuresAreShown(final List<List<DimensionNode>> rowNodes) {
        for (List<DimensionNode> dns : rowNodes) {
            isMultipleMeasuresShown = isMultipleMeasuresShown || (!dns.isEmpty() && dns.get(0).getDimension().isMeasure());
        }
    }

    /**
     * Specific row or column contains subsets if there exists at least one
     * subset separator If a subset separator is present then node is expanded
     * with its children. Instead only the node itself is shown.
     * 
     * @param headers
     * @return
     */
    private List<Boolean> selectSubsets(List<String> headers) {
        List<Boolean> result = Lists.newArrayList();
        for (String header : headers) {
            result.add(header.contains(Constants.DEPRECATED_SUBSET_SEPARATOR) || header.contains(Constants.SUBSET_SEPARATOR));
        }
        return result;
    }

    /**
     * Maps list of identifiers or identifier lists to a list of lists of
     * dimension nodes where each list of lists represents a row or column
     * 
     * @param source
     * @param identifiers
     * @return
     */
    private List<List<DimensionNode>> asNodes(HydraSource source, List<String> identifiers, List<Boolean> modified) {
        List<List<DimensionNode>> nodes = Lists.newArrayList();
        nodes.addAll(Collections2.filter(Lists.transform(identifiers, new FindNodes(source, searchType)), new Predicate<List<DimensionNode>>() {
            @Override
            public boolean apply(List<DimensionNode> input) {
                return input != null;
            }
        }));

        if (null != modified && SearchType.SURROGATE.equals(searchType)) {
            addTotalNodeToLevel(identifiers, modified, nodes);
        }

        return nodes;
    }

    private void addTotalNodeToLevel(List<String> identifiers, List<Boolean> modified, List<List<DimensionNode>> nodes) {
        // Find out if the same dimension appears more than once
        // If identifier with this kind of dimensions appears with the suffix
        // L (Show all nodes in level)
        // Add the sum node to the end

        for (int i = 0; i < identifiers.size(); ++i) {
            modified.add(false);
            String identifier = identifiers.get(i);
            if (identifier.endsWith("L") && !nodes.get(i).isEmpty()) {
                Dimension dim = nodes.get(i).get(0).getDimension();
                for (int j = i - 1; j >= 0; --j) {
                    if (!nodes.get(j).isEmpty() && dim.equals(nodes.get(j).get(0).getDimension())) {
                        nodes.get(i).add(nodes.get(j).get(nodes.get(j).size() - 1));
                        modified.set(i, true);
                        break;
                    }
                }
            }
        }
    }

    public boolean isCubeAccessDenied() {
        return source.isCubeAccessDenied();
    }

}
