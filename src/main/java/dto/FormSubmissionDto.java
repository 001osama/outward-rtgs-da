package dto;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class FormSubmissionDto {
    private int branchCode;
    private int accountTypeCode;
    private BigDecimal customerNo;
    private int runNo;
    private BigDecimal checkDigit;
    private BigDecimal voucherNo;
    private BigDecimal voucherYear;
    private BigDecimal sbpTransactionTypeCode;
    private BigDecimal currencyCode;
    private String transferType;
    private String paymentMode;
    private BigDecimal totalAmount;
    private Date postedDate;
    private Time postedTime;
    private String postedBy;
    private Date authDate;
    private Time authTime;
    private String authBy;
    private int purposeOfPaymentCode;
    private String remarks;
    private String status;

}
