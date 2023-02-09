package org.richardyang.multiThread;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import org.richardyang.model.PoisonPill;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {
    private final String[] leftOrRight = new String[] {"left", "right"};
    private final BlockingQueue<SwipeDetails> queue;
    private final CountDownLatch completed;
    private final AtomicInteger consumeSuccessCount;
    private final AtomicInteger consumeFailedCount;
    private final SwipeApi apiInstance;

    public Consumer(BlockingQueue<SwipeDetails> queue,
                    CountDownLatch completed,
                    String baseUrl,
                    AtomicInteger consumeSuccessCount,
                    AtomicInteger consumeFailedCount) {
        this.queue = queue;
        this.completed = completed;
        this.consumeSuccessCount = consumeSuccessCount;
        this.consumeFailedCount = consumeFailedCount;

        apiInstance = new SwipeApi(new ApiClient().setBasePath(baseUrl));
    }

    public void run() {
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
                        ApiResponse res = apiInstance.swipeWithHttpInfo(body, leftOrRight[ThreadLocalRandom.current().nextInt(2)]);

                        if (res.getStatusCode() == 201) {
                            consumeSuccessCount.incrementAndGet();
                        }

                        break;

                    } catch (ApiException e) {
                        System.err.println(Thread.currentThread().getName() + " result: " + e.getCode());
                    }

                    retries++;
                } while (retries < 6);

                if (retries == 6) {
                    consumeFailedCount.incrementAndGet();
                }

                completed.countDown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
