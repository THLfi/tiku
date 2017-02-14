package fi.thl.pivot.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import fi.thl.pivot.model.Report;
import fi.thl.pivot.model.Report.ReportType;

/**
 * {@link RowMapper} that generates a report descriptor object based on a jdbc
 * result set. 
 * 
 * @author aleksiyrttiaho
 *
 */
class ReportMapper implements RowMapper<Report> {
    @Override
    public Report mapRow(ResultSet rs, int arg1) throws SQLException {
        Report r = new Report();
        r.setSubject(rs.getString("subject"));
        r.setHydra(rs.getString("hydra"));
        r.setFact(rs.getString("logical_name"));
        r.setAdded(rs.getTimestamp("added_meta_hydra"));
        r.setRunId(rs.getString("run_id"));
        r.setName(rs.getString("name"));
        if (rs.getMetaData().getColumnCount() > 5) {
            ReportType viewType;
            if ("cube".equals(rs.getString("view_type"))) {
                viewType = ReportType.CUBE;
            } else {
                viewType = ReportType.SUMMARY;
            }
            r.setType(viewType);
        }
        return r;
    }
}