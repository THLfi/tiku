
package fi.thl.pivot.model;

import static fi.thl.pivot.model.ModelTestUtil.mockDimension;
import static fi.thl.pivot.model.ModelTestUtil.mockNode;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import fi.thl.pivot.web.tools.FilterEmpty;

public class PivotImplTest {

    private Pivot pivot;
    private Dataset dataset;

    @Before
    public void setup() {
        dataset = new Dataset();
        pivot = new ModifiablePivot(dataset);
    }

    @Test
    public void shouldHandleSingleColumnHeader() {

        appendColumn(Lists.newArrayList(mockNode("1.1"), mockNode("1.2"), mockNode("1.3")));

        assertEquals(1, pivot.getColumns().size());
        assertEquals(3, pivot.getColumns().get(0).size());
        assertEquals(3, pivot.getColumnCount());

        assertEquals("1.1", columnLabel(0, 0));
        assertEquals("1.2", columnLabel(0, 1));
        assertEquals("1.3", columnLabel(0, 2));

    }

    @Test
    public void shouldHandleMultipleSingleColumnHeader() {
        appendColumn(Lists.newArrayList(mockNode("1.1"), mockNode("1.2")));
        appendColumn(Lists.newArrayList(mockNode("2.1"), mockNode("2.2"), mockNode("2.3")));
        appendColumn(Lists.newArrayList(mockNode("3.1")));

        assertEquals(3, pivot.getColumns().size());
        assertEquals(2, pivot.getColumns().get(0).size());
        assertEquals(3, pivot.getColumns().get(1).size());
        assertEquals(1, pivot.getColumns().get(2).size());
        assertEquals(6, pivot.getColumnCount());

        assertEquals("1.2", columnLabel(0, 1));
        assertEquals("2.2", columnLabel(1, 1));
        assertEquals("3.1", columnLabel(2, 0));
    }

    @Test
    public void shouldHandleSingleRowHeader() {

        appendRow(Lists.newArrayList(mockNode("1.2"), mockNode("1.1"), mockNode("1.3")));

        assertEquals(1, pivot.getRows().size());
        assertEquals(3, pivot.getRows().get(0).size());
        assertEquals(3, pivot.getRowCount());

        assertEquals("1.2", rowLabel(0, 0));
        assertEquals("1.1", rowLabel(0, 1));
        assertEquals("1.3", rowLabel(0, 2));

    }

    @Test
    public void shouldFindCellsAtCoordinates() {

        addTestDimensions();

        assertEquals("1.1;2.1;3.1", pivot.getCellAt(0, 0).getValue());
        assertEquals("1.2;2.1;3.2", pivot.getCellAt(1, 3).getValue());
        assertEquals("1.2;2.3;3.3", pivot.getCellAt(2, 5).getValue());

    }

    @Test
    public void shouldIterateOverCells() {

        addTestDimensions();

        List<String> expected = Lists.newArrayList("1.1;2.1;3.1", "1.1;2.2;3.1", "1.1;2.3;3.1", "1.2;2.1;3.1",
                "1.2;2.2;3.1", "1.2;2.3;3.1",

                "1.1;2.1;3.2", "1.1;2.2;3.2", "1.1;2.3;3.2", "1.2;2.1;3.2", "1.2;2.2;3.2", "1.2;2.3;3.2",

                "1.1;2.1;3.3", "1.1;2.2;3.3", "1.1;2.3;3.3", "1.2;2.1;3.3", "1.2;2.2;3.3", "1.2;2.3;3.3"

        );
        int index = 0;
        for (PivotCell c : pivot) {
            assertEquals("cell " + index + " does not match expected", expected.get(index++), c.getValue());
        }

    }

    @Test
    public void shouldFindHeadersAtCoordinates() {

        addTestDimensions();

        assertEquals("1.1", label(pivot.getColumnAt(0, 0)));
        assertEquals("2.1", label(pivot.getColumnAt(1, 0)));
        assertEquals("2.1", label(pivot.getColumnAt(1, 3)));
        assertEquals("2.3", label(pivot.getColumnAt(1, 5)));

        assertEquals("3.1", label(pivot.getRowAt(0, 0)));
        assertEquals("3.2", label(pivot.getRowAt(0, 1)));
        assertEquals("3.3", label(pivot.getRowAt(0, 2)));
    }

    @Test
    public void shouldApplyConstantDimensionValues() {
        addTestDimensions(mockNode("4", "4"), mockNode("5", "5"));

        assertEquals(3, pivot.getRowCount());
        assertEquals(6, pivot.getColumnCount());
        assertEquals("1.1;2.1;3.1;4;5", pivot.getCellAt(0, 0).getValue());
        assertEquals("1.2;2.1;3.2;4;5", pivot.getCellAt(1, 3).getValue());
        assertEquals("1.2;2.3;3.3;4;5", pivot.getCellAt(2, 5).getValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFilterAll() {

        addTestDimensions();
        pivot = new FilterablePivot(pivot);
        ((FilterablePivot) pivot).applyFilter(new Predicate<PivotCell>() {

            @Override
            public boolean apply(PivotCell input) {
                return true;
            }
        });

        assertEquals(0, pivot.getRowCount());
        assertEquals(0, pivot.getColumnCount());

        pivot.getCellAt(0, 0);
    }

    @Test
    public void shouldFilterColumn() {

        addTestDimensions();
        pivot = new FilterablePivot(pivot);
        ((FilterablePivot) pivot).applyFilter(new Predicate<PivotCell>() {

            @Override
            public boolean apply(PivotCell input) {
                return input.getValue().contains("2.2");
            }
        });

        assertEquals(3, pivot.getRowCount());
        assertEquals(4, pivot.getColumnCount());

        assertEquals("1.1;2.1;3.1", pivot.getCellAt(0, 0).getValue());
        assertEquals("1.1;2.3;3.1", pivot.getCellAt(0, 1).getValue());
        assertEquals("1.2;2.1;3.1", pivot.getCellAt(0, 2).getValue());
        assertEquals("1.2;2.3;3.1", pivot.getCellAt(0, 3).getValue());

        assertEquals("2.3", label(pivot.getColumnAt(1, 1)));

    }

    @Test
    public void shouldFilterRows() {

        addTestDimensions();
        pivot = new FilterablePivot(pivot);
        ((FilterablePivot) pivot).applyFilter(new Predicate<PivotCell>() {

            @Override
            public boolean apply(PivotCell input) {
                return input.getValue().contains("3.2");
            }
        });

        assertEquals(2, pivot.getRowCount());
        assertEquals(6, pivot.getColumnCount());

        assertEquals("1.1;2.1;3.1", pivot.getCellAt(0, 0).getValue());
        assertEquals("1.1;2.1;3.3", pivot.getCellAt(1, 0).getValue());
        assertEquals("1.2;2.3;3.1", pivot.getCellAt(0, 5).getValue());
        assertEquals("1.2;2.3;3.3", pivot.getCellAt(1, 5).getValue());

        assertEquals("3.3", label(pivot.getRowAt(0, 1)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFilterNone() {

        addTestDimensions();
        FilterablePivot fpivot = new FilterablePivot(pivot);
        fpivot.applyFilters(Lists.newArrayList(new Predicate<PivotCell>() {

            @Override
            public boolean apply(PivotCell input) {
                return false;
            }
        }));
        assertEquals(3, pivot.getRowCount());
        assertEquals(6, pivot.getColumnCount());
        assertEquals(3, fpivot.getRowCount());
        assertEquals(6, fpivot.getColumnCount());

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenCellRowIsNegative() {
        addTestDimensions();
        pivot.getCellAt(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenCellColumnIsNegative() {
        addTestDimensions();
        pivot.getCellAt(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenCellRowIsTooLarge() {
        addTestDimensions();
        pivot.getCellAt(pivot.getRowCount(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenCellColumnIsTooLarge() {
        addTestDimensions();
        pivot.getCellAt(0, pivot.getColumnCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenColumnIsNegative() {
        addTestDimensions();
        pivot.getColumnAt(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenRowIsNegative() {
        addTestDimensions();
        pivot.getRowAt(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenColumnIsTooLarge() {
        addTestDimensions();
        pivot.getColumnAt(0, pivot.getColumnCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenRowIsTooLarge() {
        addTestDimensions();
        pivot.getRowAt(0, pivot.getColumnCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenColumnLevelIsNegative() {
        addTestDimensions();
        pivot.getColumnAt(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenColumnLevelIsTooLarge() {
        addTestDimensions();
        pivot.getColumnAt(pivot.getColumns().size(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenRowLevelIsTooLarge() {
        addTestDimensions();
        pivot.getRowAt(pivot.getRows().size(), 0);
    }

    @Test
    public void shouldFilter() {
        Dataset realDataset = new Dataset();
        realDataset.put("1", Lists.newArrayList(mockNode("1.1"), mockNode("2.2")));
        pivot = new ModifiablePivot(realDataset);

        appendColumn(Lists.newArrayList(mockNode("1.1")));
        appendRow(Lists.newArrayList(mockNode("2.1"), mockNode("2.2")));

        assertEquals(1, pivot.getColumnCount());
        assertEquals(2, pivot.getRowCount());

        assertEquals("1.1", pivot.getColumnAt(0, 0).getId());
        assertEquals("2.1", pivot.getRowAt(0, 0).getId());
        assertEquals("2.2", pivot.getRowAt(0, 1).getId());

        pivot = new FilterablePivot(pivot);
        ((FilterablePivot) pivot).applyFilter(new FilterEmpty());

        assertEquals(1, pivot.getRowCount());
        assertEquals(1, pivot.getColumnCount());

        assertEquals("1.1", pivot.getColumnAt(0, 0).getId());
        assertEquals("2.2", pivot.getRowAt(0, 0).getId());
    }

    @Test
    public void shouldOrderRows() {
        Dataset realDataset = new Dataset();
        realDataset.put("1", Lists.newArrayList(mockNode("1.1"), mockNode("2.1")));
        realDataset.put("2", Lists.newArrayList(mockNode("1.1"), mockNode("2.2")));

        pivot = new ModifiablePivot(realDataset);

        appendColumn(Lists.newArrayList(mockNode("1.1")));
        appendRow(Lists.newArrayList(mockNode("2.1"), mockNode("2.2")));

        assertEquals("2.1", pivot.getRowAt(0, 0).getId());
        assertEquals("2.2", pivot.getRowAt(0, 1).getId());

        pivot = new OrderablePivot(pivot);
        ((OrderablePivot) pivot).sortBy(0, OrderablePivot.SortBy.Column, OrderablePivot.SortMode.Descending);

        assertEquals("2.2", pivot.getRowAt(0, 0).getId());
        assertEquals("2.1", pivot.getRowAt(0, 1).getId());
    }

    @Test
    public void shouldOrderColumns() {
        Dataset realDataset = new Dataset();
        realDataset.put("1", Lists.newArrayList(mockNode("1.1"), mockNode("2.1")));
        realDataset.put("2", Lists.newArrayList(mockNode("1.1"), mockNode("2.2")));

        pivot = new ModifiablePivot(realDataset);

        appendRow(Lists.newArrayList(mockNode("1.1")));
        appendColumn(Lists.newArrayList(mockNode("2.1"), mockNode("2.2")));

        assertEquals("2.1", pivot.getColumnAt(0, 0).getId());
        assertEquals("2.2", pivot.getColumnAt(0, 1).getId());

        pivot = new OrderablePivot(pivot);
        ((OrderablePivot) pivot).sortBy(0, OrderablePivot.SortBy.Row, OrderablePivot.SortMode.Descending);

        assertEquals("2.2", pivot.getColumnAt(0, 0).getId());
        assertEquals("2.1", pivot.getColumnAt(0, 1).getId());
    }

    private String rowLabel(int level, int node) {
        return pivot.getRows().get(level).get(node).getLabel().getValue("fi");
    }

    private String columnLabel(int level, int node) {
        return pivot.getColumns().get(level).get(node).getLabel().getValue("fi");
    }

    private void addTestDimensions(IDimensionNode... constants) {
        Dimension d1 = mockDimension("d1");
        Dimension d2 = mockDimension("d2");
        Dimension d3 = mockDimension("d3");

        ArrayList<IDimensionNode> c1 = Lists.newArrayList(mockNode("1.1", d1), mockNode("1.2", d1));
        appendColumn(c1);
        ArrayList<IDimensionNode> c2 = Lists.newArrayList(mockNode("2.1", d2), mockNode("2.2", d2), mockNode("2.3", d2));
        appendColumn(c2);
        ArrayList<IDimensionNode> c3 = Lists.newArrayList(mockNode("3.1", d3), mockNode("3.2", d3), mockNode("3.3", d3));
        appendRow(c3);

        for (IDimensionNode c : constants) {
            ((ModifiablePivot) pivot).appendConstant(c);
        }

        Joiner joiner = Joiner.on(";");
        for (IDimensionNode n1 : c1) {
            for (IDimensionNode n2 : c2) {
                for (IDimensionNode n3 : c3) {
                    List<IDimensionNode> keys = Lists.newArrayList(n1, n2, n3);
                    for (IDimensionNode c : constants) {
                        keys.add(c);
                    }
                    String label = joiner.join(keys);
                    dataset.put(label, keys);
                }
            }
        }
    }

    private void appendColumn(ArrayList<IDimensionNode> newArrayList) {
        PivotLevel level = new PivotLevel();
        level.add(newArrayList);
        ((ModifiablePivot) pivot).appendColumn(level);
    }

    private void appendRow(ArrayList<IDimensionNode> newArrayList) {
        PivotLevel level = new PivotLevel();
        level.add(newArrayList);
        ((ModifiablePivot) pivot).appendRow(level);
    }

    private String label(IDimensionNode n) {
        return n.getLabel().getValue("fi");
    }
}
