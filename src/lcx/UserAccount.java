/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;
import java.math.BigDecimal;
/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class UserAccount
    {
    private String userPassword;
    private String userNumber;
    private BigDecimal userLatinum;
    private String userName;
    
    public UserAccount()
        {
        
        }
   
    public UserAccount(String inUserNumber, String inPassword)
        {
        userNumber = inUserNumber;
        userPassword = inPassword;
        }
        
    public void setUserName(String inName)
        {
        userName = inName;
        }
    
    public void setUserNumber(String inNumber)
        {
        userNumber = inNumber;
        }
    
    public void setPassword(String inPass)
        {
        userPassword = inPass;
        }
    
    public String getUserName()
        {
        return userName;
        }
    
    public String getUserPassword()
        {
        return userPassword;
        }
    
    public String getUserNumber()
        {
        return userNumber;
        }
    
    public BigDecimal getLatinum()
        {
        return userLatinum;
        }
    
    public double getApproxLatinum()
        {
        return getLatinum().doubleValue();
        }
    }
