package com.sismics.books.rest.resource;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;

import com.sismics.books.core.constant.OnlineSearchContentProvider;
import com.sismics.books.core.constant.OnlineSearchContentType;
import com.sismics.books.core.dao.jpa.criteria.OnlineSearchCriteria;
import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.books.core.model.jpa.Podcast;
import com.sismics.books.core.service.OnlineIntegration.OnlineSearchContext;
import com.sismics.books.core.service.OnlineIntegration.OnlineSearchStrategy;
import com.sismics.books.core.service.OnlineIntegration.SpotifySearchStrategy;
import com.sismics.books.core.service.OnlineIntegration.iTunesSearchStrategy;
import com.sismics.rest.exception.ForbiddenClientException;

@Path("/service/connect")
public class OnlineIntegrationResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response connect(
            @QueryParam("service") String service,
            @QueryParam("search") String search,
            @QueryParam("contentType") String contentType
        ) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String query = search;
        System.out.println(query);
        System.out.println(service);
        System.out.println(contentType);

        OnlineSearchCriteria criteria = new OnlineSearchCriteria();
        if (contentType.equalsIgnoreCase("Audio Books")) {
            criteria.setContentType(OnlineSearchContentType.AUDIO_BOOKS);
        } else {
            criteria.setContentType(OnlineSearchContentType.PODCASTS);
        }
        if(service.equalsIgnoreCase("spotify")) {
            criteria.setProvider(OnlineSearchContentProvider.SPOTIFY);
        } else {
            criteria.setProvider(OnlineSearchContentProvider.I_TUNES);
        }
        criteria.setQuery(query);

        OnlineSearchContext searchContext = new OnlineSearchContext();
        if (criteria.getProvider() == OnlineSearchContentProvider.SPOTIFY) {
            OnlineSearchStrategy strategy = new SpotifySearchStrategy();
            searchContext.setSearchStrategy(strategy);
        } else if (criteria.getProvider() == OnlineSearchContentProvider.I_TUNES) {
            OnlineSearchStrategy strategy = new iTunesSearchStrategy();
            searchContext.setSearchStrategy(strategy);
        }

        List<?> resultList = searchContext.fetchResults(criteria);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(resultList);

            // Return the JSON response
            return Response.ok(json)
                    .header("Content-Type", "application/json")
                    .build();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            return Response.serverError().build();
        }
    }
}
