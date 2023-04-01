import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.rabbitmq.client.Channel;
import model.ResponseMsg;
import model.SwipePayload;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import rmq.RMQChannelFactory;
import rmq.RMQChannelPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "SwipeApiServletImpl", value = "/SwipeApiServletImpl")
public class SwipeApiServletImpl extends HttpServlet {

//    private final static String RMQ_HOST = "localhost";
//    private final static String RMQ_HOST = "<rmq-ec2-Public IPv4 DNS>";
    private final static String RMQ_HOST = "ec2-34-219-3-218.us-west-2.compute.amazonaws.com";
    private final static int RMQ_POOL_SIZE = 50;
//    private final static String QUEUE_NAME = "hello";
    private final static String EXCHANGE_NAME = "twinder_exchange";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String ROUTING_KEY = "";
    private RMQChannelPool pool;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        // initialize a pool for channels
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(RMQ_HOST);
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        try {
            connection = connectionFactory.newConnection();
            RMQChannelFactory factory = new RMQChannelFactory(connection);
            pool = new RMQChannelPool(RMQ_POOL_SIZE, factory, EXCHANGE_NAME, EXCHANGE_TYPE);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        // init response
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String urlPath = req.getPathInfo();
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResponseMsg msg = new ResponseMsg("Empty Url Parameters");
            out.print(msg);
            out.flush();
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResponseMsg msg = new ResponseMsg("Invalid Url Parameters");
            out.print(msg);
            out.flush();
            return;
        }

        // process request body
        SwipePayload validSwipe;
        try {
            validSwipe = new Gson().fromJson(req.getReader(), SwipePayload.class);

            if (validSwipe == null || !validSwipe.isValid()) {
                throw new JsonParseException("Entry is null or empty");
            }

            // update payload with like or dislike
            validSwipe.setLike(isSwipeRight(urlParts[1]));

            // grab a pre-created channel from pool
            Channel currChannel = pool.borrowObject();

            // send the message to the exchange with the routing key
            // push serialized request body to queue
            currChannel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, SerializationUtils.serialize(validSwipe));

            // return the channel to queue
            pool.returnObject(currChannel);

        } catch (JsonParseException parseException) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResponseMsg msg = new ResponseMsg("Invalid Inputs");
            out.print(msg);
            out.flush();
            return;
        }

        res.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    public void destroy() {
        // close any open connections

        pool.close();

        // Close the channel and the connection
        try {
            Channel channel = connection.createChannel();
            channel.exchangeDelete(EXCHANGE_NAME);
            channel.close();
            connection.close();
        } catch (IOException e) {
            // Handle the exception
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]

        if (urlPath.length == 2 &&
                (urlPath[1].equals("left") || urlPath[1].equals("right"))) {
            return true;
        }

        return false;
    }

    /**
     * swipe left is dislike, swipe right is like
     *
     * @param firstUrlParam
     * @return
     */
    private boolean isSwipeRight(String firstUrlParam) {
        return firstUrlParam.equals("right");
    }
}
