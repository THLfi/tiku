package fi.thl.summary.model;

import java.util.List;

import com.google.common.collect.Lists;

public class Value {

    private String id;
    private List<Selection> filters = Lists.newArrayList();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Selection> getFilters() {
        return filters;
    }

    public void addFilter(Selection filter) {
        this.filters.add(filter);
    }
}
