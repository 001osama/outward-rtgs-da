package resource;

import org.jboss.logging.Logger;
import dto.AccountNumberDto;
import dto.ApiResponse;
import dto.BeneficiaryAccountDto;
import dto.LoginRequestDto;
import dto.OutwardRTGSFormDto;
import dto.UserDto;
import dto.UserInfoDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
                !result.getBranchCode().equals(loginRequest.getBranchCode()) ||
                !result.getPassword().equals(loginRequest.getPassword()) 
            )
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse(null,"Invalid credentials",false))
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
    @Path("/getbranchlist")
    public Response getBranchesList() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getBranchList();
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
    @Path("/getcurrencylist")
    public Response getCurrencyList() 
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getCurrencyList();
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
    @Path("/getsbptrantypelist")
    public Response getSBPTranTypeList()
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getSBPTranTypeList();
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
    @Path("/getcustomeraccountnos")
    public Response getCustomerAccountNos(@QueryParam("customerNo") String customerNo) 
    {
        logger.info("message : =====> getAccountNo method hit");
        try
        {
            var accountNolist = mainService.getCustomerAccountNos(customerNo);
            if(accountNolist.size() == 0)
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


    @GET
    @Path("/getpurposeofpaymentlist")                                                                                                      
    public Response getPurposeofPaymentList()
    {
        logger.info("message : =====> getAll method hit");
        try
        {
            var result = mainService.getPurposeofPaymentList();
            if(result == null)
                return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(null,"Invalid Data",false))
                .build();

            return Response.ok(new ApiResponse(result, null, true))
                .build();
        }
        catch(Exception ex)
        {
            logger.error("Error in retrieving purpose of payment", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null,"Failed to retrieve transaction type",false))
                .build();
            
        }
    }


    @GET
    @Path("/getbeneficiarybanklist")
    public Response getBeneficiaryBankList(@QueryParam("search") String search) 
    {
        logger.info("message : =====> getBeneficiaryBankList method hit with search term: {}" + search);
        try {
        var result = mainService.getBeneficiaryBankList(search);
        if (result == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse(null, "Invalid Data", false))
                    .build();
        }
        return Response.ok(new ApiResponse(result, null, true))
                .build();
        } catch (Exception ex) {
            logger.error("Error in retrieving beneficiary bank list", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(null, "Failed to retrieve beneficiary bank list", false))
                    .build();
        }
    }



    @GET
    @Path("/getbeneficiaryaccountdetails")
    public Response getBeneficairyAccountDetails(
        @QueryParam("accOrIban") String accOrIban,
        @QueryParam("bankCode") String bankCode) 
        {
        logger.info("message : =====> getBeneficiaryAccount method hit with accountNo: " + accOrIban);
        try {
            BeneficiaryAccountDto result = null;

            if (accOrIban != null && !accOrIban.trim().isEmpty() 
            && bankCode != null && !bankCode.trim().isEmpty()) 
                result = mainService.getBeneficiaryAccountDetails(accOrIban, bankCode);

            if (result == null) 
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiResponse(null, "Beneficiary Account not found", false))
                        .build();


            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching Account details", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred", false))
                    .build();
        }
    }



    

    @GET
    @Path("/getexceptionlist")
    public Response getExceptionList(@QueryParam("activityCode") String activityCode) 
    {
        logger.info("message : =====> getExceptionList method hit with activity Code" + activityCode);
        try {
            var result = mainService.getExceptionList(activityCode);
            if (result == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse(null, "Invalid Data", false))
                    .build();
            }
            return Response.ok(new ApiResponse(result, null, true))
                .build();
        } catch (Exception ex) {
            logger.error("Error in retrieving beneficiary bank list", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse(null, "Failed to retrieve beneficiary bank list", false))
                .build();
        }
    }


    @POST
    @Path("/createOutwardRtgs")
    //Create the complete voucher and laxes logic
    public Response createOutwardRtgs(OutwardRTGSFormDto rtgsForm) {
        logger.info("message : =====> submitOutwardRtgs method hit with body " + rtgsForm.toString());
        try {
            // You might want to add validation logic here before calling the service
            
            OutwardRTGSFormDto createdTransaction = mainService.createOutwardRtgs(rtgsForm);

            if (createdTransaction == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiResponse(null, "Invalid RTGS Form Data", false))
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(new ApiResponse(rtgsForm, "RTGS form submitted successfully", true))
                    .build();

        } 
        catch (Exception ex) {
            logger.error("Error submitting RTGS form", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred while submitting RTGS form", false))
                    .build();
        }
    }

    @PUT // Using PUT as it's an update operation
    @Path("/cancel")
    public Response cancelRtgsTransaction(@QueryParam("voucherNo") String voucherNo,
                                           @QueryParam("voucherYear") String voucherYear) {
        logger.info("message : =====> cancelRtgsTransaction method hit for voucherNo: {} and voucherYear: {}" + voucherNo + voucherYear);
        try {
            if (voucherNo == null || voucherNo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiResponse(null, "Voucher number is required for cancellation.", false))
                        .build();
            }

            boolean isCancelled = mainService.cancelRtgsTransaction(voucherNo, voucherYear);

            if (isCancelled) {
                return Response.ok(new ApiResponse(null, "RTGS transaction with voucher number " + voucherNo + (voucherYear != null && !voucherYear.trim().isEmpty() ? " and year " + voucherYear : "") + " has been successfully cancelled.", true))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiResponse(null, "RTGS transaction with voucher number " + voucherNo + (voucherYear != null && !voucherYear.trim().isEmpty() ? " and year " + voucherYear : "") + " not found or could not be cancelled.", false))
                        .build();
            }
        } catch (Exception ex) {
            logger.error("Error cancelling RTGS transaction for voucherNo: {} and year: {}" + voucherNo + voucherYear, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(null, "Internal Server Error occurred while cancelling RTGS transaction.", false))
                    .build();
        }
    }


    @PUT // Using PUT as it's an update operation
    @Path("/authorize")
    public Response authorizeRtgsTransaction(@QueryParam("voucherNo") String voucherNo,
                                             @QueryParam("voucherYear") String voucherYear) {
        logger.info("message : =====> authorizeRtgsTransaction method hit for voucherNo: {} and voucherYear: {}" + voucherNo + voucherYear);
        try {
            if (voucherNo == null || voucherNo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiResponse(null, "Voucher number is required for authorization.", false))
                        .build();
            }

            boolean isAuthorized = mainService.authorizeRtgsTransaction(voucherNo, voucherYear);

            if (isAuthorized) {
                return Response.ok(new ApiResponse(null, "RTGS transaction with voucher number " + voucherNo + (voucherYear != null && !voucherYear.trim().isEmpty() ? " and year " + voucherYear : "") + " has been successfully authorized.", true))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiResponse(null, "RTGS transaction with voucher number " + voucherNo + (voucherYear != null && !voucherYear.trim().isEmpty() ? " and year " + voucherYear : "") + " not found or could not be authorized.", false))
                        .build();
            }
        } catch (Exception ex) {
            logger.error("Error authorizing RTGS transaction for voucherNo: {} and year: {}" + voucherNo + voucherYear, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(null, "Internal Server Error occurred while authorizing RTGS transaction.", false))
                    .build();
        }
    }


    @GET 
    @Path("/validatecheque")
    public Response validateCheque(
        @QueryParam("accountNo") String accountNo,
        @QueryParam("chequeNo") String chequeNo) 
        {
            logger.info("message : =====> authorizeRtgsTransaction method hit for chequeNo: { "+ chequeNo +" } ");
            try {
                if (chequeNo == null || chequeNo.trim().isEmpty() || 
                    accountNo == null || chequeNo.trim().isEmpty()) 
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ApiResponse(null, "Cheque Number is required.", false))
                            .build();


                var detailsArray = accountNo.split("-");
                var accountNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2], detailsArray[3], detailsArray[4]);
            
                boolean isValid = mainService.isChequeExist(accountNumberDto, chequeNo);
                
            } catch (Exception ex) {
                logger.error("Error authorizing RTGS transaction for chequeNo: { "+ chequeNo +" }", ex);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponse(null, "Internal Server Error occurred while validating cheque.", false))
                        .build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponse(null, "Every thing working fine.", false))
                        .build();
    }









}
