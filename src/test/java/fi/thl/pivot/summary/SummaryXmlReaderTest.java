package fi.thl.pivot.summary;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fi.thl.pivot.summary.SummaryException;
import fi.thl.pivot.summary.SummaryReader;
import fi.thl.pivot.summary.model.DataPresentation;
import fi.thl.pivot.summary.model.Presentation;
import fi.thl.pivot.summary.model.Summary;
import fi.thl.pivot.summary.model.SummaryDimension;
import fi.thl.pivot.summary.model.TextPresentation;

public class SummaryXmlReaderTest {

    private Summary summary;

    @Before
    public void setup() throws FileNotFoundException, SummaryException {

        SummaryReader reader = new SummaryReader();
        reader.read(new FileInputStream("src/test/resources/summary_demo2.xml"));
        this.summary = reader.getSummary();

    }

    @Test
    public void shouldReadFactTable() {

        assertEquals("fact_toitu", summary.getFactTable());

    }

    @Test
    public void shouldReadItemSpecificationLanguage() {

        assertEquals("fi", summary.getItemLanguage());
    }

    @Test
    public void shouldReadSummaryAttributes() {

        assertEquals("demo2", summary.getId());
        assertEquals("1.1", summary.getSpecificationVersion());
        assertFalse(summary.isDrillEnabled());

    }

    @Test
    public void shouldReadFilters() {

        assertEquals(2, summary.getSelections().size());
        assertNotNull(summary.getSelection("kuntakaikki"));
        assertEquals("municip", summary.getSelection("kuntakaikki").getDimension());
        assertTrue(summary.getSelection("kuntakaikki").getDefaultItem().contains("jämsä"));

        assertEquals("leaf", summary.getSelection("mittari").getStages().get(0));

    }

    @Test
    public void shouldReadSummaryDescription() {
        assertTrue(summary.getSupportedLanguages().contains("fi"));
        assertTrue(summary.getSupportedLanguages().contains("sv"));
        assertTrue(summary.getSupportedLanguages().contains("en"));

    }

    @Test
    public void shouldReadLabels() {
        assertEquals("Demotiiviste 2", summary.getSubject().getValue("fi"));
        assertEquals("All graph types", summary.getTitle().getValue("en"));
        assertEquals("http://wwww.thl.fi/", summary.getLink().getValue("fi"));
        assertEquals("Svensk text här.", summary.getNote().getValue("sv"));
    }

    @Test
    public void shouldReadPresentations() {
        assertEquals(32, summary.getPresentations().size());
        assertEquals(5, count(summary.getPresentations(), TextPresentation.class));
        assertEquals(27, count(summary.getPresentations(), DataPresentation.class));

        assertPresentationOrder(summary.getPresentations(),

                "line", "line", "subtitle", "column", "bar", "column", "bar", "subtitle", "info", "columnstacked", "barstacked", "columnstacked100",
                "barstacked100", "info", "pie", "gauge", "subtitle", "gauge", "gauge", "gauge", "gauge", "gauge", "pie", "pie", "pie", "pie", "pie", "line",
                "column", "line", "column", "columnstacked100");
    }

    @Test
    public void shouldParseSingleDimensionPresentationConfiguration() {
        DataPresentation p = (DataPresentation) summary.getPresentations().get(0);
        assertEquals("line", p.getType());
        assertEquals(1, p.getDimensions().size());
        assertEquals("time", (((SummaryDimension) p.getDimensions().get(0))).getDimension());
        assertEquals("year", (((SummaryDimension) p.getDimensions().get(0))).getStage().getStage());
    }

    @Test
    public void shouldParseMultiDimensionPresentationConfiguration() {
        DataPresentation p = (DataPresentation) summary.getPresentations().get(1);
        assertEquals("line", p.getType());
        assertEquals(2, p.getDimensions().size());
        assertEquals("time", (((SummaryDimension) p.getDimensions().get(0))).getDimension());
        assertEquals("year", (((SummaryDimension) p.getDimensions().get(0))).getStage().getStage());
        assertEquals("avi", (((SummaryDimension) p.getDimensions().get(1))).getDimension());
        assertEquals("avi", (((SummaryDimension) p.getDimensions().get(1))).getStage().getStage());
    }

    @Test
    public void shouldParsePresentationFilter() {
        DataPresentation p = (DataPresentation) summary.getPresentations().get(0);

        assertEquals(2, p.getFilters().size());

        assertEquals("municip", p.getFilters().get(0).getDimension());
        assertEquals("kuntakaikki", p.getFilters().get(0).getId());
        assertEquals("mittari", p.getFilters().get(1).getId());

    }

    @Test
    public void shouldParseTextPresentationContent() {
        TextPresentation p = (TextPresentation) summary.getPresentations().get(2);
        assertEquals("Tämä on alaotsikko", p.getContent().getValue("fi"));
        assertEquals("This is a subtitle", p.getContent().getValue("en"));
    }

    private void assertPresentationOrder(List<Presentation> p, String... type) {
        int i = 0;
        for (; i < p.size(); ++i) {
            if (i >= type.length) {
                fail("Number of expected types exceeded, " + i + " >=" + type.length);
            }
            assertEquals(type[i], p.get(i).getType());
        }
        if (i < type.length) {
            fail("Number of expected types not reached, " + i + " < " + type.length);
        }
    }

    private int count(List<?> objects, Class<?> cls) {
        int count = 0;
        for (Object o : objects) {
            if (cls.isInstance(o)) {
                ++count;
            }
        }
        return count;
    }

}
