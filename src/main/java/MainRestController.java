import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.WinApiClass;
import model.WinApiFunction;
import model.WinApiParameter;
import model.common.URLS;
import model.common.exception.HandbookException;
import model.common.service.impl.WinApiHandbookServiceJdbc;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/rest_service")
public class MainRestController {

    private final WinApiHandbookServiceJdbc service;
    private static Logger log = Logger.getLogger(MainRestController.class);
    private Gson gson = new Gson();

    /**
     * Initialize datasorce for jdbc Connection.
     *
     * @throws ServletException
     */
    public MainRestController() throws ServletException {
        try {
            Context initContext = new InitialContext();
            DataSource ds = (DataSource) initContext.lookup("MyDatabase");
            service = new WinApiHandbookServiceJdbc(ds);
            log.error("Executed task :  Init data sources context\n");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @GET
    @Path(URLS.GET_WIN_API_CLASS)
    @Produces(MediaType.APPLICATION_JSON)
    public String getWinApiClass(@QueryParam("id") long id) throws HandbookException {
        log.error("Executed task : @GET:class\n" + id);
        WinApiClass temp = service.getWinApiClass(id);
        String json = gson.toJson(temp);
        return json;
    }

    @GET
    @Path(URLS.FIND_WIN_API_CLASS)
    @Produces(MediaType.APPLICATION_JSON)
    public String findClasses(@QueryParam("keyword") String keyword) throws HandbookException {//List<WinApiclass> list to json
        log.error("Executed task : @GET:class\n" + keyword);
        List<WinApiClass> classes = service.findClasses(keyword);
        return gson.toJson(classes);
    }


    @POST
    @Path(URLS.WIN_API_CLASS)
    @Produces(MediaType.APPLICATION_JSON)
    public String saveOrUpdate(String apiClass) throws HandbookException {
        log.error("Executed task : @POST:class\n" + apiClass);
        WinApiClass winApiClass;
        try {
            winApiClass = gson.fromJson(apiClass, WinApiClass.class);
            service.saveOrUpdate(winApiClass);
        } catch (HandbookException e) {
            throw new HandbookException(e);
        } catch (JsonSyntaxException e) {
            throw new HandbookException(e);
        }
        return gson.toJson(winApiClass);
    }

    @DELETE
    @Path(URLS.WIN_API_CLASS)
    @Produces(MediaType.APPLICATION_JSON)
    public String removeClass(@QueryParam("id") long id) throws HandbookException {
        log.error("Executed task : @DELETE:class\n" + id);
        try {
            service.removeClass(id);
        } catch (HandbookException e) {
            return gson.toJson(e);
        }
        return gson.toJson("Class with id "
                + id + " were deleted successfully");
    }

    @PUT
    @Path(URLS.FUNCTION)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateFunction(@QueryParam("function") String function) throws HandbookException {
        log.error("Executed task : @PUT:function\n" + function);
        try {
            service.updateFunction(gson.fromJson(function, WinApiFunction.class));
        } catch (HandbookException e) {
            return gson.toJson(e);
        }
        return gson.toJson("Function with id "
                + gson.fromJson(function, WinApiFunction.class).getId()
                + " were updated successfully");
    }

    @DELETE
    @Path(URLS.FUNCTION)
    @Produces(MediaType.APPLICATION_JSON)
    public String removeWinApiFunction(@QueryParam("id") long id) throws HandbookException {
        log.error("Executed task : @DELETE:function\n" + id);
        try {
            service.removeWinApiFunction(id);
        } catch (HandbookException e) {
            return gson.toJson(e);
        }
        return gson.toJson("Function with id " + id + " were deleted successfully");
    }

    @PUT
    @Path(URLS.PARAMETER)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateParam(@QueryParam("parameter") String parameter) throws HandbookException {
        log.error("Executed task : @PUT:parameter\n" + parameter);
        try {
            service.updateParam(gson.fromJson(parameter, WinApiParameter.class));
        } catch (HandbookException e) {
            return gson.toJson(e);
        }
        return gson.toJson("Parameter with id "
                + gson.fromJson(parameter, WinApiParameter.class).getId()
                + " were updated successfully");
    }

    @DELETE
    @Path(URLS.PARAMETER)
    @Produces(MediaType.APPLICATION_JSON)
    public String removeWinApiParameter(@QueryParam("id") long id) throws HandbookException {
        log.error("Executed task : @DELETE:parameter\n" + id);
        try {
            service.removeWinApiParameter(id);
        } catch (HandbookException e) {
            return gson.toJson(e);
        }
        return gson.toJson("Parameter with id " + id
                + " were deleted successfully");
    }
}