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

import shared.Commands;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class Server
    {
    private Socket client;
    private UserAccount user;
    private Logger userTransLog;
    private static Logger systemLog;
    private FileHandler fh;
    private String serverUSID = "unset";
    private DataInputStream in;
    private DataOutputStream out;
    private final String firstCommand;
    boolean running = true;
    

    
    public Server(Socket inClient,DataInputStream inDIS,DataOutputStream inDOS,String inFirstCommand)
        {
        firstCommand = inFirstCommand;
        if(firstCommand.equals("New Session"))
            {
            
            }
        else
            {
            System.err.println("First Command was not 'New Session'");
            System.exit(-1); //Need a better way to handle this first command thing, but I don't really see how it won't be the right command, unless it was sent to the original client worker by mistake.
            }
        client = inClient;
        in = inDIS;
        out = inDOS;
        serverUSID = genUSID();
        System.out.println("Client connected: " + client.getLocalSocketAddress() + " USID: " + serverUSID);
        }
  
    private void sendCommand(DataOutputStream out, Commands com) throws IOException 
        {
        out.writeUTF(com.msg());
        }
    
    private String readString(DataInputStream in) throws IOException
    {
        return in.readUTF();
    }
    
    private void sendString(DataOutputStream out, String sendstr) throws IOException
    {
        out.writeUTF(sendstr);
    }
    

    public void run()
        {
        try
            {
            sendCommand(out, Commands.NEW_SESSION_ACKNOWLEDGE);
            System.out.println("Client: New Session");
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
    
    private void waitForCommand(DataInputStream in,DataOutputStream out) throws IOException 
        {
        if(running)
            {
             System.out.println("Waiting for command from client...");
             String inMessage = readString(in);
            Commands command = Commands.fromString(inMessage);
            switch (command)
                {
            case NEW_SESSION_REQUEST:
                //The stuff in here was moved to "run" since it will always be called first.
                System.out.println("Getting to this println statment, in case NEW_SESSION_REQUEST: on switch(command), should be impossible...");
                break;
            case NEW_USID_REQUEST:
                sendString(out,genUSID());
                System.out.println("Client: New USID");
                break;
            case LOGIN_REQUEST:
                loginRequested(in,out);
                break;
            case UPDATE_REQUEST:
                System.out.println("Client: Update");
                sendString(out,LCX.databaseIF.readName(user.getUserNumber(),serverUSID));
                System.out.println("Client says " + readString(in));
                sendString(out,LCX.databaseIF.readLatinumString(user.getUserNumber(),serverUSID));
                System.out.println("Client says " + readString(in));
                sendString(out,LCX.databaseIF.readTransactionLog(user.getUserNumber(),serverUSID));
                break; 
            case NEW_USER_REQUEST:
                System.out.println("Client: New User");
                sendCommand(out, Commands.NEW_USER_ACKNOWLEDGE);
                String newUserAccNum = readString(in);
                sendCommand(out, Commands.RECEIPT_ACCOUNT_NUMBER);
                String newName = readString(in);
                sendCommand(out, Commands.RECEIPT_ACCOUNT_NAME);
                String newPass = readString(in);
                sendCommand(out, Commands.RECEIPT_ACCOUNT_PASSWORD);
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
                sendString(out, LCX.databaseIF.newAccountNumber());
                break;
            case NEW_TRANSFER_REQUEST:
                System.out.println("Client: Transfer");
                sendCommand(out,Commands.NEW_TRANSFER_AWAITING_RECEIPIENT);
                String transferTo = readString(in);
                sendCommand(out,Commands.NEW_TRANSFER_AWAITING_AMOUNT);
                String transferAmount = readString(in);
                userTransLog.log(Level.INFO, "[TRANSFER OUT]: Transfering: {0} Transfering to: {1}", new Object[]{transferAmount, transferTo});
                LCX.databaseIF.transfer(user.getUserNumber(),transferTo,transferAmount);
                userTransLog.log(Level.INFO, "[TRANSFER REPORT]: Transfer Complete. Account now has: {0}", LCX.databaseIF.readLatinumString(user.getUserNumber(),genUSID()));
                sendCommand(out,Commands.RECEIPT_TRANSFER_COMPLETE);
                break;
            case CONNECTION_CLOSE_REQUEST:
                System.out.println("Client: Close");
                sendCommand(out, Commands.CONNECTION_CLOSE_ACKNOWLEDGE);
                running = false;
                this.fh.close();
                in.close();
                out.close(); 
                break;
            default:
                System.err.println("[Error]: default case reached in command switch, an illegal command was probably sent by the client.");
                System.out.println("Attempting to send error to client...");
                sendString(out, "SERVER ERROR");
                if(readString(in).equals("Error Report Request"))
                    {
                    sendString(out,inMessage);
                    System.out.println("Client says " + readString(in));
                    sendString(out, "[Server Error]: The server reported an illegal command was sent, that command was: " + command);
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
    
    private void loginRequested(DataInputStream in,DataOutputStream out) throws IOException
        {
        System.out.println("Client: Login Request");
        System.out.println("Exiting command structure to recieve login data");
        sendCommand(out,Commands.LOGIN_AWAITING_ACCOUNT_NUMBER);
        String inAccountNum = readString(in);
        sendCommand(out,Commands.LOGIN_AWAITING_PASSWORD);
        String inPassword = readString(in);
        boolean validLogin = LCX.databaseIF.login(inAccountNum,inPassword,serverUSID);
        if(validLogin)
            {
            sendCommand(out,Commands.LOGIN_PREPARE_FOR_DETAILS);
            user = new UserAccount (inAccountNum,inPassword);
            System.out.println("Login was succesful, sending user data to client");
            sendString(out,LCX.databaseIF.readName(user.getUserNumber(),serverUSID));
            System.out.println(readString(in));
            sendString(out,LCX.databaseIF.readLatinumString(user.getUserNumber(),serverUSID));
            System.out.println(readString(in));
            sendString(out,LCX.databaseIF.readTransactionLog(user.getUserNumber(),serverUSID));
            System.out.println(readString(in));
            
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
            
            sendCommand(out, Commands.LOGIN_COMPLETE_RECEIPT);
            System.out.println("Returning to command structure");
            }
        else
            {
            sendCommand(out, Commands.LOGIN_FAIL_RECEIPT);
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
