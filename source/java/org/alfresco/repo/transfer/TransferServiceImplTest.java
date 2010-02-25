/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.Pair;

/**
 * Unit test for TransferServiceImpl
 * 
 * Contains some unit tests for the transfer definitions.
 *
 * @author Mark Rogers
 */
public class TransferServiceImplTest extends BaseAlfrescoSpringTest 
{
    private TransferService transferService;
    private ContentService contentService;
    private TransferServiceImpl transferServiceImpl;
    private SearchService searchService;
    private TransactionService transactionService;
    private TransferReceiver receiver;
    private TransferManifestNodeFactory transferManifestNodeFactory; 
    
    String COMPANY_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home";
    String GUEST_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}guest_home";

    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.transferService = (TransferService)this.applicationContext.getBean("TransferService");
        this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        this.transferServiceImpl = (TransferServiceImpl)this.applicationContext.getBean("transferService");
        this.searchService = (SearchService)this.applicationContext.getBean("SearchService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("TransactionService");
        this.receiver = (TransferReceiver)this.applicationContext.getBean("transferReceiver");
        this.transferManifestNodeFactory = (TransferManifestNodeFactory)this.applicationContext.getBean("transferManifestNodeFactory");
        
        assertNotNull("receiver is null", this.receiver);
        
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
        TransferTarget ret = transferService.createTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
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
            transferService.createTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
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
        transferService.createTransferTarget(nameA, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        transferService.createTransferTarget(nameB, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        
        Set<TransferTarget> targets = transferService.getTransferTargets();
        assertTrue("targets is empty", targets.size() > 0);
        assertEquals("targets is empty", targets.size(), 2);
        for(TransferTarget target : targets)
        {
            System.out.println("found target");
        }
    }
    
    /**
     * Test of Get All Trabsfer Targets By Group
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
        transferService.createTransferTarget(getMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        
        Set<TransferTarget> targets = transferService.getTransferTargets("Default Group");
        assertTrue("targets is empty", targets.size() > 0);     
        
        /**
         * Negative test - group does not exist
         */
        try
        {
            transferService.getTransferTargets("Rubbish");
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
        TransferTarget target = transferService.createTransferTarget(updateMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        
        /*
         * Now update with exactly the same values.
         */
        TransferTarget update1 = transferService.updateTransferTarget(target);
        
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
        target.setName(updateMe); 
        target.setPassword(password2);
        target.setUsername(username2);
        
        TransferTarget update2 = transferService.updateTransferTarget(target);
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
        transferService.createTransferTarget(deleteMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
                        
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
     * Test the transfer method by sending one node.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testTransferOneNode() throws Exception
    {
       
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, COMPANY_HOME_XPATH_QUERY);
        NodeRef companyHome = result.getNodeRef(0);

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
        String CONTENT_TITLE = "ContentTitle";
        String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        String CONTENT_NAME = "Demo Node 1";
        Locale CONTENT_LOCALE = Locale.GERMAN; 
        String CONTENT_STRING = "Hello";
        
        ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testNode"), ContentModel.TYPE_CONTENT);
        NodeRef contentNodeRef = child.getChildRef();
        nodeService.setProperty(contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(contentNodeRef, ContentModel.PROP_NAME, CONTENT_NAME);
        
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setLocale(CONTENT_LOCALE);
        writer.putContent(CONTENT_STRING);
          
        /**
          *  For unit test 
          *  - replace the HTTP transport with the in-process transport
          *  - replace the node factory with one that will map node refs, paths etc.
          */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
          
        /**
          * Now go ahead and create our first transfer target
          */
        String targetName = "testTransferOneNodeNoContent";
        TransferTarget transferMe = createTransferTarget(targetName);
        try 
        {
           /**
             * Transfer our transfer target node which has no content
             */
            {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef>nodes = new HashSet<NodeRef>();
                    nodes.add(contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition, null);
            }  
            
            // Now validate that the target node exists and has similar properties to the source
            NodeRef destNodeRef = testNodeFactory.getMappedNodeRef(contentNodeRef);
            assertFalse("unit test stuffed up - comparing with self", destNodeRef.equals(transferMe.getNodeRef()));
            assertTrue("dest node ref does not exist", nodeService.exists(destNodeRef));
            assertEquals("title is wrong", (String)nodeService.getProperty(destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE); 
            assertEquals("type is wrong", nodeService.getType(contentNodeRef), nodeService.getType(destNodeRef));
            
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE_UPDATED);   
            
            /**
             * Transfer our node again - so this is an update
             */
            {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef>nodes = new HashSet<NodeRef>();
                    nodes.add(contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition, null);
            }  
            
            // Now validate that the target node exists and has similar properties to the source
            assertFalse("unit test stuffed up - comparing with self", destNodeRef.equals(transferMe.getNodeRef()));
            assertTrue("dest node ref does not exist", nodeService.exists(destNodeRef));
            assertEquals("title is wrong", (String)nodeService.getProperty(destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE_UPDATED); 
            assertEquals("type is wrong", nodeService.getType(contentNodeRef), nodeService.getType(destNodeRef));

            /**
              * Negative test transfer nothing
              */
            try
            {
                    TransferDefinition definition = new TransferDefinition();
                    transferService.transfer(targetName, definition, null);
                    fail("exception not thrown");
            }
            catch(TransferException te)
            {
                    // expect to go here
            }
        }
        finally     
        {
            //transferService.deleteTransferTarget(targetName);     
        }
    }
    
    /**
     * Test the transfer method by sending a graph of node.
     * 
     * This is a unit test so it does some shenanigans to send to he same instance of alfresco.
     */
    public void testManyNodes() throws Exception
    {
       
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, COMPANY_HOME_XPATH_QUERY);
        NodeRef companyHome = result.getNodeRef(0);
        
        Set<NodeRef>nodes = new HashSet<NodeRef>();

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
        String CONTENT_TITLE = "ContentTitle";
        String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        String CONTENT_NAME = "Demo Node 1";
        Locale CONTENT_LOCALE = Locale.GERMAN; 
        String CONTENT_STRING = "The quick brown fox";
        
        /**
         * Create a tree
         * A
         * --AA
         * --AB
         * ----ABA
         * -------- 100+ nodes
         * ----ABB
         * ----ABC   
         * B
         */
        
        ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeA"), ContentModel.TYPE_CONTENT);
        NodeRef nodeA = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        
        {
            ContentWriter writer = contentService.getWriter(nodeA , ContentModel.PROP_CONTENT, true);
            writer.setLocale(CONTENT_LOCALE);
            writer.putContent(CONTENT_STRING);
            nodes.add(nodeA);
        }
        
        child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeB"), ContentModel.TYPE_CONTENT);
        NodeRef nodeB = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        nodes.add(nodeB);
        
        child = nodeService.createNode(nodeA, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeAA"), ContentModel.TYPE_CONTENT);
        NodeRef nodeAA = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        nodes.add(nodeAA);
        
        child = nodeService.createNode(nodeA, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeAB"), ContentModel.TYPE_CONTENT);
        NodeRef nodeAB = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        nodes.add(nodeAB);
        
        child = nodeService.createNode(nodeAB, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeABA"), ContentModel.TYPE_CONTENT);
        NodeRef nodeABA = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        nodes.add(nodeABA);
        
        child = nodeService.createNode(nodeAB, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeABB"), ContentModel.TYPE_CONTENT);
        NodeRef nodeABB = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        nodes.add(nodeABB);
        
        child = nodeService.createNode(nodeAB, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeABC"), ContentModel.TYPE_CONTENT);
        NodeRef nodeABC = child.getChildRef();
        nodeService.setProperty(nodeA , ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(nodeA , ContentModel.PROP_NAME, CONTENT_NAME);
        nodes.add(nodeABC);
          
        /**
          *  For unit test 
          *  - replace the HTTP transport with the in-process transport
          *  - replace the node factory with one that will map node refs, paths etc.
          */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
          
        /**
          * Now go ahead and create our first transfer target
          */
        String targetName = "testManyNodes";
        TransferTarget transferMe = createTransferTarget(targetName);
        
        try 
        {
           /**
             * Transfer our transfer target nodes 
             */
            {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition, null);
            }  
            
            // Now validate that the target node exists and has similar properties to the source
            NodeRef destNodeA = testNodeFactory.getMappedNodeRef(nodeA);
            assertFalse("unit test stuffed up - comparing with self", destNodeA.equals(transferMe.getNodeRef()));
            assertTrue("dest node ref A does not exist", nodeService.exists(destNodeA));
            assertEquals("title is wrong", (String)nodeService.getProperty(destNodeA, ContentModel.PROP_TITLE), CONTENT_TITLE); 
            assertEquals("type is wrong", nodeService.getType(nodeA), nodeService.getType(destNodeA));
            
            NodeRef destNodeB = testNodeFactory.getMappedNodeRef(nodeB);
            assertTrue("dest node B does not exist", nodeService.exists(destNodeB));
            
            NodeRef destNodeAA = testNodeFactory.getMappedNodeRef(nodeAA);
            assertTrue("dest node AA ref does not exist", nodeService.exists(destNodeAA));
            
            NodeRef destNodeAB = testNodeFactory.getMappedNodeRef(nodeAB);
            assertTrue("dest node AB ref does not exist", nodeService.exists(destNodeAB));
            
            NodeRef destNodeABA = testNodeFactory.getMappedNodeRef(nodeABA);
            assertTrue("dest node ABA ref does not exist", nodeService.exists(destNodeABA));
            
            NodeRef destNodeABB = testNodeFactory.getMappedNodeRef(nodeABB);
            assertTrue("dest node ABB ref does not exist", nodeService.exists(destNodeABB));
            
            NodeRef destNodeABC = testNodeFactory.getMappedNodeRef(nodeABC);
            assertTrue("dest node ABC ref does not exist", nodeService.exists(destNodeABC));
             
            /**
             * Update a single node (NodeAB) from the middle of the tree
             */
            {
                nodeService.setProperty(nodeAB , ContentModel.PROP_TITLE, CONTENT_TITLE_UPDATED);   
                
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>toUpdate = new HashSet<NodeRef>();
                toUpdate.add(nodeAB);
                definition.setNodes(toUpdate);
                transferService.transfer(targetName, definition, null);
                
                assertEquals("title is wrong", (String)nodeService.getProperty(destNodeAB, ContentModel.PROP_TITLE), CONTENT_TITLE_UPDATED);        
            }  
            
            /**
             * Now generate a large number of nodes
             */
            
            for(int i = 0; i < 100; i++)
            {
                child = nodeService.createNode(nodeABA, ContentModel.ASSOC_CONTAINS, QName.createQName("testNode" + i), ContentModel.TYPE_CONTENT);
            
                NodeRef nodeX = child.getChildRef();
                nodeService.setProperty(nodeX , ContentModel.PROP_TITLE, CONTENT_TITLE + i);   
                nodeService.setProperty(nodeX , ContentModel.PROP_NAME, CONTENT_NAME +i);
                nodes.add(nodeX);
                
                ContentWriter writer = contentService.getWriter(nodeX, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING + i);
            }
            
            /**
             * Transfer our transfer target nodes 
             */
            {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition, null);
            } 
            

        }
        finally     
        {
            transferService.deleteTransferTarget(targetName);     
        }
    } // end many nodes

    
    /**
     * Test the path based update.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testPathBasedUpdate() throws Exception
    {
       
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, COMPANY_HOME_XPATH_QUERY);
        NodeRef companyHome = result.getNodeRef(0);
        
        QName TEST_QNAME = QName.createQName("testPathBasedUpdate");

        /**
          * Get guest home
          */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        NodeRef guestHome = guestHomeResult.getNodeRef(0); 

        /**
         * Create a test node that we will transfer.   Its path is what is important
         */
        String CONTENT_TITLE = "ContentTitle";
        String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        String CONTENT_NAME = "Demo Node 1";
        Locale CONTENT_LOCALE = Locale.GERMAN; 
        String CONTENT_STRING = "Hello";
        
        ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, TEST_QNAME, ContentModel.TYPE_CONTENT);
        NodeRef contentNodeRef = child.getChildRef();
        nodeService.setProperty(contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
        nodeService.setProperty(contentNodeRef, ContentModel.PROP_NAME, CONTENT_NAME);
        
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setLocale(CONTENT_LOCALE);
        writer.putContent(CONTENT_STRING);
          
        /**
          *  For unit test 
          *  - replace the HTTP transport with the in-process transport
          *  - replace the node factory with one that will map node refs, paths etc.
          */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
          
        /**
          * Now go ahead and create our transfer target
          */
        String targetName = "testPathBasedUpdate";
        TransferTarget transferMe = createTransferTarget(targetName);
        try 
        {
           /**
             * Transfer our transfer target node
             */
            {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef>nodes = new HashSet<NodeRef>();
                    nodes.add(contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition, null);
            }  
            
            // Now validate that the target node exists and has similar properties to the source
            NodeRef destNodeRef = testNodeFactory.getMappedNodeRef(contentNodeRef);
            assertFalse("unit test stuffed up - comparing with self", destNodeRef.equals(transferMe.getNodeRef()));
            assertTrue("dest node ref does not exist", nodeService.exists(destNodeRef));
            assertEquals("title is wrong", (String)nodeService.getProperty(destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE); 
            assertEquals("type is wrong", nodeService.getType(contentNodeRef), nodeService.getType(destNodeRef));
                                   
            /**
             * Now delete the content node and re-create another one with the old path
             */
            nodeService.deleteNode(contentNodeRef);
            
            child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, TEST_QNAME, ContentModel.TYPE_CONTENT);
            contentNodeRef = child.getChildRef();
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE_UPDATED); 
            
            /**
             * Transfer our node which is a new node (so will not exist on the back end) with a path that already has a node.
             */
            {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef>nodes = new HashSet<NodeRef>();
                    nodes.add(contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition, null);
            }  
            
            // Now validate that the target node exists and has been updated by the node with the new node ref
            assertFalse("unit test stuffed up - comparing with self", destNodeRef.equals(transferMe.getNodeRef()));
            assertTrue("dest node ref does not exist", nodeService.exists(destNodeRef));
            assertEquals("title is wrong", (String)nodeService.getProperty(destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE_UPDATED); 
            assertEquals("type is wrong", nodeService.getType(contentNodeRef), nodeService.getType(destNodeRef));

        }
        finally     
        {
            transferService.deleteTransferTarget(targetName);     
        }
    } // Path based update

    
    /**
     * Test the transfer method when it is running async.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testAsyncCallback() throws Exception
    {
        int MAX_SLEEPS = 5;
       
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, COMPANY_HOME_XPATH_QUERY);
        NodeRef companyHome = result.getNodeRef(0);
        
        /**
          * Get guest home
          */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        NodeRef guestHome = guestHomeResult.getNodeRef(0); 

        /**
          *  For unit test 
          *  - replace the HTTP transport with the in-process transport
          *  - replace the node factory with one that will map node refs, paths etc.
          */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
          
        /**
          * Now go ahead and create our first transfer target
          * This needs to be committed before we can call transfer asycnc.
          */
        String CONTENT_TITLE = "ContentTitle";
        String CONTENT_NAME_A = "Demo Node A";
        String CONTENT_NAME_B = "Demo Node B";
        Locale CONTENT_LOCALE = Locale.GERMAN; 
        String CONTENT_STRING = "Hello";
        
        NodeRef nodeRefA = null;
        NodeRef nodeRefB = null;
        String targetName = "testAsyncCallback";
        {
            UserTransaction trx = transactionService.getNonPropagatingUserTransaction();
            trx.begin();
            try
            {
                nodeRefA = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_A);
                
                if(nodeRefA == null)
                {
                    /**
                     * Create a test node that we will read and write
                     */
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testAsycNodeA"), ContentModel.TYPE_CONTENT);
                    nodeRefA = child.getChildRef();
                    nodeService.setProperty(nodeRefA, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                    nodeService.setProperty(nodeRefA, ContentModel.PROP_NAME, CONTENT_NAME_A);
            
                    ContentWriter writer = contentService.getWriter(nodeRefA, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                
                nodeRefB = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_B);
                
                if(nodeRefB == null)
                {
                    ChildAssociationRef  child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testAsyncNodeB"), ContentModel.TYPE_CONTENT);
                    nodeRefB = child.getChildRef();
                    nodeService.setProperty(nodeRefB, ContentModel.PROP_TITLE, CONTENT_TITLE);   
                    nodeService.setProperty(nodeRefB, ContentModel.PROP_NAME, CONTENT_NAME_B);
            
                    ContentWriter writer = contentService.getWriter(nodeRefB, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                TransferTarget transferMe = createTransferTarget(targetName);
            }
            finally    
            {
                trx.commit();
            }
        }
        
        /**
         * The transfer report is a plain report of the transfer - no async shenanigans to worry about
         */
        List<TransferEvent>transferReport = new ArrayList<TransferEvent>(50);
        
        try 
        {
           /**
             * Call the transferAsync method.
             */
            {
                TestTransferCallback callback = new TestTransferCallback();
                Set<TransferCallback> callbacks = new HashSet<TransferCallback>();
                callbacks.add(callback);
                TransferDefinition definition = new TransferDefinition();
                Set<NodeRef>nodes = new HashSet<NodeRef>();
                nodes.add(nodeRefA);
                nodes.add(nodeRefB);
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
            }  
                        
            /**
             * Now validate the transferReport
             */
            assertTrue("transfer report is too small", transferReport.size() > 2);
            assertTrue("transfer report does not start with START", transferReport.get(0).getTransferState().equals(TransferEvent.TransferState.START));
            assertTrue("transfer report does not end with SUCCESS", transferReport.get(transferReport.size()-1).getTransferState().equals(TransferEvent.TransferState.SUCCESS));
        }
        finally     
        {   
            UserTransaction trx = transactionService.getNonPropagatingUserTransaction();
            trx.begin();
            transferService.deleteTransferTarget(targetName);     
            trx.commit();
        }
    } // test async callback
    
    /**
     * Test the transfer report.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testTransferReport() throws Exception
    {
       
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, COMPANY_HOME_XPATH_QUERY);
        NodeRef companyHome = result.getNodeRef(0);
        
        /**
          * Get guest home
          */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        NodeRef guestHome = guestHomeResult.getNodeRef(0); 

        /**
          *  For unit test 
          *  - replace the HTTP transport with the in-process transport
          *  - replace the node factory with one that will map node refs, paths etc.
          */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory); 
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory); 
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
          
        /**
          * Now go ahead and create our first transfer target
          * This needs to be committed before we can call transfer asycnc.
          */
        String CONTENT_TITLE = "ContentTitle";
        String CONTENT_NAME_A = "Demo Node A";
        String CONTENT_NAME_B = "Demo Node B";
        Locale CONTENT_LOCALE = Locale.GERMAN; 
        String CONTENT_STRING = "Hello";
        
        NodeRef nodeRefA = null;
        NodeRef nodeRefB = null;
        TransferTarget transferMe = null; 
        String targetName = "testTransferReport";
     
        nodeRefA = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_A);
                
        if(nodeRefA == null)
        {
            /**
             * Create a test node that we will read and write
             */
            ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testAsycNodeA"), ContentModel.TYPE_CONTENT);
            nodeRefA = child.getChildRef();
            nodeService.setProperty(nodeRefA, ContentModel.PROP_TITLE, CONTENT_TITLE);   
            nodeService.setProperty(nodeRefA, ContentModel.PROP_NAME, CONTENT_NAME_A);
            
            ContentWriter writer = contentService.getWriter(nodeRefA, ContentModel.PROP_CONTENT, true);
            writer.setLocale(CONTENT_LOCALE);
            writer.putContent(CONTENT_STRING);
        }
                
        nodeRefB = nodeService.getChildByName(guestHome, ContentModel.ASSOC_CONTAINS, CONTENT_NAME_B);
                
        if(nodeRefB == null)
        {
            ChildAssociationRef  child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testAsyncNodeB"), ContentModel.TYPE_CONTENT);
            nodeRefB = child.getChildRef();
            nodeService.setProperty(nodeRefB, ContentModel.PROP_TITLE, CONTENT_TITLE);   
            nodeService.setProperty(nodeRefB, ContentModel.PROP_NAME, CONTENT_NAME_B);
            
            ContentWriter writer = contentService.getWriter(nodeRefB, ContentModel.PROP_CONTENT, true);
            writer.setLocale(CONTENT_LOCALE);
            writer.putContent(CONTENT_STRING);
        }
                
        transferMe = createTransferTarget(targetName);
                
        /**
          * Call the transfer method.
          */
        {
            TestTransferCallback callback = new TestTransferCallback();
            Set<TransferCallback> callbacks = new HashSet<TransferCallback>();
            callbacks.add(callback);
            TransferDefinition definition = new TransferDefinition();
            Set<NodeRef>nodes = new HashSet<NodeRef>();
            nodes.add(nodeRefA);
            nodes.add(nodeRefB);
            definition.setNodes(nodes);
                
            NodeRef transferReport = transferService.transfer(targetName, definition, callbacks);
            assertNotNull("transfer report is null", transferReport);
                
            ContentReader reader = contentService.getReader(transferReport, ContentModel.PROP_CONTENT);
            assertNotNull("transfer reader is null", reader);
            
            logger.debug("now show the contents of the transfer report");
            reader.getContent(System.out);  
        }
        
        logger.debug("now delete the target:" + targetName);

        transferService.deleteTransferTarget(targetName);     

    } // test transfer report

    
    
    
//    /**
//     * Test the transfer method with big content - commented out since it takes a long time to run.
//     */
//    public void testTransferOneNodeWithBigContent() throws Exception
//    {
//        String CONTENT_TITLE = "ContentTitle";
//        String CONTENT_NAME = "Demo Node 6";
//        Locale CONTENT_LOCALE = Locale.GERMAN; 
//        String CONTENT_STRING = "Hello";
//        
//        String targetName = "testTransferOneNodeWithBigContent";
//        
//        String guestHomeQuery = "/app:company_home/app:guest_home";
//        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
//        
//        assertEquals("", 1, result.length());
//        NodeRef guestHome = result.getNodeRef(0);
//        ChildAssociationRef childAssoc = result.getChildAssocRef(0);
//        System.out.println("Guest home:" + guestHome);
//        assertNotNull(guestHome);
//        
//        /**
//         * Now go ahead and create our first transfer target
//         */
//        TransferTarget transferMe = createTransferTarget(targetName);
//        
//        /**
//         * Create a test node that we will read and write
//         */
//        ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName("testNode6"), ContentModel.TYPE_CONTENT);
//        
//        
//        File tempFile = TempFileProvider.createTempFile("test", ".dat");
//        FileWriter fw = new FileWriter(tempFile);
//        for(int i = 0; i < 100000000; i++)
//        {
//            fw.write("hello world this is my text, I wonder how much text I can transfer?" + i);
//        }
//        System.out.println("Temp File Size is:" + tempFile.length());
//        fw.close();
// 
//        NodeRef contentNodeRef = child.getChildRef();
//        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
//        writer.setLocale(CONTENT_LOCALE);
//        //File file = new File("c:/temp/images/BigCheese1.bmp");
//        writer.setMimetype("application/data");
//        //writer.putContent(file);
//        writer.putContent(tempFile);
//        
//        tempFile.delete();
//        
//        nodeService.setProperty(contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);   
//        nodeService.setProperty(contentNodeRef, ContentModel.PROP_NAME, CONTENT_NAME);
//
//        try 
//        {
//            /**
//             * Transfer the node created above 
//             */
//            {
//                TransferDefinition definition = new TransferDefinition();
//                Set<NodeRef>nodes = new HashSet<NodeRef>();
//                nodes.add(contentNodeRef);
//                definition.setNodes(nodes);
//                transferService.transfer(targetName, definition, null);
//            }  
//                      
//            /**
//             * Negative test transfer nothing
//             */
//            try
//            {
//                TransferDefinition definition = new TransferDefinition();
//                transferService.transfer(targetName, definition, null);
//                fail("exception not thrown");
//            }
//            catch(TransferException te)
//            {
//                // expect to go here
//            }
//        }
//        finally
//        {
//            transferService.deleteTransferTarget(targetName);
//        } 
//    }


    private TransferTarget createTransferTarget(String name)
    {
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "MARKR02";
        int endpointPort = 6080;
        String endpointPath = "/alfresco/service/api/transfer";
        String username = "admin";
        char[] password = "admin".toCharArray();
    
        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget target = transferService.createTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        return target;
    }

}
