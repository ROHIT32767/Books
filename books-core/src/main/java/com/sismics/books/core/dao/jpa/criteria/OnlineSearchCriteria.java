package com.sismics.books.core.dao.jpa.criteria;

import com.sismics.books.core.constant.OnlineSearchContentProvider;
import com.sismics.books.core.constant.OnlineSearchContentType;

public class OnlineSearchCriteria {
    private OnlineSearchContentType contentType;
    private String query;
    private OnlineSearchContentProvider provider;

    public OnlineSearchContentType getContentType() {
        return contentType;
    }

    public void setContentType(OnlineSearchContentType contentType) {
        this.contentType = contentType;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public OnlineSearchContentProvider getProvider() {
        return provider;
    }

    public void setProvider(OnlineSearchContentProvider provider) {
        this.provider = provider;
    }
    
}
