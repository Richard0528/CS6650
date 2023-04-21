import com.rabbitmq.client.*;
import consumer.BrokerImp;
import consumer.ConsumerImp;
import db.DatabaseClient;
import db.SwipeDAO;
import model.SwipePayload;
import software.amazon.awssdk.regions.Region;

import java.util.concurrent.*;

public class TwinderConsumer {

    private final static String RMQ_HOST = "ec2-52-39-189-165.us-west-2.compute.amazonaws.com";
//    private final static String RMQ_HOST = "<ec2-mq-consumer-Public IPv4 DNS>";
    private final static String EXCHANGE_NAME = "twinder_exchange";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String ROUTING_KEY = "";
//    private final static int NUM_CONSUMERS = 5;
    private static final int BOUND = 50000;

    private static final Region AWS_REGION = Region.US_WEST_2;
    private static final String DB_TABLE_NAME = "TwinderTable";

    public static void main(String[] args) throws Exception {

        int consumerCnt = Integer.parseInt(args[0]);

        // configure the factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RMQ_HOST);
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // configure channel
        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);

        // shared payload queue
        BlockingQueue<SwipePayload> queue = new LinkedBlockingQueue<>(BOUND);

        // RMQ channel is not thread-safe.
        // since we are using only one channel, it's best practise to use one thread pulling messages
        // BrokerImp is acting as a consumer of RMQ channel and producer of the ConsumerImp
        System.out.println("Broker started");
        new Thread(new BrokerImp(queue, channel, queueName)).start();

        // Start actual consumers
        ConcurrentHashMap<String, SwipeDAO> map = new ConcurrentHashMap<>();

        // Dynamo db client is thread-safe, so only once instance is needed
        DatabaseClient client = new DatabaseClient(AWS_REGION, DB_TABLE_NAME);
//        client.createTableIfNotExists()

        System.out.println("Consumers started");
        for (int i = 0; i < consumerCnt; i++) {
            new Thread(new ConsumerImp(map, queue, client)).start();
        }

    }
}

