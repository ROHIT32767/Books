package com.sismics.books.core.dao.jpa.criteria;


/**
 * Common book criteria.
 *
 * @author bgamard 
 */
public class CommonBookCriteria {
    /**
     * CommonBook ID.
     */
    private String commonBookId;
    
    /**
     * Search query.
     */
    private String search;
    
    /**
     * Read state.
     */
    private Boolean read;
    
    
    /**
     * Getter of commonBookId.
     *
     * @return commonBookId
     */
    public String getCommonBookId() {
        return commonBookId;
    }

    /**
     * Setter of commonBookId.
     *
     * @param commonBookId commonBookId
     */
    public void setCommonBookId(String commonBookId) {
        this.commonBookId = commonBookId;
    }

    /**
     * Getter of search.
     *
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Setter of search.
     *
     * @param search search
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Getter of read.
     * @return read
     */
    public Boolean getRead() {
        return read;
    }

    /**
     * Setter of read.
     * @param read read
     */
    public void setRead(Boolean read) {
        this.read = read;
    }
}
