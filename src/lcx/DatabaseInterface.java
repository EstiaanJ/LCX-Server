/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.UserAccount;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class DatabaseInterface
    {
    private final static String DB_DIR = "database" + File.separator;
    private final static String DB_LOG_DIR = "database" + File.separator + "dblogs" + File.separator;
    private final static String LCX_FEE_ACCOUNT_NUMBER = "816192";
    private final static Logger dbLog = Logger.getLogger(LCX.class.getName());
    private static FileHandler fh;
    private FileWriter accountWriter;
    private BufferedWriter accountWriteBuffer;
    private FileReader accountReader;
    private BufferedReader accountReadBuffer;
    //***************************** Constructors | Standard OO stuff **********************************

    public DatabaseInterface()
        {
        try
            {
            fh = new FileHandler(DB_LOG_DIR + "databaseLog" + LocalDateTime.now().toString() + ".txt");
            }
        catch(IOException e)
            {
            e.printStackTrace();
            }
        }
    
    

    //***************************** Setters | Standard OO stuff **********************************
    //***************************** Getters | Standard OO stuff **********************************
    //***************************** Server Requests | Highest Level stuff **********************************
    public boolean login(String inAcc, String inPass)
        {
        dbLog.log(Level.FINE, "Server requested login for Account: {0}", inAcc);
        boolean validLogin = false;
        String actualPass = readFileLine(inAcc, 1);
        if (inPass.equals(actualPass))
            {
            dbLog.log(Level.FINE, "Passwords Matched");
            validLogin = true;
            }
        else
            {
            dbLog.log(Level.FINE, "Passwords did not Match");
            }
        return validLogin;
        }
    
    public boolean createNewAccount(String inName, String inPass)
        {
        return createNewAccount(newAccountNumber(),inName,inPass);
        }
    
    public boolean createNewAccount(String inAccNum, String inName, String inPass)
        {
        boolean wasCreated = false;
        dbLog.log(Level.FINE, "Server requested for a new account to be created with Account Number: {0} With Name: {1}", new Object[]
            {
            inAccNum, inName
            });
        try
            {
            accountWriter = new FileWriter(DB_DIR + inAccNum + ".csv", true);
            dbLog.log(Level.FINEST, "Created file: {0}.csv", inAccNum);
            accountWriteBuffer = new BufferedWriter(accountWriter);
            accountWriteBuffer.write(inAccNum);
            accountWriteBuffer.write(System.lineSeparator());
            dbLog.log(Level.FINEST, "Wrote Account Number: {0} to memory for file: {1}.csv", new Object[]
                {
                inAccNum, inAccNum
                });
            accountWriteBuffer.write(inPass);
            accountWriteBuffer.write(System.lineSeparator());
            dbLog.log(Level.FINEST, "Wrote Password to memory for file: {0}.csv", inAccNum);
            accountWriteBuffer.write(inName);
            accountWriteBuffer.write(System.lineSeparator());
            dbLog.log(Level.FINEST, "Wrote Name: {0} to memory for file: {1}.csv", new Object[]
                {
                inName, inAccNum
                });
            accountWriteBuffer.write("0");
            accountWriteBuffer.close();
            accountWriter.close();
            dbLog.log(Level.FINEST, "Wrote all changes from memory to file: {0}.csv", inAccNum);
            wasCreated = true;
            }
        catch (IOException e)
            {
            wasCreated = false;
            }
        return wasCreated;
        }

    public boolean transfer(String inFrom, String inTo, String inAmount)
        {
        dbLog.log(Level.FINE, "Server requested transfer from: {0} to: {1} Ammount: {2}", new Object[]
            {
            inFrom, inTo, inAmount
            });
        boolean didTransfer = false;

        String fromStartLatinum = readFileLine(inFrom, 3);

        System.out.println("Transfer From Account: " + inFrom + " had: " + fromStartLatinum);

        BigDecimal fromLatinum = new BigDecimal(fromStartLatinum);
        BigDecimal amount = new BigDecimal(inAmount);
        BigDecimal fee = new BigDecimal(amount.toPlainString());
        fee = fee.multiply(new BigDecimal("0.001"));
        System.out.println("Fee is: " + fee.toPlainString());
        fromLatinum = fromLatinum.subtract(amount);
        fromLatinum = fromLatinum.subtract(fee);
        System.out.println("Transfer From Account: " + inFrom + " now has: " + fromLatinum.toPlainString());

        String toStartLatinum = readFileLine(inTo, 3);
        System.out.println("Transfer To Account: " + inTo + " had: " + toStartLatinum);
        BigDecimal toLatinum = new BigDecimal(toStartLatinum);
        toLatinum = toLatinum.add(amount);
        System.out.println("Transfer To Account: " + inTo + " now has: " + toLatinum.toPlainString());

        String bankStartLatinum = readFileLine(LCX_FEE_ACCOUNT_NUMBER , 3);
        System.out.println("Bank Account had: " + bankStartLatinum);
        BigDecimal bankLatinum = new BigDecimal(bankStartLatinum);
        bankLatinum = bankLatinum.add(fee);
        System.out.println("Bank Account now has: " + bankLatinum.toPlainString());

        System.out.println("Writing 'Transfer From' Account");
        overwriteLine(inFrom, 3, fromLatinum.toPlainString());
        System.out.println("Writing 'Transfer To' Account");
        overwriteLine(inTo, 3, toLatinum.toPlainString());
        System.out.println("Writing Bank Account");
        overwriteLine(LCX_FEE_ACCOUNT_NUMBER, 3, bankLatinum.toPlainString());

        return true;
        }
    
    /**
    *Request a new account number, for a new account, from the database.
    * This method will randomly generate a new account number between 100 000 and 999 999
    * and then check if a file name with that number already exists. If not it will return
    * the generated number as a string. If it does then it will call newAccountNumber(),
    * and whatever that method returns will be the new account number.
    *
    * @return a String unique and unused account number.
    */
    public String newAccountNumber()
        {
        String newAccountNum = Integer.toString(ThreadLocalRandom.current().nextInt(100000, 999999));
        File f = new File(newAccountNum + ".csv");
        if (f.exists())
            {
            newAccountNum = newAccountNumber();
            }
        return newAccountNum;
        }

    //***************************** Server Direct Interface Methods | High Level stuff **********************************
    
    /**
    *safely close the database after writing a status flag to the log.
    * At the moment that means saving the database log.
    *
    * @param flag the status flag
    */
    public void close(int flag)
        {
        dbLog.log(Level.INFO, "The server has requested the database to close with the flag: {0}", flag);
        fh.close();
        }    
    
    /**
    *Safely close the database with the status flag 0.
    * At the moment that means saving the database log.
    *
    * @param flag the status flag
    */
    public void close()
        {
        close(0);
        }
    
    public void writeName(String inAcc, String inName)
        {
        overwriteLine(inAcc, 2, inName);
        }

    public void writeLatinum(String inAcc, String inLatinum)
        {
        overwriteLine(inAcc, 3, inLatinum);
        }

    public void writeLatinum(String inAcc, BigDecimal inLatinum)
        {
        writeLatinum(inAcc, inLatinum.toPlainString());
        }

    public void writePassword(String inAcc, String inPassword)
        {
        overwriteLine(inAcc, 1, inPassword);
        }

    public String readName(String inAcc)
        {
        String name = readFileLine(inAcc, 2);
        return name;
        }

    
    /**
    *Return a specific account number when given a username.
    *If no account is found with the provided username, it will return "000000".
    *
    * @param inName the username of the account to be seached for.
    * @return a string, the last account number it scans that matches the inName argument, or "000000" if no match is found.
    */
    public String readAcc(String inName)
        {
        String acc = "000000";
        File[] allAccounts = ls(DB_DIR);
        String[] allAccountNumbers = new String[allAccounts.length];
        for (int i = 0; i < allAccounts.length; i++)
            {
            allAccountNumbers[i] = allAccounts[i].getName();
            String name = readName(allAccountNumbers[i]);
            if (inName.equals(name))
                {
                acc = allAccountNumbers[i];
                break;
                }
            }
        return acc;
        }

    public BigDecimal readLatinum(String inAcc)
        {
        BigDecimal money = new BigDecimal(readFileLine(inAcc, 3));
        return money;
        }

    public String readLatinumString(String inAcc)
        {
        String money = readLatinum(inAcc).toPlainString();
        return money;
        }

    public String readTransactionLog(String inAcc)
        {
        String log = "default";
        return log;
        }

    public UserAccount getAccountFromName(String inName)
        {
        String accountNum = readAcc(inName);
        UserAccount userAcc = new UserAccount(accountNum, inName);
        userAcc.setLatinum(readLatinumString(accountNum));
        return userAcc;
        }

    //***************************** OS/Storage Interface methods | Lowest Level Stuff **********************************
    /*These methods should always be private*/
    private void writeFileAppend()
        {

        }

    private void overwriteFile()
        {

        }

    private void overwriteLine(String inFileName, int pos, String inLine)
        {
        try
            {
            dbLog.log(Level.FINER, "Server requested to overwrite line number {0} in file {1}", new Object[]
                {
                pos, inFileName
                });
            List<String> allLines = new ArrayList();
            dbLog.log(Level.FINEST, "Opening file as read only {0}.csv", inFileName);
            accountReader = new FileReader(DB_DIR + inFileName + ".csv");
            accountReadBuffer = new BufferedReader(accountReader);

            dbLog.log(Level.FINEST, "Reading file into an Array List");
            String line;
            while ((line = accountReadBuffer.readLine()) != null)
                {
                allLines.add(line);
                }

            dbLog.log(Level.FINEST, "Removing line: {0} Line used to be: {1}", new Object[]
                {
                pos, allLines.get(pos)
                });
            allLines.remove(pos);
            dbLog.log(Level.FINEST, "Adding line: {0} to position: {1}", new Object[]
                {
                inLine, pos
                });
            allLines.add(pos, inLine);

            dbLog.log(Level.FINEST, "Closing file");
            accountReadBuffer.close();
            accountReader.close();

            dbLog.log(Level.FINEST, "Opening file: " + inFileName);
            accountWriter = new FileWriter(DB_DIR + inFileName + ".csv");
            accountWriteBuffer = new BufferedWriter(accountWriter);

            for (int i = 0; i < allLines.size(); i++)
                {
                accountWriteBuffer.write(allLines.get(i));
                if (i < 4)
                    {
                    accountWriteBuffer.write(System.lineSeparator());
                    }
                dbLog.log(Level.FINEST, "Writing to memory: {0}at position: {1}", new Object[]
                    {
                    allLines.get(i), i
                    }); //This could be overkill
                }
            dbLog.log(Level.FINEST, "Writing all changes to file.");
            accountWriteBuffer.close();
            accountWriter.close();
            }
        catch (IOException e)
            {
            dbLog.log(Level.SEVERE, "Failed to write to file, this could be very bad, the file was: "
                    + "{0} Tried to overwrite line: {1} With string: {2}", new Object[]
                        {
                        inFileName, pos, inLine
                        });
            LCX.systemLog.log(Level.SEVERE, "Failed to write to file: {0} Tried to "
                    + "overwrite line: {1} With string: {2}", new Object[]
                        {
                        inFileName, pos, inLine
                        });
            }
        }

    private String readFileLine(String inFileName, int pos)
        {
        String line = "ERROR";
        try
            {
            dbLog.log(Level.FINEST, "Opening {0} as readOnly", inFileName);
            accountReader = new FileReader(DB_DIR + inFileName + ".csv");
            accountReadBuffer = new BufferedReader(accountReader);
            for (int i = 0; i < pos + 1; i++) //0) Account number, 1) password, 2) name, 3) latinum
                {
                line = accountReadBuffer.readLine();
                }
            dbLog.log(Level.FINEST, "Line: {0} was read as: {1}", new Object[]
                {
                pos, line
                });
            accountReadBuffer.close();
            accountReader.close();
            }
        catch (IOException e)
            {
            dbLog.log(Level.SEVERE, "Failed to read file line: {0} In file: {1}", new Object[]
                {
                pos, inFileName
                });
            }
        return line;
        }

    private File[] ls(String inDir)
        {
        File folder = new File(DB_DIR);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++)
            {
            if (listOfFiles[i].isFile())
                {
                System.out.println("File " + listOfFiles[i].getName());
                }
            else
                {
                if (listOfFiles[i].isDirectory())
                    {
                    System.out.println("Directory " + listOfFiles[i].getName());
                    }
                }
            }
        return listOfFiles;
        }
    }
