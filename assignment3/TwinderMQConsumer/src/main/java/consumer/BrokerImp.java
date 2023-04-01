package consumer;

import com.rabbitmq.client.Channel;
import model.SwipePayload;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class BrokerImp implements Runnable {

    private final BlockingQueue<SwipePayload> queue;
    private final Channel channel;
    private final String queueName;

    public BrokerImp(BlockingQueue<SwipePayload> queue, Channel channel, String queueName) {
        this.queue = queue;
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

                        try {
                            queue.put(payload);
//                            System.out.println(queue.size());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }, consumerTag -> System.out.println("Consumer cancelled: " + consumerTag));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
