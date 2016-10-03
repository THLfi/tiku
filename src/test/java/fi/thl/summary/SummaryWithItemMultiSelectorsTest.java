package fi.thl.summary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import fi.thl.summary.model.Summary;

public class SummaryWithItemMultiSelectorsTest {

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {
        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary7.xml"));
        this.summary = reader.getSummary();
    }

    @Test
    public void shouldNotFail() {
        assertEquals("Should use ref scheme", Summary.Scheme.Reference, summary.getScheme());
        assertEquals("Should describe all presentations", 13, summary.getPresentations().size());
        assertEquals("Should describe all filters", 17, summary.getSelections().size());

        assertEquals("alue1", summary.getSelection("alue1").getId());
        assertEquals("avi", summary.getSelection("alue1").getDimension());
        assertEquals(2, summary.getSelection("alue1").getSets().size());
        assertEquals(1, summary.getSelection("alue1").getItems().size());
        assertTrue(summary.getSelection("alue1").getDefaultItem().contains("http://meta.thl.fi/codes/wild/dimension/avi/2011/region/11"));
    }

}
