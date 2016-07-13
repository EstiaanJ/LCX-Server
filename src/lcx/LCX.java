/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import static lcx.DatabaseInterface.DB_LOG_DIR;


/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class LCX extends Thread
    {
    public static final Logger systemLog = Logger.getLogger( LCX.class.getName() );
    public static FileHandler fileHandler;
    public static DatabaseInterface databaseIF;
    private final static int PORT = 2388;
    private ServerSocket serverSocket;
    private final static int STD_TIMEOUT = 5;
            
    public LCX() throws IOException
        {
        }
    
    @Override
    public void run()
        {
        listenSocket();
        }
    
    public void listenSocket()
        {
        try
            {
            serverSocket = new ServerSocket(PORT);
            }
        catch(IOException e)
            {
            System.err.println("Could not listen on port " + String.valueOf(PORT));
            System.exit(-1);
            }
        systemLog.log(Level.FINE, "Ready for clients.");
        while(true)
            {
            systemLog.log(Level.INFO, "Waiting for client...");
            ServerSocketThread server;
            try
                {
                server = new ServerSocketThread(serverSocket.accept());
                Thread thread = new Thread(server);
                thread.start();
                }
            catch (IOException e)
                {
                System.err.println("Accept failed: " + String.valueOf(PORT));
                System.exit(-1);   
                }
            }
        }


    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args)
        {
        
        try
            {
            int logNumber = 0;
            while((new File("log-" + logNumber + ".txt")).exists())
                {
                logNumber++ ;
                }
            fileHandler = new FileHandler("log-" + logNumber + ".txt");
            systemLog.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            systemLog.setLevel(Level.ALL);
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        
        databaseIF = new DatabaseInterface();
        try
            {
            Thread listeningThread = new LCX();
            listeningThread.start();
            }
        catch(IOException e)
            {
            System.err.println("Could not create new thread");
            System.exit(-1);
            }
        }
    }
