package consumer;

import db.DatabaseClient;
import db.SwipeData;
import model.SwipePayload;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConsumerImp implements Runnable {

    private final ConcurrentHashMap<String, SwipeData> map;
    private final BlockingQueue<SwipePayload> queue;
    private final DatabaseClient client;
    private final DynamoDbTable<SwipeData> table;

    public ConsumerImp(ConcurrentHashMap<String, SwipeData> map,
                       BlockingQueue<SwipePayload> queue,
                       DatabaseClient client) {
        this.map = map;
        this.queue = queue;
        this.client = client;
        this.table = client.getSwipeDataTable();
    }

    @Override
    public void run() {
//        System.out.println("Consumer started");
        try {
            while (true) {

                Set<String> idList = new HashSet<>();

                // the reason behind picking 23 is that dynamodb has a limit of 25 for batch write.
                // since we do not know if the next payload will have one or two updates,
                // just reserve 2 empty spots for the next poll
                while (idList.size() <= 23) {
                    SwipePayload payload = queue.poll(1000, TimeUnit.MILLISECONDS);

                    // if payload is empty after timeout, just exit and update db
                    if (payload == null) {
                        break;
                    }

                    map.compute(payload.getSwiper(), (k, v) -> {
                        if (v == null) {
                            v = new SwipeData(k);
                        }

                        if (payload.isLike()) {
                            v.incrementLikeCnt();
                            v.addLike(payload.getSwipee());
                        } else {
                            v.incrementDislikeCnt();
                        }
                        return v;
                    });

                    idList.add(payload.getSwiper());

                    // if swipe is dislike, we are done
                    if (!payload.isLike()) {
                        continue;
                    }

                    // if swipe is like, update both swiper and swipee
                    map.compute(payload.getSwipee(), (k, v) -> {
                        if (v == null) {
                            v = new SwipeData(k);
                        }

                        v.addBeingLiked(payload.getSwiper());
                        return v;
                    });

                    idList.add(payload.getSwipee());
                }

                // if empty, do not perform write
                if (idList.isEmpty()) {
                    continue;
                }

                List<SwipeData> records = idList.stream().map(map::get).collect(Collectors.toList());
//                records.forEach(System.out::println);
                client.putOrUpdateItems(table, records);

                idList.clear();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
