/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import lcx.LCX;
import shared.UserAccount;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class DatabaseInterface
    {
    public final static String DB_DIR = "database" + File.separator;
    public final static String DB_LOG_DIR = "database" + File.separator + "dblogs" + File.separator;
    public final static String LCX_FEE_ACCOUNT_NUMBER = "816192";
    public final static String EXTERNAL_VALIDATION = "EXTERNAL";
    public final static int ACCOUNT_NUM_POS = 0;
    public final static int PASSWORD_POS = 1;
    public final static int NAME_POS = 2;
    public final static int LATINUM_POS = 3;
    public final static Logger dbLog = Logger.getLogger(LCX.class.getName());
    private static FileHandler fh;
    public static String feeMultiplier = "0.001";
    
    //***************************** Static Methods ****************************************************
    public static void setFee(String inFee)
        {
        if(validateLatinum(inFee,"setFee(String inFee)"))
            {
            BigDecimal validNumTest = new BigDecimal(inFee);
            feeMultiplier = inFee;
            }
        }
    
    public static String getFee()
        {
        return feeMultiplier;
        }
    
    public static boolean validateAccountNum(String inNum,String inSource)
        {
        try
            {
            int num = Integer.valueOf(inNum);
            if(num > 100000 && num < 999999)
                {
                return true;
                }
            else
                {
                if(!(inSource.equals(EXTERNAL_VALIDATION)))
                    {
                    dbLog.log(Level.WARNING, "An invalid account number (too high, or too low) was passed to the database interface function: "
                    + "{0} The given account number was: {1}", new Object[]{inSource, inNum});
                    }
                else
                    {
                    System.err.println("An invalid account number (too high, or too low) was passed to the database interface validate function: " + inNum);
                    }
                return false;
                }
            }
        catch(NumberFormatException nfe)
            {
            if(!(inSource.equals(EXTERNAL_VALIDATION)))
                    {
                    dbLog.log(Level.WARNING, "An invalid account number (not a number) was passed to the database interface function: "
                    + "{0} The given account number was: {1}", new Object[]{inSource, inNum});
                    }
                else
                    {
                    System.err.println("An invalid account number (not a number) was passed to the database interface validate function: " + inNum);
                    }
            return false;
            }
        }
    
    public static boolean validateLatinum(String inNum, String inSource)
        {
        try
            {
            BigDecimal test = new BigDecimal(inNum);
            return true;
            }
        catch(NumberFormatException nfe)
            {
            if(!(inSource.equals(EXTERNAL_VALIDATION)))
                {
                dbLog.log(Level.WARNING, "An invalid account number was passed to the database interface function: "
                    + "{0} The given account number was: {1}", new Object[]{inSource, inNum});
                }
            else
                {
                System.err.println("An invalid latinum number (not a number) was passed to the database interface validate function: " + inNum);
                }
            return false;
            }
        }

    //***************************** Constructors | Standard OO stuff **********************************
    public DatabaseInterface()
        {
        if (!Files.exists(Paths.get(DB_DIR)))
            {
            File dir = new File(DB_DIR);
            dir.mkdir();
            }
        if (!Files.exists(Paths.get(DB_LOG_DIR)))
            {
            File dir = new File(DB_LOG_DIR);
            dir.mkdir();
            }

        if (!(accountNumberExists(LCX_FEE_ACCOUNT_NUMBER)))
            {
            createNewAccount(LCX_FEE_ACCOUNT_NUMBER, "LCX", "terella");
            }

        try
            {
            int logNumber = 0;
            while((new File(DB_LOG_DIR + "databaseLog-" + logNumber + ".txt")).exists())
                {
                logNumber++ ;
                }
            fh = new FileHandler(DB_LOG_DIR + "databaseLog-" + logNumber + ".txt");
            dbLog.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            dbLog.setLevel(Level.ALL);
            }
        catch (IOException e)
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
        String actualPass = readFileLine(inAcc, PASSWORD_POS);
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

    public String createNewAccount(String inName, String inPass)
        {
        String newNum = newAccountNumber();
        createNewAccount(newNum, inName, inPass);
        return newNum;
        }

    public void createNewAccount(String inAccNum, String inName, String inPass)
        {
        FileWriter accountWriter;
        BufferedWriter accountWriteBuffer;
        
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
            }
        catch (IOException e)
            {
            LCX.systemLog.log(Level.SEVERE, "Failed to create new account. Check database log for details.{0}", e.toString());
            dbLog.log(Level.SEVERE, "Failed to create new account file: {0} with name: {1}", new Object[]
                {
                inAccNum, inName
                });
            }
        }
    
    
    
    
    public boolean transfer(String originNum, String recipientNum, String inAmount)
        {
        if (!accountNumberExists(originNum) || !accountNumberExists(recipientNum))
            {
            dbLog.log(Level.WARNING ,"'Trasnfer from' or 'Transfer to' account did not exist. Transfer failed.");
            return false;
            }
        if(originNum.equals(recipientNum))
            {
            dbLog.log(Level.WARNING ,"'Trasnfer from' account was the same as 'Transfer to' account. Transfer failed.");
            return false;
            }
        dbLog.log(Level.FINE, "Server requested transfer from: {0} to: {1} Ammount: {2}", new Object[]
            {
            originNum, recipientNum, inAmount
            });
        
        Transfer transfer = new Transfer(originNum,recipientNum,inAmount,DatabaseInterface.feeMultiplier);
        
        return transfer.execute();
        }

    /**
     * Request a new account number, for a new account, from the database. This
     * method will randomly generate a new account number between 100 000 and
     * 999 999 and then check if an account with that number already exists. If
     * not, it will return the generated number as a string. If it does, then it
     * will generate another ad infinitum.
     *
     * @return a String unique and unused account number.
     */
    private String newAccountNumber()
        {

        String newAccountNum;
        do
            {
            newAccountNum = Integer.toString(ThreadLocalRandom.current().nextInt(100000, 999999));
            }
        while (accountNumberExists(newAccountNum));

        return newAccountNum;

        }

    private boolean accountNumberExists(String an)
        {
        return ((new File(DB_DIR + an + ".csv")).exists());
        }
    //***************************** Direct Interface Methods | High Level stuff **********************************

    /**
     * safely close the database after writing a status flag to the log. At the
     * moment that means saving the database log.
     *
     * @param flag the status flag
     */
    public void close(int flag)
        {
        dbLog.log(Level.INFO, "The server has requested the database to close with the flag: {0}", flag);
        fh.close();
        }

    /**
     * Safely close the database with the status flag 0. At the moment that
     * means saving the database log.
     *
     * @param flag the status flag
     */
    public void close()
        {
        close(0);
        }

    public void writeName(String inAcc, String inName)
        {
        overwriteLine(inAcc, NAME_POS, inName);
        }

    public void writeLatinum(String inAcc, String inLatinum)
        {
        overwriteLine(inAcc, LATINUM_POS, inLatinum);
        }

    public void writeLatinum(String inAcc, BigDecimal inLatinum)
        {
        writeLatinum(inAcc, inLatinum.toPlainString());
        }

    public void writePassword(String inAcc, String inPassword)
        {
        overwriteLine(inAcc, PASSWORD_POS, inPassword);
        }
    
    public String addLatinum(String inAcc, String inAmount)
        {
        String finalLat = "LATINUM WAS NOT SET";
        if(validateAccountNum(inAcc,"addLatinum(String inAcc, String inAmount)"))
            {
            if(validateLatinum(inAmount,"addLatinum(String inAcc, String inAmount)"))
                {
                BigDecimal originalLatinum = new BigDecimal(readFileLine(inAcc,LATINUM_POS));
                BigDecimal amountToAdd = new BigDecimal(inAmount);
                BigDecimal finalAmount = originalLatinum.add(amountToAdd);
                overwriteLine(inAcc,LATINUM_POS,finalAmount.toPlainString());
                finalLat = finalAmount.toPlainString();
                }
            }
        return finalLat;
        }
    
    public String subLatinum(String inAcc, String inAmount)
        {
        String finalLat = "LATINUM WAS NOT SET";
        if(validateAccountNum(inAcc,"subLatinum(String inAcc, String inAmount)"))
            {
            if(validateLatinum(inAmount,"subLatinum(String inAcc, String inAmount)"))
                {
                BigDecimal originalLatinum = new BigDecimal(readFileLine(inAcc,LATINUM_POS));
                BigDecimal amountToSub = new BigDecimal(inAmount);
                BigDecimal finalAmount = originalLatinum.subtract(amountToSub);
                overwriteLine(inAcc,LATINUM_POS,finalAmount.toPlainString());
                finalLat = finalAmount.toPlainString();
                }
            }
        return finalLat;
        }
    
    public String readName(String inAcc)
        {
        if(validateAccountNum(inAcc,"readName(String inAcc)"))
            {
            String name = readFileLine(inAcc, NAME_POS);
            return name;
            }
        else
            {
            return "invalid";
            }
        }

    /**
     * Return a specific account number when given a username. If no account is
     * found with the provided username, it will return "000000".
     *
     * @param inName the username of the account to be seached for.
     * @return a string, the last account number it scans that matches the
     * inName argument, or "000000" if no match is found.
     */
    private String readAcc(String inName)
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
        BigDecimal money = new BigDecimal(readFileLine(inAcc, LATINUM_POS));
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
        FileReader accountReader;
        BufferedReader accountReadBuffer;
        FileWriter accountWriter;
        BufferedWriter accountWriteBuffer;
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

            dbLog.log(Level.FINEST, "Removing line (in memory): {0} Line used to be: {1}", new Object[]
                {
                pos, allLines.get(pos)
                });
            allLines.remove(pos);
            dbLog.log(Level.FINEST, "Adding line (in memory): {0} to position: {1}", new Object[]
                {
                inLine, pos
                });
            allLines.add(pos, inLine);

            dbLog.log(Level.FINEST, "Closing readonly file");
            accountReadBuffer.close();
            accountReader.close();

            dbLog.log(Level.FINEST, "Opening file to write: {0}", inFileName);
            accountWriter = new FileWriter(DB_DIR + inFileName + ".csv");
            accountWriteBuffer = new BufferedWriter(accountWriter);

            for (int i = 0; i < allLines.size(); i++)
                {
                accountWriteBuffer.write(allLines.get(i));
                if (i < 4)
                    {
                    accountWriteBuffer.write(System.lineSeparator());
                    }
                dbLog.log(Level.FINEST, "Reading to memory: {0} at position: {1}", new Object[]
                    {
                    allLines.get(i), i
                    }); //This could be overkill
                }
            dbLog.log(Level.FINEST, "Writing all changes to file. Overwrite Complete.");
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

    private String readFileLine(String inAccountNumber, int pos)
        {
        FileReader accountReader;
        BufferedReader accountReadBuffer;
        
        String line = "ERROR";

        try
            {
            dbLog.log(Level.FINEST, "Opening {0} as readOnly", inAccountNumber);
            accountReader = new FileReader(DB_DIR + inAccountNumber + ".csv");
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
                pos, inAccountNumber
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
