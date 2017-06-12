package fi.thl.summary.model.hydra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import fi.thl.pivot.datasource.HydraSource;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.summary.model.Selection;
import fi.thl.summary.model.Summary;

/**
 * 
 * Implements selection based on hydra source. Makes selectable elements
 * concrete by providing list of options (dimension node) instead of summary
 * definitions
 * 
 * @author aleksiyrttiaho
 *
 */
public final class HydraFilter extends Selection {

    private final Selection delegate;
    private final Summary summary;
    private HydraSource source;
    private ArrayList<FilterStage> filterStages;
    private ItemFinder finder;

    HydraFilter(HydraSource source, Summary summary, Selection s) {
        this.source = source;
        this.delegate = s;
        this.summary = summary;
        this.finder = new ItemFinder(summary);
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public boolean getVisible() {
        return delegate.getVisible();
    }

    public Dimension getDimensionEntity() {
        for (Dimension d : source.getDimensionsAndMeasures()) {
            if (d.getId().equals(getDimension())) {
                return d;
            }
        }
        return null;
    }

    /**
     * Gets the selected element or default
     * 
     * @return
     */
    public List<DimensionNode> getSelected() {
        getFilterStages();
        List<DimensionNode> selected = filterStages.get(filterStages.size() - 1).getSelected();
        return selected;
    }
    
    public List<DimensionNode> getSelected(String stage) {
        for(FilterStage s : getFilterStages()) {
            if(stage.equals(s.getId())) {
                return s.getSelected();
            }
        }
        return Collections.emptyList();
    }

    public String getId() {
        return delegate.getId();
    }

    public String getDimension() {
        return delegate.getDimension();
    }

    public List<String> getItems() {
        return delegate.getItems();
    }

    @Override
    public List<String> getSets() {
        return delegate.getSets();
    }

    public List<String> getStages() {
        return delegate.getStages();
    }

    public Collection<String> getDefaultItem() {
        return delegate.getDefaultItem();
    }

    public void select(int stageIndex, String[] ids) {
        getFilterStages();
        if (stageIndex < 0 || stageIndex >= filterStages.size()) {
            throw new IllegalStateException("Invalid filter stage selected");
        }
        FilterStage stage = filterStages.get(stageIndex);
        if (null != ids) {
            for (String id : ids) {
                stage.select(id);
            }
        } else {
            stage.select(null);
        }
    }

    public List<FilterStage> getFilterStages() {
        if (this.filterStages == null) {
            this.filterStages = new ArrayList<>();
            for (Dimension dim : source.getDimensionsAndMeasures()) {
                if (dim.getId().equalsIgnoreCase(getDimension())) {
                    if (!getItems().isEmpty() || !getSets().isEmpty()) {
                        List<DimensionNode> options = new ArrayList<>();

                        FilterStage stage = new FilterStage(dim.getLabel(), options);
                        stage.setScheme(summary.getScheme());

                        createStageContainingAllNodesInSets(dim, stage, options);
                        createStageContainingEnumeratedItems(dim, stage, options);

                        filterStages.add(stage);
                    } else if (!getStages().isEmpty()) {
                        createMultiStageFilter(dim);
                    } else {
                        createStageContainingAllNodes(dim);
                    }
                }
            }
            for (FilterStage fs : filterStages) {
                fs.setDefaultItem(getDefaultItem());
                fs.setItemLanguage(summary.getItemLanguage());
                fs.setScheme(summary.getScheme());
            }
        }
        return filterStages;
    }

    private void createMultiStageFilter(Dimension dim) {
        List<String> stages = getStages();
        DimensionLevel level = dim.getRootLevel();
        FilterStage parent = null;
        int stageIndex = 0;
        while (level != null) {
            if (stages.size() > stageIndex && stages.get(stageIndex).equals(level.getId())) {
                Label label = delegate.getLabelMode().equals(LabelMode.dimension) && stages.size() == 1 ? dim.getLabel() : level.getLabel();
                FilterStage stage = new FilterStage(label, level.getNodes());
                stage.setId(level.getId());
                stage.setScheme(summary.getScheme());
                stage.setParent(parent);
                filterStages.add(stage);
                parent = stage;
                ++stageIndex;
            }
            level = level.getChildLevel();
        }
    }

    private void createStageContainingEnumeratedItems(Dimension dim, FilterStage stage, List<DimensionNode> options) {
        if (getItems().isEmpty()) {
            return;
        }
        options.addAll(finder.findItems(getItems(), dim));
    }

    private void createStageContainingAllNodesInSets(Dimension dim, FilterStage stage, List<DimensionNode> options) {
        if (getSets().isEmpty()) {
            return;
        }
        List<DimensionNode> sets = finder.findItems(getSets(), dim);
        for (DimensionNode set : sets) {
            options.addAll(set.getChildren());
        }
    }

    private void createStageContainingAllNodes(Dimension dim) {
        List<DimensionNode> options = Lists.newArrayList();
        List<DimensionNode> candidates = Lists.newArrayList(dim.getRootNode());
        populateDepthFirst(options, candidates);
        FilterStage stage = new FilterStage(dim.getLabel(), options);
        stage.setScheme(summary.getScheme());
        filterStages.add(stage);
    }

    private void populateDepthFirst(List<DimensionNode> options, Collection<DimensionNode> candidates) {
        for (DimensionNode candidate : candidates) {
            options.add(candidate);
            populateDepthFirst(options, candidate.getChildren());
        }
    }

    @Override
    public String toString() {
        return "HydraFilter [getId()=" + getId() + ", getDimension()=" + getDimension() + ", instance = " + hashCode()+"]";
    }

    @Override
    public boolean getSearchable() {
        return delegate.getSearchable();
    }

    @Override
    public boolean getMultiple() {
        return delegate.getMultiple();
    }

    @Override
    public boolean getIsCompleteDimension() {
        return delegate.getIsCompleteDimension();
    }
    
    @Override
    public SelectionMode getSelectionMode() {
        return delegate.getSelectionMode();
    }
    
    @Override
    public LabelMode getLabelMode() {
        return delegate.getLabelMode();
    }

}