package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import database.Datasources;
import dto.AccountDetailDto;
import dto.AccountNumberDto;
import dto.ListDto;
import dto.UserDto;
import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

@ApplicationScoped
public class MainService {

    @Inject
    Datasources datasource;

    @Inject
    Logger logger;

    public UserDto getUser(String loginId) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM USER_TL WHERE login_id = ?")) {

            preparedStatement.setString(1, loginId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserDto(
                            resultSet.getString("login_id"),
                            resultSet.getInt("role_id"),
                            resultSet.getString("branch_cd"),
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
                ResultSet resultSet = statement.executeQuery(
                        "SELECT sbp_transaction_type_cd as value, sbp_transaction_type || ' - ' || sbp_transaction_type_cd as label FROM SBP_TRANSACTION_TYPE_TL")) {

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
                        .prepareStatement("SELECT LPAD(branch_cd, 4, '0') || '-' || LPAD(account_type_cd, 4, '0') || '-' || LPAD(customer_no, 6, '0') || '-' || LPAD(run_no, 4, '0') || '-' || check_digit AS account_number FROM account_tl where customer_no = ?");

                        // .prepareStatement("SELECT " +
                        //         "CONCAT(FORMAT(branch_cd, '0000'), '-', FORMAT(account_type_cd, '0000'), '-', FORMAT(customer_no, '000000'), '-', FORMAT(run_no, '0000'), '-', check_digit) "
                        //         +
                        //         "AS account_number FROM account_tl where customer_no = ?;")

        ) {
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

    // public RtgsTDto createRtgsTransaction(RtgsTDto rtgsDto) {
    // String insertSql = "INSERT INTO RTGS_TL (" +
    // "BRANCH_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, " +
    // "VOUCHER_NO, VOUCHER_YEAR, SBP_TRANSACTION_TYPE_CD, CURRENCY_CD, " +
    // "TRANSFER_TYPE, PAYMENT_MODE, TOTAL_AMOUNT, POSTED_DATE, " +
    // "POSTED_TIME, POSTED_BY, AUTH_DATE, AUTH_TIME, AUTH_BY, " +
    // "PURPOSE_OF_PAYMENT_CD, REMARKS, STATUS" +
    // ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // try (Connection connection = dataSource.getConnection();
    // PreparedStatement preparedStatement = connection.prepareStatement(insertSql))
    // {

    // preparedStatement.setBigDecimal(1, rtgsDto.getBranchCd());
    // preparedStatement.setInt(2, rtgsDto.getAccountTypeCd());
    // preparedStatement.setBigDecimal(3, rtgsDto.getCustomerNo());
    // preparedStatement.setBigDecimal(4, rtgsDto.getRunNo());
    // preparedStatement.setBigDecimal(5, rtgsDto.getCheckDigit());
    // preparedStatement.setBigDecimal(6, rtgsDto.getVoucherNo());
    // if (rtgsDto.getVoucherYear() != null) {
    // preparedStatement.setBigDecimal(7, rtgsDto.getVoucherYear());
    // } else {
    // preparedStatement.setNull(7, java.sql.Types.DECIMAL);
    // }
    // preparedStatement.setBigDecimal(8, rtgsDto.getSbpTransactionTypeCd());
    // preparedStatement.setBigDecimal(9, rtgsDto.getCurrencyCd());
    // preparedStatement.setString(10, rtgsDto.getTransferType());
    // preparedStatement.setString(11, rtgsDto.getPaymentMode());
    // if (rtgsDto.getTotalAmount() != null) {
    // preparedStatement.setBigDecimal(12, rtgsDto.getTotalAmount());
    // } else {
    // preparedStatement.setNull(12, java.sql.Types.DECIMAL);
    // }
    // if (rtgsDto.getPostedDate() != null) {
    // preparedStatement.setDate(13, Date.valueOf(rtgsDto.getPostedDate()));
    // } else {
    // preparedStatement.setNull(13, java.sql.Types.DATE);
    // }
    // if (rtgsDto.getPostedTime() != null) {
    // preparedStatement.setTime(14, Time.valueOf(rtgsDto.getPostedTime()));
    // } else {
    // preparedStatement.setNull(14, java.sql.Types.TIME);
    // }
    // preparedStatement.setString(15, rtgsDto.getPostedBy());
    // if (rtgsDto.getAuthDate() != null) {
    // preparedStatement.setDate(16, Date.valueOf(rtgsDto.getAuthDate()));
    // } else {
    // preparedStatement.setNull(16, java.sql.Types.DATE);
    // }
    // if (rtgsDto.getAuthTime() != null) {
    // preparedStatement.setTime(17, Time.valueOf(rtgsDto.getAuthTime()));
    // } else {
    // preparedStatement.setNull(17, java.sql.Types.TIME);
    // }
    // preparedStatement.setString(18, rtgsDto.getAuthBy());
    // preparedStatement.setInt(19, rtgsDto.getPurposeOfPaymentCd());
    // preparedStatement.setString(20, rtgsDto.getRemarks());
    // preparedStatement.setString(21, rtgsDto.getStatus());

    // int affectedRows = preparedStatement.executeUpdate();
    // if (affectedRows > 0) {
    // return rtgsDto;
    // }
    // } catch (SQLException ex) {
    // logger.error("Unexpected error occurred while creating RTGS transaction",
    // ex);
    // }
    // return null;
    // }
}

// public boolean submitForm(FormSubmissionDto dto) {
// String formInsert = INSERT INTO RTGS_TL (branch_cd, currency_cd,
// sbp_transaction_type_cd, customer_no, account_type_cd, run_no, check_digit,
// available_balance,
// payment_mode, transfer_amount, beneficiary_account_name,
// beneficiary_bank_name, beneficiary_branch_code, beneficiary_iban,
// purpose_of_payment, remarks
// ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);

// // Assuming you have a `dataSource` or `EntityManager` injected
// var conn = dataSource.getConnection();
// var ps = conn.prepareStatement(lodgementInsert);

// ps.setString(1, dto.branch);
// ps.setString(2, dto.currency);
// ps.setString(3, dto.sbpTranType);
// ps.setString(4, dto.transferType);
// ps.setString(5, dto.customerNo);
// ps.setString(6, dto.accountNo);
// ps.setString(7, dto.acType);
// ps.setString(8, dto.iban);
// ps.setString(9, dto.accountName);
// ps.setBigDecimal(10, dto.availableBalance);
// ps.setString(11, dto.paymentMode);
// ps.setBigDecimal(12, dto.transferAmount);
// ps.setString(13, dto.beneficiaryAccountName);
// ps.setString(14, dto.beneficiaryBankName);
// ps.setString(15, dto.beneficiaryBranchCode);
// ps.setString(16, dto.beneficiaryIBAN);
// ps.setString(17, dto.purposeOfPayment);
// ps.setString(18, dto.remarks);

// int inserted = ps.executeUpdate();
// ps.close();
// conn.close();

// return inserted > 0;
// }
