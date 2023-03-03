import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import consumer.MatchConsumerImp;
import model.SwipeMatchStat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class RabbitMQMatchConsumer {

    private final static String RMQ_HOST = "localhost";
//    private final static String RMQ_HOST = "<ec2-consumer-Public IPv4 DNS>";
    private final static String EXCHANGE_NAME = "twinder_exchange";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String ROUTING_KEY = "";
//    private final static int NUM_CONSUMERS = 5;

    public static void main(String[] args) throws Exception {

        int consumerCnt = Integer.parseInt(args[0]);

        // queue factory configuration
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RMQ_HOST);
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        // start new connection
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // connect to exchange and queue
        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);

        // storage cache
        ConcurrentHashMap<String, SwipeMatchStat> map = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(consumerCnt);

        for (int i = 0; i < consumerCnt; i++) {
            Runnable consumer = new MatchConsumerImp(map, channel, queueName);
            executor.execute(consumer);
        }
    }
}

