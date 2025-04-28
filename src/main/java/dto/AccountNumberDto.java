package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AccountNumberDto {
    String branchCode;
    String accountType;
    String customerNo;
    String runNo;
    String checkDigit;
}
