package com.imooc.action;

import org.apache.struts2.interceptor.validation.SkipValidation;

public class Login extends ExampleSupport {

    public String execute() throws Exception {
        return SUCCESS;
    }

    @SkipValidation
    public String form() throws Exception {
        return INPUT;
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}