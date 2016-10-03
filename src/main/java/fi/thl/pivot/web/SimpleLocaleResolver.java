package fi.thl.pivot.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;

public class SimpleLocaleResolver implements LocaleResolver {

    private final ThreadLocal<Locale> locale = new ThreadLocal<>();

    @Override
    public Locale resolveLocale(HttpServletRequest arg0) {
        return null == locale.get() ? new Locale("fi") : locale.get();
    }

    @Override
    public void setLocale(HttpServletRequest arg0, HttpServletResponse arg1, Locale aLocale) {
        this.locale.set(aLocale);
    }

}
