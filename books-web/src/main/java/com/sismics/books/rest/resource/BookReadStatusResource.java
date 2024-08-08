package com.sismics.books.rest.resource;

import java.util.Date;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.rest.exception.ForbiddenClientException;

/**
 * Book read status resource.
 * 
 * @version 0.1.0
 * @since 0.1.0
 */
@Path("/book/{id: [a-z0-9\\\\-]+}/read")
public class BookReadStatusResource extends BaseResource{
        /**
     * Set a book as read/unread.
     * 
     * @param id User book ID
     * @param read Read state
     * @return Response
     * @throws JSONException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") final String userBookId,
            @FormParam("read") boolean read) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        
        // Update the read date
        userBook.setReadDate(read ? new Date() : null);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
