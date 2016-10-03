package fi.thl.pivot.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class PdfExporter {

    private static final Logger LOG = Logger.getLogger(PdfExporter.class);
 
    private FreeMarkerConfig freemarker;

    public PdfExporter(String language, FreeMarkerConfig configurer) {
        this.freemarker = configurer;
    }

    public void export(Model model, OutputStream out) throws IOException {

        try {
            
            ITextRenderer renderer = new ITextRenderer();
            LOG.debug("Creating HTML representation of cube ");
            renderer.setDocument(createHtmlTable(model),  null);
            renderer.layout();
            LOG.debug("Rendering HTML as PDF");
            renderer.createPDF(out, true);
        } catch (DocumentException e) {
            LOG.error("Failed to create pdf", e);
            throw new IOException(e);
        } catch (TemplateException e) {
            LOG.error("Failed to render template", e);
            throw new IOException(e);
        } catch (Exception e) {
            LOG.error("Failed to create pdf", e);
            throw new IOException(e);
        }
    }

    private org.w3c.dom.Document createHtmlTable(Model model) throws IOException,
            TemplateException, SAXException, ParserConfigurationException {

        ByteArrayOutputStream bos = processFreemarkerTemplate(model);
        
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        documentBuilderFactory.setValidating(false);
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(FSEntityResolver.instance());
        org.w3c.dom.Document document = builder.parse(new ByteArrayInputStream(
                bos.toString().getBytes("UTF-8")));
        
        return document;

    }

    private ByteArrayOutputStream processFreemarkerTemplate(Model model) throws IOException, TemplateException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos, false);

        
        final Configuration configuration = freemarker.getConfiguration();
        Template tmpl = configuration.getTemplate("cube.pdf.ftl");
        tmpl.process(model.asMap(), writer);
        writer.flush();
        return bos;
    }
}
