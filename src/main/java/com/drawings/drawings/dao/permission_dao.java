package com.drawings.drawings.dao;

import com.drawings.drawings.model.permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class permission_dao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    private RowMapper<permissions> permissionRowMapper(){
        return (rs, rowNum) -> new permissions(
                rs.getInt("user_id"),
                rs.getInt("draw_id"),
                rs.getBoolean("can_read"),
                rs.getBoolean("can_write")
        );
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
}
