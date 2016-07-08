/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class ClientWorker implements Runnable
    {
    private Socket client;
    private UserAccount user;
    private Logger userTransLog;
    private static Logger systemLog;
    private FileHandler fh;
    private String serverUSID = "unset";
    boolean running = true;

    
    public ClientWorker(Socket inClient)
        {
        client = inClient;
        serverUSID = genUSID();
        System.out.println("Client connected: " + client.getLocalSocketAddress() + " USID: " + serverUSID);
        }
  
    private void sendCommand(DataOutputStream out, CommandEnum com) throws IOException 
        {
        out.writeUTF(com.msg());
        }
    
    private String readCommand(DataInputStream in) throws IOException
    {
        return in.readUTF();
    }
    
    public enum CommandEnum {
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

        private CommandEnum(String msg) {
            this.msg = msg;
        }
        
        public static CommandEnum fromString(String text) {
            if (text != null) {
                for (CommandEnum b : CommandEnum.values()) {
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
    }
    
    @Override
    public void run()
        {
        try
            {
            System.out.println("Opening IO");
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            while(running)
                {
                waitForCommand(in,out);
                }
            }
        catch (SocketTimeoutException s)
            {
            System.out.println("Socket timed out!");
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        }
    
    public void waitForCommand(DataInputStream in,DataOutputStream out) throws IOException 
        {
        if(running)
            {
             System.out.println("Waiting for command from client...");
            CommandEnum command = CommandEnum.fromString(readCommand(in));
            switch (command)
                {
            case NEW_SESSION_REQUEST:
                out.writeUTF("New Session Granted");
                System.out.println("Client: New Session");
                break;
            case NEW_USID_REQUEST:
                out.writeUTF(genUSID());
                System.out.println("Client: New USID");
                break;
            case LOGIN_REQUEST:
                loginRequested(in,out);
                break;
            case UPDATE_REQUEST:
                System.out.println("Client: Update");
                out.writeUTF(LCX.databaseIF.readName(user.getUserNumber(),serverUSID));
                System.out.println("Client says " + in.readUTF());
                out.writeUTF(LCX.databaseIF.readLatinumString(user.getUserNumber(),serverUSID));
                System.out.println("Client says " + in.readUTF());
                out.writeUTF(LCX.databaseIF.readTransactionLog(user.getUserNumber(),serverUSID));
                break; 
            case NEW_USER_REQUEST:
                System.out.println("Client: New User");
                out.writeUTF("New User Ready");
                String newUserAccNum = in.readUTF();
                out.writeUTF("Account Number Recieved");
                String newName = in.readUTF();
                out.writeUTF("Name Recieved");
                String newPass = in.readUTF();
                out.writeUTF("Password Recieved");
                if(LCX.databaseIF.createNewAccount(newUserAccNum,newName,newPass))
                    {
                    System.out.println("Created Account Succesfully");
                    }
                else
                    {
                    System.err.println("Failed to create account");
                    }
                break;
            case NEW_ACCOUNT_NUMBER_REQUEST:
                System.out.println("Client: New Account Number");
                out.writeUTF(LCX.databaseIF.newAccountNumber());
                break;
            case NEW_TRANSFER_REQUEST:
                System.out.println("Client: Transfer");
                out.writeUTF("Ready for transfer to");
                String transferTo = in.readUTF();
                out.writeUTF("Ready for amount");
                String transferAmount = in.readUTF();
                userTransLog.log(Level.INFO, "[TRANSFER OUT]: Transfering: {0} Transfering to: {1}", new Object[]{transferAmount, transferTo});
                LCX.databaseIF.transfer(user.getUserNumber(),transferTo,transferAmount);
                userTransLog.log(Level.INFO, "[TRANSFER REPORT]: Transfer Complete. Account now has: {0}", LCX.databaseIF.readLatinumString(user.getUserNumber(),genUSID()));
                out.writeUTF("Done with transfer");
                break;
            case CONNECTION_CLOSE_REQUEST:
                System.out.println("Client: Close");
                out.writeUTF("Closing");
                running = false;
                this.fh.close();
                in.close();
                out.close(); 
                break;
            default:
                System.err.println("[Error]: default case reached in command switch, an illegal command was probably sent by the client.");
                System.out.println("Attempting to send error to client...");
                out.writeUTF("SERVER ERROR");
                if(in.readUTF().equals("Error Report Request"))
                    {
                    sendCommand(out,command);
                    System.out.println("Client says " + in.readUTF());
                    out.writeUTF("[Server Error]: The server reported an illegal command was sent, that command was: " + command);
                    System.out.println("The error was succesfully sent to the client.");
                    }
                else
                    {
                    System.err.println("[Error]: Could not report illegal command to client! There is either a flaw in the client's command recieving structure, or the client is stuck in a loop and cannot read commands.");
                    System.out.println("The server cannot recover this thread from this situation. Closing connection.");
                    client.close();
                    }
                }
            
            }
        else
            {
            System.out.println("This thread is supposed to stop. If this println statment keeps appearing there is an issue");
            }
        //System.out.println("Exited wait for command");
        }
    
    public void loginRequested(DataInputStream in,DataOutputStream out) throws IOException
        {
        System.out.println("Client: Login Request");
        System.out.println("Exiting command structure to recieve login data");
        sendCommand(out,CommandEnum.LOGIN_AWAITING_ACCOUNT_NUMBER);
        String inAccountNum = readCommand(in);
        sendCommand(out,CommandEnum.LOGIN_AWAITING_PASSWORD);
        String inPassword = in.readUTF();
        boolean validLogin = LCX.databaseIF.login(inAccountNum,inPassword,serverUSID);
        if(validLogin)
            {
            out.writeUTF("Login Succesful");
            user = new UserAccount (inAccountNum,inPassword);
            System.out.println("Login was succesful, sending user data to client");
            out.writeUTF(LCX.databaseIF.readName(user.getUserNumber(),serverUSID));
            System.out.println(in.readUTF());
            out.writeUTF(LCX.databaseIF.readLatinumString(user.getUserNumber(),serverUSID));
            System.out.println(in.readUTF());
            out.writeUTF(LCX.databaseIF.readTransactionLog(user.getUserNumber(),serverUSID));
            System.out.println(in.readUTF());
            
            this.userTransLog = Logger.getLogger(user.getUserNumber() + "_log");
            try
                {
                fh = new FileHandler(user.getUserNumber() + "_Transaction_log.txt");
                userTransLog.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
                }
            catch(IOException e)
                {
                e.printStackTrace();  
                }
            
            out.writeUTF("Login Done");
            System.out.println("Returning to command structure");
            }
        else
            {
            out.writeUTF("Login Unsuccesful");
            System.out.println("[Login Error]: Login was unsuccesful");
            System.out.println("Returning to command structure");
            }
        }
    
    public static String genUSID()
        {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
        }
    }
