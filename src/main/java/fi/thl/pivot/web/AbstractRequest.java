package fi.thl.pivot.web;

import java.util.Locale;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import fi.thl.pivot.util.Constants;

/**
 * 
 * AbstractRequest is a helper class for sharing information on cube and summary
 * requests in different parts of the controller. It is also responsible for
 * checking if the parameters are sensible.
 * 
 * @author aleksiyrttiaho
 *
 */
public class AbstractRequest {
    private static final String DEFAULT_LANGUAGE = "fi";
    private static final ImmutableList<String> VALID_UI_LANGUAGES = ImmutableList.of(DEFAULT_LANGUAGE, "sv", "en");
    protected String cube;
    protected String env = "prod";
    protected String subject;
    protected String hydra;
    protected String runId = "latest";
    protected Locale locale = new Locale(DEFAULT_LANGUAGE);
    protected String uiLanguage = DEFAULT_LANGUAGE;

    public String getUiLanguage() {
        return this.uiLanguage;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        try {
            if (locale != null && locale.length() == 2) {
                this.locale = new Locale(locale);
                if (VALID_UI_LANGUAGES.contains(locale)) {
                    this.uiLanguage = locale;
                } else {
                    this.uiLanguage = DEFAULT_LANGUAGE;
                }
            } else {
                this.locale = new Locale(DEFAULT_LANGUAGE);
                this.uiLanguage = DEFAULT_LANGUAGE;
            }
        } catch (Exception e) {
            this.locale = new Locale(DEFAULT_LANGUAGE);
            this.uiLanguage = DEFAULT_LANGUAGE;
        }
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        if (Constants.VALID_ENVIRONMENTS.contains(env)) {
            this.env = env;
        } else {
            this.env = "prod";
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHydra() {
        return hydra;
    }

    public void setHydra(String hydra) {
        this.hydra = hydra;
    }

    public String getRunId() {
        return runId;
    }

    public String getCube() {
        return Joiner.on(".").join(subject, hydra, cube, runId);
    }

    public void setCube(String cube) {
        this.cube = cube;
    }

    public void setRunId(String runId) {
        if (null != runId && runId.matches("\\d{17}")) {
            this.runId = runId;
        } else {
            this.runId = "latest";
        }
    }

    public String getTarget() {
        return cube;
    }

    public String toDataUrl() {
        return null;
    }
}
