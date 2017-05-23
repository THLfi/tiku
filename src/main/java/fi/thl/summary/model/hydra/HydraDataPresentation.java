package fi.thl.summary.model.hydra;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryItem;

public class HydraDataPresentation extends DataPresentation {

    private final HydraSource source;
    private final DataPresentation delegate;
    private final Summary summary;
    private ItemFinder finder;
    private boolean emphasizedNodeCached;
    private List<DimensionNode> emphasizedNode;

    HydraDataPresentation(HydraSource source, Summary summary, Presentation p) {
        this.source = source;
        delegate = (DataPresentation) p;
        this.summary = summary;
        this.finder = new ItemFinder(summary);
    }

    public String getType() {
        return delegate.getType();
    }

    @Override
    public SortMode getSortMode() {
        return delegate.getSortMode();
    }

    @Override
    public int getGroupSize() {
        return delegate.getGroupSize();
    }

    @Override
    public boolean isLast() {
        return delegate.isLast();
    }

    @Override
    public boolean isFirst() {
        return delegate.isFirst();
    }

    /**
     * Constructs the URL to fetch data from the pivot api. Attemps to make sure
     * that both row and column attributes are used in the data query. If not
     * then the pivot API will fail.
     * 
     * @return
     */
    public String getDataUrl() {
        UrlBuilder url = new UrlBuilder();
        url.addRows();
        appendDimensionNodes(url);
        appendMeasureItems(url);
        appendFilters(url);

        url.suppress(delegate.getSuppress());

        if (delegate.getShowConfidenceInterval()) {
            url.showConfidenceInterval();
        }
        if (delegate.isShowSampleSize()) {
            url.showSampleSize();
        }

        // Sorting is disabled for now as a sorted JSON-STAT data set
        // does not make sense if there are multiple more than two
        // rows and columns int total.
        // if(!SortMode.none.equals(delegate.getSortMode())) {
        // url.sort(delegate.getSortMode());
        // }

        return url.toString();
    }

    private void appendFilters(UrlBuilder url) {
        for (Selection s : getFilters()) {
            if ("measure".equals(s.getDimension())) {
                //
            } else {
                // We do not want extra dimension in the returned JSON-STAT
                // resource
                // so we use filters
                url.addFilters();
            }
            List<DimensionNode> nodes;
            List<DimensionNode> selected = ((HydraFilter) s).getSelected();
            switch (s.getSelectionMode()) {
            case directDescendants:
                nodes = Lists.newArrayList();
                for (DimensionNode node : selected) {
                    nodes.add(node);
                    nodes.addAll(node.getChildren());
                }
                break;
            case allDescendants:
                nodes = Lists.newArrayList();
                addRecursive(selected, nodes);
                break;
            default:
                nodes = selected;
                break;
            }
            url.addParameter(s.getDimension(), nodes);

        }
    }

    private void addRecursive(Collection<DimensionNode> selected, List<DimensionNode> nodes) {
        for (DimensionNode node : selected) {
            nodes.add(node);
            addRecursive(node.getChildren(), nodes);
        }
    }

    private void appendMeasureItems(UrlBuilder url) {
        if (delegate.hasMeasures()) {
            url.addParameter("measure",
                    new MeasureExtension(((HydraSummary) summary), source, delegate.getMeasures()).getNodes());
            url.addColumns();
        }
    }

    private void appendDimensionNodes(UrlBuilder url) {
        for (SummaryItem d : getDimensions()) {
            url.addParameter(((Extension) d).getDimension(), ((Extension) d).getNodes());
            url.addColumns();
        }
    }

    public MeasureExtension getMeasuresExtension() {
        return new MeasureExtension((HydraSummary) summary, source, delegate.getMeasures());
    }

    public List<SummaryItem> getDimensions() {
        return extendDimensions();
    }

    private List<SummaryItem> extendDimensions() {
        return Lists.transform(delegate.getDimensions(), new ExtendDimensionFunction(((HydraSummary) summary), source));
    }

    public List<Selection> getFilters() {
        List<Selection> presentationFilters = Lists.newArrayList();
        for (Selection filter : delegate.getFilters()) {
            presentationFilters.add(summary.getSelection(filter.getId()));
        }
        return presentationFilters;
    }

    @Override
    public Integer getMin() {
        return delegate.getMin();
    }

    @Override
    public Integer getMax() {
        return delegate.getMax();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getPalette() {
        return delegate.getPalette();
    }

    @Override
    public boolean getShowConfidenceInterval() {
        return delegate.getShowConfidenceInterval();
    }

    @Override
    public List<String> getEmphasize() {
        return delegate.getEmphasize();
    }

    public List<DimensionNode> getEmphasizedNode() {
        if (emphasizedNodeCached) {
            return emphasizedNode;
        }
        if (null == getEmphasize()) {
            return null;
        }
        emphasizedNode = finder.findItems(getEmphasize(), source);
        emphasizedNodeCached = true;
        return emphasizedNode;

    }
}