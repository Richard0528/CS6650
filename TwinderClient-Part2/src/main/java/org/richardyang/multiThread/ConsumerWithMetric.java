package org.richardyang.multiThread;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import org.richardyang.model.PoisonPill;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerWithMetric implements Runnable {
    private final String[] leftOrRight = new String[] {"left", "right"};
    private final BlockingQueue<SwipeDetails> queue;
    private final CountDownLatch completed;
    private final AtomicInteger consumeSuccessCount;
    private final AtomicInteger consumeFailedCount;
    private final ConcurrentLinkedQueue<String> writeOut;
    private final SwipeApi apiInstance;

    public ConsumerWithMetric(BlockingQueue<SwipeDetails> queue,
                    CountDownLatch completed,
                    String baseUrl,
                    AtomicInteger consumeSuccessCount,
                    AtomicInteger consumeFailedCount,
                    ConcurrentLinkedQueue<String> writeOut) {
        this.queue = queue;
        this.completed = completed;
        this.consumeSuccessCount = consumeSuccessCount;
        this.consumeFailedCount = consumeFailedCount;
        this.writeOut = writeOut;

        apiInstance = new SwipeApi(new ApiClient().setBasePath(baseUrl));
    }

    public void run() {
        StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                SwipeDetails body = queue.take();
                if (body instanceof PoisonPill) {
                    queue.put(body);
                    return;
                }

                int retries = 0;

                do {

                    try {
                        long startTime = System.currentTimeMillis();
                        // call /swipe
                        ApiResponse res = apiInstance.swipeWithHttpInfo(body, leftOrRight[ThreadLocalRandom.current().nextInt(2)]);
                        long wallTime = System.currentTimeMillis() - startTime;

                        sb.append(startTime);
                        sb.append(",POST,");
                        sb.append(wallTime);

                        if (res.getStatusCode() == 201) {
                            sb.append(",201\n");
                            consumeSuccessCount.incrementAndGet();
                        }

                        break;

                    } catch (ApiException e) {
                        System.err.println(Thread.currentThread().getName() + " result: " + e.getCode());
                        sb.append(",");
                        sb.append(e.getCode());
                        sb.append("\n");
                        writeOut.add(sb.toString());
                        sb.setLength(0);
                    }

                    retries++;
                } while (retries < 6);

                if (retries == 6) {
                    // failed
                    consumeFailedCount.incrementAndGet();
                } else {
                    writeOut.add(sb.toString());
                    sb.setLength(0);
                }

                completed.countDown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
