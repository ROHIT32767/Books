package com.sismics.books.core.service.OnlineIntegration;

import org.json.JSONObject;

import com.sismics.books.core.dao.jpa.AudioBookDao;
import com.sismics.books.core.dao.jpa.PodcastDao;
import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.books.core.model.jpa.Podcast;

public class iTunesResourceFactory implements OnlineResourceFactory {

    @Override
    public AudioBook createAudioBooks(JSONObject itemObject) {
        AudioBookDao dao = new AudioBookDao();

        AudioBook audiobook = new AudioBook();
        audiobook.setExternalId(String.valueOf(itemObject.optInt("collectionId", 0)));

        // audiobook.setId(UUID.randomUUID().toString());
        audiobook.setName(itemObject.optString("collectionName", ""));
        audiobook.setDescription(itemObject.optString("description", ""));
        audiobook.setThumbnailUrl(itemObject.optString("artworkUrl100", ""));
        audiobook.setAuthors(itemObject.optString("artistName", ""));
        audiobook.setUrl(itemObject.optString("collectionViewUrl", ""));

        audiobook = dao.createIfNotExistAndReturn(audiobook);
        return audiobook;
    }

    @Override
    public Podcast createPodcasts(JSONObject itemObject) {
        PodcastDao dao = new PodcastDao();

        Podcast podcast = new Podcast();

        podcast.setExternalId(String.valueOf(itemObject.optInt("trackId", 0)));
        // podcast.setId(UUID.randomUUID().toString());
        podcast.setName(itemObject.optString("trackName", ""));
        podcast.setDuration(itemObject.optInt("trackTimeMillis", 0));
        podcast.setThumbnailUrl(itemObject.optString("artworkUrl100", ""));
        podcast.setUrl(itemObject.optString("trackViewUrl", ""));

        podcast = dao.createIfNotExistAndReturn(podcast);
        return podcast;
    }

}
