package fi.thl.pivot.datasource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import fi.thl.pivot.model.Dataset;
import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Query;
import fi.thl.pivot.model.Tuple;

public class CSVSource extends HydraSource {

    private static final String FIELD_DIMENSION = "dim";
    private static final String FIELD_DIMENSION_LEVEL = "stage";
    private static final String FIELD_NODE_ID = "key";
    private static final String FIELD_PARENT_ID = "parent_key";
    private static final String FIELD_META_REFERENCE = "ref";

    private static final String FIELD_PREDICATE_VALUE = "data";
    private static final String FIELD_PREDICATE_LANGUAGE = "lang";
    private static final String FIELD_PREDICATE = "tag";

    /**
     * Sorts a table representation of a tree to breadth-first order: Roots come
     * first then all nodes in the next level and so on.
     * 
     * @author aleksiyrttiaho
     *
     */
    private static final class BFSComparator implements Comparator<Map<String, String>> {
        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            if (isRoot(o1) && isRoot(o2)) {
                return 0;
            } else if (isRoot(o1) || isParentOf(o1, o2)) {
                return -1;
            } else if (isRoot(o2) || isParentOf(o2, o1)) {
                return 1;
            } else {
                return 0;
            }
        }

        private boolean isRoot(Map<String, String> a) {
            return null == a.get(FIELD_PARENT_ID);
        }

        private boolean isParentOf(Map<String, String> a, Map<String, String> b) {
            return a.get(FIELD_NODE_ID).equals(b.get(FIELD_PARENT_ID));
        }
    }

    private class CsvTreeCallbackHandler extends TreeRowCallbackHandler {

        private Map<String, String> row;

        protected CsvTreeCallbackHandler(Map<String, Dimension> dimensions, Map<String, DimensionNode> nodes, Map<String, DimensionNode> nodesByRef,
                Map<String, DimensionLevel> dimensionLevels) {
            super(dimensions, nodes, nodesByRef, dimensionLevels);
        }

        public void processRow(Map<String, String> row) {
            this.row = row;
            try {
                handleRow();
            } catch (Exception e) {
                throw new IllegalStateException("Could not read row " + row, e);
            }
        }

        @Override
        protected String getDimension() throws Exception {
            return row.get(FIELD_DIMENSION);
        }

        @Override
        protected String getDimensionLevel() throws Exception {
            return row.get(FIELD_DIMENSION_LEVEL);
        }

        @Override
        protected String getMetaReference() throws Exception {
            return row.get(FIELD_META_REFERENCE);
        }

        @Override
        protected String getNodeId() throws Exception {
            return row.get(FIELD_NODE_ID);
        }

        @Override
        protected String getParentId() throws Exception {
            return row.get(FIELD_PARENT_ID);
        }

    }

    private final File treeSource;
    private final File factSource;
    private final File metaSource;
    private static final Logger LOG = Logger.getLogger(CSVSource.class);

    public CSVSource(File factSource, File treeSource, File metaSource) {
        this.factSource = factSource;
        this.treeSource = treeSource;
        this.metaSource = metaSource;
    }

    @Override
    protected String getTreeSource() {
        return "file";
    }

    @Override
    protected String getFactSource() {
        return "file";
    }

    @Override
    protected Dataset loadDataInner() {
        final Dataset newDataSet = new Dataset();
        CsvMapReader reader = null;
        try {
            reader = new CsvMapReader(new FileReader(factSource), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            String[] header = reader.getHeader(true);
            LOG.debug("Loading data from colums: " + Arrays.asList(header));
            Map<String, String> row = null;
            while ((row = reader.read(header)) != null) {
                DimensionNode[] keys = new DimensionNode[getColumns().size()];
                int index = 0;
                for (String column : getColumns()) {
                    keys[index++] = getNode(row.get(column));
                }
                if (null != row.get("val")) {
                    newDataSet.put(row.get("val"), Arrays.asList(keys));
                } else {
                    LOG.warn("No value found in row " + row);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file", e);
        } finally {
            try {
                Closeables.close(reader, true);
            } catch (IOException e) {
                LOG.warn("Could not close CSV file reader", e);
            }
        }
        return newDataSet;
    }

    @Override
    protected void loadFactDimensionMetadata(List<String> newDimensionColumns) {

        CsvMapReader reader = null;
        try {
            reader = new CsvMapReader(new FileReader(factSource), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            String[] header = reader.getHeader(true);
            reader.close();
            for (String h : header) {
                if (h.endsWith("_key")) {
                    newDimensionColumns.add(h);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file", e);
        } finally {
            try {
                Closeables.close(reader, true);
            } catch (IOException e) {
                LOG.warn("Could not close CSV file reader", e);
            }
        }
    }

    @Override
    protected void loadNodes(Map<String, Dimension> newDimensions, Map<String, DimensionLevel> dimensionLevels, Map<String, DimensionNode> newNodes,
            Map<String, DimensionNode> nodesByRef) {

        CsvMapReader reader = null;
        try {
            reader = new CsvMapReader(new FileReader(treeSource), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            String[] header = reader.getHeader(true);
            LOG.debug("Loading nodes from columns " + Arrays.asList(header));
            CsvTreeCallbackHandler ctch = new CsvTreeCallbackHandler(newDimensions, newNodes, nodesByRef, dimensionLevels);
            Map<String, String> row = null;
            int rowNum = 0;

            List<Map<String, String>> rows = Lists.newArrayList();
            while ((row = reader.read(header)) != null) {
                rows.add(row);
                ++rowNum;
            }
            LOG.debug("Read " + rowNum + " rows from tree file");
            processDataRootFirst(ctch, rows);

        } catch (IOException e) {
            throw new IllegalStateException("Could not read file", e);
        } finally {
            try {
                Closeables.close(reader, true);
            } catch (IOException e) {
                LOG.warn("Could not close CSV file reader", e);
            }
        }
    }

    private void processDataRootFirst(CsvTreeCallbackHandler ctch, List<Map<String, String>> rows) {
        Collections.sort(rows, new BFSComparator());
        for (Map<String, String> r : rows) {
            ctch.processRow(r);
        }
    }

    @Override
    protected void loadMetadata(Map<String, List<Property>> propertiesByRef) {
        CsvMapReader reader = null;
        try {
            reader = new CsvMapReader(new FileReader(metaSource), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            String[] header = reader.getHeader(true);
            Map<String, String> row = null;
            while ((row = reader.read(header)) != null) {
                if (!propertiesByRef.containsKey(row.get(FIELD_META_REFERENCE))) {
                    propertiesByRef.put(row.get(FIELD_META_REFERENCE), new ArrayList<HydraSource.Property>());
                }
                propertiesByRef.get(row.get(FIELD_META_REFERENCE))
                        .add(new Property(row.get(FIELD_PREDICATE), row.get(FIELD_PREDICATE_LANGUAGE), row.get(FIELD_PREDICATE_VALUE)));

            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file", e);
        } finally {
            try {
                Closeables.close(reader, true);
            } catch (IOException e) {
                LOG.warn("Could not close CSV file reader", e);
            }
        }

    }

    @Override
    public Dataset loadSubset(Query query, List<DimensionNode> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dataset loadSubset(Query query, List<DimensionNode> filter, boolean showValueTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Tuple> loadFactMetadata() {
        return Collections.emptyList();
    }

}
