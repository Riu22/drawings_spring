package com.drawings.drawings.dao;

import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.permissions;
import com.drawings.drawings.model.version;
import org.springframework.dao.DataAccessException;
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


    public List<draw> select_owners_draws(int userId){
        String sql = """
        SELECT id, user_id, title, created_at, ispublic
        FROM draw
        WHERE in_trash = FALSE
          AND id IN (
            SELECT id FROM draw WHERE user_id = ? 
            UNION
            SELECT draw_id FROM permissios WHERE user_id = ? AND (can_read = TRUE OR can_write = TRUE)
          )
        ORDER BY created_at DESC
        """;        return jdbcTemplate.query(sql, drawRowMapper(), userId, userId);
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
        String sql = "SELECT id, user_id, title, created_at, ispublic FROM draw WHERE ispublic = true AND in_trash = FALSE";
        return jdbcTemplate.query(sql, drawRowMapper());
    }

    public Optional<draw> select_draw_by_id(int drawId) {
        String sql = "SELECT id, user_id, title, created_at, ispublic FROM draw WHERE id = ? AND in_trash = FALSE";
        try {
            draw result = jdbcTemplate.queryForObject(sql, drawRowMapper(), drawId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<draw> select_trashed_draws(int user_id) {
        String sql = "SELECT id, user_id, title, created_at, ispublic FROM draw WHERE user_id = ? AND in_trash = TRUE";
        return jdbcTemplate.query(sql, drawRowMapper(), user_id);
    }

    public int delete_draw_by_id(int draw_id) {
        String sql = "DELETE FROM draw WHERE id = ?";
        return jdbcTemplate.update(sql, draw_id);
    }

    public int update_draw_to_trashed(int draw_id, int user_id) {
        String sql = "UPDATE draw SET in_trash = TRUE WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, draw_id, user_id);
    }

    public int rescue_draw_from_trash(int draw_id, int user_id) {
        String sql = "UPDATE draw SET in_trash = FALSE WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, draw_id, user_id);
    }

    public void save_or_update_permissions(int draw_id, int user_id, boolean can_read, boolean can_write) {
        String checkSql = "SELECT COUNT(*) FROM permissios WHERE draw_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, draw_id, user_id);

        if (count > 0) {
            String updateSql = "UPDATE permissios SET can_read = ?, can_write = ? WHERE draw_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, can_read, can_write, draw_id, user_id);
        } else {
            // INSERT: Si no existe, crea la entrada
            String insertSql = "INSERT INTO permissios (draw_id, user_id, can_read, can_write) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, draw_id, user_id, can_read, can_write);
        }
    }

    /**
     * Elimina los permisos de un usuario sobre un dibujo (si ambos permisos son falsos).
     */
    public int delete_permissions(int draw_id, int user_id) {
        String sql = "DELETE FROM permissios WHERE draw_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, draw_id, user_id);
    }

    public Optional<permissions> get_permissions_for_user(int draw_id, int user_id) {
        String sql = "SELECT user_id, draw_id, can_read, can_write FROM permissios WHERE draw_id = ? AND user_id = ?";
        try {
            permissions result = jdbcTemplate.queryForObject(sql, permissionRowMapper(), user_id, draw_id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    public boolean is_owner(int drawId, int userId) {
        String sql = "SELECT COUNT(*) FROM draw WHERE id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, drawId, userId);
        return count != null && count > 0;
    }

    public boolean get_can_write_permission(int drawId, int userId) {
        String sql = "SELECT can_write FROM permissios WHERE draw_id = ? AND user_id = ?";

        try {
            // queryForObject(String sql, Class<T> requiredType, Object... args)
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, drawId, userId));
        } catch (EmptyResultDataAccessException e) {
            // Si no hay entrada en la tabla permissios, no tiene permiso explícito.
            return false;
        }
    }

    public List<draw> select_viewable_draws(int userId) {
        String sql = """
    SELECT DISTINCT d.id, d.user_id, d.title, d.created_at, d.ispublic
    FROM draw d
    LEFT JOIN permissios p ON d.id = p.draw_id AND p.user_id = ?
    WHERE d.in_trash = FALSE
      AND (
        d.user_id = ?
        OR (p.can_read = TRUE OR p.can_write = TRUE)
      )
    ORDER BY d.created_at DESC
    """;

        return jdbcTemplate.query(sql, drawRowMapper(), userId, userId);
    }


    /**
     * Actualiza el título y el estado público de un dibujo existente
     */
    public int update_draw_metadata(int draw_id, String title, boolean ispublic) {
        String sql = "UPDATE draw SET title = ?, ispublic = ? WHERE id = ?";
        return jdbcTemplate.update(sql, title, ispublic, draw_id);
    }

    public List<version> select_all_versions_by_draw_id(int draw_id) {
        String sql = "SELECT id, draw_id, version_number, created_at FROM version WHERE draw_id = ? ORDER BY version_number DESC";
        return jdbcTemplate.query(sql, versionRowMapper(), draw_id);
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

    public String select_autor_by_id(int user_id){
        String sql = "SELECT username FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, user_id);

    }

}