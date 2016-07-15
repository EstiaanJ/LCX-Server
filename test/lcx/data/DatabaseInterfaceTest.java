/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lcx.data;

import java.io.File;
import java.math.BigDecimal;
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
public class DatabaseInterfaceTest
    {
    
    public DatabaseInterfaceTest()
        {
        }
    
    @BeforeClass
    public static void setUpClass()
        {
        }
    
    @AfterClass
    public static void tearDownClass()
        {
        }
    
    @Before
    public void setUp()
        {
        }
    
    @After
    public void tearDown()
        {
        }

    /**
     * Test of setFee method, of class DatabaseInterface.
     */
    @Test
    public void testSetFee()
        {
        System.out.println("setFee");
        String inFee = "";
        DatabaseInterface.setFee(inFee);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of getFee method, of class DatabaseInterface.
     */
    @Test
    public void testGetFee()
        {
        System.out.println("getFee");
        String expResult = "";
        String result = DatabaseInterface.getFee();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of validateAccountNum method, of class DatabaseInterface.
     */
    @Test
    public void testValidateAccountNum()
        {
        System.out.println("validateAccountNum");
        String inNum = "";
        String inSource = "";
        boolean expResult = false;
        boolean result = DatabaseInterface.validateAccountNum(inNum, inSource);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of validateLatinum method, of class DatabaseInterface.
     */
    @Test
    public void testValidateLatinum()
        {
        System.out.println("validateLatinum");
        String inNum = "";
        String inSource = "";
        boolean expResult = false;
        boolean result = DatabaseInterface.validateLatinum(inNum, inSource);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of login method, of class DatabaseInterface.
     */
    @Test
    public void testLogin()
        {
        System.out.println("login");
        String inAcc = "";
        String inPass = "";
        DatabaseInterface instance = new DatabaseInterface();
        boolean expResult = false;
        boolean result = instance.login(inAcc, inPass);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of createNewAccount method, of class DatabaseInterface.
     */
    @Test
    public void testCreateNewAccount_String_String()
        {
        System.out.println("createNewAccount");
        String inName = "";
        String inPass = "";
        DatabaseInterface instance = new DatabaseInterface();
        String expResult = "";
        String result = instance.createNewAccount(inName, inPass);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of createNewAccount method, of class DatabaseInterface.
     */
    @Test
    public void testCreateNewAccount_3args()
        {
        System.out.println("createNewAccount");
        String inAccNum = "";
        String inName = "";
        String inPass = "";
        DatabaseInterface instance = new DatabaseInterface();
        instance.createNewAccount(inAccNum, inName, inPass);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of transfer method, of class DatabaseInterface.
     */
    @Test
    public void testTransfer()
        {
        System.out.println("transfer");
        String originNum = "";
        String recipientNum = "";
        String inAmount = "";
        DatabaseInterface instance = new DatabaseInterface();
        boolean expResult = false;
        boolean result = instance.transfer(originNum, recipientNum, inAmount);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of close method, of class DatabaseInterface.
     */
    @Test
    public void testClose_int()
        {
        System.out.println("close");
        int flag = 0;
        DatabaseInterface instance = new DatabaseInterface();
        instance.close(flag);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of close method, of class DatabaseInterface.
     */
    @Test
    public void testClose_0args()
        {
        System.out.println("close");
        DatabaseInterface instance = new DatabaseInterface();
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of writeName method, of class DatabaseInterface.
     */
    @Test
    public void testWriteName()
        {
        System.out.println("writeName");
        String inAcc = "";
        String inName = "";
        DatabaseInterface instance = new DatabaseInterface();
        boolean expResult = false;
        boolean result = instance.writeName(inAcc, inName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of writeLatinum method, of class DatabaseInterface.
     */
    @Test
    public void testWriteLatinum_String_String()
        {
        System.out.println("writeLatinum");
        String inAcc = "";
        String inLatinum = "";
        DatabaseInterface instance = new DatabaseInterface();
        boolean expResult = false;
        boolean result = instance.writeLatinum(inAcc, inLatinum);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of writeLatinum method, of class DatabaseInterface.
     */
    @Test
    public void testWriteLatinum_String_BigDecimal()
        {
        System.out.println("writeLatinum");
        String inAcc = "";
        BigDecimal inLatinum = null;
        DatabaseInterface instance = new DatabaseInterface();
        boolean expResult = false;
        boolean result = instance.writeLatinum(inAcc, inLatinum);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of writePassword method, of class DatabaseInterface.
     */
    @Test
    public void testWritePassword()
        {
        System.out.println("writePassword");
        String inAcc = "";
        String inPassword = "";
        DatabaseInterface instance = new DatabaseInterface();
        boolean expResult = false;
        boolean result = instance.writePassword(inAcc, inPassword);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of addLatinum method, of class DatabaseInterface.
     */
    @Test
    public void testAddLatinum()
        {
        System.out.println("addLatinum");
        String inAcc = "";
        String inAmount = "";
        DatabaseInterface instance = new DatabaseInterface();
        String expResult = "";
        String result = instance.addLatinum(inAcc, inAmount);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of subLatinum method, of class DatabaseInterface.
     */
    @Test
    public void testSubLatinum()
        {
        System.out.println("subLatinum");
        String inAcc = "";
        String inAmount = "";
        DatabaseInterface instance = new DatabaseInterface();
        String expResult = "";
        String result = instance.subLatinum(inAcc, inAmount);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of readName method, of class DatabaseInterface.
     */
    @Test
    public void testReadName()
        {
        System.out.println("readName");
        String inAcc = "";
        DatabaseInterface instance = new DatabaseInterface();
        String expResult = "";
        String result = instance.readName(inAcc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of readLatinum method, of class DatabaseInterface.
     */
    @Test
    public void testReadLatinum()
        {
        System.out.println("readLatinum");
        String inAcc = "";
        DatabaseInterface instance = new DatabaseInterface();
        BigDecimal expResult = null;
        BigDecimal result = instance.readLatinum(inAcc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of readLatinumString method, of class DatabaseInterface.
     */
    @Test
    public void testReadLatinumString()
        {
        System.out.println("readLatinumString");
        String inAcc = "";
        DatabaseInterface instance = new DatabaseInterface();
        String expResult = "";
        String result = instance.readLatinumString(inAcc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of readTransactionLog method, of class DatabaseInterface.
     */
    @Test
    public void testReadTransactionLog()
        {
        System.out.println("readTransactionLog");
        String inAcc = "";
        DatabaseInterface instance = new DatabaseInterface();
        String expResult = "";
        String result = instance.readTransactionLog(inAcc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }

    /**
     * Test of ls method, of class DatabaseInterface.
     */
    @Test
    public void testLs()
        {
        System.out.println("ls");
        String inDir = "";
        DatabaseInterface instance = new DatabaseInterface();
        File[] expResult = null;
        File[] result = instance.ls(inDir);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }
    
    }
