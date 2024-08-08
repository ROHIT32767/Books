package com.sismics.books.core.dao.jpa.criteria;

/**
 * User's contact search criteria.
 * 
 * @author jtremeaux
 */
public class UserContactCriteria {

    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Fulltext query.
     */
    private String query;

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of query.
     *
     * @return query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Setter of query.
     *
     * @param query query
     */
    public void setQuery(String query) {
        this.query = query;
    }
}
