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
import dto.BeneficiaryAccountDto;
import dto.OutwardRTGSFormDto;
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
                ResultSet resultSet = statement.executeQuery("SELECT sbp_transaction_type_cd as value, sbp_transaction_type || ' - ' || sbp_transaction_type_cd as label FROM SBP_TRANSACTION_TYPE_TL")) {

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
    

    public OutwardRTGSFormDto submitRtgsForm(OutwardRTGSFormDto formSubmissionDto) {
        String insertSql = "INSERT INTO RTGS_TL (BRANCH_CD, ACCOUNT_TYPE_CD, CUSTOMER_NO, RUN_NO, CHECK_DIGIT, VOUCHER_NO, VOUCHER_YEAR, SBP_TRANSACTION_TYPE_CD, CURRENCY_CD, TRANSFER_TYPE, PAYMENT_MODE, TOTAL_AMOUNT, POSTED_DATE, POSTED_TIME, POSTED_BY, AUTH_DATE, AUTH_TIME, AUTH_BY, PURPOSE_OF_PAYMENT_CD, REMARKS, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {

            preparedStatement.setInt(1, formSubmissionDto.getBranchCode());
            preparedStatement.setInt(2, formSubmissionDto.getAccountTypeCode());
            preparedStatement.setBigDecimal(3, formSubmissionDto.getCustomerNo());
            preparedStatement.setInt(4, formSubmissionDto.getRunNo());
            preparedStatement.setBigDecimal(5, formSubmissionDto.getCheckDigit());
            preparedStatement.setBigDecimal(6, formSubmissionDto.getVoucherNo());
            if (formSubmissionDto.getVoucherYear() != null) {
                preparedStatement.setBigDecimal(7, formSubmissionDto.getVoucherYear());
            } else {
                preparedStatement.setNull(7, java.sql.Types.DECIMAL);
            }
            preparedStatement.setBigDecimal(8, formSubmissionDto.getSbpTransactionTypeCode());
            preparedStatement.setBigDecimal(9, formSubmissionDto.getCurrencyCode());
            preparedStatement.setString(10, formSubmissionDto.getTransferType());
            preparedStatement.setString(11, formSubmissionDto.getPaymentMode());
            if (formSubmissionDto.getTotalAmount() != null) {
                preparedStatement.setBigDecimal(12, formSubmissionDto.getTotalAmount());
            } else {
                preparedStatement.setNull(12, java.sql.Types.DECIMAL);
            }
            if (formSubmissionDto.getPostedDate() != null) {
                preparedStatement.setDate(13, formSubmissionDto.getPostedDate());
            } else {
                preparedStatement.setNull(13, java.sql.Types.DATE);
            }
            if (formSubmissionDto.getPostedTime() != null) {
                preparedStatement.setTime(14, formSubmissionDto.getPostedTime());
            } else {
                preparedStatement.setNull(14, java.sql.Types.TIME);
            }
            preparedStatement.setString(15, formSubmissionDto.getPostedBy());
            if (formSubmissionDto.getAuthDate() != null) {
                preparedStatement.setDate(16, formSubmissionDto.getAuthDate());
            } else {
                preparedStatement.setNull(16, java.sql.Types.DATE);
            }
            if (formSubmissionDto.getAuthTime() != null) {
                preparedStatement.setTime(17, formSubmissionDto.getAuthTime());
            } else {
                preparedStatement.setNull(17, java.sql.Types.TIME);
            }
            preparedStatement.setString(18, formSubmissionDto.getAuthBy());
            preparedStatement.setInt(19, formSubmissionDto.getPurposeOfPaymentCode());
            preparedStatement.setString(20, formSubmissionDto.getRemarks());
            preparedStatement.setString(21, formSubmissionDto.getStatus());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return formSubmissionDto;
            }
        } catch (SQLException ex) {
            logger.error("Unexpected error occurred while creating RTGS transaction", ex);
        }
        return null;
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


    public BeneficiaryAccountDto getBeneficiaryAccountDetails(String beneficiaryAccOrIban) {
        try (Connection connection = datasource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT beneficiary_branch_cd, account_title FROM BENEFICIARY_ACCOUNT_TL WHERE ? IN (beneficiary_iban, BENEFICIARY_ACCOUNT_NO)")) {
            preparedStatement.setString(1, beneficiaryAccOrIban);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new BeneficiaryAccountDto(
                            resultSet.getInt("beneficiary_branch_cd"),
                            resultSet.getString("account_title"));
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving beneficiary account with IBAN: " + beneficiaryAccOrIban, e);
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
    
}


