package com.sismics.books.core.service.OnlineIntegration;

import org.json.JSONObject;

import com.sismics.books.core.dao.jpa.AudioBookDao;
import com.sismics.books.core.dao.jpa.PodcastDao;
import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.books.core.model.jpa.Podcast;

public class SpotifyResourceFactory implements OnlineResourceFactory {

    @Override
    public AudioBook createAudioBooks(JSONObject itemObject) {
        AudioBookDao dao = new AudioBookDao();

        AudioBook audioBook = new AudioBook();

        audioBook.setExternalId(itemObject.optString("id"));
        audioBook.setName(itemObject.optString("name"));
        audioBook.setDescription(itemObject.optString("description"));
        audioBook.setUrl(itemObject.optJSONObject("external_urls").optString("spotify"));
        audioBook.setThumbnailUrl(itemObject.optJSONArray("images").getJSONObject(0).optString("url"));
        audioBook.setAuthors(itemObject.optJSONArray("authors").getJSONObject(0).optString("name") + ", "
                + itemObject.optJSONArray("narrators").getJSONObject(0).optString("name"));

        audioBook = dao.createIfNotExistAndReturn(audioBook);
        return audioBook;
    }

    @Override
    public Podcast createPodcasts(JSONObject itemObject) {
        PodcastDao dao = new PodcastDao();

        Podcast podcast = new Podcast();
        podcast.setExternalId(itemObject.optString("id"));
        // podcast.setId(UUID.randomUUID().toString());
        podcast.setDuration(itemObject.optInt("duration_ms", 0) / 1000);
        podcast.setName(itemObject.optString("name", ""));
        podcast.setThumbnailUrl(itemObject.optJSONArray("images").getJSONObject(0).optString("url"));
        podcast.setUrl(itemObject.optJSONObject("external_urls").optString("spotify"));

        podcast = dao.createIfNotExistAndReturn(podcast);
        return podcast;
    }

}
