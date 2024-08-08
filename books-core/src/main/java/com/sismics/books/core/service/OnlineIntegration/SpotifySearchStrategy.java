package com.sismics.books.core.service.OnlineIntegration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sismics.books.core.constant.OnlineSearchContentType;
import com.sismics.books.core.dao.jpa.criteria.OnlineSearchCriteria;

public class SpotifySearchStrategy implements OnlineSearchStrategy {

    private String accessToken=null;

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String CLIENT_ID = "5ec2313ea7b3423a9ed28c5060b3ad41";
    private static final String CLIENT_SECRET = "31ca4c3d2bcc460da3fd2e7953230933";

    private static int limit = 20;
    private static int offset = 0;
    private static String market = "US";

    @Override
    public JSONArray fetchResults(OnlineSearchCriteria criteria){
        // List<AudioBook> result = new ArrayList<>();
        String type = null;
        if(criteria.getContentType() == OnlineSearchContentType.AUDIO_BOOKS){
            type = "audiobook";
        }else if(criteria.getContentType() == OnlineSearchContentType.PODCASTS){
            type = "episode";
        }
        String encodedQuery = encodeQuery(criteria.getQuery());
        String apiUrl = "https://api.spotify.com/v1/search" + "?q=" + encodedQuery + "&type="+ type + "&market=" + market + "&limit=" + limit + "&offset=" + offset;

        String jsonResponse = performHttpRequest(apiUrl);

        JSONObject response = new JSONObject(jsonResponse);

        // AudioBookDao dao = new AudioBookDao();

        if (response.has(type+"s")) {
            JSONObject responseObject = response.getJSONObject(type+"s");
            JSONArray itemsArray = responseObject.getJSONArray("items");

            return itemsArray;
        }
        return new JSONArray();
    }

    public static String encodeQuery(String query) {
        System.out.println("Inside encode query: "+query);
        try {
            return URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String performHttpRequest(String url) {
        if (accessToken == null) {
            obtainAccessToken();
        }

        try {
            // Create a URL object with the specified URL
            URL apiUrl = new URL(url);
    
            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
    
            // Set the request method to GET
            connection.setRequestMethod("GET");
    
            // Add the authorization header with the access token
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
    
            // Get the response code
            int responseCode = connection.getResponseCode();
    
            // If the response code is HTTP_OK (200), read the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
    
                // Read each line of the response and append it to the StringBuilder
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
    
                // Close the BufferedReader
                reader.close();
                connection.disconnect();
    
                // Return the response as a String
                return response.toString();
            } else {
                // If the response code is not HTTP_OK, handle the error
                System.out.println("HTTP request failed with response code: " + responseCode);
                connection.disconnect();
    
                return "";
            }
    
            // Close the connection
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void obtainAccessToken() {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String postData = "grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                accessToken = jsonResponse.getString("access_token");
                System.out.println(accessToken);
            } else {
                System.err.println("Failed to obtain access token. Response code: " + responseCode);
                accessToken = null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            accessToken = null;
        }
    }
}
