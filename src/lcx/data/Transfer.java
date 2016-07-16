/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import lcx.LCX;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class Transfer
    {
    public final static Logger transLog = Logger.getLogger(LCX.class.getName());
    public final static String TRANSFER_LOG_DIR = "database" + File.separator + "transferLogs" + File.separator;
    private static FileHandler fh;
    
    private final String amount;
    private final String feeFraction;
    private final String originAccount;
    private final String recipientAccount;
    
    private String originStartLatinum;
    private String originFinalLatinum;
    private String recipientStartLatinum;
    private String recipientFinalLatinum;
    private String bankStartLatinum;
    private String bankFinalLatinum;
    
    public Transfer(String inOrigin,String inRecipient,String inAmount,String inFee)
        {
        
        if (!Files.exists(Paths.get(TRANSFER_LOG_DIR)))
            {
            File dir = new File(TRANSFER_LOG_DIR);
            dir.mkdir();
            }
        
        try
            {
            int logNumber = 0;
            while ((new File(TRANSFER_LOG_DIR + "transferLog-" + logNumber + ".txt")).exists())
                {
                logNumber++;
                }
            fh = new FileHandler(TRANSFER_LOG_DIR + "transferLog-" + logNumber + ".txt");
            transLog.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            transLog.setLevel(Level.ALL);
            }
        catch (IOException e)
            {
            DatabaseInterface.dbLog.log(Level.SEVERE, "Failed to setup transfer logs! {0}", e.toString());
            }
        
        if(DatabaseInterface.validateAccountNum(inOrigin, "Transfer <init>"))
            {
            originAccount = inOrigin;
            }
        else
            {
            originAccount = "000000";
            }
        if(DatabaseInterface.validateAccountNum(inRecipient, "Transfer <init>"))
            {
            recipientAccount = inRecipient;
            }
        else
            {
            recipientAccount = "000000";
            }
        if(DatabaseInterface.validateLatinum(inFee, "Transfer <init>"))
            {
            feeFraction = inFee;
            }
        else
            {
            feeFraction = "0.001";
            }
        if(DatabaseInterface.validateLatinum(inAmount, "Transfer <init>"))
            {
            amount = inAmount;
            }
        else
            {
            amount = "0";
            }
        }
    
    public boolean execute()
        {
        boolean didComplete = false;
        if(readLatinum())
            {
            System.out.println(bankStartLatinum);
            if(doMath())
                {
                didComplete = writeLatinum();
                }
            }
        return false;//didComplete;
        }
    
    private boolean writeLatinum2()
        {
        return true;
        }
    
    private boolean writeLatinum()
        {
        
        File origin = new File(DatabaseInterface.DB_ACC_DIR + originAccount + ".csv");
        File recipient = new File(DatabaseInterface.DB_ACC_DIR + recipientAccount + ".csv");
        File bank = new File(DatabaseInterface.DB_ACC_DIR + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER + ".csv");
        try 
            {
            List<String> originLines =Files.readAllLines(origin.toPath());
            List<String> recipientLines =Files.readAllLines(recipient.toPath());
            List<String> bankLines =Files.readAllLines(bank.toPath());
            
            originLines.remove(DatabaseInterface.LATINUM_POS);
            recipientLines.remove(DatabaseInterface.LATINUM_POS);
            bankLines.remove(DatabaseInterface.LATINUM_POS);
            
            originLines.add(DatabaseInterface.LATINUM_POS,originFinalLatinum);
            recipientLines.add(DatabaseInterface.LATINUM_POS,recipientFinalLatinum);
            bankLines.add(DatabaseInterface.LATINUM_POS,bankFinalLatinum);
            
            FileOutputStream outOrigin = new FileOutputStream(origin);
            FileOutputStream outRecipient = new FileOutputStream(recipient);
            FileOutputStream outBank = new FileOutputStream(bank);
            try
                {
                try
                    {
                    Writer writerOrigin = new OutputStreamWriter(outOrigin);
                    Writer writerRecipient = new OutputStreamWriter(outRecipient);
                    Writer writerBank = new OutputStreamWriter(outBank);
                    
                    BufferedWriter bufferedOrigin = new BufferedWriter(writerOrigin);
                    BufferedWriter bufferedRecipient = new BufferedWriter(writerRecipient);
                    BufferedWriter bufferedBank = new BufferedWriter(writerBank);
                    System.out.println(originFinalLatinum);
                    for (int i = 0; i < originLines.size(); i++)
                        {
                        System.out.println(i);
                        bufferedOrigin.write(originLines.get(i));
                        bufferedRecipient.write(recipientLines.get(i));
                        bufferedBank.write(bankLines.get(i));
                        if (i < 4)
                            {
                            bufferedOrigin.write(System.lineSeparator());
                            bufferedRecipient.write(System.lineSeparator());
                            bufferedBank.write(System.lineSeparator());
                            }
                        }
                    bufferedOrigin.close();
                    bufferedRecipient.close();
                    bufferedBank.close();
                    
                    writerOrigin.close();
                    writerRecipient.close();
                    writerBank.close();
                    }
                catch(IOException e)
                    {
                    DatabaseInterface.dbLog.log(Level.WARNING, "Failed to write accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
                    e.printStackTrace();
                    return false;
                    }

                }
            finally
                {
                outOrigin.close();
                outRecipient.close();
                outBank.close();
                }
            outOrigin.close();
            outRecipient.close();
            outBank.close();
            }
        catch(IOException e)
            {
            DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
            e.printStackTrace();
            return false;
            }
        return true;
        }
    
    private boolean doMath()
        {
        BigDecimal feeMultiplier = new BigDecimal(feeFraction);
        BigDecimal originStartMoney = new BigDecimal(originStartLatinum);
        BigDecimal recipientStartMoney = new BigDecimal(recipientStartLatinum);
        BigDecimal bankStartMoney = new BigDecimal(bankStartLatinum);
        BigDecimal transferMoney = new BigDecimal(amount);
        
        BigDecimal totalFee = transferMoney.multiply(feeMultiplier);
        BigDecimal totalSubtract = transferMoney.add(totalFee);
        
        BigDecimal originFinalMoney = originStartMoney.subtract(totalSubtract);
        BigDecimal recipientFinalMoney = recipientStartMoney.add(transferMoney);
        BigDecimal bankFinalMoney = bankStartMoney.add(totalFee);
        
        originFinalLatinum = originFinalMoney.toPlainString();
        recipientFinalLatinum = recipientFinalMoney.toPlainString();
        bankFinalLatinum = bankFinalMoney.toPlainString();
        return true;
        }
    
    private boolean readLatinum()
        {
        long readStartNano = System.nanoTime();
        transLog.fine("Reading Original Latinum...");
        boolean didComplete = false;
        
        try
            {
            FileReader readerOrigin = new FileReader(DatabaseInterface.DB_ACC_DIR + originAccount + ".csv");
            transLog.log(Level.FINEST, "Opened origin account: ");
            FileReader readerRecipient = new FileReader(DatabaseInterface.DB_ACC_DIR + recipientAccount + ".csv");
            transLog.log(Level.FINEST, "Opened recipient account: ");
            FileReader readerBank = new FileReader(DatabaseInterface.DB_ACC_DIR + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER + ".csv");
            transLog.log(Level.FINEST, "Opened bank account: ");
            
            BufferedReader bufferedOrigin = new BufferedReader(readerOrigin);
            BufferedReader bufferedRecipient = new BufferedReader(readerRecipient);
            BufferedReader bufferedBank = new BufferedReader(readerBank);
            
            for(int i = 0; i < DatabaseInterface.LATINUM_POS + 1; i ++)
                {
                originStartLatinum = bufferedOrigin.readLine();
                recipientStartLatinum = bufferedRecipient.readLine();
                bankStartLatinum = bufferedBank.readLine();
                }
            transLog.log(Level.FINE, "Read origin account, latinum is: {0}", originStartLatinum);
            transLog.log(Level.FINE, "Read recipient account, latinum is: {0}", recipientStartLatinum);
            transLog.log(Level.FINE, "Read bank account, latinum is: {0}", bankStartLatinum);
            System.out.println(bankStartLatinum);
            
            readerOrigin.close();
            readerRecipient.close();
            readerBank.close();
            
            didComplete = true;
            long readEndNano = System.nanoTime();
            float totalMilliSec = (float)((readEndNano - readStartNano) / 1000000);
            transLog.log(Level.FINE, "Completed read in: {0}ms", totalMilliSec);
            }
        catch(FileNotFoundException fnfe)
            {
            transLog.log(Level.SEVERE,"File not found during readLatinum(), file was one of these: {0}" 
                    + ".csv" + " | {1}" + ".csv" + " | " + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER 
                    + ".csv", new Object[]{originAccount, recipientAccount});
            fnfe.printStackTrace();
            }
        catch(IOException e)
            {
            transLog.log(Level.SEVERE,"Failed to read from a file during readLatinum, the file was one of these: {0}"
            + ".csv" + " | {1}" + ".csv" + " | " + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER 
                    + ".csv", new Object[]{originAccount, recipientAccount});
            e.printStackTrace();
            }
        return didComplete;
        }
    }
