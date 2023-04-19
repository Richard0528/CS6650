package consumer;

import db.DatabaseClient;
import db.SwipeADO;
import model.SwipePayload;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsumerImp implements Runnable {

    private final ConcurrentHashMap<String, SwipeADO> map;
    private final BlockingQueue<SwipePayload> queue;
    private final DatabaseClient client;
    private final DynamoDbAsyncTable<SwipeADO> table;

    public ConsumerImp(ConcurrentHashMap<String, SwipeADO> map,
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
        List<SwipeADO> records = new ArrayList<>(25);
        Set<String> idList = new HashSet<>();
        int counter = 0;
        try {
            while (true) {

                // the reason behind picking 22 is that dynamodb has a limit of 25 for batch write.
                // since we do not know if the next payload will have two or three updates,
                // just reserve 3 empty spots for the next poll
                while (counter <= 22) {
                    SwipePayload payload = queue.poll(1000, TimeUnit.MILLISECONDS);

                    // if payload is empty after timeout, just exit and update db
                    if (payload == null) {
                        break;
                    }

                    map.compute(payload.getSwiper(), (k, v) -> {
                        if (v == null) {
                            v = new SwipeADO(payload.getSwiper(), "count");
                        }

                        if (payload.isLike()) {
                            v.incrementLikeCnt();
                        } else {
                            v.incrementDislikeCnt();
                        }
                        return v;
                    });

                    if (idList.add(payload.getSwiper())) {
                        counter++;
                    }

                    if (payload.isLike()) {
                        records.add(new SwipeADO(payload.getSwiper(), "like_".concat(payload.getSwipee()), payload.getSwipee()));
                        records.add(new SwipeADO(payload.getSwipee(), "isLiked_".concat(payload.getSwiper()), payload.getSwiper()));
                        counter += 2;
                    }
                }

                // if empty, do not perform write
                if (idList.isEmpty() && records.isEmpty()) {
                    continue;
                }

                records = Stream.concat(records.stream(), idList.stream().map(map::get)).collect(Collectors.toList());
//                records.forEach(System.out::println);
                client.putOrUpdateItems(table, records);

                // reset counter
                counter = 0;
                // clear out the current batch
                records.clear();
                idList.clear();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
