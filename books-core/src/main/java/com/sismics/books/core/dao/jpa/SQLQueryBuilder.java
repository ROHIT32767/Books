package com.sismics.books.core.dao.jpa;
import com.sismics.books.core.dao.jpa.criteria.CommonBookCriteria;
public interface SQLQueryBuilder {
    String buildQuery(CommonBookCriteria criteria);
}