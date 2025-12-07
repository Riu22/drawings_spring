package com.drawings.drawings.dao;

import com.drawings.drawings.model.user;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class user_dao {

    private final JdbcTemplate jdbcTemplate;

    public user_dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<user> getUserRowMapper() {
        return (rs, rowNum) -> new user(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("password"),
                rs.getString("username")
        );
    }

    public void add_user(user user) {
        String sql = "INSERT INTO users (name, password, username) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, user.getName(), user.getPassword(), user.getUsername());
    }


    public void delete_user(int id){
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void update_user(user user){
        String sql = "UPDATE users SET name = ?, password = ?, username = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getPassword(), user.getUsername(), user.getId());
    }

    public user find_user(String username) {
        String sql = "SELECT id, name, password, username FROM users WHERE username = ?";

        try {
            return jdbcTemplate.queryForObject(sql, getUserRowMapper(), username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public int iduser(String username) throws EmptyResultDataAccessException {
        String sql = "SELECT id FROM users WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, username);
    }
    public boolean exists_by_username(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
}