package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RTGSReportDto {
    String adviceNo;
    String branchCode;
    String transactionDate;
    String transferType;
    String accountInfo;
    String beneficiaryAddress;
    public boolean isBeneficiaryTitleFetch;
    String bankName;
    String voucherNo;
    String totalAmount;
    String sbpTransactionType;
    String beneficiaryAccountInfo;
    String status;
}
