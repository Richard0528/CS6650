package org.richardyang.multiThread;

import io.swagger.client.model.SwipeDetails;
import org.richardyang.model.PoisonPill;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Producer implements Runnable {

    private final BlockingQueue<SwipeDetails> numbersQueue;
    private final AtomicInteger producersCount;
    private final int count;

    public Producer(BlockingQueue<SwipeDetails> numbersQueue, AtomicInteger producersCount, int count) {
        this.numbersQueue = numbersQueue;
        this.producersCount = producersCount;
        this.count = count;
    }

    public void run() {
        try {
            generateDetails();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateDetails() throws InterruptedException {

//        byte[] array = new byte[256];

        for (int i = 0; i < count; i++) {
            SwipeDetails tmp = new SwipeDetails();
            tmp.setSwiper("" + ThreadLocalRandom.current().nextInt(1, 50001));
            tmp.setSwipee("" + ThreadLocalRandom.current().nextInt(1, 50001));

//            ThreadLocalRandom.current().nextBytes(array);
            tmp.setComment("");
//            System.out.println(numbersQueue.size());
            numbersQueue.put(tmp);
        }

        if (producersCount.decrementAndGet() <= 0) {
            numbersQueue.put(new PoisonPill());
        }
    }
}
