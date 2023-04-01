import db.DatabaseClient;
import db.SwipeData;
import model.MatchStats;
import model.ResponseMsg;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.regions.Region;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "StatsApiServletlmpl", value = "/StatsApiServletlmpl")
public class StatsApiServletlmpl extends HttpServlet {
    private static final Region AWS_REGION = Region.US_WEST_2;
    private static final String DB_TABLE_NAME = "TwinderTable";
    private DatabaseClient client;

    private DynamoDbTable<SwipeData> table;

    @Override
    public void init() throws ServletException {
        super.init();
        client = new DatabaseClient(AWS_REGION, DB_TABLE_NAME);
        table = client.getSwipeDataTable();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

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

        String id = parseUrl(urlParts);

        if (id == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("Invalid Url Parameters");
            out.flush();
            return;
        }

        SwipeData requested = client.getItem(table, id);

        if (requested == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ResponseMsg msg = new ResponseMsg("User not found");
            out.print(msg);
            out.flush();
            return;
        }

        MatchStats requestedMatchStats = new MatchStats(requested.getLikeCnt(), requested.getDislikeCnt());
        out.print(requestedMatchStats);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    private String parseUrl(String[] urlPath) {

        if (urlPath.length == 2 && !urlPath[1].isEmpty() && !urlPath[1].isBlank()) {
            return urlPath[1];
        }

        return null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
