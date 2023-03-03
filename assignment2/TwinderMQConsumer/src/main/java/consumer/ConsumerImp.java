package consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import model.SwipePayload;
import model.SwipeStat;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerImp implements Runnable {

    private ConcurrentHashMap<String, SwipeStat> map;

    private final Channel channel;

    private final String queueName;

    public ConsumerImp(ConcurrentHashMap<String, SwipeStat> map, Channel channel, String queueName) {
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

                    map.compute(payload.getSwipee(), (k, v) -> {
                        if (v == null) {
                            v = new SwipeStat();
                        }

                        if (payload.isLike()) {
                            v.incrementLikeCnt();
                        } else {
                            v.incrementDislikeCnt();
                        }
                        return v;
                    });

                    System.out.println("Received message: " + map.get(payload.getSwipee()));
                }, consumerTag -> System.out.println("Consumer cancelled: " + consumerTag));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
