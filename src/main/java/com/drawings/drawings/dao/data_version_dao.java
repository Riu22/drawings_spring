package com.drawings.drawings.dao;

import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class data_version_dao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    RowMapper<version> versionRowMapper(){
        return (rs, rowNum) -> new version(
                rs.getInt("id"),
                rs.getInt("version_number"),
                rs.getTimestamp("created_at"),
                rs.getInt("draw_id")
        );
    }

    private RowMapper<draw_data> drawDataRowMapper(){
        return (rs, rowNum) -> new draw_data(
                rs.getInt("version_id"),
                rs.getString("draw_content")
        );
    }

    public void add_draw_content(int versionId, String drawContent){
        String sql = "INSERT INTO draw_data (version_id, draw_content) VALUES (?, ?)";
        jdbcTemplate.update(sql, versionId, drawContent);
    }

    public int add_version(int draw_id, int version_number) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO version (draw_id, version_number) VALUES (?, ?)";

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setInt(1, draw_id);
            ps.setInt(2, version_number);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    public int get_latest_version_number(int draw_id) {
        String sql = "SELECT MAX(version_number) FROM version WHERE draw_id = ?";
        Integer max_version = jdbcTemplate.queryForObject(sql, Integer.class, draw_id);
        return max_version != null ? max_version : 0;
    }

    public version select_latest_draw_version(int draw_id){
        String sql = "SELECT id, draw_id, version_number, created_at FROM version WHERE draw_id = ? ORDER BY version_number DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, versionRowMapper(), draw_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public draw_data select_draw_data(int version_id){
        String sql = "SELECT version_id, draw_content FROM draw_data WHERE version_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, drawDataRowMapper(), version_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Optional<version> select_version_by_number(int draw_id, int version_number) {
        String sql = "SELECT id, draw_id, version_number, created_at FROM version WHERE draw_id = ? AND version_number = ?";
        try {
            version result = jdbcTemplate.queryForObject(sql, versionRowMapper(), draw_id, version_number);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
