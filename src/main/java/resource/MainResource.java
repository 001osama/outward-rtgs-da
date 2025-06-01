package resource;

import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;
import dto.AccountNumberDto;
import dto.ApiResponse;
import dto.AuthorizeUserDto;
import dto.BeneficiaryAccountDto;
import dto.LoginRequestDto;
import dto.OutwardRTGSFormDto;
import dto.OutwardRTGSReponseDto;
import dto.RTGSReportDto;
import dto.ReportFilterDto;
import dto.UserDto;
import dto.VoucherCancelDto;
import dto.VoucherDto;
import dto.VoucherInfoDto;
import dto.VoucherLaxDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.BirtService;
import service.MainService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Path("/data-access")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MainResource {

    @Inject
    Logger logger;

    @Inject
    MainService mainService;

    @Inject
    BirtService birtService;

    @POST
    @Path("/createuser")
    public Response createUser(UserDto user) {
        logger.info("message : =====> create method hit with body " + user.toString());
        try {
            var isUserExist = mainService.isUserExist(user.getLoginId());
            if (isUserExist)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Login Id already exists", false))
                        .build();

            var result = mainService.createUser(user);

            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error creating user", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occured", false))
                    .build();
        }
    }

    @POST
    @Path("/getuserinfo")
    public Response getUserInfo(LoginRequestDto loginRequest) {
        logger.info("message : =====> login method hit with body " + loginRequest.toString());
        try {
            var result = mainService.getUser(loginRequest);

            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid credentials", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error creating user", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occured", false))
                    .build();
        }
    }

    @GET
    @Path("/getbranchlist")
    public Response getBranchesList() {
        logger.info("message : =====> getAll method hit");
        try {
            var result = mainService.getBranchList();
            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching branch details", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve branch details", false))
                    .build();
        }
    }

    @GET
    @Path("/getcurrencylist")
    public Response getCurrencyList() {
        logger.info("message : =====> getAll method hit");
        try {
            var result = mainService.getCurrencyList();
            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching currency", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve currency", false))
                    .build();

        }
    }

    @GET
    @Path("/getsbptrantypelist")
    public Response getSBPTranTypeList() {
        logger.info("message : =====> getAll method hit");
        try {
            var result = mainService.getSBPTranTypeList();
            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching transaction type", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve transaction type", false))
                    .build();

        }
    }

    @GET
    @Path("/getcustomeraccountnos")
    public Response getCustomerAccountNos(@QueryParam("customerNo") String customerNo, @QueryParam("branchCode") String branchCode) 
    {
        logger.info("message : =====> getAccountNo method hit");
        try
        {
            var accountNolist = mainService.getCustomerAccountNos(customerNo, branchCode);
            if(accountNolist.size() == 0)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Account not found", false))
                        .build();

            return Response.ok(new ApiResponse(accountNolist, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching Accounts", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occured", false))
                    .build();
        }
    }

    @GET
    @Path("/getaccountdetails")
    public Response getAccountDetails(@QueryParam("accountNo") String accountNo) {
        logger.info("message : =====> getAccountDetails method hit");
        var detailsArray = accountNo.split("-");
        var accoutNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2], detailsArray[3],
                detailsArray[4]);

        try {
            var accountDetails = mainService.getAccountDetails(accoutNumberDto);
            if (accountDetails == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Account not found", false))
                        .build();

            return Response.ok(new ApiResponse(accountDetails, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching Accounts", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occured", false))
                    .build();
        }
    }

    @GET
    @Path("/getpurposeofpaymentlist")
    public Response getPurposeofPaymentList() {
        logger.info("message : =====> getAll method hit");
        try {
            var result = mainService.getPurposeofPaymentList();
            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error in retrieving purpose of payment", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve transaction type", false))
                    .build();

        }
    }

    @GET
    @Path("/getbeneficiarybanklist")
    public Response getBeneficiaryBankList(@QueryParam("searchTerm") String searchTerm) {
        logger.info("message : =====> getBeneficiaryBankList method hit with search term: {}" + searchTerm);
        try {
            var result = mainService.getBeneficiaryBankList(searchTerm);
            if (result == null) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();
            }
            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error in retrieving beneficiary bank list", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve beneficiary bank list", false))
                    .build();
        }
    }

    @GET
    @Path("/getbeneficiaryaccountdetails")
    public Response getBeneficairyAccountDetails(
            @QueryParam("accOrIban") String accOrIban,
            @QueryParam("bankCode") String bankCode) {
        logger.info("message : =====> getBeneficiaryAccount method hit with accountNo: " + accOrIban);
        try {
            BeneficiaryAccountDto result = null;

            if (accOrIban != null && !accOrIban.trim().isEmpty()
                    && bankCode != null && !bankCode.trim().isEmpty())
                result = mainService.getBeneficiaryAccountDetails(accOrIban, bankCode);

            if (result == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Beneficiary Account not found", false))
                        .build();

            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error fetching Account details", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred", false))
                    .build();
        }
    }

    @GET
    @Path("/getexceptionlist")
    public Response getExceptionList(@QueryParam("activityCode") String activityCode) {
        logger.info("message : =====> getExceptionList method hit with activity Code" + activityCode);
        try {
            var result = mainService.getExceptionList(activityCode);
            if (result == null) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();
            }
            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error in retrieving beneficiary bank list", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve beneficiary bank list", false))
                    .build();
        }
    }

    @GET
    @Path("/getreasonlist")
    public Response getReasonList() {
        logger.info("message : =====> getReasonList method hit with activity Code");
        try {
            var result = mainService.getReasonList();
            if (result == null) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Data", false))
                        .build();
            }
            return Response.ok(new ApiResponse(result, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error in retrieving reason code list", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to retrieve reason code list", false))
                    .build();
        }
    }

    @GET
    @Path("/getunauthorizedoutwardrtgslist")
    public Response getUnauthorizedOutwardRTGSList(@QueryParam("branchCode") String branchCode) {
        logger.info("message : =====> getUnauthorizedOutwardRTGSList method hit");
        try {
            
            var rtgsList = mainService.getUnauthorizedOutwardRTGSList(branchCode);

            if (rtgsList == null || rtgsList.size() == 0) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "No Voucher exist for authorization", false))
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(rtgsList, null, true))
                    .build();

        } catch (Exception ex) {
            logger.error("Error submitting RTGS form", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred while submitting RTGS form", false))
                    .build();
        }
    }

    @GET
    @Path("/iscancelvoucherexist")
    public Response isCancelVoucherExist(
        @QueryParam("voucherNo") String voucherNo,
        @QueryParam("voucherYear") String voucherYear,
        @QueryParam("branchCode") String branchCode
    ) {
        logger.info("message : =====> isCancelVoucherExist method hit");
        try {
            
            boolean isVoucherExist = mainService.isCancelVoucherExist(voucherNo, voucherYear, branchCode);

            if (!isVoucherExist) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Voucher not found for cancellation", false))
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(isVoucherExist, null, true))
                    .build();

        } catch (Exception ex) {
            logger.error("Error submitting RTGS form", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred while submitting RTGS form", false))
                    .build();
        }
    }

    @POST
    @Path("/authorizeUser")
    public Response authorizeUser(AuthorizeUserDto authUserDto) {
        logger.info("message : =====> authorizeUser method hit with body " + authUserDto.toString());
        try {

            var isUserExist = mainService.isUserExist(authUserDto.getLoginId());
            if (!isUserExist)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid User Id", false))
                        .build();

            var roleId = mainService.getUserRole(authUserDto);

            if (roleId == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Password", false))
                        .build();

            if (roleId != 1)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "You are not allowed to perform this transaction", false))
                        .build();

            return Response.status(Response.Status.CREATED)
                    .entity(new ApiResponse(null, "User authorized successfully", true))
                    .build();

        } catch (Exception ex) {
            logger.error("Error submitting RTGS form", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred while submitting RTGS form", false))
                    .build();
        }
    }

    @POST
    @Path("/createOutwardRtgs")
    public Response createOutwardRtgs(OutwardRTGSFormDto rtgsForm) {
        logger.info("message : =====> submitOutwardRtgs method hit with body " + rtgsForm.toString());
        try {

            var createdLaxList = mainService.createOutwardRtgs(rtgsForm);

            if (createdLaxList == null || createdLaxList.getVoucherLaxList().size() == 0) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid RTGS Form Data", false))
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(new ApiResponse(createdLaxList, "RTGS form submitted successfully", true))
                    .build();

        } catch (Exception ex) {
            logger.error("Error submitting RTGS form", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error", false))
                    .build();
        }
    }

    @GET
    @Path("/getOutwardRTGS")
    public Response getOutwardRTGS(
        @QueryParam("voucherNo") String voucherNo, 
        @QueryParam("voucherYear") String voucherYear,
        @QueryParam("branchCode") String branchCode
    ) 
    {
        logger.info("message : =====> getOutwardRTGS method hit with voucherInfo " + voucherNo + voucherYear);
        try {

            if (voucherNo == null || voucherNo.isEmpty() || voucherNo.isBlank()
            || voucherYear == null || voucherYear.isEmpty() || voucherYear.isBlank()) 
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid Request Data", false))
                        .build();
            
            var outwardRTGS = mainService.getOutwardRTGS(voucherNo, voucherYear);

            if (outwardRTGS == null) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Invalid RTGS Form Data", false))
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(outwardRTGS, null, true))
                    .build();

        } catch (Exception ex) {
            logger.error("Error submitting RTGS form", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error Occurred while submitting RTGS form", false))
                    .build();
        }
    }

    @POST
    @Path("/cancelOutwardRtgs")
    public Response cancelOutwardRtgs(VoucherCancelDto voucherDto) {
        logger.info("message : =====> cancelRtgsTransaction method hit for voucherNo: {} and voucherYear: {}"
                + voucherDto.toString());
        try {
            if (voucherDto.getVoucherNo() == null ||
                    voucherDto.getVoucherNo().trim().isEmpty() ||
                    voucherDto.getVoucherYear() == null ||
                    voucherDto.getVoucherYear().trim().isEmpty()) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Voucher number is required for cancellation.", false))
                        .build();
            }

            // boolean isPostingUser = mainService.isPostingUser(voucherDto.getVoucherNo(),
            // voucherDto.getVoucherYear(), voucherDto.getLoginId());
            // if(isPostingUser)
            // return Response.ok(new ApiResponse(null, "Same user cannot cancel", false))
            // .build();

            boolean isCancelled = mainService.cancelOutwardRtgs(voucherDto.getVoucherNo(), voucherDto.getVoucherYear(),
                    voucherDto.getLoginId(), voucherDto.getCancellationReason());

            if (isCancelled) {
                return Response.ok(new ApiResponse(null, "RTGS transaction with voucher number  ", true))
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Voucher not found", false))
                    .build();

        } catch (Exception ex) {
            logger.error("Error cancelling RTGS transaction for voucherNo: {} and year: {}", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Error occurred while cancelling voucher.", false))
                    .build();
        }
    }

    @POST
    @Path("/authorizeOutwardRtgs")
    public Response authorizeOutwardRtgs(VoucherInfoDto voucherInfo) {
        logger.info("message : =====> authorizeRtgsTransaction method hit with voucher Info" + voucherInfo.toString());
        try {
            if (voucherInfo.getVoucherNo() == null || voucherInfo.getVoucherNo().isEmpty()
                    || voucherInfo.getVoucherYear() == null || voucherInfo.getVoucherYear().isEmpty()
                    || voucherInfo.getLoginId() == null || voucherInfo.getLoginId().isEmpty()) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Voucher number is required for authorization.", false))
                        .build();
            }

            boolean isBalanceSufficient = mainService.isBalanceSufficient(voucherInfo.getVoucherNo(),
                    voucherInfo.getVoucherYear(), voucherInfo.getLoginId());
            if (!isBalanceSufficient)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Insufficient funds in customer account", false))
                        .build();

            // boolean isPostingUser = mainService.isPostingUser(voucherInfo.getVoucherNo(),
            // voucherInfo.getVoucherYear(), voucherInfo.getLoginId());
            // if(isPostingUser)
            // return Response.ok(new ApiResponse(null, "Same user cannot authorize",
            // false))
            // .build();

            OutwardRTGSReponseDto outwardRTGSReponseDto = mainService.authorizeOutwardRtgs(voucherInfo.getVoucherNo(),
                    voucherInfo.getVoucherYear(), voucherInfo.getLoginId());

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(outwardRTGSReponseDto, null, true))
                    .build();
        } catch (Exception ex) {
            logger.error("Error authorizing RTGS transaction for voucher " + voucherInfo.toString(), ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error occurred while authorizing RTGS transaction.",
                            false))
                    .build();
        }
    }

    @GET
    @Path("/validatecheque")
    public Response validateCheque(
            @QueryParam("accountNo") String accountNo,
            @QueryParam("chequeNo") String chequeNo,
            @QueryParam("chequeDate") String chequeDate) {
        logger.info("message : =====> authorizeRtgsTransaction method hit for chequeNo: { " + chequeNo + " } ");
        try {
            if (chequeNo == null || chequeNo.trim().isEmpty() ||
                    accountNo == null || accountNo.trim().isEmpty())
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Cheque Number is required.", false))
                        .build();

            var detailsArray = accountNo.split("-");
            var accountNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2],
                    detailsArray[3], detailsArray[4]);

            String chequeStatus = mainService.getChequeStatus(accountNumberDto, chequeNo);

            if (chequeStatus == null)
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Cheque No " + chequeNo + " does not exist", false))
                        .build();

            else if (chequeStatus.equals("C")) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Cheque " + chequeNo + " has cancelled", false))
                        .build();
            } else if (chequeStatus.equals("P")) {
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Cheque " + chequeNo + " is already Paid", false))
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(chequeStatus, "null", true))
                    .build();

        } catch (Exception ex) {
            logger.error("Error authorizing RTGS transaction for chequeNo: { " + chequeNo + " }", ex);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Internal Server Error occurred while validating cheque.", false))
                    .build();
        }
    }

    @GET
    @Path("/getVoucherExcel")
    @Produces("application/json")
    public Response getVoucherExcel(
            @QueryParam("voucherNo") String voucherNo,
            @QueryParam("voucherYear") String voucherYear) {
        try {
            List<VoucherLaxDto> dataList = mainService.getTransactionLaxList(voucherNo, voucherYear);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Report");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Voucher No.");
            headerRow.createCell(1).setCellValue("Ref No");
            headerRow.createCell(2).setCellValue("Account No.");
            headerRow.createCell(3).setCellValue("A/C Title");
            headerRow.createCell(4).setCellValue("CCY");
            headerRow.createCell(5).setCellValue("DR");
            headerRow.createCell(6).setCellValue("CR");

            int rowNum = 1;
            for (VoucherLaxDto entity : dataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entity.getVoucherNo());
                row.createCell(1).setCellValue(entity.getReferenceNo());
                row.createCell(2).setCellValue(entity.getAccountNo());
                row.createCell(3).setCellValue(entity.getAccountNo());
                row.createCell(4).setCellValue(entity.getCurrencyCode());
                row.createCell(5).setCellValue(entity.getDebitAmount());
                row.createCell(6).setCellValue(entity.getCreditAmount());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            String base64Excel = Base64.getEncoder().encodeToString(out.toByteArray());

            return Response.ok(new ApiResponse(base64Excel, null, true))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to create report", false))
                    .build();
        }
    }

    @POST
    @Path("/downloadRtgsReport")
    @Produces("application/json")

    public Response downloadRtgsReport(ReportFilterDto reportFilters) {
        try {
            String base64Report;
            if (reportFilters.getReportFormat().equals("excel")) {
                List<RTGSReportDto> dataList = mainService.getRtgsExcelReportData(reportFilters);

                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("RTGSInquiry");

                // === Styles ===
                CellStyle titleStyle = workbook.createCellStyle();
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                titleStyle.setFont(boldFont);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                CellStyle centerStyle = workbook.createCellStyle();
                centerStyle.setFont(boldFont);
                centerStyle.setAlignment(HorizontalAlignment.CENTER);
                centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(boldFont);

                // === Merge & Write Bank Name ===
                // Merge A1:D3 (0–2 rows, 0–3 cols)
                sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 3));
                Row bankRow = sheet.createRow(0); // Middle row
                Cell bankCell = bankRow.createCell(0);
                bankCell.setCellValue("Bank Al Habib");
                bankCell.setCellStyle(titleStyle);

                // === Merge & Write RTGS Header and Dates ===
                // Merge E1:H1 (0, 4–7)
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 7));
                Row row0 = sheet.getRow(0);
                if (row0 == null)
                    row0 = sheet.createRow(0);
                Cell header1 = row0.createCell(4);
                header1.setCellValue("RTGS TRANSACTION DETAIL");
                header1.setCellStyle(centerStyle);

                // Merge E2:H2 (1, 4–7)
                // sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 7));
                // Cell header2 = bankRow.createCell(4);
                // header2.setCellValue("RTGS TRANSACTION DETAIL");
                // header2.setCellStyle(centerStyle);

                // Merge E3:H3 (2, 4–7)

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // Parse dates and find min and max
                Optional<LocalDate> minDate = dataList.stream()
                        .map(dto -> LocalDate.parse(dto.getTransactionDate(), formatter))
                        .min(Comparator.naturalOrder());

                Optional<LocalDate> maxDate = dataList.stream()
                        .map(dto -> LocalDate.parse(dto.getTransactionDate(), formatter))
                        .max(Comparator.naturalOrder());

                // Convert to formatted strings (optional)
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                StringBuilder dateRangeStringBuilder = new StringBuilder();
                dateRangeStringBuilder.append("From: ");
                if (!reportFilters.getIsFromDate() && minDate.isPresent()) {
                    String minDateStr = minDate.get().format(formatter2);
                    dateRangeStringBuilder.append(minDateStr);
                } else {
                    try {
                        LocalDate fromDate = LocalDate.parse(reportFilters.getFromDate(), formatter);
                        dateRangeStringBuilder.append(fromDate.format(formatter2));
                    } catch (DateTimeParseException e) {
                        dateRangeStringBuilder.append(reportFilters.getFromDate()); 
                    }
                }

                dateRangeStringBuilder.append("    To: ");
                if (!reportFilters.getIsToDate() && maxDate.isPresent()) {
                    String maxDateStr = maxDate.get().format(formatter2);
                    dateRangeStringBuilder.append(maxDateStr);
                } 
                else 
                {
                    try {
                        LocalDate toDate = LocalDate.parse(reportFilters.getToDate(), formatter);
                        dateRangeStringBuilder.append(toDate.format(formatter2));
                    } catch (DateTimeParseException e) {
                        dateRangeStringBuilder.append(reportFilters.getToDate()); // fallback
                    }
                }

                Row row2 = sheet.createRow(2);
                sheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 7));
                Cell header3 = row2.createCell(4);
                header3.setCellValue(dateRangeStringBuilder.toString());
                header3.setCellStyle(centerStyle);

                sheet.addMergedRegion(new CellRangeAddress(0, 0, 8, 10));
                Cell header4 = row0.createCell(8);
                header4.setCellValue("Report ID: OUTWARD RTGS");
                header4.setCellStyle(centerStyle);

                LocalDateTime currentDatetime = LocalDateTime.now();
                DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm:ss");
                String formattedDateTime = currentDatetime.format(dateTimeFormat);

                sheet.addMergedRegion(new CellRangeAddress(1, 1, 8, 10));
                Row row1 = sheet.getRow(1);
                if (row1 == null)
                    row1 = sheet.createRow(1);
                Cell header5 = row1.createCell(8);
                header5.setCellValue("Produce On: " + formattedDateTime);
                header5.setCellStyle(centerStyle);

                sheet.addMergedRegion(new CellRangeAddress(2, 2, 8, 10));
                Cell header6 = row2.createCell(8);
                header6.setCellValue("Produce By: " + reportFilters.getLoginId() + "    From Branch: "
                        + reportFilters.getBranchCode());
                header6.setCellStyle(centerStyle);

                Row headerRow = sheet.createRow(3);
                headerRow.createCell(0).setCellValue("Branch Cd");
                headerRow.createCell(1).setCellValue("Tran Dt");
                headerRow.createCell(2).setCellValue("Advice No/Year");
                headerRow.createCell(3).setCellValue("Tran. Type");
                headerRow.createCell(4).setCellValue("AccountNumber / AccountName");
                headerRow.createCell(5).setCellValue("Benef A/C No / IBAN / Name");
                headerRow.createCell(6).setCellValue("Beneficiary Address");
                // headerRow.createCell(7).setCellValue("Benef. Title Fetch Through 1-Link");
                headerRow.createCell(7).setCellValue("Benef. Bank Name");
                headerRow.createCell(8).setCellValue("VouchNo/Year");
                headerRow.createCell(9).setCellValue("Tran. Amount");
                headerRow.createCell(10).setCellValue("Status");

                for (int i = 0; i <= 10; i++) {
                    headerRow.getCell(i).setCellStyle(headerStyle);
                }

                int rowNum = 4;
                for (RTGSReportDto entity : dataList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entity.getBranchCode());
                    row.createCell(1).setCellValue(entity.getTransactionDate());
                    row.createCell(2).setCellValue(entity.getAdviceNo());
                    row.createCell(3).setCellValue(entity.getTransferType());
                    row.createCell(4).setCellValue(entity.getAccountInfo());
                    row.createCell(5).setCellValue(entity.getBeneficiaryAccountInfo());
                    row.createCell(6).setCellValue(entity.getBeneficiaryAddress());
                    // row.createCell(7).setCellValue(entity.isBeneficiaryTitleFetch());
                    row.createCell(7).setCellValue(entity.getBankName());
                    row.createCell(8).setCellValue(entity.getVoucherNo());
                    row.createCell(9).setCellValue(entity.getTotalAmount());
                    row.createCell(10).setCellValue(entity.getStatus());
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close();

                base64Report = Base64.getEncoder().encodeToString(out.toByteArray());
            } else {

                byte[] reportByteArray = birtService.generateRTGSReport(reportFilters);
                base64Report = Base64.getEncoder().encodeToString(reportByteArray);
            }

            return Response.ok(new ApiResponse(base64Report, null, true))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to create report", false))
                    .build();
        }
    }

    @POST
    @Path("/downloadVoucherReport")
    @Produces("application/json")
    public Response getVoucherReport(VoucherDto voucherInfo) {
        try {
            if (voucherInfo.getVoucherNo() == null || voucherInfo.getVoucherNo().trim().isEmpty() ||
                    voucherInfo.getVoucherYear() == null || voucherInfo.getVoucherYear().trim().isEmpty())
                return Response.status(Response.Status.OK)
                        .entity(new ApiResponse(null, "Voucher No and Voucher Year is required.", false))
                        .build();

            String base64Report;
            byte[] reportByteArray = birtService.generateVoucherReport(voucherInfo.getVoucherNo(),
                    voucherInfo.getVoucherYear());
            base64Report = Base64.getEncoder().encodeToString(reportByteArray);

            return Response.ok(new ApiResponse(base64Report, null, true))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse(null, "Failed to create report", false))
                    .build();
        }
    }

    // @GET
    // @Path("/downloadBirtReport")
    // @Produces("application/json")
    // public Response downloadBirtReport() {
    // try {
    // byte[] reportByteArray = birtService.generateReport();

    // String base64Report = Base64.getEncoder().encodeToString(reportByteArray);

    // return Response.ok(new ApiResponse(base64Report, null, true))
    // .build();

    // } catch (Exception e) {
    // e.printStackTrace();
    // return Response.status(Response.Status.OK)
    // .entity(new ApiResponse(null, "Failed to create report", false))
    // .build();
    // }
    // }

}
