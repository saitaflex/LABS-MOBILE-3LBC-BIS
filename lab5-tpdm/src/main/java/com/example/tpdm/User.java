package com.example.tpdm;

/** Model: one row of the user table. */
public class User {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_EMPLOYEE = "Employee";

    private String username, fname, lname, cin, pwd, role;

    public User() {
    }

    public User(String username, String fname, String lname, String cin, String pwd, String role) {
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.cin = cin;
        this.pwd = pwd;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
