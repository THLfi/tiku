package fi.thl.pivot.summary.model.hydra;

import java.util.List;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.IDimensionNode;
import fi.thl.pivot.summary.model.Presentation;
import fi.thl.pivot.summary.model.Selection;
import fi.thl.pivot.summary.model.Summary;
import fi.thl.pivot.summary.model.SummaryItem;
import fi.thl.pivot.summary.model.TablePresentation;

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

    public boolean isValid() {
        try {
            String url = getDataUrl();
            return url.contains("row=") && url.contains("column=");
        } catch (Exception e) {
            return false;
        }
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

        boolean hasMeasureFilter = false;
        boolean hasMeasureDimension = false;
        for (SummaryItem d : getRows()) {
            if (((Extension) d).getDimension().equals("measure")) {
                hasMeasureDimension = true;
                break;
            }
        }
        for (SummaryItem d : getColumns()) {
            if (hasMeasureDimension || ((Extension) d).getDimension().equals("measure")) {
                hasMeasureDimension = true;
                break;
            }
        }
        for (Selection s : getFilters()) {
            if ("measure".equals(s.getDimension())) {
                hasMeasureFilter = true;
                break;
            }
        }

        UrlBuilder url = new UrlBuilder();
        url.addRows();
        // Select row parameters
        for (SummaryItem d : getRows()) {
            String dimension = ((Extension) d).getDimension();
            if (hasMeasureFilter && "measure".equals(dimension)) {
                url.addParameter(dimension, nodesOf(d));
            } else {
                url.addParameter(dimension, nodesOf(d));
            }
        }

        // Select column parameters
        url.addColumns();
        for (SummaryItem d : getColumns()) {
            String dimension = ((Extension) d).getDimension();
            if (hasMeasureFilter && "measure".equals(dimension)) {
                url.addParameter(dimension, nodesOf(d));
            } else {
                url.addParameter(dimension, nodesOf(d));
            }
        }

        // Select filter parameters
        // NOTE: order of parameters must equal the order in CubeRequest.toDataUrl or else
        // cubes with restricted access via summaries do not work .
        for (Selection s : getFilters()) {
            if ("measure".equals(s.getDimension())) {
                if(hasMeasureDimension) {
                    url.addFilters();
                } else {
                    url.addColumns();
                }
                url.addParameter(s.getDimension(), IncludeDescendants.apply(s));
            }
        }
        for (Selection s : getFilters()) {
            if (!"measure".equals(s.getDimension())) {
                // We do not want extra dimension in the returned JSON-STAT
                // resource
                // so we use filters
                url.addFilters();
                url.addParameter(s.getDimension(), IncludeDescendants.apply(s));
            }
        }

        url.suppress(delegate.getSuppress());

        return url.toString();
    }

  
    private List<IDimensionNode> nodesOf(SummaryItem d) {
        return ((Extension) d).getNodes();
    }

    @Override
    public SuppressMode getSuppress() {
        return delegate.getSuppress();
    }
    
    @Override
    public HighlightMode getHighlight() {
        return delegate.getHighlight();
    }

    public boolean isVisible() {
        return new HydraRule(delegate.getRule(), ((HydraSummary) summary)).eval();
    }
}
