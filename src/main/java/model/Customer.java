package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Customer {
    private int customerNo;
    private String customerName;
    private String customerAddress;
    private String customerMobile;
}
