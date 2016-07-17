/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import lcx.DatabaseInterface;
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
    
    private boolean validTransfer = true;
    
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
            CustomFormatter formatter = new CustomFormatter();
            fh.setFormatter(formatter);
            transLog.setLevel(Level.ALL);
            }
        catch (IOException e)
            {
            DatabaseInterface.dbLog.log(Level.SEVERE, "Failed to setup transfer logs! {0}", e.toString());
            }
        
        transLog.finer("Validating transfer details...");
        if(DatabaseInterface.validateAccountNum(inOrigin, "Transfer <init>"))
            {
            originAccount = inOrigin;
            }
        else
            {
            originAccount = "000000";
            validTransfer = false;
            transLog.log(Level.SEVERE, "Origin account number was invalid!");
            }
        
        if(DatabaseInterface.validateAccountNum(inRecipient, "Transfer <init>"))
            {
            recipientAccount = inRecipient;
            }
        else
            {
            recipientAccount = "000000";
            validTransfer = false;
            transLog.log(Level.SEVERE, "Recipient account number was invalid!");
            }
        
        if(DatabaseInterface.validateLatinum(inFee, "Transfer <init>"))
            {
            feeFraction = inFee;
            }
        else
            {
            feeFraction = "0.001";
            validTransfer = false;
            transLog.log(Level.SEVERE, "Fee was invalid: Not a number");
            DatabaseInterface.dbLog.severe("LCX Fee was invalid!");
            lcx.LCX.systemLog.log(Level.SEVERE,"LCX Fee was invalid!");
            }
        
        if(DatabaseInterface.validateLatinum(inAmount, "Transfer <init>"))
            {
            BigDecimal test = new BigDecimal(inAmount);
            if(test.signum() <= 0)
                {
                amount = "0";
                validTransfer = false;
                transLog.log(Level.SEVERE, "Transfer amount was invalid: amount was negative!");
                }
            else
                {
                amount = inAmount;
                }
            }
        else
            {
            amount = "0";
            validTransfer = false;
            transLog.log(Level.SEVERE, "Transfer amount was invalid: Not a number");
            }

        if(validTransfer)
            {
            transLog.log(Level.FINER, "Done validating transfer...");
            }
        else
            {
            transLog.log(Level.SEVERE, "Validation failed!");
            DatabaseInterface.dbLog.warning("Transfer validation failed; check transfer logs for detials.");
            }
        }
    
    public boolean execute()
        {
        boolean didComplete = false;
        /*The reason for the nesting is to remove any ambiguity 
        around the execution order of readLatinum(), doMath() and writeLatinum()
        */
        if(validTransfer)
            {
            if(readLatinum())
                {
                if(doMath())
                    {
                    didComplete = writeLatinum();
                    }
                }
            }
        return didComplete;
        }
    
    private boolean writeLatinum()
        {
        long writeStartNano = System.nanoTime();
        File origin = new File(DatabaseInterface.DB_ACC_DIR + originAccount + ".csv");
        File recipient = new File(DatabaseInterface.DB_ACC_DIR + recipientAccount + ".csv");
        File bank = new File(DatabaseInterface.DB_ACC_DIR + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER + ".csv");
        try 
            {
            List<String> originLines =Files.readAllLines(origin.toPath());
            List<String> recipientLines =Files.readAllLines(recipient.toPath());
            List<String> bankLines =Files.readAllLines(bank.toPath());
            
            transLog.log(Level.INFO, "The following transaction has been requested, and is now being processed: "
                    + "---------------------------------------------------------------------||||||||");
            transLog.log(Level.INFO, "Origin Account: {0}RecipientAccount: {1}Amount: {2}", 
                    new Object[]{originAccount, recipientAccount, amount});
            transLog.log(Level.FINE, "Origin Start Latinum: {0} Origin Final Latinum: {1} Recipient Start Latinum: {2}", 
                    new Object[]{originStartLatinum, originFinalLatinum, recipientStartLatinum});
            transLog.log(Level.FINE, "Fee fraction: {0} Bank Start Latinum: {1} Bank Final Latinum: {2}", 
                    new Object[]{feeFraction, bankStartLatinum, bankFinalLatinum});

            
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
                    for (int i = 0; i < originLines.size(); i++)
                        {
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
                    
                    long writeEndNano = System.nanoTime();
                    float totalMilliSec = (float)((writeEndNano - writeStartNano) / 1000000);
                    transLog.log(Level.INFO, "Transaction has been completed in: {0}ms ||------------------------------------|||||", totalMilliSec);
                    }
                catch(IOException e)
                    {
                    DatabaseInterface.dbLog.log(Level.WARNING, "Failed to write accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
                    e.printStackTrace();
                    //System.out.println("WAS HERE! :/");
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
            //System.out.println("WAS HERE! :/");
            return false;
            }
        //System.out.println("WAS HERE!");
        return true;
        }
    
    private boolean doMath()
        {
        long mathStartNano = System.nanoTime();
        boolean didComplete = true;
        BigDecimal feeMultiplier = new BigDecimal(feeFraction);
        BigDecimal originStartMoney = new BigDecimal(originStartLatinum);
        BigDecimal recipientStartMoney = new BigDecimal(recipientStartLatinum);
        BigDecimal bankStartMoney = new BigDecimal(bankStartLatinum);
        BigDecimal transferMoney = new BigDecimal(amount);
        
        BigDecimal totalFee = transferMoney.multiply(feeMultiplier);
        BigDecimal totalSubtract = transferMoney.add(totalFee);
        
        BigDecimal originFinalMoney = originStartMoney.subtract(totalSubtract);
        
        if(originFinalMoney.signum() == -1)
            {
            didComplete = false;
            transLog.log(Level.WARNING, "The transfer is being canceled: Not enough money to complete!");
            }
        
        BigDecimal recipientFinalMoney = recipientStartMoney.add(transferMoney);
        BigDecimal bankFinalMoney = bankStartMoney.add(totalFee);
        
        originFinalLatinum = originFinalMoney.toPlainString();
        recipientFinalLatinum = recipientFinalMoney.toPlainString();
        bankFinalLatinum = bankFinalMoney.toPlainString();
        
        long mathEndNano = System.nanoTime();
        float totalMicroSec = (float)((mathEndNano - mathStartNano) / 1000);
        transLog.log(Level.FINE, "Completed transaction math in: {0} Microseconds", totalMicroSec);
        
        return didComplete;
        }
    
    private boolean readLatinum()
        {
        long readStartNano = System.nanoTime();
        transLog.fine("Reading Original Latinum...");
        boolean didComplete = false;
        
        try
            {
            FileReader readerOrigin = new FileReader(DatabaseInterface.DB_ACC_DIR + originAccount + ".csv");
            FileReader readerRecipient = new FileReader(DatabaseInterface.DB_ACC_DIR + recipientAccount + ".csv");
            FileReader readerBank = new FileReader(DatabaseInterface.DB_ACC_DIR + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER + ".csv");
            
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
