package com.sismics.books.core.service.OnlineIntegration;

import org.json.JSONObject;

import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.books.core.model.jpa.Podcast;

public interface OnlineResourceFactory {
  public AudioBook createAudioBooks(JSONObject itemObject);
  public Podcast createPodcasts(JSONObject itemObject);
}
