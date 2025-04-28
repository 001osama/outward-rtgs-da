package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountNumberDto {
    String branchCode;
    String accountType;
    String customerNo;
    String runNo;
    String checkDigit;
}
