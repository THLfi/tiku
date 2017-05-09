package fi.thl.summary.model.hydra;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Section;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.TablePresentation;
import fi.thl.summary.model.TextPresentation;

public class HydraSection extends Section {

    private Section delegate;
    private HydraSource source;
    private HydraSummary summary;
    private List<Selection> selections;
    private Summary summaryDelegate;

    public HydraSection(Section input, HydraSource source, Summary summary, HydraSummary hydraSummary, List<Selection> selections) {
        this.delegate = input;
        this.source = source;
        this.summaryDelegate = summary;
        this.summary = hydraSummary;
        this.selections = selections;
    }

    @Override
    public List<Section> getChildren() {
        return Lists.transform(delegate.getChildren(), new Function<Section, Section>() {

            @Override
            public Section apply(Section input) {
                return new HydraSection(input, source, summaryDelegate, summary, selections);
            }

        });
    }

    @Override
    public List<Presentation> getPresentations() {
        
        return Lists.transform(summaryDelegate.getPresentations(), new Function<Presentation, Presentation>() {

            @Override
            public Presentation apply(final Presentation p) {
                if (p instanceof TextPresentation) {
                    return new HydraTextPresentation((TextPresentation) p, selections);
                } else if (p instanceof TablePresentation) {
                    return new HydraTablePresentation(source, summary, p);
                } else {
                    return new HydraDataPresentation(source, summary, p);
                }
            }
        });
    }

}
