/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.model.filefolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.imap.AlfrescoImapFolder;
import org.alfresco.repo.imap.AlfrescoImapUser;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.SearchLanguageConversion;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.context.ApplicationContext;

public class HiddenAspectTest
{
    @Rule public TestName name = new TestName();
    
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    protected HiddenAspect hiddenAspect;
    private TransactionService transactionService;
    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    private CheckOutCheckInService cociService;
    private UserTransaction txn;

    private SearchService searchService;
    private DictionaryNamespaceComponent namespacePrefixResolver;
    private ImapService imapService;
    private PersonService personService;
    private FilenameFilteringInterceptor interceptor;

    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private NodeRef topNodeRef;

    private boolean imapEnabled;

    private final String MAILBOX_NAME_A = "mailbox_a";
    private final String MAILBOX_NAME_B = ".mailbox_a";
    private String username;
    private AlfrescoImapUser user;
    
    protected boolean cmisDisableHide;

    @Before
    public void setup() throws SystemException, NotSupportedException
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");
        hiddenAspect = (HiddenAspect) ctx.getBean("hiddenAspect");
        interceptor = (FilenameFilteringInterceptor) ctx.getBean("filenameFilteringInterceptor");
        namespacePrefixResolver = (DictionaryNamespaceComponent) ctx.getBean("namespaceService");
        cociService = (CheckOutCheckInService) ctx.getBean("checkOutCheckInService");
        imapService = serviceRegistry.getImapService();
        personService = serviceRegistry.getPersonService();
        searchService = serviceRegistry.getSearchService();
        permissionService = serviceRegistry.getPermissionService();
        imapEnabled = serviceRegistry.getImapService().getImapServerEnabled();
        
        nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
        Properties properties = (Properties) ctx.getBean("global-properties");
        cmisDisableHide = Boolean.getBoolean(properties.getProperty("cmis.disable.hidden.leading.period.files"));
        
        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
        
        username = "user" + System.currentTimeMillis();
        
        PropertyMap testUser = new PropertyMap();
        testUser.put(ContentModel.PROP_USERNAME, username);
        testUser.put(ContentModel.PROP_FIRSTNAME, username);
        testUser.put(ContentModel.PROP_LASTNAME, username);
        testUser.put(ContentModel.PROP_EMAIL, username + "@alfresco.com");
        testUser.put(ContentModel.PROP_JOBTITLE, "jobTitle");

        // authenticate
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        personService.createPerson(testUser);
        
        // create the ACEGI Authentication instance for the new user
        authenticationService.createAuthentication(username, username.toCharArray());
        
        user = new AlfrescoImapUser(username + "@alfresco.com", username, username);

        // create a test store
        storeRef = nodeService
                .createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        permissionService.setPermission(rootNodeRef, username, PermissionService.CREATE_CHILDREN, true);
        
        AuthenticationUtil.setFullyAuthenticatedUser(username);
        
        topNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "working root"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }
    
    private String getName()
    {
        return name.getMethodName();
    }
    
    @After
    public void tearDown() throws Exception
    {
        try
        {
            if (txn.getStatus() != Status.STATUS_ROLLEDBACK && txn.getStatus() != Status.STATUS_COMMITTED)
            {
                txn.commit();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testHiddenFilesEnhancedClient()
    {
        FileFilterMode.setClient(Client.webdav);

        try
        {
            // check temporary file
            NodeRef parent = fileFolderService.create(topNodeRef, "New Folder", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef child = fileFolderService.create(parent, "file.tmp", ContentModel.TYPE_CONTENT).getNodeRef();
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
            List<FileInfo> children = fileFolderService.list(parent);
            assertEquals(1, children.size());

            // check hidden files - should be hidden for an enhanced client
            parent = fileFolderService.create(topNodeRef, "abc", ContentModel.TYPE_FOLDER).getNodeRef();
            child = fileFolderService.create(parent, ".TemporaryItems", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef child1 = fileFolderService.create(child, "inTemporaryItems", ContentModel.TYPE_FOLDER).getNodeRef();
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_TEMPORARY));
            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_INDEX_CONTROL));
            ResultSet results = searchForName(".TemporaryItems");
            assertEquals("", 0, results.length());

            children = fileFolderService.list(parent);
            assertEquals(1, children.size());

            Client saveClient = FileFilterMode.setClient(Client.script);
            try
            {
                children = fileFolderService.list(parent);
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }
            assertEquals(0, children.size());

            parent = fileFolderService.create(topNodeRef, "Folder 2", ContentModel.TYPE_FOLDER).getNodeRef();
            child = fileFolderService.create(parent, "Thumbs.db", ContentModel.TYPE_CONTENT).getNodeRef();
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("Thumbs.db");
            assertEquals("", 0, results.length());
            children = fileFolderService.list(parent);
            assertEquals(1, children.size());
            // set hidden attribute for cifs, webdav. ftp, nfs should be able to see, other clients not
            assertEquals(Visibility.Visible, hiddenAspect.getVisibility(Client.ftp, child));
            assertEquals(Visibility.Visible, hiddenAspect.getVisibility(Client.nfs, child));
            assertEquals(Visibility.Visible, hiddenAspect.getVisibility(Client.webdav, child));
            assertEquals(Visibility.HiddenAttribute, hiddenAspect.getVisibility(Client.cifs, child));
            assertEquals(Visibility.NotVisible, hiddenAspect.getVisibility(Client.script, child));
            assertEquals(Visibility.NotVisible, hiddenAspect.getVisibility(Client.webclient, child));

            // surf-config should not be visible to any client
            NodeRef node = fileFolderService.create(topNodeRef, "surf-config", ContentModel.TYPE_FOLDER).getNodeRef();
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("surf-config");
            assertEquals("", 0, results.length());
            for(Client client : hiddenAspect.getClients())
            {
            	if(!client.equals(Client.admin))
            	{
            		assertEquals(Visibility.NotVisible, hiddenAspect.getVisibility(client, node));
            	}
            }
            
            // .DS_Store is a system path and so is visible in nfs and webdav, as a hidden file in cifs and hidden to all other clients
            node = fileFolderService.create(topNodeRef, ".DS_Store", ContentModel.TYPE_CONTENT).getNodeRef();
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName(".DS_Store");
            assertEquals("", 0, results.length());
            for(Client client : hiddenAspect.getClients())
            {
                if(client == Client.cifs)
                {
                    assertEquals("Should have hidden attribute set for client " + client, Visibility.HiddenAttribute, hiddenAspect.getVisibility(client, node));                    
                }
                else if(client == Client.webdav || client == Client.nfs || client == Client.ftp)
                {
                    assertEquals("Should be visible for client " + client, Visibility.Visible, hiddenAspect.getVisibility(client, node));
                }
                else if(client != Client.admin)
                {
                    assertEquals("Should not be visible for client " + client, Visibility.NotVisible, hiddenAspect.getVisibility(client, node));
                }
            }

            // Resource fork should not be visible to any client except webdav(see MNT-10333)
            node = fileFolderService.create(topNodeRef, "._resourceFork", ContentModel.TYPE_FOLDER).getNodeRef();
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("._resourceFork");
            assertEquals("", 0, results.length());
            for(Client client : hiddenAspect.getClients())
            {
                if(client != Client.admin)
                {
                    if(client != Client.webdav)
                    {
                        assertEquals("Client " + client.toString(), Visibility.NotVisible, hiddenAspect.getVisibility(client, node));
                    }
                }
            }
        }
        finally
        {
            FileFilterMode.clearClient();
        }
    }

    @Test
    public void testClientControlled() throws Exception
    {
        // test that a client controlled hidden node is not subject to hidden file patterns
    	{
    		// node does not match a hidden file pattern
        	String nodeName = GUID.generate();
        	String hiddenNodeName = nodeName;
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(11);
            properties.put(ContentModel.PROP_NAME, hiddenNodeName);

            // create the node
            QName assocQName = QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI,
                    QName.createValidLocalName(hiddenNodeName));
            ChildAssociationRef assocRef = null;
            try
            {
                assocRef = nodeService.createNode(
                        topNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        assocQName,
                        ContentModel.TYPE_FOLDER,
                        properties);
            }
            catch (DuplicateChildNodeNameException e)
            {
                throw new FileExistsException(topNodeRef, hiddenNodeName);
            }

            NodeRef parent = assocRef.getChildRef();
            NodeRef child = null;
            NodeRef child1 = null;

            Client saveClient = FileFilterMode.setClient(Client.cmis);
            try
            {
	            hiddenAspect.hideNode(parent, true, true, true);

	            child = fileFolderService.create(parent, "folder11", ContentModel.TYPE_FOLDER).getNodeRef();
	            child1 = fileFolderService.create(child, "folder21", ContentModel.TYPE_FOLDER).getNodeRef();
	            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
	            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
	            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_HIDDEN));
	            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_INDEX_CONTROL));

	            // renaming from a hidden file pattern to a non-hidden file pattern should leave the node as hidden
	            // because it is client-controlled.
	            fileFolderService.rename(parent, nodeName);

	            assertTrue(nodeService.hasAspect(parent, ContentModel.ASPECT_HIDDEN));
	            assertTrue(nodeService.hasAspect(parent, ContentModel.ASPECT_INDEX_CONTROL));
	            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
	            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
	            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_HIDDEN));
	            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_INDEX_CONTROL));
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }

            List<FileInfo> children = fileFolderService.list(parent);
            assertEquals(0, children.size());

            saveClient = FileFilterMode.setClient(Client.script);
            try
            {
                children = fileFolderService.list(parent);
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }
            assertEquals(0, children.size());

            saveClient = FileFilterMode.setClient(Client.cmis);
            try
            {
                children = fileFolderService.list(parent);
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }
            assertEquals(0, children.size());

            // remove the client-controlled hidden aspect from the parent and check that it is no longer hidden
            saveClient = FileFilterMode.setClient(Client.cmis);
            try
            {
            	nodeService.removeAspect(parent, ContentModel.ASPECT_HIDDEN);

	            assertFalse(nodeService.hasAspect(parent, ContentModel.ASPECT_HIDDEN));
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }
    	}

        // test that a cascading hidden pattern defined in model-specific-services-content.xml results in hidden files
    	// (node is not client-controlled hidden)
        {
            NodeRef parent = null;
            NodeRef child = null;
            NodeRef child1 = null;

            Client saveClient = FileFilterMode.setClient(Client.cmis);
            try
            {
                parent = fileFolderService.create(topNodeRef, "." + GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                child = fileFolderService.create(parent, "folder11", ContentModel.TYPE_FOLDER).getNodeRef();
                child1 = fileFolderService.create(child, "folder21", ContentModel.TYPE_FOLDER).getNodeRef();
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }

            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN) != cmisDisableHide );
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL ) != cmisDisableHide);
            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_HIDDEN) != cmisDisableHide);
            assertTrue(nodeService.hasAspect(child1, ContentModel.ASPECT_INDEX_CONTROL) != cmisDisableHide);

            List<FileInfo> children = fileFolderService.list(parent);
            assertEquals(cmisDisableHide ? 1: 0, children.size());

            saveClient = FileFilterMode.setClient(Client.script);
            try
            {
                children = fileFolderService.list(parent);
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }
            assertEquals(cmisDisableHide ? 1: 0, children.size());

            saveClient = FileFilterMode.setClient(Client.cmis);
            try
            {
                children = fileFolderService.list(parent);
            }
            finally
            {
                FileFilterMode.setClient(saveClient);
            }
            assertEquals(cmisDisableHide ? 1: 0, children.size());
        }
    }

    @Test
    public void testImap()
    {
    	if(imapEnabled)
    	{
	        FileFilterMode.setClient(Client.webdav);
	
	        try
	        {
	            // Test that hidden files don't apply to imap service
	            imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
	            imapService.renameMailbox(user, MAILBOX_NAME_A, MAILBOX_NAME_B);
	            assertFalse("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_A));
	            assertTrue("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_B));
	            assertEquals("Can't rename mailbox", 0, numMailboxes(user, MAILBOX_NAME_A));
	            assertEquals("Can't rename mailbox", 1, numMailboxes(user, MAILBOX_NAME_B));
	        }
	        finally
	        {
	            FileFilterMode.clearClient();
	        }
		}
    }

    @Test
    public void testRename()
    {
        FileFilterMode.setClient(Client.webdav);

        try
        {
            // Test renaming
            String nodeName = GUID.generate();
            NodeRef node = fileFolderService.create(topNodeRef, nodeName, ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node11 = fileFolderService.create(node, nodeName + ".11", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node12 = fileFolderService.create(node, nodeName + ".12", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node21 = fileFolderService.create(node11, nodeName + ".21", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node22 = fileFolderService.create(node11, nodeName + ".22", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node31 = fileFolderService.create(node21, ".31", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node41 = fileFolderService.create(node31, nodeName + ".41", ContentModel.TYPE_CONTENT).getNodeRef();
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node11, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node11, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node12, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node12, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node21, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node21, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node22, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node22, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node31, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node31, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node41, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node41, ContentModel.ASPECT_INDEX_CONTROL));
    
            ResultSet results = searchForName(nodeName);
            assertEquals("", 1, results.length());
    
            try
            {
                fileFolderService.rename(node, "." + nodeName);
            }
            catch (FileExistsException e)
            {
                fail();
            }
            catch (FileNotFoundException e)
            {
                fail();
            }
            
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node11, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node11, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node12, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node12, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node21, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node21, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node22, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node22, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node31, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node31, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node41, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node41, ContentModel.ASPECT_INDEX_CONTROL));
    
            results = searchForName(nodeName);
            assertEquals("", 0, results.length());
    
            results = searchForName("." + nodeName);
            assertEquals("", 0, results.length());
    
            try
            {
                fileFolderService.rename(node, nodeName);
            }
            catch (FileExistsException e)
            {
                fail();
            }
            catch (FileNotFoundException e)
            {
                fail();
            }

            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node11, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node11, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node12, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node12, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node21, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node21, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node22, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node22, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node31, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node31, ContentModel.ASPECT_INDEX_CONTROL));
            assertTrue(nodeService.hasAspect(node41, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node41, ContentModel.ASPECT_INDEX_CONTROL));

            results = searchForName(nodeName);
            assertEquals("", 1, results.length());
        }
        finally
        {
            FileFilterMode.clearClient();
        }
    }
    
    @Test
    public void testFolderRename()
    {
        FileFilterMode.setClient(Client.cmis);
        NodeRef workingCopyNodeRef = null;

        try
        {
            String guid  = GUID.generate();
            String nodeName = "MyFolder" + guid;
            String newName = "AnotherFolder" + guid;
            NodeRef node = fileFolderService.create(topNodeRef, nodeName, ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef checkedOutNode = fileFolderService.create(node, guid + ".lockedchild",  ContentModel.TYPE_CONTENT).getNodeRef();

            ResultSet results = searchForName(nodeName);
            assertEquals("", 1, results.length());
            
            workingCopyNodeRef = cociService.checkout(checkedOutNode);
            assertNotNull(workingCopyNodeRef);
            assertTrue(nodeService.hasAspect(checkedOutNode, ContentModel.ASPECT_CHECKED_OUT));
            assertTrue(nodeService.hasAspect(checkedOutNode, ContentModel.ASPECT_LOCKABLE));
    
            try
            {
                fileFolderService.rename(node, newName);
            }
            catch (NodeLockedException e)
            {
                fail("It should be possible to rename folder with locked items");
            }
            catch (FileExistsException e)
            {
                fail();
            }
            catch (FileNotFoundException e)
            {
                fail();
            }
            
            results = searchForName(nodeName);
            assertEquals("File with old name should not be found", 0, results.length());
    
            results = searchForName(newName);
            assertEquals("File with new name should be found", 1, results.length());
        }
        finally
        {
            cociService.cancelCheckout(workingCopyNodeRef);
            FileFilterMode.clearClient();
        }
    }
    
    @Test
    public void testHiddenFilesBasicClient()
    {
    	if(imapEnabled)
    	{
	        FileFilterMode.setClient(Client.imap);
	
	        try
	        {
	            // check temporary file
	            NodeRef parent = fileFolderService.create(topNodeRef, "New Folder", ContentModel.TYPE_FOLDER).getNodeRef();
	            NodeRef child = fileFolderService.create(parent, "file.tmp", ContentModel.TYPE_CONTENT).getNodeRef();
	            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
	            ResultSet results = searchForName("file.tmp");
	            assertEquals("", 1, results.length());
	            List<FileInfo> children = fileFolderService.list(parent);
	            assertEquals(1, children.size());
	
	            // check hidden files - should not be hidden for a basic client
	            parent = fileFolderService.create(topNodeRef, ".TemporaryItems", ContentModel.TYPE_FOLDER).getNodeRef();
	            child = fileFolderService.create(parent, "inTemporaryItems", ContentModel.TYPE_FOLDER).getNodeRef();
	            assertFalse(nodeService.hasAspect(parent, ContentModel.ASPECT_TEMPORARY));
	            assertFalse(nodeService.hasAspect(parent, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(parent, ContentModel.ASPECT_INDEX_CONTROL));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
	            results = searchForName(".TemporaryItems");
	            assertEquals("", 1, results.length());
	            children = fileFolderService.list(parent);
	            assertEquals(1, children.size());
	
	            parent = fileFolderService.create(topNodeRef, "Folder 2", ContentModel.TYPE_FOLDER).getNodeRef();
	            child = fileFolderService.create(parent, "Thumbs.db", ContentModel.TYPE_CONTENT).getNodeRef();
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
	            results = searchForName("Thumbs.db");
	            assertEquals("", 1, results.length());
	            children = fileFolderService.list(parent);
	            assertEquals(1, children.size());
	
	            NodeRef node = fileFolderService.create(topNodeRef, "surf-config", ContentModel.TYPE_FOLDER).getNodeRef();
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
	            results = searchForName("surf-config");
	            assertEquals("", 1, results.length());
	            
	            node = fileFolderService.create(topNodeRef, ".DS_Store", ContentModel.TYPE_CONTENT).getNodeRef();
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
	            results = searchForName(".DS_Store");
	            assertEquals("", 1, results.length());
	            for(Client client : hiddenAspect.getClients())
	            {
	                assertEquals("Should be visible for client " + client, Visibility.Visible, hiddenAspect.getVisibility(client, node));
	            }
	
	            node = fileFolderService.create(topNodeRef, "._resourceFork", ContentModel.TYPE_FOLDER).getNodeRef();
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
	            results = searchForName("._resourceFork");
	            assertEquals("", 1, results.length());
	
	            children = fileFolderService.list(parent);
	            assertEquals(1, children.size());
	            
	
	            String nodeName = "Node" + System.currentTimeMillis();
	            node = fileFolderService.create(topNodeRef, nodeName, ContentModel.TYPE_CONTENT).getNodeRef();
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
	            results = searchForName(nodeName);
	            assertEquals("", 1, results.length());
	            try
	            {
	                fileFolderService.rename(node, "." + nodeName);
	            }
	            catch (FileExistsException e)
	            {
	                fail();
	            }
	            catch (FileNotFoundException e)
	            {
	                fail();
	            }
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
	
	            results = searchForName(nodeName);
	            assertEquals("", 1, results.length());
	
	            results = searchForName("." + nodeName);
	            assertEquals("", 1, results.length());
	            
	            try
	            {
	                fileFolderService.rename(node, nodeName);
	            }
	            catch (FileExistsException e)
	            {
	                fail();
	            }
	            catch (FileNotFoundException e)
	            {
	                fail();
	            }
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
	            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
	
	            results = searchForName("." + nodeName);
	            assertEquals("", 1, results.length());
	
	            imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
	            imapService.renameMailbox(user, MAILBOX_NAME_A, MAILBOX_NAME_B);
	            assertFalse("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_A));
	            assertTrue("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_B));
	            assertEquals("Can't rename mailbox", 0, numMailboxes(user, MAILBOX_NAME_A));
	            assertEquals("Can't rename mailbox", 1, numMailboxes(user, MAILBOX_NAME_B));
	        }
	        finally
	        {
	            FileFilterMode.clearClient();
	        }
		}
    }

    @SuppressWarnings("unused")
    @Test
    public void testCheckHidden() throws Exception
    {
        String nodeName = GUID.generate();

        interceptor.setEnabled(false);

        try
        {
            // Create some nodes that should be hidden but aren't
            NodeRef node = fileFolderService.create(topNodeRef, nodeName, ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node11 = fileFolderService.create(node, nodeName + ".11", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node12 = fileFolderService.create(node, ".12", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node21 = fileFolderService.create(node11, nodeName + ".21", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node22 = fileFolderService.create(node11, nodeName + ".22", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node31 = fileFolderService.create(node21, ".31", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef node41 = fileFolderService.create(node31, nodeName + ".41", ContentModel.TYPE_CONTENT).getNodeRef();

            assertEquals(1, searchForName(".12").length());
            assertEquals(1, searchForName(".31").length());
            assertEquals(1, searchForName(nodeName + ".41").length());

            txn.commit();
        }
        finally
        {
            interceptor.setEnabled(true);
        }
    }
    
    
    @Test
    public void testHideNodeExplicit() throws Exception
    {        
        FileFilterMode.setClient(Client.cifs);
        try
        {
            // check temporary file
            NodeRef parent = fileFolderService.create(topNodeRef, "New Folder", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef childA = fileFolderService.create(parent, "fileA", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef childB = fileFolderService.create(parent, "Thumbs.db", ContentModel.TYPE_CONTENT).getNodeRef();
            hiddenAspect.hideNodeExplicit(childA);
            
            // Nodes A and B should be hidden, one by pattern, one explicit
            assertTrue("node a should be hidden", nodeService.hasAspect(childA, ContentModel.ASPECT_HIDDEN));
//            assertFalse("node b should be hidden", nodeService.hasAspect(childB, ContentModel.ASPECT_HIDDEN));
            
            {
              List<FileInfo> children = fileFolderService.list(parent);
              assertEquals(2, children.size());
            }
            
            hiddenAspect.unhideExplicit(childA);
            hiddenAspect.unhideExplicit(childB);
            
            // Node B should still be hidden,  A should not
            assertFalse(nodeService.hasAspect(childA, ContentModel.ASPECT_HIDDEN));
//            assertTrue(nodeService.hasAspect(childB, ContentModel.ASPECT_HIDDEN));
            
            
            // call checkHidden to maks sure it does not make a mistake
            hiddenAspect.checkHidden(childA, true, false);
//            hiddenAspect.checkHidden(childB, true);
            
            {
              List<FileInfo> children = fileFolderService.list(parent);
              assertEquals(2, children.size());
            }
            
            // Node B should still be hidden,  A should not
            assertFalse(nodeService.hasAspect(childA, ContentModel.ASPECT_HIDDEN));
//            assertTrue(nodeService.hasAspect(childB, ContentModel.ASPECT_HIDDEN));
            
            {
              List<FileInfo> children = fileFolderService.list(parent);
              assertEquals(2, children.size());
            }
            
        }
        finally
        {
            FileFilterMode.clearClient();
        }

    }
    
    

    private List<NodeRef> getHiddenNodes(final StoreRef storeRef)
    {
        final List<NodeRef> nodes = new ArrayList<NodeRef>(20);

        NodeRefQueryCallback resultsCallback = new NodeRefQueryCallback()
        {
            @Override
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                if(storeRef == null || nodePair.getSecond().getStoreRef().equals(storeRef))
                {
                    nodes.add(nodePair.getSecond());
                }

                return true;
            }
            
        };
        nodeDAO.getNodesWithAspects(Collections.singleton(ContentModel.ASPECT_HIDDEN), 0l, Long.MAX_VALUE, resultsCallback);

        return nodes;
    }

    private int numMailboxes(AlfrescoImapUser user, String mailboxName)
    {
        int numMailboxes = 0;

        try
        {
            List<AlfrescoImapFolder> folders = imapService.listMailboxes(user, mailboxName, false);
            numMailboxes = folders.size(); 
        }
        catch (AlfrescoRuntimeException e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }

        return numMailboxes;
    }

    private boolean checkMailbox(AlfrescoImapUser user, String mailboxName)
    {
        try
        {
            imapService.getOrCreateMailbox(user, mailboxName, true, false);
        }
        catch (AlfrescoRuntimeException e)
        {
            return false;
        }
        return true;
    }
    
    private ResultSet searchForName(String name)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + SearchLanguageConversion.escapeLuceneQuery(ContentModel.PROP_NAME.toString()) + ":\"" + name + "\"");
        sp.addLocale(new Locale("en"));
        return searchService.query(sp);
    }
}
