package org.alfresco.repo.model.filefolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.imap.AlfrescoImapFolder;
import org.alfresco.repo.imap.AlfrescoImapUser;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
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
    
    private HiddenAspect hiddenAspect;
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private MutableAuthenticationService authenticationService;
    private UserTransaction txn;
    private NodeRef rootNodeRef;
    private SearchService searchService;
    private DictionaryNamespaceComponent namespacePrefixResolver;
    private ImapService imapService;
    private PersonService personService;

    private final String MAILBOX_NAME_A = "mailbox_a";
    private final String MAILBOX_NAME_B = ".mailbox_a";
    private String anotherUserName;
    private AlfrescoImapUser user;

    @Before
    public void setup() throws SystemException, NotSupportedException
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");
        hiddenAspect = (HiddenAspect) ctx.getBean("hiddenAspect");
        searchService = (SearchService) ctx.getBean("searchService");
        namespacePrefixResolver = (DictionaryNamespaceComponent) ctx.getBean("namespaceService");
        imapService = serviceRegistry.getImapService();
        personService = serviceRegistry.getPersonService();
        
        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        // authenticate
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // create a test store
        StoreRef storeRef = nodeService
                .createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        anotherUserName = "user" + System.currentTimeMillis();
        
        PropertyMap testUser = new PropertyMap();
        testUser.put(ContentModel.PROP_USERNAME, anotherUserName);
        testUser.put(ContentModel.PROP_FIRSTNAME, anotherUserName);
        testUser.put(ContentModel.PROP_LASTNAME, anotherUserName);
        testUser.put(ContentModel.PROP_EMAIL, anotherUserName + "@alfresco.com");
        testUser.put(ContentModel.PROP_JOBTITLE, "jobTitle");
        
        personService.createPerson(testUser);
        
        // create the ACEGI Authentication instance for the new user
        authenticationService.createAuthentication(anotherUserName, anotherUserName.toCharArray());
        
        user = new AlfrescoImapUser(anotherUserName + "@alfresco.com", anotherUserName, anotherUserName);
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
                txn.rollback();
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
long start = System.currentTimeMillis();
        try
        {
            // check temporary file
            NodeRef parent = fileFolderService.create(rootNodeRef, "New Folder", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef child = fileFolderService.create(parent, "file.tmp", ContentModel.TYPE_CONTENT).getNodeRef();
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
            List<FileInfo> children = fileFolderService.list(parent);
            assertEquals(1, children.size());

            // check hidden files - should be hidden for an enhanced client
            parent = fileFolderService.create(rootNodeRef, "abc", ContentModel.TYPE_FOLDER).getNodeRef();
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

            parent = fileFolderService.create(rootNodeRef, "Folder 2", ContentModel.TYPE_FOLDER).getNodeRef();
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
            NodeRef node = fileFolderService.create(rootNodeRef, "surf-config", ContentModel.TYPE_FOLDER).getNodeRef();
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("surf-config");
            assertEquals("", 0, results.length());
            for(Client client : hiddenAspect.getClients())
            {
                assertEquals(Visibility.NotVisible, hiddenAspect.getVisibility(client, node));
            }
            
            // .DS_Store is a system path and so is visible in nfs and webdav, as a hidden file in cifs and hidden to all other clients
            node = fileFolderService.create(rootNodeRef, ".DS_Store", ContentModel.TYPE_CONTENT).getNodeRef();
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
                else
                {
                    assertEquals("Should not be visible for client " + client, Visibility.NotVisible, hiddenAspect.getVisibility(client, node));
                }
            }

            // Resource fork should not be visible to any client
            node = fileFolderService.create(rootNodeRef, "._resourceFork", ContentModel.TYPE_FOLDER).getNodeRef();
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("._resourceFork");
            assertEquals("", 0, results.length());
            for(Client client : hiddenAspect.getClients())
            {
                assertEquals(Visibility.NotVisible, hiddenAspect.getVisibility(client, node));
            }

            // Test renaming
            String nodeName = GUID.generate();
            node = fileFolderService.create(rootNodeRef, nodeName, ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node1 = fileFolderService.create(node, nodeName + ".1", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node2 = fileFolderService.create(node1, nodeName + ".2", ContentModel.TYPE_CONTENT).getNodeRef();
            NodeRef node3 = fileFolderService.create(node2, nodeName + ".3", ContentModel.TYPE_CONTENT).getNodeRef();
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node1, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node1, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node2, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node2, ContentModel.ASPECT_INDEX_CONTROL));
            assertFalse(nodeService.hasAspect(node3, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node3, ContentModel.ASPECT_INDEX_CONTROL));

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
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertTrue(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));

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

            results = searchForName(nodeName);
            assertEquals("", 1, results.length());

            // Test imap service
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
            long end = System.currentTimeMillis();
            System.out.println((end - start)/1000 + "s");
        }
    }
    
    @Test
    public void testHiddenFilesBasicClient()
    {
        FileFilterMode.setClient(Client.imap);

        try
        {
            // check temporary file
            NodeRef parent = fileFolderService.create(rootNodeRef, "New Folder", ContentModel.TYPE_FOLDER).getNodeRef();
            NodeRef child = fileFolderService.create(parent, "file.tmp", ContentModel.TYPE_CONTENT).getNodeRef();
            assertTrue(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
            ResultSet results = searchForName("file.tmp");
            assertEquals("", 1, results.length());
            List<FileInfo> children = fileFolderService.list(parent);
            assertEquals(1, children.size());

            // check hidden files - should not be hidden for a basic client
            parent = fileFolderService.create(rootNodeRef, ".TemporaryItems", ContentModel.TYPE_FOLDER).getNodeRef();
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

            parent = fileFolderService.create(rootNodeRef, "Folder 2", ContentModel.TYPE_FOLDER).getNodeRef();
            child = fileFolderService.create(parent, "Thumbs.db", ContentModel.TYPE_CONTENT).getNodeRef();
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_TEMPORARY));
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(child, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("Thumbs.db");
            assertEquals("", 1, results.length());
            children = fileFolderService.list(parent);
            assertEquals(1, children.size());

            NodeRef node = fileFolderService.create(rootNodeRef, "surf-config", ContentModel.TYPE_FOLDER).getNodeRef();
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("surf-config");
            assertEquals("", 1, results.length());
            
            node = fileFolderService.create(rootNodeRef, ".DS_Store", ContentModel.TYPE_CONTENT).getNodeRef();
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName(".DS_Store");
            assertEquals("", 1, results.length());
            for(Client client : hiddenAspect.getClients())
            {
                assertEquals("Should be visible for client " + client, Visibility.Visible, hiddenAspect.getVisibility(client, node));
            }

            node = fileFolderService.create(rootNodeRef, "._resourceFork", ContentModel.TYPE_FOLDER).getNodeRef();
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_HIDDEN));
            assertFalse(nodeService.hasAspect(node, ContentModel.ASPECT_INDEX_CONTROL));
            results = searchForName("._resourceFork");
            assertEquals("", 1, results.length());

            children = fileFolderService.list(parent);
            assertEquals(1, children.size());
            

            String nodeName = "Node" + System.currentTimeMillis();
            node = fileFolderService.create(rootNodeRef, nodeName, ContentModel.TYPE_CONTENT).getNodeRef();
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
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_NAME.toString()) + ":\"" + name + "\"");
        sp.addLocale(new Locale("en"));
        return searchService.query(sp);
    }
}
