/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TempFileProvider;
import org.springframework.util.ResourceUtils;

/**
 * Unit test for TransferServiceImpl
 * 
 * Contains some integration tests for the transfer service
 *
 * @author Mark Rogers
 */
@SuppressWarnings("deprecation")
public class TransferServiceImplTest extends BaseAlfrescoSpringTest 
{
    private TransferService transferService;
    private ContentService contentService;
    private TransferServiceImpl2 transferServiceImpl;
    private SearchService searchService;
    private TransactionService transactionService;
    private TransferReceiver receiver;
    private TransferManifestNodeFactory transferManifestNodeFactory; 
    private PermissionService permissionService;
    private LockService lockService;
    private PersonService personService;
    private DescriptorService descriptorService;
    private CopyService copyService;
    private Descriptor serverDescriptor;
    
    String COMPANY_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home";
    String GUEST_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}guest_home";

    String REPO_ID_A = "RepoIdA";
    String REPO_ID_B;
    String REPO_ID_C = "RepoIdC";
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUp() throws Exception
    {
       
        super.onSetUp();
        
        // Get the required services
        this.transferService = (TransferService)this.applicationContext.getBean("TransferService");
        this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        this.transferServiceImpl = (TransferServiceImpl2)this.applicationContext.getBean("transferService2");
        this.searchService = (SearchService)this.applicationContext.getBean("SearchService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("TransactionService");
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        this.permissionService = (PermissionService)this.applicationContext.getBean("permissionService");
        this.receiver = (TransferReceiver)this.applicationContext.getBean("transferReceiver");
        this.transferManifestNodeFactory = (TransferManifestNodeFactory)this.applicationContext.getBean("transferManifestNodeFactory");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        this.lockService = (LockService) this.applicationContext.getBean("lockService");
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.descriptorService = (DescriptorService)this.applicationContext.getBean("DescriptorService");
        this.copyService = (CopyService)this.applicationContext.getBean("CopyService");
        
        this.serverDescriptor = descriptorService.getServerDescriptor();
        
        REPO_ID_B = descriptorService.getCurrentRepositoryDescriptor().getId();
        
        authenticationComponent.setSystemUserAsCurrentUser();
        assertNotNull("receiver is null", this.receiver);     
    }
    
    @Override
    public void runBare() throws Throwable
    {
        preventTransaction();
        super.runBare();
    }
    
    public void testSetup()
    {
        assertEquals(
                "Must run without transactions",
                TxnReadState.TXN_NONE, AlfrescoTransactionSupport.getTransactionReadState());
    }
    
    /**
     * Test create target.
     * 
     * @throws Exception
     */
    public void testCreateTarget() throws Exception
    {
        String name = "name";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();


        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget ret = transferService.createAndSaveTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        assertNotNull("return value is null", ret);
        assertNotNull("node ref is null", ret.getNodeRef());

        //titled aspect
        assertEquals("name not equal", ret.getName(), name);
        assertEquals("title not equal", ret.getTitle(), title);
        assertEquals("description not equal", ret.getDescription(), description);

        // endpoint 
        assertEquals("endpointProtocol not equal", ret.getEndpointProtocol(), endpointProtocol);
        assertEquals("endpointHost not equal", ret.getEndpointHost(), endpointHost);
        assertEquals("endpointPort not equal", ret.getEndpointPort(), endpointPort);
        assertEquals("endpointPath not equal", ret.getEndpointPath(), endpointPath);

        // authentication
        assertEquals("username not equal", ret.getUsername(), username);
        char[] password2 = ret.getPassword();

        assertEquals(password.length, password2.length);

        for(int i = 0; i < password.length; i++)
        {
            if(password[i] != password2[i])
            {
                fail("password not equal:" + new String(password) + new String(password2));
            }
        }

        /**
         * Negative test - try to create a transfer target with a name that's already used.
         */
        try 
        {    
            transferService.createAndSaveTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
            fail("duplicate name not detected");
        }
        catch (TransferException e)
        {
            // expect to go here
        }
    }
    
    /**
     * Test create target via in memory data object.
     * 
     * @throws Exception
     */
    public void testCreateTargetSyntax2() throws Exception
    {
        String name = "nameSyntax2";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();

        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget newValue = transferService.createTransferTarget(name);
        newValue.setDescription(description);
        newValue.setEndpointHost(endpointHost);
        newValue.setEndpointPort(endpointPort);
        newValue.setEndpointPath(endpointPath);
        newValue.setEndpointProtocol(endpointProtocol);
        newValue.setPassword(password);
        newValue.setTitle(title);
        newValue.setUsername(username);

        TransferTarget ret = transferService.saveTransferTarget(newValue);

        assertNotNull("return value is null", ret);
        assertNotNull("node ref is null", ret.getNodeRef());

        //titled aspect
        assertEquals("name not equal", ret.getName(), name);
        assertEquals("title not equal", ret.getTitle(), title);
        assertEquals("description not equal", ret.getDescription(), description);

        // endpoint 
        assertEquals("endpointProtocol not equal", ret.getEndpointProtocol(), endpointProtocol);
        assertEquals("endpointHost not equal", ret.getEndpointHost(), endpointHost);
        assertEquals("endpointPort not equal", ret.getEndpointPort(), endpointPort);
        assertEquals("endpointPath not equal", ret.getEndpointPath(), endpointPath);

        // authentication
        assertEquals("username not equal", ret.getUsername(), username);
        char[] password2 = ret.getPassword();

        assertEquals(password.length, password2.length);

        for(int i = 0; i < password.length; i++)
        {
            if(password[i] != password2[i])
            {
                fail("password not equal:" + new String(password) + new String(password2));
            }
        }

        /**
         * Negative test - try to create a transfer target with a name that's already used.
         */
        try 
        {

            transferService.createAndSaveTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
            fail("duplicate name not detected");
        }
        catch (TransferException e)
        {
            // expect to go here
        }
    }
    
    /**
     * Test of Get TransferTargets
     * 
     * @throws Exception
     */
    public void testGetTransferTargets() throws Exception
    {
        String nameA = "nameA";
        String nameB = "nameB";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();

        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget targetA = transferService.createAndSaveTransferTarget(nameA, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        TransferTarget targetB = transferService.createAndSaveTransferTarget(nameB, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);

        Set<TransferTarget> targets = transferService.getTransferTargets();
        assertTrue("targets is empty", targets.size() > 0);
        assertTrue("didn't find target A", targets.contains(targetA) );
        assertTrue("didn't find target B", targets.contains(targetB));
        for(TransferTarget target : targets)
        {
            System.out.println("found target: " + target.getName());
        }
    }
    
    /**
     * Test that if someone copies a transfer group using a client app then the getTransferTarget operations still succeed
     * 
     * @throws Exception
     */
    public void testALF6565() throws Exception
    {
        String nameA = GUID.generate();
        String nameB = GUID.generate();
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();

        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget targetA = transferService.createAndSaveTransferTarget(nameA, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        TransferTarget targetB = transferService.createAndSaveTransferTarget(nameB, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);

        NodeRef transferHome = transferServiceImpl.getTransferHome();
        NodeRef defaultGroup = transferServiceImpl.getDefaultGroup();
        assertNotNull(defaultGroup);
        copyService.copyAndRename(defaultGroup, transferHome, ContentModel.ASSOC_CONTAINS, QName.createQName("test"), true);
        
        Set<TransferTarget> targets = transferService.getTransferTargets();

        int targetACount = 0;
        int targetBCount = 0;
        for (TransferTarget target : targets)
        {
            if (target.getName().equals(nameA)) ++targetACount;
            if (target.getName().equals(nameB)) ++targetBCount;
        }
        assertEquals(2, targetACount);
        assertEquals(2, targetBCount);

        assertEquals(targetA.getNodeRef(), transferService.getTransferTarget(nameA).getNodeRef());
        assertEquals(targetB.getNodeRef(), transferService.getTransferTarget(nameB).getNodeRef());
    }
    
    /**
     * Test of Get All Transfer Targets By Group
     */
    //TODO Test not complete - can't yet put targets in different groups
    public void testGetAllTransferTargetsByGroup() throws Exception
    {
        String getMe = "getMe";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();

        /**
         * Now go ahead and create our first transfer target
         */
        transferService.createAndSaveTransferTarget(getMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);

        Set<TransferTarget> targets = transferService.getTransferTargets("Default Group");
        assertTrue("targets is empty", targets.size() > 0);     
        /**
         * Negative test - group does not exist
         */
        try {
            targets = transferService.getTransferTargets("Rubbish");
            assertTrue("targets is empty", targets.size() > 0);
            fail("group does not exist");
        }
        catch (TransferException te)
        {
            // expect to go here
        }

    }
    
    /**
     * 
     */
    public void testUpdateTransferTarget() throws Exception
    {
        String updateMe = "updateMe";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();

        /**
         * Create our transfer target
         */
        TransferTarget target = transferService.createAndSaveTransferTarget(updateMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);

        /*
         * Now update with exactly the same values.
         */
        TransferTarget update1 = transferService.saveTransferTarget(target);

        assertNotNull("return value is null", update1);
        assertNotNull("node ref is null", update1.getNodeRef());

        //titled aspect
        assertEquals("name not equal", update1.getName(), updateMe);
        assertEquals("title not equal", update1.getTitle(), title);
        assertEquals("description not equal", update1.getDescription(), description);

        // endpoint 
        assertEquals("endpointProtocol not equal", update1.getEndpointProtocol(), endpointProtocol);
        assertEquals("endpointHost not equal", update1.getEndpointHost(), endpointHost);
        assertEquals("endpointPort not equal", update1.getEndpointPort(), endpointPort);
        assertEquals("endpointPath not equal", update1.getEndpointPath(), endpointPath);

        // authentication
        assertEquals("username not equal", update1.getUsername(), username);
        char[] pass = update1.getPassword();

        assertEquals(password.length, pass.length);

        for(int i = 0; i < password.length; i++)
        {
            if(password[i] != pass[i])
            {
                fail("password not equal:" + new String(password) + new String(pass));
            }
        }

        /**
         * Now update with different values
         */
        String title2 = "Two";
        String description2 = "descriptionTwo";
        String endpointProtocol2 = "https";
        String endpointHost2 = "1.0.0.127";
        int endpointPort2 = 4040;
        String endpointPath2 = "custard";
        String username2 = "admin_two";
        char[] password2 = "two".toCharArray();

        target.setDescription(description2);
        target.setTitle(title2);
        target.setEndpointHost(endpointHost2);
        target.setEndpointPath(endpointPath2);
        target.setEndpointPort(endpointPort2);
        target.setEndpointProtocol(endpointProtocol2);
        target.setPassword(password2);
        target.setUsername(username2);

        TransferTarget update2 = transferService.saveTransferTarget(target);
        assertNotNull("return value is null", update2);
        assertNotNull("node ref is null", update2.getNodeRef());

        //titled aspect
        assertEquals("name not equal", update2.getName(), updateMe);
        assertEquals("title not equal", update2.getTitle(), title2);
        assertEquals("description not equal", update2.getDescription(), description2);

        // endpoint 
        assertEquals("endpointProtocol not equal", update2.getEndpointProtocol(), endpointProtocol2);
        assertEquals("endpointHost not equal", update2.getEndpointHost(), endpointHost2);
        assertEquals("endpointPort not equal", update2.getEndpointPort(), endpointPort2);
        assertEquals("endpointPath not equal", update2.getEndpointPath(), endpointPath2);

        // authentication
        assertEquals("username not equal", update2.getUsername(), username2);
        pass = update2.getPassword();

        assertEquals(password2.length, pass.length);

        for(int i = 0; i < pass.length; i++)
        {
            if(pass[i] != password2[i])
            {
                fail("password not equal:" + new String(pass) + new String(password2));
            }
        }
    }
    
    /**
     * 
     */
    public void testDeleteTransferTarget() throws Exception
    {
        String deleteMe = "deleteMe";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();

        /**
         * Now go ahead and create our first transfer target
         */
        transferService.createAndSaveTransferTarget(deleteMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);

        transferService.deleteTransferTarget(deleteMe);

        /**
         * Negative test - try to delete the transfer target we deleted above.
         */
        try 
        {
            transferService.deleteTransferTarget(deleteMe);
            fail("duplicate name not detected");
        }
        catch (TransferException e)
        {
            // expect to go here
        }   

        /**
         * Negative test - try to delete a transfer target that has never existed
         */
        try 
        {
            transferService.deleteTransferTarget("rubbish");

            fail("rubbish deleted");
        }
        catch (TransferException e)
        {
            // expect to go here
        }

    }
    
    public void testEnableTransferTarget() throws Exception
    {
        String targetName = "enableMe";

        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget enableMe = createTransferTarget(targetName);
        try 
        {
            /**
             * Check a new target is enabled
             */
            TransferTarget target = transferService.getTransferTarget(targetName);
            assertTrue("new target is not enabled", enableMe.isEnabled());

            /**
             * Diasble the target
             */
            transferService.enableTransferTarget(targetName, false);
            target = transferService.getTransferTarget(targetName);
            assertFalse("target is not disabled", target.isEnabled());

            /**
             * Now re-enable the target
             */
            transferService.enableTransferTarget(targetName, true);
            target = transferService.getTransferTarget(targetName);
            assertTrue("re-enabled target is not enabled", target.isEnabled());
        }
        finally
        {
            transferService.deleteTransferTarget(targetName);
        }
    }

    /**
     * Test the transfer method by sending one node (CRUD).
     * 
     * Step 1: Create a new node (No content)
     * transfer
     * 
     * Step 2: Update Node title property
     * transfer
     *  
     * Step 3: Update Content property (add content)
     * transfer
     * 
     * Step 4: Transfer again
     * transfer (Should transfer but not request the content item)
     * 
     * Step 5: Update Content property (update content)
     *      
     * Step 6: Delete the node
     * 
     * Step 7: Negative test : transfer no nodes
     * transfer (should throw exception)
     * 
     * Step 8: Negative test : transfer to a disabled transfer target
     * transfer (should throw exception)
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testTransferOneNode() throws Exception
    {
        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "Hello World";
        final String CONTENT_UPDATE_STRING = "Foo Bar";
        final String targetName = "testXferOneNode";

        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        class TestContext
        {
            TransferTarget transferMe;
            NodeRef contentNodeRef;
            NodeRef destNodeRef;
        };

        /**
         * Unit test kludge to transfer from guest home to company home
         */
        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();
        
        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();
                
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0); 

                /**
                 * Create a test node that we will read and write
                 */
                String name = GUID.generate();
                ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_CONTENT);
                ctx.contentNodeRef = child.getChildRef();
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_NAME, name);

                if(!transferService.targetExists(targetName))
                {
                    ctx.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    ctx.transferMe = transferService.getTransferTarget(targetName);
                }
                transferService.enableTransferTarget(targetName, true);

                return ctx;
            } 
        };
        final TestContext testContext = tran.doInTransaction(setupCB); 

        /**
         * Step 1: Transfer our node which has no content
         */ 
        logger.debug("First transfer - create new node (no content yet)");
        RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
                return null;
            }
        };
        tran.doInTransaction(transferCB);
        
        RetryingTransactionCallback<Void> validateStep1CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testContext.destNodeRef = testNodeFactory.getMappedNodeRef( testContext.contentNodeRef);
                assertFalse("unit test stuffed up - comparing with self",  testContext.destNodeRef.equals( testContext.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists( testContext.destNodeRef));
                assertEquals("title is wrong", (String)nodeService.getProperty( testContext.destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE); 
                assertEquals("type is wrong", nodeService.getType( testContext.contentNodeRef), nodeService.getType( testContext.destNodeRef));
                
                // Check the modified time of the destination node is the same as the source node.
                Date destModifiedDate = (Date)nodeService.getProperty( testContext.destNodeRef, ContentModel.PROP_MODIFIED);
                Date srcModifiedDate = (Date)nodeService.getProperty( testContext.contentNodeRef, ContentModel.PROP_MODIFIED);
                
                logger.debug("srcModifiedDate : " + srcModifiedDate + " destModifiedDate : " + destModifiedDate);
                assertTrue("dest modified date is not correct", destModifiedDate.compareTo(srcModifiedDate)== 0);
                
                Date destCreatedDate = (Date)nodeService.getProperty( testContext.destNodeRef, ContentModel.PROP_CREATED);
                Date srcCreatedDate = (Date)nodeService.getProperty( testContext.contentNodeRef, ContentModel.PROP_CREATED);
                
                logger.debug("srcCreatedDate : " + srcCreatedDate + " destCreatedDate : " + destCreatedDate);
                assertTrue("dest created date is not correct", destCreatedDate.compareTo(srcCreatedDate)== 0);
                
                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String)nodeService.getProperty( testContext.destNodeRef, TransferModel.PROP_REPOSITORY_ID)); 
                
                // Now set up the next test which is to change the title 
                nodeService.setProperty( testContext.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE_UPDATED);   
                return null;
            }
        };
        tran.doInTransaction(validateStep1CB);
        
        /**
         * Step 2:
         * Transfer our node again - so this is an update of the title property
         */
        logger.debug("Second transfer - update title property (no content yet)");
        tran.doInTransaction(transferCB);
     
        RetryingTransactionCallback<Void> validateStep2CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {

                // Now validate that the target node exists and has similar properties to the source
                assertFalse("unit test stuffed up - comparing with self", testContext.destNodeRef.equals(testContext.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists(testContext.destNodeRef));
                assertEquals("title is wrong", (String)nodeService.getProperty(testContext.destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE_UPDATED); 
                assertEquals("type is wrong", nodeService.getType(testContext.contentNodeRef), nodeService.getType(testContext.destNodeRef));
                
                // Check the modified time of the destination node is the same as the source node.
                Date destModifiedDate = (Date)nodeService.getProperty(testContext.destNodeRef, ContentModel.PROP_MODIFIED);
                Date srcModifiedDate = (Date)nodeService.getProperty(testContext.contentNodeRef, ContentModel.PROP_MODIFIED);

                logger.debug("srcModifiedDate : " + srcModifiedDate + " destModifiedDate : " + destModifiedDate);
                assertTrue("after update, modified date is not correct", destModifiedDate.compareTo(srcModifiedDate) == 0);
                
                Date destCreatedDate = (Date)nodeService.getProperty(testContext.destNodeRef, ContentModel.PROP_CREATED);
                Date srcCreatedDate = (Date)nodeService.getProperty(testContext.contentNodeRef, ContentModel.PROP_CREATED);
                
                logger.debug("srcCreatedDate : " + srcCreatedDate + " destCreatedDate : " + destCreatedDate);
                assertTrue("after update, created date is not correct", destCreatedDate.compareTo(srcCreatedDate)== 0);
          
                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String)nodeService.getProperty(testContext.destNodeRef, TransferModel.PROP_REPOSITORY_ID)); 
              
                return null;
            }
        };

        tran.doInTransaction(validateStep2CB);
        
        /**
         * Step 3 - update to add content
         */
        
        RetryingTransactionCallback<Void> step3WriteContentCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(testContext.contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);

                return null;
            }
        };

        tran.doInTransaction(step3WriteContentCB);

       
        logger.debug("Transfer again - this is an update to add new content");
        tran.doInTransaction(transferCB);
        
        RetryingTransactionCallback<Void> validateStep3CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testContext.destNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("reader is null", reader);
                String contentStr = reader.getContentString();
                assertEquals("Content is wrong", contentStr, CONTENT_STRING);

              
                return null;
            }
        };

        tran.doInTransaction(validateStep3CB);
  
        /**
         * Step 4:
         * Now transfer nothing - content items do not need to be transferred since its already on 
         * the destination.
         */
        logger.debug("Transfer again - with no new content");
        tran.doInTransaction(transferCB);
        
        RetryingTransactionCallback<Void> validateStep4CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node still exists and in particular that the old content is still there
                assertFalse("unit test stuffed up - comparing with self", testContext.destNodeRef.equals(testContext.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists(testContext.destNodeRef));
                assertEquals("title is wrong", (String)nodeService.getProperty(testContext.destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE_UPDATED); 
                assertEquals("type is wrong", nodeService.getType(testContext.contentNodeRef), nodeService.getType(testContext.destNodeRef));
                
                ContentReader reader = contentService.getReader(testContext.destNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("reader is null", reader);
                String contentStr = reader.getContentString();
                assertEquals("Content is wrong", contentStr, CONTENT_STRING);
                
                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String)nodeService.getProperty(testContext.destNodeRef, TransferModel.PROP_REPOSITORY_ID));               
                return null;
            }
        };

        tran.doInTransaction(validateStep4CB);

                    
        /**
         * Step 5 - update content through transfer
         */
        
        RetryingTransactionCallback<Void> step5UpdateContentCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(testContext.contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_UPDATE_STRING);
              
                return null;
            }
        };

        tran.doInTransaction(step5UpdateContentCB);

        
        logger.debug("Transfer again - this is an update to add new content");
        tran.doInTransaction(transferCB);
        
        RetryingTransactionCallback<Void> validateStep5CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testContext.destNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("reader is null", reader);
                String contentStr = reader.getContentString();
                assertEquals("Content is wrong", CONTENT_UPDATE_STRING, contentStr);

              
                return null;
            }
        };

        tran.doInTransaction(validateStep5CB);
      
        /**
         * Step 6
         * Delete the node through transfer of the archive node
         */
        logger.debug("Transfer again - to delete a node through transferring an archive node");
        RetryingTransactionCallback<Void> step6CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testContext.contentNodeRef);
                return null;
            }
        };

        tran.doInTransaction(step6CB);
        
        RetryingTransactionCallback<Void> transferDeletedCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef deletedContentNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testContext.contentNodeRef.getId());
                
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(deletedContentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
              
                return null;
            }
        };

        tran.doInTransaction(transferDeletedCB);
       
        RetryingTransactionCallback<Void> validateStep6CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                assertFalse("dest node still exists", nodeService.exists(testContext.destNodeRef));              
                return null;
            }
        };

        tran.doInTransaction(validateStep6CB);

        /**
          * Step 7
          * Negative test transfer nothing
          */
        logger.debug("Transfer again - with no content - should throw exception");
        try
        {
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
                fail("exception not thrown");
        }
        catch(TransferException te)
        {
            // expect to go here
        }
       
        /**
         * Step 7: Negative test : transfer to a disabled transfer target
         * transfer (should throw exception)
         */ 
        logger.debug("Transfer again - with no content - should throw exception");
        try
        {
                transferService.enableTransferTarget(targetName, false);
                TransferDefinition definition = new TransferDefinition();
                
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
                fail("target not enabled exception not thrown");
        }
        catch(TransferException te)
        {
           // expect to go here
           assertTrue("check contents of exception message :" + te.toString(), te.getCause().getMessage().contains("enabled"));
        }
     }
    
    /**
     * Test the transfer method w.r.t. moving a node.
     * 
     * Step 1.
     * Move by changing the parent's node ref.
     *  
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testMoveNode() throws Exception
    {
        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "Hello";

        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        /**
         * Now go ahead and create our first transfer target
         */
        final String targetName = "testTransferMoveNode";
        
        class TestContext
        {
            TransferTarget transferMe;
            NodeRef contentNodeRef;
            NodeRef parentNodeRef;
            NodeRef destNodeRef;
            NodeRef moveToNodeRef;
        };

        /**
         * Unit test kludge to transfer from guest home to company home
         */
        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();

        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();

                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0); 

                /**
                 * Create a test node that we will read and write
                 */
                String name = GUID.generate();

                ChildAssociationRef newParent = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                ctx.parentNodeRef = newParent.getChildRef();
                nodeService.setProperty(ctx.parentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.parentNodeRef, ContentModel.PROP_NAME, name);

                ChildAssociationRef child = nodeService.createNode(ctx.parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("TransferOneNode"), ContentModel.TYPE_CONTENT);
                ctx.contentNodeRef = child.getChildRef();
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_NAME, "TransferOneNode");

                ChildAssociationRef moveTo = nodeService.createNode(ctx.parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("moveTo"), ContentModel.TYPE_FOLDER);
                ctx.moveToNodeRef = moveTo.getChildRef();
                nodeService.setProperty(ctx.moveToNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.moveToNodeRef, ContentModel.PROP_NAME, "moveTo");

                if(!transferService.targetExists(targetName))
                {
                    ctx.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    ctx.transferMe = transferService.getTransferTarget(targetName);
                }
                transferService.enableTransferTarget(targetName, true);


                return ctx;
            } 
        };
        
        final TestContext testContext = tran.doInTransaction(setupCB); 

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        RetryingTransactionCallback<Void> firstTransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                logger.debug("First transfer - create new node (no content yet)");

                /**
                 * Step 0: Transfer our node which has no content
                 */

                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.parentNodeRef);
                nodes.add(testContext.contentNodeRef);
                nodes.add(testContext.moveToNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);

                return null;
            }
        };

        tran.doInTransaction(firstTransferCB);

        RetryingTransactionCallback<Void> validateTransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {

                // Now validate that the target node exists and has similar properties to the source
                NodeRef destNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                NodeRef destParentNodeRef = testNodeFactory.getMappedNodeRef(testContext.parentNodeRef);

                ChildAssociationRef destParent = nodeService.getPrimaryParent(destNodeRef);
                assertEquals("parent node ref not correct prior to test", destParentNodeRef, destParent.getParentRef());

                return null;
            }
        };

        tran.doInTransaction(validateTransferCB);


        /**
         * Step 1: Move a node through transfer
         * Move the destination node
         * transfer (Should transfer the destination node back)
         */

        RetryingTransactionCallback<Void> moveNodeCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                logger.debug("Transfer again with moved node");

                // Move the node up one level on the destination.
                nodeService.moveNode(testContext.contentNodeRef, testContext.moveToNodeRef, ContentModel.ASSOC_CONTAINS,  QName.createQName("testOneNode"));   
                return null;
            }
        };

        tran.doInTransaction(moveNodeCB);

        RetryingTransactionCallback<Void> secondTransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                logger.debug("Second transfer");

                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);

                return null;
            }
        };

        tran.doInTransaction(secondTransferCB);

        RetryingTransactionCallback<Void> secondValidateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef destNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                NodeRef destParentNodeRef = testNodeFactory.getMappedNodeRef(testContext.moveToNodeRef);

                ChildAssociationRef destParent = nodeService.getPrimaryParent(destNodeRef);
                assertEquals("node not moved", destParentNodeRef, destParent.getParentRef());

                return null;
            }
        };

        tran.doInTransaction(secondValidateCB);

    } // test move node


    /**
     * Test the transfer method by sending a graph of nodes.
     * 
     * This is a unit test so it does some shenanigans to send to he same instance of alfresco.
     */
    public void testManyNodes() throws Exception
    {        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final String CONTENT_NAME = "Demo Node 1";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "The quick brown fox";
        final Set<NodeRef>nodes = new HashSet<NodeRef>();

        final String targetName = "testManyNodes";
        
        class TestContext
        {
            TransferTarget transferMe;
            NodeRef nodeA = null;
            NodeRef nodeB = null;
            NodeRef nodeAA = null;
            NodeRef nodeAB = null;
            NodeRef nodeABA = null;
            NodeRef nodeABB = null;
            NodeRef nodeABC = null;
        };
        
        /**
         * Unit test kludge to transfer from guest home to company home
         */
        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();
        
        TransferTarget transferMe;
        
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();
                
                /**
                 * Get guest home
                 */
               String guestHomeQuery = "/app:company_home/app:guest_home";
               ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
               assertEquals("", 1, guestHomeResult.length());
               NodeRef guestHome = guestHomeResult.getNodeRef(0); 
       
               /**
                * Create a test node that we will read and write
                */
               String guid = GUID.generate();
               
               /**
                * Create a tree
                * ManyNodesRoot
                * AC (Content Node)
                * A (Folder)
                * --AA
                * --AB (Folder)
                * ----ABA (Folder)
                * -------- 100+ nodes
                * ----ABB
                * ----ABC   
                * B
                */
               ChildAssociationRef child;
               
               child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(guid), ContentModel.TYPE_FOLDER);
               NodeRef testRootNode = child.getChildRef();
               nodeService.setProperty(testRootNode , ContentModel.PROP_TITLE, guid);   
               nodeService.setProperty(testRootNode , ContentModel.PROP_NAME, guid);
               nodes.add(testRootNode);
            
               child = nodeService.createNode(testRootNode, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeAC"), ContentModel.TYPE_CONTENT);
               NodeRef nodeAC = child.getChildRef();
               nodeService.setProperty(nodeAC , ContentModel.PROP_TITLE, CONTENT_TITLE + "AC");   
                nodeService.setProperty(nodeAC , ContentModel.PROP_NAME, "DemoNodeAC");
            
                {
                    ContentWriter writer = contentService.getWriter(nodeAC , ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                    nodes.add(nodeAC);
                }
            
                child = nodeService.createNode(testRootNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeA"), ContentModel.TYPE_FOLDER);
                ctx.nodeA = child.getChildRef();
                nodeService.setProperty(ctx.nodeA , ContentModel.PROP_TITLE, "TestNodeA");   
                nodeService.setProperty(ctx.nodeA , ContentModel.PROP_NAME, "TestNodeA");
                nodes.add(ctx.nodeA);
            
                child = nodeService.createNode(testRootNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeB"), ContentModel.TYPE_FOLDER);
                ctx.nodeB = child.getChildRef();
                nodeService.setProperty(ctx.nodeB , ContentModel.PROP_TITLE, "TestNodeB");   
                nodeService.setProperty(ctx.nodeB , ContentModel.PROP_NAME, "TestNodeB");
                nodes.add(ctx.nodeB);
            
                child = nodeService.createNode(ctx.nodeA, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeAA"), ContentModel.TYPE_FOLDER);
                ctx.nodeAA = child.getChildRef();
                nodeService.setProperty(ctx.nodeAA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.nodeAA , ContentModel.PROP_NAME, "DemoNodeAA" );
                nodes.add(ctx.nodeAA);
            
                child = nodeService.createNode(ctx.nodeA, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeAB"), ContentModel.TYPE_FOLDER);
                ctx.nodeAB = child.getChildRef();
                nodeService.setProperty(ctx.nodeAB , ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.nodeAB , ContentModel.PROP_NAME, "DemoNodeAB" );
                nodes.add(ctx.nodeAB);
            
                child = nodeService.createNode(ctx.nodeAB, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeABA"), ContentModel.TYPE_FOLDER);
                ctx.nodeABA = child.getChildRef();
                nodeService.setProperty(ctx.nodeABA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.nodeABA , ContentModel.PROP_NAME, "DemoNodeABA" );
                nodes.add(ctx.nodeABA);
            
                child = nodeService.createNode(ctx.nodeAB, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeABB"), ContentModel.TYPE_FOLDER);
                ctx.nodeABB = child.getChildRef();
                nodeService.setProperty(ctx.nodeABB , ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.nodeABB , ContentModel.PROP_NAME, "DemoNodeABB" );
                nodes.add(ctx.nodeABB);
            
                child = nodeService.createNode(ctx.nodeAB, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeABC"), ContentModel.TYPE_FOLDER);
                ctx.nodeABC = child.getChildRef();
                nodeService.setProperty(ctx.nodeABC , ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.nodeABC , ContentModel.PROP_NAME, "DemoNodeABC" );
                nodes.add(ctx.nodeABC);
                
                /**
                 * Now go ahead and create our first transfer target
                 */
                if(!transferService.targetExists(targetName))
                {
                    ctx.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    ctx.transferMe = transferService.getTransferTarget(targetName);
                }       
                
                return ctx;
            } 
        };
        
        final TestContext testContext = tran.doInTransaction(setupCB); 
        
        RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
              
               return null;
            }
        };
        
        tran.doInTransaction(transferCB); 
        
        RetryingTransactionCallback<Void> checkTransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef destNodeA;
                NodeRef destNodeB;
                NodeRef destNodeAA;
                NodeRef destNodeAB;
                NodeRef destNodeABA;
                NodeRef destNodeABB;
                NodeRef destNodeABC;

                // Now validate that the target node exists and has similar properties to the source
                destNodeA = testNodeFactory.getMappedNodeRef(testContext.nodeA);
                assertFalse("unit test stuffed up - comparing with self", destNodeA.equals(testContext.transferMe.getNodeRef()));
                assertTrue("dest node ref A does not exist", nodeService.exists(destNodeA));
                assertEquals("title is wrong", (String)nodeService.getProperty(destNodeA, ContentModel.PROP_TITLE), "TestNodeA"); 
                assertEquals("type is wrong", nodeService.getType(testContext.nodeA), nodeService.getType(destNodeA));
                
                destNodeB = testNodeFactory.getMappedNodeRef(testContext.nodeB);
                assertTrue("dest node B does not exist", nodeService.exists(destNodeB));
                
                destNodeAA = testNodeFactory.getMappedNodeRef(testContext.nodeAA);
                assertTrue("dest node AA ref does not exist", nodeService.exists(destNodeAA));
                
                destNodeAB = testNodeFactory.getMappedNodeRef(testContext.nodeAB);
                assertTrue("dest node AB ref does not exist", nodeService.exists(destNodeAB));
                
                destNodeABA = testNodeFactory.getMappedNodeRef(testContext.nodeABA);
                assertTrue("dest node ABA ref does not exist", nodeService.exists(destNodeABA));
                
                destNodeABB = testNodeFactory.getMappedNodeRef(testContext.nodeABB);
                assertTrue("dest node ABB ref does not exist", nodeService.exists(destNodeABB));
                
                destNodeABC = testNodeFactory.getMappedNodeRef(testContext.nodeABC);
                assertTrue("dest node ABC ref does not exist", nodeService.exists(destNodeABC));
              
               return null;
            }
        };
        
        tran.doInTransaction(checkTransferCB); 
        
        /**
         * Update a single node (NodeAB) from the middle of the tree
         */
        RetryingTransactionCallback<Void> updateSingleNodeCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                nodeService.setProperty(testContext.nodeAB , ContentModel.PROP_TITLE, CONTENT_TITLE_UPDATED);   
                
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>toUpdate = new HashSet<NodeRef>();
                toUpdate.add(testContext.nodeAB);
                definition.setNodes(toUpdate);
                transferService.transfer(targetName, definition);
                         
               return null;
            }
        };
          
        tran.doInTransaction(updateSingleNodeCB); 
            
        /**
         * Now generate a large number of nodes
         */
        RetryingTransactionCallback<Void> generateLotsOfNodesCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                for(int i = 0; i < 100; i++)
                {
                    ChildAssociationRef child = nodeService.createNode(testContext.nodeABA, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate() + i), ContentModel.TYPE_CONTENT);
                
                    NodeRef nodeX = child.getChildRef();
                    nodeService.setProperty(nodeX , ContentModel.PROP_TITLE, CONTENT_TITLE + i);   
                    nodeService.setProperty(nodeX , ContentModel.PROP_NAME, CONTENT_NAME +i);
                    nodes.add(nodeX);
                    
                    ContentWriter writer = contentService.getWriter(nodeX, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING + i);
                }
      
               return null;
            }
        };
          
        tran.doInTransaction(generateLotsOfNodesCB); 
        
        /**
         * Transfer our transfer target nodes 
         */
        tran.doInTransaction(transferCB); 
      
    } // end many nodes
    
    /**
     * Test the path based update.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testPathBasedUpdate() throws Exception
    {
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final String CONTENT_NAME = GUID.generate();
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "Hello";
        final QName TEST_QNAME = QName.createQName(CONTENT_NAME);

        class TestContext
        {
            String targetName;
            NodeRef contentNodeRef;
            NodeRef newContentNodeRef;          
            NodeRef guestHome;
            ChildAssociationRef child;
        };

        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();
                ctx.targetName = GUID.generate();
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                ctx.guestHome = guestHomeResult.getNodeRef(0); 

                /**
                 * Create a test node that we will transfer.   Its path is what is important
                 */
                ctx.child = nodeService.createNode(ctx.guestHome, ContentModel.ASSOC_CONTAINS, TEST_QNAME, ContentModel.TYPE_CONTENT);
                ctx.contentNodeRef = ctx.child.getChildRef();
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_NAME, CONTENT_NAME);

                ContentWriter writer = contentService.getWriter(ctx.contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);

                /**
                 * Now go ahead and create our first transfer target
                 */
                if(!transferService.targetExists(ctx.targetName))
                {
                    createTransferTarget(ctx.targetName);
                }
                else
                {
                    transferService.getTransferTarget(ctx.targetName);
                }              

                return ctx;
            } 
        };

        final TestContext testContext = tran.doInTransaction(setupCB);

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        RetryingTransactionCallback<Void> step1CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target node
                 */     
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(testContext.targetName, definition);
                return null;
            }
        };
        tran.doInTransaction(step1CB);

        RetryingTransactionCallback<Void> transfer1CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef destNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                assertFalse("unit test stuffed up - comparing with self", destNodeRef.equals(testContext.contentNodeRef));
                assertTrue("dest node ref does not exist", nodeService.exists(destNodeRef));
                assertEquals("title is wrong", (String)nodeService.getProperty(destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE); 
                assertEquals("type is wrong", nodeService.getType(testContext.contentNodeRef), nodeService.getType(destNodeRef));

                /**
                 * Now delete the content node and re-create another one with the old path
                 */
                nodeService.deleteNode(testContext.contentNodeRef);

                testContext.child = nodeService.createNode(testContext.guestHome, ContentModel.ASSOC_CONTAINS, TEST_QNAME, ContentModel.TYPE_CONTENT);
                testContext.newContentNodeRef = testContext.child.getChildRef();
                nodeService.setProperty(testContext.newContentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE_UPDATED); 

                /**
                 * Transfer our node which is a new node (so will not exist on the back end) with a path that already has a node.
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef>nodes = new HashSet<NodeRef>();
                    nodes.add(testContext.newContentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(testContext.targetName, definition);
                }        
                return null;
            }
        };
        tran.doInTransaction(transfer1CB);

        RetryingTransactionCallback<Void> validateStep1CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef oldDestNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                NodeRef newDestNodeRef = testNodeFactory.getMappedNodeRef(testContext.newContentNodeRef);

                // Now validate that the target node does not exist - it should have 
                // been updated by path.
                assertFalse("unit test stuffed up - comparing with self", oldDestNodeRef.equals(newDestNodeRef));
                assertFalse("new dest node ref exists", nodeService.exists(newDestNodeRef));
                assertTrue("old dest node does not exists", nodeService.exists(oldDestNodeRef));

                assertEquals("title is wrong", (String)nodeService.getProperty(oldDestNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE_UPDATED); 
                //             assertEquals("type is wrong", nodeService.getType(contentNodeRef), nodeService.getType(destNodeRef));        
                return null;
            }
        };
        tran.doInTransaction(validateStep1CB);

    } // Path based update

    
    /**
     * Test the transfer method when it is running async.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testAsyncCallback() throws Exception
    {
        final int MAX_SLEEPS = 5;
       
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        /**
         * Unit test kludge to transfer from guest home to company home
         */
        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();

        /**
          * This needs to be committed before we can call transfer asycnc.
          */
        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_NAME_A = "Demo Node A";
        final String CONTENT_NAME_B = "Demo Node B";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "Hello";
        
        final String targetName = "testAsyncCallback";
        class TestContext
        {
            TransferTarget transferMe;
            NodeRef nodeRefA = null;
            NodeRef nodeRefB = null;
        };
        
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();
                
                /**
                 * Get guest home
                 */
               String guestHomeQuery = "/app:company_home/app:guest_home";
               ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
               assertEquals("", 1, guestHomeResult.length());
               final NodeRef guestHome = guestHomeResult.getNodeRef(0); 
                
                ctx.nodeRefA = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_A);
                
                if(ctx.nodeRefA == null)
                {
                    /**
                     * Create a test node that we will read and write
                     */
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), ContentModel.TYPE_CONTENT);
                    ctx.nodeRefA = child.getChildRef();
                    nodeService.setProperty(ctx.nodeRefA, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                    nodeService.setProperty(ctx.nodeRefA, ContentModel.PROP_NAME, CONTENT_NAME_A);
            
                    ContentWriter writer = contentService.getWriter(ctx.nodeRefA, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                
                ctx.nodeRefB = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_B);
                
                if(ctx.nodeRefB == null)
                {
                    ChildAssociationRef  child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), ContentModel.TYPE_CONTENT);
                    ctx.nodeRefB = child.getChildRef();
                    nodeService.setProperty(ctx.nodeRefB, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                    nodeService.setProperty(ctx.nodeRefB, ContentModel.PROP_NAME, CONTENT_NAME_B);
            
                    ContentWriter writer = contentService.getWriter(ctx.nodeRefB, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                
                /**
                 * Now go ahead and create our first transfer target
                 */
                if(!transferService.targetExists(targetName))
                {
                    createTransferTarget(targetName);
                }
                else
                {
                    transferService.getTransferTarget(targetName);
                }           

                return ctx;
            } 
        };
        
        final TestContext testContext = tran.doInTransaction(setupCB); 
    
        RetryingTransactionCallback<List<TransferEvent>> transferCB = new RetryingTransactionCallback<List<TransferEvent>>() {

            @Override
            public List<TransferEvent> execute() throws Throwable
            {
                List<TransferEvent>transferReport = new ArrayList<TransferEvent>(50);
                
                TestTransferCallback callback = new TestTransferCallback();
                Set<TransferCallback> callbacks = new HashSet<TransferCallback>();
                callbacks.add(callback);
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.nodeRefA);
                nodes.add(testContext.nodeRefB);
                definition.setNodes(nodes);
                
                transferService.transferAsync(targetName, definition, callbacks);
                logger.debug("transfer async has returned");
                
                /**
                 * Need to poll the transfer events here until callback receives the last event
                 */
                Queue<TransferEvent> events = callback.getEvents();
                
                int sleepCount = MAX_SLEEPS;
                boolean ended = false;
                
                TransferEvent event = events.poll();
                while (!ended)
                {
                    logger.debug("polling loop:" + sleepCount);
                    
                    while(event != null)
                    {
                        /**
                         * Got an event - reset the sleep counter
                         */
                        sleepCount = MAX_SLEEPS;
                                                
                        logger.debug("Got an event" + event.toString());
                        
                        /**
                         * Squirrel away the event for analysis later
                         */
                        transferReport.add(event);
                        
                        /**
                         * If we read the last record which will either be SUCCESS or ERROR then we we have finished
                         */
                        if(event.isLast())
                        {
                            logger.debug("got last event");
                            ended = true;                            
                        }
                        
                        /**
                         * Try to get the next event
                         */
                        event = events.poll();
                    }
                    
                    if(event == null && !ended)
                    {
                        if(sleepCount <= 0)
                        {
                            fail("timed out without receiving last event");
                            ended = true;
                        }
                        else
                        {
                            /**
                             *  No content - go to sleep to wait for some more
                             */
                            if(sleepCount-- > 0)
                            {
                                // Sleep for 5 second
                                Thread.sleep(5000);
                            }                
                        }
                        
                        /**
                         * Try to get the next event
                         */
                        event = events.poll();
                    }
                }
            
                return transferReport;
            }
        };
        
        /**
         * The transfer report is a plain report of the transfer - no async shenanigans to worry about
         */
        final List<TransferEvent>transferReport = tran.doInTransaction(transferCB); 
                     
        /**
          * Now validate the transferReport
          */
        assertTrue("transfer report is too small", transferReport.size() > 2);
        assertTrue("transfer report does not start with START", transferReport.get(0).getTransferState().equals(TransferEvent.TransferState.START));
            
        boolean success = false;
        for(TransferEvent event : transferReport)
        {
           if(event.getTransferState() == TransferEvent.TransferState.SUCCESS)
           {
               success = true;
           }
        } 
        assertTrue("transfer report does not contain SUCCESS", success);

    } // test async callback
    
    
    /**
     * Test the transfer cancel method when it is running async.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testAsyncCancel() throws Exception
    {
        final int MAX_SLEEPS = 5;

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_NAME_A = "Demo Node A";
        final String CONTENT_NAME_B = "Demo Node B";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "Hello";
        final String targetName = "testAsyncCallback";
        /**
         * Get guest home
         */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        final NodeRef guestHome = guestHomeResult.getNodeRef(0); 

        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        class TestContext
        {
            TransferTarget transferMe;
            NodeRef nodeRefA = null;
            NodeRef nodeRefB = null;
        };

        /**
         * Unit test kludge to transfer from guest home to company home
         */
        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();


        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();

                ctx.nodeRefA = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_A);

                if(ctx.nodeRefA == null)
                {
                    /**
                     * Create a test node that we will read and write
                     */
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), ContentModel.TYPE_CONTENT);
                    ctx.nodeRefA = child.getChildRef();
                    nodeService.setProperty(ctx.nodeRefA, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                    nodeService.setProperty(ctx.nodeRefA, ContentModel.PROP_NAME, CONTENT_NAME_A);

                    ContentWriter writer = contentService.getWriter(ctx.nodeRefA, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                ctx.nodeRefB = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_B);

                if(ctx.nodeRefB == null)
                {
                    ChildAssociationRef  child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), ContentModel.TYPE_CONTENT);
                    ctx.nodeRefB = child.getChildRef();
                    nodeService.setProperty(ctx.nodeRefB, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                    nodeService.setProperty(ctx.nodeRefB, ContentModel.PROP_NAME, CONTENT_NAME_B);

                    ContentWriter writer = contentService.getWriter(ctx.nodeRefB, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                /**
                 * Now go ahead and create our first transfer target
                 */
                if(!transferService.targetExists(targetName))
                {
                    createTransferTarget(targetName);
                }
                else
                {
                    transferService.getTransferTarget(targetName);
                }   

                return ctx;
            } 
        };

        final TestContext testContext = tran.doInTransaction(setupCB); 

        /**
         * The transfer report is a plain report of the transfer - no async shenanigans to worry about
         */
        final List<TransferEvent>transferReport = new ArrayList<TransferEvent>(50);

        RetryingTransactionCallback<Void> transferAsyncCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {

                /**
                 * The poison callback will cancel the transfer after 
                 * the begin
                 */
                TransferCallback poison = new TransferCallback() 
                {
                    String transferId = null;

                    public void processEvent(TransferEvent event)
                    {
                        logger.debug(event.toString());

                        if(event instanceof TransferEventBegin)
                        {
                            TransferEventBegin beginEvent = (TransferEventBegin)event;
                            transferId = beginEvent.getTransferId();

                            transferService.cancelAsync(transferId);
                        }
                    }
                };

                TestTransferCallback callback = new TestTransferCallback();
                Set<TransferCallback> callbacks = new HashSet<TransferCallback>();
                callbacks.add(callback);
                callbacks.add(poison);
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.nodeRefA);
                nodes.add(testContext.nodeRefB);
                definition.setNodes(nodes);

                transferService.transferAsync(targetName, definition, callbacks);
                logger.debug("transfer async has returned");

                /**
                 * Need to poll the transfer events here until callback receives the last event
                 */
                Queue<TransferEvent> events = callback.getEvents();

                int sleepCount = MAX_SLEEPS;
                boolean ended = false;

                TransferEvent event = events.poll();
                while (!ended)
                {
                    logger.debug("polling loop:" + sleepCount);

                    while(event != null)
                    {
                        /**
                         * Got an event - reset the sleep counter
                         */
                        sleepCount = MAX_SLEEPS;

                        logger.debug("Got an event" + event.toString());

                        /**
                         * Squirrel away the event for analysis later
                         */
                        transferReport.add(event);

                        /**
                         * If we read the last record which will either be SUCCESS or ERROR then we we have finished
                         */
                        if(event.isLast())
                        {
                            logger.debug("got last event");
                            ended = true;                            
                        }

                        /**
                         * Try to get the next event
                         */
                        event = events.poll();
                    }

                    if(event == null && !ended)
                    {
                        if(sleepCount <= 0)
                        {
                            fail("timed out without receiving last event");
                            ended = true;
                        }
                        else
                        {
                            /**
                             *  No content - go to sleep to wait for some more
                             */
                            if(sleepCount-- > 0)
                            {
                                // Sleep for 5 second
                                Thread.sleep(5000);
                            }                
                        }

                        /**
                         * Try to get the next event
                         */
                        event = events.poll();
                    }
                }



                return null;
            }
        };
        tran.doInTransaction(transferAsyncCB);


        /**
         * Now validate the transferReport
         */
        assertTrue("transfer report is too small", transferReport.size() > 3);
        assertTrue("transfer report does not start with START", transferReport.get(0).getTransferState().equals(TransferEvent.TransferState.START));
        assertTrue("transfer report does not end with CANCELLED", transferReport.get(transferReport.size()-1).getTransferState().equals(TransferEvent.TransferState.CANCELLED));
        // last event is the transfer report event.


    } // test async cancel

    private void dumpToSystemOut(NodeRef nodeRef) throws IOException
    {
        ContentReader reader2 = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("transfer reader is null", reader2);
        InputStream is = reader2.getContentInputStream();
    
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
    
        String s = br.readLine();
        while(s != null)
        {
            System.out.println(s);   
            s = br.readLine();
        }
    }

    private  UnitTestTransferManifestNodeFactory unitTestKludgeToTransferGuestHomeToCompanyHome()
    {
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
   
        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);
        
        return testNodeFactory;
    }
    
    /**
     * Test the transfer method with regard to big content. 
     * 
     * This test takes a long time to run and is by default not run in the overnight build.
     * 
     * Turn it on by turning debug logging on for this class or by changing the "runTest" value;
     */
    public void testTransferOneNodeWithBigContent() throws Exception
    { 
        /**
         * This test takes a long time to run - so switch it on and off here.
         */
        boolean runTest = false;
        if(runTest || logger.isDebugEnabled())
        {
            final String CONTENT_TITLE = "ContentTitle";
            final String CONTENT_NAME = "BigContent";
            final Locale CONTENT_LOCALE = Locale.UK; 
        
            logger.debug("testTransferOneNodeWithBigContent starting");
            
            final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
            
            /**
             * Unit test kludge to transfer from guest home to company home
             */
            final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();
                
            final String targetName = "testTransferOneNodeWithBigContent";
        
            class TestContext
            {
                TransferTarget transferMe;
                NodeRef contentNodeRef;
                NodeRef destNodeRef;
            };
            
            RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
            {
                @Override
                public TestContext execute() throws Throwable
                {
                    TestContext ctx = new TestContext();
                    
                    String guestHomeQuery = "/app:company_home/app:guest_home";
                    ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                        
                    assertEquals("", 1, result.length());
                    NodeRef guestHome = result.getNodeRef(0);
              
                    System.out.println("Guest home:" + guestHome);
                    assertNotNull(guestHome);
                 
                    ctx.contentNodeRef = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME);
                    if(ctx.contentNodeRef == null)
                    {
                        /**
                         * Create a test node that we will read and write
                         */
                        ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(CONTENT_NAME), ContentModel.TYPE_CONTENT);
            
                        File tempFile = TempFileProvider.createTempFile("test", ".dat");
                        FileWriter fw = new FileWriter(tempFile);
                        for(int i = 0; i < 100000000; i++)
                        {
                            fw.write("hello world this is my text, I wonder how much text I can transfer?" + i);
                        }
                        System.out.println("Temp File Size is:" + tempFile.length());
                        fw.close();
     
                        ctx.contentNodeRef = child.getChildRef();
                        ContentWriter writer = contentService.getWriter(ctx.contentNodeRef, ContentModel.PROP_CONTENT, true);
                        writer.setLocale(CONTENT_LOCALE);
                        writer.setMimetype("application/data");
                        writer.putContent(tempFile);
            
                        tempFile.delete();
            
                        nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                        nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_NAME, CONTENT_NAME);
                    }
                    if(!transferService.targetExists(targetName))
                    {
                        createTransferTarget(targetName);
                    }
                    
                    return ctx;
                } 
            };
            
            final TestContext testContext = tran.doInTransaction(setupCB); 
        
            RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {

                @Override
                public Void execute() throws Throwable
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef>nodes = new HashSet<NodeRef>();
                    nodes.add(testContext.contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition);
                    
                    return null;
                }
            };
            
            RetryingTransactionCallback<Void> finishCB = new RetryingTransactionCallback<Void>() {

                @Override
                public Void execute() throws Throwable
                {
                    NodeRef oldDestNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                    
                    ContentReader source = contentService.getReader(testContext.contentNodeRef, ContentModel.PROP_CONTENT);
                    ContentReader destination = contentService.getReader(oldDestNodeRef, ContentModel.PROP_CONTENT);
                    
                    assertNotNull("source is null", source);
                    assertNotNull("destination is null", destination);
                    assertEquals("size different", source.getSize(), destination.getSize());
                    
                    /**
                     * Now get rid of the transferred node so that the test can run again. 
                     */
                    nodeService.deleteNode(oldDestNodeRef);
                    
                    return null;
                }
            };
            
            /**
             * This is the test
             */
            tran.doInTransaction(transferCB); 
            tran.doInTransaction(finishCB); 
        }
        else
        {
            System.out.println("test supressed");
        }
    } // test big content     

    /**  
     * Test the transfer method with regard to an empty content property.  ALF-4865
     * 
     * Step 1: create a node with an empty content property
     * transfer
     * 
     * Step 2: add non empty content property 
     * transfer
     * 
     * Step 3: update from non empty content to empty content property
     * transfer
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testEmptyContent() throws Exception
    {
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.ENGLISH; 
        final String CONTENT_ENCODING = "UTF-8";
        final String CONTENT_STRING = "The quick brown fox jumps over the lazy dog.";
        final String targetName = "testTransferEmptyContent";

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         *  
         *  Fake Repository Id
         */
        class TestContext
        {
            TransferTarget transferMe;
            NodeRef contentNodeRef;
            NodeRef savedDestinationNodeRef;
        };

        /**
         * Unit test kludge to transfer from guest home to company home
         */
        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        TransferTarget transferMe;

        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext ctx = new TestContext();

                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0); 

                /**
                 * Create a test node with an empty content that we will read and write
                 */       
                String name = GUID.generate();
                ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_CONTENT);
                ctx.contentNodeRef = child.getChildRef();
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_NAME, name);
                ContentData cd = new ContentData(null, null, 0, null);
                nodeService.setProperty(ctx.contentNodeRef, ContentModel.PROP_CONTENT, cd);

                if(!transferService.targetExists(targetName))
                {
                    ctx.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    ctx.transferMe = transferService.getTransferTarget(targetName);
                }
                transferService.enableTransferTarget(targetName, true);

                return ctx;
            } 
        };
        final TestContext testContext = tran.doInTransaction(setupCB); 

        final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");  

        /**
         * Step 1: Transfer our node which has empty content
         */
        logger.debug("testEmptyContent : First transfer - create new node (empty content)");


        RetryingTransactionCallback<Void> step1CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testContext.contentNodeRef, ContentModel.PROP_CONTENT);
                assertNull("test setup content reader not null", reader);
                Map<QName, Serializable> props = nodeService.getProperties(testContext.contentNodeRef);
                assertTrue(props.containsKey(ContentModel.PROP_CONTENT));

                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);

                return null;
            }
        };
        tran.doInTransaction(step1CB);

        RetryingTransactionCallback<Void> validateStep1CB = new RetryingTransactionCallback<Void>() 
        {
            @Override
            public Void execute() throws Throwable
            {
                Serializable modifiedDate = nodeService.getProperty(testContext.contentNodeRef, ContentModel.PROP_MODIFIED);
                if(modifiedDate instanceof Date)
                {
                    logger.debug("srcModified: "  + SDF.format(modifiedDate));
                }

                NodeRef destinationNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                testContext.savedDestinationNodeRef = destinationNodeRef;
                assertTrue("content node (dest) does not exist", nodeService.exists(destinationNodeRef));

                ContentReader reader = contentService.getReader(destinationNodeRef, ContentModel.PROP_CONTENT);
                assertNull("content reader not null", reader);
                Map<QName, Serializable> props = nodeService.getProperties(destinationNodeRef);
                assertTrue(props.containsKey(ContentModel.PROP_CONTENT));      
                return null;
            }
        };
        tran.doInTransaction(validateStep1CB);

        /**
         * Step 2: replace empty content with new content
         */
        logger.debug("testEmptyContent : Second transfer - replace empty content with some content");


        RetryingTransactionCallback<Void> step2CB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                Serializable modifiedDate = nodeService.getProperty(testContext.contentNodeRef, ContentModel.PROP_MODIFIED);
                if(modifiedDate instanceof Date)
                {
                    logger.debug("srcModified: "  + SDF.format(modifiedDate));
                }

                ContentWriter writer = contentService.getWriter(testContext.contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.setEncoding(CONTENT_ENCODING);
                writer.putContent(CONTENT_STRING);

                return null;
            }
        };

        tran.doInTransaction(step2CB);


        RetryingTransactionCallback<Void> step2TransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testContext.contentNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("test setup content reader not null", reader);
                Map<QName, Serializable> props = nodeService.getProperties(testContext.contentNodeRef);
                assertTrue(props.containsKey(ContentModel.PROP_CONTENT));

                /**
                 * Step 2: replace empty content with new content
                 */
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);

                return null;
            }
        };

        tran.doInTransaction(step2TransferCB);


        RetryingTransactionCallback<Void> step2ValidateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef destinationNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);

                assertEquals("test error destinationNodeRef not correct", testContext.savedDestinationNodeRef, destinationNodeRef);
                ContentReader reader = contentService.getReader(destinationNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("content reader is null", reader);
                assertTrue("content encoding is wrong", reader.getEncoding().equalsIgnoreCase(CONTENT_ENCODING));
                assertEquals("content locale is wrong", reader.getLocale(), CONTENT_LOCALE);
                assertTrue("content does not exist", reader.exists());
                String contentStr = reader.getContentString();
                assertEquals("Content is wrong", contentStr, CONTENT_STRING);

                return null;
            }
        };

        tran.doInTransaction(step2ValidateCB);

        /**
         * Step 3 - transition from a content property having content to one that is empty
         */
        logger.debug("testEmptyContent : Third transfer - remove existing content");


        RetryingTransactionCallback<Void> step3SetupCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentData cd = new ContentData(null, null, 0, null);
                nodeService.setProperty(testContext.contentNodeRef, ContentModel.PROP_CONTENT, cd);         
                return null;
            }
        };
        tran.doInTransaction(step3SetupCB);


        RetryingTransactionCallback<Void> step3TransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testContext.contentNodeRef, ContentModel.PROP_CONTENT);
                assertNull("test setup content reader not null", reader);
                Map<QName, Serializable> props = nodeService.getProperties(testContext.contentNodeRef);
                assertTrue(props.containsKey(ContentModel.PROP_CONTENT));

                /**
                 * Step 3: Transfer our node which has empty content to over-write existing
                 * content
                 */
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.contentNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
                return null;
            }
        };
        tran.doInTransaction(step3TransferCB);

        RetryingTransactionCallback<Void> step3ValidateCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                NodeRef destinationNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
                assertTrue("content node (dest) does not exist", nodeService.exists(destinationNodeRef));

                ContentReader reader = contentService.getReader(destinationNodeRef, ContentModel.PROP_CONTENT);
                assertNull("content reader not null", reader);
                Map<QName, Serializable> props = nodeService.getProperties(destinationNodeRef);
                assertTrue(props.containsKey(ContentModel.PROP_CONTENT));         
                return null;
            }
        };
        tran.doInTransaction(step3ValidateCB);
    } // end of testEmptyContent
    
    
    /**
     * Test the transfer method with regard to a repeated update of content.by sending one node (CRUD).
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testRepeatUpdateOfContent() throws Exception
    {
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        final String CONTENT_TITLE = "ContentTitle";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_ENCODING = "UTF-8";

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         *  
         *  Fake Repository Id
         */
        final TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        final List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
        
        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);
        
        final String targetName = "testRepeatUpdateOfContent";
        
        class TestContext
        {
           TransferTarget transferMe;
           NodeRef contentNodeRef;
           NodeRef destNodeRef;
           String contentString;
        };
       
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext testContext = new TestContext();
            
                /**
                 * Get guest home
                 */
               String guestHomeQuery = "/app:company_home/app:guest_home";
               ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
               assertEquals("", 1, guestHomeResult.length());
               NodeRef guestHome = guestHomeResult.getNodeRef(0); 
   
               /**
                * Create a test node that we will read and write
                */  
               String name = GUID.generate();
               ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_CONTENT);
               testContext.contentNodeRef = child.getChildRef();
               nodeService.setProperty(testContext.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
               nodeService.setProperty(testContext.contentNodeRef, ContentModel.PROP_NAME, name);
           
               /**
                * Make sure the transfer target exists and is enabled.
                */
               if(!transferService.targetExists(targetName))
               {
                   testContext.transferMe = createTransferTarget(targetName);
               }
               else
               {
                   testContext.transferMe = transferService.getTransferTarget(targetName);
               }
               transferService.enableTransferTarget(targetName, true);
               return testContext;
            } 
        };
        
        final TestContext testContext = tran.doInTransaction(setupCB); 
        
        RetryingTransactionCallback<Void> updateContentCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(testContext.contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.setEncoding(CONTENT_ENCODING);
                writer.putContent(testContext.contentString);
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               TransferDefinition definition = new TransferDefinition();
               Set<NodeRef>nodes = new HashSet<NodeRef>();
               nodes.add(testContext.contentNodeRef);
               definition.setNodes(nodes);
               transferService.transfer(targetName, definition);
               return null;
            }
        };
        
        RetryingTransactionCallback<Void> checkTransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef destNodeRef = testNodeFactory.getMappedNodeRef(testContext.contentNodeRef);
           
                ContentReader reader = contentService.getReader(destNodeRef, ContentModel.PROP_CONTENT);
                assertNotNull("content reader is null", reader);
                assertTrue("content encoding is wrong", reader.getEncoding().equalsIgnoreCase(CONTENT_ENCODING));
                assertEquals("content locale is wrong", reader.getLocale(), CONTENT_LOCALE);
                assertTrue("content does not exist", reader.exists());
                String contentStr = reader.getContentString();
                assertEquals("Content is wrong", contentStr, testContext.contentString);
                
                return null;
            }
        };
        
        /**
         * This is the test
         */
        for(int i = 0; i < 6 ; i++)
        {
            logger.debug("testRepeatUpdateContent - iteration:" + i);
            testContext.contentString = String.valueOf(i);
            tran.doInTransaction(updateContentCB);
            tran.doInTransaction(transferCB); 
            tran.doInTransaction(checkTransferCB); 
        }
    } // test repeat update content

    /**
     * Test the transfer method with regard to replacing a node.  ALF-5109
     * 
     * Step 1: Create a new parent node and child node
     * transfer
     * 
     * Step 2: Delete the parent node
     * transfer
     *  
     * Step 3: Create new parent child node with same names and assocs.
     * transfer
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testReplaceNode() throws Exception
    {
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_STRING = "Hello World";
        final String CONTENT_UPDATE_STRING = "Foo Bar";
        
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         *  
         *  Fake Repository Id
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
        
        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);
        
        /**
         * Get guest home
         */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        final NodeRef guestHome = guestHomeResult.getNodeRef(0); 
        
        final String targetName = "testRepeatUpdateOfContent";
        
        class TestContext
        {
           TransferTarget transferMe;
           NodeRef parentNodeRef;
           NodeRef middleNodeRef;
           NodeRef childNodeRef;
           QName parentName;
           QName middleName;
           QName childName;
           
        };
       
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext testContext = new TestContext();
            
               /**
                * Create a test node that we will read and write
                */  
               String name = GUID.generate();
               
               testContext.parentName = QName.createQName(name);
               testContext.childName = QName.createQName("Ermintrude");
               testContext.middleName = QName.createQName("Matilda");
               
               ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, testContext.parentName, ContentModel.TYPE_FOLDER);
               testContext.parentNodeRef = child.getChildRef();
               logger.debug("parentNodeRef created:"  + testContext.parentNodeRef );
               nodeService.setProperty(testContext.parentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
               nodeService.setProperty(testContext.parentNodeRef, ContentModel.PROP_NAME, testContext.parentName.getLocalName());
         
               ChildAssociationRef child2 = nodeService.createNode(testContext.parentNodeRef, ContentModel.ASSOC_CONTAINS, testContext.childName, ContentModel.TYPE_FOLDER);
               testContext.middleNodeRef = child2.getChildRef();
               logger.debug("middleNodeRef created:"  + testContext.middleNodeRef );
               nodeService.setProperty(testContext.middleNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
               nodeService.setProperty(testContext.middleNodeRef, ContentModel.PROP_NAME, testContext.childName.getLocalName());
         
               ChildAssociationRef child3 = nodeService.createNode(testContext.middleNodeRef, ContentModel.ASSOC_CONTAINS, testContext.childName, ContentModel.TYPE_CONTENT);
               testContext.childNodeRef = child3.getChildRef();
               logger.debug("childNodeRef created:"  + testContext.childNodeRef );
               nodeService.setProperty(testContext.childNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
               nodeService.setProperty(testContext.childNodeRef, ContentModel.PROP_NAME, testContext.childName.getLocalName());
         
               /**
                * Make sure the transfer target exists and is enabled.
                */
               if(!transferService.targetExists(targetName))
               {
                   testContext.transferMe = createTransferTarget(targetName);
               }
               else
               {
                   testContext.transferMe = transferService.getTransferTarget(targetName);
               }
               transferService.enableTransferTarget(targetName, true);
               return testContext;
            } 
        };
        
        final TestContext testContext = tran.doInTransaction(setupCB); 
        
        RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
               TransferDefinition definition = new TransferDefinition();
               Collection<NodeRef> nodes = new ArrayList<NodeRef>();
               nodes.add(testContext.childNodeRef);
               nodes.add(testContext.parentNodeRef);
               nodes.add(testContext.middleNodeRef);
               definition.setSync(true);
               definition.setNodes(nodes);
               transferService.transfer(targetName, definition);
               return null;
            }
        };
        
        RetryingTransactionCallback<Void> checkTransferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {        
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> replaceNodesCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                // Delete the old nodes
                
                nodeService.deleteNode(testContext.middleNodeRef);
                logger.debug("deleted node");
                
                ChildAssociationRef child2 = nodeService.createNode(testContext.parentNodeRef, ContentModel.ASSOC_CONTAINS, testContext.childName, ContentModel.TYPE_FOLDER);
                testContext.middleNodeRef = child2.getChildRef();
                logger.debug("middleNodeRef created:"  + testContext.middleNodeRef );
                nodeService.setProperty(testContext.middleNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(testContext.middleNodeRef, ContentModel.PROP_NAME, testContext.childName.getLocalName());
          
                ChildAssociationRef child3 = nodeService.createNode(testContext.middleNodeRef, ContentModel.ASSOC_CONTAINS, testContext.childName, ContentModel.TYPE_CONTENT);
                testContext.childNodeRef = child3.getChildRef();
                logger.debug("childNodeRef created:"  + testContext.childNodeRef );
                nodeService.setProperty(testContext.childNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(testContext.childNodeRef, ContentModel.PROP_NAME, testContext.childName.getLocalName());
                   
                return null;
            }
        };
        
        // This is the test
        
        tran.doInTransaction(transferCB); 
        tran.doInTransaction(replaceNodesCB);
        tran.doInTransaction(transferCB); 
        tran.doInTransaction(checkTransferCB);         
         
    } // test replace node

//    /**
//     * Test the transfer method with regard to obscure paths.
//     * 
//     * This is a unit test so it does some shenanigans to send to he same instance of alfresco.
//     */
//    public void testHorriblePaths() throws Exception
//    {
//        setDefaultRollback(false);
//        
//        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
//        
//        final String CONTENT_TITLE = "ContentTitle";
//        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
//        final String CONTENT_NAME = "Demo Node 1";
//        final Locale CONTENT_LOCALE = Locale.GERMAN; 
//        final String CONTENT_STRING = "The quick brown fox";
//        final Set<NodeRef>nodes = new HashSet<NodeRef>();
//

//        final String targetName = "testManyNodes";
//        
//        class TestContext
//        {
//            TransferTarget transferMe;
//            NodeRef nodeA = null;
//            NodeRef childNode = null;
//        };
//        
//        /**
//         * Unit test kludge to transfer from guest home to company home
//         */
//        final UnitTestTransferManifestNodeFactory testNodeFactory = unitTestKludgeToTransferGuestHomeToCompanyHome();
//        
//        TransferTarget transferMe;
//        
//        final QName[] difficult = { QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeB"),
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"with.dot"),
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"8332"),
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"&#~@"),
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"_-+ )"),
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"with space"),
//                // A, e with accent
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "\u0041\u00E9"), 
//                // Greek Alpha, Omega
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "\u0391\u03A9")
//        };
//        
//        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
//        {
//            @Override
//            public TestContext execute() throws Throwable
//            {
//                TestContext ctx = new TestContext();
//                
//                /**
//                 * Get guest home
//                 */
//               String guestHomeQuery = "/app:company_home/app:guest_home";
//               ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
//               assertEquals("", 1, guestHomeResult.length());
//               NodeRef guestHome = guestHomeResult.getNodeRef(0); 
//       
//               /**
//                * Create a test node that we will read and write
//                */
//               String guid = GUID.generate();
//               
//               /**
//                * Create a tree with "difficult" characters in the path
//                * ManyNodesRoot
//                * A (Folder)
//                * ... childNode
//                */
//               ChildAssociationRef child;
//               
//               child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(guid), ContentModel.TYPE_FOLDER);
//               NodeRef testRootNode = child.getChildRef();
//               nodeService.setProperty(testRootNode , ContentModel.PROP_TITLE, guid);   
//               nodeService.setProperty(testRootNode , ContentModel.PROP_NAME, guid);
//               nodes.add(testRootNode);
//            
//               child = nodeService.createNode(testRootNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"testNodeA"), ContentModel.TYPE_FOLDER);
//               ctx.nodeA = child.getChildRef();
//               nodeService.setProperty(ctx.nodeA , ContentModel.PROP_TITLE, "TestNodeA");   
//               nodeService.setProperty(ctx.nodeA , ContentModel.PROP_NAME, "TestNodeA");
//               nodes.add(ctx.nodeA);
//                
//               NodeRef current = ctx.nodeA;
//                
//               for(QName name : difficult)
//               {
//                   child = nodeService.createNode(current, ContentModel.ASSOC_CONTAINS, name, ContentModel.TYPE_FOLDER);
//                   current = child.getChildRef();
//                   nodeService.setProperty(current , ContentModel.PROP_TITLE, name);   
//                   nodeService.setProperty(current , ContentModel.PROP_NAME, "testName");
//                   nodes.add(current);
//               }
//               
//               child = nodeService.createNode(current, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeAC"), ContentModel.TYPE_CONTENT);
//               ctx.childNode = child.getChildRef();
//               nodeService.setProperty(  ctx.childNode , ContentModel.PROP_TITLE, CONTENT_TITLE + "AC");   
//               nodeService.setProperty(  ctx.childNode , ContentModel.PROP_NAME, "DemoNodeAC");
//             
//                 {
//                     ContentWriter writer = contentService.getWriter(  ctx.childNode , ContentModel.PROP_CONTENT, true);
//                     writer.setLocale(CONTENT_LOCALE);
//                     writer.putContent(CONTENT_STRING);
//                     nodes.add(  ctx.childNode);
//                 }
//         
//                
//                /**
//                 * Now go ahead and create our first transfer target
//                 */
//                if(!transferService.targetExists(targetName))
//                {
//                    ctx.transferMe = createTransferTarget(targetName);
//                }
//                else
//                {
//                    ctx.transferMe = transferService.getTransferTarget(targetName);
//                }       
//                
//                return ctx;
//            } 
//        };
//        
//        final TestContext testContext = tran.doInTransaction(setupCB); 
//        
//        RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {
//
//            @Override
//            public Void execute() throws Throwable
//            {
//                TransferDefinition definition = new TransferDefinition();
//                definition.setNodes(nodes);
//                transferService.transfer(targetName, definition);
//              
//               return null;
//            }
//        };
//        
//        tran.doInTransaction(transferCB); 
//        
//        RetryingTransactionCallback<Void> check1CB = new RetryingTransactionCallback<Void>() {
//
//            @Override
//            public Void execute() throws Throwable
//            {
//                NodeRef destChildNode = testNodeFactory.getMappedNodeRef(testContext.childNode);
//                assertTrue("dest node does not exist", nodeService.exists(destChildNode));
//                
//                /**
//                 * Step through source and dest trees on nodes comparing the path as we go.
//                 */
//                Path srcPath = nodeService.getPath(testContext.childNode);
//                Path destPath = nodeService.getPath(destChildNode);
//                
//                int srcSize = srcPath.size();
//                int destSize = destPath.size();
//                
//                Path dest = destPath.subPath(2, destSize-1);
//                Path src = srcPath.subPath(3, srcSize-1);
//                
////                System.out.println("src=" + src);
////                System.out.println("dest=" + dest);
//                assertEquals("paths are different", src.toString(), dest.toString());
//                             
//                return null;
//            }
//        };
//        
//        tran.doInTransaction(check1CB); 
//        
//        RetryingTransactionCallback<Void> updateCB = new RetryingTransactionCallback<Void>() {
//
//            @Override
//            public Void execute() throws Throwable
//            {
//              
//               return null;
//            }
//        };
//        
//        tran.doInTransaction(updateCB);
//        
//        tran.doInTransaction(transferCB); 
//        
//        RetryingTransactionCallback<Void> check2CB = new RetryingTransactionCallback<Void>() {
//
//            @Override
//            public Void execute() throws Throwable
//            {
//                assertTrue("dest node does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testContext.childNode)));
//                
//               return null;
//            }
//        };
//        tran.doInTransaction(check2CB); 
//             
//    } // horrible paths

    /**
     * ALF-6174
     * Test transfer of peer associations
     * 
     * Step 1 : Create 2 nodes 
     * Add a peer assoc
     * Transfer
     * 
     * Step 2: Add another peer assoc
     * Transfer
     * 
     * Step 3: Remove a peer assoc
     * Transfer
     * 
     * Step 4: Remove a peer assoc
     * Transfer
     * 
     * @throws Exception
     */
    public void testPeerAssocs() throws Exception
    {
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        final String CONTENT_TITLE = "ContentTitle";
        final Locale CONTENT_LOCALE = Locale.GERMAN; 
        final String CONTENT_ENCODING = "UTF-8";

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         *  
         *  Fake Repository Id
         */
        final TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        final List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        final String targetName = "testPeerAssocs";

        class TestContext
        {
            TransferTarget transferMe;
            NodeRef folderNodeRef;
            NodeRef sourceNodeRef;
            NodeRef targetNodeRef;
            NodeRef destSourceNodeRef;
            NodeRef destTargetNodeRef;
        };

        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext testContext = new TestContext();

                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0); 

                /**
                 * Create a test node that we will read and write
                 */  
                String name = GUID.generate();

                TransferDefinition def = new TransferDefinition();

                ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                testContext.folderNodeRef = child.getChildRef();
                nodeService.setProperty(testContext.folderNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(testContext.folderNodeRef, ContentModel.PROP_NAME, name);

                // Side effect - initialisee nodeid mapping
                testNodeFactory.createTransferManifestNode(testContext.folderNodeRef, def);

                child = nodeService.createNode(testContext.folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("source"), ContentModel.TYPE_CONTENT);
                testContext.sourceNodeRef = child.getChildRef();
                nodeService.setProperty(testContext.sourceNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(testContext.sourceNodeRef, ContentModel.PROP_NAME, "source");

                // Side effect - initialise nodeid mapping
                testNodeFactory.createTransferManifestNode(testContext.sourceNodeRef, def);

                child = nodeService.createNode(testContext.folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("target"), ContentModel.TYPE_CONTENT);
                testContext.targetNodeRef = child.getChildRef();
                nodeService.setProperty(testContext.targetNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                nodeService.setProperty(testContext.targetNodeRef, ContentModel.PROP_NAME, "target");
                testNodeFactory.createTransferManifestNode(testContext.folderNodeRef, def);
                nodeService.createAssociation(testContext.sourceNodeRef, testContext.targetNodeRef,  ContentModel.ASSOC_REFERENCES);

                // Side effect - initialise nodeid mapping
                testNodeFactory.createTransferManifestNode(testContext.targetNodeRef, def);

                /**
                 * Make sure the transfer target exists and is enabled.
                 */
                if(!transferService.targetExists(targetName))
                {
                    testContext.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testContext.transferMe = transferService.getTransferTarget(targetName);
                }
                transferService.enableTransferTarget(targetName, true);
                return testContext;
            } 
        };

        final TestContext testContext = tran.doInTransaction(setupCB); 

        RetryingTransactionCallback<Void> addPeerAssocCB = new RetryingTransactionCallback<Void>() {

            public QName assocQName = ContentModel.ASSOC_ATTACHMENTS;

            @Override
            public Void execute() throws Throwable
            {
                nodeService.createAssociation(testContext.sourceNodeRef, testContext.targetNodeRef,  assocQName);

                return null;
            }
        };

        RetryingTransactionCallback<Void> removePeerAssocCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                List<AssociationRef> refs = nodeService.getTargetAssocs(testContext.sourceNodeRef, RegexQNamePattern.MATCH_ALL);
                if(refs.size() > 0)
                {
                    AssociationRef ref = refs.get(0);
                    nodeService.removeAssociation(ref.getSourceRef(), ref.getTargetRef(),  ref.getTypeQName());
                }
                return null;
            }
        };

        RetryingTransactionCallback<Void> transferCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(testContext.sourceNodeRef);
                nodes.add(testContext.targetNodeRef);
                nodes.add(testContext.folderNodeRef);
                definition.setNodes(nodes);
                transferService.transfer(targetName, definition);
                return null;
            }
        };

        RetryingTransactionCallback<List<AssociationRef>> readAssocsCB = new RetryingTransactionCallback<List<AssociationRef>>() {

            @Override
            public List<AssociationRef>  execute() throws Throwable
            {
                List<AssociationRef> source = nodeService.getSourceAssocs(testContext.sourceNodeRef, RegexQNamePattern.MATCH_ALL);
                List<AssociationRef> target = nodeService.getTargetAssocs(testContext.sourceNodeRef, RegexQNamePattern.MATCH_ALL);

                NodeRef destNode = testNodeFactory.getMappedNodeRef(testContext.sourceNodeRef);
                List<AssociationRef> destSource = nodeService.getSourceAssocs(destNode, RegexQNamePattern.MATCH_ALL);
                List<AssociationRef> destTarget = nodeService.getTargetAssocs(destNode, RegexQNamePattern.MATCH_ALL);

                assertEquals("source peers different sizes",  destSource.size(), source.size() );
                assertEquals("target peers different sizes",  destTarget.size(), target.size() );

                if(destSource.size() == 1)
                {
                    assertEquals(destSource.get(0).getTypeQName(), source.get(0).getTypeQName());
                }

                if(destTarget.size() == 1)
                {
                    assertEquals(destTarget.get(0).getTypeQName(), target.get(0).getTypeQName());
                }

                return destTarget;
            }
        };

        /**
         * This is the test
         */

        tran.doInTransaction(transferCB);

        List<AssociationRef> assocs = tran.doInTransaction(readAssocsCB); 
        assertEquals("assocs not one", 1, assocs.size());

        tran.doInTransaction(addPeerAssocCB);

        tran.doInTransaction(transferCB); 

        assocs = tran.doInTransaction(readAssocsCB); 
        assertEquals("assocs not two", 2, assocs.size());

        tran.doInTransaction(removePeerAssocCB);

        tran.doInTransaction(transferCB); 

        tran.doInTransaction(removePeerAssocCB);

        tran.doInTransaction(transferCB); 

    } // testPeerAssocs
    
    // Utility methods below.
    private TransferTarget createTransferTarget(String name)
    {   
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "MARKR02";
        int endpointPort = 7080;
        String endpointPath = "/alfresco/service/api/transfer";
        String username = "admin";
        char[] password = "admin".toCharArray();
    
        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget target = transferService.createAndSaveTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        return target;
    }
        
    private DescriptorService getMockDescriptorService(String repositoryId)
    {
        DescriptorService descriptorService = mock(DescriptorService.class);
        Descriptor descriptor = mock(Descriptor.class);
        when(descriptor.getId()).thenReturn(repositoryId);
        when(descriptorService.getCurrentRepositoryDescriptor()).thenReturn(descriptor);
        when(descriptorService.getServerDescriptor()).thenReturn(serverDescriptor);
        
        return descriptorService;
    }    
}
