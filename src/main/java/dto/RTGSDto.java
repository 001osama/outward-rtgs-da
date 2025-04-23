package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RTGSDto {
    String currency;
    String amount;
    String postedBy;
    String postedDate;
    String voucherNo;
    String expCode;
}
