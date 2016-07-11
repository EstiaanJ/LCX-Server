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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class DatabaseInterface
    {
    private FileWriter accountWriter;
    private BufferedWriter accountWriteBuffer;
    private FileReader accountReader;
    private BufferedReader accountReadBuffer;
    
    public DatabaseInterface()
        {
        
        }
    
    public boolean createNewAccount(String inAccNum, String inName, String inPass)
        {
        boolean wasCreated = false;
        try
            {
            accountWriter = new FileWriter(inAccNum + ".csv",true);
            accountWriteBuffer = new BufferedWriter(accountWriter);
            accountWriteBuffer.write(inAccNum);
            accountWriteBuffer.write(System.lineSeparator());
            accountWriteBuffer.write(inPass);
            accountWriteBuffer.write(System.lineSeparator());
            accountWriteBuffer.write(inName);
            accountWriteBuffer.write(System.lineSeparator());
            accountWriteBuffer.write("0");
            accountWriteBuffer.close();
            accountWriter.close();
            wasCreated = true;
            }
        catch(IOException e)
            {
            wasCreated = false;
            }
        return wasCreated;
        }
    
    public void writeFileAppend()
        {
        
        }
    
    public void overwriteFile()
        {
        
        }
    
    private void overwriteLine(String inFileName,int pos,String inLine)
        {
        try
            {
            List<String> allLines = new ArrayList();
            accountReader = new FileReader(inFileName + ".csv");
            accountReadBuffer = new BufferedReader(accountReader);

            String line;
            while((line = accountReadBuffer.readLine()) != null)
                {
                allLines.add(line);
                }
            allLines.remove(pos);
            allLines.add(pos, inLine);
            
            accountReadBuffer.close();
            accountReader.close();
            
            
            accountWriter = new FileWriter(inFileName + ".csv");
            accountWriteBuffer = new BufferedWriter(accountWriter);
            
            for (int i = 0; i < allLines.size(); i++)
                {
                accountWriteBuffer.write(allLines.get(i));
                if(i < 4)
                    {
                    accountWriteBuffer.write(System.lineSeparator());
                    }
                }
            
            accountWriteBuffer.close();
            accountWriter.close();
            }
        catch(IOException e)
            {
            System.err.println("Could not write to file");
            }
        }
    
    
    public String readFileLine(String inFileName,int pos)
        {
        String line = "ERROR";
        try
            {
            accountReader = new FileReader(inFileName + ".csv");
            accountReadBuffer = new BufferedReader(accountReader);
            for(int i = 0; i < pos + 1; i++) //0) Account number, 1) password, 2) name, 3) latinum
                {
                line = accountReadBuffer.readLine();
                }
            accountReadBuffer.close();
            accountReader.close();
            }
        catch(IOException e)
            {
            System.err.println("Could not read file");
            
            }
        return line;
        }
    
    public boolean login(String inAcc,String inPass)
        {
        boolean validLogin = false;
        String actualPass = readFileLine(inAcc,1);
        System.out.println("Provided Password: " + inPass);
        System.out.println("Actual Password: " + actualPass);
        if(inPass.equals(actualPass))
            {
            validLogin = true;
            }
        return validLogin;
        }
    
    public boolean transfer(String inFrom, String inTo, String inAmount)
        {
        boolean didTransfer = false;
        String fromStartLatinum = readFileLine(inFrom,3);
        System.out.println("Transfer From Account: " + inFrom + " had: " + fromStartLatinum);
        
        BigDecimal fromLatinum = new BigDecimal(fromStartLatinum);
        BigDecimal amount = new BigDecimal(inAmount);
        BigDecimal fee = new BigDecimal(amount.toPlainString());
        fee = fee.multiply(new BigDecimal("0.001"));
        System.out.println("Fee is: " + fee.toPlainString());
        fromLatinum = fromLatinum.subtract(amount);
        fromLatinum = fromLatinum.subtract(fee);
        System.out.println("Transfer From Account: " + inFrom + " now has: " + fromLatinum.toPlainString());
        
        String toStartLatinum = readFileLine(inTo,3);
        System.out.println("Transfer To Account: " + inTo+ " had: " + toStartLatinum);
        BigDecimal toLatinum = new BigDecimal(toStartLatinum);
        toLatinum = toLatinum.add(amount);
        System.out.println("Transfer To Account: " + inTo + " now has: " + toLatinum.toPlainString());
        
        String bankStartLatinum = readFileLine("816192",3);
        System.out.println("Bank Account had: " + bankStartLatinum);
        BigDecimal bankLatinum = new BigDecimal(bankStartLatinum);
        bankLatinum = bankLatinum.add(fee);
        System.out.println("Bank Account now has: " + bankLatinum.toPlainString());
        
        System.out.println("Writing 'Transfer From' Account");
        overwriteLine(inFrom,3,fromLatinum.toPlainString());
        System.out.println("Writing 'Transfer To' Account");
        overwriteLine(inTo,3,toLatinum.toPlainString());
        System.out.println("Writing Bank Account");
        overwriteLine("816192",3,bankLatinum.toPlainString());
        
        return true;
        }
    
    public void writeName(String inAcc,String inName)
        {
        overwriteLine(inAcc,2,inName);
        }
    
    public void writeLatinum(String inAcc,String inLatinum)
        {
        overwriteLine(inAcc,3,inLatinum);
        }
    
    public void writeLatinum(String inAcc,BigDecimal inLatinum)
        {
        writeLatinum(inAcc,inLatinum.toPlainString());
        }
    
    public void writePassword(String inAcc,String inPassword)
        {
        overwriteLine(inAcc,1,inPassword);
        }
    
    public String newAccountNumber()
        {
        String newAccountNum = Integer.toString(ThreadLocalRandom.current().nextInt(100000,999999));
        File f = new File(newAccountNum + ".csv");
        if(f.exists())
            {
            newAccountNumber();
            }
        return newAccountNum;
        }
    
    public String readName(String inAcc)
        {
        String name = readFileLine(inAcc,2);
        return name;
        }
    
    public String readAcc(String inName)
        {
        String acc = "123456";
        return acc;
        }
    
    public BigDecimal readLatinum(String inAcc)
        {
        BigDecimal money = new BigDecimal(readFileLine(inAcc,3));
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
    }
