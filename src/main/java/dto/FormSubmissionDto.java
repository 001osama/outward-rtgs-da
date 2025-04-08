package dto;

import java.math.BigDecimal;
import java.util.List;

public class FormSubmissionDto {

    public String branchCode;
    public String currency;
    public String sbpTranType;
    public String transferType;
    public String customerNo;
    public String accountNo;
    public String accType;
    public String iban;
    public String accountName;
    public BigDecimal availableBalance;
    public String paymentMode;
    public BigDecimal transferAmount;
    public String beneficiaryAccountName;
    public String beneficiaryBankName;
    public String beneficiaryBranchCode;
    public String beneficiaryIBAN;
    public String purposeOfPayment;
    public String remarks;
    public List<ChargesDto> charges;

}
