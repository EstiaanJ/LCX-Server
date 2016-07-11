/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class ClientAcceptor implements Runnable
    {
    private Server clientWorker;
    private DataInputStream in;
    private DataOutputStream out;
    private final Socket client;
    private final Session session;
    
    public ClientAcceptor(Socket inClient)
            {
            session = new Session();
            client = inClient;
            try
                { 
                in = new DataInputStream(client.getInputStream());
                out = new DataOutputStream(client.getOutputStream());
                }
            catch(SocketTimeoutException ste)
                {
                //TODO: Log this!
                }
            catch(IOException e)
                {
                //TODO: Log this!
                }
            System.out.println("Client connected: " + client.getLocalSocketAddress() + " USID: " + session.getSID());
            }
    
    @Override
    public void run()
        {
        try
            { 
            String firstCommand = in.readUTF();
            if(Commands.PREPARE_FOR_VERSION.equals(firstCommand))
                {
                //Launch appropriate ClientWorker<version> for the version.
                /*
                To be clear, version here refers to the communication protocal version.
                Changes can be made to a ClientWorker class without changing it's communcation protocal version,
                which is only changed if the commands sent between the client and server are changed.
                I'm not sure how to handle all of this yet.
                */
                }
            else
                {
                clientWorker = new Server(client,in,out,firstCommand);
                clientWorker.run();
                }
            }
        catch(SocketTimeoutException ste)
            {
            //TODO: Log this!
            }
        catch(IOException e)
            {
            //TODO: Log this!
            }
        }
    }
