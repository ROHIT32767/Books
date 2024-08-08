package com.sismics.books.core.service.OnlineIntegration;

import org.json.JSONArray;

import com.sismics.books.core.dao.jpa.criteria.OnlineSearchCriteria;


public interface OnlineSearchStrategy {
  public JSONArray  fetchResults(OnlineSearchCriteria criteria);
}
