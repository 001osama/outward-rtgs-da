package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReportFilterDto {
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
}
