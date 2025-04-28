package dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OutwardRTGSReponseDto {
    List<VoucherLaxDto> voucherLaxList;
    List<String> adviceNoList;
}


