package com.sismics.books.core.util;

import java.util.Date;

import com.sismics.books.core.model.jpa.User;

public class UserBuilder {
    private static String DEFAULT_USER_ROLE = "user";
    private static String DEFAULT_LOCALE_ID = "en";

    private User product;

    public UserBuilder() {
        reset();
        product.setCreateDate(new Date());
        product.setFirstConnection(true);
        product.setRoleId(DEFAULT_USER_ROLE);
        product.setLocaleId(DEFAULT_LOCALE_ID);
    }

    public void reset(){
        product = new User();
    }
    public UserBuilder withId(String id) {
        product.setId(id);
        return this;
    }

    public UserBuilder withLocaleId(String localeId) {
        product.setLocaleId(localeId);
        return this;
    }

    public UserBuilder withRoleId(String roleId) {
        product.setRoleId(roleId);
        return this;
    }

    public UserBuilder withUsername(String username) {
        product.setUsername(username);
        return this;
    }

    public UserBuilder withPassword(String password) {
        product.setPassword(password);
        return this;
    }

    public UserBuilder withEmail(String email) {
        product.setEmail(email);
        return this;
    }

    public UserBuilder withTheme(String theme) {
        product.setTheme(theme);
        return this;
    }

    public UserBuilder withFirstConnection(boolean firstConnection) {
        product.setFirstConnection(firstConnection);
        return this;
    }

    public UserBuilder withCreateDate(Date createDate) {
        product.setCreateDate(createDate);
        return this;
    }

    public User build() {
        return product;
    }
}
