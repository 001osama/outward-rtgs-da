package model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Account {

    private int branchCode;
    private int acctypeCode;
    private int customerNo;
    private int runNo;
    private int checkDigit;
    private String accTitle;
    private int availableBalance;
    private String status;
    
}
