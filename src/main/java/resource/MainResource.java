package resource;

import org.jboss.logging.Logger;
import dto.AccountNumberDto;
import dto.ApiResponse;
import dto.LoginRequestDto;
import dto.UserDto;
import dto.UserInfoDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.MainService;

@Path("/data-access")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MainResource {

    @Inject
    Logger logger;

    @Inject
    MainService mainService;

    @POST
    @Path("/createuser")
    public Response createUser(UserDto user) 
    {
        logger.info("message : =====> create method hit with body "+ user.toString());
        try 
        {
            var result = mainService.getUser(user.getLoginId());
            if(result != null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Login Id already exists",false))
                .build();

            result = mainService.createUser(user);

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
    @Path("/getuserinfo")
    public Response getUserInfo(LoginRequestDto loginRequest) 
    {
        logger.info("message : =====> login method hit with body "+ loginRequest.toString());
        try 
        {
            var result = mainService.getUser(loginRequest.getLoginId());

            if(result == null || 
                // !result.getBranchCode().equals(loginRequest.getBranchCode()) ||
                !result.getPassword().equals(loginRequest.getPassword()) 
            )
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse(null,"Invalid username or password",false))
                    .build();

            var response = new UserInfoDto(result.getLoginId(), result.getBranchCode(), result.getRoleId());
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

    @GET
    @Path("/getallbranchlist")
    public Response getAllBranchesList() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getAllBranchList();
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Invalid Data",false))
                .build();

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
    @Path("/getallbranches")
    public Response getAllBranches() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getAllBranches();
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Invalid Data",false))
                .build();

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
    @Path("/getallcurrencies")
    public Response getAllCurrencies() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getAllCurrencies();
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Invalid Data",false))
                .build();

            return Response.ok(new ApiResponse(result, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching currency", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Failed to retrieve currency",false))
                .build();
            
        }
    }

    @GET
    @Path("/getallsbptrantypes")
    public Response getSBPTranType()
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getAllSBPTranTypes();
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Invalid Data",false))
                .build();

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


    @GET
    @Path("/getallaccountnos")
    public Response getAllAccounts(@QueryParam("customerNo") String customerNo) 
    {
        logger.info("message : =====> getAccountNo method hit");
        try
        {
            var accountNolist = mainService.getAllAccounts(customerNo);
            if(accountNolist == null)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponse(null,"Account not found",false))
                    .build();

            
            return Response.ok(new ApiResponse(accountNolist, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching Accounts", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }


    @GET
    @Path("/getaccountdetails")
    public Response getAccountDetails(@QueryParam("accountNo") String accountNo) 
    {
        logger.info("message : =====> getAccountDetails method hit");
        var detailsArray = accountNo.split("-");
        var accoutNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2], detailsArray[3], detailsArray[4]);
        try
        {
            var accountDetails = mainService.getAccountDetails(accoutNumberDto);
            if(accountDetails == null)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponse(null,"Account not found",false))
                    .build();
            
            return Response.ok(new ApiResponse(accountDetails, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error fetching Accounts", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Internal Server Error Occured",false))
                .build();
        }
    }

}
