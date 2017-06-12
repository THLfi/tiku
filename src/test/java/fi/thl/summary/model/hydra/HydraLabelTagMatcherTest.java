package fi.thl.summary.model.hydra;

import static org.junit.Assert.*;

import org.junit.Test;

public class HydraLabelTagMatcherTest {


    @Test
    public void shouldMatchBasicSelection() {
        HydraLabelTagMatcher m = new HydraLabelTagMatcher("$$id.value$$");
        assertTrue("Could not detect tag in content", m.find());
        assertEquals("Identifier did not equal expected", "id", m.getIdentifier());
        assertEquals("Property did not equal expected", "value", m.getProperty());
        assertNull("Should not have included level", m.getStage());
    }
    
    @Test
    public void shouldMatchBasicSelectionWithUnderscore() {
        HydraLabelTagMatcher m = new HydraLabelTagMatcher("$$id_specific.value$$");
        assertTrue("Could not detect tag in content", m.find());
        assertEquals("Identifier did not equal expected", "id", m.getIdentifier());
        assertEquals("Property did not equal expected", "value", m.getProperty());
        assertEquals("Should include level", "specific", m.getStage());
    }

}
