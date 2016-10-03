package fi.thl.pivot.web.tools;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * Provides a simple wrapper for a MessageSource that can be used when templates
 * are generated out of request context. This is used in generating a PDF from
 * freemarker template.
 * 
 * @author aleksiyrttiaho
 *
 */
public final class MessageSourceWrapper {

    private MessageSource src;
    private Locale locale;

    public MessageSourceWrapper(MessageSource src, String language) {
        this.src = src;
        this.locale = new Locale(language);
    }

    public String getMessage(String code) {
        return src.getMessage(code, null, code, locale);
    }
}