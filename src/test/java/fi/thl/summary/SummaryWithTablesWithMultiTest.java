package fi.thl.summary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fi.thl.summary.model.MeasureItem;
import fi.thl.summary.model.Presentation;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryMeasure;
import fi.thl.summary.model.TablePresentation;

public class SummaryWithTablesWithMultiTest {

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {
        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary5.xml"));
        this.summary = reader.getSummary();
    }

    @Test
    public void shouldReadMultiRowsAndColumns() {
        assertEquals("definition should describe one presentation", 1, summary.getPresentations().size());
        Presentation p = summary.getPresentations().get(0);
        assertTrue("definition should describe a data presentation", p instanceof TablePresentation);

        TablePresentation tp = (TablePresentation) p;

        assertEquals(2, tp.getRows().size());
        assertEquals(2, tp.getColumns().size());
    }

    @Test
    public void shouldReadMeasureRefsFromColumnsOrRows() {
        Presentation p = summary.getPresentations().get(0);
        TablePresentation tp = (TablePresentation) p;
        assertTrue(tp.getColumns().get(0) instanceof SummaryMeasure);
        SummaryMeasure sm = (SummaryMeasure) tp.getColumns().get(0);
        assertEquals(2, sm.getMeasures().size());
        
        List<MeasureItem> items = Lists.newArrayList(sm.getMeasures());
        
        assertEquals(MeasureItem.Type.REFERENCE, items.get(0).getType());
        assertEquals(MeasureItem.Type.REFERENCE, items.get(1).getType());

        assertEquals("bruttomenot", items.get(0).getCode());
        assertEquals("kotitaloudet", items.get(1).getCode());

    }

}
