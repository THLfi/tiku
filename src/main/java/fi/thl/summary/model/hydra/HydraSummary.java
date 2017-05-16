package fi.thl.summary.model.hydra;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.pivot.web.tools.FindNodes;
import fi.thl.pivot.web.tools.FindNodes.SearchType;
import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Section;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryDimension;
import fi.thl.summary.model.SummaryItem;
import fi.thl.summary.model.TablePresentation;
import fi.thl.summary.model.TextPresentation;

/**
 * Provides an delegate for {@link Summary} that contains a reference to the
 * {@link HydraSource} that is used to introduce metadata from the hydra
 * metadata and tree definitions
 * 
 * @author aleksiyrttiaho
 *
 */
public class HydraSummary extends Summary {

    final HydraSource source;
    private final Summary summary;
    private List<Selection> selections;
    private Map<String, List<DimensionNode>> drilledDimensions = Maps.newHashMap();
    private Map<String, DimensionLevel> dimensionMaxLevel = Maps.newHashMap();

    public HydraSummary(Summary theSummary, HydraSource theSource) {
        this.source = theSource;
        this.summary = theSummary;

        findMaximumLevelReferenced();

    }

    public String getMaximumLevelId(String dimension) {
        DimensionLevel l = dimensionMaxLevel.get(dimension);
        return l == null || l.getParentLevel() != null ? "root" : l.getParentLevel().getId();
    }

    private void findMaximumLevelReferenced() {
        for (Presentation p : summary.getPresentations()) {
            if (p instanceof DataPresentation) {
                findMaximumLevelInPresentation((DataPresentation) p);
            }
        }
    }

    private void findMaximumLevelInPresentation(DataPresentation dp) {
        for (SummaryItem si : dp.getDimensions()) {
            if (si instanceof SummaryDimension) {
                SummaryDimension sd = (SummaryDimension) si;
                if (!sd.getStage().isLogical()) {
                    Dimension d = source.getDimension(sd.getDimension());
                    if (null == d) {
                        continue;
                    }
                    DimensionLevel l = d.getLevel(sd.getStage().getStage());
                    if (null == l) {
                        continue;
                    }
                    if (dimensionMaxLevel.containsKey(d.getId())) {
                        checkIfLevelIsHigherThanFound(d, l);
                    } else {
                        dimensionMaxLevel.put(d.getId(), l);
                    }

                }
            }
        }
    }

    private void checkIfLevelIsHigherThanFound(Dimension d, DimensionLevel l) {
        DimensionLevel old = dimensionMaxLevel.get(d.getId());
        while (old != null) {
            if (old.getId().equalsIgnoreCase(l.getId())) {
                break;
            }
            old = old.getChildLevel();
        }
        if (old == null) {
            dimensionMaxLevel.put(d.getId(), l);
        }
    }

    public boolean equals(Object obj) {
        return summary.equals(obj);
    }

    public String getId() {
        return summary.getId();
    }

    public String getSpecificationVersion() {
        return summary.getSpecificationVersion();
    }

    public boolean isDrillEnabled() {
        return summary.isDrillEnabled();
    }

    public Collection<String> getSupportedLanguages() {
        return summary.getSupportedLanguages();
    }

    public Label getSubject() {
        return new HydraLabel(summary.getSubject(), selections);
    }

    public Label getTitle() {
        return new HydraLabel(summary.getTitle(), selections);
    }

    public Label getLink() {
        return new HydraLabel(summary.getLink(), selections);
    }

    public Label getNote() {
        return new HydraLabel(summary.getNote(), selections);
    }

    public List<Presentation> getPresentations() {
        return Lists.transform(summary.getPresentations(), new Function<Presentation, Presentation>() {

            @Override
            public Presentation apply(final Presentation p) {
                if (p instanceof TextPresentation) {
                    return new HydraTextPresentation((TextPresentation) p, selections);
                } else if (p instanceof TablePresentation) {
                    return new HydraTablePresentation(source, HydraSummary.this, p);
                } else {
                    return new HydraDataPresentation(source, HydraSummary.this, p);
                }
            }
        });
    }
    
    public List<Section> getSections() {
        return Lists.transform(summary.getSections(), new Function<Section, Section>() {

            @Override
            public Section apply(Section input) {
               return new HydraSection(input, source, summary, HydraSummary.this, selections);
            }
            
        });
    }

    public Collection<Selection> getSelections() {
        if (this.selections != null) {
            return this.selections;
        }
        List<Selection> filter = Lists.newArrayList(summary.getSelections());
        // Note: We need to assign extended filters to a new list
        // or else each filter is created anew every time the list is
        // iterated.
        this.selections = Lists.newArrayList(Util.extendFilters(source, this, filter));
        return this.selections;

    }

    public Selection getSelection(String id) {
        for (Selection s : getSelections()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * This is a helper method used to aid UI labeling
     * 
     * @return
     */
    public Collection<DimensionNode> getNodes() {
        Set<DimensionNode> nodes = Sets.newHashSet();
        for (Presentation p : getPresentations()) {
            if (p instanceof HydraDataPresentation) {
                HydraDataPresentation hp = (HydraDataPresentation) p;
                for (SummaryItem s : hp.getDimensions()) {
                    nodes.addAll(((Extension) s).getNodes());
                }
                nodes.addAll(hp.getMeasuresExtension().getNodes());
            }
            if (p instanceof HydraTablePresentation) {
                HydraTablePresentation hp = (HydraTablePresentation) p;
                addNodes(nodes, hp.getRows());
                addNodes(nodes, hp.getColumns());
            }
        }
        return nodes;
    }

    private void addNodes(Set<DimensionNode> nodes, final List<SummaryItem> level) {
        for (SummaryItem s : level) {
            nodes.addAll(((Extension) s).getNodes());
        }
    }

    public String getFactTable() {
        return summary.getFactTable();
    }

    public int hashCode() {
        return summary.hashCode();
    }

    public String toString() {
        return summary.toString();
    }

    public String getItemLanguage() {
        return summary.getItemLanguage();
    }

    @Override
    public Scheme getScheme() {
        return summary.getScheme();
    }

    public boolean isDrilled(String dimension) {
        return isDrillEnabled() && drilledDimensions != null && drilledDimensions.containsKey(dimension);
    }

    public DimensionNode drillTo(String dimension, String identifier) {
        if (null == this.drilledDimensions) {
            this.drilledDimensions = Maps.newHashMap();
        }
        List<DimensionNode> drillNode = new FindNodes(source, SearchType.SURROGATE).apply(identifier);
        this.drilledDimensions.put(dimension, drillNode);
        return drillNode.isEmpty() ? null : drillNode.get(0);
    }

    public List<DimensionNode> getDrilledNodes(String dimension) {
        return drilledDimensions.get(dimension);
    }

    public Collection<String> getDrilledDimensions() {
        return drilledDimensions.keySet();
    }

    public List<DimensionNode> getSelectionByDimension(String dimension) {
        for (Selection s : getSelections()) {
            if (s.getDimension().equals(dimension)) {
                return ((HydraFilter) s).getSelected();
            }
        }
        return null;
    }

}
