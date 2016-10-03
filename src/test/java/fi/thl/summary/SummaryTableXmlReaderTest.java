package fi.thl.summary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import fi.thl.summary.model.MeasureItem;
import fi.thl.summary.model.Summary;
import fi.thl.summary.model.SummaryDimension;
import fi.thl.summary.model.SummaryItem;
import fi.thl.summary.model.SummaryMeasure;
import fi.thl.summary.model.TablePresentation;

public class SummaryTableXmlReaderTest {

    private final class MeasureItemToCode implements Function<MeasureItem, String> {
        @Override
        public String apply(MeasureItem mi) {
            return mi.getCode();
        }
    }

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {

        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary_table.xml"));
        this.summary = reader.getSummary();

    }

    @Test
    public void shouldReadFactTable() {

        assertEquals("fact_ahil_hpaasysth01_kaikki", summary.getFactTable());

    }

    @Test
    public void shouldReadItemSpecificationLanguage() {

        assertEquals("fi", summary.getItemLanguage());
    }

    @Test
    public void shouldReadSummaryAttributes() {

        assertEquals("sthrapo1", summary.getId());
        assertEquals("1.1", summary.getSpecificationVersion());
        assertTrue(summary.isDrillEnabled());

    }

    @Test
    public void shouldReadFilters() {

        assertEquals(3, summary.getSelections().size());
        assertNotNull(summary.getSelection("aika"));
        assertEquals("aika", summary.getSelection("aika").getDimension());
        assertEquals("kuukausi", summary.getSelection("aika").getStages().get(0));
        assertTrue(summary.getSelection("aika").getDefaultItem().contains(":last:"));

        assertEquals("leaf", summary.getSelection("mittari").getStages().get(0));

    }

    @Test
    public void shouldReadSummaryDescription() {
        assertTrue(summary.getSupportedLanguages().contains("fi"));
    }

    @Test
    public void shouldReadTablePresentation() {
        assertEquals(1, summary.getPresentations().size());
        assertTrue("table presentation should be an instance of TablePresentation " + summary.getPresentations().get(0).getClass(),
                summary.getPresentations().get(0) instanceof TablePresentation);
    }

    @Test
    public void shouldReadRows() {
        final List<SummaryItem> rows = ((TablePresentation) summary.getPresentations().get(0)).getRows();
        assertEquals(1, rows.size());
        SummaryDimension dim = (SummaryDimension) rows.get(0);
        assertTrue("Should display total", dim.includeTotal());
        assertEquals("palveluntuottaja", dim.getDimension());
        assertEquals("palveluntuottaja_avi", dim.getStage().getStage());
    }

    @Test
    public void shouldReadColumns() {
        final List<SummaryItem> cols = ((TablePresentation) summary.getPresentations().get(0)).getColumns();

        assertEquals(1, cols.size());

        final Set<MeasureItem> measures = ((SummaryMeasure) cols.get(0)).getMeasures();
        assertTrue(Collections2.transform(measures, new MeasureItemToCode()).contains("TESTIMITTARI"));
        assertTrue(Collections2.transform(measures, new MeasureItemToCode()).contains("II"));

    }

}
