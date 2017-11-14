package hku.cs.smp.guardian.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CallsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    void tag(String number, String tag) {
        jdbcTemplate.query("SELECT guardian.tag_call(?, ?);", new String[]{
                number, tag
        }, rs -> {
        });
    }

    Map<String, Integer> inquiry(String number) {
        return jdbcTemplate.query("SELECT calls.type, calls.time " +
                        "FROM  guardian.calls " +
                        "WHERE guardian.calls.number = ?",
                new String[]{number},
                rs -> {
                    Map<String, Integer> result = new HashMap<>();
                    while (rs.next()) {
                        result.put(
                                rs.getString(rs.findColumn("type")),
                                rs.getInt(rs.findColumn("time")));
                    }
                    return result;
                });
    }
}
