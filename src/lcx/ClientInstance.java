/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class ClientInstance
    {
    private final String CIID;
    private final UserAccount currentUser;
    private boolean instanceActive;
    private String clientVersion;
    private String serverVersion;
    
    public ClientInstance(String inVersion)
        {
        serverVersion = inVersion;
        CIID =  genSID();
        currentUser = new UserAccount(); 
        }
    
    public String getSID()
        {
        return CIID;
        }
    
    public UserAccount getUserAccount()
        {
        return currentUser;
        }
    
    public boolean isActive()
        {
        return instanceActive;
        }
    
    public String getServerVersion()
        {
        return serverVersion;
        }
    
    public String getClientVersion()
        {
        return clientVersion;
        }
    
    public void setServerVersion(String inVersion)
        {
        serverVersion = inVersion;
        }
    
    public void setClientVersion(String inVersion)
        {
        clientVersion = inVersion;
        }
    
    public void setActive(boolean inState)
        {
        instanceActive = inState;
        }
    
    private static String genSID()
        {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
        }
    }
