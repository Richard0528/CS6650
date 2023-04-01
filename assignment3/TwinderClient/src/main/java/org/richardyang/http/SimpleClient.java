package org.richardyang.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleClient {

    private String baseUrl;

    public SimpleClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String call(String userId) throws IOException {
        URL obj = new URL(baseUrl + userId);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        // uncomment the following if we want to print response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
