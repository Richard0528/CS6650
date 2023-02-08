import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import model.ResponseMsg;
import model.SwipeDetails;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {
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
        SwipeDetails validSwipe;
        try {
            validSwipe = new Gson().fromJson(req.getReader(), SwipeDetails.class);

            if (validSwipe == null || !validSwipe.isValid()) {
                throw new JsonParseException("Entry is null or empty");
            }

        } catch (JsonParseException parseException) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResponseMsg msg = new ResponseMsg("Invalid Inputs");
            out.print(msg);
            out.flush();
            return;
        }

        res.setStatus(HttpServletResponse.SC_CREATED);
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
}
