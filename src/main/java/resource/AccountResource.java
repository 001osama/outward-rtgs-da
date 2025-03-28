package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.GET;
//import jakarta.ws.rs.POST;
//import jakarta.ws.rs.PUT;
//import jakarta.ws.rs.DELETE;
// import java.util.Optional;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.AccountService;
import model.ApiResponse;
// import model.Account;
// import dto.AccountDetailResponseDto;

import org.jboss.logging.Logger;

import dto.AccountResponseDto;



@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class AccountResource {
    @Inject
    Logger logger;

    @Inject
    AccountService accountService;

    @GET
    @Path("/accountnumber")
    public Response getAccountNo(@QueryParam("customerNo") int customerNo) 
    {
        logger.info("message : =====> getAccountNo method hit");
        try
        {
            var accountNolist = accountService.getAccountNo(customerNo);
            if(accountNolist == null)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponse(null,"Account not found",false))
                    .build();

            var response = new AccountResponseDto(accountNolist);
            
            return Response.ok(new ApiResponse(response, null, true))
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


    // @GET
    // @Path("/accountdetails")
    // public Response getAccountDetails(@QueryParam("accountNo") String accountNo)
        
    // {
    //     logger.info("message : =====> getAccountDetails method hit");
    //     try
    //     {
    //         String[] parts = accountNo.split("-");
    //         if (parts.length != 5)
    //             return Response.status(Response.Status.BAD_REQUEST)
    //                 .entity("Invalid account number format. Expected format: branchCd-accountTypeCd-customerNo-runNo-checkDigit")
    //                 .build();

    //         int branchCd = Integer.parseInt(parts[0]);
    //         int accountTypeCd = Integer.parseInt(parts[1]);
    //         int customerNo = Integer.parseInt(parts[2]);
    //         int runNo = Integer.parseInt(parts[3]);
    //         int checkDigit = Integer.parseInt(parts[4]);



    //         var accountDetails = accountService.getAccountDetails(branchCd, accountTypeCd, customerNo, runNo, checkDigit);
    //         if(accountDetails == null)
    //             return Response.status(Response.Status.NOT_FOUND)
    //                 .entity(new ApiResponse(null,"Account not found",false))
    //                 .build();

    //         var response = new AccountDetailResponseDto(accountDetails.getAccountTitle(), accountDetails.getAvailableBalance());
            
    //         return Response.ok(new ApiResponse(response, null, true))
    //             .build();
    //     }
    //     catch(Exception ex)
    //     {
    //         logger.error("Error fetching Accounts", ex);
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //             .entity(new ApiResponse(null,"Internal Server Error Occured",false))
    //             .build();
    //     }
    // }
    

    
}

