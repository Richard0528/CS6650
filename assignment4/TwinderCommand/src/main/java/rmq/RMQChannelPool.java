package rmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple RabbitMQ channel pool based on a BlockingQueue implementation
 *
 */
public class RMQChannelPool {

    // used to store and distribute channels
    private final BlockingQueue<Channel> pool;
    // fixed size pool
    private int capacity;
    // used to create channels
    private RMQChannelFactory factory;

    public RMQChannelPool(int maxSize, RMQChannelFactory factory, String exchangeName, String exchangeType) {
        this.capacity = maxSize;
        pool = new LinkedBlockingQueue<>(capacity);
        this.factory = factory;
        for (int i = 0; i < capacity; i++) {
            Channel chan;
            try {
                chan = factory.create();
                // declare the exchange
                chan.exchangeDeclare(exchangeName, exchangeType);
                pool.put(chan);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(RMQChannelPool.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public Channel borrowObject() {

        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error: no channels available" + e.toString());
        }
    }

    public void returnObject(Channel channel) {
        if (channel != null) {
            pool.add(channel);
        }
    }

    public void close() {
        pool.forEach(channel -> {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
        pool.clear();
        // pool.close();
    }
}

