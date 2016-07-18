/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx;

import lcx.Transfer;
import lcx.Database;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Estiaan Janse Van Rensburg <https://github.com/EstiaanJ>
 */
public class TransferTest
    {
    private final static String TEST_NAME_ONE = "jeff";
    private final static String TEST_PASS_ONE = "mysomewhataveragepass";
    
    private final static String TEST_NAME_TWO = "bob";
    private final static String TEST_PASS_TWO = "myothersomewhataveragepass";
    
    private static Database dbif;
    public TransferTest()
        {
        }
    
    @BeforeClass
    public static void setUpClass()
        {
        }
    

    public static void tearDownClass()
        {
        
        }

    
    
    @Before
    public void setUp()
        {
        dbif = new Database();
        }
    
    @After
    public void tearDown()
        {
        File allFiles[] = dbif.ls(Database.DB_ACC_DIR);
        for(int i = 0; i < allFiles.length; i++)
            {
            rm(allFiles[i]);
            }
        dbif.close();
        }

    /**
     * Test of execute method, of class Transfer.
     */
    @Test
    public void testExecuteValid()
        {
        System.out.println("testing execute() with valid values: ");
        
        String originAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String recipAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(originAcc, "2");
        
        String testAmount = "1";
        
        Transfer transfer = new Transfer(originAcc,recipAcc,testAmount,Database.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        
        
        assertEquals(expResult, result);
        
        String originFinalLat = dbif.readLatinumString(originAcc);
        String recipFinalLat = dbif.readLatinumString(recipAcc);
        String bankFinalLat = dbif.readLatinumString(Database.LCX_FEE_ACCOUNT_NUMBER);
        
        assertEquals(originFinalLat,"0.999");
        assertEquals(recipFinalLat,"1");
        assertEquals(bankFinalLat,"0.001");
        }
    
    
    @Test
    public void testExecuteInvalidNegative()
        {
        System.out.println("testing execute() with negative transfer: ");
        
        String originAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String recipAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(originAcc, "2");
        
        String testAmount = "-1";
        
        Transfer transfer = new Transfer(originAcc,recipAcc,testAmount,Database.getFee());
        boolean expResult = false;
        boolean result = transfer.execute();
        
        
        assertEquals(expResult, result);
        
        String originFinalLat = dbif.readLatinumString(originAcc);
        String recipFinalLat = dbif.readLatinumString(recipAcc);
        String bankFinalLat = dbif.readLatinumString(Database.LCX_FEE_ACCOUNT_NUMBER);
        
        assertEquals(originFinalLat,"2");
        assertEquals(recipFinalLat,"0");
        assertEquals(bankFinalLat,"0");
        }
    
    @Test
    public void testExecuteInvalidZero()
        {
        System.out.println("testing execute() with zero transfer: ");
        
        String originAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String recipAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(originAcc, "2");
        
        String testAmount = "0";
        
        Transfer transfer = new Transfer(originAcc,recipAcc,testAmount,Database.getFee());
        boolean expResult = false;
        boolean result = transfer.execute();
        
        assertEquals(expResult, result);
        
        String originFinalLat = dbif.readLatinumString(originAcc);
        String recipFinalLat = dbif.readLatinumString(recipAcc);
        String bankFinalLat = dbif.readLatinumString(Database.LCX_FEE_ACCOUNT_NUMBER);
        
        assertEquals(originFinalLat,"2");
        assertEquals(recipFinalLat,"0");
        assertEquals(bankFinalLat,"0");
        }
    
    @Test
    public void testExecuteInvalidNotEnoughFunds()
        {
        System.out.println("testing execute() with not enough funds: ");
        
        String originAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String recipAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(originAcc, "2");
        
        String testAmount = "5";
        
        Transfer transfer = new Transfer(originAcc,recipAcc,testAmount,Database.getFee());
        boolean expResult = false;
        boolean result = transfer.execute();
        
        assertEquals(expResult, result);
        
        String originFinalLat = dbif.readLatinumString(originAcc);
        String recipFinalLat = dbif.readLatinumString(recipAcc);
        String bankFinalLat = dbif.readLatinumString(Database.LCX_FEE_ACCOUNT_NUMBER);
        
        assertEquals(originFinalLat,"2");
        assertEquals(recipFinalLat,"0");
        assertEquals(bankFinalLat,"0");
        }
    
    
    @Test
    public void testExecuteInvalidNotEnoughFundsTwo()
        {
        System.out.println("testing execute() with not enough funds to pay fee (but enough for transfer): ");
        
        String originAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String recipAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(originAcc, "2");
        
        String testAmount = "2";
        
        Transfer transfer = new Transfer(originAcc,recipAcc,testAmount,Database.getFee());
        boolean expResult = false;
        boolean result = transfer.execute();
        
        assertEquals(expResult, result);
        String originFinalLat = dbif.readLatinumString(originAcc);
        String recipFinalLat = dbif.readLatinumString(recipAcc);
        String bankFinalLat = dbif.readLatinumString(Database.LCX_FEE_ACCOUNT_NUMBER);
        
        assertEquals(originFinalLat,"2");
        assertEquals(recipFinalLat,"0");
        assertEquals(bankFinalLat,"0");
        }
    
    
    private static void rm(File inFile)
        {
        if(inFile.isDirectory())
            {
            return;
            }
        
        try
            {
            Files.delete(inFile.toPath());
            }
        catch (NoSuchFileException x)
            {
            System.err.format("%s: no such" + " file or directory%n", inFile.toPath());
            }
        catch (DirectoryNotEmptyException x)
            {
            System.err.format("%s not empty%n", inFile.toPath());
            }
        catch (IOException x)
            {
            // File permission problems are caught here.
            System.err.println(x);
            }
        }
    }
