package com.sismics.books.rest.resource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseBuilder {
    private JSONObject response;
    private static final Logger logger = Logger.getLogger(ResponseBuilder.class.getName());

    public ResponseBuilder() {
        this.response = new JSONObject();
    }

    /**
     * Adds a key-value pair to the JSON response. Ignores the key if the value is
     * null.
     *
     * @param key   The JSON key.
     * @param value The value associated with the key.
     * @return The ResponseBuilder instance for chaining.
     */
    public ResponseBuilder add(String key, Object value) {
        try {
            this.response.put(key, value);
        } catch (JSONException e) {
            // Log with more context and at SEVERE level indicating a serious failure.
            logger.log(Level.SEVERE, "JSONException occurred while adding key '" + key + "' with value '" + value
                    + "' to JSON response.", e);
        }
        return this;
    }

   
    /**
     * Builds the JSONObject.
     *
     * @return The constructed JSONObject.
     */
    public JSONObject build() {
        return this.response;
    }

    /**
     * Builds the response as a JAX-RS Response object.
     * This can be used to directly return a Response from your JAX-RS resource
     * methods.
     *
     * @return A JAX-RS Response object containing the JSON.
     */
    public Response buildResponse() {
        return Response.ok(this.response.toString()).build();
    }
}
