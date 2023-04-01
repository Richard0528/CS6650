package consumer;

import com.rabbitmq.client.Channel;
import model.SwipeMatchStat;
import model.SwipePayload;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MatchConsumerImp implements Runnable {

    private ConcurrentHashMap<String, SwipeMatchStat> map;

    private final Channel channel;

    private final String queueName;

    public MatchConsumerImp(ConcurrentHashMap<String, SwipeMatchStat> map, Channel channel, String queueName) {
        this.map = map;
        this.channel = channel;
        this.queueName = queueName;
    }

    @Override
    public void run() {
        try {

            // Consume a message from the queue
            channel.basicConsume(queueName, true,
                (consumerTag, delivery) -> {
                    SwipePayload payload = SerializationUtils.deserialize(delivery.getBody());

                    if (!payload.isLike()) {
                        return;
                    }

                    map.compute(payload.getSwiper(), (k, v) -> {
                        if (v == null) {
                            v = new SwipeMatchStat();
                        }

                        v.addLike(payload.getSwipee());

                        return v;
                    });

                    map.compute(payload.getSwipee(), (k, v) -> {
                        if (v == null) {
                            v = new SwipeMatchStat();
                        }

                        v.addBeingLiked(payload.getSwiper());

                        return v;
                    });

                    System.out.println("Received message: " + map.get(payload.getSwipee()) + ", " + map.get(payload.getSwiper()));
                }, consumerTag -> System.out.println("Consumer cancelled: " + consumerTag));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
