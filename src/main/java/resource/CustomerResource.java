package resource;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.ApiResponse;
import service.CustomerService;
//import model.Customer;

//import org.jboss.logging.Logger;


@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class CustomerResource {
    @Inject
    Logger logger;

    @Inject
    CustomerService customerService;


    @GET
    @Path("/get")
    public Response getAccount(@QueryParam("no") int no) 
    {
        logger.info("message : =====> get method hit");
        try
        {
            var customer = customerService.get(no);
            if(customer == null)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponse(null,"Customer not found",false))
                    .build();

            return Response.ok(new ApiResponse(customer, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching Customer", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }

}
