package fi.thl.pivot.datasource;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import fi.thl.pivot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fi.thl.pivot.util.Constants;

/**
 * An implementatio of a hydra source where data is loaded through a JDBC
 * connection.
 * 
 * @author aleksiyrttiaho
 *
 */
public class JDBCSource extends HydraSource {

    private static final String ALWAYS_TRUE = "1 = 1";

    private static final String ALWAYS_FALSE = "1 = 0";

    private static final String FACT_QUERY_TEMPLATE = "SELECT %s, VAL from %s WHERE %s";

    private static final int FETCH_SIZE = 2048;

    private static final String FIELD_DIMENSION = "dim";
    private static final String FIELD_DIMENSION_LEVEL = "stage";
    private static final String FIELD_NODE_ID = "key";
    private static final String FIELD_PARENT_ID = "parent_key";
    private static final String FIELD_META_REFERENCE = "ref";
    private static final String FIELD_PREDICATE_VALUE = "data";
    private static final String FIELD_PREDICATE_LANGUAGE = "lang";
    private static final String FIELD_PREDICATE = "tag";
    private static final String FIELD_SURROGATE_ID = "surrogate_id";

    private static final class JDBCTreeCallbackHandler extends TreeRowCallbackHandler implements RowCallbackHandler {

        private ResultSet rs;

        protected JDBCTreeCallbackHandler(Map<String, Dimension> dimensions, Map<String, IDimensionNode> nodes, Map<String, IDimensionNode> nodesByRef,
                                          Map<String, DimensionLevel> dimensionLevels) {
            super(dimensions, nodes, nodesByRef, dimensionLevels);
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            this.rs = rs;
            try {
                handleRow();
            } catch (SQLException e) {
                logger.error("Failed to process tree", e);
                throw e;

            } catch (Exception e) {
                logger.error("Failed to process tree", e);
                throw new SQLException("", e);
            }
        }

        @Override
        protected String getDimension() throws Exception {
            return rs.getString(FIELD_DIMENSION);
        }

        @Override
        protected String getDimensionLevel() throws Exception {
            return rs.getString(FIELD_DIMENSION_LEVEL);
        }

        @Override
        protected String getMetaReference() throws Exception {
            return rs.getString(FIELD_META_REFERENCE);
        }

        @Override
        protected String getNodeId() throws Exception {
            return rs.getString(FIELD_NODE_ID);
        }

        @Override
        protected String getParentId() throws Exception {
            return rs.getString(FIELD_PARENT_ID);
        }

        @Override
        protected Integer getSurrogateId() throws Exception {
            return rs.getInt(FIELD_SURROGATE_ID);
        }

    }

    private final class ListDimensionColumnsBasedOnDatabaseMetadata implements DatabaseMetaDataCallback {
        private final List<String> dimensionColumns;

        private ListDimensionColumnsBasedOnDatabaseMetadata(List<String> dimensionColumns) {
            this.dimensionColumns = dimensionColumns;
        }

        @Override
        public Object processMetaData(DatabaseMetaData dmd) throws SQLException, MetaDataAccessException {
            ResultSet rs = queryForColumnsInFactTable(dmd);
            listDimensionKeyColumnsInTable(rs);
            rs.close();
            return null;
        }

        private ResultSet queryForColumnsInFactTable(DatabaseMetaData dmd) throws SQLException {
            String[] tableNameParts = factTable.split("\\.");
            logger.debug("Fetching metadata for table " + Lists.newArrayList(tableNameParts));

            String schemaName = isSchemaDefined(tableNameParts) ? tableNameParts[0].toLowerCase() : "";
            String tableName = isSchemaDefined(tableNameParts) ? tableNameParts[1].toLowerCase() : tableNameParts[0].toLowerCase();
            
            
            ResultSet rs =  dmd.getColumns(null, schemaName, tableName, null);
            if(rs.next()) {
                rs.previous();
                return rs;
            } else {
                // Postgres uses lowercase letter in schema and table names by default 
                // but SQL standard states that they should be in upper case
                // When testing with HSQLDB or other inmemory database this 
                // method wont work unless name are transformed to upper case. 
                return dmd.getColumns(null, schemaName.toUpperCase(), tableName.toUpperCase(), null);
            }
        }

        private void listDimensionKeyColumnsInTable( ResultSet rs) throws SQLException {
            while (rs.next()) {
                logger.debug(String.format("Column '%s' detected in fact table", rs.getString("COLUMN_NAME")));
                if (rs.getString("COLUMN_NAME").toLowerCase().endsWith("_key")) {
                    dimensionColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
            logger.debug("All columns processed");
        }

        private boolean isSchemaDefined(String[] tableNameParts) {
            return tableNameParts.length == 2;
        }
    }

    private final class MetadataRowCallbackHandler implements RowCallbackHandler {
        private final Map<String, List<Property>> propertiesByRef;

        private MetadataRowCallbackHandler(Map<String, List<Property>> propertiesByRef) {
            this.propertiesByRef = propertiesByRef;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            if (!propertiesByRef.containsKey(rs.getString(FIELD_META_REFERENCE))) {
                propertiesByRef.put(rs.getString(FIELD_META_REFERENCE), new ArrayList<HydraSource.Property>());
            }
            propertiesByRef.get(rs.getString(FIELD_META_REFERENCE))
                    .add(new Property(rs.getString(FIELD_PREDICATE), rs.getString(FIELD_PREDICATE_LANGUAGE), rs.getString(FIELD_PREDICATE_VALUE)));
        }
    }

    private final Logger logger = LoggerFactory.getLogger(JDBCSource.class);
    private final String factTable;
    private final String treeTable;
    private final String metaTable;
    private final JdbcTemplate jdbcTemplate;
    private final Properties queries;
    private String label;
    private String fact;

    private String schema;

    public JDBCSource(String label, String fact, DataSource dataSource, Properties queries, String factTable, String treeTable, String metaTable,
            String environment) {
        Preconditions.checkNotNull(queries, "No queries defined for source");
        Preconditions.checkNotNull(dataSource, "No JDBC connection defined for source");
        Preconditions.checkNotNull(factTable, "No fact table defined for source");
        Preconditions.checkNotNull(treeTable, "No tree table defined for source");
        Preconditions.checkNotNull(metaTable, "No meta table defined for source");

        Preconditions.checkNotNull(environment, "No environment defined for source");

        // FIXME: there is no validation to check if table names are legal
        // this leads to a possible SQL injection.

        Preconditions.checkNotNull(queries.getProperty("traverse-tree-using-bfs"), "Queries missing required query 'traverse-tree-using-bfs'");
        Preconditions.checkNotNull(queries.getProperty("load-node-metadata"), "Queries missing required query 'load-node-metadata'");

        this.factTable = factTable;
        this.treeTable = treeTable;
        this.metaTable = metaTable;
        this.queries = queries;
        this.label = label == null ? factTable : label;
        this.fact = fact;
        this.schema = environment;

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(FETCH_SIZE);
    }

    @Override
    protected Dataset loadDataInner() {
        final Dataset newDataSet = new Dataset();
        final int columns = getColumns().size();

        jdbcTemplate.query(buildFactQuery(), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                List<IDimensionNode> keys = new ArrayList<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    keys.add(getNode(rs.getString(i)));
                }
                newDataSet.put(rs.getString(columns + 1), keys);

            }
        });
        return newDataSet;
    }

    @Override
    public Dataset loadSubset(Query queryNodes, List<IDimensionNode> filter) {
        return loadSubset(queryNodes, filter, false);
    }

    @Override
    @Transactional
    public Dataset loadSubset(Query queryNodes, List<IDimensionNode> filter, boolean showValueTypes) {
        final Dataset newDataSet = new Dataset();
        final String query = buildFactQuery(queryNodes.getNodesPerDimension().values(), filter, showValueTypes);
        logger.debug("Loading subset of facts using :" + query);
        jdbcTemplate.setFetchSize(FETCH_SIZE);
        jdbcTemplate.query(query, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                IDimensionNode[] keys = new IDimensionNode[getColumns().size()];
                for (int i = 1; i <= getColumns().size(); ++i) {
                    keys[i - 1] = getNode(rs.getString(i));
                }
                newDataSet.put(rs.getString(getColumns().size() + 1), Arrays.asList(keys));
            }
        });
        return newDataSet;
    }

    protected void loadFactDimensionMetadata(final List<String> newDimensionColumns) {
        try {
            JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new ListDimensionColumnsBasedOnDatabaseMetadata(newDimensionColumns));
        } catch (MetaDataAccessException e) {
            logger.error("could not fetch metadata for " + factTable, e);
        }
    }

    protected void loadNodes(final Map<String, Dimension> newDimensions, final Map<String, DimensionLevel> dimensionLevels,
                             final Map<String, IDimensionNode> newNodes, final Map<String, IDimensionNode> nodesByRef) {
        jdbcTemplate.setFetchSize(FETCH_SIZE);
        jdbcTemplate.query(String.format(queries.getProperty("traverse-tree-using-bfs"), treeTable, schema),
                new JDBCTreeCallbackHandler(newDimensions, newNodes, nodesByRef, dimensionLevels));
    }

    protected void loadMetadata(final Map<String, List<Property>> propertiesByRef) {
        jdbcTemplate.setFetchSize(FETCH_SIZE);
        jdbcTemplate.query(String.format(queries.getProperty("load-node-metadata"), metaTable), new MetadataRowCallbackHandler(propertiesByRef));
    }

    @Override
    protected Label loadCubeName() {
        final Label name = new Label();
        jdbcTemplate.query(String.format(queries.getProperty("load-cube-name"), metaTable), new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                name.setValue(rs.getString("lang"), rs.getString("data"));
            }
        }, fact);
        return name;
    }

    @Override
    protected String getFactSource() {
        return label;
    }

    @Override
    protected String getTreeSource() {
        return treeTable;
    }

    @Override
    protected List<Tuple> loadFactMetadata() {
        return jdbcTemplate.query(String.format("select * from %s where ref = (select ref from %s where tag = 'is' and data = ?)", metaTable, metaTable),
                new TupleMapper(), fact);
    }

    private String buildFactQuery() {
        return String.format(FACT_QUERY_TEMPLATE, Joiner.on(',').join(getColumns()), factTable, ALWAYS_TRUE);
    }

    private String buildFactQuery(Collection<IDimensionNode> shown, List<IDimensionNode> filter, boolean showValueTypes) {
        Multimap<String, String> shownDimensionNodes = determineFilterationRules(shown, filter, showValueTypes);
        if (shownDimensionNodes.isEmpty()) {
            return String.format(FACT_QUERY_TEMPLATE, Joiner.on(',').join(getColumns()), factTable, ALWAYS_FALSE);
        } else {
            return String.format(FACT_QUERY_TEMPLATE, Joiner.on(',').join(getColumns()), factTable,
                    Joiner.on(" and ").join(constructWhereStatementsParts(shownDimensionNodes)));
        }
    }

    private Multimap<String, String> determineFilterationRules(Collection<IDimensionNode> shown, List<IDimensionNode> filter, boolean showValueTypes) {
        Multimap<String, String> shownDimensionNodes = ArrayListMultimap.create();
        Set<IDimensionNode> expandedFilter = Sets.newHashSet();

        expandedFilter.addAll(filter);
        removeFilteredNodes(shown, expandedFilter);
        addShownDimensionNodesAsFilter(shown, shownDimensionNodes, showValueTypes);
        addHiddenDimensionsAsFilter(expandedFilter, shownDimensionNodes, showValueTypes);

        return shownDimensionNodes;
    }

    private void removeFilteredNodes(Collection<IDimensionNode> shown, Set<IDimensionNode> expandedFilter) {
        for (IDimensionNode hiddenFilterNode : expandedFilter) {
            for (Iterator<IDimensionNode> it = shown.iterator(); it.hasNext();) {
                IDimensionNode shownFilterNode = it.next();
                if (nodesInSameDimension(hiddenFilterNode, shownFilterNode) && !shownFilterNode.descendentOf(hiddenFilterNode)) {
                    it.remove();
                }
            }
        }
    }

    private boolean nodesInSameDimension(IDimensionNode hiddenFilterNode, IDimensionNode shownFilterNode) {
        return shownFilterNode.getDimension().getId().equals(hiddenFilterNode.getDimension().getId());
    }

    private List<String> constructWhereStatementsParts(Multimap<String, String> conditions) {
        Joiner j = Joiner.on(",");
        List<String> c = Lists.newArrayList();
        for (String key : conditions.keySet()) {
            c.add(String.format("%s_key in (%s)", key, j.join(conditions.get(key))));
        }
        return c;
    }

    private void addHiddenDimensionsAsFilter(Collection<IDimensionNode> filter, Multimap<String, String> conditions, boolean showValueTypes) {
        for (IDimensionNode h : filter) {
            conditions.put(h.getDimension().getId(), "'" + h.getId() + "'");
            addValueTypes(conditions, showValueTypes, h);
        }
    }

    private void addShownDimensionNodesAsFilter(Collection<IDimensionNode> shown, Multimap<String, String> conditions, boolean showValueTypes) {
        for (IDimensionNode h : shown) {
            conditions.put(h.getDimension().getId(), "'" + h.getId() + "'");
            addValueTypes(conditions, showValueTypes, h);
        }
    }

    private void addValueTypes(Multimap<String, String> conditions, boolean showValueTypes, IDimensionNode h) {
        if(showValueTypes && Constants.MEASURE.equals(h.getDimension().getId())) {
           putMeasureIfNotNull(conditions, h.getConfidenceLowerLimitNode());
           putMeasureIfNotNull(conditions, h.getConfidenceUpperLimitNode());
           putMeasureIfNotNull(conditions, h.getSampleSizeNode());
           
        }
    }

    private void putMeasureIfNotNull(Multimap<String, String> conditions, IDimensionNode node) {
        if(null != node) {
            conditions.put(Constants.MEASURE, "'" + node.getId() + "'");
        }
    }

}
