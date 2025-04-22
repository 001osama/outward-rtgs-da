package dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OutwardRTGSReponseDto {
    private List<VoucherLaxDto> voucherLaxList;
    private List<String> adviceNoList;
}


