package dto;
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

public class BeneficiaryTransactionLaxDto {
    String transactionAmount; 
    String beneficiaryBankName; 
    String beneficiaryBankCode; 
    String beneficiaryAccountName; 
    String beneficiaryIbanOrAccountNo; 
    String beneficiaryAddress; 
    String beneficiaryBranchCode;
    public boolean isBeneficiaryTitleFetch;
}
