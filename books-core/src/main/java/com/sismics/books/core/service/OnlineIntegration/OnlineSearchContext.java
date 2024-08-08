package com.sismics.books.core.service.OnlineIntegration;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sismics.books.core.constant.OnlineSearchContentProvider;
import com.sismics.books.core.constant.OnlineSearchContentType;
import com.sismics.books.core.dao.jpa.criteria.OnlineSearchCriteria;
import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.books.core.model.jpa.Podcast;

public class OnlineSearchContext {
    private OnlineSearchStrategy searchStrategy;

    public void setSearchStrategy(OnlineSearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public List<?> fetchResults(OnlineSearchCriteria criteria) {
        JSONArray resultItems = this.searchStrategy.fetchResults(criteria);

        OnlineResourceFactory factory = null;
        if (criteria.getProvider() == OnlineSearchContentProvider.SPOTIFY) {
            factory = new SpotifyResourceFactory();
        } else if (criteria.getProvider() == OnlineSearchContentProvider.I_TUNES) {
            factory = new iTunesResourceFactory();
        } else {
            throw new IllegalArgumentException("Unsupported onlince provider");
        }

        if(criteria.getContentType() == OnlineSearchContentType.AUDIO_BOOKS){
            List<AudioBook> results = new ArrayList<>();
            for (int i = 0; i < resultItems.length(); i++) {
                JSONObject itemObject = resultItems.getJSONObject(i);
                
                AudioBook item = factory.createAudioBooks(itemObject);
                results.add(item);
            }
            return results;
        } else if (criteria.getContentType() == OnlineSearchContentType.PODCASTS){
            List<Podcast> results = new ArrayList<>();
            for (int i = 0; i < resultItems.length(); i++) {
                JSONObject itemObject = resultItems.getJSONObject(i);
                
                Podcast item = factory.createPodcasts(itemObject);
                results.add(item);
            }
            return results;
        } else {
            // Handle the case where content type is not recognized
            throw new IllegalArgumentException("Unsupported content type");
        }    
    }
}
