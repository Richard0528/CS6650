package org.richardyang;

import io.swagger.client.model.SwipeDetails;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.richardyang.multiThread.Consumer;
import org.richardyang.multiThread.ConsumerWithMetric;
import org.richardyang.multiThread.Producer;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    /**
     * Base Url for Twinder
     */
    private static final String TWINDER_BASE_URL = "http://54.203.156.202:8080/Twinder";
    /**
     * Shared buffer queue size
     */
    private static final int BOUND = 5000;
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

    /**
     * Producer thread count: 50
     * Consumer thread count: 80
     * Number of successful requests sent: 500000
     * Number of unsuccessful requests: 0
     * The total run time for all phases to complete: 118s
     * The total throughput in requests per second: 4237
     * Expected throughput: 4705.882353
     *
     * @param records
     * @throws IOException
     */
    private static void write(ConcurrentLinkedQueue<String> records) throws IOException {

        File file = File.createTempFile("metricOutput", ".csv", new File("/Users/richardyang/Downloads/metricOutput"));

        FileWriter writer = new FileWriter(file);
//        BufferedWriter writer = new BufferedWriter(new FileWriter(file), 4194304);

        for (String record: records) {
            writer.write(record);
        }
        writer.close();
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        final AtomicInteger producerCount = new AtomicInteger(N_PRODUCERS);
        final AtomicInteger consumeSuccessCount = new AtomicInteger(0);
        final AtomicInteger consumeFailedCount = new AtomicInteger(0);
        CountDownLatch completed = new CountDownLatch(N_TOTAL);

        BlockingQueue<SwipeDetails> queue = new LinkedBlockingQueue<>(BOUND);
        // start time, request type (ie POST), latency, response code
        ConcurrentLinkedQueue<String> writeOut = new ConcurrentLinkedQueue<>();

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
//        for (int j = 0; j < N_CONSUMERS; j++) {
//            new Thread(new Consumer(queue, completed, TWINDER_BASE_URL, consumeSuccessCount, consumeFailedCount)).start();
//        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Consumer with Metric
        /**
         * Producer thread count: 50
         * Consumer thread count: 80
         * Number of successful requests sent: 500000
         * Number of unsuccessful requests: 0
         * The total run time for all phases to complete: 116s
         * The total throughput in requests per second: 4310 vs 437 from previous
         */
        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new ConsumerWithMetric(queue, completed, TWINDER_BASE_URL, consumeSuccessCount, consumeFailedCount, writeOut)).start();
        }

        completed.await();

        // Write to csv file
        write(writeOut);

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

        double[] out = writeOut
                .stream()
                .map(line -> {
                    String[] tmp = line.split(",");
                    return Double.valueOf(tmp[2]);
                })
                .mapToDouble(x -> x)
                .toArray();

        DescriptiveStatistics stat = new DescriptiveStatistics(out);

        /**
         * mean response time (millisecs): 18.611827999999807
         * median response time (millisecs): 18.0
         * throughput = total number of requests/wall time (requests/second): 4310
         * p99 (99th percentile) response time: 38.0
         * min response time (millisecs): 11.0
         * max response time (millisecs): 282.0
         */
        System.out.println("mean response time (millisecs): " + stat.getMean());
        System.out.println("median response time (millisecs): " + stat.getPercentile(50));
        System.out.println("throughput = total number of requests/wall time (requests/second): " + N_TOTAL / wallTime);
        System.out.println("p99 (99th percentile) response time: " + stat.getPercentile(99));
        System.out.println("min response time (millisecs): " + stat.getMin());
        System.out.println("max response time (millisecs): " + stat.getMax());
    }
}
