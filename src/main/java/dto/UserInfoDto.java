package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoDto {
    String loginId;
    String branchCode;
    String role;
    String lastLoggedInDate;
    String currentLoggedInDate;
}
