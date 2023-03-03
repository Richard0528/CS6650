import com.rabbitmq.client.*;
import consumer.ConsumerImp;
import model.SwipeStat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RabbitMQConsumer {

    private final static String RMQ_HOST = "localhost";
//    private final static String RMQ_HOST = "<ec2-mq-consumer-Public IPv4 DNS>";
    private final static String EXCHANGE_NAME = "twinder_exchange";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String ROUTING_KEY = "";
//    private final static int NUM_CONSUMERS = 5;

    public static void main(String[] args) throws Exception {

        int consumerCnt = Integer.parseInt(args[0]);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RMQ_HOST);
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);

        ConcurrentHashMap<String, SwipeStat> map = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(consumerCnt);

        for (int i = 0; i < consumerCnt; i++) {

            Runnable consumer = new ConsumerImp(map, channel, queueName);
            executor.execute(consumer);
        }
    }
}

