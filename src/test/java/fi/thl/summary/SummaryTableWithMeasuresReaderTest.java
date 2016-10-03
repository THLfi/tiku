package fi.thl.summary;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import fi.thl.summary.model.DataPresentation;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Summary;

public class SummaryTableWithMeasuresReaderTest {

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {
        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary4.xml"));
        this.summary = reader.getSummary();
    }

    @Test
    public void shouldReadMeasuresAsRows() {
        assertEquals("definition should describe one presentation", 1, summary.getPresentations().size());
        Presentation p = summary.getPresentations().get(0);
        assertTrue("definition should describe a data presentation", p instanceof DataPresentation);
        assertEquals("definition should describe a column graph", "column", p.getType());

        DataPresentation dp = (DataPresentation) p;
        assertEquals("Should have one classifier", 1, dp.getDimensions().size());
        assertTrue("Should have measures defined", dp.hasMeasures());
        assertEquals("Should have 5 measures", 5, dp.getMeasures().getMeasures().size());

    }

}
