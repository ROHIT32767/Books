package com.sismics.books.core.dao.jpa;

public interface BaseDao<T> {
    String create(T entity);
    void delete(String id);
    T getById(String id);
}
