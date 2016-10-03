package fi.thl.summary;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.Summary;

public class SummaryWithGaugeTest {

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {
        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary8.xml"));
        this.summary = reader.getSummary();
    }

    @Test
    public void shouldNotFail() {
        
        assertEquals("gauge1", summary.getPresentations().get(2).getId());
        DataPresentation dp = (DataPresentation) summary.getPresentations().get(2);
        assertEquals(new Integer(0), dp.getMin());
        assertEquals(new Integer(100), dp.getMax());
        assertEquals("greenyellowred", dp.getPalette());
    }
    
    

}
