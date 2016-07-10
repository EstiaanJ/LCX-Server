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
    PREPARE_FOR_VERSION("VERSION"); //Find a better enum for this...
    
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
