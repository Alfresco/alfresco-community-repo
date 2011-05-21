/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.imap;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.Utf7;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit test for ImapServiceImpl
 */
public class ImapServiceImplTest extends TestCase 
{

    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";

    private static final String TEST_IMAP_FOLDER_NAME = "aaa";

    private static final String MAILBOX_NAME_A = "mailbox_a";
    private static final String MAILBOX_NAME_B = "mailbox_b";
    private static final String MAILBOX_PATTERN = "mailbox*";
    private static final String FOLDER_PATTERN = "___-___folder*";
    private static final String FILE_PATTERN = "___-___file*";

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private TransactionService transactionService;
    private NodeService nodeService;
    private ImporterService importerService;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    
    private AlfrescoImapUser user;
    private ImapService imapService;
    private UserTransaction txn;

    private NodeRef testImapFolderNodeRef;
    private Flags flags;
    
    String anotherUserName;

    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        importerService = serviceRegistry.getImporterService();
        personService = serviceRegistry.getPersonService();
        authenticationService = serviceRegistry.getAuthenticationService();
        permissionService = serviceRegistry.getPermissionService();
        imapService = serviceRegistry.getImapService();
        searchService = serviceRegistry.getSearchService();
        namespaceService = serviceRegistry.getNamespaceService();
        fileFolderService = serviceRegistry.getFileFolderService();

        
        flags = new Flags();
        flags.add(Flags.Flag.SEEN);
        flags.add(Flags.Flag.FLAGGED);
        flags.add(Flags.Flag.ANSWERED);
        flags.add(Flags.Flag.DELETED);

        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
        authenticationService.authenticate(USER_NAME, USER_PASSWORD.toCharArray());

        // downgrade integrity
        IntegrityChecker.setWarnInTransaction();

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

        String storePath = "workspace://SpacesStore";
        String companyHomePathInStore = "/app:company_home";

        StoreRef storeRef = new StoreRef(storePath);

        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
        NodeRef companyHomeNodeRef = nodeRefs.get(0);

        

        ChildApplicationContextFactory imap = (ChildApplicationContextFactory) ctx.getBean("imap");
        ApplicationContext imapCtx = imap.getApplicationContext();
        ImapServiceImpl imapServiceImpl = (ImapServiceImpl)imapCtx.getBean("imapService");

        // Creating IMAP test folder for IMAP root
        LinkedList<String> folders = new LinkedList<String>();
        folders.add(TEST_IMAP_FOLDER_NAME);
        FileFolderServiceImpl.makeFolders(fileFolderService, companyHomeNodeRef, folders, ContentModel.TYPE_FOLDER);
        
        // Setting IMAP root
        RepositoryFolderConfigBean imapHome = new RepositoryFolderConfigBean();
        imapHome.setStore(storePath);
        imapHome.setRootPath(companyHomePathInStore);
        imapHome.setFolderPath(TEST_IMAP_FOLDER_NAME);
        imapServiceImpl.setImapHome(imapHome);
        
        // Starting IMAP
        imapServiceImpl.startup();
        
        nodeRefs = searchService.selectNodes(storeRootNodeRef,
                companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_FOLDER_NAME,
                null,
                namespaceService,
                false);
        testImapFolderNodeRef = nodeRefs.get(0);

        
        /* 
         * Importing test folders:
         * 
         * Test folder contains: "___-___folder_a"
         * 
         * "___-___folder_a" contains: "___-___folder_a_a",
         *                             "___-___file_a",
         *                             "Message_485.eml" (this is IMAP Message)
         *                           
         * "___-___folder_a_a" contains: "____-____file_a_a"
         * 
         */
        importInternal("imap/imapservice_test_folder_a.acp", testImapFolderNodeRef);

        reauthenticate(anotherUserName, anotherUserName);
    }

    public void tearDown() throws Exception
    {
        try
        {
            txn.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void importInternal(String acpName, NodeRef space)
            throws IOException
    {
        ClassPathResource acpResource = new ClassPathResource(acpName);
        ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(acpResource.getFile(), null);
        Location importLocation = new Location(space);
        importerService.importView(acpHandler, importLocation, null, null);
    }

    private boolean checkMailbox(AlfrescoImapUser user, String mailboxName)
    {
        AlfrescoImapFolder mailFolder = (AlfrescoImapFolder)imapService.getFolder(user, mailboxName);

        if (mailFolder.getFolderInfo() == null)
        {
            return false;
        }
        return true;
    }

    private boolean checkSubscribedMailbox(AlfrescoImapUser user, String mailboxName)
    {
        List<AlfrescoImapFolder> aifs = imapService.listSubscribedMailboxes(user, mailboxName);
        boolean present = false;
        for (AlfrescoImapFolder aif : aifs)
        {
            if (aif.getName().equals(mailboxName))
            {
                present = true;
                break;
            }
        }
        return present;
    }
    
    private void reauthenticate(String name, String password)
    {
        authenticationService.invalidateTicket(authenticationService.getCurrentTicket());
        authenticationService.clearCurrentSecurityContext();
        authenticationService.authenticate(name, password.toCharArray());
    }

    public void testGetFolder() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        assertTrue(checkMailbox(user, MAILBOX_NAME_A));
    }
    
    public void testListMailbox() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        imapService.createMailbox(user, MAILBOX_NAME_B);
        List<AlfrescoImapFolder> mf = imapService.listMailboxes(user, MAILBOX_PATTERN);
        assertEquals(2, mf.size());
        
        boolean foundA = false;
        boolean foundB = false;
        
        for(AlfrescoImapFolder folder : mf)
        {
            if(MAILBOX_NAME_A.equals(folder.getName()))
            {
               foundA = true;
            }
            if(MAILBOX_NAME_B.equals(folder.getName()))
            {
               foundB = true;
            }
        }
        
        assertTrue("folder A found", foundA);
        assertTrue("folder B found", foundB);
        
        /**
         * The new mailboxes should be subscribed?
         */
        List<AlfrescoImapFolder> aif = imapService.listSubscribedMailboxes(user, MAILBOX_PATTERN);
        assertEquals(2, aif.size());
        
        /**
         * Unsubscribe to one of the mailboxes.
         */
        imapService.unsubscribe(user, MAILBOX_NAME_B);
        List<AlfrescoImapFolder> aif2 = imapService.listSubscribedMailboxes(user, MAILBOX_PATTERN);
        assertEquals(1, aif2.size());
    }
    
    public void testListSubscribedMailbox() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        imapService.createMailbox(user, MAILBOX_NAME_B);
        imapService.subscribe(user, MAILBOX_NAME_A);
        imapService.subscribe(user, MAILBOX_NAME_B);
        List<AlfrescoImapFolder> aif = imapService.listSubscribedMailboxes(user, MAILBOX_PATTERN);
        assertEquals(aif.size(), 2);
        
        assertTrue("Can't subscribe mailbox A", checkSubscribedMailbox(user, MAILBOX_NAME_A));
        assertTrue("Can't subscribe mailbox B", checkSubscribedMailbox(user, MAILBOX_NAME_B));
    }

    public void testCreateMailbox() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        assertTrue("Mailbox isn't created", checkMailbox(user, MAILBOX_NAME_A));
    }

    public void testDuplicateMailboxes() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        try
        {
            imapService.createMailbox(user, MAILBOX_NAME_A);
            fail("Duplicate Mailbox was created");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected
        }

    }

    public void testRenameMailbox() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        imapService.renameMailbox(user, MAILBOX_NAME_A, MAILBOX_NAME_B);
        assertFalse("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_A));
        assertTrue("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_B));
    }

    public void testRenameMailboxDuplicate() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        imapService.createMailbox(user, MAILBOX_NAME_B);
        try
        {
            imapService.renameMailbox(user, MAILBOX_NAME_A, MAILBOX_NAME_B);
            fail("Mailbox was renamed to existing one but shouldn't");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected
        }
    }

    public void testDeleteMailbox() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_B);
        imapService.deleteMailbox(user, MAILBOX_NAME_B);
        assertFalse("Can't delete mailbox", checkMailbox(user, MAILBOX_NAME_B));
    }

//    public void testSearchFoldersInArchive() throws Exception
//    {
//        List<FileInfo> fi = imapService.searchFolders(testImapFolderNodeRef, FOLDER_PATTERN, true, ImapViewMode.ARCHIVE);
//        assertNotNull("Can't find folders in Archive Mode", fi);
//        assertEquals("Can't find folders in Archive Mode", fi.size(), 2);
//        
//        fi = imapService.searchFolders(testImapFolderNodeRef, FOLDER_PATTERN, false, ImapViewMode.ARCHIVE);
//        assertNotNull("Can't find folders in Archive Mode", fi);
//        assertEquals("Can't find folders in Archive Mode", fi.size(), 1);
//    }
//
//    public void testSearchFoldersInVirtual() throws Exception
//    {
//        List<FileInfo> fi = imapService.searchFolders(testImapFolderNodeRef, FOLDER_PATTERN, true, ImapViewMode.VIRTUAL);
//        assertNotNull("Can't find folders in Virtual Mode", fi);
//        assertEquals("Can't find folders in Virtual Mode", fi.size(), 2);
//
//        fi = imapService.searchFolders(testImapFolderNodeRef, FOLDER_PATTERN, false, ImapViewMode.VIRTUAL);
//        assertNotNull("Can't find folders in Virtual Mode", fi);
//        assertEquals("Can't find folders in Virtual Mode", fi.size(), 1);
//    }
//    
//    public void testSearchFoldersInMixed() throws Exception
//    {
//        List<FileInfo> fi = imapService.searchFolders(testImapFolderNodeRef, FOLDER_PATTERN, true, ImapViewMode.MIXED);
//        assertNotNull("Can't find folders in Mixed Mode", fi);
//        assertEquals("Can't find folders in Mixed Mode", fi.size(), 2);
//
//        fi = imapService.searchFolders(testImapFolderNodeRef, FOLDER_PATTERN, false, ImapViewMode.MIXED);
//        assertNotNull("Can't find folders in Mixed Mode", fi);
//        assertEquals("Can't find folders in Mixed Mode", fi.size(), 1);
//    }

//    public void testSearchFiles() throws Exception
//    {
//        List<FileInfo> fi = imapService.searchFiles(testImapFolderNodeRef, FILE_PATTERN, true);
//        assertNotNull(fi);
//        assertTrue(fi.size() > 0);
//    }
//
//    public void testSearchMails() throws Exception
//    {
//        List<FileInfo> fi = imapService.searchMails(testImapFolderNodeRef, ImapViewMode.MIXED);
//        assertNotNull(fi);
//        assertTrue(fi.size() > 0);
//    }

    public void testSubscribe() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);

        imapService.subscribe(user, MAILBOX_NAME_A);
        assertTrue("Can't subscribe mailbox", checkSubscribedMailbox(user, MAILBOX_NAME_A));
    }

    public void testUnsubscribe() throws Exception
    {
        imapService.createMailbox(user, MAILBOX_NAME_A);
        imapService.subscribe(user, MAILBOX_NAME_A);
        imapService.unsubscribe(user, MAILBOX_NAME_A);
        // TODO MER 21/05/2010 : line below looks like a bug to me.
        assertFalse("Can't unsubscribe mailbox", checkSubscribedMailbox(user, MAILBOX_NAME_A));
    }
    
    private void setFlags(FileInfo messageFileInfo) throws Exception
    {
        imapService.setFlags(messageFileInfo, flags, true);
        NodeRef messageNodeRef = messageFileInfo.getNodeRef();
        Map<QName, Serializable> props = nodeService.getProperties(messageNodeRef);

        assertTrue("Can't set SEEN flag", props.containsKey(ImapModel.PROP_FLAG_SEEN));
        assertTrue("Can't set FLAGGED flag", props.containsKey(ImapModel.PROP_FLAG_FLAGGED));
        assertTrue("Can't set ANSWERED flag", props.containsKey(ImapModel.PROP_FLAG_ANSWERED));
        assertTrue("Can't set DELETED flag", props.containsKey(ImapModel.PROP_FLAG_DELETED));
    }

    public void testSetFlags() throws Exception
    {
        List<FileInfo> fis = imapService.searchMails(testImapFolderNodeRef, ImapViewMode.ARCHIVE);
        if (fis != null && fis.size() > 0)
        {
            FileInfo messageFileInfo = fis.get(0);
            try
            {
                setFlags(messageFileInfo);
                fail("Can't set flags");
            }
            catch (Exception e)
            {
                if (e instanceof AccessDeniedException)
                {
                    // expected
                }
                else
                {
                    throw e;
                }
            }
            
            reauthenticate(USER_NAME, USER_PASSWORD);
            
            permissionService.setPermission(testImapFolderNodeRef, anotherUserName, PermissionService.WRITE, true);
            
            reauthenticate(anotherUserName, anotherUserName);
            
            setFlags(messageFileInfo);
        }
    }
    
    public void testSetFlag() throws Exception
    {
        List<FileInfo> fis = imapService.searchMails(testImapFolderNodeRef, ImapViewMode.ARCHIVE);
        if (fis != null && fis.size() > 0)
        {
            FileInfo messageFileInfo = fis.get(0);
            
            reauthenticate(USER_NAME, USER_PASSWORD);
            
            permissionService.setPermission(testImapFolderNodeRef, anotherUserName, PermissionService.WRITE, true);
            
            reauthenticate(anotherUserName, anotherUserName);
            
            imapService.setFlag(messageFileInfo, Flags.Flag.RECENT, true);
            
            Serializable prop = nodeService.getProperty(messageFileInfo.getNodeRef(), ImapModel.PROP_FLAG_RECENT);
            assertNotNull("Can't set RECENT flag", prop);
        }
    }

    public void testGetFlags() throws Exception
    {
        List<FileInfo> fis = imapService.searchMails(testImapFolderNodeRef, ImapViewMode.ARCHIVE);
        if (fis != null && fis.size() > 0)
        {
            FileInfo messageFileInfo = fis.get(0);
            
            reauthenticate(USER_NAME, USER_PASSWORD);
            
            permissionService.setPermission(testImapFolderNodeRef, anotherUserName, PermissionService.WRITE, true);
            
            imapService.setFlags(messageFileInfo, flags, true);
            
            reauthenticate(anotherUserName, anotherUserName);

            Flags fl = imapService.getFlags(messageFileInfo);
            assertTrue(fl.contains(flags));
        }
    }
    
    public void testRenameAccentedMailbox() throws Exception
    {
        String MAILBOX_ACCENTED_NAME_A = "Hôtel";
        String MAILBOX_ACCENTED_NAME_B = "HôtelXX";
        
        imapService.createMailbox(user, MAILBOX_ACCENTED_NAME_A);
        imapService.deleteMailbox(user, MAILBOX_ACCENTED_NAME_A);
        
        imapService.createMailbox(user, MAILBOX_ACCENTED_NAME_A);
        imapService.renameMailbox(user, MAILBOX_ACCENTED_NAME_A, MAILBOX_ACCENTED_NAME_B);
        assertFalse("Can't rename mailbox", checkMailbox(user, MAILBOX_ACCENTED_NAME_A));
        assertTrue("Can't rename mailbox", checkMailbox(user, MAILBOX_ACCENTED_NAME_B));
        imapService.deleteMailbox(user, MAILBOX_ACCENTED_NAME_B);
    } 
}
