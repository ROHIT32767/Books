package com.sismics.books.core.dao.jpa;
import com.sismics.books.core.dao.jpa.criteria.CommonBookCriteria;
import com.google.common.base.Strings;
public class DefaultSQLQueryBuilder implements SQLQueryBuilder {
    @Override
    public String buildQuery(CommonBookCriteria criteria) {
        StringBuilder sb = new StringBuilder(
            "SELECT cb.COMMON_BOOK_ID c0, b.BOK_ID_C c1, cb.CREATE_DATE c2, cb.READ_DATE c3");
        sb.append(" FROM T_BOOK b ");
        sb.append(" JOIN T_COMMON_BOOK cb ON cb.BOOK_ID = b.BOK_ID_C AND cb.DELETE_DATE IS NULL ");

        if (!Strings.isNullOrEmpty(criteria.getSearch())) {
            sb.append(" WHERE (b.BOK_TITLE_C LIKE :search OR b.BOK_SUBTITLE_C LIKE :search OR b.BOK_AUTHOR_C LIKE :search) ");
        }
        if (criteria.getRead() != null) {
            sb.append(" AND cb.READ_DATE IS " + (criteria.getRead() ? "NOT" : "") + " NULL ");
        }
        return sb.toString();
    }
}