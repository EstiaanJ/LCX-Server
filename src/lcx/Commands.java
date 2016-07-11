/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public enum Commands
    {
    PREPARE_FOR_VERSION("VERSION"), //Find a better enum for this...
    NEW_SESSION_REQUEST("New Session"),
    NEW_SESSION_ACKNOWLEDGE("New Session Granted"),

    NEW_USID_REQUEST("New USID"),

    UPDATE_REQUEST("Update"),

    NEW_USER_REQUEST("New User"),
    NEW_USER_ACKNOWLEDGE("New User Ready"),

    RECEIPT_ACCOUNT_NUMBER("Account Number Recieved"),
    RECEIPT_ACCOUNT_NAME("Name Recieved"),
    RECEIPT_ACCOUNT_PASSWORD("Password Recieved"),

    NEW_ACCOUNT_NUMBER_REQUEST("New Account Number"),

    NEW_TRANSFER_REQUEST("Transfer"),
    NEW_TRANSFER_AWAITING_RECEIPIENT("Ready for transfer to"),
    NEW_TRANSFER_AWAITING_AMOUNT("Ready for amount"),
    RECEIPT_TRANSFER_COMPLETE("Done with transfer"),

    CONNECTION_CLOSE_REQUEST("Close"),
    CONNECTION_CLOSE_ACKNOWLEDGE("Closing"),

    LOGIN_REQUEST("Login Request"),
    LOGIN_AWAITING_ACCOUNT_NUMBER("Login Ready"),
    LOGIN_AWAITING_PASSWORD("Ready for password"),
    LOGIN_PREPARE_FOR_DETAILS("Login Succesful"),
    LOGIN_FAIL_RECEIPT("Login Unsuccesful"),
    LOGIN_COMPLETE_RECEIPT("Login Done"),

    ERROR_GENERIC("SERVER ERROR");
    
    private String msg;
    
    private Commands(String msg) {
            this.msg = msg;
        }
    
    public static Commands fromString(String text) {
            if (text != null) {
                for (Commands b : Commands.values()) {
                    if (text.equals(b.msg())) {
                        return b;
                    }
                }
            }
            return null;
        }

        public String msg() {
            return msg;
        }
        
    public boolean equals(String inString)
        {
        boolean isEqual = false;
        if(inString.equals(msg))
            {
            isEqual = true;
            }
        return isEqual;
        }
    }
