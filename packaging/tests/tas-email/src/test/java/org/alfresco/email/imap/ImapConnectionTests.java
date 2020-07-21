package org.alfresco.email.imap;

import org.alfresco.email.EmailTest;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Connection to Imap
 * 
 * @author Cristina Axinte
 *
 */
public class ImapConnectionTests extends EmailTest
{
    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
    }
   
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify user can connect successfully to IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void correctUserConnectsToIMAPSuccessfully() throws Exception
    {
        imapProtocol.authenticateUser(testUser).then().assertThat().userIsConnected();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify user fails to connect to IMAP on different port")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY }, expectedExceptions = TestConfigurationException.class)
    public void userFailsConnectToIMAPOnDifferentPort() throws Exception
    {
        imapProtocol.authenticateUser(testUser, 43).then().assertThat().userIsNotConnected();
    } 

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify user can disconnect successfully from IMAP Server")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void userDisconnectsFromImapServerSuccessfully() throws Exception
    {
        imapProtocol.authenticateUser(testUser)
            .disconnect().then().assertThat().userIsNotConnected();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user fails to connect to IMAP on different host")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = TestConfigurationException.class)
    public void userFailsConnectToIMAPOnDifferentHost() throws Exception
    {
        imapProtocol.authenticateUser(testUser, "172.29.101.1256").then().assertThat().userIsNotConnected();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify existing user with wrong password fails to connect to IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = TestConfigurationException.class,
    expectedExceptionsMessageRegExp =".*You missed some configuration settings in your tests: User failed to connect to IMAP server LOGIN failed. Invalid login/password$"  )
    public void userFailsConnectToIMAPWithWrongPassword() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testUser.setPassword("invalid");        
        imapProtocol.authenticateUser(testUser);   
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify a non existing user fails to connect to IMAP.")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = TestConfigurationException.class,
            expectedExceptionsMessageRegExp =".*You missed some configuration settings in your tests: User failed to connect to IMAP server LOGIN failed. Invalid login/password")
    public void nonExistentUserFailsConnectToIMAP() throws Exception
    {
        imapProtocol.authenticateUser(new UserModel("nonExistingUser", "pass"));
    }
}
