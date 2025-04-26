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
    private String loginId;
    private String accountNo;
    private String branchCode;
    private String currencyCode;
    private String customerNo;
    private boolean isCustAcc;
    private String paymentMode;
    private String purposeOfPaymentCode;
    private String remarks;
    private String sbpTranTypeCode;
    private String senderToReceiverInformation;
    private String chequeDate;
    private String chequeNo;
    private String transferTypeCode;
    private String totalAmount;
    private List<BeneficiaryTransactionLaxDto> transactionLaxList;
}
