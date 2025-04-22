package dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class BeneficiaryTransactionLaxDto {
    private String transactionAmount; 
    private String beneficiaryBankName; 
    private String beneficiaryBankCode; 
    private String beneficiaryAccountName; 
    private String beneficiaryIbanOrAccountNo; 
    private String beneficiaryAddress; 
    private String beneficiaryBranchCode;
}
