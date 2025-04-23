package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import database.Datasources;
import dto.AccountDetailDto;
import dto.AccountNumberDto;
import dto.BeneficiaryAccountDto;
import dto.BeneficiaryTransactionLaxDto;
import dto.ExceptionDto;
import dto.OutwardRTGSFormDto;
import dto.OutwardRTGSReponseDto;
import dto.RTGSDto;
import dto.ListDto;
import dto.OutwardRTGSDto;
import dto.UserDto;
import dto.VoucherLaxDto;
import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import com.ibm.db2.jcc.DB2Diagnosable;
import com.ibm.db2.jcc.DB2Sqlca;

@ApplicationScoped
public class MainService {

    @Inject
    Datasources datasource;

    @Inject
    Logger logger;

    public UserDto getUser(String loginId) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT *, LPAD(BRANCH_CD, 4, '0') AS branch_cd_padded FROM USER_TL WHERE login_id = ?")) {

            preparedStatement.setString(1, loginId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserDto(
                            resultSet.getString("login_id"),
                            resultSet.getInt("role_id"),
                            resultSet.getString("branch_cd_padded"),
                            resultSet.getString("password"));
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving user with loginId " + loginId, e);
        }
        return null;
    }

    public UserDto createUser(UserDto user) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO USER_TL(login_id, role_id, branch_cd, password) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setString(1, user.getLoginId());
            preparedStatement.setInt(2, user.getRoleId());
            preparedStatement.setBigDecimal(3, new BigDecimal(user.getBranchCode()));
            preparedStatement.setString(4, user.getPassword());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return user;
            }
        } catch (SQLException ex) {
            logger.error("Unexpected error occurred while creating user", ex);
        }
        return null;
    }

    // public List<BranchDto> getAllBranches() {
    // List<BranchDto> branches = new ArrayList<>();
    // try (Connection connection = datasource.getConnection();
    // Statement statement = connection.createStatement();
    // ResultSet resultSet = statement.executeQuery("SELECT branch_cd as value,
    // branch_name || ' - ' ||branch_cd as label FROM BRANCH_TL")) {

    // while (resultSet.next()) {
    // branches.add(new BranchDto(resultSet.getInt("value"),
    // resultSet.getString("label")));
    // }
    // } catch (SQLException e) {
    // logger.error("Unexpected error occurred while retrieving branches", e);
    // }
    // return branches;
    // }

    public List<ListDto> getBranchList() {
        List<ListDto> optionList = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT branch_cd as value, branch_name || ' - ' ||branch_cd as label FROM BRANCH_TL")) {

            while (resultSet.next()) {
                optionList.add(new ListDto(resultSet.getString("label"), resultSet.getString("value")));
            }

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
        }
        return optionList;
    }

    public List<ListDto> getCurrencyList() {
        List<ListDto> optionList = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT currency_cd as value, currency_type || ' - ' || currency_cd as label FROM CURRENCY_TL")) {

            while (resultSet.next()) {
                optionList.add(new ListDto(resultSet.getString("label"), resultSet.getString("value")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing currency", e);
        }
        return optionList;
    }

    public List<ListDto> getSBPTranTypeList() {
        List<ListDto> optionList = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT sbp_transaction_type_cd as value, sbp_transaction_type || '-' || LPAD(sbp_transaction_type_cd , 3, '0') as label FROM SBP_TRANSACTION_TYPE_TL")) {

            while (resultSet.next()) {
                optionList.add(new ListDto(resultSet.getString("label"), resultSet.getString("value")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing transaction type", e);
        }
        return optionList;
    }

    public List<String> getCustomerAccountNos(String customerNo) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        // For ibm db2 testing remains
                        .prepareStatement("SELECT LPAD(branch_cd, 4, '0') || '-' || LPAD(account_type_cd, 4, '0') || '-' || LPAD(customer_no, 6, '0') || '-' || LPAD(run_no, 4, '0') || '-' || check_digit AS account_number FROM account_tl where customer_no = ?")) {
            preparedStatement.setString(1, customerNo);

            List<String> result = new ArrayList<String>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("account_number"));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving account with customer number ", e);
        }
        return null;
    }

    public AccountDetailDto getAccountDetails(AccountNumberDto accountNo) {
        StringBuilder query = new StringBuilder();

        //FOR MSSQL
        // query.append(" SELECT ");
        // query.append(" acctype.account_type + '-' + RIGHT('0000' + CAST(acc.account_type_cd AS VARCHAR), 4) AS acc_type, ");
        // query.append(" 'PK65BAHL' + ");
        // query.append(" RIGHT('0000' + CAST(acc.branch_cd AS VARCHAR), 4) + ");
        // query.append(" RIGHT('0000' + CAST(acc.account_type_cd AS VARCHAR), 4) + ");
        // query.append(" RIGHT('000000' + CAST(acc.customer_no AS VARCHAR), 6) + ");
        // query.append(" RIGHT('0000' + CAST(acc.run_no AS VARCHAR), 4) + ");
        // query.append(" CAST(acc.check_digit AS VARCHAR) AS iban, ");
        // query.append(" RIGHT('0000' + CAST(acc.branch_cd AS VARCHAR), 4) + '-' + ");
        // query.append(" RIGHT('0000' + CAST(acc.account_type_cd AS VARCHAR), 4) + '-' + ");
        // query.append(" RIGHT('000000' + CAST(acc.customer_no AS VARCHAR), 6) + '-' + ");
        // query.append(" RIGHT('0000' + CAST(acc.run_no AS VARCHAR), 4) + '-' + ");
        // query.append(" CAST(acc.check_digit AS VARCHAR) + '-' + acc.account_title AS acc_name, ");
        // query.append(" acc.available_bal ");
        // query.append(" FROM account_tl AS acc ");
        // query.append(" LEFT JOIN account_type_tl AS acctype ");
        // query.append(" ON acc.account_type_cd = acctype.account_type_cd ");
        // query.append(" WHERE acc.branch_cd = ? ");
        // query.append(" AND acc.account_type_cd = ? ");
        // query.append(" AND acc.customer_no = ? ");
        // query.append(" AND acc.run_no = ? ");
        // query.append(" AND acc.check_digit = ? ");

        query.append(" SELECT");
        query.append(" acctype.account_type || '-' || LPAD(acc.account_type_cd, 4,'0') AS acc_type,");
        query.append(" 'PK65BAHL' || LPAD(acc.branch_cd, 4, '0') || LPAD(acc.account_type_cd, 4, '0') || LPAD(acc.customer_no, 6, '0') || LPAD(acc.run_no, 4, '0') || acc.check_digit AS iban,");
        query.append(" LPAD(acc.branch_cd, 4, '0') || '-' || LPAD(acc.account_type_cd, 4, '0') || '-' || LPAD(acc.customer_no, 6, '0') || '-' || LPAD(acc.run_no, 4, '0') || '-' || acc.check_digit || '-' || acc.account_title AS acc_name,");
        query.append(" acc.available_bal");
        query.append(" FROM account_tl AS acc");
        query.append(" LEFT JOIN account_type_tl AS acctype");
        query.append(" ON acc.account_type_cd = acctype.account_type_cd");
        query.append(" WHERE acc.branch_cd = ?");
        query.append(" AND acc.account_type_cd = ?");
        query.append(" AND acc.customer_no = ?");
        query.append(" AND acc.run_no = ?");
        query.append(" AND acc.check_digit = ?");

        String sqlQuery = query.toString();

        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, accountNo.getBranchCode());
            preparedStatement.setString(2, accountNo.getAccountType());
            preparedStatement.setString(3, accountNo.getCustomerNo());
            preparedStatement.setString(4, accountNo.getRunNo());
            preparedStatement.setString(5, accountNo.getCheckDigit());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new AccountDetailDto(
                            resultSet.getString("acc_type"),
                            resultSet.getString("iban"),
                            resultSet.getString("acc_name"),
                            resultSet.getString("available_bal"));
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving branch with CD " + accountNo, e);
        }
        return null;
    }

    public List<ListDto> getPurposeofPaymentList() {
        List<ListDto> optionList = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT purpose_of_payment_cd as value, purpose_of_payment_type || ' - ' || purpose_of_payment_cd as label FROM PURPOSE_OF_PAYMENT_TL")) {

            while (resultSet.next()) {
                optionList.add(new ListDto(resultSet.getString("label"), resultSet.getString("value")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing purpose of payment", e);
        }
        return optionList;
    }
    

    


    public List<ListDto> getBeneficiaryBankList(String searchTerm) {
        List<ListDto> optionList = new ArrayList<>();
        String sql = "SELECT bank_cd as value, bank_name || ' - ' || bank_cd as label FROM BANKS_TL";
    
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql += " WHERE LOWER(bank_name) LIKE LOWER('%' || ? || '%') OR LOWER(bank_cd) LIKE LOWER('%' || ? || '%')";
        }
    
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
    
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                preparedStatement.setString(1, searchTerm);
                preparedStatement.setString(2, searchTerm);
            }
    
            ResultSet resultSet = preparedStatement.executeQuery();
    
            while (resultSet.next()) {
                optionList.add(new ListDto(resultSet.getString("label"), resultSet.getString("value")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing bank name", e);
        }
        return optionList;
    }

    public List<ExceptionDto> getExceptionList(String activityCode) {
        List<ExceptionDto> optionList = new ArrayList<>();
        String sql = "SELECT EXCEPTION_CD, EXCEPTION_DESC, ACTION_REQUIRED FROM exception_tl WHERE ACTIVITY_CD = ?";
    
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, activityCode);

            ResultSet resultSet = preparedStatement.executeQuery();
    
            while (resultSet.next()) {
                optionList.add(new ExceptionDto(resultSet.getString("EXCEPTION_CD"), resultSet.getString("EXCEPTION_DESC"), resultSet.getString("ACTION_REQUIRED")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while getting exception list for activity", e);
        }
        return optionList;
    }


    public BeneficiaryAccountDto getBeneficiaryAccountDetails(String AccOrIban, String BankCode) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT beneficiary_branch_cd, account_title FROM BENEFICIARY_ACCOUNT_TL WHERE ? IN (beneficiary_iban, BENEFICIARY_ACCOUNT_NO) AND bank_cd = ?")) {
            preparedStatement.setString(1, AccOrIban);
            preparedStatement.setString(2, BankCode);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new BeneficiaryAccountDto(
                            resultSet.getInt("beneficiary_branch_cd"),
                            resultSet.getString("account_title"));
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving beneficiary account with IBAN", e);
        }
        return null;
    }

    public OutwardRTGSDto getOutwardRTGS(String VoucherNo, String VoucherYear) throws Exception
    {
        OutwardRTGSDto outwardRTGS = new OutwardRTGSDto();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT LPAD(BRANCH_CD, 4, '0') || '-' || LPAD(ACCOUNT_TYPE_CD , 4, '0') || '-' ||");
        sb.append("LPAD(CUSTOMER_NO, 6, '0') || '-' || LPAD(RUN_NO, 4, '0') || '-' || ");
        sb.append("CHECK_DIGIT AS ACCOUNT_NO, BRANCH_CD, CURRENCY_CD, SBP_TRANSACTION_TYPE_CD, TRANSFER_TYPE, ");
        sb.append("CUSTOMER_NO, TOTAL_AMOUNT, PURPOSE_OF_PAYMENT_CD, REMARKS, PAYMENT_MODE FROM rtgs_tl ");
        sb.append("WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ? AND STATUS = ?");

        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setString(1, VoucherNo);
            preparedStatement.setString(2, VoucherYear);
            preparedStatement.setString(3, "U");

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                outwardRTGS = new OutwardRTGSDto(
                    resultSet.getString("ACCOUNT_NO"), 
                    resultSet.getString("BRANCH_CD"),
                    resultSet.getString("CURRENCY_CD"),
                    resultSet.getString("CUSTOMER_NO"),
                    resultSet.getString("PAYMENT_MODE"),
                    resultSet.getString("PURPOSE_OF_PAYMENT_CD"), 
                    resultSet.getString("REMARKS"), 
                    resultSet.getString("SBP_TRANSACTION_TYPE_CD"),
                    resultSet.getString("TRANSFER_TYPE"),
                    resultSet.getString("TOTAL_AMOUNT"),
                    new ArrayList<>()
                    );
            
            }
        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
             if (ex instanceof DB2Diagnosable) {
                 DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                 if (sqlca != null) {
                     errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                     ", SQLSTATE = " + sqlca.getSqlState() +
                                     ", Message = " + sqlca.getMessage();
                     logger.error(errorMessage, ex); // Log the detailed error
                 }
             }
             errorMessage += ex.getMessage();
              logger.error(errorMessage, ex);
        }


        StringBuilder sb2 = new StringBuilder();
        sb2.append("SELECT AMOUNT,btl.BANK_NAME, btl.BANK_CD, rbt.ACCOUNT_TITLE, rbt.ACCOUNT_NO, ");
        sb2.append("rbt.ADDRESS, rbt.BRANCH_CD FROM RTGS_BENEFICIARY_TL AS rbt LEFT JOIN BANKS_TL AS btl ON rbt.BANK_CD  = btl.BANK_CD ");
        sb2.append("WHERE rbt.VOUCHER_NO = ? AND rbt.VOUCHER_YEAR = ?");

        List<BeneficiaryTransactionLaxDto> transactionLaxList = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sb2.toString())) {
            preparedStatement.setString(1, VoucherNo);
            preparedStatement.setString(2, VoucherYear);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                transactionLaxList.add(
                    new BeneficiaryTransactionLaxDto(
                        resultSet.getString("AMOUNT"), 
                        resultSet.getString("BANK_NAME"),
                        resultSet.getString("BANK_CD"),
                        resultSet.getString("ACCOUNT_TITLE"),
                        resultSet.getString("ACCOUNT_NO"),
                        resultSet.getString("ADDRESS"),
                        resultSet.getString("BRANCH_CD")
                    )
                );
            }

            outwardRTGS.setTransactionLaxList(transactionLaxList);

        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
             if (ex instanceof DB2Diagnosable) {
                 DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                 if (sqlca != null) {
                     errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                     ", SQLSTATE = " + sqlca.getSqlState() +
                                     ", Message = " + sqlca.getMessage();
                     logger.error(errorMessage, ex); // Log the detailed error
                 }
             }
             errorMessage += ex.getMessage();
              logger.error(errorMessage, ex);
        }

        return outwardRTGS;
    }


    public List<RTGSDto> getUnauthorizedOutwardRTGSList()
    {
        List<RTGSDto> resultList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CURRENCY_TYPE, TOTAL_AMOUNT, POSTED_BY, POSTED_DATE, VOUCHER_NO || '/' || VOUCHER_YEAR AS VOUCHER_NO,");
        sb.append("'OUTBR' AS EXP_CODE FROM RTGS_TL AS RT LEFT JOIN CURRENCY_TL AS CT ON RT.CURRENCY_CD = CT.CURRENCY_CD ");
        sb.append("WHERE STATUS = ?");
    
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setString(1, "U");

            ResultSet resultSet = preparedStatement.executeQuery();
    
            while (resultSet.next()) {
                resultList.add(
                    new RTGSDto(
                        resultSet.getString("CURRENCY_TYPE"), 
                        resultSet.getString("TOTAL_AMOUNT"), 
                        resultSet.getString("POSTED_BY"),
                        resultSet.getString("POSTED_DATE"),
                        resultSet.getString("VOUCHER_NO"),
                        resultSet.getString("EXP_CODE")
                    )
                );
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while getting unauthorized rtgs list", e);
        }
        return resultList;
    }


    public OutwardRTGSReponseDto createOutwardRtgs(OutwardRTGSFormDto rtgsForm) throws Exception {

        var detailsArray = rtgsForm.getAccountNo().split("-");
        var accoutNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2], detailsArray[3], detailsArray[4]);

        LocalDate currentDate = LocalDate.now();
        String currentYearString = String.valueOf(currentDate.getYear());

        var localDateString = String.valueOf(LocalDate.now());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String localTimeString = LocalTime.now().format(timeFormatter);

        BigDecimal newVoucherNo = generateVoucherNo();
        
        StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO RTGS_TL (BRANCH_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT,");
            sb.append("VOUCHER_NO, VOUCHER_YEAR, SBP_TRANSACTION_TYPE_CD, CURRENCY_CD, TRANSFER_TYPE, PAYMENT_MODE,");
            sb.append("TOTAL_AMOUNT, POSTED_DATE, POSTED_TIME, POSTED_BY, PURPOSE_OF_PAYMENT_CD, REMARKS, STATUS)");
            sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {

                    preparedStatement.setBigDecimal(1, new BigDecimal(rtgsForm.getBranchCode()));
                    preparedStatement.setBigDecimal(2, new BigDecimal(accoutNumberDto.getAccountType()));
                    preparedStatement.setBigDecimal(3, new BigDecimal(rtgsForm.getCustomerNo()));
                    preparedStatement.setBigDecimal(4, new BigDecimal(accoutNumberDto.getRunNo()));
                    preparedStatement.setBigDecimal(5, new BigDecimal(accoutNumberDto.getCheckDigit()));
                    preparedStatement.setBigDecimal(6, newVoucherNo);
                    preparedStatement.setString(7, currentYearString);
                    preparedStatement.setBigDecimal(8, new BigDecimal(rtgsForm.getSbpTranTypeCode()));
                    preparedStatement.setBigDecimal(9, new BigDecimal(rtgsForm.getCurrencyCode()));
                    preparedStatement.setString(10, rtgsForm.getTransferTypeCode());
                    preparedStatement.setString(11, rtgsForm.getPaymentMode());
                    preparedStatement.setBigDecimal(12, new BigDecimal(rtgsForm.getTotalAmount()));
                    preparedStatement.setString(13, localDateString);
                    preparedStatement.setString(14, localTimeString);
                    preparedStatement.setString(15, rtgsForm.getLoginId());
                    preparedStatement.setString(16, rtgsForm.getPurposeOfPaymentCode());
                    preparedStatement.setString(17, rtgsForm.getRemarks());
                    preparedStatement.setString(18, "U");
    
    
                    int affectedRows = preparedStatement.executeUpdate();
                    logger.info("Affected No of rows is: ");
                    logger.info(affectedRows);
        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
             if (ex instanceof DB2Diagnosable) {
                 DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                 if (sqlca != null) {
                     errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                     ", SQLSTATE = " + sqlca.getSqlState() +
                                     ", Message = " + sqlca.getMessage();
                     logger.error(errorMessage, ex); // Log the detailed error
                 }
             }
             errorMessage += ex.getMessage();
              logger.error(errorMessage, ex);
        }

        StringBuilder sb2 = new StringBuilder();
        sb2.append("INSERT INTO RTGS_BENEFICIARY_TL (ACCOUNT_NO, ACCOUNT_TITLE, BANK_CD, BRANCH_CD, ADDRESS, ADVISE_NO, ");
        sb2.append("ADVISE_YEAR, VOUCHER_NO, VOUCHER_YEAR, AMOUNT) ");
        sb2.append("VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        var transactionLax = rtgsForm.getTransactionLaxList().get(0);
        var newAdviceNo = generateAdviseNo();
        try (Connection connection = datasource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sb2.toString())) {

            preparedStatement.setString(1, transactionLax.getBeneficiaryIbanOrAccountNo());
            preparedStatement.setString(2, transactionLax.getBeneficiaryAccountName());
            preparedStatement.setString(3, transactionLax.getBeneficiaryBankCode());
            preparedStatement.setString(4, transactionLax.getBeneficiaryBranchCode());
            preparedStatement.setString(5, transactionLax.getBeneficiaryAddress());
            preparedStatement.setBigDecimal(6, newAdviceNo);
            preparedStatement.setString(7, currentYearString);
            preparedStatement.setBigDecimal(8, newVoucherNo);
            preparedStatement.setString(9, currentYearString);
            preparedStatement.setString(10, transactionLax.getTransactionAmount());


            int affectedRows = preparedStatement.executeUpdate();
            logger.info("Affected No of rows is: ");
            logger.info(affectedRows);
        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                    ", SQLSTATE = " + sqlca.getSqlState() +
                                    ", Message = " + sqlca.getMessage();
                    logger.error(errorMessage, ex); // Log the detailed error
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }


        var newReferenceNo = generateRefNo();

        
        StringBuilder sb3 = new StringBuilder();
            sb3.append("INSERT INTO TRANSACTION_TL (BRANCH_CD, VOUCHER_NO, VOUCHER_YEAR, REFERENCE_NO,");
            sb3.append("ACCOUNT_TYPE_CD, TRAN_CODE, MAIN_HD, SUB_HD_1, SUB_HD_2,");
            sb3.append("GL_RUN_NO, GL_CHECK_DIGIT, AMOUNT, ACCOUNT_IDENTITY, IDENTITY, STATUS)");
            sb3.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            try (Connection connection = datasource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sb3.toString())) {
            preparedStatement.setString(1, "1001"); 
            preparedStatement.setBigDecimal(2, newVoucherNo); 
            preparedStatement.setString(3, currentYearString); 
            preparedStatement.setString(4, newReferenceNo); 
            preparedStatement.setString(5, accoutNumberDto.getAccountType());
            preparedStatement.setString(6, "80630");  
            preparedStatement.setBigDecimal(7,new BigDecimal("400")); 
            preparedStatement.setBigDecimal(8,new BigDecimal("25")); 
            preparedStatement.setBigDecimal(9,new BigDecimal("20"));  
            preparedStatement.setBigDecimal(10,new BigDecimal("4567"));  
            preparedStatement.setBigDecimal(11,new BigDecimal("2"));   
            preparedStatement.setBigDecimal(12, new BigDecimal(transactionLax.getTransactionAmount())); 
            preparedStatement.setString(13, "G");  
            preparedStatement.setString(14, "M"); 
            preparedStatement.setString(15, "A"); 

            int affectedRows = preparedStatement.executeUpdate();
            logger.info("Affected No of rows is: ");
            logger.info(affectedRows);
        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                    ", SQLSTATE = " + sqlca.getSqlState() +
                                    ", Message = " + sqlca.getMessage();
                    logger.error(errorMessage, ex); // Log the detailed error
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }

        newReferenceNo = incrementReferenceNo(newReferenceNo);
        StringBuilder sb4 = new StringBuilder();
        sb4.append("INSERT INTO TRANSACTION_TL (BRANCH_CD, VOUCHER_NO, VOUCHER_YEAR, REFERENCE_NO,");
        sb4.append("ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, TRAN_CODE,");
        sb4.append("AMOUNT, ACCOUNT_IDENTITY, STATUS)");
        sb4.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        try (Connection connection = datasource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sb4.toString())) {
            preparedStatement.setString(1, rtgsForm.getBranchCode()); 
            preparedStatement.setBigDecimal(2, newVoucherNo); 
            preparedStatement.setString(3, currentYearString); 
            preparedStatement.setString(4, newReferenceNo); 
            preparedStatement.setString(5, accoutNumberDto.getAccountType()); 
            preparedStatement.setString(6, accoutNumberDto.getCustomerNo()); 
            preparedStatement.setString(7, accoutNumberDto.getRunNo());
            preparedStatement.setString(8, accoutNumberDto.getCheckDigit()); 
            preparedStatement.setString(9, "30630");   
            preparedStatement.setBigDecimal(10, new BigDecimal(transactionLax.getTransactionAmount()).negate()); 
            preparedStatement.setString(11, "C");  
            preparedStatement.setString(12, "A"); 


            int affectedRows = preparedStatement.executeUpdate();
            logger.info("Affected No of rows is: ");
            logger.info(affectedRows);
        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                    ", SQLSTATE = " + sqlca.getSqlState() +
                                    ", Message = " + sqlca.getMessage();
                    logger.error(errorMessage, ex); // Log the detailed error
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }


        StringBuilder sb5 = new StringBuilder();
        sb5.append("SELECT Voucher_No || '/' || Voucher_year AS VOUCHER_NO, REFERENCE_NO, ");
        sb5.append("CASE WHEN AMOUNT < 0 THEN ABS(AMOUNT) ELSE 0 END AS CREDIT, ");
        sb5.append("CASE WHEN AMOUNT > 0 THEN AMOUNT ELSE 0 END AS DEBIT, ");
        sb5.append("CASE WHEN ACCOUNT_IDENTITY = 'C' THEN ");
        sb5.append("LPAD(branch_cd, 4, '0') || '-' || LPAD(account_type_cd, 4, '0') || '-' || ");
        sb5.append("LPAD(customer_no, 6, '0') || '-' || LPAD(run_no, 4, '0') || '-' || check_digit ");
        sb5.append("ELSE IDENTITY || '-' || LPAD(MAIN_HD, 3, '0') || '-' || LPAD(SUB_HD_1, 2, '0') || '-' || ");
        sb5.append("LPAD(SUB_HD_2, 2, '0') || '-' || LPAD(GL_RUN_NO, 4, '0') || '-' || GL_CHECK_DIGIT END AS ACCOUNT_NO, ");
        sb5.append("'PKR' AS CURRENCY_CODE ");
        sb5.append("FROM TRANSACTION_TL WHERE VOUCHER_NO=?");

        OutwardRTGSReponseDto result = new OutwardRTGSReponseDto();
        List<VoucherLaxDto> resultList = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sb5.toString())) {
            preparedStatement.setBigDecimal(1, newVoucherNo); 
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                resultList.add( new VoucherLaxDto(
                    resultSet.getString("VOUCHER_NO"),
                    resultSet.getString("REFERENCE_NO"),
                    resultSet.getString("CREDIT"),
                    resultSet.getString("DEBIT"),
                    resultSet.getString("ACCOUNT_NO"),
                    resultSet.getString("CURRENCY_CODE"))
                );
            }
            result.setVoucherLaxList(resultList);
        }
        catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                    ", SQLSTATE = " + sqlca.getSqlState() +
                                    ", Message = " + sqlca.getMessage();
                    logger.error(errorMessage, ex); // Log the detailed error
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }


        StringBuilder sb6 = new StringBuilder();
        sb6.append("SELECT ADVISE_NO FROM RTGS_BENEFICIARY_TL WHERE VOUCHER_NO = ?");

        try (Connection connection = datasource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sb6.toString())) {
            preparedStatement.setBigDecimal(1, newVoucherNo); 
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> resultList2 = new ArrayList<>(); 
            
            while (resultSet.next()) {
                resultList2.add(resultSet.getString("ADVISE_NO"));
            }

            result.setAdviceNoList(resultList2);
            return result;
        }
        catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                    ", SQLSTATE = " + sqlca.getSqlState() +
                                    ", Message = " + sqlca.getMessage();
                    logger.error(errorMessage, ex); // Log the detailed error
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }
               
        return null;
    }




    // public BeneficiaryAccountDto getBeneficiaryAccount(String beneficiaryAccountNo) {
    //     try (Connection connection = datasource.getConnection();
    //             PreparedStatement preparedStatement = connection
    //                     // For ibm db2 testing remains
    //                     .prepareStatement("SELECT beneficiary_iban, beneficiary_branch_cd, account_title FROM beneficiary_account_tl where beneficiary_account_no = ?")) {
    //         preparedStatement.setString(1, beneficiaryAccountNo);

    //         try (ResultSet resultSet = preparedStatement.executeQuery()) {
    //             if (resultSet.next()) {
    //                 return new BeneficiaryAccountDto(
    //                         resultSet.getString("beneficiary_iban"),
    //                         resultSet.getInt("beneficiary_branch_cd"),
    //                         resultSet.getString("account_title"));
    //             }
    //         }
    //         return null;
    //     } catch (Exception e) {
    //         logger.error("Unexpected error occurred while retrieving branch with CD " + beneficiaryAccountNo, e);
    //     }
    //     return null;
    // }
    
    public boolean cancelRtgsTransaction(String voucherNo, String voucherYear) {
        String updateSql = "UPDATE RTGS_TL SET STATUS = 'CANCELLED' WHERE VOUCHER_NO = ?";
        if (voucherYear != null && !voucherYear.trim().isEmpty()) {
            updateSql += " AND VOUCHER_YEAR = ?";
        }
        int rowsUpdated = 0;

        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {

            preparedStatement.setString(1, voucherNo);
            if (voucherYear != null && !voucherYear.trim().isEmpty()) {
                preparedStatement.setString(2, voucherYear);
            }
            rowsUpdated = preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            logger.error("Error updating RTGS transaction status to CANCELLED for voucherNo: {} and year: {}" + voucherNo + voucherYear, ex);
        }
        return rowsUpdated > 0;
    }


    public boolean authorizeRtgsTransaction(String voucherNo, String voucherYear) {
        String updateSql = "UPDATE RTGS_TL SET STATUS = 'AUTHORIZED' WHERE VOUCHER_NO = ?";
        if (voucherYear != null && !voucherYear.trim().isEmpty()) {
            updateSql += " AND VOUCHER_YEAR = ?";
        }
        int rowsUpdated = 0;

        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {

            preparedStatement.setString(1, voucherNo);
            if (voucherYear != null && !voucherYear.trim().isEmpty()) {
                preparedStatement.setString(2, voucherYear);
            }
            rowsUpdated = preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            logger.error("Error updating RTGS transaction status to AUTHORIZED for voucherNo: {} and year: {}" + voucherNo + voucherYear, ex);
        }
        return rowsUpdated > 0;
    }

    public boolean isChequeExist(AccountNumberDto accountDetails, String chequeNo){
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT START_NO from CHEQUEBOOK_TL WHERE branch_cd = ? AND account_type_cd = ? AND customer_no = ? AND run_no = ? AND check_digit = ?")) {
            preparedStatement.setString(1, accountDetails.getBranchCode());
            preparedStatement.setString(2, accountDetails.getAccountType());
            preparedStatement.setString(3, accountDetails.getCustomerNo());
            preparedStatement.setString(4, accountDetails.getRunNo());
            preparedStatement.setString(5, accountDetails.getCheckDigit());

            BigDecimal chequeStartNo = null;
            String test = null;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())  
                    chequeStartNo = resultSet.getBigDecimal("START_NO");
            }

            if(chequeStartNo != null)
            {
                chequeStartNo = chequeStartNo.add(BigDecimal.TEN); 
            }



        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving beneficiary account with IBAN", e);
        }
        return false;
    }

    private BigDecimal generateAdviseNo()
    {
        BigDecimal previousAdvice = BigDecimal.ZERO;
        String sqlQuery = "SELECT MAX(ADVISE_NO) AS ADVISE_NO FROM RTGS_BENEFICIARY_TL";
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            if (resultSet.next()) {
                previousAdvice = resultSet.getBigDecimal("ADVISE_NO");
            }
            
            if(previousAdvice == null)
            {
                previousAdvice = new BigDecimal(7070500);
            }

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
            return null;
        }
        return previousAdvice.add(BigDecimal.ONE);
    }

    private String generateRefNo()
    {
        String previousReference = "A1072";
        String sqlQuery = "SELECT MAX(REFERENCE_NO) AS REFERENCE_NO FROM TRANSACTION_TL";
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            if (resultSet.next()) {
                previousReference = resultSet.getString("REFERENCE_NO");
            }


        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving Reference No", e);
            return null;
        }
        return incrementReferenceNo(previousReference);
    }


    private BigDecimal generateVoucherNo()
    {
        BigDecimal previousVoucher = BigDecimal.ZERO;
        String sqlQuery = "SELECT VOUCHER_NO FROM RTGS_TL ORDER BY VOUCHER_NO DESC, POSTED_TIME DESC, POSTED_DATE DESC FETCH FIRST 1 ROW ONLY";
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            if (resultSet.next()) {
                previousVoucher = resultSet.getBigDecimal("VOUCHER_NO");
            }
            else
            {
                previousVoucher = new BigDecimal(386100);
            }

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
            return null;
        }
        return previousVoucher.add(BigDecimal.ONE);
    }

    private String incrementReferenceNo(String referenceNo)
    {
        if (referenceNo == null || referenceNo.isEmpty()) {
            return "A1073";  
        }

        try {
            String numericPartStr = referenceNo.substring(1);  
            int numericPart = Integer.parseInt(numericPartStr);
            numericPart++;
            return "A" + String.format("%04d", numericPart); 

        } catch (NumberFormatException e) {
            logger.error("Error incrementing reference number: Invalid format: "+ referenceNo.toString());
            return "A1073"; 
        }
    }
}

//SQL Queries written

// CREATE TABLE EXCEPTION_TL(EXCEPTION_CD DECIMAL(4,0), ACTIVITY_CD DECIMAL(4,0), EXCEPTION_DESC VARCHAR(50), ACTION_REQUIRED VARCHAR(40) )


// SELECT * FROM exception_tl

// INSERT INTO EXCEPTION_TL (EXCEPTION_CD, ACTIVITY_CD, EXCEPTION_DESC, ACTION_REQUIRED) 
// VALUES(10, 300, 'Invalid Account Number', 'Verify Account'),
//     (20, 200, 'Transaction Limit Exceeded', 'Authorize Transaction'),
//     (80, 100, 'Need Authorization', 'Require Authorization'),
//     (40, 400, 'Incorrect Amount', 'Check Amount');

