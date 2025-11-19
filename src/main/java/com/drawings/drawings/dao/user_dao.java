package com.drawings.drawings.dao;

import com.drawings.drawings.model.user;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class user_dao {

    private final JdbcTemplate jdbcTemplate;

    public user_dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<user> getUserRowMapper() {
        return (rs, rowNum) -> new user(
                rs.getString("name"),
                rs.getString("password"),
                rs.getString("username")
        );
    }

    public void add_user(user user) {
        String sql = "INSERT INTO users (name, password, username) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, user.getName(), user.getPassword(), user.getUsername());
    }

    public user find_user(int id) {
        String sql = "SELECT id, name, password, username FROM users WHERE id = ?";

        return (user) jdbcTemplate.query(sql, getUserRowMapper(), id);
    }

    public void delete_user(int id){
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void update_user(user user){
        String sql = "UPDATE users SET name = ?, password = ?, username = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getPassword(), user.getUsername(), user.getId());
    }

    public List<user> findAll() {
        String sql = "SELECT id, name, password, username FROM users";
        return jdbcTemplate.query(sql, getUserRowMapper());
    }
}