package fi.thl.pivot.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fi.thl.pivot.TextView;
import fi.thl.pivot.model.ModifiablePivot;
import fi.thl.pivot.model.PivotLevel;

public class CsvSourceTest {

    private HydraSource source;

    @Before
    public void loadCube() {
        source = new CSVSource(new File("src/test/resources/test-fact.csv"), new File("src/test/resources/test-tree.csv"),
                new File("src/test/resources/test-meta.csv"));
        source.loadMetadata();
    }

    @Test
    public void shouldLoadAllNodes() {
        for (Integer id : new Integer[] { 1118, 1128, 1158, 22, 23, 29, 330, 333, 334 }) {
            assertNotNull("Node " + id + " not loaded", source.getNode(String.valueOf(id)));
        }
    }

    @Test
    public void shouldLoadAreaHierarchy() {
        assertParentAndChildrenAssociation("29", "22", "23");
    }

    @Test
    public void shouldLoadTimeHierarchy() {
        assertParentAndChildrenAssociation("1158", "1118", "1128");
    }

    private void assertParentAndChildrenAssociation(String parent, String... children) {
        assertEquals(children.length, source.getNode(parent).getChildren().size());
        for (String child : children) {
            assertEquals(source.getNode(parent), source.getNode(child).getParent());
        }
    }

    @Test
    public void shouldDisplayDataAsTable() {
        PivotLevel rows = new PivotLevel();
        rows.add(source.getNode("29"));

        PivotLevel columns = new PivotLevel();
        columns.add(Lists.newArrayList(source.getNode("1158")));

        PivotLevel column2 = new PivotLevel();
        column2.add(Lists.newArrayList(source.getNode("333")));
        
        ModifiablePivot pivot = new ModifiablePivot(source.loadData());
        pivot.appendColumn(columns);
        pivot.appendColumn(column2);
        pivot.appendRow(rows);
        
        TextView view = new TextView(pivot, "fi");
        view.display(System.out);
    }
}
