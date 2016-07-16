/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx.data;

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
    
    private static DatabaseInterface dbif;
    public TransferTest()
        {
        }
    
    @BeforeClass
    public static void setUpClass()
        {
        }
    

    public static void tearDownClass()
        {
        DatabaseInterface cleanupDB = new DatabaseInterface();
        File allFiles[] = cleanupDB.ls(DatabaseInterface.DB_ACC_DIR);
        File alldbLogs[] = cleanupDB.ls(DatabaseInterface.DB_LOG_DIR);
        for(int i = 0; i < allFiles.length; i++)
            {
            rm(allFiles[i]);
            }
        dbif.close();
        }

    
    
    @Before
    public void setUp()
        {
        dbif = new DatabaseInterface();
        }
    
    @After
    public void tearDown()
        {
        dbif.close();
        }

    /**
     * Test of execute method, of class Transfer.
     */
    @Test
    public void testExecuteValid()
        {
        System.out.println("testing execute() with valid values: ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "1";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
        }
    
    public void testExecuteValidLarge()
        {
        System.out.println("testing execute() with very large valid values: ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "100000000000000000000000000";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
        }
    
    public void testExecuteSmall()
        {
        System.out.println("testing execute() with very small valid values: ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "0.000000000000001";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
        }
    
    @Test
    public void testExecuteInvalidNegative()
        {
        System.out.println("testing execute() with negative transfer: ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "-1";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
        }
    
    public void testExecuteInvalidZero()
        {
        System.out.println("testing execute() with zero transfer: ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "0";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
        }
    
    public void testExecuteInvalidNotEnoughFunds()
        {
        System.out.println("testing execute() with not enough funds: ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "5";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
        }
    
    public void testExecuteInvalidNotEnoughFunds2()
        {
        System.out.println("testing execute() with not enough funds to pay fee (but enough for transfer): ");
        
        String firstAcc = dbif.createNewAccount(TEST_NAME_ONE, TEST_PASS_ONE);
        String secondAcc = dbif.createNewAccount(TEST_NAME_TWO, TEST_PASS_TWO);
        
        dbif.addLatinum(firstAcc, "2");
        
        String testAmount = "2";
        
        Transfer transfer = new Transfer(firstAcc,secondAcc,testAmount,DatabaseInterface.getFee());
        boolean expResult = true;
        boolean result = transfer.execute();
        assertEquals(expResult, result);
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
