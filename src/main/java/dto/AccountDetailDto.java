package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AccountDetailDto {
    String accountType;
    String iban;
    String accountName;
    String availableBalance;
}
