package dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class OutwardRTGSFormDto {
    String loginId;
    String accountNo;
    String branchCode;
    String currencyCode;
    String customerNo;
    public boolean isCustAcc;
    String paymentMode;
    String purposeOfPaymentCode;
    String remarks;
    String sbpTranTypeCode;
    String senderToReceiverInformation;
    String chequeDate;
    String chequeNo;
    String transferTypeCode;
    String totalAmount;
    List<BeneficiaryTransactionLaxDto> transactionLaxList;
}
