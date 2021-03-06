/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.EOFException;
import java.io.IOException;
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

    public static final String PROTOCOL_VERSION = "0.1";

    private final Socket clientSocket;
    private MessageHandler messageHandler;

    //Map of AuthTokens (GUIDs) as keys and Account numbers as values.
    private Map<String, String> sessions;

    public ServerSocketThread(Socket inClientSocket)
        {
        clientSocket = inClientSocket;

        LCX.systemLog.log(Level.INFO, "Accepted client. Now opening input and output streams.");
        try
            {
            messageHandler = new MessageHandler(clientSocket.getInputStream(), clientSocket.getOutputStream());
            }
        catch (IOException e)
            {
            LCX.systemLog.log(Level.SEVERE, "Unable to open object streams.");
            e.printStackTrace();
            LCX.systemLog.log(Level.SEVERE, e.getMessage());
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
        LCX.systemLog.log(Level.FINE, "Clieprivateprivateprivatent version is: {0}", clientInstance.getClientVersion());
        
        LCX.systemLog.log(Level.FINE,"Sending Server version to client: {0}", clientInstance.getServerVersion());
        sendCommand(CommonMessages.VERSION_MESSAGE_START + clientInstance.getServerVersion());
        
        LCX.systemLog.log(Level.FINE,"Server is ready to listen for client requests.");

         */
        LCX.systemLog.log(Level.FINE, "Waiting for request from client...");
        processMessages();
       closeConnection();
        }

    private void processMessages()
        {
            
            Message inMsg = new Message(MessageHeaders.NO_MESSAGE,PROTOCOL_VERSION,new String[0],"");
            
            while (!inMsg.getHead().equals(MessageHeaders.CONNECTION_CLOSE)) {
            
                try {
                    inMsg = messageHandler.receive();

                    if (inMsg.getHead().equals(MessageHeaders.NEW_ACCOUNT_REQUEST)) {
                        String name = inMsg.getData()[0];
                        String pass = inMsg.getData()[1];
                        String accNum = LCX.databaseIF.createNewAccount(name,pass);
                        messageHandler.send(new Message(MessageHeaders.NEW_ACCOUNT_RECEIPT,PROTOCOL_VERSION,new String[]{accNum},null));
                        continue;
                    }

                //If a user is doing anything but logging in, we need to check that their token is still valid.
                //If the client submitted a non-empty token but that token is not a recognised session, then we need to tell them this.
                if (!inMsg.getHead().equals(MessageHeaders.LOGIN_REQUEST))
                    {
                    if (!inMsg.getAuthToken().equals(""))
                        {
                        String token = inMsg.getAuthToken();
                        if (!sessions.containsKey(token))
                            {
                            messageHandler.send(new Message(MessageHeaders.SESSION_EXPIRED_NOTIFY, PROTOCOL_VERSION, new String[0], null));
                            continue;
                            }
                        }
                    }

                switch (inMsg.getHead())
                    {
                case LOGIN_REQUEST:
                    //Using the LCXDelegate class, a login request should never be made with a non-empty token.
                    assert (inMsg.getAuthToken().equals(""));

                    //This simply checks whether the credentials are valid or not.
                    if (LCX.databaseIF.login(inMsg.getData()[0], inMsg.getData()[1]))
                        {
                        String guid = genGUID();
                        sessions.put(guid, inMsg.getData()[0]);
                        messageHandler.send(new Message(MessageHeaders.AUTH_TOKEN_ISSUE, PROTOCOL_VERSION, new String[]
                            {
                            guid
                            }, null));
                        }
                    else
                        {
                        messageHandler.send(new Message(MessageHeaders.LOGIN_FAIL_RECEIPT, PROTOCOL_VERSION, new String[0], null));
                        }
                    break;
                //Nothing bad will happen if people don't log out, but doing so will destroy the token.
                case PING:
                    messageHandler.send(new Message(MessageHeaders.PONG, PROTOCOL_VERSION, new String[0], null));
                    break;
                case LOGOUT_REQUEST:
                    sessions.remove(inMsg.getAuthToken());
                    messageHandler.send(new Message(MessageHeaders.LOGOUT_CONFIRMED, PROTOCOL_VERSION, new String[0], null));
                    break;
                default:
                    Message m = generateReply(inMsg);
                    if (m != null)
                        {
                        messageHandler.send(m);
                        }
                    }

                }
            catch (EOFException e)
                {
                //This end-of-field exception means that the socket has been closed.
                //So we should just end the thread safely by letting it run its course.
                break;
                }
            catch (IOException e)
                {
                //Something's wrong with the communication between the client and server. Abort.
                e.printStackTrace();
                break;
                }

            }

        }

    private void closeConnection()
        {
        try
            {
            if(!clientSocket.isClosed()) {
                clientSocket.close();
                //clientSocket.getInputStream().close();
                //clientSocket.getOutputStream().close();
            }
            }
        catch (IOException e)
            {
            LCX.systemLog.log(Level.SEVERE, "Cound't close input stream");
            }
        }

    //The meat of this class!
    private Message generateReply(Message inMsg)
        {
        //The cases of login, logout and conneciton close requests should already be handled before this method is called.
        //Also, if we're at this method, then the client has a valid authentication token.

        String authToken = inMsg.getAuthToken();
        if (!sessions.containsKey(authToken))
            {
            return null;
            }
        String accountNum = sessions.get(authToken);
        
        String[] data = inMsg.getData();

        switch (inMsg.getHead())
            {
        case BALANCE_INQUIRY:
            String balance = LCX.databaseIF.readLatinumString(accountNum);
            return new Message(MessageHeaders.BALANCE_STATEMENT, PROTOCOL_VERSION, new String[]
                {
                balance
                }, null);
        case TRANSFER_REQUEST:
            Message reply;
            if (LCX.databaseIF.transfer(accountNum, data[0], data[1])) {
                reply = new Message(MessageHeaders.TRANSFER_RECEIPT_SUCCESS,PROTOCOL_VERSION,new String[0],null);
            } else {
                reply = new Message(MessageHeaders.TRANSFER_RECEIPT_FAIL,PROTOCOL_VERSION,new String[0],null);
            }
            return reply;
            
        default:
            return null;
            }
        }

    private String genGUID()
        {
        return java.util.UUID.randomUUID().toString();
        }
    }
