package org.richardyang.multiThread;

import org.richardyang.http.SimpleClient;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class GetThread implements Runnable {

    private final ConcurrentLinkedQueue<Double> latencies;
    private final SimpleClient matchesApi;
    private final SimpleClient statsApi;

    public GetThread(ConcurrentLinkedQueue<Double> latencies, String baseUrl) {
        this.latencies = latencies;
        this.matchesApi = new SimpleClient(baseUrl + "/matches/");
        this.statsApi = new SimpleClient(baseUrl + "/stats/");
    }

    public void run() {

        int option = ThreadLocalRandom.current().nextInt(2);
        String userId = "" + ThreadLocalRandom.current().nextInt(1, 50001);

        long startTime = System.currentTimeMillis();

        try {

            if (option == 0) {
                matchesApi.call(userId);
            } else {
                statsApi.call(userId);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long wallTime = System.currentTimeMillis() - startTime;
//        System.out.println(wallTime);

        latencies.add((double) wallTime);
    }
}
