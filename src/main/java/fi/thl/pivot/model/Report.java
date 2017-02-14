package fi.thl.pivot.model;

import java.util.Date;

public class Report {

    public static enum ReportType {
        CUBE, SUMMARY
    }

    private String subject, hydra, runId, fact;
    private ReportType type;
    private Date added;

    private Label title = new Label(), subjectTitle = new Label();
    private boolean isProtected;
    private String name;

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

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public String getFact() {
        return fact;
    }

    public void setFact(String fact) {
        this.fact = fact;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        this.title = title;
    }

    public Label getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(Label subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public void setIsProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public String toString() {
        return "\n\nReport [subject=" + subject + ", hydra=" + hydra + ", runId=" + runId + ", fact=" + fact + ", type=" + type + ", added=" + added + ", title="
                + title + ", subjectTitle=" + subjectTitle + ", isProtected=" + isProtected + "]";
    }

    public void setName(String string) {
       this.name = string;
    }
    
    public String getName() {
        return name;
    }

}
