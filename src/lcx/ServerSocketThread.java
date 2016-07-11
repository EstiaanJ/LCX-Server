/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import shared.MessageHeaders;
import shared.Message;
import shared.MessageHandler;
/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class ServerSocketThread implements Runnable 
//A thread and object of this class only ever deals with one client program instance (if the user closes the client program, the thread it used will expire)
    {
    
    private static final String PROTOCOL_VERSION = "0.1";
    private static final String SERVER_VERSION = "0.2";
    
    private final Socket clientSocket;
    private MessageHandler messageHandler;
    
    //Map of AuthTokens (GUIDs) as keys and Account numbers as values.
    private Map<String,String> sessions;
    
    
    public ServerSocketThread(Socket inClientSocket)
        {
        clientSocket = inClientSocket;
        
        LCX.systemLog.log(Level.INFO, "Accepted client. Now opening input and output streams.");
        try {
        messageHandler = new MessageHandler(clientSocket.getInputStream(),clientSocket.getOutputStream());
        } catch (IOException e) {
            LCX.systemLog.log(Level.SEVERE, "Unable to open object streams.");
            e.printStackTrace();
            LCX.systemLog.log(Level.SEVERE,e.getMessage());
        }
        LCX.systemLog.log(Level.FINE, "Client connected: {0}", clientSocket.getLocalSocketAddress());
        
        sessions = new HashMap<>();
        }
    
    
    @Override
    public void run()
        {
        /*
        LCX.systemLog.log(Level.FINE, "Determining client version.");
        determineClientVersion();
        LCX.systemLog.log(Level.FINE, "Client version is: {0}", clientInstance.getClientVersion());
        
        LCX.systemLog.log(Level.FINE,"Sending Server version to client: {0}", clientInstance.getServerVersion());
        sendCommand(CommonMessages.VERSION_MESSAGE_START + clientInstance.getServerVersion());
        
        LCX.systemLog.log(Level.FINE,"Server is ready to listen for client requests.");

        */
        LCX.systemLog.log(Level.FINE,"Waiting for request from client...");
        processMessages();
        closeConnection();
        }
    
    private void processMessages()
        {
            
            //Now we're awaiting a "Message" object to be sent, so we should open
            //an ObjectInputStream on the socket.
            
            Message inMsg = new Message(MessageHeaders.NO_MESSAGE,PROTOCOL_VERSION,new String[0],"");
            
            while (!inMsg.getHead().equals(MessageHeaders.CONNECTION_CLOSE_REQUEST)) {
            
            try {
                inMsg = messageHandler.receive();

                //If a user is logging in, then we need to do some variable management.
                switch (inMsg.getHead()) {
                    case LOGIN_REQUEST:
                        //This simply checks whether the credentials are valid or not.
                        if (LCX.databaseIF.login(inMsg.getData()[0], inMsg.getData()[1])) {
                            String guid = genGUID();
                            sessions.put(guid, inMsg.getData()[0]);
                            messageHandler.send(new Message(MessageHeaders.AUTH_TOKEN_ISSUE, PROTOCOL_VERSION, new String[]{guid}, null));
                        } else {
                            messageHandler.send(new Message(MessageHeaders.LOGIN_FAIL_RECEIPT, PROTOCOL_VERSION, new String[0], null));
                        }
                        break;
                    //Nothing bad will happen if people don't log out, but doing so will destroy the token.
                    case LOGOUT_REQUEST:
                        sessions.remove(inMsg.getAuthToken());
                        messageHandler.send(new Message(MessageHeaders.LOGOUT_CONFIRMED,PROTOCOL_VERSION,new String[0],null));
                        break;
                    default:
                        Message m = generateReply(inMsg);
                        if (m != null)
                            messageHandler.send(generateReply(inMsg));
                }
                
            } catch (EOFException e) {
                //This end-of-field exception means that the socket has been closed.
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            }
            
            
        }
    
    private void closeConnection()
        {
        try
            {
            clientSocket.getInputStream().close();
            clientSocket.getOutputStream().close();
            clientSocket.close();
            }
        catch(IOException e)
            {
            LCX.systemLog.log(Level.SEVERE,"Cound't close input stream");
            }
        }

    //The meat of this class!
    private Message generateReply(Message inMsg) {
        //The cases of login, logout and conneciton close requests should already be handled before this method is called.
        
        String authToken = inMsg.getAuthToken();
        if (!sessions.containsKey(authToken))
            return null;
        String accountNum = sessions.get(authToken);
                
        switch (inMsg.getHead()) {
            case BALANCE_INQUIRY:
                String balance = LCX.databaseIF.readLatinumString(accountNum);
                return new Message(MessageHeaders.BALANCE_STATEMENT,PROTOCOL_VERSION,new String[]{balance},null);
            default:
                return null;
        }
    }
       
    
    private String genGUID() {
        return java.util.UUID.randomUUID().toString();
    }
    }
