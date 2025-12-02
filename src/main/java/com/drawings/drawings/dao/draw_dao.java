package com.drawings.drawings.dao;

import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.permissions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class draw_dao {
    private final JdbcTemplate  jdbcTemplate;
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
    public void add_permissions(){
        String sql = " INSERT INTO permissios (user_id, draw_id, can_read, can_write) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, 1, 1, true, true);
    }

    public void add_draw_content(int versionId, String drawContent){
        if (drawContent == null) {
            System.out.println("Intentando insertar NULL en draw_content para versionId: {}" + versionId);
            throw new IllegalArgumentException("El contenido del dibujo no puede ser nulo.");
        }
        System.out.println("Insertando contenido de dibujo. Longitud: {} caracteres." + drawContent.length());
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


}
