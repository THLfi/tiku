package fi.thl.summary;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryDimension;
import fi.thl.summary.model.SummaryItem;

public class ComplexSummaryTest {

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {
        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary6.xml"));
        this.summary = reader.getSummary();
    }

    @Test
    public void shouldAcceptMultiItemInPresentations() {
        assertEquals("Should describe all presentations", 15, summary.getPresentations().size());
        DataPresentation dp = (DataPresentation) summary.getPresentations().get(3);
        assertEquals("graafiitemmulti", dp.getId());
        for (SummaryItem si : dp.getDimensions()) {
            SummaryDimension sd = (SummaryDimension) si;
            if ("municip".equals(sd.getDimension())) {
                assertEquals(4, ((SummaryDimension) si).getStage().getItems().size());
            }
        }
    }

}
