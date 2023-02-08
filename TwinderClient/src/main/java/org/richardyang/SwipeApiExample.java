package org.richardyang;

import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SwipeApi;

import java.io.File;
import java.util.*;

public class SwipeApiExample {

    private static final String TWINDER_BASE_URL = "http://35.86.108.111:8080/Twinder";

    public static void main(String[] args) {

        SwipeApi apiInstance = new SwipeApi();
        apiInstance.getApiClient().setBasePath(TWINDER_BASE_URL);
        SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
        body.setSwiper("233");
        body.setSwipee("21345");
        String leftorright = "left"; // String | Ilike or dislike user
        try {
            ApiResponse res = apiInstance.swipeWithHttpInfo(body, leftorright);
            System.out.println(res.getStatusCode());
        } catch (ApiException e) {
            System.err.println("Exception when calling SwipeApi#swipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
