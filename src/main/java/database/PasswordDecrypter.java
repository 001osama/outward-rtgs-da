/*! \brief <b>Folder decription</b> \n This folder contains all utilities of dataaccess-ms-validatetransactiondetail.
 */
package database;

import java.util.ArrayList;
import java.util.List;

/**
 * import java.util.ArrayList;
 * import java.util.List;
 */
/**
 * <b> Class : <i>Password Decrypter</i> </b>
 * <p>
 * <b>ServiceName:</b>dataaccess-ms-validatetransactiondetail.
 * <p>
 * <b>CallingServices:</b> CpuCredentialProvider.java 
 * CoventionalCredentialsProvider.java <p>
 * CreditCardCredentialsProvider.java <p>
 * IslamicCredentialsProvider.java <p>
 * IslamicCredentialsProvider.java <p>
 * KarachiMainCredentialsProvider.java <p>
 * PoCredentialsProvider.java <p>
 * <p>
 * @author Rithik Rathi
 * @version 1.1
 * @brief Contains object password decrypter for validateTransaction.
 */

public class PasswordDecrypter {

    private PasswordDecrypter() {
    }

    /**
     * This method is used to decrypt password .
     * 
     * @param userId   username
     * @param password encrypted password
     * @return decrypted password
     */
    public static String getDecryptPsw(String userId, String password) {
        int codeSetIndex;
        // vector for storing the Code Set
        List<String> codeSet = new ArrayList<>(5);
        codeSet.add("0");
        codeSet.add("*");
        codeSet.add("1");
        codeSet.add("N");
        codeSet.add("2");
        codeSet.add("G");
        codeSet.add("3");
        codeSet.add("Z");
        codeSet.add("4");
        codeSet.add("=");
        codeSet.add("5");
        codeSet.add("R");
        codeSet.add("6");
        codeSet.add("A");
        codeSet.add("7");
        codeSet.add("P");
        codeSet.add("8");
        codeSet.add("3");
        codeSet.add("9");
        codeSet.add("?");
        codeSet.add("10");
        codeSet.add("K");
        codeSet.add("11");
        codeSet.add("2");
        codeSet.add("12");
        codeSet.add("Q");
        codeSet.add("13");
        codeSet.add("/");
        codeSet.add("14");
        codeSet.add("V");
        codeSet.add("15");
        codeSet.add("6");
        codeSet.add("16");
        codeSet.add("D");
        codeSet.add("17");
        codeSet.add("F");
        codeSet.add("18");
        codeSet.add("O");
        codeSet.add("19");
        codeSet.add("C");
        codeSet.add("20");
        codeSet.add("X");
        codeSet.add("21");
        codeSet.add("9");
        codeSet.add("22");
        codeSet.add("M");
        codeSet.add("23");
        codeSet.add(";");
        codeSet.add("24");
        codeSet.add("U");
        codeSet.add("25");
        codeSet.add("8");
        codeSet.add("26");
        codeSet.add("J");
        codeSet.add("27");
        codeSet.add("E");
        codeSet.add("28");
        codeSet.add("I");
        codeSet.add("29");
        codeSet.add("4");
        codeSet.add("30");
        codeSet.add("T");
        codeSet.add("31");
        codeSet.add(":");
        codeSet.add("32");
        codeSet.add("1");
        codeSet.add("33");
        codeSet.add("Y");
        codeSet.add("34");
        codeSet.add("S");
        codeSet.add("35");
        codeSet.add("W");
        codeSet.add("36");
        codeSet.add("B");
        codeSet.add("37");
        codeSet.add("<");
        codeSet.add("38");
        codeSet.add("5");
        codeSet.add("39");
        codeSet.add("0");
        codeSet.add("40");
        codeSet.add(">");
        codeSet.add("41");
        codeSet.add("H");
        codeSet.add("42");
        codeSet.add("L");
        codeSet.add("43");
        codeSet.add("7");

        // vector for storing the Key Code of user id
        List<String> keyCode = new ArrayList<>(5);
        int passwordLength = password.length();
        String userChar;
        char passwordChar;
        int ord;
        int tempCode;
        int mostPrev = 0;
        int tempAftAdd;
        StringBuilder decryptPsw = new StringBuilder();
        String lclChar = null;

        //////////////// Calculating value for First a pair of digits of the Key
        // constant char ';'
        keyCode.add("23");
        // user char at 6th position
        userChar = userId.substring(5, 6).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(23, ord, keyCode, 0);

        //////////////// Calculating value for Second pair of digits of the Key
        // constant char '>'
        keyCode.add("40");
        // user char at 2nd position
        userChar = userId.substring(1, 2).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(40, ord, keyCode, 1);

        //////////////// Calculating value for Third pair of digits of the Key
        // constant char 'F'
        keyCode.add("17");
        // user char at 3rd position
        userChar = userId.substring(2, 3).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(17, ord, keyCode, 2);

        //////////////// Calculating value for Fourth pair of digits of the Key
        // constant char '@'
        keyCode.add(String.valueOf(ord));
        // user char at 1st position
        userChar = userId.substring(0, 1).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode((Integer.parseInt(keyCode.get(3))), ord, keyCode, 3);

        //////////////// Calculating value for Fifth pair of digits of the Key
        // constant char 'R'
        keyCode.add("5");
        // user char at 1st position
        userChar = userId.substring(0, 1).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(5, ord, keyCode, 4);

        //////////////// Calculating value for Sixth pair of digits of the Key
        // constant char '6'
        keyCode.add("15");
        // user char at 3rd position
        userChar = userId.substring(2, 3).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(Integer.parseInt(keyCode.get(0)), ord, keyCode, 0);

        //////////////// Calculating value for Seventh pair of digits of the Key
        // constant char '?'
        keyCode.add("09");
        // user char at 6th position
        userChar = userId.substring(5, 6).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(Integer.parseInt(keyCode.get(0)), ord, keyCode, 0);

        //////////////// Calculating value for Eighth pair of digits of the Key
        // constant char 'U'
        keyCode.add("24");
        // user char at 4th position
        userChar = userId.substring(3, 4).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(Integer.parseInt(keyCode.get(0)), ord, keyCode, 0);

        //////////////// Calculating value for Ninth pair of digits of the Key
        // constant char 'M'
        keyCode.add("22");
        // user char at 5th position
        userChar = userId.substring(4, 5).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(22, ord, keyCode, 8);

        //////////////// Calculating value for Tenth pair of digits of the Key
        // constant char 'X'
        keyCode.add("20");
        // user char at 2nd position
        userChar = userId.substring(1, 2).toUpperCase();
        // finding the ordinal value for it
        ord = getOrd(codeSet, userChar);
        // adding constant char ordinal value to that of user char. and setting the
        // added value in the key code
        setKeyCode(20, ord, keyCode, 9);

        /* Decrypting the Password **/
        for (int i = 0; i < passwordLength; i++) {
            // getting password char
            passwordChar = password.charAt(i);
            // finding the ordinal value for it
            ord = getOrd(codeSet, String.valueOf(passwordChar));

            if (ord < 43)
                tempCode = ord + 43;
            else
                tempCode = ord;

            tempAftAdd = tempCode;
            // Subtracting the key code value at the respective position from the temp code
            tempCode = tempCode - Integer.parseInt(keyCode.get(i));

            // if temp code is less than zero
            if (tempCode < 0) {
                tempCode = tempCode + Integer.parseInt(keyCode.get(i)) + 43;
                tempAftAdd = tempCode;
                // Subtracting the key code value at the respective position from the temp code
                tempCode = tempCode - Integer.parseInt(keyCode.get(i));
            }

            if (mostPrev == tempCode) {
                return decryptPsw.toString();
            }
            // if temp code is greater than 43
            if (tempCode > 43) {
                tempCode = tempCode - 43;
                tempAftAdd = tempCode;
            }
            // finding the character value for it
            codeSetIndex = -1;
            for (int lcl_k = 0; lcl_k < 44; lcl_k++) {
                if (codeSet.get(codeSetIndex + 1).equals(String.valueOf(tempCode)))
                    lclChar = codeSet.get(codeSetIndex + 2);
                codeSetIndex = codeSetIndex + 2;
            }
            // saving the encrypted character in a variable
            decryptPsw.append(lclChar);
            mostPrev = tempAftAdd;
        }
        return decryptPsw.toString();
    }

    private static void setKeyCode(int x, int ord, List<String> keyCode, int index) {
        String code;
        if ((x + ord) > 99) {
            code = String.valueOf(x + ord).substring(1, 3);
            keyCode.set(index, code);
        } else
            keyCode.set(index, String.valueOf(x + ord));
    }

    private static int getOrd(List<String> codeSet, String userChar) {
        int codeSetIndex = -1;
        int ord = 0;
        for (int i = 0; i < 44; i++) {
            if (codeSet.get(codeSetIndex + 2).equals(userChar))
                ord = Integer.parseInt(codeSet.get(codeSetIndex + 1));
            codeSetIndex = codeSetIndex + 2;
        }
        return ord;
    }
}