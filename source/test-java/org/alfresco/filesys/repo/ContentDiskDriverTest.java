/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.filesys.repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.transaction.UserTransaction;
import javax.xml.ws.Holder;

import junit.framework.TestCase;

import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.jlan.server.NetworkServer;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.AccessMode;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FileAction;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileExistsException;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.NetworkFileServer;
import org.alfresco.jlan.server.filesys.PermissionDeniedException;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit tests for Alfresco Repository ContentDiskDriver
 */
@Category(BaseSpringTestsCategory.class)
public class ContentDiskDriverTest extends TestCase
{
    private static final String TEST_PROTOTYPE_NAME = "test";
    private static final String TEST_REMOTE_NAME = "remoteName";
    private static final String TEST_SERVER_NAME = "testServer";

    private static final String TEST_USER_AUTHORITY = "userx";

    private Repository repositoryHelper;
    private CifsHelper cifsHelper;
    private ExtendedDiskInterface driver;
    private NodeService mlAwareNodeService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private ContentService contentService;
    private RuleService ruleService;
    private ActionService actionService;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    private OwnableService ownableService;
    private FileFolderService fileFolderService;
    private CheckOutCheckInService checkOutCheckInService;
    
    private static Log logger = LogFactory.getLog(ContentDiskDriverTest.class);

    final String SHARE_NAME = "test";
    final String STORE_NAME = "workspace://SpacesStore";
    final String ROOT_PATH = "/app:company_home";
    
    private ApplicationContext applicationContext;
    
    private final String TEST_ROOT_PATH="ContentDiskDriverTest";
    private final String TEST_ROOT_DOS_PATH="\\"+TEST_ROOT_PATH;
    
    @Override
    protected void setUp() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();
        repositoryHelper = (Repository)this.applicationContext.getBean("repositoryHelper");
        ApplicationContextFactory fileServers = (ApplicationContextFactory) this.applicationContext.getBean("fileServers");
        cifsHelper = (CifsHelper) fileServers.getApplicationContext().getBean("cifsHelper");
        driver = (ExtendedDiskInterface)this.applicationContext.getBean("contentDiskDriver");
        mlAwareNodeService = (NodeService) this.applicationContext.getBean("mlAwareNodeService"); 
        nodeService = (NodeService)applicationContext.getBean("nodeService");
        transactionService = (TransactionService)applicationContext.getBean("transactionService");
        contentService = (ContentService)applicationContext.getBean("contentService");
        ruleService = (RuleService)applicationContext.getBean("ruleService");
        actionService = (ActionService)this.applicationContext.getBean("actionService");
        personService = (PersonService) this.applicationContext.getBean("personService");
        authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        permissionService = (PermissionService) this.applicationContext.getBean("permissionService");
        ownableService = (OwnableService) this.applicationContext.getBean("ownableService");
        fileFolderService = (FileFolderService) this.applicationContext.getBean("fileFolderService");
        checkOutCheckInService = (CheckOutCheckInService) this.applicationContext.getBean("checkOutCheckInService");
        
        assertNotNull("content disk driver is null", driver);
        assertNotNull("repositoryHelper is null", repositoryHelper);
        assertNotNull("mlAwareNodeService is null", mlAwareNodeService);
        assertNotNull("nodeService is null", nodeService);
        assertNotNull("transactionService is null", transactionService);
        assertNotNull("contentService is null", contentService);
        assertNotNull("ruleService is null", ruleService);
        assertNotNull("actionService is null", actionService);
        assertNotNull("cifsHelper", cifsHelper);
        assertNotNull("checkOutCheckInService", checkOutCheckInService);
        
        AuthenticationUtil.setRunAsUserSystem();
        
        // remove our test root 
        RetryingTransactionCallback<Void> removeRootCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef rootNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, TEST_ROOT_PATH);
                if(rootNode != null)
                {
                    logger.debug("Clean up test root node");
                    nodeService.deleteNode(rootNode);
                }
                return null;
            }
        };
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        tran.doInTransaction(removeRootCB, false, true);
        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
    }

    private DiskSharedDevice getDiskSharedDevice() throws DeviceContextException
    {
        
        ContentContext ctx = new ContentContext( "testContext", STORE_NAME, ROOT_PATH, repositoryHelper.getCompanyHome());
      
        DiskSharedDevice share = new DiskSharedDevice("test", driver, ctx);        
        return share;
    }
    
    /**
     * Test Get File Information
     */
    public void testGetFileInformation() throws Exception
    {
        logger.debug("testGetFileInformation");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {     
            NodeRef testNodeRef;    
        };
        
        final TestContext testContext = new TestContext();

        /**
         * Test 1 : Get the root info
         */
        FileInfo finfo = driver.getFileInformation(testSession, testConnection, "");
        assertNotNull("root info is null", finfo);
        assertEquals("root has a unexpected file name", "", finfo.getFileName());
        
    }

    /**
     * Test Create File
     */
    public void testCreateFile() throws Exception
    {
        logger.debug("testCreatedFile");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {     
            NodeRef testNodeRef;    
        };
        
        final TestContext testContext = new TestContext();
      
        /**
          * Step 1 : Create a new file in read/write mode and add some content.
           */
        int openAction = FileAction.CreateNotExist;
        

        final String FILE_NAME="testCreateFileX.new";
        final String FILE_PATH="\\"+FILE_NAME;
                  
        FileOpenParams params = new FileOpenParams(FILE_PATH, openAction, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                
        final NetworkFile file = driver.createFile(testSession, testConnection, params);
        assertNotNull("file is null", file);
        assertFalse("file is read only, should be read-write", file.isReadOnly());
        assertFalse("file is not closed ", file.isClosed());
        
        RetryingTransactionCallback<Void> writeStuffCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                byte[] stuff = "Hello World".getBytes();
                driver.writeFile(testSession, testConnection, file, stuff, 0, stuff.length, 0);
                driver.closeFile(testSession, testConnection, file); 
                return null;
            }
        };
        tran.doInTransaction(writeStuffCB);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                testContext.testNodeRef = newNode;
                assertNotNull("can't find new node", newNode);
                Serializable content = nodeService.getProperty(newNode, ContentModel.PROP_CONTENT);
                assertNotNull("content is null", content);       
                return null;
            }
        };
        tran.doInTransaction(validateCB);
        
        // now validate that the new node is in the correct location and has the correct name
        FileInfo info = driver.getFileInformation(testSession, testConnection, FILE_PATH);
        assertNotNull("info is null", info);
        
        NodeRef n2 = getNodeForPath(testConnection, FILE_PATH);
        assertEquals("get Node For Path returned different node", testContext.testNodeRef, n2);
        
        /**
         * Step 2 : Negative Test Attempt to create the same file again
         */
        try
        {
            driver.createFile(testSession, testConnection, params);
            fail("File exists not detected");
        }
        catch (FileExistsException fe)
        {
            // expect to go here
        }
        
        // Clean up so we could run the test again
        driver.deleteFile(testSession, testConnection, FILE_PATH);

        /**
         * Step 3 : create a file in a new directory in read only mode
         */        
        String FILE2_PATH = TEST_ROOT_DOS_PATH + FILE_PATH;
        
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, openAction, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);
        
        FileOpenParams file2Params = new FileOpenParams(FILE2_PATH, openAction, AccessMode.ReadOnly, FileAttribute.NTNormal, 0);
        NetworkFile file2 = driver.createFile(testSession, testConnection, file2Params);
        
        // clean up so we could run the test again
        driver.deleteFile(testSession, testConnection, FILE2_PATH);
    }
    
    /**
     * Unit test of delete file
     */
    public void testDeleteFile() throws Exception
    {
        logger.debug("testDeleteFile");
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();

        TreeConnection testConnection = testServer.getTreeConnection(share);
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        /**
         * Step 1 : Create a new file in read/write mode and add some content.
         */
        int openAction = FileAction.CreateNotExist;
        String FILE_PATH="\\testDeleteFile.new";
          
        FileOpenParams params = new FileOpenParams(FILE_PATH, openAction, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                
        final NetworkFile file = driver.createFile(testSession, testConnection, params);
        assertNotNull("file is null", file);
        assertFalse("file is read only, should be read-write", file.isReadOnly());
            
        RetryingTransactionCallback<Void> writeStuffCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                byte[] stuff = "Hello World".getBytes();
                file.writeFile(stuff, stuff.length, 0, 0);
                file.close();  // needed to actually flush content to node
                return null;
            }
        };
        tran.doInTransaction(writeStuffCB);
                 
        /**
          * Step 1: Delete file by path
          */
        driver.deleteFile(testSession, testConnection, FILE_PATH);
        
        /**
         * Step 2: Negative test - Delete file again
         */
        try
        {
            driver.deleteFile(testSession, testConnection, FILE_PATH);
            fail("delete a non existent file");
        }
        catch (IOException fe)
        {
                // expect to go here
        }
    }
    
    /**
     * Test Set Info
     * 
     * Three flags set
     * <ol>
     * <li>SetDeleteOnClose</li>
     * <li>SetCreationDate</li>
     * <li>SetModifyDate</li>
     * </ol>
     */
    public void testSetFileInfo() throws Exception
    {
        logger.debug("testSetFileInfo");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        Date now = new Date();
        
        // CREATE 6 hours ago
        final Date CREATED = new Date(now.getTime() - 1000 * 60 * 60 * 6);
        // Modify one hour ago
        final Date MODIFIED = new Date(now.getTime() - 1000 * 60 * 60 * 1);
        
        class TestContext
        {     
            NodeRef testNodeRef;    
        };
        
        final TestContext testContext = new TestContext();
        

      
        /**
          * Step 1 : Create a new file in read/write mode and add some content.
          * Call SetInfo to set the creation date
          */
        int openAction = FileAction.CreateNotExist;
        
        final String FILE_NAME="testSetFileInfo.txt";
        final String FILE_PATH="\\"+FILE_NAME;
        
        // Clean up junk if it exists
        try
        {
            driver.deleteFile(testSession, testConnection, FILE_PATH);
        }
        catch (IOException ie)
        {
           // expect to go here
        }
                  
        final FileOpenParams params = new FileOpenParams(FILE_PATH, openAction, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                
        final NetworkFile file = driver.createFile(testSession, testConnection, params);
        assertNotNull("file is null", file);
        assertFalse("file is read only, should be read-write", file.isReadOnly());
        
        RetryingTransactionCallback<Void> writeStuffCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                byte[] stuff = "Hello World".getBytes();
                
                driver.writeFile(testSession, testConnection, file, stuff, 0, stuff.length, 0);
                driver.closeFile(testSession, testConnection, file);
                              
                FileInfo info = driver.getFileInformation(testSession, testConnection, FILE_PATH);
                info.setFileInformationFlags(FileInfo.SetModifyDate);
                info.setModifyDateTime(MODIFIED.getTime());
                driver.setFileInformation(testSession, testConnection, FILE_PATH, info);
                return null;
            }
        };
        tran.doInTransaction(writeStuffCB);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                testContext.testNodeRef = newNode;
                assertNotNull("can't find new node", newNode);
                Serializable content = nodeService.getProperty(newNode, ContentModel.PROP_CONTENT);
                assertNotNull("content is null", content);     
                Date modified = (Date)nodeService.getProperty(newNode, ContentModel.PROP_MODIFIED);
                assertEquals("modified time not set correctly", MODIFIED, modified);
                return null;
            }
        };
        tran.doInTransaction(validateCB);
        
        /**
         * Step 2: Change the created date
         */
        logger.debug("Step 2: Change the created date");
        RetryingTransactionCallback<Void> changeCreatedCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileInfo info = driver.getFileInformation(testSession, testConnection, FILE_PATH);
                info.setFileInformationFlags(FileInfo.SetCreationDate);
                info.setCreationDateTime(CREATED.getTime());
                driver.setFileInformation(testSession, testConnection, FILE_PATH, info);
                return null;
            }
        };
        tran.doInTransaction(changeCreatedCB);
  
        RetryingTransactionCallback<Void> validateCreatedCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                testContext.testNodeRef = newNode;
                assertNotNull("can't find new node", newNode);
                Serializable content = nodeService.getProperty(newNode, ContentModel.PROP_CONTENT);
                assertNotNull("content is null", content);     
                Date created = (Date)nodeService.getProperty(newNode, ContentModel.PROP_CREATED);
                assertEquals("created time not set correctly", CREATED, created);
                return null;
            }
        };
        tran.doInTransaction(validateCreatedCB);
        
        /**
         * Step 3: Test 
         */
        logger.debug("Step 3: test deleteOnClose");
        RetryingTransactionCallback<Void> deleteOnCloseCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NetworkFile f2 = driver.openFile(testSession, testConnection, params);
                 
               FileInfo info = driver.getFileInformation(testSession, testConnection, FILE_PATH);
               info.setFileInformationFlags(FileInfo.SetDeleteOnClose);
               driver.setFileInformation(testSession, testConnection, FILE_PATH, info);
               file.setDeleteOnClose(true);
               
               byte[] stuff = "Update".getBytes();              
               driver.writeFile(testSession, testConnection, file, stuff, 0, stuff.length, 0);
               driver.closeFile(testSession, testConnection, file);
             
               return null;
            }
        };
        tran.doInTransaction(deleteOnCloseCB);
  
        RetryingTransactionCallback<Void> validateDeleteOnCloseCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                assertNull("can still find new node", newNode);
                return null;
            }
        };
        tran.doInTransaction(validateDeleteOnCloseCB);
        
        // clean up so we could run the test again
        //driver.deleteFile(testSession, testConnection, FILE_PATH);    
        
    } // test set file info

    
    /**
     * Test Open File
     */
    public void testOpenFile() throws Exception
    {    
        logger.debug("testOpenFile");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        class TestContext
        {
            NodeRef testDirNodeRef;
        };
        
        final TestContext testContext = new TestContext();
        
        final String FILE_NAME="testOpenFile.txt";
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);  
        
        testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_ROOT_DOS_PATH);

        /**
         * Step 1 : Negative test - try to open a file that does not exist
         */
        final String FILE_PATH= TEST_ROOT_DOS_PATH + "\\" + FILE_NAME;
          
        FileOpenParams params = new FileOpenParams(FILE_PATH, FileAction.CreateNotExist, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        try
        {
            NetworkFile file = driver.openFile(testSession, testConnection, params);
            fail ("managed to open non existant file!");
        }
        catch (IOException ie)
        {
           // expect to go here
        }
        
        /**
         * Step 2: Now create the file through the node service and open it.
         */
        logger.debug("Step 2) Open file created by node service");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ChildAssociationRef ref = nodeService.createNode(testContext.testDirNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, FILE_NAME), ContentModel.TYPE_CONTENT);
                nodeService.setProperty(ref.getChildRef(), ContentModel.PROP_NAME, FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        NetworkFile file = driver.openFile(testSession, testConnection, params);
        assertNotNull(file);
        assertFalse("file is closed", file.isClosed());
        
        /**
         * Step 3: Open the root directory.
         */
        logger.debug("Step 3) Open the root directory");
        
        FileOpenParams rootParams = new FileOpenParams("\\", FileAction.CreateNotExist, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        NetworkFile file3 = driver.openFile(testSession, testConnection, rootParams);
        assertNotNull(file3);
        assertFalse("file is closed", file3.isClosed());

                
    } // testOpenFile

    
    /**
     * Unit test of file exists
     */
    public void testFileExists() throws Exception
    {
        logger.debug("testFileExists");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        final String FILE_PATH= TEST_ROOT_DOS_PATH + "\\testFileExists.new";
        
        class TestContext
        {
        };
        
        final TestContext testContext = new TestContext();
       
        /**
         * Step 1 : Call FileExists for a directory which does not exist
         */
        logger.debug("Step 1, negative test dir does not exist");
        int status = driver.fileExists(testSession, testConnection, TEST_ROOT_DOS_PATH);
        assertEquals(status, 0);
  
        /**
         * Step 2 : Call FileExists for a file which does not exist
         */
        logger.debug("Step 2, negative test file does not exist");
        status = driver.fileExists(testSession, testConnection, FILE_PATH);
        assertEquals(status, 0);
        
        /**
         * Step 3: Create a new file in read/write mode and add some content.
         */
        int openAction = FileAction.CreateNotExist;

        FileOpenParams params = new FileOpenParams(FILE_PATH, openAction, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
 
        driver.createDirectory(testSession, testConnection, dirParams);        
        final NetworkFile file = driver.createFile(testSession, testConnection, params);
        assertNotNull("file is null", file);
        assertFalse("file is read only, should be read-write", file.isReadOnly());
        
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                byte[] stuff = "Hello World".getBytes();
                file.writeFile(stuff, stuff.length, 0, 0);
                driver.closeFile(testSession, testConnection, file);
            
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
         
        status = driver.fileExists(testSession, testConnection, FILE_PATH);
        assertEquals(status, 1);
         
        /**
          * Step 4 : Delete the node - check status goes back to 0
          */
        logger.debug("Step 4, successfully delete node");
        driver.deleteFile(testSession, testConnection, FILE_PATH);
        
        status = driver.fileExists(testSession, testConnection, FILE_PATH);
        assertEquals(status, 0);  
   
    } // testFileExists
    
    /**
     * Unit test of rename file
     */
    public void testRenameFile() throws Exception
    {  
        logger.debug("testRenameFile");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);

        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
 
        final String FILE_PATH1=TEST_ROOT_DOS_PATH + "\\SourceFile1.new";
        final String FILE_NAME2 = "SourceFile2.new";
        final String FILE_PATH2=TEST_ROOT_DOS_PATH +"\\" + FILE_NAME2;
        final String FILE_PATH3=TEST_ROOT_DOS_PATH +"\\SourceFile3.new";
        
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);

        FileOpenParams params1 = new FileOpenParams(FILE_PATH1, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        final NetworkFile file1 = driver.createFile(testSession, testConnection, params1);
  
        FileOpenParams params3 = new FileOpenParams(FILE_PATH3, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        final NetworkFile file3 = driver.createFile(testSession, testConnection, params3);
        
        /**
         * Step 1 : Negative test, Call Rename for a file which does not exist
        */
        try
        {
            driver.renameFile(testSession, testConnection, "\\Wibble\\wobble", FILE_PATH1);
            fail("rename did not detect missing file");
        }
        catch (IOException e)
        {
            // expect to go here
        }
          
        /**
         * Step 2: Negative test, Call Rename for a destination that does not exist.
         */
        try
        {
            driver.renameFile(testSession, testConnection, FILE_PATH1, "\\wibble\\wobble");
            fail("rename did not detect missing file");
        }
        catch (IOException e)
        {
            // expect to go here
        }
        
        /**
         * Step 3: Rename a file to a destination that is a file rather than a directory
         */
        try
        {
            driver.renameFile(testSession, testConnection, FILE_PATH1, FILE_PATH3);
            fail("rename did not detect missing file");
        }
        catch (IOException e)
        {
            // expect to go here
        }
                 
        /**
         * Step 4: Successfully rename a file - check the name, props and content.
         */
        final String LAST_NAME= "Bloggs";
        
        RetryingTransactionCallback<Void> setPropertiesCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                final NodeRef file1NodeRef = getNodeForPath(testConnection, FILE_PATH1);
                assertNotNull("node ref not found", file1NodeRef);
                nodeService.setProperty(file1NodeRef, ContentModel.PROP_LASTNAME, LAST_NAME);
         
                return null;
            }
        };
        tran.doInTransaction(setPropertiesCB, false, true);
        
        driver.renameFile(testSession, testConnection, FILE_PATH1, FILE_PATH2);
       
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef file2NodeRef = getNodeForPath(testConnection, FILE_PATH2);
                //assertEquals("node ref has changed on a rename", file1NodeRef, file2NodeRef);
                assertEquals(nodeService.getProperty(file2NodeRef, ContentModel.PROP_LASTNAME), LAST_NAME);
                ChildAssociationRef parentRef = nodeService.getPrimaryParent(file2NodeRef);
                assertTrue("file has wrong assoc local name", parentRef.getQName().getLocalName().equals(FILE_NAME2));
                assertTrue("not primary assoc", parentRef.isPrimary());

                return null;
            }
        };
        tran.doInTransaction(validateCB, false, true);

        /**
         * Step 5: Rename to another directory
         */
        String DIR_NEW_PATH = TEST_ROOT_DOS_PATH + "\\NewDir";
        String NEW_PATH = DIR_NEW_PATH + "\\File2";
        FileOpenParams params5 = new FileOpenParams(DIR_NEW_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        driver.createDirectory(testSession, testConnection, params5);
        
        NodeRef newDirNodeRef = getNodeForPath(testConnection, DIR_NEW_PATH);
        
        driver.renameFile(testSession, testConnection, FILE_PATH2, NEW_PATH);
        
        NodeRef file5NodeRef = getNodeForPath(testConnection, NEW_PATH);
        ChildAssociationRef parentRef5 = nodeService.getPrimaryParent(file5NodeRef);
        
        assertTrue(parentRef5.getParentRef().equals(newDirNodeRef));
        
//        /** 
//         * Step 5: rename to self - check no damage.
//         */
//        try
//        {
//            driver.renameFile(testSession, testConnection, FILE_PATH2, FILE_PATH2);
//            fail("rename did not detect rename to self");
//        }
//        catch (IOException e)
//        {
            // expect to go here
//        }
        
    } // testRenameFile


    /**
     * Unit test of rename versionable file
     */
    public void testScenarioRenameVersionableFile() throws Exception
    {  
        logger.debug("testScenarioRenameVersionableFile");
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        final String FILE_PATH1=TEST_ROOT_DOS_PATH + "\\SourceFile1.new";
        final String FILE_PATH2=TEST_ROOT_DOS_PATH + "\\SourceFile2.new";
        
        class TestContext
        {
        };
        
        final TestContext testContext = new TestContext();
   
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);

        FileOpenParams params1 = new FileOpenParams(FILE_PATH1, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        NetworkFile file1 = driver.createFile(testSession, testConnection, params1);
        
        /**
         * Make Node 1 versionable
         */
        final String LAST_NAME= "Bloggs";
         
        RetryingTransactionCallback<Void> makeVersionableCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef file1NodeRef = getNodeForPath(testConnection, FILE_PATH1);
                nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                ContentWriter contentWriter2 = contentService.getWriter(file1NodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent("test rename versionable");
                
                nodeService.setProperty(file1NodeRef, ContentModel.PROP_LASTNAME, LAST_NAME);
                nodeService.setProperty(file1NodeRef, TransferModel.PROP_ENDPOINT_PROTOCOL, "http");

                return null;
            }
        };
        tran.doInTransaction(makeVersionableCB, false, true);
  
        /**
         * Step 1: Successfully rename a versionable file - check the name, props and content.
         * TODO Check primary assoc, peer assocs, child assocs, modified date, created date, nodeid, permissions.
         */
        driver.renameFile(testSession, testConnection, FILE_PATH1, FILE_PATH2);
        
        RetryingTransactionCallback<Void> validateVersionableCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef file2NodeRef = getNodeForPath(testConnection, FILE_PATH2);
                assertNotNull("file2 node ref is null", file2NodeRef);
                //assertEquals(nodeService.getProperty(file2NodeRef, ContentModel.PROP_LASTNAME), LAST_NAME);
                assertTrue("does not have versionable aspect", nodeService.hasAspect(file2NodeRef, ContentModel.ASPECT_VERSIONABLE));   
                assertTrue("sample property is null", nodeService.getProperty(file2NodeRef, TransferModel.PROP_ENDPOINT_PROTOCOL) != null);     
                
                return null;
            }
        };
        tran.doInTransaction(validateVersionableCB, false, true);

    } // testRenameVersionable
    

    /**
     * This test tries to simulate the shuffling that is done by MS Word 2003 upon file save
     * 
     * a) TEST.DOC
     * b) Save to ~WRDnnnn.TMP
     * c) Delete ~WRLnnnn.TMP
     * d) Rename TEST.DOC ~WDLnnnn.TMP
     * e) Delete TEST.DOC
     * f) Rename ~WRDnnnn.TMP to TEST.DOC
     * g) Delete ~WRLnnnn.TMP
     * 
     * We need to check that properties, aspects, primary assocs, secondary assocs, peer assocs, node type, 
     * version history, creation date are maintained.
     */
    public void testScenarioMSWord2003SaveShuffle() throws Exception
    {
        logger.debug("testScenarioMSWord2003SaveShuffle");
        final String FILE_NAME = "TEST.DOC";
        final String FILE_TITLE = "Test document";
        final String FILE_DESCRIPTION = "This is a test document to test CIFS shuffle";
        final String FILE_OLD_TEMP = "~WRL0002.TMP";
        final String FILE_NEW_TEMP = "~WRD0002.TMP";
        
        final QName RESIDUAL_MTTEXT = QName.createQName("{gsxhjsx}", "whatever");
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;
            NetworkFile oldFileHandle;
            
            NodeRef testNodeRef;   // node ref of test.doc
            
            Serializable testCreatedDate;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMSWord2003SaveShuffle";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
  
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // now load up the node with lots of other stuff that we will test to see if it gets preserved during the
                // shuffle.
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                
                // test non CM namespace property
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                // test CM property not related to an aspect
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_ADDRESSEE, "Fred");
                
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_TITLE, FILE_TITLE);
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_DESCRIPTION, FILE_DESCRIPTION);
                
                /**
                 * MLText value - also a residual value in a non cm namespace
                 */
                MLText mltext = new MLText();
                mltext.addValue(Locale.FRENCH, "Bonjour");
                mltext.addValue(Locale.ENGLISH, "Hello");
                mltext.addValue(Locale.ITALY, "Buongiorno");
                mlAwareNodeService.setProperty(testContext.testNodeRef, RESIDUAL_MTTEXT, mltext);

                // classifiable chosen since its not related to any properties.
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_CLASSIFIABLE, null);
                //nodeService.createAssociation(testContext.testNodeRef, targetRef, assocTypeQName);
        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        /**
         * Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "MS Word 2003 shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();   
                
                testContext.testCreatedDate = nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_CREATED);
                
                MLText multi = (MLText)mlAwareNodeService.getProperty(testContext.testNodeRef, RESIDUAL_MTTEXT) ;
                multi.getValues();
     
     
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * b) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "MS Word 2003 shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.newFileHandle.close();
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB, false, true);
        
        /**
         * rename the old file
         */
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
        

        RetryingTransactionCallback<Void> validateOldFileGoneCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                try
                {
                    driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                }
                catch (IOException e)
                { 
                    // expect to go here since previous step renamed the file.
                }
                
                return null;
            }
        };
        tran.doInTransaction(validateOldFileGoneCB, false, true);
        
        logger.debug("Shuffle step next");
        /**
         * Move the new file into place, stuff should get shuffled
         */
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        logger.debug("end of shuffle step");
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
           
               // Check trx:enabled has been shuffled.
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               // check my residual MLText has been transferred
               assertTrue(props.containsKey(RESIDUAL_MTTEXT));
               
               // Check the titled aspect is correct
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );
               assertEquals("title wrong", FILE_TITLE, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_TITLE) );
               assertEquals("description wrong", FILE_DESCRIPTION, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_DESCRIPTION) );

               // ALF-7641 - CIFS shuffle, does not preseve MLText values.
               Map<QName, Serializable> mlProps = mlAwareNodeService.getProperties(shuffledNodeRef);
               
               MLText multi = (MLText)mlAwareNodeService.getProperty(shuffledNodeRef, RESIDUAL_MTTEXT) ;
               assertTrue("MLText has lost values", multi.getValues().size() > 2);
               
//               // ALF-7635 check auditable properties 
               assertEquals("creation date not preserved", ((java.util.Date)testContext.testCreatedDate).getTime(), ((java.util.Date)nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_CREATED)).getTime());
               
               // ALF-7628 - preserve addressee and classifiable
               assertEquals("ADDRESSEE PROPERTY Not copied", "Fred", nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_ADDRESSEE));
               assertTrue("CLASSIFIABLE aspect not present", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_CLASSIFIABLE));
               
               // ALF-7584 - preserve node ref.
               assertEquals("noderef changed", testContext.testNodeRef, shuffledNodeRef);
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        
        tran.doInTransaction(deleteOldFileCB, false, true);
        
    } // testScenarioMSWord2003SaveShuffle
    
    
    /**
     * This test tries to simulate the shuffling that is done by MS Word 2003 
     * with backup enabled upon file save
     * 
     * a) TEST.DOC
     * b) Save to ~WRDnnnn.TMP
     * c) Delete "Backup of TEST.DOC"
     * d) Rename TEST.DOC to "Backup of TEST.DOC"
     * e) Delete TEST.DOC
     * f) Rename ~WRDnnnn.TMP to TEST.DOC
     * 
     * We need to check that properties, aspects, primary assocs, secondary assocs, peer assocs, node type, 
     * version history, creation date are maintained.
     */
    public void testScenarioMSWord2003SaveShuffleWithBackup() throws Exception
    {
        logger.debug("testScenarioMSWord2003SaveShuffleWithBackup");
        final String FILE_NAME = "TEST.DOC";
        final String FILE_OLD_TEMP = "Backup of TEST.DOC";
        final String FILE_NEW_TEMP = "~WRD0002.TMP";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMSWord2003SaveShuffleWithBackup";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
             
        /**
         * Create a file in the test directory
         */            
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // now load up the node with lots of other stuff that we will test to see if it gets preserved during the
                // shuffle.
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                // test non CM namespace property
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                // test CM property not related to an aspect
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_ADDRESSEE, "Fred");
                nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_CREATED);
                // classifiable chosen since its not related to any properties.
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_CLASSIFIABLE, null);
                //nodeService.createAssociation(testContext.testNodeRef, targetRef, assocTypeQName);
        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        /**
         * Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "MS Word 2003 shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();            
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * b) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "MS Word 2003 shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.newFileHandle.close();
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB, false, true);
        
        /**
         * rename the old file
         */
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
        

        RetryingTransactionCallback<Void> validateOldFileGoneCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                try
                {
                    driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                }
                catch (IOException e)
                { 
                    // expect to go here since previous step renamed the file.
                }
                
                return null;
            }
        };
        tran.doInTransaction(validateOldFileGoneCB, false, true);
        
        /**
         * Move the new file into place, stuff should get shuffled
         */
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );
               
               // commented out due to ALF-7628 
               //assertEquals("ADDRESSEE PROPERTY Not copied", "Fred", nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_ADDRESSEE));
               //assertEquals("created date changed", testContext.testCreatedDate, (Date)nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_CREATED));
               
               // assertTrue("CLASSIFIABLE aspect not present", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_CLASSIFIABLE));
               
               //assertEquals("noderef changed", testContext.testNodeRef, shuffledNodeRef);
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);

    } // testScenarioMSWord2003SaveShuffleWithBackup
    
    /**
     * This test tries to simulate the cifs shuffling that is done to
     * support MS Word 2007 
     * 
     * a) TEST.DOCX
     * b) Save new to 00000001.TMP
     * c) Rename TEST.DOCX to 00000002.TMP
     * d) Rename 000000001.TMP to TEST.DOCX
     * e) Delete 000000002.TMP
     */
    public void testScenarioMSWord2007Save() throws Exception
    {
        logger.debug("testScenarioMSWord2007SaveShuffle");
        final String FILE_NAME = "TEST.DOCX";
        final String FILE_OLD_TEMP = "788A1D3D.tmp";
        final String FILE_NEW_TEMP = "19ECA1A.tmp";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMSWord2007Save";
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Create a file in the test directory
         */           
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // no need to test lots of different properties, that's already been tested above
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        /**
         * a) Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "MS Word 2007 shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();            
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * b) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "MS Word 2007 shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.newFileHandle.close();
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB, false, true);
        
        /**
         * c) rename the old file
         */
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
        
        
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(deleteOldFileCB, false, true);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );    
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);

    } // testScenarioWord2007 save
    
    /**
     * This test tries to simulate the cifs shuffling that is done to
     * support EMACS 
     * 
     * a) emacsTest.txt
     * b) Rename original file to emacsTest.txt~
     * c) Create emacsTest.txt
     */
    public void DISABLED_testScenarioEmacsSave() throws Exception
    {
        logger.debug("testScenarioEmacsSave");
        final String FILE_NAME = "emacsTest.txt";
        final String FILE_OLD_TEMP = "emacsTest.txt~";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioEmacsSave";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
                
        /**
         * Create a file in the test directory
         */    
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // no need to test lots of different properties, that's already been tested above
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB);
        
        /**
         * a) Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "Emacs shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();            
                return null;
            }
        };
        tran.doInTransaction(writeFileCB);
        
        /**
         * b) rename the old file out of the way
         */
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB);
        
        /**
         * c) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "EMACS shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.newFileHandle.close();
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               return null;
            }
        };
        
        tran.doInTransaction(validateCB);

    } // testScenarioEmacs save

    /**
     * This test tries to simulate the cifs shuffling that is done to
     * support vi 
     * 
     * a) viTest.txt
     * b) Rename original file to viTest.txt~
     * c) Create viTest.txt
     * d) Delete viTest.txt~
     */
    public void testScenarioViSave() throws Exception
    {
        logger.debug("testScenarioViSave");
        final String FILE_NAME = "viTest.txt";
        final String FILE_OLD_TEMP = "viTest.txt~";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioViSave";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
            
        /**
         * Create a file in the test directory
         */            
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // no need to test lots of different properties, that's already been tested above
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB);
        
        /**
         * a) Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "Emacs shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);            
                return null;
            }
        };
        tran.doInTransaction(writeFileCB);
        
        /**
         * b) rename the old file out of the way
         */
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB);
        
        /**
         * c) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "Vi shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                driver.closeFile(testSession, testConnection, testContext.newFileHandle);            
                logger.debug("delete temporary file - which will trigger shuffle");
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               assertNotNull("shuffledNodeRef is null", shuffledNodeRef);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );   
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
 
               return null;
            }
        };
        
        tran.doInTransaction(validateCB);

    } // testScenarioViSave
    
    /**
     * This test tries to simulate the cifs shuffling that is done to
     * support smultron 
     * 
     * a) smultronTest.txt
     * b) Save new file to .dat04cd.004
     * c) Delete smultronTest.txt
     * c) Rename .dat04cd.004 to smultronTest.txt
     */
    public void testScenarioSmultronSave() throws Exception
    {
        logger.debug("testScenarioSmultronSave");
        final String FILE_NAME = "smultronTest.txt";
        final String FILE_NEW_TEMP = ".dat04cd.004";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioSmultronSave";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
          
        /**
         * Create a file in the test directory
         */           
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // no need to test lots of different properties, that's already been tested above
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB);
        
        /**
         * a) Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "Smultron shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);            
                return null;
            }
        };
        tran.doInTransaction(writeFileCB);
        
        /**
         * b) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "Smultron shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                driver.closeFile(testSession, testConnection, testContext.newFileHandle);
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB);
        
        /**
         * c) Delete the old file
         */
        RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(deleteOldFileCB);
          
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );    
               return null;
            }
        };
        
        tran.doInTransaction(validateCB);
        
    } // testScenarioSmultronSave

    
    /**
     * This time we create a file through the ContentDiskDriver and then delete it 
     * through the repo.   We check its no longer found by the driver.
     */
    public void testScenarioDeleteViaNodeService() throws Exception
    {
        logger.debug("testScenarioDeleteViaNodeService");
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
              
        int openAction = FileAction.CreateNotExist;
        final String FILE_NAME="testDeleteFileViaNodeService.new";
        final String FILE_PATH="\\" + FILE_NAME;

          
        FileOpenParams params = new FileOpenParams(FILE_PATH, openAction, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                
        final NetworkFile file = driver.createFile(testSession, testConnection, params);
        
        assertNotNull("file is null", file);
        assertFalse("file is read only, should be read-write", file.isReadOnly());
              
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {

                byte[] stuff = "Hello World".getBytes();
                file.writeFile(stuff, stuff.length, 0, 0);
                file.close();
                
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                assertNotNull("can't find new node", newNode);
             
                     
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * Step 1: Delete the new node via the node service
         */
        RetryingTransactionCallback<Void> deleteNodeCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {

                
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                assertNotNull("can't find new node", newNode);
                nodeService.deleteNode(newNode);
                return null;
            }
        };
        tran.doInTransaction(deleteNodeCB, false, true);
        
        try
        {
            getNodeForPath(testConnection, FILE_PATH);
            fail("getNode for path unexpectedly succeeded");
        } 
        catch (IOException ie)
        {
            // expect to go here
        }
        
        /**
         * Delete file by path - file should no longer exist
         */
        try
        {
            driver.deleteFile(testSession, testConnection, FILE_PATH);
            fail("delete unexpectedly succeeded");
        } 
        catch (IOException ie)
        {
            // expect to go here
        }
        
    }
    
    /**
     * This test tries to simulate the shuffling that is done by MS Word 2003 
     * with regard to metadata extraction.
     * <p>
     * 1: Setup an inbound rule for ContentMetadataExtractor.
     * 2: Write ContentDiskDriverTest1 file to ContentDiskDriver.docx
     * 3: Check metadata extraction for non update test
     * Simulate a WORD 2003 CIFS shuffle
     * 4: Write ContentDiskDriverTest2 file to ~WRD0003.TMP
     * 5: Rename ContentDiskDriver.docx to ~WRL0003.TMP
     * 6: Rename ~WRD0003.TMP to ContentDiskDriver.docx
     * 7: Check metadata extraction
     */
    public void testMetadataExtraction() throws Exception
    {
        logger.debug("testMetadataExtraction");
        final String FILE_NAME = "ContentDiskDriver.docx";
        final String FILE_OLD_TEMP = "~WRL0003.TMP";
        final String FILE_NEW_TEMP = "~WRD0003.TMP";
         
        class TestContext
        {
            NodeRef testDirNodeRef;
            NodeRef testNodeRef;
            NetworkFile firstFileHandle;
            NetworkFile secondFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testMetadataExtraction";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteDirectory(testSession, testConnection, TEST_DIR);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(deleteGarbageDirCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("create Test directory" + TEST_DIR);
        RetryingTransactionCallback<Void> createTestDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_DIR);
                assertNotNull("testDirNodeRef is null", testContext.testDirNodeRef);   
                
                UserTransaction txn = transactionService.getUserTransaction();
              
                return null;
                
                
            }
        };                
        tran.doInTransaction(createTestDirCB);
        logger.debug("Create rule on test dir");
        
        RetryingTransactionCallback<Void> createRuleCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {                 
                Rule rule = new Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.applyToChildren(true);
                rule.setRuleDisabled(false);
                rule.setTitle("Extract Metadata from content");
                rule.setDescription("ContentDiskDriverTest");
                
                Map<String, Serializable> props = new HashMap<String, Serializable>(1);
                Action extractAction = actionService.createAction("extract-metadata", props);
                
                ActionCondition noCondition1 = actionService.createActionCondition(NoConditionEvaluator.NAME);
                extractAction.addActionCondition(noCondition1);
                
                ActionCondition noCondition2 = actionService.createActionCondition(NoConditionEvaluator.NAME);
                CompositeAction compAction = actionService.createCompositeAction();
                compAction.setTitle("Extract Metadata");
                compAction.setDescription("Content Disk Driver Test - Extract Metadata");
                compAction.addAction(extractAction);
                compAction.addActionCondition(noCondition2);

                rule.setAction(compAction);           
                         
                ruleService.saveRule(testContext.testDirNodeRef, rule);
                
                logger.debug("rule created");
                     
                return null;
            }
        };
        tran.doInTransaction(createRuleCB, false, true);

        /**
         * Create a file in the test directory
         */  
        logger.debug("create test file in test directory");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {               
                /**
                 * Create the file we are going to use to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // now load up the node with lots of other stuff that we will test to see if it gets preserved during the
                // shuffle.
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                assertNotNull("testContext.testNodeRef is null", testContext.testNodeRef);
                
                // test non CM namespace property
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        logger.debug("step b: write content to test file");
        
        /**
         * Write ContentDiskDriverTest1.docx to the test file,
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest1.docx");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest1.docx", fileResource);
                writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
            
                logger.debug("close the file, firstFileHandle");
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        logger.debug("Step c: validate metadata has been extracted.");
        
        /**
         * c: check simple case of meta-data extraction has worked.
         */
        RetryingTransactionCallback<Void> validateFirstExtractionCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> props = nodeService.getProperties(testContext.testNodeRef);
                
                assertTrue("Enabled property has been lost", props.containsKey(TransferModel.PROP_ENABLED));
                
                ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                assertEquals("size is wrong", 11302, data.getSize());
                assertEquals("mimeType is wrong", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", data.getMimetype());
                
                // These metadata values should be extracted.
                assertEquals("description is not correct", "This is a test file", nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_DESCRIPTION));
                assertEquals("title is not correct", "ContentDiskDriverTest", nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_TITLE));
                assertEquals("author is not correct", "mrogers", nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_AUTHOR));
                
    
                        
                return null;
            }
        };
        tran.doInTransaction(validateFirstExtractionCB, false, true);
        
        
        /**
         * d: Save the new file as an update file in the test directory
         */
        logger.debug("Step d: create update file in test directory " + FILE_NEW_TEMP);
        RetryingTransactionCallback<Void> createUpdateFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {               
                /**
                 * Create the file we are going to use to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.secondFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.secondFileHandle);
                  
                return null;
            }
        };
        tran.doInTransaction(createUpdateFileCB, false, true);

        RetryingTransactionCallback<Void> writeFile2CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest2.docx");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest2.docx", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.secondFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
            
                driver.closeFile(testSession, testConnection, testContext.secondFileHandle);
                    
                return null;
            }
        };
        tran.doInTransaction(writeFile2CB, false, true);
        
        /**
         * rename the old file
         */
        logger.debug("move old file out of the way.");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
  
        /**
         * Check the old file has gone.
         */
        RetryingTransactionCallback<Void> validateOldFileGoneCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                try
                {
                    driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                }
                catch (IOException e)
                { 
                    // expect to go here since previous step renamed the file.
                }
                
                return null;
            }
        };
        tran.doInTransaction(validateOldFileGoneCB, false, true);
        
//        /**
//         * Check metadata extraction on intermediate new file
//         */
//        RetryingTransactionCallback<Void> validateIntermediateCB = new RetryingTransactionCallback<Void>() {
//
//            @Override
//            public Void execute() throws Throwable
//            {
//               NodeRef updateNodeRef = driver.getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP);
//               
//               Map<QName, Serializable> props = nodeService.getProperties(updateNodeRef);
//                        
//               // These metadata values should be extracted from file2.
//               assertEquals("intermediate file description is not correct", "Content Disk Test 2", props.get(ContentModel.PROP_DESCRIPTION));
//               assertEquals("intermediate file title is not correct", "Updated", props.get(ContentModel.PROP_TITLE));
//               assertEquals("intermediate file author is not correct", "mrogers", props.get(ContentModel.PROP_AUTHOR));
//
//               return null;
//            }
//        };
//        
//        tran.doInTransaction(validateIntermediateCB, true, true);
        
        /**
         * Move the new file into place, stuff should get shuffled
         */
        logger.debug("move new file into place.");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        logger.debug("validate update has run correctly.");
        RetryingTransactionCallback<Void> validateUpdateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               // Check trx:enabled has been shuffled and not lost.
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertEquals("mimeType is wrong", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", data.getMimetype());
               assertEquals("size is wrong", 11265, data.getSize());
           
               // These metadata values should be extracted from file2.   However they will not be applied in PRAGMATIC mode.
//               assertEquals("description is not correct", "Content Disk Test 2", props.get(ContentModel.PROP_DESCRIPTION));
//               assertEquals("title is not correct", "Updated", props.get(ContentModel.PROP_TITLE));
//               assertEquals("author is not correct", "mrogers", props.get(ContentModel.PROP_AUTHOR));
               
                    return null;
            }
        };
        
        tran.doInTransaction(validateUpdateCB, true, true);
        
    } // testScenarioShuffleMetadataExtraction
    
    
    /**
     * ALF-12812
     * 
     * This test tries to simulate the shuffling that is done by MS Word 2011 for Mac 
     * with regard to metadata extraction.  In particular the temporary file names are
     * different.
     * <p>
     * 1: Setup an update rule for ContentMetadataExtractor.
     * Simulate a WORD 2011 for Mac Create
     * 2: Write "Word Work File D_1725484373.tmp"
     * 3: Close file
     * 4: Rename "Word Work File D_1725484373.tmp" to ContentDiskDriver.docx
     * 5: Check metadata extraction
     */
    public void testMetadataExtractionForMac() throws Exception
    {
        logger.debug("testMetadataExtractionForMac");
        final String FILE_NAME = "ContentDiskDriver.docx";
        //final String FILE_OLD_TEMP = "._Word Work File D_1725484373.tmp";
        final String FILE_NEW_TEMP = "Word Work File D_1725484373.tmp";
                 
        class TestContext
        {
            NodeRef testDirNodeRef;
            NodeRef testNodeRef;
            NetworkFile firstFileHandle;
//            NetworkFile secondFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testMetadataExtractionForMac";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteDirectory(testSession, testConnection, TEST_DIR);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(deleteGarbageDirCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("create Test directory" + TEST_DIR);
        RetryingTransactionCallback<Void> createTestDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_DIR);
                assertNotNull("testDirNodeRef is null", testContext.testDirNodeRef);   
                
                UserTransaction txn = transactionService.getUserTransaction();
              
                return null;
                
                
            }
        };                
        tran.doInTransaction(createTestDirCB);
        logger.debug("Create rule on test dir");
        
        RetryingTransactionCallback<Void> createRuleCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {                 
                Rule rule = new Rule();
                rule.setRuleType(RuleType.UPDATE);
                rule.applyToChildren(true);
                rule.setRuleDisabled(false);
                rule.setTitle("Extract Metadata from update content");
                rule.setDescription("ContentDiskDriverTest");
                
                Map<String, Serializable> props = new HashMap<String, Serializable>(1);
                Action extractAction = actionService.createAction("extract-metadata", props);
                
                ActionCondition noCondition1 = actionService.createActionCondition(NoConditionEvaluator.NAME);
                extractAction.addActionCondition(noCondition1);
                
                ActionCondition noCondition2 = actionService.createActionCondition(NoConditionEvaluator.NAME);
                CompositeAction compAction = actionService.createCompositeAction();
                compAction.setTitle("Extract Metadata");
                compAction.setDescription("Content Disk Driver Test - Extract Metadata");
                compAction.addAction(extractAction);
                compAction.addActionCondition(noCondition2);

                rule.setAction(compAction);           
                         
                ruleService.saveRule(testContext.testDirNodeRef, rule);
                
                logger.debug("rule created");
                     
                return null;
            }
        };
        tran.doInTransaction(createRuleCB, false, true);

        /**
         * Create a file in the test directory
         */  
        logger.debug("create test file in test directory");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {               
                /**
                 * Create the file we are going to use to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull("first file Handle is null", testContext.firstFileHandle);
                
                // now load up the node with lots of other stuff that we will test to see if it gets preserved during the
                // shuffle.
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP);
                assertNotNull("testContext.testNodeRef is null", testContext.testNodeRef);
                
                // test non CM namespace property
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
        
                // Check that the temporary aspect has been applied.
                assertTrue("temporary aspect not applied", nodeService.hasAspect(testContext.testNodeRef, ContentModel.ASPECT_TEMPORARY));
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        logger.debug("step b: write content to test file");
        
        /**
         * Write ContentDiskDriverTest1.docx to the test file,
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest1.docx");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest1.docx", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.firstFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
                
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        logger.debug("Step b: rename the test file.");
                
        /**
         * Move the new file into place, stuff should get shuffled
         */
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
      logger.debug("Step c: validate metadata has been extracted.");  
      /**
      * c: check simple case of meta-data extraction has worked.
      */
     RetryingTransactionCallback<Void> validateFirstExtractionCB = new RetryingTransactionCallback<Void>() {

         @Override
         public Void execute() throws Throwable
         {
             Map<QName, Serializable> props = nodeService.getProperties(testContext.testNodeRef);
             
             assertTrue("Enabled property has been lost", props.containsKey(TransferModel.PROP_ENABLED));
             
             // Check that the temporary aspect has been applied.
             assertTrue("temporary aspect has not been removed", !nodeService.hasAspect(testContext.testNodeRef, ContentModel.ASPECT_TEMPORARY));
             assertTrue("hidden aspect has not been removed", !nodeService.hasAspect(testContext.testNodeRef, ContentModel.ASPECT_HIDDEN));

        
          
             // These metadata values should be extracted.
             assertEquals("description is not correct", "This is a test file", nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_DESCRIPTION));
             assertEquals("title is not correct", "ContentDiskDriverTest", nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_TITLE));
             assertEquals("author is not correct", "mrogers", nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_AUTHOR));
             
             ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
             assertEquals("mimeType is wrong", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", data.getMimetype());
             assertEquals("size is wrong", 11302, data.getSize());
                     
             return null;
         }
     };
     tran.doInTransaction(validateFirstExtractionCB, false, true);

        
    } // testScenarioMetadataExtractionForMac
    
    public void testDirListing()throws Exception
    {
        logger.debug("testDirListing");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        final String FOLDER_NAME = "parentFolder" + System.currentTimeMillis();
        final String HIDDEN_FOLDER_NAME = "hiddenFolder" + System.currentTimeMillis();
        RetryingTransactionCallback<NodeRef> createNodesCB = new RetryingTransactionCallback<NodeRef>() {

            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef parentNode = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, FOLDER_NAME), ContentModel.TYPE_FOLDER).getChildRef();
                nodeService.setProperty(parentNode, ContentModel.PROP_NAME, FOLDER_NAME);
                
                NodeRef hiddenNode = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, HIDDEN_FOLDER_NAME), ForumModel.TYPE_FORUM).getChildRef();
                nodeService.setProperty(hiddenNode, ContentModel.PROP_NAME, HIDDEN_FOLDER_NAME);
                return parentNode;
            }
        };
        final NodeRef parentFolder = tran.doInTransaction(createNodesCB);
        
        List<String> excludedTypes = new ArrayList<String>();
        excludedTypes.add(ForumModel.TYPE_FORUM.toString());
        cifsHelper.setExcludedTypes(excludedTypes);
        SearchContext result = driver.startSearch(testSession, testConnection, "\\"+FOLDER_NAME + "\\*", 0);
        while(result.hasMoreFiles())
        {
            if (result.nextFileName().equals(HIDDEN_FOLDER_NAME))
            {
                fail("Exluded types mustn't be shown in cifs");    
            } 
        }

        RetryingTransactionCallback<Void> deleteNodeCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(parentFolder);
                return null;
            }
        };
        tran.doInTransaction(deleteNodeCB, false, true);
    } //testDirListing


    public void testFileInformationUpdatingByEditorUserForAlf8808() throws Exception
    {
        final Holder<org.alfresco.service.cmr.model.FileInfo> editorFolder = new Holder<org.alfresco.service.cmr.model.FileInfo>();
        final Holder<org.alfresco.service.cmr.model.FileInfo> testFile = new Holder<org.alfresco.service.cmr.model.FileInfo>();

        // Configuring test server with test server configuration and getting test tree connection for test shared device
        ServerConfiguration config = new ServerConfiguration(ContentDiskDriverTest.TEST_SERVER_NAME);
        TestServer server = new TestServer(ContentDiskDriverTest.TEST_SERVER_NAME, config);
        DiskSharedDevice device = getDiskSharedDevice();
        final TreeConnection treeConnection = server.getTreeConnection(device);

        // Getting target entity for testing - ContentDiskDriver
        final ExtendedDiskInterface deviceInterface = (ExtendedDiskInterface) treeConnection.getInterface();
        // Creating mock-session
        final SrvSession session = new TestSrvSession(13, server, ContentDiskDriverTest.TEST_PROTOTYPE_NAME, ContentDiskDriverTest.TEST_REMOTE_NAME);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    NodeRef rootNode = repositoryHelper.getCompanyHome();
                    // Creating test user to invite him as Editor for test content. This user will be created correctly (with person and authentication options)
                    createUser(ContentDiskDriverTest.TEST_USER_AUTHORITY, ContentDiskDriverTest.TEST_USER_AUTHORITY, rootNode);
                    // Safely creating folder for test content
                    editorFolder.value = getOrCreateNode(rootNode, PermissionService.EDITOR, ContentModel.TYPE_FOLDER).getFirst();
                    // Creating test content which will be editable by user created above
                    testFile.value = getOrCreateNode(rootNode, "Test.txt", ContentModel.TYPE_CONTENT).getFirst();

                    // Applying 'Editor' role for test user to test file
                    permissionService.setPermission(testFile.value.getNodeRef(), ContentDiskDriverTest.TEST_USER_AUTHORITY, PermissionService.EDITOR, true);

                    try
                    {
                        // Creating data for target method invocation
                        final FileInfo updatedInfo = new FileInfo();
                        updatedInfo.setFileName(testFile.value.getName());
                        updatedInfo.setFileId(DefaultTypeConverter.INSTANCE.intValue(testFile.value.getProperties().get(ContentModel.PROP_NODE_DBID)));

                        // Testing ContentDiskDriver.setFileInformation() with test user authenticated who has 'Editor' role for test content.
                        // This method should fail if check on 'DELETE' permission was not moved to 'DeleteOnClose' context
                        AuthenticationUtil.runAs(new RunAsWork<Void>()
                        {
                            @Override
                            public Void doWork() throws Exception
                            {
                                deviceInterface.setFileInformation(session, treeConnection, testFile.value.getName(), updatedInfo);
                                return null;
                            }
                        }, ContentDiskDriverTest.TEST_USER_AUTHORITY);
                    }
                    catch (Exception e)
                    {
                        // Informing about test failure. Expected exception is 'org.alfresco.jlan.server.filesys.AccessDeniedException'
                        if (e.getCause() instanceof AccessDeniedException)
                        {
                            fail("For user='" + TEST_USER_AUTHORITY + "' " + e.getCause().toString());
                        }
                        else
                        {
                            fail("Unexpected exception was caught: " + e.toString());
                        }
                    }
                }
                finally
                {
                    // Cleaning all test data and rolling back transaction to revert all introduced changes during testing

                    if (authenticationService.authenticationExists(ContentDiskDriverTest.TEST_USER_AUTHORITY))
                    {
                        authenticationService.deleteAuthentication(ContentDiskDriverTest.TEST_USER_AUTHORITY);
                    }

                    if (personService.personExists(ContentDiskDriverTest.TEST_USER_AUTHORITY))
                    {
                        personService.deletePerson(ContentDiskDriverTest.TEST_USER_AUTHORITY);
                    }

                    try
                    {
                        if (null != testFile.value)
                        {
                            nodeService.deleteNode(testFile.value.getNodeRef());
                        }
                    }
                    catch (Exception e)
                    {
                        // Doing nothing
                    }

                    try
                    {
                        if (null != editorFolder.value)
                        {
                            nodeService.deleteNode(editorFolder.value.getNodeRef());
                        }
                    }
                    catch (Exception e)
                    {
                        // Doing nothing
                    }
                }

                return null;
            }
        }, false, true);
    }

    /**
     * Searching for file object with specified name or creating new one if such object is not exist
     * 
     * @param parentRef - {@link NodeRef} of desired parent object
     * @param name - {@link String} value for name of desired file object
     * @param type - {@link QName} instance which determines type of the object. It may be cm:content, cm:folder etc (see {@link ContentModel})
     * @return {@link Pair}&lt;{@link org.alfresco.service.cmr.model.FileInfo}, {@link Boolean}> instance which contains {@link NodeRef} of newly created object and
     *         <code>true</code> value if file object with specified name was not found or {@link NodeRef} of existent file object and <code>false</code> in other case
     */
    private Pair<org.alfresco.service.cmr.model.FileInfo, Boolean> getOrCreateNode(NodeRef parentRef, String name, QName type)
    {
        NodeRef result = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, name);
        Boolean created = false;
        if (null == result)
        {
            result = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CHILDREN, name);
        }
        if (created = (null == result))
        {
            result = fileFolderService.create(parentRef, name, type).getNodeRef();
        }
        return new Pair<org.alfresco.service.cmr.model.FileInfo, Boolean>(fileFolderService.getFileInfo(result), created);
    }

    /**
     * Creates correct user entity with correct user home space, person and authentication with password equal to '<code>password</code>' options if these options are not exist.
     * Method searches for space with name equal to '<code>name</code>' to make it user home space or creates new folder with name equal to '<code>name</code>'. All required
     * permissions and roles will be applied to user home space
     * 
     * @param name - {@link String} value which contains new user name
     * @param password - {@link String} value of text password for new user
     * @param parentNodeRef - {@link NodeRef} instance of parent folder where user home space should be found or created
     */
    private void createUser(String name, String password, NodeRef parentNodeRef)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, name);
        Pair<org.alfresco.service.cmr.model.FileInfo, Boolean> userHome = getOrCreateNode(parentNodeRef, name, ContentModel.TYPE_FOLDER);
        if (userHome.getSecond())
        {
            NodeRef nodeRef = userHome.getFirst().getNodeRef();
            permissionService.setPermission(nodeRef, name, permissionService.getAllPermission(), true);
            permissionService.setPermission(nodeRef, permissionService.getAllAuthorities(), PermissionService.CONSUMER, true);
            permissionService.setPermission(nodeRef, permissionService.getOwnerAuthority(), permissionService.getAllPermission(), true);
            ownableService.setOwner(nodeRef, name);
            permissionService.setInheritParentPermissions(nodeRef, false);

            properties.put(ContentModel.PROP_HOMEFOLDER, nodeRef);
            if (!personService.personExists(name))
            {
                personService.createPerson(properties);
            }
            if (!authenticationService.authenticationExists(name))
            {
                authenticationService.createAuthentication(name, password.toCharArray());
            }
        }
    }
    
    /**
     * Excel 2003 With Versionable file
     *
     * CreateFile 5EE27100
     * RenameFile oldPath:\Espaces Utilisateurs\System\Cherries.xls, 
     *          newPath:\Espaces Utilisateurs\System\Cherries.xls~RF172f241.TMP
     * RenameFile oldName=\Espaces Utilisateurs\System\5EE27100, 
     *          newName=\Espaces Utilisateurs\System\Cherries.xls, session:WNB0
     *
     * Set Delete On Close for Cherries.xls~RF172f241.TMP
     */
    public void testExcel2003SaveShuffle() throws Exception
    {
        //fail("not yet implemented");
        logger.debug("testScenarioExcel2003SaveShuffle");
        final String FILE_NAME = "Cherries.xls";
        final String FILE_TITLE = "Cherries";
        final String FILE_DESCRIPTION = "This is a test document to test CIFS shuffle";
        final String FILE_OLD_TEMP = "Cherries.xls~RF172f241.TMP";
        final String FILE_NEW_TEMP = "5EE27100";
        
        final QName RESIDUAL_MTTEXT = QName.createQName("{gsxhjsx}", "whatever");
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;
            NetworkFile oldFileHandle;
            
            NodeRef testNodeRef;   // node ref of test.doc
            
            Serializable testCreatedDate;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMSExcel2003SaveShuffle";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
  
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // now load up the node with lots of other stuff that we will test to see if it gets preserved during the
                // shuffle.
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);

                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                // test non CM namespace property
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                // test CM property not related to an aspect
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_ADDRESSEE, "Fred");
                
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_TITLE, FILE_TITLE);
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_DESCRIPTION, FILE_DESCRIPTION);
                
                /**
                 * MLText value - also a residual value in a non cm namespace
                 */
                MLText mltext = new MLText();
                mltext.addValue(Locale.FRENCH, "Bonjour");
                mltext.addValue(Locale.ENGLISH, "Hello");
                mltext.addValue(Locale.ITALY, "Buongiorno");
                mlAwareNodeService.setProperty(testContext.testNodeRef, RESIDUAL_MTTEXT, mltext);

                // classifiable chosen since its not related to any properties.
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_CLASSIFIABLE, null);
                //nodeService.createAssociation(testContext.testNodeRef, targetRef, assocTypeQName);
        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        /**
         * Write some content to the test file
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "MS Excel 2003 shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();   
                
                testContext.testCreatedDate = nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_CREATED);
                
                MLText multi = (MLText)mlAwareNodeService.getProperty(testContext.testNodeRef, RESIDUAL_MTTEXT) ;
                multi.getValues();
     
     
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * b) Save the new file
         */
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "MS Word 2003 shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.newFileHandle.close();
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB, false, true);
        
        /**
         * rename the old file
         */
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
        

        RetryingTransactionCallback<Void> validateOldFileGoneCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                try
                {
                    driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                }
                catch (IOException e)
                { 
                    // expect to go here since previous step renamed the file.
                }
                
                return null;
            }
        };
        tran.doInTransaction(validateOldFileGoneCB, false, true);
        
        /**
         * Move the new file into place, stuff should get shuffled
         */
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
           
               // Check trx:enabled has been shuffled.
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               // check my residual MLText has been transferred
               assertTrue(props.containsKey(RESIDUAL_MTTEXT));
               
               // Check the titled aspect is correct
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );
               assertEquals("title wrong", FILE_TITLE, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_TITLE) );
               assertEquals("description wrong", FILE_DESCRIPTION, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_DESCRIPTION) );

               // commented out due to ALF-7641
               // CIFS shuffle, does not preseve MLText values.
               // Map<QName, Serializable> mlProps = mlAwareNodeService.getProperties(shuffledNodeRef);
               
               // MLText multi = (MLText)mlAwareNodeService.getProperty(shuffledNodeRef, RESIDUAL_MTTEXT) ;
               // multi.getValues();
               
               // check auditable properties 
               // commented out due to ALF-7635
               // assertEquals("creation date not preserved", ((Date)testContext.testCreatedDate).getTime(), ((Date)nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_CREATED)).getTime());
               
               // commented out due to ALF-7628 
               // assertEquals("ADDRESSEE PROPERTY Not copied", "Fred", nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_ADDRESSEE));
               // assertTrue("CLASSIFIABLE aspect not present", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_CLASSIFIABLE));
               
               // commented out due to ALF-7584.
               // assertEquals("noderef changed", testContext.testNodeRef, shuffledNodeRef);
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        
        tran.doInTransaction(deleteOldFileCB, false, true);
        
    }

    /**
     * Excel 2003 CSV file with Versionable file 
     * 
     * CreateFile csv.csv and 5EE27101 
     * Add versionable aspect 
     * RenameFile oldPath:\Espaces Utilisateurs\System\csv.csv, newPath:\Espaces\Utilisateurs\System\5EE27101 
     * CreateFile name=\Espaces Utilisateurs\System\csv.csv
     * Add content
     */
    public void testCSVExcel2003SaveShuffle() throws Exception
    {
        logger.debug("testCSVExcel2003SaveShuffle");
        final String FILE_NAME = "csv.csv";
        final String FILE_TITLE = "csv";
        final String FILE_DESCRIPTION = "This is a test document to test CIFS shuffle";
        final String FILE_TEMP = "AAAA0000";

        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;

            NodeRef testNodeRef;

            Serializable testCreatedDate;
        }
        ;

        final TestContext testContext = new TestContext();

        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testMSExcel2003CSVShuffle";

        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        /**
         * Clean up from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };

        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }

        /**
         * Create a file in the test directory
         */
        RetryingTransactionCallback<Void> createTestFileFirstTime = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {

                /**
                 * Create the test directory we are going to use
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);

                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);

                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);

                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_TITLE, FILE_TITLE);
                nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_DESCRIPTION, FILE_DESCRIPTION);

                return null;
            }
        };
        tran.doInTransaction(createTestFileFirstTime, false, true);

        /**
         * Write some content to the test file. Add versionable aspect
         */
        RetryingTransactionCallback<Void> writeToTestFileAndAddVersionableAspect = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                String testContent = "MS Excel 2003 for CSV shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();

                testContext.testCreatedDate = nodeService.getProperty(testContext.testNodeRef, ContentModel.PROP_CREATED);

                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, Collections
                        .<QName, Serializable> singletonMap(ContentModel.PROP_VERSION_TYPE,
                                org.alfresco.service.cmr.version.VersionType.MINOR));

                return null;
            }
        };
        tran.doInTransaction(writeToTestFileAndAddVersionableAspect, false, true);

        /**
         * rename the test file to the temp
         */
        RetryingTransactionCallback<Void> renameTestFileToTemp = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameTestFileToTemp, false, true);

        /**
         * create the test file one more
         */
        RetryingTransactionCallback<Void> createTestFileOneMore = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {

                FileOpenParams params = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, FileAction.TruncateExisting, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                NetworkFile file = driver.createFile(testSession, testConnection, params);
                driver.closeFile(testSession, testConnection, file);

                return null;
            }
        };
        tran.doInTransaction(createTestFileOneMore, false, true);
        
      /**
      * Write the new content
      */
     RetryingTransactionCallback<Void> writeUpdate = new RetryingTransactionCallback<Void>()
     {

         @Override
         public Void execute() throws Throwable
         {
             FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
             testContext.newFileHandle = driver.openFile(testSession, testConnection, createFileParams);
             assertNotNull(testContext.newFileHandle);
             String testContent = "MS Word 2003 for CSV shuffle test This is new content";
             byte[] testContentBytes = testContent.getBytes();
             testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
             testContext.newFileHandle.close();

             return null;
         }
     };
     tran.doInTransaction(writeUpdate, false, true);

        // Check results
        RetryingTransactionCallback<Void> validate = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);

                // Check versionable aspect, version label and nodeRef
                assertTrue("VERSIONABLE aspect not present", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                assertEquals("nodeRef changed", testContext.testNodeRef, shuffledNodeRef);
    
                // Check the titled aspect is correct
                assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME));
                assertEquals("title wrong", FILE_TITLE, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_TITLE));
                assertEquals("description wrong", FILE_DESCRIPTION, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_DESCRIPTION));

                return null;
            }
        };

        tran.doInTransaction(validate, true, true);

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteTestFile = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };

        tran.doInTransaction(deleteTestFile, false, true);

    }

    /**
     * Simulates a SaveAs from Word2003
     * 1. Create new document SAVEAS.DOC, file did not exist
     * 2. Create -WRDnnnn.TMP file, where 'nnnn' is a 4 digit sequence to make the name unique
     * 3. Rename SAVEAS.DOC to Backup of SAVEAS.wbk
     * 4. Rename -WRDnnnn.TMP to SAVEAS.DOC 
     */
    public void testScenarioMSWord2003SaveAsShuffle() throws Exception
    {
        logger.debug("testScenarioMSWord2003SaveShuffle");
        final String FILE_NAME = "SAVEAS.DOC";
        final String FILE_OLD_TEMP = "SAVEAS.wbk";
        final String FILE_NEW_TEMP = "~WRD0002.TMP";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMSWord2003SaveAsShuffle";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("a) create new file");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
  
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
             
        /**
         * b) Save the new file
         * Write ContentDiskDriverTest3.doc to the test file,
         */
        logger.debug("b) move new file into place");
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
         
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.firstFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
            
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * c) rename the old file
         */
        logger.debug("c) rename old file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
           
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        logger.debug("d) move new file into place");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        logger.debug("e) validate results");
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertNotNull("data is null", data);
               assertEquals("size is wrong", 26112, data.getSize());
               assertEquals("mimeType is wrong", "application/msword",data.getMimetype());
           
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        
    }
    
    /**
     * Test Open Close File Scenario
     * 
     * 1) open(readOnly)
     * 2) open(readWrite)
     * 3) open(readWrite) - does nothing.
     * 4) close - does nothing
     * 5) close - does nothing
     * 6) close - updates the repo
     */
    public void testScenarioOpenCloseFile() throws Exception
    {    
        logger.debug("start of testScenarioOpenCloseFile");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {
            NodeRef testDirNodeRef;
            NodeRef targetNodeRef;
        };
        
        final TestContext testContext = new TestContext();
        
        final String FILE_NAME="testScenarioOpenFile.txt";
        final String FILE_PATH= TEST_ROOT_DOS_PATH + "\\" + FILE_NAME;
        
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);  
        
        testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_ROOT_DOS_PATH);
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, FILE_PATH);
                return null;
            }
        };
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
       
        /**
         * Step 1: Now create the file through the node service and open it.
         */
        logger.debug("Step 1) Create File and Open file created by node service");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                logger.debug("create file and close it immediatly");
                FileOpenParams createFileParams = new FileOpenParams(FILE_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                NetworkFile dummy = driver.createFile(testSession, testConnection, createFileParams);  
                assertFalse("file is closed after create", dummy.isClosed());
                driver.closeFile(testSession, testConnection, dummy);
                logger.debug("after create and close");
// TODO Bug in JavaNetworkFile                
//                assertTrue("file is not closed after close", dummy.isClosed());
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        testContext.targetNodeRef = getNodeForPath(testConnection, FILE_PATH);
       
        FileOpenParams openRO = new FileOpenParams(FILE_PATH, FileAction.CreateNotExist, AccessMode.ReadOnly, FileAttribute.NTNormal, 0);
        FileOpenParams openRW = new FileOpenParams(FILE_PATH, FileAction.CreateNotExist, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        
        /**
         * First open - read only
         */
        logger.debug("open file1 read only");
        NetworkFile file1 = driver.openFile(testSession, testConnection, openRO);
        assertNotNull(file1);
        assertFalse("file1 is closed", file1.isClosed());
        
        final String testString = "Yankee doodle went to town";
        byte[] stuff = testString.getBytes("UTF-8");
        
        /**
         * Negative test - file is open readOnly
         */
        try
        {
            driver.writeFile(testSession, testConnection, file1, stuff, 0, stuff.length, 0);
            fail("can write to a read only file!");
        }
        catch(Exception e)
        {
            // Expect to go here
        }
        
        logger.debug("open file 2 for read write");
        NetworkFile file2 = driver.openFile(testSession, testConnection, openRW);
        assertNotNull(file2);
        assertFalse("file is closed", file2.isClosed());

        /**
         * Write Some Content 
         */
        driver.writeFile(testSession, testConnection, file2, stuff, 0, stuff.length, 0);
            
        NetworkFile file3 = driver.openFile(testSession, testConnection, openRW);
        assertNotNull(file3);
       
        logger.debug("first close");
        driver.closeFile(testSession, testConnection, file2);
        // assertTrue("node does not have no content aspect", nodeService.hasAspect(testContext.targetNodeRef, ContentModel.ASPECT_NO_CONTENT));
        
        logger.debug("second close");
        driver.closeFile(testSession, testConnection, file3);
//        //assertTrue("node does not have no content aspect", nodeService.hasAspect(testContext.targetNodeRef, ContentModel.ASPECT_NO_CONTENT));
        
//        logger.debug("this should be the last close");
//        driver.closeFile(testSession, testConnection, file1);
//        assertFalse("node still has no content aspect", nodeService.hasAspect(testContext.targetNodeRef, ContentModel.ASPECT_NO_CONTENT));
        
        /**
         * Step 2: Negative test - Close the file again - should do nothing quietly!
         */
        logger.debug("this is a negative test - should do nothing");
        driver.closeFile(testSession, testConnection, file1);
        
        logger.debug("now validate");
         
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                Map<QName,Serializable> props = nodeService.getProperties(testContext.targetNodeRef);
                ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                assertNotNull("data is null", data);
                assertEquals("data wrong length", testString.length(), data.getSize());
           
                ContentReader reader = contentService.getReader(testContext.targetNodeRef, ContentModel.PROP_CONTENT);
                String s = reader.getContentString();
                assertEquals("content not written", testString, s);

                return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);
                
    } // testOpenCloseFileScenario
    
    
    /**
     * Test Open Close File Scenario II  ALF-13401
     * Open Read Only of a file already open for read/write.
     * 
     * 1) open(readWrite)
     * 2) write some content.
     * 3) open(readOnly).
     * 4) read some content.
     * 5) close - updates the repo
     */
    public void testScenarioOpenCloseFileTwo() throws Exception
    {    
        logger.debug("start of testScenarioOpenCloseFileTwo");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {
            NodeRef testDirNodeRef;
            NodeRef targetNodeRef;
        };
        
        final TestContext testContext = new TestContext();
        
        final String FILE_NAME="testScenarioOpenFileTwo.txt";
        final String FILE_PATH= TEST_ROOT_DOS_PATH + "\\" + FILE_NAME;
        
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);  
        
        testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_ROOT_DOS_PATH);
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, FILE_PATH);
                return null;
            }
        };
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
       
        /**
         * Step 1: Now create the file through the node service and open it.
         */
        logger.debug("Step 1) Create File and Open file created by node service");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {   
                logger.debug("create file and close it immediatly");
                FileOpenParams createFileParams = new FileOpenParams(FILE_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                NetworkFile dummy = driver.createFile(testSession, testConnection, createFileParams);    
                driver.closeFile(testSession, testConnection, dummy);
                logger.debug("after create and close");
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        testContext.targetNodeRef = getNodeForPath(testConnection, FILE_PATH);
       
        FileOpenParams openRO = new FileOpenParams(FILE_PATH, FileAction.CreateNotExist, AccessMode.ReadOnly, FileAttribute.NTNormal, 0);
        FileOpenParams openRW = new FileOpenParams(FILE_PATH, FileAction.CreateNotExist, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        
        /**
         * First open - read write 
         */
        logger.debug("open file1 read only");
        NetworkFile file1 = driver.openFile(testSession, testConnection, openRW);
        assertNotNull(file1);
        
        final String testString = "Yankee doodle went to town, riding on a donkey.";
        byte[] stuff = testString.getBytes("UTF-8");
        
        driver.writeFile(testSession, testConnection, file1, stuff, 0, stuff.length, 0);
        
        logger.debug("open file 2 for read only");
        NetworkFile file2 = driver.openFile(testSession, testConnection, openRO);
        assertNotNull(file2);
        
        
        assertTrue("file size is 0", file2.getFileSize() > 0);
        
        
        /**
         * Write Some More Content 
         */
        driver.writeFile(testSession, testConnection, file1, stuff, 0, stuff.length, 0);
                 
        logger.debug("first close");
        driver.closeFile(testSession, testConnection, file2);
        
        logger.debug("second close");
        driver.closeFile(testSession, testConnection, file1);
       
        logger.debug("now validate");
         
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                Map<QName,Serializable> props = nodeService.getProperties(testContext.targetNodeRef);
                ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                assertNotNull("data is null", data);
                assertEquals("data wrong length", testString.length(), data.getSize());
           
                ContentReader reader = contentService.getReader(testContext.targetNodeRef, ContentModel.PROP_CONTENT);
                String s = reader.getContentString();
                assertEquals("content not written", testString, s);

                return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);
                
    } // testOpenCloseFileScenarioTwo


    
    
    /**
     * Unit test of open read/write close versionable file - should not do anything.
     * <p>
     * This is done with a CIFS shuffle from word.  Basically Word holds the file open with a read/write lock while the 
     * shuffle is going on.
     * <p>
     * Create a file.
     * Apply versionable aspect
     * Open the file ReadWrite + OpLocks
     * Close the file
     * Check Version has not incremented.
     */
    public void testOpenCloseVersionableFile() throws Exception
    {  
        logger.debug("testOpenCloseVersionableFile");
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        final String FILE_PATH1=TEST_ROOT_DOS_PATH + "\\OpenCloseFile.new";
        
        class TestContext
        {
        };
        
        final TestContext testContext = new TestContext();
   
        FileOpenParams dirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadOnly, FileAttribute.NTDirectory, 0);
        driver.createDirectory(testSession, testConnection, dirParams);

        FileOpenParams params1 = new FileOpenParams(FILE_PATH1, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
        NetworkFile file1 = driver.createFile(testSession, testConnection, params1);
        driver.closeFile(testSession, testConnection, file1);
        
        /**
         * Make Node 1 versionable
         */   
        RetryingTransactionCallback<Void> makeVersionableCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef file1NodeRef = getNodeForPath(testConnection, FILE_PATH1);
                nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                ContentWriter contentWriter2 = contentService.getWriter(file1NodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent("test open close versionable node");
                
                return null;
            }
        };
        tran.doInTransaction(makeVersionableCB, false, true);
        
        
        RetryingTransactionCallback<String> readVersionCB = new RetryingTransactionCallback<String>() {

            @Override
            public String execute() throws Throwable
            {
                NodeRef shuffledNodeRef = getNodeForPath(testConnection, FILE_PATH1);
                
                Map<QName,Serializable> props = nodeService.getProperties(shuffledNodeRef);
                
                assertTrue("versionable aspect not present", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                props.get(ContentModel.PROP_VERSION_LABEL);


                return (String)props.get(ContentModel.PROP_VERSION_LABEL);
            }
        };
        
        String version = tran.doInTransaction(readVersionCB, false, true);
  
        /**
         * Step 1: Open The file Read/Write 
         * TODO Check primary assoc, peer assocs, child assocs, modified date, created date, nodeid, permissions.
         */
        NetworkFile file = driver.openFile(testSession, testConnection, params1);

        assertNotNull( "file is null", file);
        
        /**
         * Step 2: Close the file
         */
        driver.closeFile(testSession, testConnection, file);
        
        /**
         * Validate that there is no version increment.
         */
        String version2 = tran.doInTransaction(readVersionCB, false, true);
       
        assertEquals("version has incremented", version, version2);
        
        /**
         * Now do an update and check the version increments
         */
        file = driver.openFile(testSession, testConnection, params1);

        assertNotNull( "file is null", file);
        
        byte[] stuff = "Hello World".getBytes();
        driver.writeFile(testSession, testConnection, file, stuff, 0, stuff.length, 0);
        
        /**
         * Step 2: Close the file
         */
        driver.closeFile(testSession, testConnection, file);
        
        String version3 = tran.doInTransaction(readVersionCB, false, true);
        
        assertFalse("version not incremented", version.equals(version3));
         
    } // OpenCloseVersionableFile
    
    /**
    * Frame maker save
    * a) Lock File Created    (X.fm.lck)
    * b) Create new file (X.fm.C29)
    * c) Existing file rename out of the way.   (X.backup.fm)
    * d) New file rename into place. (X.fm.C29)
    * e) Old file deleted (open with delete on close)
    * f) Lock file deleted (open with delete on close)
    */
    public void testScenarioFrameMakerShuffle() throws Exception
    {  
        logger.debug("testScenarioFramemakerShuffle");
        
        final String LOCK_FILE = "X.fm.lck";
        final String FILE_NAME = "X.fm";
        final String FILE_OLD_TEMP = "X.backup.fm";
        final String FILE_NEW_TEMP = "X.fm.C29";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            String mimetype;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioFramemakerShuffle";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("a) create new file");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                ClassPathResource fileResource = new ClassPathResource("filesys/X1.fm");
                assertNotNull("unable to find test resource filesys/X1.fm", fileResource);
                writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                NodeRef file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);

                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
             
        /**
         * b) Save the new file
         * Write X2.fm to the test file,
         */
        logger.debug("b) move new file into place");
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
         
                ClassPathResource fileResource = new ClassPathResource("filesys/X2.fm");
                assertNotNull("unable to find test resource filesys/X2.fm", fileResource);
                writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                
                
                NodeRef file1NodeRef = getNodeForPath(testConnection,  TEST_DIR + "\\" + FILE_NAME);
                Map<QName, Serializable> props = nodeService.getProperties(file1NodeRef);
                ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                assertNotNull("data is null", data);
                assertEquals("size is wrong", 166912, data.getSize());
                testContext.mimetype = data.getMimetype();
                
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * c) rename the old file
         */
        logger.debug("c) rename old file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
           
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        logger.debug("d) move new file into place");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        /**
         * d) Delete the old file
         */
        logger.debug("d) move new file into place");
        RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        
        tran.doInTransaction(deleteOldFileCB, false, true);
        
        logger.debug("e) validate results");
        
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertNotNull("data is null", data);
               assertEquals("size is wrong", 123904, data.getSize());
               
               NodeRef file1NodeRef = getNodeForPath(testConnection,  TEST_DIR + "\\" + FILE_NAME); 
               assertTrue("file has lost versionable aspect", nodeService.hasAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE));

               assertEquals("mimeType is wrong", testContext.mimetype, data.getMimetype());
               
           
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
    }  // Scenario frame maker save
    
    /**
     * Test that rules fire on zero byte long files.
     * In this case check that a new file gets the versionable
     * aspect added.
     */
    public void testZeroByteRules() throws Exception
    {
        logger.debug("testZeroByteRules");
        final String FILE_NAME_ZERO = "Zero.docx";
        final String FILE_NAME_NON_ZERO = "NonZero.docx";
         
        class TestContext
        {
            NodeRef testDirNodeRef;
            NodeRef testZeroNodeRef;
            NodeRef testNonZeroNodeRef;
            NetworkFile firstFileHandle;
            NetworkFile secondFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testZeroByteRules";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteDirectory(testSession, testConnection, TEST_DIR);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(deleteGarbageDirCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("create Test directory" + TEST_DIR);
        RetryingTransactionCallback<Void> createTestDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_DIR);
                assertNotNull("testDirNodeRef is null", testContext.testDirNodeRef);                 
                return null;
                
                
            }
        };                
        tran.doInTransaction(createTestDirCB);
        logger.debug("Create rule on test dir");
        
        RetryingTransactionCallback<Void> createRuleCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {                 
                Rule rule = new Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.applyToChildren(true);
                rule.setRuleDisabled(false);
                rule.setTitle("Make Versionable");
                rule.setDescription("ContentDiskDriverTest Test Zero Byte files");
                
                Map<String, Serializable> props = new HashMap<String, Serializable>(1);
                props.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);
                Action addVersionable = actionService.createAction("add-features", props);
                
                ActionCondition noCondition1 = actionService.createActionCondition(NoConditionEvaluator.NAME);
                addVersionable.addActionCondition(noCondition1);
                
                ActionCondition noCondition2 = actionService.createActionCondition(NoConditionEvaluator.NAME);
                CompositeAction compAction = actionService.createCompositeAction();
                compAction.setTitle("Make Versionablea");
                compAction.setDescription("Add Aspect - Versionable");
                compAction.addAction(addVersionable);
                compAction.addActionCondition(noCondition2);

                rule.setAction(compAction);           
                         
                ruleService.saveRule(testContext.testDirNodeRef, rule);
                
                logger.debug("add aspect versionable rule created");
                     
                return null;
            }
        };
        tran.doInTransaction(createRuleCB, false, true);

        /**
         * Create a file in the test directory
         */  
        logger.debug("create test file in test directory");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {               
                /**
                 * Create the zero byte file we are going to use to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME_ZERO, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                testContext.testZeroNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME_ZERO);
                assertNotNull("testContext.testNodeRef is null", testContext.testZeroNodeRef);
                
                /**
                 * Create the non zero byte file we are going to use to test
                 */
                FileOpenParams createFileParams2 = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME_NON_ZERO, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.secondFileHandle = driver.createFile(testSession, testConnection, createFileParams2);
                assertNotNull(testContext.secondFileHandle);
                
                testContext.testNonZeroNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME_NON_ZERO);
                assertNotNull("testContext.testNodeRef is null", testContext.testNonZeroNodeRef);
        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        logger.debug("step b: close the file with zero byte content");
        
        /**
         * Write ContentDiskDriverTest1.docx to the test file,
         */
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
            
                logger.debug("close the file, firstFileHandle");
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                
                
                // Write hello world into the second file
                byte[] stuff = "Hello World".getBytes();
                driver.writeFile(testSession, testConnection, testContext.secondFileHandle, stuff, 0, stuff.length, 0);
                
                logger.debug("close the second non zero file, secondFileHandle");
                driver.closeFile(testSession, testConnection, testContext.secondFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        logger.debug("Step c: validate versioble aspect has been applied.");
        
        /**
         * c: check zero byte file has the versionable aspect.
         */
        RetryingTransactionCallback<Void> validateFirstExtractionCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                assertTrue("versionable aspect not applied to non zero file.", nodeService.hasAspect(testContext.testNonZeroNodeRef, ContentModel.ASPECT_VERSIONABLE));
                assertTrue("versionable aspect not applied to zero byte file.", nodeService.hasAspect(testContext.testZeroNodeRef, ContentModel.ASPECT_VERSIONABLE));
                return null;
            }
        };
        tran.doInTransaction(validateFirstExtractionCB, false, true);    
    } // testZeroByteRules
    
    /**
     * Test that files can be created with empty content and that
     * existing content can be over-wrriten by empty content.
     */
    public void testEmptyContent() throws Exception
    {
        logger.debug("testEmptyContent");
        final String FILE_NAME_ZERO = "Zero.docx";
        final String FILE_NAME_NON_ZERO = "NonZero.docx";
         
        class TestContext
        {
            NodeRef testDirNodeRef;
            NodeRef testZeroNodeRef;
            NodeRef testNonZeroNodeRef;
            NetworkFile firstFileHandle;
            NetworkFile secondFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testEmptyContent";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteDirectory(testSession, testConnection, TEST_DIR);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(deleteGarbageDirCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("create Test directory" + TEST_DIR);
        RetryingTransactionCallback<Void> createTestDirCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                testContext.testDirNodeRef = getNodeForPath(testConnection, TEST_DIR);
                assertNotNull("testDirNodeRef is null", testContext.testDirNodeRef);                 
                return null;
                
                
            }
        };                
        tran.doInTransaction(createTestDirCB);

        /**
         * Create a file in the test directory
         */  
        logger.debug("create test file in test directory");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {               
                /**
                 * Create the zero byte file we are going to use to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME_ZERO, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                testContext.testZeroNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME_ZERO);
                assertNotNull("testContext.testNodeRef is null", testContext.testZeroNodeRef);
                
                logger.debug("close the file, firstFileHandle");
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                
                /**
                 * Create the non zero byte file we are going to use to test
                 */
                FileOpenParams createFileParams2 = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME_NON_ZERO, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.secondFileHandle = driver.createFile(testSession, testConnection, createFileParams2);
                assertNotNull(testContext.secondFileHandle);
                
                testContext.testNonZeroNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME_NON_ZERO);
                assertNotNull("testContext.testNodeRef is null", testContext.testNonZeroNodeRef);
                
                // Write hello world into the second file
                byte[] stuff = "Hello World".getBytes();
                driver.writeFile(testSession, testConnection, testContext.secondFileHandle, stuff, 0, stuff.length, 0);
                
                logger.debug("close the second non zero file, secondFileHandle");
                driver.closeFile(testSession, testConnection, testContext.secondFileHandle);   
   
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
            
        /**
         * d: check both files have content properties.
         */
        RetryingTransactionCallback<Void> checkContentPropsCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                assertNotNull("content missing create non zero file.", nodeService.getProperty(testContext.testNonZeroNodeRef, ContentModel.PROP_CONTENT));
                assertNotNull("content missing create zero byte file.", nodeService.getProperty(testContext.testZeroNodeRef, ContentModel.PROP_CONTENT));
                return null;
            }
        };
        tran.doInTransaction(checkContentPropsCB, false, true);  
        
        RetryingTransactionCallback<Void> truncateFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {                 
                /**
                 * Truncate the non zero byte file we are going to use to test
                 */
                FileOpenParams createFileParams2 = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME_NON_ZERO, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.secondFileHandle = driver.openFile(testSession, testConnection, createFileParams2);
                assertNotNull(testContext.secondFileHandle);
                
                testContext.testNonZeroNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME_NON_ZERO);
                assertNotNull("testContext.testNodeRef is null", testContext.testNonZeroNodeRef);

                driver.truncateFile(testSession, testConnection, testContext.secondFileHandle, 0);
                
                logger.debug("close the second non zero file, secondFileHandle");
                driver.closeFile(testSession, testConnection, testContext.secondFileHandle);   
   
                return null;
            }
        };
        tran.doInTransaction(truncateFileCB, false, true);
        
        /**
         * d: check both files have content properties.
         */
        RetryingTransactionCallback<Void> checkContentProps2CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testContext.testNonZeroNodeRef, ContentModel.PROP_CONTENT);
                String s = reader.getContentString();
                assertEquals("content not truncated", "", s);
                
                ContentReader reader2 = contentService.getReader(testContext.testZeroNodeRef, ContentModel.PROP_CONTENT);
                String s2 = reader2.getContentString();
                assertEquals("content not empty", "", s2);
                return null;
            }
        };
        tran.doInTransaction(checkContentProps2CB, false, true);    



    } // testEmptyFiles
    
    
    /**
     * Simulates a SaveAs from Word2003 for a checked out file
     * 
     * 1. Create new document TESTFILE.DOC, file did not exist
     * 2. CheckOut TESTFILE.DOC
     * 3. Create -WRDnnnn.TMP file, where 'nnnn' is a 4 digit sequence to make the name unique
     * 4. Rename TESTFILE(Working Copy).DOC to Backup of SAVEAS.wbk
     * 5. Rename -WRDnnnn.TMP to TESTFILE(Working Copy).DOC 
     * 6  CheckIn working copy.
     * 7. Validate TESTFILE.DOC
     */
    public void testScenarioMSWord2003SaveAsShuffleCheckedOutFile() throws Exception
    {
        logger.debug("testScenarioMSWord2003SaveShuffleLockedFile");
        final String FILE_NAME = "TESTFILE.DOC";
        final String FILE_OLD_TEMP = "SAVEAS.wbk";
        final String FILE_NEW_TEMP = "~WRD0002.TMP";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            String workingFileName;
            NodeRef workingCopy;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMSWord2003ShuffleLockedFile";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("a) create new file and check out");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
  
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);
                
                NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                
                /**
                 * CheckOut the test node
                 */
                NodeRef workingCopy = checkOutCheckInService.checkout(shuffledNodeRef);
                assertNotNull("Working copy is null", workingCopy);
                testContext.workingCopy = workingCopy;
                
                ChildAssociationRef ref = nodeService.getPrimaryParent(workingCopy);
                QName name = ref.getQName();
                testContext.workingFileName = ref.getQName().getLocalName();
                assertNotNull("working file name is null", testContext.workingFileName );
                
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
             
        /**
         * b) Save the new file
         * Write ContentDiskDriverTest3.doc to the test file,
         */
        logger.debug("b) move new file into place");
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
         
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.firstFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
            
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * c) rename the old working file
         */
        logger.debug("c) rename old file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + testContext.workingFileName, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
           
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        logger.debug("d) move new file into place");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + testContext.workingFileName); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        logger.debug("e) now check in");
        
        /**
         * Now Check In
         */
        RetryingTransactionCallback<Void> checkInCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               
               checkOutCheckInService.checkin(testContext.workingCopy, null);         
               return null;
            }
        };
        
        tran.doInTransaction(checkInCB, false, true);
        
        
        logger.debug("e) validate results");
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                             
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertNotNull("data is null", data);
               assertEquals("size is wrong", 26112, data.getSize());
               assertEquals("mimeType is wrong", "application/msword",data.getMimetype());
           
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        
    } // Test Word Save Locked File
    
    /**
     * ALF-10686
     * This scenario is executed by windows explorer.
     * 
     * A file is created and the file handle kept open.
     * stuff is written
     * Then the modified date is set
     * Then the file is closed.
     * @throws Exception
     */
    public void testSetFileScenario() throws Exception
    {
        logger.debug("testSetFileInfo");
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        Date now = new Date();
        
        // CREATE 6 hours ago
        final Date CREATED = new Date(now.getTime() - 1000 * 60 * 60 * 6);
        // Modify one hour ago
        final Date MODIFIED = new Date(now.getTime() - 1000 * 60 * 60 * 1);
        
        class TestContext
        {     
            NodeRef testNodeRef;    
        };
        
        final TestContext testContext = new TestContext();
      
        /**
          * Step 1 : Create a new file in read/write mode and add some content.
          * Call SetInfo to set the creation date
          */
        int openAction = FileAction.CreateNotExist;
        
        final String FILE_NAME="testSetFileScenario.txt";
        final String FILE_PATH="\\"+FILE_NAME;
        
        // Clean up junk if it exists
        try
        {
            driver.deleteFile(testSession, testConnection, FILE_PATH);
        }
        catch (IOException ie)
        {
           // expect to go here
        }
                  
        final FileOpenParams params = new FileOpenParams(FILE_PATH, openAction, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                
        final NetworkFile file = driver.createFile(testSession, testConnection, params);
        assertNotNull("file is null", file);
        assertFalse("file is read only, should be read-write", file.isReadOnly());
        
        RetryingTransactionCallback<Void> writeStuffCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                byte[] stuff = "Hello World".getBytes();
                
                driver.writeFile(testSession, testConnection, file, stuff, 0, stuff.length, 0);
                              
                FileInfo info = driver.getFileInformation(testSession, testConnection, FILE_PATH);
                info.setFileInformationFlags(FileInfo.SetModifyDate);
                info.setModifyDateTime(MODIFIED.getTime());
                info.setNetworkFile(file);
                driver.setFileInformation(testSession, testConnection, FILE_PATH, info);
                
                return null;
            }
        };
        tran.doInTransaction(writeStuffCB);
        
        RetryingTransactionCallback<Void> closeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                 // This close is in a different position to the simple setFileInformation scenarios above.
                driver.closeFile(testSession, testConnection, file);

                return null;
            }
        };
        tran.doInTransaction(closeFileCB);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef newNode = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                testContext.testNodeRef = newNode;
                assertNotNull("can't find new node", newNode);
                Serializable content = nodeService.getProperty(newNode, ContentModel.PROP_CONTENT);
                assertNotNull("content is null", content);     
                Date modified = (Date)nodeService.getProperty(newNode, ContentModel.PROP_MODIFIED);
                assertEquals("modified time not set correctly", MODIFIED, modified);
                return null;
            }
        };
        tran.doInTransaction(validateCB);
        
         
        // clean up so we could run the test again
        RetryingTransactionCallback<Void> deleteFile = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, FILE_PATH);
                return null;
            }
        };
        tran.doInTransaction(deleteFile, false, true);
        
    } // test set modified scenario
    
    /**
     * This test tries to simulate the cifs shuffling that is done 
     * from Save from Mac Lion by TextEdit
     * 
     * a) Temp file created in temporary folder (test.txt)
     * b) Resource fork file created in temporary folder (._test.txt)
     * b) Target file deleted
     * c) Temp file moved to target file.
     */
    public void testScenarioMacLionTextEdit() throws Exception
    {
        logger.debug("testScenarioLionTextEdit");
        final String FILE_NAME = "test.txt";
        final String FORK_FILE_NAME = "._test.txt";
        final String TEMP_FILE_NAME = "test.txt";
        
        final String UPDATED_TEXT = "Mac Lion Text Updated Content";
        
        class TestContext
        {
            NetworkFile lockFileHandle;
            NetworkFile firstFileHandle;
            NetworkFile tempFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioLionTextEdit";
        final String TEST_TEMP_DIR = "\\ContentDiskDriverTest\\testScenarioLionTextEdit\\.Temporary Items";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Create a file in the test directory
         */           
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                driver.createDirectory(testSession, testConnection, createTempDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                String testContent = "Mac Lion Text";
                byte[] testContentBytes = testContent.getBytes();
                 
                driver.writeFile(testSession, testConnection, testContext.firstFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                
                /**
                 * Create the temp file we are going to use
                 */
                FileOpenParams createTempFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                assertNotNull(testContext.tempFileHandle);
                               
                testContent = UPDATED_TEXT;
                testContentBytes = testContent.getBytes();
                driver.writeFile(testSession, testConnection, testContext.tempFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                driver.closeFile(testSession, testConnection, testContext.tempFileHandle); 
                
                /**
                 * Create the temp resource fork file we are going to use
                 */
                createFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + FORK_FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.lockFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.lockFileHandle);
                testContext.lockFileHandle.closeFile();
                
                /**
                 * Also add versionable to target file
                 */
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
            
        /**
         * b) Delete the target file
         */
        RetryingTransactionCallback<Void> deleteTargetFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(deleteTargetFileCB, false, true);
        
        /**
         * c) Move the temp file into place
         */
        RetryingTransactionCallback<Void> moveTempFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, TEST_DIR + "\\" + FILE_NAME); 
                driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + FORK_FILE_NAME, TEST_DIR + "\\" + FORK_FILE_NAME); 
                return null;
            }
        };
        tran.doInTransaction(moveTempFileCB, false, true);
        
        /**
         * Validate results.
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {

                NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);

                assertEquals("shuffledNode ref is different", shuffledNodeRef, testContext.testNodeRef);
                assertTrue("node is not versionable", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                
                ContentReader reader = contentService.getReader(shuffledNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("Reader is null", reader);
                String s = reader.getContentString();
                assertEquals("content not written", UPDATED_TEXT, s);
                
                
                return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);

    } // testScenarioLionTextEdit

    /**
     * Simulates a Save from Powerpoint 2011 Mac
     * 0. FileA.pptx already exists.
     * 1. Create new document FileA1.pptx
     * 2. Delete FileA.pptx
     * 3. Rename FileA1.pptx to FileA.pptx 
     */
    public void testScenarioMSPowerpoint2011MacSaveShuffle() throws Exception
    {
        logger.debug("testScenarioMSPowerpoint2011MacSaveShuffle(");
        
        final String FILE_NAME = "FileA.pptx";
        final String FILE_NEW_TEMP = "FileA1.pptx";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMSPowerpoint2011MacSaveShuffle";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            logger.debug("expect to get exception - cleaning garbage");
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("0) create new file");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
  
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use (FileA.pptx)
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                
                NodeRef file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
             
        /**
         * b) Save the new file
         * Write ContentDiskDriverTest3.doc to the test file,
         */
        logger.debug("b) write some content");
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
         
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.firstFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
            
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * c) delete the old file
         */
        logger.debug("c) delete old file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
           
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        logger.debug("d) rename new file into place");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        logger.debug("e) validate results");
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertNotNull("data is null", data);
               assertEquals("size is wrong", 26112, data.getSize());
               assertEquals("mimeType is wrong", "application/msword",data.getMimetype());
               
               assertTrue("versionable aspect missing", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
               assertTrue("hidden aspect still applied", !nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_HIDDEN));
               assertTrue("temporary aspect still applied", !nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_TEMPORARY));
           
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        
    } // testScenarioMSPowerpoint2011MacSaveShuffle

    /**
     * Simulates a Save from Excel 2011 Mac
     * 0. FileA.xlsx already exists.
     * 1. Create new document ._A8A09200
     * 2. Delete FileA.xlsx
     * 3. Rename ._A8A09200 to FileA.xlsx 
     */
    public void testScenarioMSExcel2011MacSaveShuffle() throws Exception
    {
        logger.debug("testScenarioMSExcel2011MacSaveShuffle(");
        
        final String FILE_NAME = "FileA.xlsx";
        final String FILE_NEW_TEMP = "._A8A09200";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMSExcel2011MacSaveShuffle";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            logger.debug("expect to get exception - cleaning garbage");
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("0) create new file");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
  
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use (FileA.xlsx)
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                
                NodeRef file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);

                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
             
        /**
         * b) Save the new file
         * Write ContentDiskDriverTest3.doc to the test file,
         */
        logger.debug("b) write some content");
        RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
         
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.firstFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
            
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(writeFileCB, false, true);
        
        /**
         * c) delete the old file
         */
        logger.debug("c) delete old file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
           
        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        logger.debug("d) rename new file into place");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        logger.debug("e) validate results");
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertNotNull("data is null", data);
               assertEquals("size is wrong", 26112, data.getSize());
               assertEquals("mimeType is wrong", "application/msword",data.getMimetype());
               
               assertTrue("versionable aspect missing", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
               assertTrue("hidden aspect still applied", !nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_HIDDEN));
               assertTrue("temporary aspect still applied", !nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_TEMPORARY));
           
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        
    } // testScenarioMSExcel2011MacSaveShuffle

    /**
     * This test tries to simulate the cifs shuffling that is done to
     * support MS Word 2011 on Mac with backup turned on. 
     * 
     * a) TEST.DOCX
     * b) Create new temp file in temp dir Word Work File D_.tmp
     * c) Delete backup file.
     * c) Rename TEST.DOCX to Backup of TEST.docx
     * d) Move temp file to target dir
     * d) Rename Word Work File D_.tmp to TEST.docx
     */
    public void testScenarioMSWord20011MacSaveWithBackup() throws Exception
    {
        logger.debug("testScenarioMSWord20011MacSaveWithBackup");
        final String FILE_NAME = "TEST.DOCX";
        final String FILE_BACKUP = "Backup of TEST.docx";
        final String FILE_NEW_TEMP = "Word Work File D_.tmp";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;            
            NodeRef testNodeRef;   // node ref of test.doc
        };
        
        final TestContext testContext = new TestContext();
        
        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMSWord20011MacSaveWithBackup";
        final String TEST_TEMP_DIR = "\\ContentDiskDriverTest\\testScenarioMSWord20011MacSaveWithBackup\\.Temporary Items";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        logger.debug("Step 0 - initialise");
        
        /**
         * Create a file in the test directory
         */           
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                driver.createDirectory(testSession, testConnection, createTempDirParams);
                
                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                // no need to test lots of different properties, that's already been tested above
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                String testContent = "MS Word 2011 shuffle test";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();  
                        
                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
               
        /**
         * a) Save the temp file in the temp dir
         */
        logger.debug("Step a - create a temp file in the temp dir");
        RetryingTransactionCallback<Void> saveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);
                String testContent = "MS Word 2011 shuffle test This is new content";
                byte[] testContentBytes = testContent.getBytes();
                testContext.newFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.newFileHandle.close();
              
                return null;
            }
        };
        tran.doInTransaction(saveNewFileCB, false, true);
        
        /*
         * Step b not used in test case
         */
        
        /**
         * c) rename the target file to a backup file
         */
        logger.debug("Step c - rename the target file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_BACKUP);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);
        
        
        /**
         * d) Move the new file into target dir
         */
        logger.debug("Step d - move new file into target dir");
        RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NEW_TEMP); 
                return null;
            }
        };
        
        tran.doInTransaction(moveNewFileCB, false, true);
        
        /**
         * e) Rename temp file into place.
         */
        logger.debug("Step e - rename temp file into place");
        RetryingTransactionCallback<Void> renameTempFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(renameTempFileCB, false, true);
        
        
        /**
         * Validate
         */
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
               assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME) );    
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);

    } // testScenarioMSMacWord20011SaveWithBackup save
    
    
    /**
     * Simulates a Mac Lion Drag and Drop 
     * 0. ALF-15158.diff  already exists and is versionable
     * 1. Delete ALF-15158.diff 
     * 2. Create new document ALF-15158.diff 
     */
    public void testMacDragAndDrop() throws Exception
    {
        logger.debug("testMacDragAndDrop()");
        
        final String FILE_NAME = "ALF-15158.diff";
        
        class TestContext
        {
            NetworkFile firstFileHandle;
            NodeRef file1NodeRef;
        };
        
        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\MacDragAndDrop";
        
        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
     
        /**
         * Create a file in the test directory
         */    
        
        try
        {
            logger.debug("expect to get exception - cleaning garbage");
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("0) create new file");
        RetryingTransactionCallback<TestContext> createFileCB = new RetryingTransactionCallback<TestContext>() {
            @Override
            public TestContext execute() throws Throwable
            {
  
                TestContext ctx = new TestContext();
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                
                /**
                 * Create the file we are going to use (FileA.pptx)
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                ctx.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(ctx.firstFileHandle);
                
                driver.closeFile(testSession, testConnection, ctx.firstFileHandle); 
                
                ctx.file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(ctx.file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                        
                return ctx;
            }
        };
        final TestContext testContext = tran.doInTransaction(createFileCB, false, true);
        
        /**
         * 1) delete the old file
         */
        logger.debug("1) delete old file");
        RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(renameOldFileCB, false, true);

             
        /**
         * 2) CreateNewFile and write some new content
         * 
         */
        logger.debug("2) write some content");
        RetryingTransactionCallback<Void> restoreFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
         
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);

                byte[] buffer= new byte[1000];
                InputStream is = fileResource.getInputStream();
                try
                {
                    long offset = 0;
                    int i = is.read(buffer, 0, buffer.length);
                    while(i > 0)
                    {
                        testContext.firstFileHandle.writeFile(buffer, i, 0, offset);
                        offset += i;
                        i = is.read(buffer, 0, buffer.length);
                    }                 
                }
                finally
                {
                    is.close();
                }
            
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                    
                return null;
            }
        };
        tran.doInTransaction(restoreFileCB, false, true);
        
                   
        logger.debug("3) validate results");
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               assertNotNull("data is null", data);
               assertEquals("size is wrong", 26112, data.getSize());
               assertEquals("mimeType is wrong", "application/msword",data.getMimetype());
               
               assertTrue("versionable aspect missing", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
               assertTrue("hidden aspect still applied", !nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_HIDDEN));
               assertTrue("temporary aspect still applied", !nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_TEMPORARY));
           
               assertEquals("Node ref has changed", shuffledNodeRef, testContext.file1NodeRef);
               return null;
            }
        };
        
        tran.doInTransaction(validateCB, true, true);
        logger.debug("end testMacDragAndDrop");
        
    } // testMacDragAndDrop
    
    
    /**
     * Mountain Lion 2011 Word
     * a) Create new file (Word Work File D2.tmp)  
     * (Actually in real life its renamed from a temp directory.
     * c) Existing file rename out of the way.   (Word Work File L_5.tmp)
     * d) New file rename into place. (MacWord1.docx)
     * e) Old file deleted
     */
     public void testScenarioMountainLionWord2011() throws Exception
     {  
         logger.debug("testScenarioMountainLionWord2011");
         
         final String FILE_NAME = "MacWord1.docx";
         final String FILE_OLD_TEMP = "Word Work File L_5.tmp";
         final String FILE_NEW_TEMP = "Word Work File D_2.tmp";
         
         class TestContext
         {
             NetworkFile firstFileHandle;
             String mimetype;
         };
         
         final TestContext testContext = new TestContext();
         
         final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testScenarioMountainLionWord2011";
         
         ServerConfiguration scfg = new ServerConfiguration("testServer");
         TestServer testServer = new TestServer("testServer", scfg);
         final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
         DiskSharedDevice share = getDiskSharedDevice();
         final TreeConnection testConnection = testServer.getTreeConnection(share);
         final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

         /**
          * Clean up just in case garbage is left from a previous run
          */
         RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };
         
         try
         {
             tran.doInTransaction(deleteGarbageFileCB);
         }
         catch (Exception e)
         {
             // expect to go here
         }
         
         logger.debug("a) create new file");
         RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 /**
                  * Create the test directory we are going to use 
                  */
                 FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 driver.createDirectory(testSession, testConnection, createRootDirParams);
                 driver.createDirectory(testSession, testConnection, createDirParams);
                 
                 /**
                  * Create the file we are going to test
                  */
                 FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                 assertNotNull(testContext.firstFileHandle);
                 
                 ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                 assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);
                 writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                 driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                 NodeRef file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);

                 return null;
             }
         };
         tran.doInTransaction(createFileCB, false, true);
              
         /**
          * b) Save the new file
          * Write ContentDiskDriverTest3.doc,
          */
         logger.debug("b) move new file into place");
         RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
          
                 ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                 assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);
                 writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                 driver.closeFile(testSession, testConnection, testContext.firstFileHandle);   
                 
                 
                 NodeRef file1NodeRef = getNodeForPath(testConnection,  TEST_DIR + "\\" + FILE_NAME);
                 Map<QName, Serializable> props = nodeService.getProperties(file1NodeRef);
                 ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
//                 assertNotNull("data is null", data);
//                 assertEquals("size is wrong", 166912, data.getSize());
                 testContext.mimetype = data.getMimetype();
                 
                 return null;
             }
         };
         tran.doInTransaction(writeFileCB, false, true);
         
         /**
          * c) rename the old file
          */
         logger.debug("c) rename old file");
         RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                 return null;
             }
         };
         tran.doInTransaction(renameOldFileCB, false, true);
            
         /**
          * d) Move the new file into place, stuff should get shuffled
          */
         logger.debug("d) move new file into place");
         RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                 return null;
             }
         };
         
         tran.doInTransaction(moveNewFileCB, false, true);
         
         /**
          * d) Delete the old file
          */
         logger.debug("d) delete the old file");
         RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                 return null;
             }
         };
         
         tran.doInTransaction(deleteOldFileCB, false, true);
         
         logger.debug("e) validate results");
         
         /**
          * Now validate everything is correct
          */
         RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                
                Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
                
                ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                //assertNotNull("data is null", data);
                //assertEquals("size is wrong", 123904, data.getSize());
                
                NodeRef file1NodeRef = getNodeForPath(testConnection,  TEST_DIR + "\\" + FILE_NAME); 
                assertTrue("file has lost versionable aspect", nodeService.hasAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE));

                assertEquals("mimeType is wrong", testContext.mimetype, data.getMimetype());
                
            
                return null;
             }
         };
         
         tran.doInTransaction(validateCB, true, true);
     }  // Test Word 2011 Mountain Lion

     /**
      * This test tries to simulate the cifs shuffling that is done 
      * from Save from Mac Mountain Lion by Preview
      * 
      * a) Temp file created in temporary folder (Crysanthemum.jpg)
      * b) Target file deleted by open / delete on close flag / close
      * c) Temp file moved to target file.
      */
     public void testScenarioMacMountainLionPreview() throws Exception
     {
         logger.debug("testScenarioMountainLionPreview");
         final String FILE_NAME = "Crysanthemeum.jpg";
         final String TEMP_FILE_NAME = "Crysanthemeum.jpg";
         
         final String UPDATED_TEXT = "Mac Lion Preview Updated Content";
         
         class TestContext
         {
             NetworkFile firstFileHandle;
             NetworkFile tempFileHandle;            
             NodeRef testNodeRef;   // node ref Crysanthemenum.jpg
         };
         
         final TestContext testContext = new TestContext();
         
         final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
         final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionPreview";
         final String TEST_TEMP_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionPreview\\.Temporary Items";
         
         ServerConfiguration scfg = new ServerConfiguration("testServer");
         TestServer testServer = new TestServer("testServer", scfg);
         final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
         DiskSharedDevice share = getDiskSharedDevice();
         final TreeConnection testConnection = testServer.getTreeConnection(share);
         final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
         
         /**
          * Create a file in the test directory
          */           
         RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 /**
                  * Create the test directory we are going to use 
                  */
                 FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 driver.createDirectory(testSession, testConnection, createRootDirParams);
                 driver.createDirectory(testSession, testConnection, createDirParams);
                 driver.createDirectory(testSession, testConnection, createTempDirParams);
                 
                 /**
                  * Create the file we are going to use
                  */
                 FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                 assertNotNull(testContext.firstFileHandle);
                 
                 String testContent = "Mac Mountain Lion Text";
                 byte[] testContentBytes = testContent.getBytes();
                  
                 driver.writeFile(testSession, testConnection, testContext.firstFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                 
                 /**
                  * Create the temp file we are going to use
                  */
                 FileOpenParams createTempFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                 assertNotNull(testContext.tempFileHandle);
                                
                 testContent = UPDATED_TEXT;
                 testContentBytes = testContent.getBytes();
                 driver.writeFile(testSession, testConnection, testContext.tempFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.tempFileHandle); 
                                  
                 /**
                  * Also add versionable to target file
                  */
                 testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                 
                 return null;
             }
         };
         tran.doInTransaction(createFileCB, false, true);
             
//         /**
//          * b) Delete the target file by opening it and set the delete on close bit
//          */
//         RetryingTransactionCallback<Void> deleteTargetFileCB = new RetryingTransactionCallback<Void>() {
//
//             @Override
//             public Void execute() throws Throwable
//             {
//                 FileOpenParams openFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
//                 testContext.tempFileHandle = driver.openFile(testSession, testConnection, openFileParams);
//                 FileInfo info = new FileInfo();
//                 info.setFileInformationFlags(FileInfo.SetDeleteOnClose);
//                 info.setDeleteOnClose(true);
//                 testContext.tempFileHandle.setDeleteOnClose(true);
//                 
//                 driver.setFileInformation(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, info);
//                 
//                 assertNotNull(testContext.tempFileHandle);
//                 logger.debug("this close should result in a file being deleted");
//                 driver.closeFile(testSession, testConnection,  testContext.tempFileHandle);
//                 return null;
//             }
//         };
//         tran.doInTransaction(deleteTargetFileCB, false, true);
         
       /**
       * b) Delete the target file by a simple delete
       */
      RetryingTransactionCallback<Void> deleteTargetFileCB = new RetryingTransactionCallback<Void>() {

          @Override
          public Void execute() throws Throwable
          {
                
              driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
              return null;
          }
      };
      tran.doInTransaction(deleteTargetFileCB, false, true);
         
         /**
          * c) Move the temp file into target directory
          */
         RetryingTransactionCallback<Void> moveTempFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, TEST_DIR + "\\" + FILE_NAME); 
                 return null;
             }
         };
         tran.doInTransaction(moveTempFileCB, false, true);
         
         /**
          * Validate results.
          */
         RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {

                 NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 assertTrue("node is not versionable", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                 assertEquals("shuffledNode ref is different", shuffledNodeRef, testContext.testNodeRef);
                 return null;
             }
         };
         
         tran.doInTransaction(validateCB, false, true);

     } // testScenarioMountainLionPreview

     /**
      * This test tries to simulate the cifs shuffling that is done 
      * from Save from Mac Mountain Lion by Preview when document is saved first time
      * 
      * a) Temp file created in temporary folder (temp\image.jpg.sb-1e5e1543-ajn3cR)
      * b) Temp file renamed to original name (temp\image.jpg.sb-1e5e1543-ajn3cR -> temp\image.jpg)
      * c) Original document renamed to backup copy name (test\image.jpg -> test\image.jpg.sb-1e5e1543-ajn3cR)
      * d) Renamed temp file moved to original (temp\image.jpg -> test\image.jpg)
      */
     public void testScenarioMacMountainLionPreview_MNT_263() throws Exception
     {
         logger.debug("testScenarioMacMountainLionPreview_MNT_263");
         final String FILE_NAME = "image.jpg";
         final String TEMP_FILE_NAME = "image.jpg.sb-1e5e1543-ajn3cR";
         
         final String UPDATED_TEXT = "Mac Lion Preview Updated Content";
         
         class TestContext
         {
             NetworkFile firstFileHandle;
             NetworkFile tempFileHandle;            
             NodeRef testNodeRef;   // node ref image.jpg
         };
         
         final TestContext testContext = new TestContext();
         
         final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
         final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionPreview";
         final String TEST_TEMP_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionPreview\\.Temporary Items";
         
         ServerConfiguration scfg = new ServerConfiguration("testServer");
         TestServer testServer = new TestServer("testServer", scfg);
         final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
         DiskSharedDevice share = getDiskSharedDevice();
         final TreeConnection testConnection = testServer.getTreeConnection(share);
         final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
         
         /**
          * Create a file in the test directory
          */           
         RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 /**
                  * Create the test directory we are going to use 
                  */
                 FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 driver.createDirectory(testSession, testConnection, createRootDirParams);
                 driver.createDirectory(testSession, testConnection, createDirParams);
                 driver.createDirectory(testSession, testConnection, createTempDirParams);
                 
                 /**
                  * Create the file we are going to use
                  */
                 FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                 assertNotNull(testContext.firstFileHandle);
                 
                 String testContent = "Mac Lion Preview Content";
                 byte[] testContentBytes = testContent.getBytes();
                  
                 driver.writeFile(testSession, testConnection, testContext.firstFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                 
                 /**
                  * Create the temp file we are going to use
                  */
                 FileOpenParams createTempFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                 assertNotNull(testContext.tempFileHandle);
                                
                 testContent = UPDATED_TEXT;
                 testContentBytes = testContent.getBytes();
                 driver.writeFile(testSession, testConnection, testContext.tempFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.tempFileHandle); 
                                  
                 /**
                  * Also add versionable to target file
                  */
                 testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                 
                 return null;
             }
         };
         tran.doInTransaction(createFileCB, false, true);
         
         RetryingTransactionCallback<Void> renameTempFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, TEST_TEMP_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };
         tran.doInTransaction(renameTempFileCB, false, true);
         
         RetryingTransactionCallback<Void> renameFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + TEMP_FILE_NAME);
                 return null;
             }
         };
         tran.doInTransaction(renameFileCB, false, true);
         
         RetryingTransactionCallback<Void> moveRenamedTempFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };
         tran.doInTransaction(moveRenamedTempFileCB, false, true);
         
         RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {

                 NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 assertTrue("node is not versionable", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                 assertEquals("shuffledNode ref is different", shuffledNodeRef, testContext.testNodeRef);
                 assertEquals("Unexpected content size", contentService.getReader(shuffledNodeRef, ContentModel.PROP_CONTENT).getSize(), UPDATED_TEXT.length());
                 
                 return null;
             }
         };
         
         tran.doInTransaction(validateCB, false, true);

     } // testScenarioMacMountainLionPreview_MNT_263

     /**
      * This test tries to simulate the cifs shuffling that is done 
      * from Save from Mac Mountain Lion by Preview when document is opened/saved few time a row
      * 
      * a) Temp file created in temporary folder (temp\image.jpg)
      * b) Original file is renamed for deletion(test\image.jpg -> test\.smbdeleteAAA1b994.4)
      * c) Renamed file has got deleteOnClose flag
      * d) Renamed file is closed.
      * e) Temp file is moved into original file location(temp\image.jpg -> test\image.jgp) 
      */
     public void testScenarioMacMountainLionPreview_MNT_317() throws Exception
    {
        logger.debug("testScenarioMacMountainLionPreview_MNT_317");
        final String FILE_NAME = "image.jpg";
        final String TEMP_FILE_NAME = ".smbdeleteAAA1b994.4";

        final String UPDATED_TEXT = "Mac Lion Preview Updated Content";

        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile tempFileHandle;
            NodeRef testNodeRef; // node ref image.jpg
        }
        ;

        final TestContext testContext = new TestContext();

        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionPreview";
        final String TEST_TEMP_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionPreview\\.Temporary Items";

        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        /**
         * Create a file in the test directory
         */
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                driver.createDirectory(testSession, testConnection, createTempDirParams);

                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);

                String testContent = "Mac Lion Preview Content";
                byte[] testContentBytes = testContent.getBytes();

                driver.writeFile(testSession, testConnection, testContext.firstFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);

                /**
                 * Create the temp file we are going to use
                 */
                FileOpenParams createTempFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                assertNotNull(testContext.tempFileHandle);

                testContent = UPDATED_TEXT;
                testContentBytes = testContent.getBytes();
                driver.writeFile(testSession, testConnection, testContext.tempFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                driver.closeFile(testSession, testConnection, testContext.tempFileHandle);

                /**
                 * Also add versionable to target file
                 */
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);

                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);

        RetryingTransactionCallback<Void> renameFileCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                FileOpenParams openFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.tempFileHandle = driver.openFile(testSession, testConnection, openFileParams);
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + TEMP_FILE_NAME);
                
                return null;
            }
        };
        tran.doInTransaction(renameFileCB, false, true);

        /**
         * Delete file via deleteOnClose flag.
         */
        RetryingTransactionCallback<Void> deleteOnCloseCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                FileInfo info = new FileInfo();
                info.setFileInformationFlags(FileInfo.SetDeleteOnClose);
                info.setDeleteOnClose(true);
                testContext.tempFileHandle.setDeleteOnClose(true);

                driver.setFileInformation(testSession, testConnection, TEST_DIR + "\\" + TEMP_FILE_NAME, info);

                assertNotNull(testContext.tempFileHandle);
                logger.debug("this close should result in a file being deleted");
                driver.closeFile(testSession, testConnection, testContext.tempFileHandle);
                return null;
            }
        };
        tran.doInTransaction(deleteOnCloseCB, false, true);

//        /**
//         * Delete file directly.
//         */
//        RetryingTransactionCallback<Void> deleteTargetFileCB = new RetryingTransactionCallback<Void>()
//        {
//
//            @Override
//            public Void execute() throws Throwable
//            {
//
//                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + TEMP_FILE_NAME);
//                return null;
//            }
//        };
//        tran.doInTransaction(deleteTargetFileCB, false, true);

        RetryingTransactionCallback<Void> moveRenamedTempFileCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        tran.doInTransaction(moveRenamedTempFileCB, false, true);

        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {

                NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                assertTrue("node is not versionable", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                assertEquals("shuffledNode ref is different", shuffledNodeRef, testContext.testNodeRef);
                assertEquals("Unexpected content size", contentService.getReader(shuffledNodeRef, ContentModel.PROP_CONTENT).getSize(), UPDATED_TEXT.length());

                return null;
            }
        };

        tran.doInTransaction(validateCB, false, true);

    }     // testScenarioMacMountainLionPreview_MNT_317
     
    /**
      * This test tries to simulate the cifs shuffling that is done 
      * from Save from Mac Mountain Lion by Keynote when document is saved first time
      * 
      * a) Temp file created in temporary folder (temp\test.key)
      * b) Original document renamed to backup copy name (test\test.key -> test\test~.key)
      * c) Temp file moved to original name (temp\test.key -> test\test.key)
      */
     public void testScenarioMacMountainLionKeynote_MNT_8558() throws Exception
     {
         logger.debug("testScenarioMacMountainLionKeynote_MNT_8558");
         final String FILE_NAME = "test.key";
         final String BCKP_FILE_NAME = "test~.key";
         final String TEMP_FILE_NAME = "test.key";
         
         final String UPDATED_TEXT = "Mac Mountain Lion Keynote Updated Content";
         
         class TestContext
         {
             NetworkFile firstFileHandle;
             NetworkFile tempFileHandle;            
             NodeRef testNodeRef;   
         };
         
         final TestContext testContext = new TestContext();
         
         final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
         final String TEST_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionKeynote";
         final String TEST_TEMP_DIR = "\\ContentDiskDriverTest\\testScenarioMountainLionKeynote\\.Temporary Items";
         
         ServerConfiguration scfg = new ServerConfiguration("testServer");
         TestServer testServer = new TestServer("testServer", scfg);
         final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
         DiskSharedDevice share = getDiskSharedDevice();
         final TreeConnection testConnection = testServer.getTreeConnection(share);
         final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
         
         /**
          * Create a file in the test directory
          */           
         RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 /**
                  * Create the test directory we are going to use 
                  */
                 FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 driver.createDirectory(testSession, testConnection, createRootDirParams);
                 driver.createDirectory(testSession, testConnection, createDirParams);
                 driver.createDirectory(testSession, testConnection, createTempDirParams);
                 
                 /**
                  * Create the file we are going to use
                  */
                 FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                 assertNotNull(testContext.firstFileHandle);
                 
                 String testContent = "Mac Mountain Lion Keynote Content";
                 byte[] testContentBytes = testContent.getBytes();
                  
                 driver.writeFile(testSession, testConnection, testContext.firstFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                 
                 /**
                  * Create the temp file we are going to use
                  */
                 FileOpenParams createTempFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                 assertNotNull(testContext.tempFileHandle);
                                
                 testContent = UPDATED_TEXT;
                 testContentBytes = testContent.getBytes();
                 driver.writeFile(testSession, testConnection, testContext.tempFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.tempFileHandle); 
                                  
                 /**
                  * Also add versionable to target file
                  */
                 testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                 
                 return null;
             }
         };
         tran.doInTransaction(createFileCB, false, true);
         
         RetryingTransactionCallback<Void> renameFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + BCKP_FILE_NAME);
                 return null;
             }
         };
         tran.doInTransaction(renameFileCB, false, true);
         
         RetryingTransactionCallback<Void> moveTempFileCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, TEST_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };
         tran.doInTransaction(moveTempFileCB, false, true);
         
         RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {

                 NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 assertTrue("node is not versionable", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                 assertEquals("shuffledNode ref is different", shuffledNodeRef, testContext.testNodeRef);
                 assertEquals("Unexpected content size", contentService.getReader(shuffledNodeRef, ContentModel.PROP_CONTENT).getSize(), UPDATED_TEXT.length());
                 
                 return null;
             }
         };
         
         tran.doInTransaction(validateCB, false, true);
         
         //Make sure that during second rename test.key->test~.key deleted test~.key is not restored and version history doesn't lost. 
         RetryingTransactionCallback<Void> prepareForSecondRunCB  = new RetryingTransactionCallback<Void>() {

             @Override
             public Void execute() throws Throwable
             {

                 driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + BCKP_FILE_NAME);
                 FileOpenParams createTempFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + TEMP_FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                 
                 String testContent = UPDATED_TEXT;
                 byte[] testContentBytes = testContent.getBytes();
                 driver.writeFile(testSession, testConnection, testContext.tempFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                 driver.closeFile(testSession, testConnection, testContext.tempFileHandle); 
                 
                 return null;
             }
         };
         
         tran.doInTransaction(prepareForSecondRunCB, false, true);
         tran.doInTransaction(renameFileCB, false, true);
         tran.doInTransaction(moveTempFileCB, false, true);
         tran.doInTransaction(validateCB, false, true);

     } // testScenarioMacMountainLionKeynote_MNT_8558

     /**
      *  Gedit has the nasty behaviour of renaming an open file.
      *  1) create file (gedit12345678.txt)
      *  2) create temp file (.goutputStream-IRYDPW) write and flush
      *  3) rename (fails name collision)
      *  4) delete target
      *  5) rename this one succeeds
      *  6) close temp file
      *  
      */
     public void testGedit() throws Exception
     {
         logger.debug("testGEdit");
         
         final String FILE_NAME = "gedit12345678.txt";
         final String FILE_TITLE = "Gedit";
         final String FILE_DESCRIPTION = "This is a test document to test CIFS shuffle";
         final String TEMP_FILE_NAME = ".goutputStream-IRYDPW";
         final String UPDATE_TEXT = "Shuffle an open file";

         class TestContext
         {
             NetworkFile firstFileHandle;
             NetworkFile tempFileHandle;
             NodeRef testNodeRef;
         };

         final TestContext testContext = new TestContext();

         final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testGEdit";

         ServerConfiguration scfg = new ServerConfiguration("testServer");
         TestServer testServer = new TestServer("testServer", scfg);
         final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
         DiskSharedDevice share = getDiskSharedDevice();
         final TreeConnection testConnection = testServer.getTreeConnection(share);
         final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

         /**
          * Clean up from a previous run
          */
         RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {
                 driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };

         try
         {
             tran.doInTransaction(deleteGarbageFileCB);
         }
         catch (Exception e)
         {
             // expect to go here
         }

         /**
          * Create a file in the test directory
          */
         RetryingTransactionCallback<Void> createTestFileFirstTime = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {

                 /**
                  * Create the test directory we are going to use
                  */
                 FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 driver.createDirectory(testSession, testConnection, createRootDirParams);
                 driver.createDirectory(testSession, testConnection, createDirParams);

                 /**
                  * Create the file we are going to use
                  */
                 FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                 assertNotNull(testContext.firstFileHandle);

                 testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);

                 nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_TITLE, FILE_TITLE);
                 nodeService.setProperty(testContext.testNodeRef, ContentModel.PROP_DESCRIPTION, FILE_DESCRIPTION);

                 String testContent = "Gedit shuffle test";
                 byte[] testContentBytes = testContent.getBytes();
                 testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);

                 driver.closeFile(testSession, testConnection, testContext.firstFileHandle);
                 
                 nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                 
                 return null;
             }
         };
         tran.doInTransaction(createTestFileFirstTime, false, true);

        /**
          * Create the temp file
          * add content
          * leave open
          */
         RetryingTransactionCallback<Void> createTempFile = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {

                 FileOpenParams params = new FileOpenParams(TEST_DIR + "\\" + TEMP_FILE_NAME, FileAction.TruncateExisting, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                 NetworkFile file = driver.createFile(testSession, testConnection, params);
                 testContext.tempFileHandle = file;
                 String testContent = UPDATE_TEXT;
                 byte[] testContentBytes = testContent.getBytes();
                 testContext.tempFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                  /**     driver.closeFile(testSession, testConnection, file);   **/

                 return null;
             }
         };
         tran.doInTransaction(createTempFile, false, true);

         /**
          * rename the test file to the temp
          */
         RetryingTransactionCallback<Void> renameTestFileToTemp = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {
                 driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + TEMP_FILE_NAME, TEST_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };
         
         // expect this one to fail
         try
         {
             tran.doInTransaction(renameTestFileToTemp, false, true);
             fail("should have failed");
         }
         catch (Exception e)
         {
             // expect to go here
         }
         
         /**
          * delete the target file
          */
         RetryingTransactionCallback<Void> deleteTargetFile = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {
                 driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                 return null;
             }
         };
         
         tran.doInTransaction(deleteTargetFile, false, true);
         
         // This one should succeed
         tran.doInTransaction(renameTestFileToTemp, false, true);
         
         RetryingTransactionCallback<Void> closeTempFile = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {
                 driver.closeFile(testSession, testConnection, testContext.tempFileHandle);
                 return null;
             }
         };
         
         tran.doInTransaction(closeTempFile, false, true);
         
         // Now validate
         RetryingTransactionCallback<Void> validate = new RetryingTransactionCallback<Void>()
         {

             @Override
             public Void execute() throws Throwable
             {
                 NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                  
                 ContentReader reader = contentService.getReader(shuffledNodeRef, ContentModel.PROP_CONTENT);
                 String s = reader.getContentString();
                 assertEquals("content not written", UPDATE_TEXT, s);
                 
                 assertTrue("node is not versionable", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                 assertEquals("shuffledNode ref is different", shuffledNodeRef, testContext.testNodeRef);
      
        
                 return null;
             }
         };
         
         tran.doInTransaction(validate, false, true);

         logger.debug("end testGedit");
     } // testGedit
     
     
     /**
      * Windows7 Explorer update
      * 0) Existing file mark.jpg 
      * a) Create new file (~ark.tmp)  
      * b) Existing file rename out of the way.   (mark.jpg~RF5bb356.TMP)
      * c) New file rename into place. (~ark.tmp - mark.jpg)
      * d) Old file opened attributes only
      * e) set delete on close
      * f) close
      */
      public void testWindows7Explorer() throws Exception
      {  
          logger.debug("testWindows7Explorer");
          
          final String FILE_NAME = "mark.jpg";
          final String FILE_OLD_TEMP = "mark.jpg~RF5bb356.TMP";
          final String FILE_NEW_TEMP = "~ark.tmp";
          
          class TestContext
          {
              NodeRef testNodeRef;
              NetworkFile firstFileHandle;
              NetworkFile secondFileHandle;
          };
          
          final TestContext testContext = new TestContext();
          
          final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testWindows7Explorer";
          
          ServerConfiguration scfg = new ServerConfiguration("testServer");
          TestServer testServer = new TestServer("testServer", scfg);
          final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
          DiskSharedDevice share = getDiskSharedDevice();
          final TreeConnection testConnection = testServer.getTreeConnection(share);
          final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

          /**
           * Clean up just in case garbage is left from a previous run
           */
          RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                  return null;
              }
          };
          
          try
          {
              tran.doInTransaction(deleteGarbageFileCB);
          }
          catch (Exception e)
          {
              // expect to go here
          }
          
          logger.debug("0) create new file");
          RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  /**
                   * Create the test directory we are going to use 
                   */
                  FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                  FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                  driver.createDirectory(testSession, testConnection, createRootDirParams);
                  driver.createDirectory(testSession, testConnection, createDirParams);
                  
                  /**
                   * Create the file we are going to test
                   */
                  FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                  testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                  assertNotNull(testContext.firstFileHandle);
                  
                  ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTestMark.jpg");
                  assertNotNull("unable to find test resource filesys/ContentDiskDriverTestMark.jpg", fileResource);
                  writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                  driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                  NodeRef file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                  testContext.testNodeRef = file1NodeRef;
                  nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);

                  return null;
              }
          };
          tran.doInTransaction(createFileCB, false, true);
               
          /**
           * a) Save the new file
           */
          logger.debug("a) save new file");
          RetryingTransactionCallback<Void> writeFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                  testContext.secondFileHandle = driver.createFile(testSession, testConnection, createFileParams);    
           
                  ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTestMark2.jpg");
                  assertNotNull("unable to find test resource filesys/ContentDiskDriverTestMark2.jpg", fileResource);
                  writeResourceToNetworkFile(fileResource, testContext.secondFileHandle);
                  driver.closeFile(testSession, testConnection, testContext.secondFileHandle);   
                  
                  return null;
              }
          };
          tran.doInTransaction(writeFileCB, false, true);
          
          /**
           * b) rename the old file
           */
          logger.debug("c) rename old file");
          RetryingTransactionCallback<Void> renameOldFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                  return null;
              }
          };
          tran.doInTransaction(renameOldFileCB, false, true);
             
          /**
           * c) Move the new file into place, stuff should get shuffled
           */
          logger.debug("d) move new file into place");
          RetryingTransactionCallback<Void> moveNewFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME); 
                  return null;
              }
          };
          
          tran.doInTransaction(moveNewFileCB, false, true);
          
          /**
           * d) Delete the old file
           */
          logger.debug("d) delete on close the old file");
          RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  FileOpenParams openFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_OLD_TEMP, 0, AccessMode.NTReadAttributesOnly, FileAttribute.NTNormal, 0);
                  testContext.secondFileHandle = driver.openFile(testSession, testConnection, openFileParams);
                  assertNotNull(testContext.secondFileHandle);
                  
                  FileInfo info = new FileInfo();
                  info.setFileInformationFlags(FileInfo.SetDeleteOnClose);
                  driver.setFileInformation(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP, info);
                  testContext.secondFileHandle.setDeleteOnClose(true);
                  
                  driver.closeFile(testSession, testConnection, testContext.secondFileHandle);
                  return null;
              }
          };
          
          tran.doInTransaction(deleteOldFileCB, false, true);
          
          logger.debug("e) validate results");
          
          /**
           * Now validate everything is correct
           */
          RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                 NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 assertTrue("file has lost versionable aspect", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
                 assertEquals("node ref has changed", shuffledNodeRef, testContext.testNodeRef);
                 
                 
                 Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
                 ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                 assertNotNull("data is null", data);
                 assertEquals("size is wrong", 10407, data.getSize());
                 assertEquals("mimeType is wrong", "image/jpeg", data.getMimetype());
                 
                 // TODO - test metadata extraction
                 //assertEquals(false, props.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "flash")));

             
                 return null;
              }
          };
          
          tran.doInTransaction(validateCB, true, true);
      }  // Test Word 7 Explorer Update

      /**
       * 0. test.txt and ~test.txt exist. 
       * 1. Delete test.txt 
       * 2. Rename test.txt~ to test.txt 
       */
      public void testNFS() throws Exception
      {
          logger.debug("testNFS()");
          
          final String FILE_NAME = "test.txt";
          final String FILE_NAME_TEMP = "test.txt~";
          
          class TestContext
          {
              NetworkFile firstFileHandle;
              NetworkFile tempFileHandle;
              NodeRef file1NodeRef;
          };
          
          final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testNFS";
          
          ServerConfiguration scfg = new ServerConfiguration("testServer");
          TestServer testServer = new TestServer("testServer", scfg);
          final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
          DiskSharedDevice share = getDiskSharedDevice();
          final TreeConnection testConnection = testServer.getTreeConnection(share);
          final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();        

          /**
           * Clean up just in case garbage is left from a previous run
           */
          RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                  return null;
              }
          };
          RetryingTransactionCallback<Void> deleteGarbageFileCB2 = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME_TEMP);
                  return null;
              }
          };
       
          /**
           * Create a file in the test directory
           */    
          
          try
          {
              logger.debug("expect to get exception - cleaning garbage");
              tran.doInTransaction(deleteGarbageFileCB);
          }
          catch (Exception e)
          {
              // expect to go here
          }
          try
          {
              logger.debug("expect to get exception - cleaning garbage");
              tran.doInTransaction(deleteGarbageFileCB2);
          }
          catch (Exception e)
          {
              // expect to go here
          }
          
          logger.debug("0) create new file");
          RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>() {
              @Override
              public TestContext execute() throws Throwable
              {
    
                  TestContext ctx = new TestContext();
                  /**
                   * Create the test directory we are going to use 
                   */
                  FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                  FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                  driver.createDirectory(testSession, testConnection, createRootDirParams);
                  driver.createDirectory(testSession, testConnection, createDirParams);
                  
                  /**
                   * Create the file we are going to use 
                   */
                  {
                      FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);  
                      ctx.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                      assertNotNull(ctx.firstFileHandle);              
                      driver.closeFile(testSession, testConnection, ctx.firstFileHandle); 
                      ctx.file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                  }
                  {
                      FileOpenParams createTempFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                      ctx.tempFileHandle = driver.createFile(testSession, testConnection, createTempFileParams);
                      assertNotNull(ctx.tempFileHandle);              
                      driver.closeFile(testSession, testConnection, ctx.tempFileHandle); 
                  }        
                  return ctx;
              }
          };
          final TestContext testContext = tran.doInTransaction(setupCB, false, true);
          
          /**
           * 1) delete the old file
           */
          logger.debug("1) delete old file");
          RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                  return null;
              }
          };
          tran.doInTransaction(deleteOldFileCB, false, true);
          
          logger.debug("2) remame temp file");
          RetryingTransactionCallback<Void> renameTempFileCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                  driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME_TEMP, TEST_DIR + "\\" + FILE_NAME);
                  return null;
              }
          };
          tran.doInTransaction(renameTempFileCB, false, true);

          logger.debug("3) validate results");
          /**
           * Now validate everything is correct
           */
          RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {

              @Override
              public Void execute() throws Throwable
              {
                 NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                 
                 Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
                                 
//                 assertTrue("versionable aspect missing", nodeService.hasAspect(shuffledNodeRef, ContentModel.ASPECT_VERSIONABLE));
             
                 assertEquals("Node ref has changed", shuffledNodeRef, testContext.file1NodeRef);
                 return null;
              }
          };
          
          tran.doInTransaction(validateCB, true, true);
          logger.debug("end testNFS");
          
      } // testNFS

    private void doTransactionWorkAsEditor(final RunAsWork<Void> work, RetryingTransactionHelper tran)
    {
        RetryingTransactionCallback<Void> transactionCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    AuthenticationUtil.runAs(work, ContentDiskDriverTest.TEST_USER_AUTHORITY);
                }
                catch (Exception e)
                {
                    // Informing about test failure. Expected exception is 'AccessDeniedException' or 'PermissionDeniedException'
                    if (e.getCause() instanceof AccessDeniedException || e.getCause() instanceof PermissionDeniedException)
                    {
                        fail("For user='" + TEST_USER_AUTHORITY + "' " + e.getCause().toString());
                    }
                    else
                    {
                        fail("Unexpected exception was caught: " + e.toString());
                    }
                }
                return null;
            }
        };
        tran.doInTransaction(transactionCallback, false, true);
    }

    /**
     * 0. test.txt exist in folder where user has Editor permissions
     * 1. as Editor create temporary file in temporary directory
     * 2. as Editor rename test.txt to test.txt.sb-1eefba7a-rkC6XE
     * 3. as Editor move temporary file to working directory
     */
    public void testScenarioMacLionTextEditByEditor_ALF_16257() throws Exception
    {
        logger.debug("test Collaborator/editor edit txt file on Mac Os Mountain Lion : Alf16257");
        final String FILE_NAME = "test.txt";
        final String FILE_BACKUP = "test.txt.sb-1eefba7a-rkC6XE";
        final String FILE_NEW_TEMP = FILE_NAME; // the same but in temp dir

        class TestContext
        {
            NetworkFile firstFileHandle;
            NetworkFile newFileHandle;
            NodeRef testNodeRef; // node ref of FILE_NAME
        };
        final TestContext testContext = new TestContext();

        final String TEST_ROOT_DIR = "\\ContentDiskDriverTest";
        final String TEST_DIR = TEST_ROOT_DIR + "\\testALF16257txt";
        final String TEST_TEMP_DIR = TEST_DIR + "\\.TemporaryItems";
        final String UPDATED_TEXT = "This is new content";

        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "cifs", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }

        logger.debug("Step 0 - initialise");
        /**
         * Create a file in the test directory
         */
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                driver.createDirectory(testSession, testConnection, createTempDirParams);

                /**
                 * Create the file we are going to use
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);

                // no need to test lots of different properties, that's already been tested above
                testContext.testNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.setProperty(testContext.testNodeRef, TransferModel.PROP_ENABLED, true);
                nodeService.addAspect(testContext.testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);

                String testContent = "CIFS: Collaborator/editor could not edit file on Mac Os Mountain Lion";
                byte[] testContentBytes = testContent.getBytes();
                testContext.firstFileHandle.writeFile(testContentBytes, testContentBytes.length, 0, 0);
                testContext.firstFileHandle.close();

                // Apply 'Editor' role for test user to test folder
                permissionService.setPermission(getNodeForPath(testConnection, TEST_DIR), ContentDiskDriverTest.TEST_USER_AUTHORITY, PermissionService.EDITOR, true);
                // Apply full control on temporary directory
                permissionService.setPermission(getNodeForPath(testConnection, TEST_TEMP_DIR), PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);

                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);

        /**
         * a) Save the temp file in the temp dir
         */
        logger.debug("Step a - create a temp file in the temp dir");
        RunAsWork<Void> saveNewFileCB = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.newFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.newFileHandle);

                byte[] testContentBytes = UPDATED_TEXT.getBytes();
                driver.writeFile(testSession, testConnection, testContext.newFileHandle, testContentBytes, 0, testContentBytes.length, 0);
                driver.closeFile(testSession, testConnection, testContext.newFileHandle);

                NodeRef tempNodeRef = getNodeForPath(testConnection, TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP);
                ContentReader reader = contentService.getReader(tempNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull(reader);
                String actualContent = reader.getContentString();
                assertEquals("new contents were not written to temporary file", UPDATED_TEXT, actualContent);
                return null;
            }
        };
        doTransactionWorkAsEditor(saveNewFileCB, tran);

        /**
         * b) rename the target file to a backup file
         */
        logger.debug("Step b - rename the target file as Editor");
        RunAsWork<Void> renameOldFileCB = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_BACKUP);
                return null;
            }
        };
        doTransactionWorkAsEditor(renameOldFileCB, tran);

        /**
         * c) Move the new file into target dir, stuff should get shuffled
         */
        logger.debug("Step c - move new file into target dir as Editor");
        RunAsWork<Void> moveNewFileCB = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        doTransactionWorkAsEditor(moveNewFileCB, tran);

        /**
         * Validate
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);

                Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
                assertTrue("node does not contain shuffled ENABLED property", props.containsKey(TransferModel.PROP_ENABLED));
                assertEquals("name wrong", FILE_NAME, nodeService.getProperty(shuffledNodeRef, ContentModel.PROP_NAME));
                ContentReader reader = contentService.getReader(testContext.testNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull(reader);
                String actualContent = reader.getContentString();
                assertEquals("contents were not updated", UPDATED_TEXT, actualContent);
                return null;
            }
        };
        tran.doInTransaction(validateCB, false, true);

        logger.debug("end testScenarioMacLionTextEditByEditor For ALF-16257");
    } // testScenarioMacLionTextEditByEditorForAlf16257

    /**
     * 0. MacWord1.docx exist in folder where user has Editor permissions
     * 1. as Editor rename MacWord1.docx to backup Word Work File L_5.tmp
     * 2. as Editor create temporary file in temporary directory and move it to working dir
     * 3. as Editor rename Word Work File D_2.tmp to MacWord1.docx
     */
    public void testScenarioMountainLionWord2011EditByEditor_ALF_16257() throws Exception
    {
        logger.debug("testScenarioMountainLionWord2011 Edit By Editor ALF-16257");

        final String FILE_NAME = "MacWord1.docx";
        final String FILE_OLD_TEMP = "Word Work File L_5.tmp";
        final String FILE_NEW_TEMP = "Word Work File D_2.tmp";

        class TestContext
        {
            NetworkFile firstFileHandle;
            String mimetype;
        };
        final TestContext testContext = new TestContext();

        final String TEST_DIR = TEST_ROOT_DOS_PATH + "\\testALF16257Word";
        final String TEST_TEMP_DIR = TEST_DIR + "\\.TemporaryItems"; // need to match with interimPattern

        ServerConfiguration scfg = new ServerConfiguration("testServer");
        TestServer testServer = new TestServer("testServer", scfg);
        final SrvSession testSession = new TestSrvSession(666, testServer, "test", "remoteName");
        DiskSharedDevice share = getDiskSharedDevice();
        final TreeConnection testConnection = testServer.getTreeConnection(share);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        /**
         * Clean up just in case garbage is left from a previous run
         */
        RetryingTransactionCallback<Void> deleteGarbageFileCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        try
        {
            tran.doInTransaction(deleteGarbageFileCB);
        }
        catch (Exception e)
        {
            // expect to go here
        }
        
        logger.debug("a) create new file");
        RetryingTransactionCallback<Void> createFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Create the test directory we are going to use 
                 */
                FileOpenParams createRootDirParams = new FileOpenParams(TEST_ROOT_DOS_PATH, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createDirParams = new FileOpenParams(TEST_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                FileOpenParams createTempDirParams = new FileOpenParams(TEST_TEMP_DIR, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                driver.createDirectory(testSession, testConnection, createRootDirParams);
                driver.createDirectory(testSession, testConnection, createDirParams);
                driver.createDirectory(testSession, testConnection, createTempDirParams);

                /**
                 * Create the file we are going to test
                 */
                FileOpenParams createFileParams = new FileOpenParams(TEST_DIR + "\\" + FILE_NAME, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);
                assertNotNull(testContext.firstFileHandle);
                
                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);
                writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle); 
                NodeRef file1NodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
                nodeService.addAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                
                // Apply 'Editor' role for test user to test folder
                permissionService.setPermission(getNodeForPath(testConnection, TEST_DIR), ContentDiskDriverTest.TEST_USER_AUTHORITY, PermissionService.EDITOR, true);
                // Apply full control on temporary directory
                permissionService.setPermission(getNodeForPath(testConnection, TEST_TEMP_DIR), PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);

                return null;
            }
        };
        tran.doInTransaction(createFileCB, false, true);
        
        /**
         * b) rename the old file, should fire doubleRenameShuffle scenario
         */
        logger.debug("b) rename old file");
        RunAsWork<Void> renameOldFileCB = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef file1NodeRef = getNodeForPath(testConnection,  TEST_DIR + "\\" + FILE_NAME);
                Map<QName, Serializable> props = nodeService.getProperties(file1NodeRef);
                ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
                testContext.mimetype = data.getMimetype();
                
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NAME, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        doTransactionWorkAsEditor(renameOldFileCB, tran);
        
        /**
         * c) as Editor Save the new file in .TemporaryItems
         * and move it to working directory (should be detected by scenario)
         * Write ContentDiskDriverTest3.doc,
         */
        logger.debug("c) create temp file in temp dir");
        RunAsWork<Void> writeFileCB = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                FileOpenParams createFileParams = new FileOpenParams(TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP, 0, AccessMode.ReadWrite, FileAttribute.NTNormal, 0);
                testContext.firstFileHandle = driver.createFile(testSession, testConnection, createFileParams);

                ClassPathResource fileResource = new ClassPathResource("filesys/ContentDiskDriverTest3.doc");
                assertNotNull("unable to find test resource filesys/ContentDiskDriverTest3.doc", fileResource);
                writeResourceToNetworkFile(fileResource, testContext.firstFileHandle);
                driver.closeFile(testSession, testConnection, testContext.firstFileHandle);

                driver.renameFile(testSession, testConnection, TEST_TEMP_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NEW_TEMP);
                return null;
            }
        };
        doTransactionWorkAsEditor(writeFileCB, tran);

        /**
         * d) Move the new file into place, stuff should get shuffled
         */
        logger.debug("d) move new file into place");
        RunAsWork<Void> moveNewFileCB = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                driver.renameFile(testSession, testConnection, TEST_DIR + "\\" + FILE_NEW_TEMP, TEST_DIR + "\\" + FILE_NAME);
                return null;
            }
        };
        doTransactionWorkAsEditor(moveNewFileCB, tran);

        /**
         * e) Delete the old file
         */
        logger.debug("e) delete the old file");
        RetryingTransactionCallback<Void> deleteOldFileCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                driver.deleteFile(testSession, testConnection, TEST_DIR + "\\" + FILE_OLD_TEMP);
                return null;
            }
        };
        
        tran.doInTransaction(deleteOldFileCB, false, true);
        
        logger.debug("e) validate results");

        logger.debug("f) validate results");
        /**
         * Now validate everything is correct
         */
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
               NodeRef shuffledNodeRef = getNodeForPath(testConnection, TEST_DIR + "\\" + FILE_NAME);
               
               Map<QName, Serializable> props = nodeService.getProperties(shuffledNodeRef);
               
               ContentData data = (ContentData)props.get(ContentModel.PROP_CONTENT);
               //assertNotNull("data is null", data);
               //assertEquals("size is wrong", 123904, data.getSize());
               
               NodeRef file1NodeRef = getNodeForPath(testConnection,  TEST_DIR + "\\" + FILE_NAME); 
               assertTrue("file has lost versionable aspect", nodeService.hasAspect(file1NodeRef, ContentModel.ASPECT_VERSIONABLE));

               assertEquals("mimeType is wrong", testContext.mimetype, data.getMimetype());
               
           
               return null;
            }
        };
        tran.doInTransaction(validateCB, true, true);
        logger.debug("end testScenarioMountainLionWord2011 Edit By Editor ALF-16257");
    } // testScenarioMountainLionWord2011EditByEditor_ALF_16257
    
    /**
     * Test server
     */
    public class TestServer extends NetworkFileServer
    {
        
        public TestServer(String proto, ServerConfiguration config)
        {
            super(proto, config);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void startServer()
        {
                       
        }

        @Override
        public void shutdownServer(boolean immediate)
        {
            
        }
        
        public TreeConnection getTreeConnection(SharedDevice share) 
        {
            return new TreeConnection(share);
        }
    }
    
    /**
     * TestSrvSession
     */
    private class TestSrvSession extends SrvSession
    {

        public TestSrvSession(int sessId, NetworkServer srv, String proto,
                String remName)
        {
            super(sessId, srv, proto, remName);
            
            // Set the client info to user "fred"
            ClientInfo cinfo = ClientInfo.createInfo("fred", null);
            setClientInformation(cinfo);
            setUniqueId("test:" + sessId);

        }

        @Override
        public InetAddress getRemoteAddress()
        {
            return null;
        }

        @Override
        public boolean useCaseSensitiveSearch()
        {
            return false;
        }
    }
    
    private NodeRef getNodeForPath(TreeConnection tree, String path)
    throws FileNotFoundException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getNodeRefForPath:" + path);
        }
   
        ContentContext ctx = (ContentContext) tree.getContext();
    
        return cifsHelper.getNodeRef(ctx.getRootNode(), path);
    }

    /**
     * Write the resource to the specified NetworkFile
     * @param resource
     * @param file
     * @throws IOException
     */
    private void writeResourceToNetworkFile(ClassPathResource resource, NetworkFile file) throws IOException
    {
 
        byte[] buffer= new byte[1000];
        InputStream is = resource.getInputStream();
        try
        {
            long offset = 0;
            int i = is.read(buffer, 0, buffer.length);
            while(i > 0)
            {
                file.writeFile(buffer, i, 0, offset);
                offset += i;
                i = is.read(buffer, 0, buffer.length);
            }                 
        }
        finally
        {
            is.close();
        }
    }
}
