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
import jakarta.transaction.Transactional;
import database.DatabaseErrorHandler;
import database.Datasources;
import dto.AccountDetailDto;
import dto.AccountNumberDto;
import dto.AuthorizeUserDto;
import dto.BeneficiaryAccountDto;
import dto.BeneficiaryTransactionLaxDto;
import dto.ExceptionDto;
import dto.OutwardRTGSFormDto;
import dto.OutwardRTGSReponseDto;
import dto.RTGSDto;
import dto.RTGSReportDto;
import dto.ReportFilterDto;
import dto.ListDto;
import dto.LoginRequestDto;
import dto.OutwardRTGSDto;
import dto.UserDto;
import dto.UserInfoDto;
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

    public UserInfoDto getUser(LoginRequestDto dto) {
        try (Connection connection = datasource.getConnection()) {
            String sqlQuery = "SELECT LOGIN_ID, ROLE_ID, VARCHAR_FORMAT(LASTLOGGEDIN, 'DD/MM/YYYY') AS LASTLOGGEDIN, VARCHAR_FORMAT(DATE(CURRENT_TIMESTAMP), 'DD/MM/YYYY') AS CURRENTDATE, LPAD(BRANCH_CD, 4, '0') AS BRANCH_CD FROM USER_TL WHERE LOGIN_ID = ? AND BRANCH_CD = ? AND PASSWORD = ? ";
            try (
                    PreparedStatement preparedStatement = connection
                            .prepareStatement(sqlQuery)) {

                preparedStatement.setString(1, dto.getLoginId());
                preparedStatement.setString(2, dto.getBranchCode());
                preparedStatement.setString(3, dto.getPassword());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        UserInfoDto user = new UserInfoDto(
                                resultSet.getString("LOGIN_ID"),
                                resultSet.getString("BRANCH_CD"),
                                resultSet.getInt("ROLE_ID"),
                                resultSet.getString("LASTLOGGEDIN"),
                                resultSet.getString("CURRENTDATE"));

                        String updateQuery = "UPDATE USER_TL SET lastLoggedIn = CURRENT_TIMESTAMP WHERE login_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, dto.getLoginId());
                            updateStmt.executeUpdate();
                        }

                        return user;
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving user with loginId " + dto.toString(), e);
        }
        return null;
    }

    public boolean isUserExist(String loginId) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT LOGIN_ID FROM USER_TL WHERE login_id = ?")) {

            preparedStatement.setString(1, loginId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving user with loginId " + loginId, e);
        }
        return false;
    }

    public boolean isAuthUserExist(String loginId) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT LOGIN_ID FROM USER_TL WHERE LOGIN_ID = ? AND ROLE_ID=1")) {

            preparedStatement.setString(1, loginId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving user with loginId " + loginId, e);
        }
        return false;
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
                        "SELECT currency_cd as value, currency_type || ' - ' || currency_cd as label FROM CURRENCY_TL ")) {

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
                ResultSet resultSet = statement.executeQuery(
                        "SELECT sbp_transaction_type_cd as value, sbp_transaction_type || '-' || LPAD(sbp_transaction_type_cd , 3, '0') as label FROM SBP_TRANSACTION_TYPE_TL")) {

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
                        .prepareStatement(
                                "SELECT LPAD(branch_cd, 4, '0') || '-' || LPAD(account_type_cd, 4, '0') || '-' || LPAD(customer_no, 6, '0') || '-' || LPAD(run_no, 2, '0') || '-' || check_digit AS account_number FROM account_tl where customer_no = ?")) {
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

        // FOR MSSQL
        // query.append(" SELECT ");
        // query.append(" acctype.account_type + '-' + RIGHT('0000' +
        // CAST(acc.account_type_cd AS VARCHAR), 4) AS acc_type, ");
        // query.append(" 'PK65BAHL' + ");
        // query.append(" RIGHT('0000' + CAST(acc.branch_cd AS VARCHAR), 4) + ");
        // query.append(" RIGHT('0000' + CAST(acc.account_type_cd AS VARCHAR), 4) + ");
        // query.append(" RIGHT('000000' + CAST(acc.customer_no AS VARCHAR), 6) + ");
        // query.append(" RIGHT('0000' + CAST(acc.run_no AS VARCHAR), 4) + ");
        // query.append(" CAST(acc.check_digit AS VARCHAR) AS iban, ");
        // query.append(" RIGHT('0000' + CAST(acc.branch_cd AS VARCHAR), 4) + '-' + ");
        // query.append(" RIGHT('0000' + CAST(acc.account_type_cd AS VARCHAR), 4) + '-'
        // + ");
        // query.append(" RIGHT('000000' + CAST(acc.customer_no AS VARCHAR), 6) + '-' +
        // ");
        // query.append(" RIGHT('0000' + CAST(acc.run_no AS VARCHAR), 4) + '-' + ");
        // query.append(" CAST(acc.check_digit AS VARCHAR) + '-' + acc.account_title AS
        // acc_name, ");
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
                optionList.add(new ExceptionDto(resultSet.getString("EXCEPTION_CD"),
                        resultSet.getString("EXCEPTION_DESC"), resultSet.getString("ACTION_REQUIRED")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while getting exception list for activity", e);
        }
        return optionList;
    }
    public List<ListDto> getReasonList() {
        List<ListDto> optionList = new ArrayList<>();
        String sql = "SELECT REASON_CD as VALUE, REASON_DESC|| ' - ' || LPAD(REASON_CD,4,0) as label FROM REASON_TL  ORDER BY VALUE";

        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                optionList.add(
                    new ListDto(
                        resultSet.getString("label"),    
                        resultSet.getString("value")
                        ));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while getting exception list for activity", e);
        }
        return optionList;
    }
    

    public Integer getUserRole(AuthorizeUserDto authUserDto) {
        String sqlQuery = "SELECT * FROM USER_TL WHERE LOGIN_ID=? AND PASSWORD=?";

        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, authUserDto.getLoginId());
            preparedStatement.setString(2, authUserDto.getPassword());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("ROLE_ID");
            }

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while getting exception list for activity", e);
        }
        return null;
    }

    public BeneficiaryAccountDto getBeneficiaryAccountDetails(String AccOrIban, String BankCode) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement(
                                "SELECT beneficiary_branch_cd, account_title FROM BENEFICIARY_ACCOUNT_TL WHERE ? IN (beneficiary_iban, BENEFICIARY_ACCOUNT_NO) AND bank_cd = ?")) {
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

    public OutwardRTGSDto getOutwardRTGS(String VoucherNo, String VoucherYear) throws Exception {
        OutwardRTGSDto outwardRTGS = new OutwardRTGSDto();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT LPAD(BRANCH_CD, 4, '0') || '-' || LPAD(ACCOUNT_TYPE_CD , 4, '0') || '-' ||");
        sb.append("LPAD(CUSTOMER_NO, 6, '0') || '-' || LPAD(RUN_NO, 2, '0') || '-' || ");
        sb.append("CHECK_DIGIT AS ACCOUNT_NO, BRANCH_CD, CURRENCY_CD, SBP_TRANSACTION_TYPE_CD, TRANSFER_TYPE, ");
        sb.append("CUSTOMER_NO, TOTAL_AMOUNT, PURPOSE_OF_PAYMENT_CD, CHEQUE_NO, CHEQUE_DATE, SENDER_TO_RECEIVER_INFO, ");
        sb.append("REMARKS, PAYMENT_MODE, POSTED_BY FROM rtgs_tl WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ? AND STATUS = ?");

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
                        resultSet.getString("SENDER_TO_RECEIVER_INFO"),
                        resultSet.getString("CHEQUE_NO"),
                        resultSet.getString("CHEQUE_DATE"),
                        resultSet.getString("TRANSFER_TYPE"),
                        resultSet.getString("TOTAL_AMOUNT"),
                        resultSet.getString("POSTED_BY"),
                        new ArrayList<>());

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
        sb2.append("SELECT AMOUNT, btl.BANK_NAME, btl.BANK_CD, rbt.ACCOUNT_TITLE, rbt.ACC_OR_IBAN, ");
        sb2.append("rbt.ADDRESS, rbt.IS_BENEF_TITLE_FETCH, rbt.BRANCH_CD FROM RTGS_BENEFICIARY_TL AS rbt LEFT JOIN BANKS_TL AS btl ON rbt.BANK_CD  = btl.BANK_CD ");
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
                            resultSet.getString("ACC_OR_IBAN"),
                            resultSet.getString("ADDRESS"),
                            resultSet.getString("BRANCH_CD"),
                            resultSet.getBoolean("IS_BENEF_TITLE_FETCH")
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

    public boolean isCancelVoucherExist(String voucherNo, String voucherYear) {
        String query = "SELECT * FROM RTGS_TL WHERE VOUCHER_NO=? AND VOUCHER_YEAR=?  AND STATUS = ?";

        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, voucherNo);
            preparedStatement.setString(2, voucherYear);
            preparedStatement.setString(3, "U");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return true;
            }
        } 
        catch (SQLException e) {
            logger.error("Unexpected error occurred while getting unauthorized rtgs list", e);
        }
        return false;
    }

    public List<RTGSDto> getUnauthorizedOutwardRTGSList() {
        List<RTGSDto> resultList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CT.ALPHA_CD || '-' || CT.CURRENCY_CD AS CURRENCY, VARCHAR_FORMAT(TOTAL_AMOUNT, '999,999,999,999,999,999.00') AS TOTAL_AMOUNT, POSTED_BY, ");
        sb.append("VARCHAR_FORMAT(POSTED_DATE, 'DD/MM/YYYY') AS POSTED_DATE, VOUCHER_NO || '/' || VOUCHER_YEAR AS VOUCHER_NO, ");
        sb.append("'OUTBR' AS EXP_CODE FROM RTGS_TL AS RT LEFT JOIN CURRENCY_TL AS CT ON RT.CURRENCY_CD = CT.CURRENCY_CD ");
        sb.append("WHERE STATUS = ? ORDER BY POSTED_DATE DESC, VOUCHER_NO DESC");
        
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setString(1, "U");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                resultList.add(
                        new RTGSDto(
                                resultSet.getString("CURRENCY"),
                                resultSet.getString("TOTAL_AMOUNT"),
                                resultSet.getString("POSTED_BY"),
                                resultSet.getString("POSTED_DATE"),
                                resultSet.getString("VOUCHER_NO"),
                                resultSet.getString("EXP_CODE")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while getting unauthorized rtgs list", e);
        }
        return resultList;
    }

    @Transactional(rollbackOn = Exception.class)
    public OutwardRTGSReponseDto createOutwardRtgs(OutwardRTGSFormDto rtgsForm) throws Exception {

        var detailsArray = rtgsForm.getAccountNo().split("-");
        var accoutNumberDto = new AccountNumberDto(
            detailsArray[0], 
            detailsArray[1], 
            detailsArray[2], 
            detailsArray[3],
            detailsArray[4]
        );

        LocalDate currentDate = LocalDate.now();
        String currentYearString = String.valueOf(currentDate.getYear());

        var localDateString = String.valueOf(LocalDate.now());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String localTimeString = LocalTime.now().format(timeFormatter);

        BigDecimal newVoucherNo = generateVoucherNo();

        try (Connection connection = datasource.getConnection()) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO RTGS_TL (BRANCH_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT,");
            sb.append("VOUCHER_NO, VOUCHER_YEAR, SBP_TRANSACTION_TYPE_CD, CURRENCY_CD, TRANSFER_TYPE, PAYMENT_MODE,");
            sb.append("TOTAL_AMOUNT, POSTED_DATE, POSTED_TIME, POSTED_BY, PURPOSE_OF_PAYMENT_CD, ");
            sb.append("SENDER_TO_RECEIVER_INFO, CHEQUE_DATE, CHEQUE_NO, REMARKS, STATUS)");
            sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
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
                preparedStatement.setString(17, rtgsForm.getSenderToReceiverInformation());
                preparedStatement.setString(18, rtgsForm.getChequeDate());
                preparedStatement.setString(19, rtgsForm.getChequeNo());
                preparedStatement.setString(20, rtgsForm.getRemarks());
                preparedStatement.setString(21, "U");

                int affectedRows = preparedStatement.executeUpdate();
                logger.info("Affected No of rows is: ");
                logger.info(affectedRows);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            var transactionLaxList = rtgsForm.getTransactionLaxList();
            for (var transactionLax : transactionLaxList) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("INSERT INTO RTGS_BENEFICIARY_TL (ACC_OR_IBAN, ACCOUNT_TITLE, BANK_CD, ADDRESS, ADVISE_NO, ");
                sb2.append("ADVISE_YEAR, VOUCHER_NO, VOUCHER_YEAR, AMOUNT, IS_BENEF_TITLE_FETCH ");

                var isBranchCodeGiven = transactionLax.getBeneficiaryBranchCode() != ""; 
                if(isBranchCodeGiven)
                    sb2.append(", BRANCH_CD");

                sb2.append(") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

                if(isBranchCodeGiven)
                    sb2.append(", ?");
                
                sb2.append(")");

                try (PreparedStatement preparedStatement = connection.prepareStatement(sb2.toString())) {

                    BigDecimal currentAdviceNo = generateAdviseNo();
                    preparedStatement.setString(1, transactionLax.getBeneficiaryIbanOrAccountNo());
                    preparedStatement.setString(2, transactionLax.getBeneficiaryAccountName());
                    preparedStatement.setString(3, transactionLax.getBeneficiaryBankCode());
                    preparedStatement.setString(4, transactionLax.getBeneficiaryAddress());
                    preparedStatement.setBigDecimal(5, currentAdviceNo);
                    preparedStatement.setString(6, currentYearString);
                    preparedStatement.setBigDecimal(7, newVoucherNo);
                    preparedStatement.setString(8, currentYearString);
                    preparedStatement.setString(9, transactionLax.getTransactionAmount());
                    preparedStatement.setBoolean(10, transactionLax.isBeneficiaryTitleFetch());
                    
                    if(isBranchCodeGiven)
                        preparedStatement.setString(11, transactionLax.getBeneficiaryBranchCode());

                    preparedStatement.addBatch();

                    currentAdviceNo = currentAdviceNo.add(BigDecimal.ONE);
                    
                    int[] affectedRows = preparedStatement.executeBatch();
                    logger.info("Affected No of rows is: " + affectedRows.length);
                } catch (SQLException ex) {
                    DatabaseErrorHandler.log((SQLException) ex);
                    throw ex;
                }
            }

            // StringBuilder sb3 = new StringBuilder();
            // sb3.append("UPDATE ACCOUNT_TL SET AVAILABLE_BAL = AVAILABLE_BAL - ? ");
            // sb3.append("WHERE BRANCH_CD = ? AND ACCOUNT_TYPE_CD = ? ");
            // sb3.append("AND CUSTOMER_NO = ? AND RUN_NO = ? AND CHECK_DIGIT = ?");

            // try (PreparedStatement preparedStatement = connection.prepareStatement(sb3.toString())) {
            //     preparedStatement.setString(1, rtgsForm.getTotalAmount());
            //     preparedStatement.setString(2, accoutNumberDto.getBranchCode());
            //     preparedStatement.setString(3, accoutNumberDto.getAccountType());
            //     preparedStatement.setString(4, accoutNumberDto.getCustomerNo());
            //     preparedStatement.setString(5, accoutNumberDto.getRunNo());
            //     preparedStatement.setString(6, accoutNumberDto.getCheckDigit());

            //     int affectedRows = preparedStatement.executeUpdate();
            //     logger.info("Affected No of rows is: " + affectedRows);
            // } catch (SQLException ex) {
            //     DatabaseErrorHandler.log((SQLException) ex);
            //     throw ex;
            // }

            BigDecimal chequeStartNo = BigDecimal.ZERO;
            String sqlQuery = "SELECT START_NO FROM CHEQUEBOOK_TL WHERE ? BETWEEN START_NO AND (START_NO+9)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, rtgsForm.getChequeNo());

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    chequeStartNo = resultSet.getBigDecimal("START_NO");
                }

                logger.info("Affected No of rows is: " + chequeStartNo);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            BigDecimal StatusNo = new BigDecimal(rtgsForm.getChequeNo()).subtract(chequeStartNo);
            String query = String.format("UPDATE CHEQUEBOOK_TL SET STATUS_%s = ? WHERE CUSTOMER_NO = ?", StatusNo);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, "P");
                preparedStatement.setString(2, accoutNumberDto.getCustomerNo());

                int affectedRows = preparedStatement.executeUpdate();
                logger.info("Affected No of rows is: " + affectedRows);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                logger.error(ex);
                throw ex;
            }


            // StringBuilder sb10 = new StringBuilder();
            // sb10.append("UPDATE GL_ACCOUNT_TL SET AVAILABLE_BAL = AVAILABLE_BAL + ? ");
            // sb10.append("WHERE IDENTITY = ? AND BRANCH_CD = ? ");
            // sb10.append("AND MAIN_HD = ? AND SUB_HD_1 = ? AND SUB_HD_2 = ?");
            // sb10.append("AND GL_RUN_NO = ? AND GL_CHECK_DIGIT = ? ");

            // try (PreparedStatement preparedStatement = connection.prepareStatement(sb10.toString())) {
            //     preparedStatement.setString(1, rtgsForm.getTotalAmount());
            //     preparedStatement.setString(2, "M");
            //     preparedStatement.setString(3, "786");
            //     preparedStatement.setString(4, "230");
            //     preparedStatement.setString(5, "01");
            //     preparedStatement.setString(6, "01");
            //     preparedStatement.setString(7, "0586");
            //     preparedStatement.setString(8, "02");

            //     int affectedRows = preparedStatement.executeUpdate();
            //     logger.info("Affected No of rows is: " + affectedRows);
            // } catch (SQLException ex) {
            //     DatabaseErrorHandler.log((SQLException) ex);
            //     throw ex;
            // }

            var newReferenceNo = generateRefNo();
            StringBuilder sb5 = new StringBuilder();
            sb5.append("INSERT INTO TRANSACTION_TL (BRANCH_CD, VOUCHER_NO, VOUCHER_YEAR, REFERENCE_NO,");
            sb5.append("ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, TRAN_CODE, ");
            sb5.append("AMOUNT, ACCOUNT_IDENTITY, CURRENCY_CD, TRANSACTION_DATE, TRANSACTION_TIME, STATUS) ");
            sb5.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb5.toString())) {
                preparedStatement.setString(1, rtgsForm.getBranchCode());
                preparedStatement.setBigDecimal(2, newVoucherNo);
                preparedStatement.setString(3, currentYearString);
                preparedStatement.setString(4, newReferenceNo);
                preparedStatement.setString(5, accoutNumberDto.getAccountType());
                preparedStatement.setString(6, accoutNumberDto.getCustomerNo());
                preparedStatement.setString(7, accoutNumberDto.getRunNo());
                preparedStatement.setString(8, accoutNumberDto.getCheckDigit());
                preparedStatement.setString(9, "30630");
                preparedStatement.setBigDecimal(10, new BigDecimal(rtgsForm.getTotalAmount()).negate());
                preparedStatement.setString(11, "C");
                preparedStatement.setString(12, rtgsForm.getCurrencyCode());
                preparedStatement.setString(13, localDateString);
                preparedStatement.setString(14, localTimeString);
                preparedStatement.setString(15, "U");

                int affectedRows = preparedStatement.executeUpdate();
                logger.info("Affected No of rows is: ");
                logger.info(affectedRows);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            newReferenceNo = incrementReferenceNo(newReferenceNo);
            var instructionNo = generateInstructionNo();
            StringBuilder sb6 = new StringBuilder();
            sb6.append("INSERT INTO TRANSACTION_TL (BRANCH_CD, VOUCHER_NO, VOUCHER_YEAR, REFERENCE_NO,");
            sb6.append("TRAN_CODE, CURRENCY_CD, MAIN_HD, SUB_HD_1, SUB_HD_2,");
            sb6.append("GL_RUN_NO, GL_CHECK_DIGIT, OR_BRANCH_CD, INSTRUCTION_NO, INSTRUCTION_DATE, ");
            sb6.append("AMOUNT, ACCOUNT_IDENTITY, IDENTITY, TRANSACTION_DATE, TRANSACTION_TIME, STATUS) ");
            
            sb6.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb6.toString())) {
                preparedStatement.setString(1, rtgsForm.getBranchCode());
                preparedStatement.setBigDecimal(2, newVoucherNo);
                preparedStatement.setString(3, currentYearString);
                preparedStatement.setString(4, newReferenceNo);
                preparedStatement.setString(5, "80630");
                preparedStatement.setString(6, rtgsForm.getCurrencyCode());
                preparedStatement.setBigDecimal(7, new BigDecimal("230"));
                preparedStatement.setBigDecimal(8, new BigDecimal("01"));
                preparedStatement.setBigDecimal(9, new BigDecimal("01"));
                preparedStatement.setBigDecimal(10, new BigDecimal("0586"));
                preparedStatement.setBigDecimal(11, new BigDecimal("02"));
                preparedStatement.setBigDecimal(12, new BigDecimal("0786"));
                preparedStatement.setBigDecimal(13, instructionNo);
                preparedStatement.setString(14, localDateString);
                preparedStatement.setBigDecimal(15, new BigDecimal(rtgsForm.getTotalAmount()));
                preparedStatement.setString(16, "G");
                preparedStatement.setString(17, "M");
                preparedStatement.setString(18, localDateString);
                preparedStatement.setString(19, localTimeString);
                preparedStatement.setString(20, "U");

                int affectedRows = preparedStatement.executeUpdate();
                logger.info("Affected No of rows is: " + affectedRows);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            StringBuilder sb7 = new StringBuilder();
            sb7.append("SELECT Voucher_No || '/' || Voucher_year AS VOUCHER_NO, REFERENCE_NO, ");
            sb7.append("CASE WHEN AMOUNT < 0 THEN VARCHAR_FORMAT(ABS(AMOUNT), '999,999,999,999,999,999.00') ELSE '0.00' END AS DEBIT, ");
            sb7.append("CASE WHEN AMOUNT > 0 THEN VARCHAR_FORMAT(AMOUNT, '999,999,999,999,999,999.00') ELSE '0.00' END AS CREDIT, ");

            sb7.append("CASE WHEN T.ACCOUNT_IDENTITY = 'C' THEN LPAD(AC.branch_cd, 4, '0') || '-' || LPAD(AC.account_type_cd, 4, '0') || '-' || ");
            sb7.append("LPAD(AC.customer_no, 6, '0') || '-' || LPAD(AC.run_no, 2, '0') || '-' || AC.check_digit ");
            sb7.append("ELSE GL.IDENTITY || '-' || LPAD(GL.MAIN_HD, 3, '0') || '-' || LPAD(GL.SUB_HD_1, 2, '0') || '-' || ");
            sb7.append("LPAD(GL.SUB_HD_2, 2, '0') || '-' || LPAD(GL.GL_RUN_NO, 4, '0') || '-' || GL.GL_CHECK_DIGIT END AS ACCOUNT_NO,");


            sb7.append("CASE WHEN T.ACCOUNT_IDENTITY = 'C' THEN AC.account_title ELSE GL.account_title END AS ACCOUNT_TITLE, ");
            sb7.append("CT.ALPHA_CD AS CURRENCY_CODE FROM TRANSACTION_TL T LEFT JOIN CURRENCY_TL CT ON T.CURRENCY_CD = CT.CURRENCY_CD ");


            sb7.append("LEFT JOIN ACCOUNT_TL AC ON T.branch_cd = AC.branch_cd AND T.account_type_cd = AC.account_type_cd ");
            sb7.append("AND T.customer_no = AC.customer_no  AND T.run_no = AC.run_no AND T.check_digit = AC.check_digit ");
            sb7.append("AND T.ACCOUNT_IDENTITY = 'C'");

            sb7.append("LEFT JOIN GL_ACCOUNT_TL GL ON T.MAIN_HD = GL.MAIN_HD AND T.SUB_HD_1 = GL.SUB_HD_1 AND T.SUB_HD_2 = GL.SUB_HD_2 ");
            sb7.append("AND T.GL_RUN_NO = GL.GL_RUN_NO AND T.GL_CHECK_DIGIT = GL.GL_CHECK_DIGIT AND T.IDENTITY = GL.IDENTITY AND T.OR_BRANCH_CD = GL.BRANCH_CD ");
            sb7.append("AND T.ACCOUNT_IDENTITY = 'G'");

            sb7.append("WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?");


            OutwardRTGSReponseDto result = new OutwardRTGSReponseDto();
            List<VoucherLaxDto> resultList = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sb7.toString())) {
                preparedStatement.setBigDecimal(1, newVoucherNo);
                preparedStatement.setString(2, currentYearString);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    resultList.add(new VoucherLaxDto(
                            resultSet.getString("VOUCHER_NO"),
                            resultSet.getString("REFERENCE_NO"),
                            resultSet.getString("CREDIT"),
                            resultSet.getString("DEBIT"),
                            resultSet.getString("ACCOUNT_NO"),
                            resultSet.getString("ACCOUNT_TITLE"),
                            resultSet.getString("CURRENCY_CODE")));
                }
                result.setVoucherLaxList(resultList);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            StringBuilder sb8 = new StringBuilder();
            sb8.append("SELECT ADVISE_NO FROM RTGS_BENEFICIARY_TL WHERE VOUCHER_NO = ?");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb8.toString())) {
                preparedStatement.setBigDecimal(1, newVoucherNo);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<String> resultList2 = new ArrayList<>();

                while (resultSet.next()) {
                    resultList2.add(resultSet.getString("ADVISE_NO"));
                }

                result.setAdviceNoList(resultList2);
                return result;
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

        } catch (Exception ex) {
            logger.info("Generic exception occurred", ex);
            throw ex;
        }
    }

    public boolean isBalanceSufficient(String voucherNo, String voucherYear, String loginId) throws Exception
    {
        int rowsUpdated = 0;
        var localDateString = String.valueOf(LocalDate.now());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String localTimeString = LocalTime.now().format(timeFormatter);

        try (Connection connection = datasource.getConnection()) 
        {
            AccountNumberDto accountNo = new AccountNumberDto();
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal availabeBalance = BigDecimal.ZERO;

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT BRANCH_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, ");
            sb.append("TOTAL_AMOUNT FROM RTGS_TL WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ? AND STATUS = ?");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
                preparedStatement.setString(1, voucherNo);
                preparedStatement.setString(2, voucherYear);
                preparedStatement.setString(3, "U");

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    accountNo.setBranchCode(resultSet.getString("BRANCH_CD"));
                    accountNo.setAccountType(resultSet.getString("ACCOUNT_TYPE_CD"));
                    accountNo.setCustomerNo(resultSet.getString("CUSTOMER_NO"));
                    accountNo.setRunNo(resultSet.getString("RUN_NO"));
                    accountNo.setCheckDigit(resultSet.getString("CHECK_DIGIT"));
                    totalAmount = resultSet.getBigDecimal("TOTAL_AMOUNT");
                }
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }
            
            StringBuilder sb2 = new StringBuilder();
            sb2.append("SELECT AVAILABLE_BAL FROM ACCOUNT_TL WHERE BRANCH_CD=?  ");
            sb2.append("AND ACCOUNT_TYPE_CD=? AND CUSTOMER_NO=? AND RUN_NO=? AND CHECK_DIGIT=? ");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb2.toString())) {
                preparedStatement.setString(1, accountNo.getBranchCode());
                preparedStatement.setString(2, accountNo.getAccountType());
                preparedStatement.setString(3, accountNo.getCustomerNo());
                preparedStatement.setString(4, accountNo.getRunNo());
                preparedStatement.setString(5, accountNo.getCheckDigit());

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    availabeBalance = resultSet.getBigDecimal("AVAILABLE_BAL");
                }
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }
            var resultDummy = availabeBalance.compareTo(totalAmount); 
            if (availabeBalance.compareTo(totalAmount) >= 0) {
                return true;
            }

            
            return false;
        } catch (Exception ex) {
            logger.info("Generic exception occurred", ex);
            throw ex;
        }
    }

    public List<VoucherLaxDto> getTransactionLaxList(String voucherNo, String voucherYear) throws Exception {
        StringBuilder sb = new StringBuilder();
            sb.append("SELECT Voucher_No || '/' || Voucher_year AS VOUCHER_NO, REFERENCE_NO, ");
            sb.append("CASE WHEN AMOUNT < 0 THEN VARCHAR_FORMAT(ABS(AMOUNT), '999,999,999,999,999,999.00') ELSE '0.00' END AS DEBIT, ");
            sb.append("CASE WHEN AMOUNT > 0 THEN VARCHAR_FORMAT(AMOUNT, '999,999,999,999,999,999.00') ELSE '0.00' END AS CREDIT, ");

            sb.append("CASE WHEN T.ACCOUNT_IDENTITY = 'C' THEN LPAD(AC.branch_cd, 4, '0') || '-' || LPAD(AC.account_type_cd, 4, '0') || '-' || ");
            sb.append("LPAD(AC.customer_no, 6, '0') || '-' || LPAD(AC.run_no, 2, '0') || '-' || AC.check_digit ");
            sb.append("ELSE GL.IDENTITY || '-' || LPAD(GL.MAIN_HD, 3, '0') || '-' || LPAD(GL.SUB_HD_1, 2, '0') || '-' || ");
            sb.append("LPAD(GL.SUB_HD_2, 2, '0') || '-' || LPAD(GL.GL_RUN_NO, 4, '0') || '-' || GL.GL_CHECK_DIGIT END AS ACCOUNT_NO,");


            sb.append("CASE WHEN T.ACCOUNT_IDENTITY = 'C' THEN AC.account_title ELSE GL.account_title END AS ACCOUNT_TITLE, ");
            sb.append("CT.ALPHA_CD  AS CURRENCY_CODE FROM TRANSACTION_TL T LEFT JOIN CURRENCY_TL CT ON T.CURRENCY_CD = CT.CURRENCY_CD ");


            sb.append("LEFT JOIN ACCOUNT_TL AC ON T.branch_cd = AC.branch_cd AND T.account_type_cd = AC.account_type_cd ");
            sb.append("AND T.customer_no = AC.customer_no  AND T.run_no = AC.run_no AND T.check_digit = AC.check_digit ");
            sb.append("AND T.ACCOUNT_IDENTITY = 'C'");

            sb.append("LEFT JOIN GL_ACCOUNT_TL GL ON T.MAIN_HD = GL.MAIN_HD AND T.SUB_HD_1 = GL.SUB_HD_1 AND T.SUB_HD_2 = GL.SUB_HD_2 ");
            sb.append("AND T.GL_RUN_NO = GL.GL_RUN_NO AND T.GL_CHECK_DIGIT = GL.GL_CHECK_DIGIT AND T.IDENTITY = GL.IDENTITY AND T.BRANCH_CD = GL.BRANCH_CD ");
            sb.append("AND T.ACCOUNT_IDENTITY = 'G'");

            sb.append("WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?");

        OutwardRTGSReponseDto result = new OutwardRTGSReponseDto();
        List<VoucherLaxDto> resultList = new ArrayList<>();
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setString(1, voucherNo);
            preparedStatement.setString(2, voucherYear);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                resultList.add(new VoucherLaxDto(
                        resultSet.getString("VOUCHER_NO"),
                        resultSet.getString("REFERENCE_NO"),
                        resultSet.getString("CREDIT"),
                        resultSet.getString("DEBIT"),
                        resultSet.getString("ACCOUNT_NO"),
                        resultSet.getString("ACCOUNT_TITLE"),
                        resultSet.getString("CURRENCY_CODE")));
            }
            result.setVoucherLaxList(resultList);

        } catch (SQLException ex) {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                            ", SQLSTATE = " + sqlca.getSqlState() +
                            ", Message = " + sqlca.getMessage();
                    logger.error(errorMessage, ex);
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }
        return resultList;
    }

    public List<RTGSReportDto> getRtgsExcelReportData(ReportFilterDto reportFilters) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT RBT.ADVISE_NO || '/' || RBT.ADVISE_YEAR AS ADVICE_NO,");
        sql.append("LPAD(RT.BRANCH_CD, 4, '0') AS BRANCH_CD, TT.TRANSACTION_DATE, TT.TRANSACTION_TIME, CASE WHEN RT.TRANSFER_TYPE = 103 THEN RT.TRANSFER_TYPE || ' (Single)' ELSE RT.TRANSFER_TYPE || ' (Multiple)' END AS TRANSFER_TYPE, ");
        sql.append("LPAD(AT.BRANCH_CD, 4, '0') || '-' || LPAD(AT.ACCOUNT_TYPE_CD, 4, '0') || '-' || LPAD(AT.CUSTOMER_NO, 6, '0') || '-' || LPAD(AT.RUN_NO, 2, '0') || '-' || AT.CHECK_DIGIT || '/' || AT.ACCOUNT_TITLE AS ACCOUNT_INFO, ");
        sql.append("RBT.ADDRESS AS BENEFICIARY_ADDRESS, RBT.IS_BENEF_TITLE_FETCH, BT.BANK_NAME, RT.VOUCHER_NO || '/' || RT.VOUCHER_YEAR AS VOUCHER_NO, ");
        sql.append("VARCHAR_FORMAT(RT.TOTAL_AMOUNT, '999,999,999,999,999,999.00') as TOTAL_AMOUNT, STTT.SBP_TRANSACTION_TYPE, RBT.ACC_OR_IBAN || '/' || RBT.ACCOUNT_TITLE AS BENEF_ACCOUNT_INFO, ");
        sql.append("CASE RT.STATUS WHEN 'U' THEN 'Unauthorize' WHEN 'A' THEN 'Authorized' ELSE 'Cancelled' END AS STATUS ");
        sql.append("FROM RTGS_BENEFICIARY_TL AS RBT LEFT JOIN RTGS_TL AS RT ON RBT.VOUCHER_NO = RT.VOUCHER_NO ");
        sql.append("LEFT JOIN SBP_TRANSACTION_TYPE_TL AS STTT ON STTT.SBP_TRANSACTION_TYPE_CD = RT.SBP_TRANSACTION_TYPE_CD ");
        sql.append("LEFT JOIN ACCOUNT_TL AS AT ON AT.BRANCH_CD = RT.BRANCH_CD AND AT.ACCOUNT_TYPE_CD = RT.ACCOUNT_TYPE_CD ");
        sql.append("AND AT.CUSTOMER_NO = RT.CUSTOMER_NO AND AT.RUN_NO = RT.RUN_NO AND AT.CHECK_DIGIT = RT.CHECK_DIGIT ");
        sql.append("LEFT JOIN TRANSACTION_TL AS TT ON RT.VOUCHER_NO = TT.VOUCHER_NO ");
        sql.append("LEFT JOIN BANKS_TL AS BT ON RBT.BANK_CD = BT.BANK_CD WHERE 1=1 ");


        List<Object> params = new ArrayList<>();

        if (reportFilters.getBranchCode() != null && !reportFilters.getBranchCode().isEmpty()) {
            sql.append(" AND RT.BRANCH_CD = ?");
            params.add(reportFilters.getBranchCode());
        }

        // if (reportFilters.getRtgsType() != null && !reportFilters.getRtgsType().isEmpty()) {
        //     sql.append(" AND rtgs_type = ?");
        //     params.add(reportFilters.getRtgsType());
        // }

        if (reportFilters.getTransactionType() != null && !"all".equalsIgnoreCase(reportFilters.getTransactionType())) {
            sql.append(" AND RT.TRANSFER_TYPE = ?");
            params.add(reportFilters.getTransactionType());
        }

        if (Boolean.TRUE.equals(reportFilters.getIsFromDate())) {
            sql.append(" AND TT.TRANSACTION_DATE >= ?");
            params.add(reportFilters.getFromDate());
        }

        if (Boolean.TRUE.equals(reportFilters.getIsToDate())) {
            sql.append(" AND TT.TRANSACTION_DATE <= ?");
            params.add(reportFilters.getToDate());
        }

        if (reportFilters.getStatus() != null && !"all".equalsIgnoreCase(reportFilters.getStatus())) {
            sql.append(" AND RT.STATUS =  ?");
            params.add(reportFilters.getStatus());
        }

        
        if (reportFilters.getAccountNo() != null && !reportFilters.getAccountNo().isEmpty()) {
            var detailsArray = reportFilters.getAccountNo().split("-");
            var accountNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2], detailsArray[3], detailsArray[4]);
    
            sql.append(" AND AT.BRANCH_CD = ?");
            params.add(accountNumberDto.getBranchCode());

            sql.append(" AND AT.ACCOUNT_TYPE_CD = ?");
            params.add(accountNumberDto.getAccountType());

            sql.append(" AND AT.CUSTOMER_NO = ?");
            params.add(accountNumberDto.getCustomerNo());

            sql.append(" AND AT.RUN_NO = ?");
            params.add(accountNumberDto.getRunNo());

            sql.append(" AND AT.CHECK_DIGIT = ?");
            params.add(accountNumberDto.getCheckDigit());            
        }

        sql.append(" ORDER BY TT.TRANSACTION_DATE, TT.TRANSACTION_TIME ");

        List<RTGSReportDto> resultList = new ArrayList<>();
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())
        ) 
        {
            for(int i = 0; i < params.size(); i++)
            {
                preparedStatement.setObject(i+1, params.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                resultList.add(new RTGSReportDto(
                        resultSet.getString("ADVICE_NO"),
                        resultSet.getString("BRANCH_CD"),
                        resultSet.getString("TRANSACTION_DATE"),
                        resultSet.getString("TRANSFER_TYPE"),
                        resultSet.getString("ACCOUNT_INFO"),
                        resultSet.getString("BENEFICIARY_ADDRESS"),
                        
                        resultSet.getBoolean("IS_BENEF_TITLE_FETCH"),
                        resultSet.getString("BANK_NAME"),
                        resultSet.getString("VOUCHER_NO"),
                        resultSet.getString("TOTAL_AMOUNT"),
                        resultSet.getString("SBP_TRANSACTION_TYPE"),
                        resultSet.getString("BENEF_ACCOUNT_INFO"),
                        resultSet.getString("STATUS")
                        )
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
                    logger.error(errorMessage, ex);
                }
            }
            errorMessage += ex.getMessage();
            logger.error(errorMessage, ex);
        }
        return resultList;
    }

    // public BeneficiaryAccountDto getBeneficiaryAccount(String
    // beneficiaryAccountNo) {
    // try (Connection connection = datasource.getConnection();
    // PreparedStatement preparedStatement = connection
    // // For ibm db2 testing remains
    // .prepareStatement("SELECT beneficiary_iban, beneficiary_branch_cd,
    // account_title FROM beneficiary_account_tl where beneficiary_account_no = ?"))
    // {
    // preparedStatement.setString(1, beneficiaryAccountNo);

    // try (ResultSet resultSet = preparedStatement.executeQuery()) {
    // if (resultSet.next()) {
    // return new BeneficiaryAccountDto(
    // resultSet.getString("beneficiary_iban"),
    // resultSet.getInt("beneficiary_branch_cd"),
    // resultSet.getString("account_title"));
    // }
    // }
    // return null;
    // } catch (Exception e) {
    // logger.error("Unexpected error occurred while retrieving branch with CD " +
    // beneficiaryAccountNo, e);
    // }
    // return null;
    // }

    @Transactional(rollbackOn = Exception.class)
    public boolean cancelOutwardRtgs(String voucherNo, String voucherYear, String loginId, String cancellationReason) throws Exception {

        try (Connection connection = datasource.getConnection()) {
            AccountNumberDto accountNo = new AccountNumberDto();
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal currencyCode = BigDecimal.ZERO;

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT BRANCH_CD, CURRENCY_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, ");
            sb.append("TOTAL_AMOUNT FROM RTGS_TL WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ? AND STATUS = ?");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
                preparedStatement.setString(1, voucherNo);
                preparedStatement.setString(2, voucherYear);
                preparedStatement.setString(3, "U");

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    accountNo.setBranchCode(resultSet.getString("BRANCH_CD"));
                    accountNo.setAccountType(resultSet.getString("ACCOUNT_TYPE_CD"));
                    accountNo.setCustomerNo(resultSet.getString("CUSTOMER_NO"));
                    accountNo.setRunNo(resultSet.getString("RUN_NO"));
                    accountNo.setCheckDigit(resultSet.getString("CHECK_DIGIT"));
                    totalAmount = resultSet.getBigDecimal("TOTAL_AMOUNT");
                    currencyCode = resultSet.getBigDecimal("CURRENCY_CD");
                }
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            
            // StringBuilder sb2 = new StringBuilder();
            // sb2.append("UPDATE ACCOUNT_TL SET AVAILABLE_BAL = AVAILABLE_BAL + ? ");
            // sb2.append("WHERE BRANCH_CD = ? AND ACCOUNT_TYPE_CD = ? ");
            // sb2.append("AND CUSTOMER_NO = ? AND RUN_NO = ? AND CHECK_DIGIT = ?");

            // try (PreparedStatement preparedStatement = connection.prepareStatement(sb2.toString())) {
            //     preparedStatement.setBigDecimal(1, totalAmount);
            //     preparedStatement.setString(2, accountNo.getBranchCode());
            //     preparedStatement.setString(3, accountNo.getAccountType());
            //     preparedStatement.setString(4, accountNo.getCustomerNo());
            //     preparedStatement.setString(5, accountNo.getRunNo());
            //     preparedStatement.setString(6, accountNo.getCheckDigit());

            //     int affectedRows = preparedStatement.executeUpdate();
            //     logger.info("Affected No of rows is: " + affectedRows);
            // } catch (SQLException ex) {
            //     DatabaseErrorHandler.log((SQLException) ex);
            //     throw ex;
            // }

            // StringBuilder sb10 = new StringBuilder();
            // sb10.append("UPDATE GL_ACCOUNT_TL SET AVAILABLE_BAL = AVAILABLE_BAL - ? ");
            // sb10.append("WHERE IDENTITY = ? AND BRANCH_CD = ? ");
            // sb10.append("AND MAIN_HD = ? AND SUB_HD_1 = ? AND SUB_HD_2 = ?");
            // sb10.append("AND GL_RUN_NO = ? AND GL_CHECK_DIGIT = ? ");

            // try (PreparedStatement preparedStatement = connection.prepareStatement(sb10.toString())) {
            //     preparedStatement.setBigDecimal(1, totalAmount);
            //     preparedStatement.setString(2, "M");
            //     preparedStatement.setString(3, "0786");
            //     preparedStatement.setString(4, "230");
            //     preparedStatement.setString(5, "01");
            //     preparedStatement.setString(6, "01");
            //     preparedStatement.setString(7, "0586");
            //     preparedStatement.setString(8, "02");

            //     int affectedRows = preparedStatement.executeUpdate();
            //     logger.info("Affected No of rows is: " + affectedRows);
            // } catch (SQLException ex) {
            //     DatabaseErrorHandler.log((SQLException) ex);
            //     throw ex;
            // }
            
            var localDateString = String.valueOf(LocalDate.now());
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String localTimeString = LocalTime.now().format(timeFormatter);

            String sql = "UPDATE TRANSACTION_TL SET STATUS=? WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) 
            {
                preparedStatement.setString(1, "C");
                preparedStatement.setString(2, voucherNo);
                preparedStatement.setString(3, voucherYear);

                preparedStatement.executeUpdate();

            } 
            catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            // var newReferenceNo = generateRefNo();
            // StringBuilder sb3 = new StringBuilder();
            // sb3.append("INSERT INTO TRANSACTION_TL (BRANCH_CD, CURRENCY_CD, VOUCHER_NO, VOUCHER_YEAR, REFERENCE_NO,");
            // sb3.append("ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, TRAN_CODE,");
            // sb3.append("AMOUNT, ACCOUNT_IDENTITY, TRANSACTION_DATE, TRANSACTION_TIME, STATUS) ");
            // sb3.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            // try (PreparedStatement preparedStatement = connection.prepareStatement(sb3.toString())) {
            //     preparedStatement.setString(1, accountNo.getBranchCode());
            //     preparedStatement.setBigDecimal(2, currencyCode);
            //     preparedStatement.setString(3, voucherNo);
            //     preparedStatement.setString(4, voucherYear);
            //     preparedStatement.setString(5, newReferenceNo);
            //     preparedStatement.setString(6, accountNo.getAccountType());
            //     preparedStatement.setString(7, accountNo.getCustomerNo());
            //     preparedStatement.setString(8, accountNo.getRunNo());
            //     preparedStatement.setString(9, accountNo.getCheckDigit());
            //     preparedStatement.setString(10, "80630");
            //     preparedStatement.setBigDecimal(11, totalAmount);
            //     preparedStatement.setString(12, "C");
            //     preparedStatement.setString(13, localDateString);
            //     preparedStatement.setString(14, localTimeString);
            //     preparedStatement.setString(15, "A");

            //     int affectedRows = preparedStatement.executeUpdate();
            //     logger.info("Affected No of rows is: ");
            //     logger.info(affectedRows);
            // } catch (SQLException ex) {
            //     DatabaseErrorHandler.log((SQLException) ex);
            //     throw ex;
            // }

            // newReferenceNo = incrementReferenceNo(newReferenceNo);
            // var instructionNo = generateInstructionNo();
            // StringBuilder sb4 = new StringBuilder();
            // sb4.append("INSERT INTO TRANSACTION_TL (BRANCH_CD, VOUCHER_NO, VOUCHER_YEAR, REFERENCE_NO, ");
            // sb4.append("TRAN_CODE, CURRENCY_CD, OR_BRANCH_CD, MAIN_HD, SUB_HD_1, SUB_HD_2, ");
            // sb4.append("GL_RUN_NO, GL_CHECK_DIGIT, AMOUNT, ACCOUNT_IDENTITY, IDENTITY, INSTRUCTION_NO, INSTRUCTION_DATE , TRANSACTION_DATE, TRANSACTION_TIME, STATUS)");
            // sb4.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            // try (PreparedStatement preparedStatement = connection.prepareStatement(sb4.toString())) {

            //     preparedStatement.setString(1, "1001");
            //     preparedStatement.setString(2, voucherNo);
            //     preparedStatement.setString(3, voucherYear);
            //     preparedStatement.setString(4, newReferenceNo);
            //     preparedStatement.setString(5, "30630");
            //     preparedStatement.setBigDecimal(6, currencyCode);
            //     preparedStatement.setBigDecimal(7, new BigDecimal("0786"));
            //     preparedStatement.setBigDecimal(8, new BigDecimal("230"));
            //     preparedStatement.setBigDecimal(9, new BigDecimal("01"));
            //     preparedStatement.setBigDecimal(10, new BigDecimal("01"));
            //     preparedStatement.setBigDecimal(11, new BigDecimal("0586"));
            //     preparedStatement.setBigDecimal(12, new BigDecimal("02"));
            //     preparedStatement.setBigDecimal(13, totalAmount.negate());
            //     preparedStatement.setString(14, "G");
            //     preparedStatement.setString(15, "M");
            //     preparedStatement.setBigDecimal(16, instructionNo);
            //     preparedStatement.setString(17, localDateString);
            //     preparedStatement.setString(18, localDateString);
            //     preparedStatement.setString(19, localTimeString);
            //     preparedStatement.setString(20, "A");

            //     int affectedRows = preparedStatement.executeUpdate();
            //     logger.info("Affected No of rows is: " + affectedRows);
            // } catch (SQLException ex) {
            //     DatabaseErrorHandler.log((SQLException) ex);
            //     throw ex;
            // }

            String updateSql = "UPDATE RTGS_TL SET STATUS=?, CANCELLED_BY=?, CANCELLED_DATE=?, CANCELLED_TIME=?, CANCELLATION_REASON=? WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?";

            int rowsUpdated = 0;
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) 
            {
                preparedStatement.setString(1, "C");
                preparedStatement.setString(2, loginId);
                preparedStatement.setString(3, localDateString);
                preparedStatement.setString(4, localTimeString);
                preparedStatement.setString(5, cancellationReason);
                preparedStatement.setString(6, voucherNo);
                preparedStatement.setString(7, voucherYear);

                rowsUpdated = preparedStatement.executeUpdate();

            } 
            catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            return rowsUpdated > 0;
        }
        catch (Exception ex) {
            logger.info("Generic exception occurred", ex);
            throw ex;
        }

    }

    @Transactional(rollbackOn = Exception.class)
    public OutwardRTGSReponseDto authorizeOutwardRtgs(String voucherNo, String voucherYear, String loginId) throws Exception {
        int rowsUpdated = 0;
        var localDateString = String.valueOf(LocalDate.now());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String localTimeString = LocalTime.now().format(timeFormatter);

        try (Connection connection = datasource.getConnection()) 
        {
            AccountNumberDto accountNo = new AccountNumberDto();
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal currencyCode = BigDecimal.ZERO;

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT BRANCH_CD, CURRENCY_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, ");
            sb.append("TOTAL_AMOUNT FROM RTGS_TL WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ? AND STATUS = ?");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
                preparedStatement.setString(1, voucherNo);
                preparedStatement.setString(2, voucherYear);
                preparedStatement.setString(3, "U");

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    accountNo.setBranchCode(resultSet.getString("BRANCH_CD"));
                    accountNo.setAccountType(resultSet.getString("ACCOUNT_TYPE_CD"));
                    accountNo.setCustomerNo(resultSet.getString("CUSTOMER_NO"));
                    accountNo.setRunNo(resultSet.getString("RUN_NO"));
                    accountNo.setCheckDigit(resultSet.getString("CHECK_DIGIT"));
                    totalAmount = resultSet.getBigDecimal("TOTAL_AMOUNT");
                    currencyCode = resultSet.getBigDecimal("CURRENCY_CD");
                }
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            StringBuilder sb2 = new StringBuilder();
            sb2.append("UPDATE ACCOUNT_TL SET AVAILABLE_BAL = AVAILABLE_BAL - ? ");
            sb2.append("WHERE BRANCH_CD = ? AND ACCOUNT_TYPE_CD = ? ");
            sb2.append("AND CUSTOMER_NO = ? AND RUN_NO = ? AND CHECK_DIGIT = ?");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb2.toString())) {
                preparedStatement.setBigDecimal(1, totalAmount);
                preparedStatement.setString(2, accountNo.getBranchCode());
                preparedStatement.setString(3, accountNo.getAccountType());
                preparedStatement.setString(4, accountNo.getCustomerNo());
                preparedStatement.setString(5, accountNo.getRunNo());
                preparedStatement.setString(6, accountNo.getCheckDigit());

                int affectedRows = preparedStatement.executeUpdate();
                logger.info("Affected No of rows is: " + affectedRows);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            StringBuilder sb3 = new StringBuilder();
            sb3.append("UPDATE GL_ACCOUNT_TL SET AVAILABLE_BAL = AVAILABLE_BAL + ? ");
            sb3.append("WHERE IDENTITY = ? AND BRANCH_CD = ? ");
            sb3.append("AND MAIN_HD = ? AND SUB_HD_1 = ? AND SUB_HD_2 = ?");
            sb3.append("AND GL_RUN_NO = ? AND GL_CHECK_DIGIT = ? ");

            try (PreparedStatement preparedStatement = connection.prepareStatement(sb3.toString())) {
                preparedStatement.setBigDecimal(1, totalAmount);
                preparedStatement.setString(2, "M");
                preparedStatement.setString(3, "786");
                preparedStatement.setString(4, "230");
                preparedStatement.setString(5, "01");
                preparedStatement.setString(6, "01");
                preparedStatement.setString(7, "0586");
                preparedStatement.setString(8, "02");

                int affectedRows = preparedStatement.executeUpdate();
                logger.info("Affected No of rows is: " + affectedRows);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            String sqlQuery = "UPDATE RTGS_TL SET AUTH_DATE = ?, AUTH_TIME = ?, AUTH_BY = ?, STATUS = ? WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, localDateString);
                preparedStatement.setString(2, localTimeString);
                preparedStatement.setString(3, loginId);
                preparedStatement.setString(4, "A");
                preparedStatement.setString(5, voucherNo);
                preparedStatement.setString(6, voucherYear);

                rowsUpdated = preparedStatement.executeUpdate();

                logger.info("Affected No of rows is: " + rowsUpdated);
            } 
            catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }


            String sqlQuery2 = "UPDATE TRANSACTION_TL SET STATUS = ?  WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery2)) {
                preparedStatement.setString(1, "A");
                preparedStatement.setString(2, voucherNo);
                preparedStatement.setString(3, voucherYear);

                rowsUpdated = preparedStatement.executeUpdate();

                logger.info("Affected No of rows is: " + rowsUpdated);
            } 
            catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            StringBuilder sb4 = new StringBuilder();
            sb4.append("SELECT Voucher_No || '/' || Voucher_year AS VOUCHER_NO, REFERENCE_NO, ");
            sb4.append("CASE WHEN AMOUNT < 0 THEN VARCHAR_FORMAT(ABS(AMOUNT), '999,999,999,999,999,999.00') ELSE '0.00' END AS DEBIT, ");
            sb4.append("CASE WHEN AMOUNT > 0 THEN VARCHAR_FORMAT(AMOUNT, '999,999,999,999,999,999.00') ELSE '0.00' END AS CREDIT, ");

            sb4.append("CASE WHEN T.ACCOUNT_IDENTITY = 'C' THEN LPAD(AC.branch_cd, 4, '0') || '-' || LPAD(AC.account_type_cd, 4, '0') || '-' || ");
            sb4.append("LPAD(AC.customer_no, 6, '0') || '-' || LPAD(AC.run_no, 2, '0') || '-' || AC.check_digit ");
            sb4.append("ELSE GL.IDENTITY || '-' || LPAD(GL.MAIN_HD, 3, '0') || '-' || LPAD(GL.SUB_HD_1, 2, '0') || '-' || ");
            sb4.append("LPAD(GL.SUB_HD_2, 2, '0') || '-' || LPAD(GL.GL_RUN_NO, 4, '0') || '-' || GL.GL_CHECK_DIGIT END AS ACCOUNT_NO,");


            sb4.append("CASE WHEN T.ACCOUNT_IDENTITY = 'C' THEN AC.account_title ELSE GL.account_title END AS ACCOUNT_TITLE, ");
            sb4.append("CT.ALPHA_CD  AS CURRENCY_CODE FROM TRANSACTION_TL T LEFT JOIN CURRENCY_TL CT ON T.CURRENCY_CD  = CT.CURRENCY_CD ");


            sb4.append("LEFT JOIN ACCOUNT_TL AC ON T.branch_cd = AC.branch_cd AND T.account_type_cd = AC.account_type_cd ");
            sb4.append("AND T.customer_no = AC.customer_no  AND T.run_no = AC.run_no AND T.check_digit = AC.check_digit ");
            sb4.append("AND T.ACCOUNT_IDENTITY = 'C'");

            sb4.append("LEFT JOIN GL_ACCOUNT_TL GL ON T.MAIN_HD = GL.MAIN_HD AND T.SUB_HD_1 = GL.SUB_HD_1 AND T.SUB_HD_2 = GL.SUB_HD_2 ");
            sb4.append("AND T.GL_RUN_NO = GL.GL_RUN_NO AND T.GL_CHECK_DIGIT = GL.GL_CHECK_DIGIT AND T.IDENTITY = GL.IDENTITY AND T.OR_BRANCH_CD = GL.BRANCH_CD ");
            sb4.append("AND T.ACCOUNT_IDENTITY = 'G'");


            sb4.append("WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ?");

            OutwardRTGSReponseDto result = new OutwardRTGSReponseDto();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sb4.toString())) {

                List<VoucherLaxDto> resultList = new ArrayList<>();

                preparedStatement.setString(1, voucherNo);
                preparedStatement.setString(2, voucherYear);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    resultList.add(new VoucherLaxDto(
                            resultSet.getString("VOUCHER_NO"),
                            resultSet.getString("REFERENCE_NO"),
                            resultSet.getString("CREDIT"),
                            resultSet.getString("DEBIT"),
                            resultSet.getString("ACCOUNT_NO"),
                            resultSet.getString("ACCOUNT_TITLE"),
                            resultSet.getString("CURRENCY_CODE")));
                }
                result.setVoucherLaxList(resultList);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            String sqlQuery3 = "SELECT ADVISE_NO FROM RTGS_BENEFICIARY_TL WHERE VOUCHER_NO = ? AND VOUCHER_YEAR=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery3)) {
                preparedStatement.setString(1, voucherNo);
                preparedStatement.setString(2, voucherYear);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<String> resultList = new ArrayList<>();

                while (resultSet.next()) {
                    resultList.add(resultSet.getString("ADVISE_NO"));
                }

                result.setAdviceNoList(resultList);
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }

            String sqlQuery4 ="SELECT TRAN_NO FROM TRANSACTION_TL WHERE VOUCHER_NO=? AND VOUCHER_YEAR=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery4)) {
                preparedStatement.setString(1, voucherNo);
                preparedStatement.setString(2, voucherYear);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<String> resultList = new ArrayList<>();

                while (resultSet.next()) {
                    resultList.add(resultSet.getString("TRAN_NO"));
                }

                result.setTransactionNoList(resultList);
                return result;
            } catch (SQLException ex) {
                DatabaseErrorHandler.log((SQLException) ex);
                throw ex;
            }
        }
        catch (Exception ex) {
            logger.info("Generic exception occurred", ex);
            throw ex;
        }
    }


    public boolean isPostingUser(String voucherNo, String voucherYear, String loginId) throws Exception {
        String sqlQuery = "SELECT * FROM RTGS_TL WHERE VOUCHER_NO = ? AND VOUCHER_YEAR = ? AND POSTED_BY = ? ";

        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery.toString())) {
            preparedStatement.setString(1, voucherNo);
            preparedStatement.setString(2, voucherYear);
            preparedStatement.setString(3, loginId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return true;
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
        return false;
    }

    public String getChequeStatus(AccountNumberDto accountDetails, String chequeNo) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CASE WHEN last_digit = 1 THEN STATUS_0 WHEN last_digit = 2 THEN STATUS_1 ");
        sb.append("WHEN last_digit = 3 THEN STATUS_2 WHEN last_digit = 4 THEN STATUS_3 ");
        sb.append("WHEN last_digit = 5 THEN STATUS_4 WHEN last_digit = 6 THEN STATUS_5 ");
        sb.append("WHEN last_digit = 7 THEN STATUS_6 WHEN last_digit = 8 THEN STATUS_7 ");
        sb.append("WHEN last_digit = 9 THEN STATUS_8 WHEN last_digit = 0 THEN STATUS_9 ");
        sb.append("END AS CHEQUE_STATUS FROM (SELECT *, CASE WHEN RIGHT(CAST(? AS VARCHAR), 1) = '0' THEN 0 ");
        sb.append("ELSE CAST(RIGHT(CAST(? AS VARCHAR), 1) AS INT) END AS last_digit FROM CHEQUEBOOK_TL ");
        sb.append("WHERE ? BETWEEN START_NO AND (START_NO + 9) AND BRANCH_CD = ? AND ACCOUNT_TYPE_CD = ? ");
        sb.append("AND CUSTOMER_NO = ? AND RUN_NO = ? AND CHECK_DIGIT = ? )");

        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setString(1, chequeNo);
            preparedStatement.setString(2, chequeNo);
            preparedStatement.setString(3, chequeNo);
            preparedStatement.setString(4, accountDetails.getBranchCode());
            preparedStatement.setString(5, accountDetails.getAccountType());
            preparedStatement.setString(6, accountDetails.getCustomerNo());
            preparedStatement.setString(7, accountDetails.getRunNo());
            preparedStatement.setString(8, accountDetails.getCheckDigit());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getString("CHEQUE_STATUS");
            }
        } catch (Exception ex) {
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
            logger.error("Unexpected error occurred while retrieving beneficiary account with IBAN", ex);
        }
        return null;
    }

    private BigDecimal generateAdviseNo() {
        BigDecimal previousAdvice = BigDecimal.ZERO;
        String sqlQuery = "SELECT MAX(ADVISE_NO) AS ADVISE_NO FROM RTGS_BENEFICIARY_TL";
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            if (resultSet.next()) {
                previousAdvice = resultSet.getBigDecimal("ADVISE_NO");
            }

            if (previousAdvice == null) {
                previousAdvice = new BigDecimal(7070500);
            }

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
            return null;
        }
        return previousAdvice.add(BigDecimal.ONE);
    }

    private String generateRefNo() {
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

    private BigDecimal generateVoucherNo() {
        BigDecimal previousVoucher = BigDecimal.ZERO;
        String sqlQuery = "SELECT VOUCHER_NO FROM TRANSACTION_TL ORDER BY VOUCHER_NO DESC, TRANSACTION_TIME DESC, TRANSACTION_DATE DESC FETCH FIRST 1 ROW ONLY";
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            if (resultSet.next()) {
                previousVoucher = resultSet.getBigDecimal("VOUCHER_NO");
            } else {
                previousVoucher = new BigDecimal(386100);
            }

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
            return null;
        }
        return previousVoucher.add(BigDecimal.ONE);
    }

    private BigDecimal generateInstructionNo() {
        BigDecimal previousVoucher = BigDecimal.ZERO;
        String sqlQuery = "SELECT MAX(INSTRUCTION_NO) AS INSTRUCTION_NO FROM TRANSACTION_TL";
        try (Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            if (resultSet.next()) 
                previousVoucher = resultSet.getBigDecimal("INSTRUCTION_NO");
            
            
            if(previousVoucher == null || previousVoucher == BigDecimal.ZERO)
                previousVoucher = new BigDecimal(6492515);

        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
            return null;
        }
        return previousVoucher.add(BigDecimal.ONE);
    }

    

    private String incrementReferenceNo(String referenceNo) {
        if (referenceNo == null || referenceNo.isEmpty()) {
            return "A1073";
        }

        try {
            String numericPartStr = referenceNo.substring(1);
            int numericPart = Integer.parseInt(numericPartStr);
            numericPart++;
            return "A" + String.format("%04d", numericPart);

        } catch (NumberFormatException e) {
            logger.error("Error incrementing reference number: Invalid format: " + referenceNo.toString());
            return "A1073";
        }
    }
}
