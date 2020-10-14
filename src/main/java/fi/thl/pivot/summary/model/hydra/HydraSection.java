package fi.thl.pivot.summary.model.hydra;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.summary.model.Presentation;
import fi.thl.pivot.summary.model.Section;
import fi.thl.pivot.summary.model.Selection;
import fi.thl.pivot.summary.model.Summary;
import fi.thl.pivot.summary.model.TablePresentation;
import fi.thl.pivot.summary.model.TextPresentation;

public class HydraSection extends Section {

    private Section delegate;
    private HydraSource source;
    private HydraSummary summary;
    private Summary summaryDelegate;

    public HydraSection(Section input, HydraSource source, Summary summary, HydraSummary hydraSummary) {
        this.delegate = input;
        this.source = source;
        this.summaryDelegate = summary;
        this.summary = hydraSummary;
    }

    @Override
    public List<Section> getChildren() {
        return Lists.transform(delegate.getChildren(), new Function<Section, Section>() {

            @Override
            public Section apply(Section input) {
                return new HydraSection(input, source, summaryDelegate, summary);
            }

        });
    }

    @Override
    public List<Presentation> getPresentations() {
        
        return Lists.transform(delegate.getPresentations(), new Function<Presentation, Presentation>() {

            @Override
            public Presentation apply(final Presentation p) {
                if (p instanceof TextPresentation) {
                    return new HydraTextPresentation((TextPresentation) p, HydraSection.this.summary);
                } else if (p instanceof TablePresentation) {
                    return new HydraTablePresentation(source, summary, p);
                } else {
                    return new HydraDataPresentation(source, summary, p);
                }
            }
        });
    }
    
    @Override
    public String getWidth() {
        return delegate.getWidth();
    }

}
