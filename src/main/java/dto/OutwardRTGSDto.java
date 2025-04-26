package dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OutwardRTGSDto {
    private String accountNo;
    private String branchCode;
    private String currencyCode;
    private String customerNo;
    private String paymentMode;
    private String purposeOfPaymentCode;
    private String remarks;
    private String sbpTranTypeCode;
    private String senderToReceiverInformation;
    private String chequeNo;
    private String chequeDate;
    private String transferTypeCode;
    private String totalAmount;
    private List<BeneficiaryTransactionLaxDto> transactionLaxList;
}
