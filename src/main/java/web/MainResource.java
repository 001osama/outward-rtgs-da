package web;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import service.ItoBatchService;



@Path("/main")
public class MainResource {

    @Inject
    Logger logger;

    @Inject
    ItoBatchService itoBatchService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/getDetail")
    public String getDetail() {
        logger.info("message : =====> getDetail method started");
        itoBatchService.getDetail();
        return "Getting response from server";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/insertDetail")
    public String insertDetail()
    {
        logger.info("message : =====> insertDetail method started");
        return itoBatchService.insertDetail();
    }
}
