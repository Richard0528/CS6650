import db.DatabaseClient;
import db.SwipeADO;
import model.Matches;
import model.ResponseMsg;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.regions.Region;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "MatchesApiServletlmpl", value = "/MatchesApiServletlmpl")
public class MatchesApiServletlmpl extends HttpServlet {

    private static final Region AWS_REGION = Region.US_WEST_2;
    private static final String DB_TABLE_NAME = "TwinderTable_query";
    private DatabaseClient client;

    @Override
    public void init() throws ServletException {
        super.init();
        client = new DatabaseClient(AWS_REGION, DB_TABLE_NAME);
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

        List<String> requested = client.getMatches(id);

        if (requested == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ResponseMsg msg = new ResponseMsg("User not found");
            out.print(msg);
            out.flush();
            return;
        }

        Matches requestedMatches = new Matches(requested);
        out.print(requestedMatches);
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
