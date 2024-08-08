package com.sismics.books.core.dao.jpa;
import com.sismics.books.core.dao.jpa.criteria.CommonBookCriteria;
public class RatingSQLQueryBuilder implements SQLQueryBuilder {
    @Override
    public String buildQuery(CommonBookCriteria criteria) {
        StringBuilder sb = new StringBuilder(
            "SELECT cb.COMMON_BOOK_ID c0, b.BOK_ID_C c1, cb.CREATE_DATE c2 , cb.READ_DATE c3");
        sb.append(", (SELECT AVG(CAST(cr.RATING AS FLOAT)) FROM COMMON_BOOK_RATING cr WHERE cr.COMMON_BOOK_ID = cb.COMMON_BOOK_ID) c4");
        sb.append(", (SELECT COUNT(*) FROM COMMON_BOOK_RATING cr WHERE cr.COMMON_BOOK_ID = cb.COMMON_BOOK_ID) c5");
        sb.append(" FROM T_BOOK b ");
        sb.append(" JOIN T_COMMON_BOOK cb ON cb.BOOK_ID = b.BOK_ID_C AND cb.DELETE_DATE IS NULL ");
        return sb.toString();
    }
}