package resource;

import org.jboss.logging.Logger;

import dto.LoginRequestDto;
import dto.LoginResponseDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.ApiResponse;
import model.User;
import service.UserService;


@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    Logger logger;

    @Inject
    UserService userService;


    @POST
    @Path("/createuser")
    public Response createUser(User user) 
    {
        logger.info("message : =====> create method hit with body "+ user.toString());
        try 
        {
            var result = userService.get(user.getLoginId());
            if(result != null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Login Id already exists",false))
                .build();

            result = userService.create(user);

            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Invalid Data",false))
                .build();

            return Response.ok(new ApiResponse(result, null, true))
                .build();
        } 
        catch (Exception ex) {
            logger.error("Error creating user", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }

    @POST
    @Path("/login")
    public Response Login(LoginRequestDto loginRequest) 
    {
        logger.info("message : =====> login method hit with body "+ loginRequest.toString());
        try 
        {
            var result = userService.get(loginRequest.getLoginId());

            if(result == null || 
                !result.getBranchCode().equals(loginRequest.getBranchCode()) ||
                !result.getPassword().equals(loginRequest.getPassword()) 
            )
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse(null,"Invalid Login Attempt",false))
                    .build();

            var response = new LoginResponseDto(result.getLoginId(), result.getBranchCode() );
            return Response.ok(new ApiResponse(response, null, true))
                .build();
        } 
        catch (Exception ex) {
            logger.error("Error creating user", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }


}
