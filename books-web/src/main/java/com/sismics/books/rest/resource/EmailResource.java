package com.sismics.books.rest.resource;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.UserDao;

/**
 * User REST resources for CRUD Operations on User.
 * 
 * @author jtremeaux
 */
@Path("/check_email")
public class EmailResource extends BaseResource {
    private String DEFAULT_ADMIN_PASSWORD = "$2a$05$6Ny3TjrW3aVAL1or2SlcR.fhuDgPKp5jp.P9fBXwVNePgeLqb4i3C";

    /**
     * Checks if a email is available. Search only on active accounts.
     * 
     * @param email email to check
     * @return Response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkEmail(
        @FormParam("email") String email) throws JSONException {
        System.out.println("Inside validation function " + email);

        UserDao userDao = new UserDao();
        Boolean isUsed = userDao.isEmailInUse(email);
        
        JSONObject response = new JSONObject();
        response.put("isUsed", isUsed);
        
        return Response.ok().entity(response).build();
    }
}
