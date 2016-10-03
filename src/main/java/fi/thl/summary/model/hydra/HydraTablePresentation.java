package fi.thl.summary.model.hydra;

import java.util.List;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryItem;
import fi.thl.summary.model.TablePresentation;

public class HydraTablePresentation extends TablePresentation {

    private HydraDataPresentation dataPresentation;
    private TablePresentation delegate;
    private HydraSource source;
    private Summary summary;

    HydraTablePresentation(HydraSource source, Summary summary, Presentation p) {
        this.dataPresentation = new HydraDataPresentation(source, summary, p);
        this.delegate = (TablePresentation) p;
        this.source = source;
        this.summary = summary;
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

    @Override
    public List<SummaryItem> getDimensions() {
        return dataPresentation.getDimensions();
    }

    @Override
    public List<Selection> getFilters() {
        return dataPresentation.getFilters();
    }

    @Override
    public List<SummaryItem> getColumns() {
        return Lists.transform(delegate.getColumns(), new ExtendDimensionFunction(((HydraSummary) summary), source));
    }

    @Override
    public List<SummaryItem> getRows() {
        return Lists.transform(delegate.getRows(), new ExtendDimensionFunction(((HydraSummary) summary), source));
    }

    @Override
    public String getType() {
        return dataPresentation.getType();
    }

    public String getDataUrl() {

        UrlBuilder url = new UrlBuilder();
        url.addRows();
        // Select row parameters
        for (SummaryItem d : getRows()) {
            url.addParameter(((Extension) d).getDimension(), nodesOf(d));
        }

        // Select column parameters
        url.addColumns();
        for (SummaryItem d : getColumns()) {
            url.addParameter(((Extension) d).getDimension(), nodesOf(d));
        }

        // Select filter parameters
        for (Selection s : getFilters()) {
            if ("measure".equals(s.getDimension())) {
                url.addColumns();
            } else {
                // We do not want extra dimension in the returned JSON-STAT
                // resource
                // so we use filters
                url.addFilters();
            }
            HydraFilter f = ((HydraFilter) s);
            url.addParameter(s.getDimension(), Lists.newArrayList(f.getSelected()));

        }

        if (delegate.getSuppress()) {
            url.suppress();
        }
        return url.toString();
    }

    private List<DimensionNode> nodesOf(SummaryItem d) {
        return ((Extension) d).getNodes();
    }

}
