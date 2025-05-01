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
    String accountNo;
    String branchCode;
    String currencyCode;
    String customerNo;
    String paymentMode;
    String purposeOfPaymentCode;
    String remarks;
    String sbpTranTypeCode;
    String senderToReceiverInformation;
    String chequeNo;
    String chequeDate;
    String transferTypeCode;
    String totalAmount;
    String postedBy;
    List<BeneficiaryTransactionLaxDto> transactionLaxList;
}
