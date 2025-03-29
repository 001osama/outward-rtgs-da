package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountNumberDto {
    private String branchCode;
    private String accountType;
    private String customerNo;
    private String runNo;
    private String checkDigit;
}
