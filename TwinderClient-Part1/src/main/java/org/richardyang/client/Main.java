package org.richardyang.client;

import io.swagger.client.model.SwipeDetails;
import org.richardyang.multiThread.Consumer;
import org.richardyang.multiThread.Producer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    /**
     * Base Url for Twinder
     */
    private static final String TWINDER_BASE_URL = "http://35.92.118.117:8080/Twinder";
    /**
     * Shared buffer queue size
     */
    private static final int BOUND = 10000;
    /**
     * Number of producer thread
     */
    private static final int N_PRODUCERS = 50;
    /**
     * Count that each producer produces
     */
    private static final int N_PRODUCER_COUNT = 10000;
    /**
     * Total count of requests
     */
    private static final int N_TOTAL = N_PRODUCERS * N_PRODUCER_COUNT;
    /**
     * Number of consumer thread
     */
    private static final int N_CONSUMERS = Runtime.getRuntime().availableProcessors() * 10;
//    private static final int N_CONSUMERS = 1;
    /**
     * Tomcat max thread 6000
     * Producer thread count: 1
     * Consumer thread count: 1
     * Number of successful requests sent: 10000
     * Number of unsuccessful requests: 0
     * The total run time for all phases to complete: 170s
     * The total throughput in requests per second: 58
     * Expected average latency(ms/req): 0.017, 17ms
     */
    private static final double AVG_LATENCY = 0.017;

    public static void main(String[] args) throws InterruptedException {

        final AtomicInteger producerCount = new AtomicInteger(N_PRODUCERS);
        final AtomicInteger consumeSuccessCount = new AtomicInteger(0);
        final AtomicInteger consumeFailedCount = new AtomicInteger(0);
        CountDownLatch completed = new CountDownLatch(N_TOTAL);

        BlockingQueue<SwipeDetails> queue = new LinkedBlockingQueue<>(BOUND);

        long startTime = System.nanoTime();
        for (int i = 0; i < N_PRODUCERS; i++) {
            new Thread(new Producer(queue, producerCount, N_PRODUCER_COUNT)).start();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Consumer
        /**
         * Producer thread count: 50
         * Consumer thread count: 80
         * Number of successful requests sent: 500000
         * Number of unsuccessful requests: 0
         * The total run time for all phases to complete: 115s
         * The total throughput in requests per second: 4347 vs expected 4705
         */
        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new Consumer(queue, completed, TWINDER_BASE_URL, consumeSuccessCount, consumeFailedCount)).start();
        }

        completed.await();

        // Console output
        long elapsedTimeNs = System.nanoTime() - startTime;

        System.out.println("Producer thread count: " + N_PRODUCERS);
        System.out.println("Consumer thread count: " + N_CONSUMERS);

        System.out.println("Number of successful requests sent: " + consumeSuccessCount.get());
        System.out.println("Number of unsuccessful requests: " + consumeFailedCount.get());
        long wallTime = TimeUnit.SECONDS.convert(elapsedTimeNs, TimeUnit.NANOSECONDS);
        System.out.println("The total run time for all phases to complete: " + wallTime + "s");
        System.out.println("The total throughput in requests per second: " + N_TOTAL / wallTime);

        double expThruPut = (double) N_CONSUMERS / AVG_LATENCY;
        System.out.printf("Expected throughput: %f\n\n", expThruPut);

        queue.clear();

        double latency = wallTime / (double) N_TOTAL;
        System.out.printf("Latency: %f\n", latency);
        double thruPut = (double) N_CONSUMERS / latency;
        // 17ms latency
        System.out.printf("Throughput using Little Laws: %f\n", thruPut);
    }
}

