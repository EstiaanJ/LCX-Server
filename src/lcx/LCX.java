/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class LCX extends Thread
    {
    public static final String SERVER_VERSION = "test 0.3.b0"; //b is for bugfix version.
    public static final ConsoleHandler conHandle = new ConsoleHandler();
    public static final Logger systemLog = Logger.getLogger( LCX.class.getName() );
    public static FileHandler fileHandler;
    public static DatabaseInterface databaseIF;
    private final static int PORT = 2388;
    private ServerSocket serverSocket;
    private final static int STD_TIMEOUT = 5;
    private static boolean isListening = false;
    private static LCX lcx;
    
            
    public LCX() throws IOException
        {
        }
    
    @Override
    public void run()
        {
        setupSockets();
        }
    
    private void setupSockets()
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
        listenSockets();
        }
    
    public void listenSockets()
        {
        while(true)
            {
            systemLog.log(Level.FINE, "Waiting for client...");
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
        //Stop loggers printing to console by default.
        LogManager.getLogManager().reset();
        
        //Only messages that are fine or above, and are attached to this handler will print to console.
        conHandle.setLevel(Level.FINE);
        
        systemLog.addHandler(conHandle);
        //DatabaseInterface.dbLog.addHandler(conHandle);
        //Transfer.transLog.addHandler(conHandle);
        /*Try to create a file handler for systemLog, 
        make systemLog handled by the conHandle and fileHandler
        so that that messages to the console, and the log file can be
        controlled.
        Finnally print the server and protocal version.
        */
        try
            {
            int logNumber = 0;
            while((new File("log-" + logNumber + ".txt")).exists())
                {
                logNumber++ ;
                }
            fileHandler = new FileHandler("log-" + logNumber + ".txt");
            systemLog.addHandler(fileHandler);
            
            CustomFormatter formatter = new CustomFormatter();
            fileHandler.setFormatter(formatter);
            conHandle.setFormatter(formatter);
            
            systemLog.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            
            systemLog.log(Level.INFO, "Server running. Version: " + SERVER_VERSION);
            systemLog.log(Level.INFO, "Communication protocol version: " + ServerSocketThread.PROTOCOL_VERSION);
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        
        /*
        Set the console handler back to info, so that only important messages
        can be printed to the console.
        */
        
        conHandle.setLevel(Level.INFO);
        
        //Initialize the database.
        databaseIF = new DatabaseInterface();
        
        //Initialize and Start the thread that listens for command line messages
        (new Thread(new UserInterface())).start();

        //Initialize and Start the thread that listens for clients.
        
        try
            {
            lcx = new LCX();
            Thread listeningThread = lcx;
            listeningThread.start();
            }
        catch(IOException e)
            {
            System.err.println("Could not create new thread");
            System.exit(-1);
            }
        }
    /*
    public static void startListening()
        {
        setIsListening(true);
        lcx.listenSockets();
        systemLog.log(Level.INFO,"Starting listen thread.");
        }
    
    public static void setIsListening(boolean inState)
        {
        isListening = inState;
        }
    
    public static boolean getIsListening()
        {
        return isListening;
        }
    */
    }
