package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.BranchService;
import model.ApiResponse;
import model.Branch;

import org.jboss.logging.Logger;


@Path("/branch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BranchResource {

    @Inject
    Logger logger;

    @Inject
    BranchService branchService;

    @GET
    @Path("/getall")
    public Response getBranches() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = branchService.getAll();
            return Response.ok(new ApiResponse(result, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching branch details", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Failed to retrieve branch details",false))
                .build();
            
        }
    }

    @GET
    @Path("/get")
    public Response getBranch(@QueryParam("cd") int cd) 
    {
        logger.info("message : =====> get method hit");
        try
        {
            var branch = branchService.get(cd);
            if(branch == null)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponse(null,"Branch not found",false))
                    .build();

            return Response.ok(new ApiResponse(branch, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching branch", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }

    @POST
    @Path("/create")
    public Response createBranch(Branch branch) 
    {
        logger.info("message : =====> create method hit with body "+ branch.toString());
        try 
        {
            var result = branchService.create(branch);

            if (result == null) 
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiResponse(null, "Branch already exist.", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                        .build();
        
            
        } 
        catch (Exception ex) {
            logger.error("Error creating branch", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }


    @PUT
    @Path("/update")
    public Response updateBranch(Branch branch) 
    {
        logger.info("message : =====> update method hit with body " + branch.toString());
        try 
        {
            var result = branchService.update(branch);
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Error occurred while updating branch",false))
                .build();
                
            return Response.ok(new ApiResponse(result, null, true))
                .build();
            
        } 
        catch (Exception ex) {
            logger.error("Error updating branch", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }


    @DELETE
    @Path("/delete")
    public Response deleteBranch(@QueryParam("cd") int cd) 
    {
        logger.info("message : =====> delete method hit");
        try 
        {
            boolean isDeleted = branchService.delete(cd);
            if(!isDeleted)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Error occurred while deleting branch",false))
                .build();

            return Response.ok(new ApiResponse(null, null, true))
                .build();
        } 
        catch (Exception ex) 
        {
            logger.error("Error deleting branch", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }

    }


}
