/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class Transfer
    {
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
        if(!readLatinum())
            {
            return false;
            }
        if(!doMath())
            {
            return false;
            }
        return writeLatinum();
        }
    
    private boolean writeLatinum()
        {
        File origin = new File(DatabaseInterface.DB_DIR + originAccount + ".csv");
        File recipient = new File(DatabaseInterface.DB_DIR + recipientAccount + ".csv");
        File bank = new File(DatabaseInterface.DB_DIR + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER + ".csv");
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
                java.nio.channels.FileLock lockOrigin = outOrigin.getChannel().lock();
                java.nio.channels.FileLock lockRecipient = outRecipient.getChannel().lock();
                java.nio.channels.FileLock lockBank = outBank.getChannel().lock();
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
                    }
                catch(IOException e)
                    {
                    DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
                    return false;
                    }
                finally
                    {
                    lockOrigin.release();
                    lockRecipient.release();
                    lockBank.release();
                    }
                }
            catch(IOException e)
                    {
                    DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
                    return false;
                    }
            finally
                {
                outOrigin.close();
                outRecipient.close();
                outBank.close();
                }
            }
        catch(IOException e)
            {
            DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
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
        return true;
        }
    
    private boolean readLatinum()
        {
        File origin = new File(DatabaseInterface.DB_DIR + originAccount + ".csv");
        File recipient = new File(DatabaseInterface.DB_DIR + recipientAccount + ".csv");
        File bank = new File(DatabaseInterface.DB_DIR + DatabaseInterface.LCX_FEE_ACCOUNT_NUMBER + ".csv");
        try 
            {
            FileInputStream inOrigin = new FileInputStream(origin);
            FileInputStream inRecipient = new FileInputStream(recipient);
            FileInputStream inBank = new FileInputStream(bank);
            try
                {
                java.nio.channels.FileLock lockOrigin = inOrigin.getChannel().lock();
                java.nio.channels.FileLock lockRecipient = inRecipient.getChannel().lock();
                java.nio.channels.FileLock lockBank = inBank.getChannel().lock();
                try
                    {
                    Reader readerOrigin = new InputStreamReader(inOrigin);
                    Reader readerRecipient = new InputStreamReader(inRecipient);
                    Reader readerBank = new InputStreamReader(inBank);
                    
                    BufferedReader bufferedOrigin = new BufferedReader(readerOrigin);
                    BufferedReader bufferedRecipient = new BufferedReader(readerRecipient);
                    BufferedReader bufferedBank = new BufferedReader(readerBank);
                    for(int i = 0; i < DatabaseInterface.LATINUM_POS + 1; i ++)
                        {
                        originStartLatinum = bufferedOrigin.readLine();
                        recipientStartLatinum = bufferedRecipient.readLine();
                        bankStartLatinum = bufferedBank.readLine();
                        }
                    }
                catch(IOException e)
                    {
                    DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
                    return false;
                    }
                finally
                    {
                    lockOrigin.release();
                    lockRecipient.release();
                    lockBank.release();
                    }
                }
            catch(IOException e)
                    {
                    DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
                    return false;
                    }
            finally
                {
                inOrigin.close();
                inRecipient.close();
                inBank.close();
                }
            }
        catch(IOException e)
            {
            DatabaseInterface.dbLog.log(Level.WARNING, "Failed to read accounts during transfer, "
                            + "the origin account was: {0} the recipient account was: {1}", 
                            new Object[]{originAccount, recipientAccount});
            return false;
            }
        return true;
        }
    }
