package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportFilterDto {
    String loginId;
    String branchCode;
    String rtgsType;
    String transactionType;
    Boolean isToDate;
    Boolean isFromDate;
    String fromDate;
    String toDate;
    String customerNo;
    String accountNo;
    String status;
    String reportFormat;
}
