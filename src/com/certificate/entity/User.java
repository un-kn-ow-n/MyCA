package com.certificate.entity;

public class User {
    private String id;
    private String password;
//    private boolean rootflag;

    public User(String id, String password) {
        this.id = id;
        this.password = password;
//        this.rootflag = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public boolean isRootflag() {
//        return rootflag;
//    }
//
//    public void setRootflag(boolean rootflag) {
//        this.rootflag = rootflag;
//    }

    @Override
    public String toString() {
        return id + " " + this.password;
    }
}
