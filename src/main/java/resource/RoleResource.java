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
import service.RoleService;
import model.ApiResponse;
import model.Role;

import org.jboss.logging.Logger;


@Path("/role")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    Logger logger;

    @Inject
    RoleService roleService;

    @GET
    @Path("/getall")
    public Response getRoles() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = roleService.getAll();
            return Response.ok(new ApiResponse(result, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching roles", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Failed to retrieve roles",false))
                .build();
            
        }
    }

    @GET
    @Path("/get")
    public Response getRole(@QueryParam("id") int id) 
    {
        logger.info("message : =====> get method hit");
        try
        {
            var role = roleService.get(id);
            if(role == null)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponse(null,"Role not found",false))
                    .build();

            return Response.ok(new ApiResponse(role, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching role", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }

    @POST
    @Path("/create")
    public Response createRole(Role role) 
    {
        logger.info("message : =====> create method hit with body "+ role.toString());
        try 
        {
            var result = roleService.create(role);
            return Response.ok(new ApiResponse(result, null, true))
                .build();
            
        } 
        catch (Exception ex) {
            logger.error("Error creating role", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }


    @PUT
    @Path("/update")
    public Response updateRole(Role role) 
    {
        logger.info("message : =====> update method hit with body " + role.toString());
        try 
        {
            var result = roleService.update(role);
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Error occurred while updating role",false))
                .build();
                
            return Response.ok(new ApiResponse(result, null, true))
                .build();
            
        } 
        catch (Exception ex) {
            logger.error("Error updating role", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }


    @DELETE
    @Path("/delete")
    public Response deleteRole(@QueryParam("id") int id) 
    {
        logger.info("message : =====> getRole method hit");
        try 
        {
            boolean isDeleted = roleService.delete(id);
            if(!isDeleted)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Error occurred while deleting role",false))
                .build();

            return Response.ok(new ApiResponse(null, null, true))
                .build();
        } 
        catch (Exception ex) 
        {
            logger.error("Error deleting role", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }

    }


}
