package fi.thl.pivot.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import fi.thl.pivot.model.Tuple;

final class TupleMapper implements RowMapper<Tuple> {
    @Override
    public Tuple mapRow(ResultSet rs, int arg1) throws SQLException {
        Tuple t = new Tuple();
        t.subject = rs.getString("ref");
        t.predicate = rs.getString("tag");
        t.object = rs.getString("data");
        t.lang = rs.getString("lang");
        return t;
    }
}