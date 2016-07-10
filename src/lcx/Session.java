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
public class Session
    {
    private final String SID;
    private final UserAccount user;
    private boolean sessionActive;
    
    public Session()
        {
        SID =  genSID();
        user = new UserAccount(); 
        /*
        Using the default constructor here for UserAccount and then setting the fields later is a bit dodgey, 
        but since the user has not logged in when the session is first created I can't see any other way for
        the time being. 
        
        In the future this will be a problem because
            a) The UserAccount field "accountNumber" will be final because it would not make sense for that to change
                especially while a session is active.
            b) The user account needs to be final (as it is now) because it doesn't make sense for the user to change mid session
        
        In essance the thing that idenfitfies the UserAccount that is attached to a particular session needs to be immutable, that being the account number,
        but another user might be allowed to sign in from the same instance of the client program (ie without closing the process), after the first user has loged out.
        This implies that either the UserAccount field of a session should not be final, or that a session is started after login, I vote for the latter.
        */
        }
    
    public String getSID()
        {
        return SID;
        }
    
    public UserAccount getUserAccount()
        {
        return user;
        }
    
    public boolean isActive()
        {
        return sessionActive;
        }
    
    public void setActive(boolean inState)
        {
        sessionActive = inState;
        }
    
    private static String genSID()
        {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
        }
    }
