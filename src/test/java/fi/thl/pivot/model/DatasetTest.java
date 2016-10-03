package fi.thl.pivot.model;

import static fi.thl.pivot.model.ModelTestUtil.mockNode;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fi.thl.pivot.model.Dataset;
import fi.thl.pivot.model.DimensionNode;

public class DatasetTest {

    private Dataset dataset;

    @Before
    public void setup() {
        this.dataset = new Dataset();
    }

    @Test
    public void shouldHoldValues() {
        assertValueIsPut("1", "A");
        assertValueIsPut("2", "B");
        assertValueIsPut("3", "C");
    }

    @Test
    public void shouldReplaceExistingValue() {
        assertValueIsPut("1", "A");
        assertValueIsPut("2", "A");
    }

    @Test
    public void shouldHandleNullValues() {
        assertValueIsPut(null, "A");
        assertValueIsPut("4", "A");
    }

    @Test
    public void shouldIgnoreNullKeys() {
        dataset.put("1", (List<DimensionNode>) null);
        dataset.put("1", Collections.<DimensionNode> emptyList());
    }

    @Test
    public void shouldBeIndifferentToKeyOrder() {
        assertValueIsPut("1", "A", "B");
        assertValueIsPut("2", "B", "A");
        assertEquals("2", dataset.get(keys("A", "B")));
    }

    private void assertValueIsPut(String value, String... key) {
        List<DimensionNode> keys = keys(key);
        dataset.put(value, keys);
        assertEquals(value, dataset.get(keys));
    }

    private List<DimensionNode> keys(String... key) {
        List<DimensionNode> keys = Lists.newArrayList();
        for (String k : key) {
            keys.add(mockNode(k));
        }
        return keys;
    }
}
