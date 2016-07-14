/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;
import consoleinput.ConsoleInput;
import java.math.BigDecimal;
/**
 *
 * @author estiaan
 */
public class UserInterface implements Runnable
    {
    final static String EXIT = "exit";
    final static String STOP_LISTEN = "stopListening";
    final static String SET_LEVEL = "setLevel"; //setLevel [log] [level] where log is systemLog or databaseLog and level is any of the log level strings
    final static String START_LISTEN = "startListening";
    final static String CREATE_ACCOUNT = "createAccount";   //CreateAccount [Account Num] [Password] [Name] [Latinum]
    final static String SET_LATINUM = "setLatinum"; //SetLatinum [Account Num] [Latinum]
    final static String SET_PASS = "setPassword"; //SetPassword [Account Num] [Password]
    final static String SET_NAME = "setName";//SetName [Account Num] [Name]
    final static String HELP = "help";//help [Command] or just help for list of commands
    final static String HELP2 = "Help";//Help [Command] or just help for list of commands
    final static String LIST = "ls"; // ls [Flag] where -a is accounts, -c is commands -d is database logs and -s is system logs.
    final static String VERSION = "version";//version [Flag] where -s is server version, -l is shared library version and -p is protocal version
    final static String ADD_LATINUM = "addLatinum"; //addLatinum [Account Num]
    final static String SUB_LATINUM = "subLatinum"; //subLatinum [Account Num]
    final static String SET_FEE = "setFee";//setFee [Latinum]
    final static String GET_FEE = "getFee";
    final static String GET_LCX_ACCOUNTS = "getLCXAccountNums";
    final static String GET_NAME = "getName";//getName [Account Num]
    final static String GET_ACCOUNT = "getAccount";//getAccount [Name]
    final static String GET_LATINUM = "getLatinum"; //getLatinum [Account Num]
    final static String CONFIRM1 = "y";
    final static String CONFIRM2 = "yes";
    final static String NEGATIVE1 = "n";
    final static String NEGATIVE2 = "no";
    //final static String NO_ARG = "#";
   
    @Override
    public void run()
        {
        String[] userInput;
        while(true)
            {
            System.out.println("---");
            System.out.println("Ready for new command: ");
            
            String message = ConsoleInput.readLine();
            userInput = message.split(" ");
            
            String command = userInput[0];
            userInput = moveDown(userInput);
            switch(command)
                {
                case EXIT:
                    exit();
                    break;
                case STOP_LISTEN:
                    stopListening();
                    break;
                case SET_LEVEL:
                    setLevel(userInput);
                    break;
                case START_LISTEN:
                    startListening();
                    break;
                case CREATE_ACCOUNT:
                    createAccount(userInput);
                    break;
                case SET_LATINUM:
                    setLatinum(userInput);
                    break;
                case SET_PASS:
                    setPass(userInput);
                    break;
                case SET_NAME:
                    setName(userInput);
                    break;
                case HELP:
                    help(userInput);
                    break;
                case LIST:
                    list(userInput);
                    break;
                case VERSION:
                    version(userInput);
                    break;
                case ADD_LATINUM:
                    addLatinum(userInput);
                    break;
                case SUB_LATINUM:
                    subLatinum(userInput);
                    break;
                case SET_FEE:
                    setFee(userInput);
                    break;
                case GET_FEE:
                    printFee();
                    break;
                case GET_LCX_ACCOUNTS:
                    printLCXAccounts();
                    break;
                case GET_NAME:
                    printName(userInput);
                    break;
                case GET_ACCOUNT:
                    printAccount(userInput);
                    break;                   
                case GET_LATINUM:
                    printLatinum(userInput);
                    break;
                case "TEST":
                    testArgs(userInput);
                    break;
                   
                default:
                    System.err.println("[Input Error] Invalid command: " + command);
                    System.out.println("For help enter 'help'");
                    System.out.println("For a list of commands enter 'ls -c'");
                }
            }
        }
    
    private void exit()
        {
        String responce = ConsoleInput.readLine("Are you sure? [Y/n]");
        responce = responce.toLowerCase();
        if(responce.equals(CONFIRM1) || responce.equals(CONFIRM2))
            {
            System.exit(0);
            }          
        }
    
    private void testArgs(String[] args)
        {
        System.out.println("This is a test method, used to test if arguments are recieved correctly: ");
        for(int i = 0; i < args.length; i++)
            {
            System.out.println("Argument " + i + " was: " + args[i]);
            }
        }
    
    private void stopListening()
        {
        System.out.println("Not Implemented yet.");
        }
    
    private void setLevel(String[] args)
        {
        System.out.println("Not implemented yet.");
        }
    
    private void startListening()
        {
        System.out.println("Not implemented yet.");
        }
    
    private void createAccount(String[] args)
        {
        final String auto = "Automatically generated";
        String accountNum = auto;
        String password;
        String name;
        String latinum = "0";
        
        if(args.length == 4)
            {
            accountNum = args[0];
            password = args[1];
            name = args[2];
            latinum = args[3];
            }
        else if( args.length == 2)
            {
            password = args[1];
            name = args[2];
            }
        else
            { 
            System.out.println("Argument length was incorrect, create account manually:");
            accountNum = ConsoleInput.readLine("Please enter new account number:");
            password = ConsoleInput.readLine("Please enter new account password:");
            name = ConsoleInput.readLine("Please enter name for new account:");
            latinum = ConsoleInput.readLine("Please enter latinum for new account:");
            }
        
        try
            {
            if(Integer.valueOf(accountNum) < 100000 || Integer.valueOf(accountNum) > 999999)
                {
                System.err.println("[Input Error] The account number was invalid: Too long, too short, or it had leading zeros.");
                return;
                }
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The account number was not an integer.");  
            }
        
        System.out.println("New Account details: ");
        System.out.println("Account Number: " + accountNum);
        System.out.println("Name: " + name);
        System.out.println("Latinum: " + latinum);
        
        String confirm = ConsoleInput.readLine("Confirm new account? [Y/n]");
        confirm = confirm.toLowerCase();
        if(confirm.equals(CONFIRM1) || confirm.equals(CONFIRM2) )
            {
            if(accountNum.equals(auto))
                {
                System.out.println("New Account number is: " + LCX.databaseIF.createNewAccount(name,password));
                }
            else
                {
                LCX.databaseIF.createNewAccount(accountNum, name, password);
                }
            }
        else
            {
            String responce = ConsoleInput.readLine("Would you like to cancel new account? [Y/n]");
            if(responce.equals(CONFIRM1) || responce.equals(CONFIRM2))
                {
                
                }
            else
                {
                createAccount(args);
                }
            }
        }
    
    private void setLatinum(String[] args)
        {
        String latinum;
        String accountNum;
        if(args.length < 2)
            {
            System.out.println("Argument length was incorrect, set latinum manually:");
            accountNum = ConsoleInput.readLine("Enter Account Number: ");
            latinum = ConsoleInput.readLine("Enter Latinum: ");
            }
        else
            {
            accountNum = args[0];
            latinum = args[1];
            }
        //Check that account number is a valid one.
        try
            {
            if(Integer.valueOf(accountNum) < 100000 || Integer.valueOf(accountNum) > 999999)
                {
                System.err.println("[Input Error]The account number was invalid: Too long, too short, or it had leading zeros.");
                return;
                }
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The account number was not an integer.");  
            }
                
        try
            {
            BigDecimal lat = new BigDecimal(latinum);
            LCX.databaseIF.writeLatinum(accountNum, lat);
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The value for latinum was not a valid number.");
            }
        }
    
    private void setPass(String[] args)
        {
        String latinum;
        String accountNum;
        if(args.length < 2)
            {
            System.out.println("Argument length was incorrect, set latinum manually:");
            accountNum = ConsoleInput.readLine("Enter Account Number: ");
            latinum = ConsoleInput.readLine("Enter Latinum: ");
            }
        else
            {
            accountNum = args[0];
            latinum = args[1];
            }
        
        try
            {
            BigDecimal lat = new BigDecimal(latinum);
            LCX.databaseIF.writeLatinum(accountNum, lat);
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The value for latinum was not a valid number.");
            }
        }
    
    private void setName(String[] args)
        {
        String acc;
        String name;
        if(args.length < 2)
            {
            acc = ConsoleInput.readLine("Please enter account number: ");
            name = ConsoleInput.readLine("Please enter new name: ");
            }
        else
            {
            acc = args[0];
            name = args[1];
            }
        
        try
            {
            Integer.valueOf(acc);
            LCX.databaseIF.writeName(acc, name);
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The given account number was not a number: " + acc);
            }
        }
    
    private void help(String[] args)
        {
        if(args.length  < 1)
            {
            System.out.println("The usage of commands without arguments is simply [command] e.g. : " + EXIT);
            System.out.println("The usage of commands with flags is: [command] -[flag 1] -[flag 2] e.g. : " + LIST + " -c");
            System.out.println("The usage of commands with arguments is: [command] [arg 1] [arg 2] e.g. : " + UserInterface.SET_FEE + " 10");
            list(new String[] {"-c"});
            }
        else
            {
            System.out.println("Not availble yet.");
            }
        }
    
    private void list(String[] args)
        {
        if(args.length < 1)
            {
            System.out.println("This command requires a flag!");
            }
        if(args[0].equals("-c"))
            {
            System.out.println();
            System.out.println("-------------------------------------------");
            System.out.println("This is a list of all commands. For details on using a specific command enter: " + HELP + " [command]");
            System.out.println();
            System.out.println(UserInterface.ADD_LATINUM);
            System.out.println(UserInterface.CREATE_ACCOUNT);
            System.out.println(UserInterface.EXIT);
            System.out.println(UserInterface.GET_ACCOUNT);
            System.out.println(UserInterface.GET_FEE);
            System.out.println(UserInterface.GET_LATINUM);
            System.out.println(UserInterface.GET_LCX_ACCOUNTS);
            System.out.println(UserInterface.GET_NAME);
            System.out.println(UserInterface.HELP);
            System.out.println(UserInterface.LIST);
            System.out.println(UserInterface.SET_FEE);
            System.out.println(UserInterface.SET_LATINUM);
            System.out.println(UserInterface.SET_LEVEL);
            System.out.println(UserInterface.SET_NAME);
            System.out.println(UserInterface.SET_PASS);
            System.out.println(UserInterface.START_LISTEN);
            System.out.println(UserInterface.STOP_LISTEN);
            System.out.println(UserInterface.SUB_LATINUM);
            System.out.println(UserInterface.VERSION);
            System.out.println("-------------------------------------------");
            }
        else
            {
            System.out.println("Not implemented yet.");
            }
        
        }
    
    private void version(String[] args)
        {
        if(args.length == 0)
            {
            System.out.println("Server Version: " + LCX.SERVER_VERSION);
            //TODO: Make library version constant public.
            System.out.println("Library Version: Is private for now.");
            System.out.println("Protocal Version: " + ServerSocketThread.PROTOCOL_VERSION);
            
            }
        else
            {
            args[0] = args[0].toLowerCase();
            if(args[0].equals("-s"))
                {
                System.out.println("Server Version: " + LCX.SERVER_VERSION);
                }
            else if(args[0].equals("-p"))
                {
                System.out.println("Protocal Version: " + ServerSocketThread.PROTOCOL_VERSION);
                }
            else{
                System.out.println("Library version and protocol version are private");
                System.out.println();
                }
            }
        }
    
    private void addLatinum(String[] args)
        {
        System.out.println("Not implemented yet.");
        }
    
    private void subLatinum(String[] args)
        {
        System.out.println("Not implemented yet.");
        }
    
    private void printFee()
        {
        System.out.println("The LCX Tranfer Fee is: " + (Double.valueOf(DatabaseInterface.getFee())*100) + "% of the value of said transfer.");
        }
    
    private void setFee(String[] args)
        {
        String inFee;
        if(args.length == 0)
            {
            inFee = ConsoleInput.readLine("Please enter the fee as a percentage (no percentage sign!).");
            }
        else
            {
            inFee = args[0];
            }
        
        try
            {
            BigDecimal validNumTest = new BigDecimal(inFee);
            DatabaseInterface.setFee(inFee);
            }
        catch(NumberFormatException nfe)
            {
            System.out.println("Set fee to: " + inFee + "?");
            System.out.println("The 'fee' argument or input was not a number.");
            }
        }
    
    private void printLCXAccounts()
        {
        System.out.println("Not implemented yet.");
        }
    
    private void printName(String[] args)
        {
        String acc;
        if(args.length == 0)
            {
            acc = ConsoleInput.readLine("Please enter account number: ");
            }
        else
            {
            acc = args[0];
            }
        
        try
            {
            Integer.valueOf(acc);
            System.out.println(LCX.databaseIF.readName(acc));
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The given account number was not a number: " + acc);
            }
        }
    
    private void printAccount(String[] args)
        {
        System.out.println("This function is experimental; it only returns the last account it finds that has the given name.");
        String name;
        if(args.length == 0)
            {
            name = ConsoleInput.readLine("Please enter name: ");
            }
        else
            {
            name = args[0];
            }

        System.out.println(LCX.databaseIF.readAcc(name));
        }
    
    private void printLatinum(String[] args)
        {
        String acc;
        if(args.length == 0)
            {
            acc = ConsoleInput.readLine("Please enter account number: ");
            }
        else
            {
            acc = args[0];
            }
        
        try
            {
            Integer.valueOf(acc);
            System.out.println(LCX.databaseIF.readLatinumString(acc));
            }
        catch(NumberFormatException nfe)
            {
            System.err.println("The given account number was not a number: " + acc);
            }
        }
    
    private static String[] moveDown(String[] inArray)
        {
        String[] retArray = new String[inArray.length - 1];
        for(int i = 0; i < retArray.length; i++)
            {
            retArray[i] = inArray[i + 1];
            }
        return retArray;
        }
    }
