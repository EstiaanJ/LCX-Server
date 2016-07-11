/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import shared.CommonMessages;
/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class Server implements Runnable 
//A thread and object of this class only ever deals with one client program instance (if the user closes the client program, the thread it used will expire)
    {
    private static final String SERVER_VERSION = "0.2";
    private BufferedReader in;
    private PrintWriter out;
    private final Socket clientSocket;
    private ClientInstance clientInstance;
    
    public Server(Socket inClientSocket)
        {
        clientInstance = new ClientInstance(SERVER_VERSION);
        clientSocket = inClientSocket;
        try
            {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream());
            }
        catch(SocketTimeoutException ste)
                {
                LCX.systemLog.log(Level.SEVERE, "Socket timed out while recieving data streams. {0}", ste.toString());
                }
        catch(IOException e)
                {
                LCX.systemLog.log(Level.SEVERE, "IOException while recieving data streams. {0}", e.toString());
                }
        LCX.systemLog.log(Level.FINE, "Client connected: {0}", clientSocket.getLocalSocketAddress());
        }
    
    
    @Override
    public void run()
        {
        LCX.systemLog.log(Level.FINE, "Determining client version.");
        determineClientVersion();
        LCX.systemLog.log(Level.FINE, "Client version is: {0}", clientInstance.getClientVersion());
        
        LCX.systemLog.log(Level.FINE,"Sending Server version to client: {0}", clientInstance.getServerVersion());
        sendCommand(CommonMessages.VERSION_MESSAGE_START + clientInstance.getServerVersion());
        
        LCX.systemLog.log(Level.FINE,"Server is ready to listen for client requests.");

        LCX.systemLog.log(Level.FINE,"Waiting for request from client...");
        waitForCommands();
        closeConnection();
        }
    
    private void determineClientVersion()
        {
        sendCommand(CommonMessages.VERSION_REQUEST.msg());

        String version = recieveMessage();
        
        if(version.contains(CommonMessages.VERSION_MESSAGE_START.msg()))
            {
            //Set the client version to the recieved version, but remove the "VERSION" part at the front.
            LCX.systemLog.log(Level.FINER,"Client Version Recieved");
            clientInstance.setClientVersion(version.replace(CommonMessages.VERSION_MESSAGE_START.msg(),""));
            }
        else
            {
            LCX.systemLog.log(Level.WARNING, "Failed to establish client version, client sent {0}", version);
            clientInstance.setClientVersion(CommonMessages.DEFAUL_VERSION.msg()); 
            }
        }
    
    private void waitForCommands()
        {
        String recievedMessage;
        CommonMessages command;
        
        while(clientInstance.isActive() && (recievedMessage = recieveMessage()) != null )
            {
            command = CommonMessages.fromString(recievedMessage);
            if(command == null)
                {
                LCX.systemLog.log(Level.SEVERE,"An invalid command was recieved from client: {0}", command);
                break;
                }
            switch(command)
                {
            case LOGIN_REQUEST:
                
                break;
                }
            }
        }
    
    private void closeConnection()
        {
        try
            {
            in.close();
            }
        catch(IOException e)
            {
            LCX.systemLog.log(Level.SEVERE,"Cound't close input stream");
            }
        out.close();
        }
    
    private boolean containsWord(String text,String word)
        {
        boolean doesContain = false;
        doesContain = word.contains(word);
        return doesContain;
        }
    
    private String recieveMessage()
        {
        String message = CommonMessages.NO_MESSAGE.msg();
        
        try
            {
            message = in.readLine();
            LCX.systemLog.log(Level.FINEST,"Read message from in stream buffer: {0}",message);
            }
        catch(IOException e)
            {
            LCX.systemLog.log(Level.WARNING,"Failed to read message from client");
            }
        return message;
        }
    
    private void sendCommand(String inCommand)
        {
        LCX.systemLog.log(Level.FINEST,"Sending message to client: {0}",inCommand);
        out.println(inCommand);
        }
    }
