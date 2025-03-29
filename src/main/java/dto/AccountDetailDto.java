package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AccountDetailDto {
    private String accounType;
    private String iban;
    private String accountName;
    private String availableBalance;
}
