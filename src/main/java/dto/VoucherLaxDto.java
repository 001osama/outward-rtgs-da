package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class VoucherLaxDto {
    String voucherNo;
    String referenceNo;
    String creditAmount;
    String debitAmount;
    String accountNo;
    String currencyCode;
}
