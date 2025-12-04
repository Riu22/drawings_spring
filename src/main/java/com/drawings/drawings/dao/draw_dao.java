package com.drawings.drawings.dao;

import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.permissions;
import com.drawings.drawings.model.version;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.util.List; // Importado para select_owners_draws
import java.util.Optional;

@Repository
public class draw_dao {
    private final JdbcTemplate jdbcTemplate;

    public draw_dao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    private RowMapper<draw> drawRowMapper(){
        return (rs, rowNum) -> new draw(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getTimestamp("created_at"),
                rs.getBoolean("ispublic")
        );
    }

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

    private RowMapper<permissions> permissionRowMapper(){
        return (rs, rowNum) -> new permissions(
                rs.getInt("user_id"),
                rs.getInt("draw_id"),
                rs.getBoolean("can_read"),
                rs.getBoolean("can_write")
        );
    }


    public draw add_draw(draw newDraw){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO draw (user_id, title, ispublic) VALUES (?, ?, ?)";

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setInt(1, newDraw.getUser_id());
            ps.setString(2, newDraw.getTitle());
            ps.setBoolean(3, newDraw.isPublic());
            return ps;
        }, keyHolder);

        // Assignacion del id generado
        Optional.ofNullable(keyHolder.getKey()).ifPresent(key ->
                newDraw.setId(key.intValue())
        );

        return newDraw;
    }

    public void add_draw_content(int versionId, String drawContent){
        String sql = "INSERT INTO draw_data (version_id, draw_content) VALUES (?, ?)";
        jdbcTemplate.update(sql, versionId, drawContent);
    }

    public int add_version(int drawId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO version (draw_id, version_number) VALUES (?, 1)";

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setInt(1, drawId);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }


    public List<draw> select_owners_draws(int userId){
        String sql ="SELECT id, user_id, title, created_at, ispublic FROM draw WHERE user_id = ?";
        return jdbcTemplate.query(sql, drawRowMapper(), userId);
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

    public List<draw> select_public_draws(){
        String sql = "SELECT id, user_id, title, created_at, ispublic FROM draw WHERE ispublic = true";
        return jdbcTemplate.query(sql, drawRowMapper());
    }

    public Optional<draw> select_draw_by_id(int drawId) {
        String sql = "SELECT id, user_id, title, created_at, ispublic FROM draw WHERE id = ?";
        try {
            draw result = jdbcTemplate.queryForObject(sql, drawRowMapper(), drawId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}