package com.sismics.books.rest.resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.AudioBookDao;
import com.sismics.books.core.dao.jpa.PodcastDao;
import com.sismics.books.core.dao.jpa.UserAudioBookDao;
import com.sismics.books.core.dao.jpa.UserPodcastDao;
import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.books.core.model.jpa.Podcast;
import com.sismics.books.core.model.jpa.UserAudioBook;
import com.sismics.books.core.model.jpa.UserPodcast;
import com.sismics.rest.exception.ForbiddenClientException;

@Path("/service/favourite")
public class FavouritesResource extends BaseResource {
    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("audioBook")
    @Produces(MediaType.APPLICATION_JSON)
    public Response favouriteAudiobook (
        @FormParam("id") String audioBookId
    ) throws JSONException {

        JSONObject response = new JSONObject();
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String userId = this.principal.getId();

        System.out.println(userId);
        System.out.println(audioBookId);

        UserAudioBook userAudioBook = new UserAudioBook();
        userAudioBook.setAudioBookId(audioBookId);
        userAudioBook.setUserId(userId);

        UserAudioBookDao dao = new UserAudioBookDao();

        try{
            dao.create(userAudioBook);
            
            response.put("message", "AudioBook favourited successfully");
            return Response.ok().entity(response.toString()).build();  
        } catch(Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("audioBook/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfavouriteAudiobook (
        @PathParam("id") String audioBookId
    ) throws JSONException {

        JSONObject response = new JSONObject();
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String userId = this.principal.getId();

        System.out.println(userId);
        System.out.println(audioBookId);

        UserAudioBookDao dao = new UserAudioBookDao();
        dao.delete(userId, audioBookId);

        response.put("message", "AudioBook unfavourited successfully");
        return Response.ok().entity(response.toString()).build();  
    }

    @GET
    @Path("audioBook")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFavouriteAudiobooks() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String userId = this.principal.getId();

        UserAudioBookDao dao = new UserAudioBookDao();
        List<String> audioBookIdList = dao.findAudiobooksByUserId(userId);

        AudioBookDao abDao = new AudioBookDao();

        List<AudioBook> audioBookList = new ArrayList<>();

        for(String audioBookId : audioBookIdList){
            AudioBook audioBook = abDao.findById(audioBookId);
            
            audioBookList.add(audioBook);
        }
        System.out.println(audioBookIdList);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(audioBookList);

            // Return the JSON response
            return Response.ok(json)
                    .header("Content-Type", "application/json")
                    .build();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            return Response.serverError().build();
        }
    }

    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("podcast")
    @Produces(MediaType.APPLICATION_JSON)
    public Response favouritePodcast (
        @FormParam("id") String podcastId
    ) throws JSONException {

        JSONObject response = new JSONObject();
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String userId = this.principal.getId();

        System.out.println(userId);
        System.out.println(podcastId);

        UserPodcast userPodcast = new UserPodcast();
        userPodcast.setPodcastId(podcastId);
        userPodcast.setUserId(userId);

        UserPodcastDao dao = new UserPodcastDao();

        try {

            dao.create(userPodcast);
            
            response.put("message", "Podcast favourited successfully");
            return Response.ok().entity(response.toString()).build();  
        } catch (Exception e){
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("podcast/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfavouritePodcast (
        @PathParam("id") String podcastId
    ) throws JSONException {
        JSONObject response = new JSONObject();
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String userId = this.principal.getId();

        System.out.println(userId);
        System.out.println(podcastId);

        UserPodcastDao dao = new UserPodcastDao();
        dao.delete(userId, podcastId);

        response.put("message", "Podcast unfavourited successfully");
        return Response.ok().entity(response.toString()).build();  
    }

    @GET
    @Path("podcast")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFavouritePodcasts() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String userId = this.principal.getId();

        UserPodcastDao dao = new UserPodcastDao();
        List<String> podcastIdList = dao.findPodcastsByUserId(userId);

        PodcastDao pDao = new PodcastDao();

        List<Podcast> podcastList = new ArrayList<>();

        for(String podcastId : podcastIdList){
            Podcast podcast = pDao.findById(podcastId);
            
            podcastList.add(podcast);
        }
        System.out.println(podcastIdList);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(podcastList);

            System.out.println(json);
            
            // Return the JSON response
            return Response.ok(json)
                    .header("Content-Type", "application/json")
                    .build();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            return Response.serverError().build();
        }
    }


    // /**
    //  * Returns the information about a user.
    //  * 
    //  * @param username Username
    //  * @return Response
    //  * @throws JSONException
    //  */
    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response getFavourites(@PathParam("username") String username) throws JSONException {
    //     if (!authenticate()) {
    //         throw new ForbiddenClientException();
    //     }
    //     checkBaseFunction(BaseFunction.ADMIN);
                
    //     UserDao userDao = new UserDao();
    //     User user = userDao.getActiveByUsername(username);
    //     if (user == null) {
    //         throw new ClientException("UserNotFound", "The user doesn't exist");
    //     }
    //     String userId = user.getId();

    //     UserPodcastDao userPodcastDao = new UserPodcastDao();
    //     List<String> favoriteEpisodeIds = userPodcastDao.findEpisodesByUserId(userId);
    //     List<Episode> favoriteEpisodes = new ArrayList<>();

    //     PodcastDao podcastDao = new PodcastDao();
    //     for (String episodeId : favoriteEpisodeIds) {
    //         Episode episode = podcastDao.getById(episodeId);
    //         if (episode != null) {
    //             favoriteEpisodes.add(episode);
    //         }
    //     }

    //     ObjectMapper objectMapper = new ObjectMapper();
    //     try {
    //         String json = objectMapper.writeValueAsString(favoriteEpisodes);

    //         // Return the JSON response
    //         return Response.ok(json)
    //                 .header("Content-Type", "application/json")
    //                 .build();
    //     } catch (IOException e) {
    //         e.printStackTrace(); // Handle the exception according to your needs
    //         return Response.serverError().build();
    //     }
    // }
}
