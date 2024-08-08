package com.sismics.books.core.dao.jpa;
import com.sismics.books.core.dao.jpa.criteria.CommonBookCriteria;
public class FilteredSQLQueryBuilder implements SQLQueryBuilder {
    @Override
    public String buildQuery(CommonBookCriteria criteria) {
        StringBuilder sb = new StringBuilder(
            "SELECT cb.COMMON_BOOK_ID c0, b.BOK_ID_C c1, cb.CREATE_DATE c2 , cb.READ_DATE c3, b.BOK_AUTHOR_C c4");
        sb.append(", (SELECT AVG(CAST(cr.RATING AS FLOAT)) FROM COMMON_BOOK_RATING cr WHERE cr.COMMON_BOOK_ID = cb.COMMON_BOOK_ID) c5");
        sb.append(", (SELECT GROUP_CONCAT(genre SEPARATOR ',') FROM common_book_genre WHERE common_book_id = cb.COMMON_BOOK_ID) AS c6");
        sb.append(" FROM T_BOOK b ");
        sb.append(" JOIN T_COMMON_BOOK cb ON cb.BOOK_ID = b.BOK_ID_C AND cb.DELETE_DATE IS NULL ");
        return sb.toString();
    }
}