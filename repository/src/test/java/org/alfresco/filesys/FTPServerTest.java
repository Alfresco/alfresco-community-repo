/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.filesys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.server.config.ServerConfigurationAccessor;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.usage.ContentUsageImpl;
import org.alfresco.repo.usage.UserUsageTrackingComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;


/**
 * End to end JUNIT test of the FTP server
 * Uses the commons-net ftp client library to connect to the
 * Alfresco FTP server.
 */
@Category(BaseSpringTestsCategory.class)
public class FTPServerTest

{
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPServerTest.class);

    private ApplicationContext applicationContext;

    private static final String USER_ADMIN = "admin";
    private static final String PASSWORD_ADMIN = "admin";
    private static final String USER_ONE = "FTPServerTestOne";
    private static final String USER_TWO = "FTPServerTestTwo";
    private static final String USER_THREE = "FTPServerTestThree";
    private static final String PASSWORD_ONE = "Password01";
    private static final String PASSWORD_TWO = "Password02";
    private static final String PASSWORD_THREE = "Password03";
    private static final String HOSTNAME = "localhost";

    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private Repository repositoryHelper;
    private PermissionService permissionService;
    private FTPConfigSection ftpConfigSection;

    @Before
    public void setUp() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();

        NodeService nodeService = (NodeService) applicationContext.getBean("nodeService");
        personService = (PersonService)applicationContext.getBean("personService");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) applicationContext.getBean(
            "authenticationComponent");
        TransactionService transactionService = (TransactionService) applicationContext.getBean("transactionService");
        repositoryHelper = (Repository)applicationContext.getBean("repositoryHelper");
        permissionService = (PermissionService)applicationContext.getBean("permissionService");
        ServerConfigurationAccessor fileServerConfiguration = (ServerConfigurationAccessor)applicationContext.getBean("fileServerConfiguration");
        ftpConfigSection = (FTPConfigSection) fileServerConfiguration.getConfigSection( FTPConfigSection.SectionName);

        assertNotNull("nodeService is null", nodeService);
        assertNotNull("repositoryHelper is null", repositoryHelper);
        assertNotNull("personService is null", personService);
        assertNotNull("authenticationService is null", authenticationService);
        assertNotNull("authenticationComponent is null", authenticationComponent);

        authenticationComponent.setSystemUserAsCurrentUser();

        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        RetryingTransactionCallback<Void> createUsersCB = () -> {
            createUser(USER_ONE, PASSWORD_ONE, -1);
            createUser(USER_TWO, PASSWORD_TWO, -1);
            createUser(USER_THREE, PASSWORD_THREE, 30);
            return null;
        };
        tran.doInTransaction(createUsersCB);

        RetryingTransactionCallback<Void> createTestDirCB = () -> {
            {
                NodeRef userOneHome = repositoryHelper.getUserHome(personService.getPerson(USER_ONE));
                permissionService.setPermission(userOneHome, USER_TWO, PermissionService.CONTRIBUTOR, true);
                permissionService.setPermission(userOneHome, USER_TWO, PermissionService.WRITE, true);
            }
            return null;
        };
        tran.doInTransaction(createTestDirCB, false, true);
    }

    /**
     * Simple test that connects to the inbuilt ftp server and logs on
     *
     * @throws Exception may throw Exception
     */
    @Test
    public void testFTPConnect() throws Exception
    {
        LOGGER.debug("Start testFTPConnect");

        FTPClient ftp = connectClient();
        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
            assertTrue("admin login should be successful", isLoggedIn);
        }
        finally
        {
            ftp.disconnect();
        }
    }

    /**
     * Simple negative test that connects to the inbuilt ftp server and attempts to
     * log on with the wrong password.
     *
     * @throws Exception may throw Exception
     */
    @Test
    public void testFTPConnectNegative() throws Exception
    {
        LOGGER.debug("Start testFTPConnectNegative");

        FTPClient ftp = connectClient();

        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, "garbage");
            assertFalse("admin login should not be successful", isLoggedIn);

            // now attempt to list the files and check that the command does not 
            // succeed
            FTPFile[] files = ftp.listFiles();

            assertNotNull("files should not be null", files);
            assertEquals("there should be no files",0, files.length);
            reply = ftp.getReplyCode();

            assertTrue("FTP server should respond negatively", FTPReply.isNegativePermanent(reply));

        }
        finally
        {
            ftp.disconnect();
        }
    }

    /**
     * Test CWD for FTP server
     *
     * @throws Exception may throw Exception
     */
    @Test
    public void testCWD() throws Exception
    {
        LOGGER.debug("Start testCWD");

        FTPClient ftp = connectClient();

        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
            assertTrue("admin login should be successful", isLoggedIn);

            FTPFile[] files = ftp.listFiles();
            reply = ftp.getReplyCode();
            assertTrue("FTP server refused connection", FTPReply.isPositiveCompletion(reply));

            assertEquals("there should be only 1 file",1, files.length);

            boolean isFileFound = false;
            for(FTPFile file : files)
            {
                final String fileName = file.getName();
                LOGGER.debug("File name = {}", fileName);
                assertTrue("file is not a directory", file.isDirectory());

                if(fileName.equalsIgnoreCase("Alfresco"))
                {
                    isFileFound = true;
                }
            }
            assertTrue("file should be found", isFileFound);

            // Change to Alfresco Dir that we know exists
            reply = ftp.cwd("/Alfresco");
            assertTrue("unable to change to /Alfresco", FTPReply.isPositiveCompletion(reply));

            // relative path with space char
            reply = ftp.cwd("Data Dictionary");
            assertTrue("unable to change to Data Dictionary", FTPReply.isPositiveCompletion(reply));

            // non existent absolute
            reply = ftp.cwd("/Garbage");
            assertTrue("able to change to nonexistent /Garbage", FTPReply.isNegativePermanent(reply));

            reply = ftp.cwd("/Alfresco/User Homes");
            assertTrue("unable to change to /Alfresco/User Homes",FTPReply.isPositiveCompletion(reply));

            // Wild card
            reply = ftp.cwd("/Alfresco/User*Homes");
            assertTrue("unable to change to /Alfresco User*Homes/", FTPReply.isPositiveCompletion(reply));

//            // Single char pattern match
//            reply = ftp.cwd("/Alfre?co");
//            assertTrue("Unable to match single char /Alfre?co", FTPReply.isPositiveCompletion(reply));

            // two level folder
            reply = ftp.cwd("/Alfresco/Data Dictionary");
            assertTrue("unable to change to /Alfresco/Data Dictionary", FTPReply.isPositiveCompletion(reply));

            // go up one
            reply = ftp.cwd("..");
            assertTrue("unable to change to ..", FTPReply.isPositiveCompletion(reply));

            reply = ftp.pwd();
            ftp.getStatus();

            assertTrue("unable to get status", FTPReply.isPositiveCompletion(reply));

            // check we are at the correct point in the tree
            reply = ftp.cwd("Data Dictionary");
            assertTrue("test should end in being in the tree of Data Dictionary", FTPReply.isPositiveCompletion(reply));


        }
        finally
        {
            ftp.disconnect();
        }

    }

    /**
     * Test CRUD for FTP server
     * @throws Exception may throw Exception
     */
    @Test
    public void testCRUD() throws Exception
    {
        final String path1 = "FTPServerTest";
        final String path2 = "Second part";

        LOGGER.debug("Start testFTPCRUD");

        FTPClient ftp = connectClient();

        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
            assertTrue("admin login should be successful", isLoggedIn);

            reply = ftp.cwd("/Alfresco/User Homes");
            assertTrue("unable to change to /Alfresco/User Homes", FTPReply.isPositiveCompletion(reply));

            // Delete the root directory in case it was left over from a previous test run
            try
            {
                ftp.removeDirectory(path1);
            }
            catch (IOException e)
            {
                // ignore this error
            }

            // make root directory
            ftp.makeDirectory(path1);
            ftp.cwd(path1);

            // make subdirectory in new directory
            ftp.makeDirectory(path2);
            ftp.cwd(path2);

            // List the files in the new directory
            FTPFile[] files = ftp.listFiles();
            assertEquals("there should be no files", 0, files.length);

            // Create a file
            String file1Content1="test file 1 content";
            String file1Name = "testFile1.txt";
            ftp.appendFile(file1Name , new ByteArrayInputStream(file1Content1.getBytes(StandardCharsets.UTF_8)));

            // Get the new file
            FTPFile[] files2 = ftp.listFiles();
            assertEquals("there should be only 1 file", 1, files2.length);

            InputStream is = ftp.retrieveFileStream(file1Name);

            String content = inputStreamToString(is);
            assertEquals("Content is not as expected", content, file1Content1);
            ftp.completePendingCommand();

            // Update the file contents
            String file1Content2 = "That's how it is says Pooh!";
            ftp.storeFile(file1Name , new ByteArrayInputStream(file1Content2.getBytes(StandardCharsets.UTF_8)));

            InputStream is2 = ftp.retrieveFileStream(file1Name);

            String content2 = inputStreamToString(is2);
            assertEquals("Content is not as expected", file1Content2, content2);
            ftp.completePendingCommand();

            // now delete the file we have been using.
            assertTrue("unsuccessful delete", ftp.deleteFile(file1Name));

            // negative test - file should have gone now.
            assertFalse("file exists after deletion", ftp.deleteFile(file1Name));

        }
        finally
        {
            // clean up tree if left over from previous run
            ftp.disconnect();
        }
    }

    /**
     * Test of obscure path names in the FTP server
     * RFC959 states that paths are constructed thus...
     * <string> ::= <char> | <char><string>
     * <pathname> ::= <string>
     * <char> ::= any of the 128 ASCII characters except <CR> and <LF>
     *  So we need to check how high characters and problematic are encoded
     */
    @Test
    public void testPathNames() throws Exception
    {

        LOGGER.debug("Start testPathNames");

        FTPClient ftp = connectClient();

        String path1 = "testPathNames";

        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
            assertTrue("admin login should be successful", isLoggedIn);

            reply = ftp.cwd("/Alfresco/User*Homes");
            assertTrue("unable to change to /Alfresco/User*Homes", FTPReply.isPositiveCompletion(reply));

            // Delete the root directory in case it was left over from a previous test run
            try
            {
                ftp.removeDirectory(path1);
            }
            catch (IOException e)
            {
                // ignore this error
            }

            // make root directory for this test
            boolean isDirectoryMade = ftp.makeDirectory(path1);
            assertTrue("unable to make directory:" + path1, isDirectoryMade);

            boolean isDirectoryChanged = ftp.changeWorkingDirectory(path1);
            assertTrue("unable to change to working directory:" + path1, isDirectoryChanged);

            assertTrue("with a space", ftp.makeDirectory("test space"));
            assertTrue("with exclamation", ftp.makeDirectory("space!"));
            assertTrue("with dollar", ftp.makeDirectory("space$"));
            assertTrue("with brackets", ftp.makeDirectory("space()"));
            assertTrue("with hash curley  brackets", ftp.makeDirectory("space{}"));


            //Pound sign U+00A3
            //Yen Sign U+00A5
            //Capital Omega U+03A9

            assertTrue("with pound sign", ftp.makeDirectory("pound \u00A3.world"));
            assertTrue("with yen sign", ftp.makeDirectory("yen \u00A5.world"));

            // Test steps that do not work
            // assertTrue("with omega", ftp.makeDirectory("omega \u03A9.world"));
            // assertTrue("with obscure ASCII chars", ftp.makeDirectory("?/.,<>"));    
        }
        finally
        {
            // clean up tree if left over from previous run
            ftp.disconnect();
        }


    }

    /**
     * Test of rename case ALF-20584
     */
    @Test
    public void testRenameCase() throws Exception
    {

        LOGGER.debug("Start testRenameCase");

        FTPClient ftp = connectClient();

        String path1 = "testRenameCase";

        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
            assertTrue("admin login should be successful", isLoggedIn);

            reply = ftp.cwd("/Alfresco/User*Homes");
            assertTrue("unable to change to /Alfresco/User*Homes", FTPReply.isPositiveCompletion(reply));

            // Delete the root directory in case it was left over from a previous test run
            try
            {
                ftp.removeDirectory(path1);
            }
            catch (IOException e)
            {
                // ignore this error
            }

            // make root directory for this test
            boolean isDirectoryMade = ftp.makeDirectory(path1);
            assertTrue("unable to make directory:" + path1, isDirectoryMade);

            ftp.cwd(path1);

            String file1Content2="That's how it is says Pooh!";
            ftp.storeFile("FileA.txt", new ByteArrayInputStream(file1Content2.getBytes(StandardCharsets.UTF_8)));

            assertTrue("unable to rename", ftp.rename("FileA.txt", "FILEA.TXT"));

        }
        finally
        {
            // clean up tree if left over from previous run

            ftp.disconnect();
        }


    } // test Rename Case


    /**
     * Create a user other than "admin" who has access to a set of files.
     * Create a folder containing test.docx as user one
     * Update that file as user two.
     * Check user one can see user two's changes.
     *
     * @throws Exception may throw Exception
     */
    @Test
    public void testTwoUserUpdate() throws Exception
    {
        LOGGER.debug("Start testFTPConnect");

        final String testDir = "/Alfresco/User Homes/" + USER_ONE;

        FTPClient ftpOne = connectClient();
        FTPClient ftpTwo = connectClient();
        try
        {
            int reply = ftpOne.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            reply = ftpTwo.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftpOne.login(USER_ONE, PASSWORD_ONE);
            assertTrue("user one login should be successful", isLoggedIn);

            isLoggedIn = ftpTwo.login(USER_TWO, PASSWORD_TWO);
            assertTrue("user two login should be successful", isLoggedIn);

            boolean isDirectoryChanged = ftpOne.changeWorkingDirectory("Alfresco");
            assertTrue("user one unable to cd to Alfresco", isDirectoryChanged);
            isDirectoryChanged = ftpOne.changeWorkingDirectory("User*Homes");
            assertTrue("user one unable to cd to User*Homes", isDirectoryChanged);
            isDirectoryChanged = ftpOne.changeWorkingDirectory(USER_ONE);
            assertTrue("user one unable to cd to " + USER_ONE, isDirectoryChanged);

            isDirectoryChanged = ftpTwo.changeWorkingDirectory("Alfresco");
            assertTrue("user two unable to cd to Alfresco", isDirectoryChanged);
            isDirectoryChanged = ftpTwo.changeWorkingDirectory("User*Homes");
            assertTrue("user two unable to cd to User*Homes", isDirectoryChanged);
            isDirectoryChanged = ftpTwo.changeWorkingDirectory(USER_ONE);
            assertTrue("user two unable to cd " + USER_ONE, isDirectoryChanged);

            // Create a file as user one
            String file1Content1 = "test file 1 content";
            String file1Name = "test.docx";
            boolean isAppend = ftpOne.appendFile(file1Name , new ByteArrayInputStream(file1Content1.getBytes(StandardCharsets.UTF_8)));
            assertTrue("user one should be able to append file", isAppend);

            // Update the file as user two
            String file1Content2 = "test file content updated";
            boolean isStored = ftpTwo.storeFile(file1Name , new ByteArrayInputStream(file1Content2.getBytes(StandardCharsets.UTF_8)));
            assertTrue("user two should be able to store file", isStored);

            // User one should read user2's content
            InputStream is1 = ftpOne.retrieveFileStream(file1Name);
            assertNotNull("is1 is null", is1);
            String content1 = inputStreamToString(is1);
            assertEquals("Content is not as expected", file1Content2, content1);
            ftpOne.completePendingCommand();

            // User two should read user2's content
            InputStream is2 = ftpTwo.retrieveFileStream(file1Name);
            assertNotNull("is2 is null", is2);
            String content2 = inputStreamToString(is2);
            assertEquals("Content is not as expected", file1Content2, content2);
            ftpTwo.completePendingCommand();
            LOGGER.debug("Test finished");
        }
        finally
        {
            ftpOne.dele(testDir);
            ftpOne.disconnect();
            ftpTwo.disconnect();
        }

    }

    /**
     * Test a quota failure exception over FTP.
     * A file should not exist after a create and quota exception.
     */
    @Test
    public void testFtpQuotaAndFtp() throws Exception
    {
        // Enable usages
        ContentUsageImpl contentUsage = (ContentUsageImpl)applicationContext.getBean("contentUsageImpl");
        contentUsage.setEnabled(true);
        contentUsage.init();
        UserUsageTrackingComponent userUsageTrackingComponent = (UserUsageTrackingComponent)applicationContext.getBean("userUsageTrackingComponent");
        userUsageTrackingComponent.setEnabled(true);
        userUsageTrackingComponent.bootstrapInternal();

        final String testDir = "/Alfresco/User Homes/" + USER_THREE;

        FTPClient ftpOne = connectClient();
        try
        {
            int reply = ftpOne.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftpOne.login(USER_THREE, PASSWORD_THREE);
            assertTrue("user three login should be successful", isLoggedIn);

            boolean isDirectoryChanged = ftpOne.changeWorkingDirectory("Alfresco");
            assertTrue("user three unable to cd to Alfresco", isDirectoryChanged);
            isDirectoryChanged = ftpOne.changeWorkingDirectory("User*Homes");
            assertTrue("user one unable to cd to User*Homes", isDirectoryChanged);
            isDirectoryChanged = ftpOne.changeWorkingDirectory(USER_THREE);
            assertTrue("user one unable to cd to " + USER_THREE, isDirectoryChanged);

            /*
             * Create a file as user three which is bigger than the quota
             */
            String file3Content3 = "test file 3 content that needs to be greater than 100 bytes to result in a quota exception being thrown";
            String file1Name = "test.docx";

            // Should not be success
            boolean isAppend = ftpOne.appendFile(file1Name , new ByteArrayInputStream(file3Content3.getBytes(StandardCharsets.UTF_8)));
            assertFalse("user one can ignore quota", isAppend);

            boolean isDeleted = ftpOne.deleteFile(file1Name);
            assertFalse("quota exception expected", isDeleted);

            LOGGER.debug("test done");

        }
        finally
        {
            // Disable usages
            contentUsage.setEnabled(false);
            contentUsage.init();
            userUsageTrackingComponent.setEnabled(false);
            userUsageTrackingComponent.bootstrapInternal();

            ftpOne.dele(testDir);
            ftpOne.disconnect();
        }

    }

    /**
     * Test Setting the modification time FTP server
     *
     * @throws Exception may throw Exception
     */
    @Test
    public void testModificationTime() throws Exception
    {
        final String path1 = "FTPServerTest";
        final String path2 = "ModificationTime";

        LOGGER.debug("Start testModificationTime");

        FTPClient ftp = connectClient();

        try
        {
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                fail("FTP server refused connection.");
            }

            boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
            assertTrue("admin login should be successful", isLoggedIn);

            reply = ftp.cwd("/Alfresco/User Homes");
            assertTrue("unable to change to /Alfresco/User Homes", FTPReply.isPositiveCompletion(reply));

            // Delete the root directory in case it was left over from a previous test run
            try
            {
                ftp.removeDirectory(path1);
            }
            catch (IOException e)
            {
                // ignore this error
            }

            // make root directory
            ftp.makeDirectory(path1);
            ftp.cwd(path1);

            // make subdirectory in new directory
            ftp.makeDirectory(path2);
            ftp.cwd(path2);

            // List the files in the new directory
            FTPFile[] files = ftp.listFiles();
            assertEquals("there should be no files", 0, files.length);

            // Create a file
            String file1Content1 = "test file 1 content";
            String file1Name = "testFile1.txt";
            ftp.appendFile(file1Name , new ByteArrayInputStream(file1Content1.getBytes(StandardCharsets.UTF_8)));


            String pathname = "/Alfresco/User Homes" + "/" + path1 + "/" + path2 + "/" + file1Name;

            LOGGER.debug("set modification time");
            // YYYYMMDDhhmmss Time set to 2012 August 30 12:39:05
            String olympicTime = "20120830123905";
            ftp.setModificationTime(pathname, olympicTime);

            String extractedTime = ftp.getModificationTime(pathname);
            // Feature of the commons ftp library ExtractedTime has a "status code" first and is followed by newline chars

            assertTrue("time not set correctly by explicit set time", extractedTime.contains(olympicTime));

            // Get the new file
            FTPFile[] files2 = ftp.listFiles();
            assertEquals("there should be only 1 file", 1, files2.length);

            InputStream is = ftp.retrieveFileStream(file1Name);

            String content = inputStreamToString(is);
            assertEquals("Content is not as expected", content, file1Content1);
            ftp.completePendingCommand();

            // Update the file contents without setting time directly
            String file1Content2 = "That's how it is says Pooh!";
            ftp.storeFile(file1Name, new ByteArrayInputStream(file1Content2.getBytes(StandardCharsets.UTF_8)));

            InputStream is2 = ftp.retrieveFileStream(file1Name);

            String content2 = inputStreamToString(is2);
            assertEquals("Content is not as expected", file1Content2, content2);
            ftp.completePendingCommand();

            extractedTime = ftp.getModificationTime(pathname);

            assertFalse("time not moved on if time not explicitly set", extractedTime.contains(olympicTime));

            // now delete the file we have been using.
            assertTrue("unsuccessful delete", ftp.deleteFile(file1Name));

            // negative test - file should have gone now.
            assertFalse("file exists after deletion", ftp.deleteFile(file1Name));

        }
        finally
        {
            // clean up tree if left over from previous run

            ftp.disconnect();
        }
    }  // test set time

    /**
     * Test for Passive Mode -> FTPCommand.Pasv command with external address functionality.
     * see MNT-16433
     */
    @Test
    public void testFTPConnectExternalAddressSet() throws Exception
    {
        LOGGER.debug("Start testFTPConnectExternalAddressSet");
        try
        {
            // use a highly improbable IP to tests Passive Mode -> FTPCommand.Pasv command
            // this is supposed to be the address of a proxy in front of Alfresco FTP server
            String improbableIPAddress = "127.255.255.42";
            ftpConfigSection.setFTPExternalAddress(improbableIPAddress);
            FTPClient ftp = connectClient();
            try
            {
                int reply = ftp.getReplyCode();

                if (!FTPReply.isPositiveCompletion(reply))
                {
                    fail("FTP server refused connection.");
                }
                boolean isLoggedIn = ftp.login(USER_ADMIN, PASSWORD_ADMIN);
                assertTrue("admin login should be successful", isLoggedIn);

                // activate passive mode
                boolean isPassiveMode = ftp.enterRemotePassiveMode();
                assertTrue("failed activating passive mode", isPassiveMode);

                assertEquals("Client should be in passive mode now", FTPClient.PASSIVE_REMOTE_DATA_CONNECTION_MODE, ftp.getDataConnectionMode());

                reply = ftp.getReplyCode();
                //see https://www.ietf.org/rfc/rfc959.txt
                assertEquals("reply code should be 227", 227, reply);

                String replyLine = ftp.getReplyString();
                assertNotNull("replyLine should not be null", replyLine);
                String encodedImprobableIPAddress = improbableIPAddress.replaceAll("\\.", ",");
                assertTrue("Pasv command should contain the set external address encoded", replyLine.contains(encodedImprobableIPAddress));

                // now attempt to list the files and check that the command does not succeed
                FTPFile[] files = ftp.listFiles();
                assertNotNull("files should not be null", files);
                assertEquals("list command should not succeed", 0, files.length);

                assertEquals("The passive host should be the one set earlier.", improbableIPAddress, ftp.getPassiveHost());
            }
            finally
            {
                safeDisconnect(ftp);
            }
        }
        finally
        {
            //always revert back to default, or the other tests will fail
            ftpConfigSection.setFTPExternalAddress(null);
        }
    }

    private void safeDisconnect(FTPClient ftp)
    {
        try
        {
            if (ftp != null)
            {
                ftp.disconnect();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Could not gracefully disconnect the ftp client.", e);
        }
    }

    private FTPClient connectClient() throws IOException
    {
        FTPClient ftp = new FTPClient();

        if(LOGGER.isDebugEnabled())
        {
            ftp.addProtocolCommandListener(new PrintCommandListener(
                                       new PrintWriter(System.out)));
        }
        ftp.connect(HOSTNAME, ftpConfigSection.getFTPPort());
        return ftp;
    }

    /**
     * Test quality utility to read an input stream into a string.
     * @param is the inputStream to convert
     * @return the content of the stream in a string.
     * @throws IOException may throw Exception
     */
    private String inputStreamToString(InputStream is) throws IOException
    {
        if (is != null)
        {
            try (StringWriter writer = new StringWriter())
            {
                char[] buffer = new char[1024];
                try
                {
                    Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    int n;
                    while ((n = reader.read(buffer)) != -1)
                    {
                        writer.write(buffer, 0, n);
                    }
                }
                finally
                {
                    is.close();
                }

                return writer.getBuffer().toString();
            }
        }
        return "";
    }

    /**
     * create a test user
     * @param userName the username
     * @param password the password
     * @param quota the sizeQuota
     */
    private void createUser(String userName, String password, long quota)
    {
        if (!this.authenticationService.authenticationExists(userName))
        {
            this.authenticationService.createAuthentication(userName, password.toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            if(quota > 0)
            {
                ppOne.put(ContentModel.PROP_SIZE_QUOTA, quota);
            }

            this.personService.createPerson(ppOne);
        }
    }

}
