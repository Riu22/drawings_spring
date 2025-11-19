package com.drawings.drawings.model;



public class user {
    int id;
    String name;
    String password;
    String username;

    public user(String name, String password, String username){
        this.name = name;
        this.password = password;
        this.username = username;
    }

    public user(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
