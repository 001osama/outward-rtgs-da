package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.GET;
// import jakarta.ws.rs.POST;
// import jakarta.ws.rs.PUT;
// import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.SBPTranTypeService;
import model.ApiResponse;
//import model.SBPTranType;

import org.jboss.logging.Logger;

@Path("/sbptrantype")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class SBPTranTypeResource {

    @Inject
    Logger logger;

    @Inject
    SBPTranTypeService sbptrantypeService;

    @GET
    @Path("/getall")
    public Response getTranType() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = sbptrantypeService.getAll();
            return Response.ok(new ApiResponse(result, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching transaction type", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Failed to retrieve transaction type",false))
                .build();
            
        }
    }
    
}
