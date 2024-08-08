package com.sismics.books.core.service.OnlineIntegration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sismics.books.core.constant.OnlineSearchContentType;
import com.sismics.books.core.dao.jpa.criteria.OnlineSearchCriteria;

public class iTunesSearchStrategy implements OnlineSearchStrategy {
    public static String encodeQuery(String query) {
        try {
            return URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONArray fetchResults(OnlineSearchCriteria criteria) {
        String type = null;
        if(criteria.getContentType() == OnlineSearchContentType.AUDIO_BOOKS){
            type = "audiobook";
        }else if(criteria.getContentType() == OnlineSearchContentType.PODCASTS){
            type = "podcast";
        }
        String encodedQuery = encodeQuery(criteria.getQuery());

        String apiUrl = "https://itunes.apple.com/search?term=" + encodedQuery + "&entity=" + type;

        // Perform the HTTP request and get the JSON response
        String jsonResponse = performHttpRequest(apiUrl);

        // Parse the JSON response
        JSONObject response = new JSONObject(jsonResponse);

        if (response.has("results")) {
            JSONArray resultsArray = response.getJSONArray("results");
            return resultsArray;
        }
        return new JSONArray();
    }

    private String performHttpRequest(String url) {
        try {
            // Create a URL object with the specified URL
            URL apiUrl = new URL(url);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

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
}
