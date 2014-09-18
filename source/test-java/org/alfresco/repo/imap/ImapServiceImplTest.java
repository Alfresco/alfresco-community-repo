/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
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
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.icegreen.greenmail.store.SimpleStoredMessage;

/**
 * Unit test for ImapServiceImpl
 */
@Category(OwnJVMTestsCategory.class)
public class ImapServiceImplTest extends TestCase 
{
    private static final String IMAP_ROOT = "Alfresco IMAP";

    private static final String APP_COMPANY_HOME = "/app:company_home";

    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";

    private static final String TEST_IMAP_FOLDER_NAME = "aaa";

    private static final String MAILBOX_NAME_A = "mailbox_a";
    private static final String MAILBOX_NAME_B = "mailbox_b";
    private static final String MAILBOX_PATTERN = "mailbox*";

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
    private ContentService contentService;

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
        contentService = serviceRegistry.getContentService();
        
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

        NodeRef companyHomeNodeRef = findCompanyHomeNodeRef();

        ChildApplicationContextFactory imap = (ChildApplicationContextFactory) ctx.getBean("imap");
        ApplicationContext imapCtx = imap.getApplicationContext();
        ImapServiceImpl imapServiceImpl = (ImapServiceImpl)imapCtx.getBean("imapService");

        // Creating IMAP test folder for IMAP root
        LinkedList<String> folders = new LinkedList<String>();
        folders.add(TEST_IMAP_FOLDER_NAME);
        FileFolderUtil.makeFolders(fileFolderService, companyHomeNodeRef, folders, ContentModel.TYPE_FOLDER);
        
        // Setting IMAP root
        RepositoryFolderConfigBean imapHome = new RepositoryFolderConfigBean();
        imapHome.setStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString());
        imapHome.setRootPath(APP_COMPANY_HOME);
        imapHome.setFolderPath(NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_FOLDER_NAME);
        imapServiceImpl.setImapHome(imapHome);
        
        // Starting IMAP
        imapServiceImpl.startupInTxn(true);

        NodeRef storeRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef ,
                APP_COMPANY_HOME + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_FOLDER_NAME,
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

    private boolean checkSubscribedMailbox(AlfrescoImapUser user, String mailboxName)
    {
        List<AlfrescoImapFolder> aifs = imapService.listMailboxes(user, mailboxName, true);
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
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        assertTrue(checkMailbox(user, MAILBOX_NAME_A));
    }
    
    public void testListMailbox() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_B, false, true);
        List<AlfrescoImapFolder> mf = imapService.listMailboxes(user, MAILBOX_PATTERN, false);
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
        
        mf = imapService.listMailboxes(user, MAILBOX_PATTERN, false);
        assertEquals("can't repeat the listing of folders", 2, mf.size());
        
        mf = imapService.listMailboxes(user, MAILBOX_PATTERN, false);
        assertEquals("can't repeat the listing of folders", 2, mf.size());
        
        /**
         * The new mailboxes should be subscribed?
         */
        List<AlfrescoImapFolder> aif = imapService.listMailboxes(user, MAILBOX_PATTERN, true);
        assertEquals("not subscribed to two mailboxes", 2, aif.size());
        
        /**
         * Unsubscribe to one of the mailboxes.
         */
        imapService.unsubscribe(user, MAILBOX_NAME_B);
        List<AlfrescoImapFolder> aif2 = imapService.listMailboxes(user, MAILBOX_PATTERN, true);
        assertEquals("not subscribed to one mailbox", 1, aif2.size());
    }
    
    public void testListSubscribedMailbox() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_B, false, true);
        imapService.subscribe(user, MAILBOX_NAME_A);
        imapService.subscribe(user, MAILBOX_NAME_B);
        List<AlfrescoImapFolder> aif = imapService.listMailboxes(user, MAILBOX_PATTERN, true);
        assertEquals(aif.size(), 2);
        
        assertTrue("Can't subscribe mailbox A", checkSubscribedMailbox(user, MAILBOX_NAME_A));
        assertTrue("Can't subscribe mailbox B", checkSubscribedMailbox(user, MAILBOX_NAME_B));
    }

    public void testCreateMailbox() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        assertTrue("Mailbox isn't created", checkMailbox(user, MAILBOX_NAME_A));
    }

    public void testDuplicateMailboxes() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        try
        {
            imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
            fail("Duplicate Mailbox was created");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected
        }

    }

    public void testRenameMailbox() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        imapService.renameMailbox(user, MAILBOX_NAME_A, MAILBOX_NAME_B);
        assertFalse("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_A));
        assertTrue("Can't rename mailbox", checkMailbox(user, MAILBOX_NAME_B));
    }

    public void testRenameMailboxDuplicate() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_B, false, true);
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
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_B, false, true);
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
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);

        imapService.subscribe(user, MAILBOX_NAME_A);
        assertTrue("Can't subscribe mailbox", checkSubscribedMailbox(user, MAILBOX_NAME_A));
    }

    public void testUnsubscribe() throws Exception
    {
        imapService.getOrCreateMailbox(user, MAILBOX_NAME_A, false, true);
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
        NavigableMap<Long, FileInfo> fis = imapService.getFolderStatus(authenticationService.getCurrentUserName(), testImapFolderNodeRef, ImapViewMode.ARCHIVE).search;
        if (fis != null && fis.size() > 0)
        {
            FileInfo messageFileInfo = fis.firstEntry().getValue();
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
        NavigableMap<Long, FileInfo> fis = imapService.getFolderStatus(authenticationService.getCurrentUserName(), testImapFolderNodeRef, ImapViewMode.ARCHIVE).search;
        if (fis != null && fis.size() > 0)
        {
            FileInfo messageFileInfo = fis.firstEntry().getValue();
            
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
        NavigableMap<Long, FileInfo> fis = imapService.getFolderStatus(authenticationService.getCurrentUserName(), testImapFolderNodeRef, ImapViewMode.ARCHIVE).search;
        if (fis != null && fis.size() > 0)
        {
            FileInfo messageFileInfo = fis.firstEntry().getValue();
            
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
        
        imapService.getOrCreateMailbox(user, MAILBOX_ACCENTED_NAME_A, false, true);
        imapService.deleteMailbox(user, MAILBOX_ACCENTED_NAME_A);
        
        imapService.getOrCreateMailbox(user, MAILBOX_ACCENTED_NAME_A, false, true);
        imapService.renameMailbox(user, MAILBOX_ACCENTED_NAME_A, MAILBOX_ACCENTED_NAME_B);
        assertFalse("Can't rename mailbox", checkMailbox(user, MAILBOX_ACCENTED_NAME_A));
        assertTrue("Can't rename mailbox", checkMailbox(user, MAILBOX_ACCENTED_NAME_B));
        imapService.deleteMailbox(user, MAILBOX_ACCENTED_NAME_B);
    }
    
    public void testContentRecovery() throws Exception
    {
        reauthenticate(USER_NAME, USER_PASSWORD);
        
        // create content
        NodeRef nodeRef = nodeService.createNode(testImapFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content_recover"), ContentModel.TYPE_CONTENT).getChildRef();
        FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);
        
        // Outlook sets flags that indicates that a content was seen and deleted
        imapService.setFlag(fileInfo, Flags.Flag.DELETED, true);
        imapService.setFlag(fileInfo, Flags.Flag.SEEN, true);
        
        // delete a content
        fileFolderService.delete(nodeRef);
        
        // get archive node reference
        String storePath = "archive://SpacesStore";
        StoreRef storeRef = new StoreRef(storePath);
        NodeRef archivedNodeRef = new NodeRef(storeRef, nodeRef.getId());
       
        // restore a node and check flags
        Boolean value = false;
        if (nodeService.exists(archivedNodeRef))
        {
            NodeRef restoredNode = nodeService.restoreNode(archivedNodeRef, testImapFolderNodeRef, null, null);

            Map<QName, Serializable> props = nodeService.getProperties(restoredNode);

            if (props.containsKey(ImapModel.PROP_FLAG_DELETED) && props.containsKey(ImapModel.PROP_FLAG_SEEN))
            {
                value = !(Boolean) props.get(ImapModel.PROP_FLAG_DELETED) && !(Boolean) props.get(ImapModel.PROP_FLAG_SEEN);
            }
        }
        
        assertTrue("Can't set DELETED flag to false", value);
    }
    
    /**
     * Test attachment extraction with a TNEF message
     * @throws Exception
     */
    public void testAttachmentExtraction() throws Exception
    {
        AuthenticationUtil.setRunAsUserSystem();
        /**
         * Load a TNEF message
         */
        ClassPathResource fileResource = new ClassPathResource("imap/test-tnef-message.eml");
        assertNotNull("unable to find test resource test-tnef-message.eml", fileResource);
        InputStream is = new FileInputStream(fileResource.getFile());
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), is);

        NodeRef companyHomeNodeRef = findCompanyHomeNodeRef();

        FileInfo f1 = fileFolderService.create(companyHomeNodeRef, "ImapServiceImplTest", ContentModel.TYPE_FOLDER);
        FileInfo f2 = fileFolderService.create(f1.getNodeRef(), "test-tnef-message.eml", ContentModel.TYPE_CONTENT);
        
        ContentWriter writer = fileFolderService.getWriter(f2.getNodeRef());
        writer.putContent(new FileInputStream(fileResource.getFile()));
        
        imapService.extractAttachments(f2.getNodeRef(), message);

        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(f2.getNodeRef(), ImapModel.ASSOC_IMAP_ATTACHMENTS_FOLDER);
        assertTrue("attachment folder is found", targetAssocs.size() == 1);
        NodeRef attachmentFolderRef = targetAssocs.get(0).getTargetRef();
        
        assertNotNull(attachmentFolderRef);

        List<FileInfo> files = fileFolderService.listFiles(attachmentFolderRef);
        assertTrue("three files not found", files.size() == 3);

    }

    public void testMailboxRenamingInTheRootForMNT9055() throws Exception
    {
        reauthenticate(USER_NAME, USER_PASSWORD);
        AlfrescoImapUser poweredUser = new AlfrescoImapUser((USER_NAME + "@alfresco.com"), USER_NAME, USER_PASSWORD);

        NodeRef root = findCompanyHomeNodeRef();

        String targetNodeName = "Test-" + System.currentTimeMillis();

        FileInfo targetNode = fileFolderService.create(root, targetNodeName, ContentModel.TYPE_FOLDER);

        String renamedNodeName = "Renamed-" + System.currentTimeMillis();
        String renamedNodePath = IMAP_ROOT + AlfrescoImapConst.HIERARCHY_DELIMITER + renamedNodeName;

        assertMailboxRenaming(poweredUser, root, (IMAP_ROOT + AlfrescoImapConst.HIERARCHY_DELIMITER + targetNodeName), java.util.Collections.singletonList(renamedNodeName),
                renamedNodePath, targetNode);

        AlfrescoImapFolder actualMailbox = imapService.getOrCreateMailbox(poweredUser, renamedNodePath, true, false);

        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(actualMailbox.getFolderInfo().getNodeRef());

        assertNotNull(parentAssocs);
        assertFalse(parentAssocs.isEmpty());
        assertEquals(1, parentAssocs.size());

        assertEquals(root, parentAssocs.iterator().next().getParentRef());
    }

    public void testMailboxRenamingInTheUserImapHomeForMNT9055() throws Exception
    {
        reauthenticate(USER_NAME, USER_PASSWORD);
        AlfrescoImapUser poweredUser = new AlfrescoImapUser((USER_NAME + "@alfresco.com"), USER_NAME, USER_PASSWORD);

        NodeRef root = findCompanyHomeNodeRef();

        String targetNodeName = "Test-" + System.currentTimeMillis();
        String renamedNodeName = "Renamed-" + System.currentTimeMillis();

        AlfrescoImapFolder mailbox = imapService.getOrCreateMailbox(poweredUser, targetNodeName, false, true);
        assertMailboxNotNull(mailbox);

        List<String> pathBeforeRenaming = fileFolderService.getNameOnlyPath(root, mailbox.getFolderInfo().getNodeRef());

        assertMailboxRenaming(poweredUser, root, targetNodeName, Collections.singletonList(renamedNodeName), renamedNodeName, null);

        mailbox = imapService.getOrCreateMailbox(poweredUser, renamedNodeName, true, false);
        assertMailboxNotNull(mailbox);

        List<String> pathAfterRenaming = fileFolderService.getNameOnlyPath(root, mailbox.getFolderInfo().getNodeRef());

        assertPathHierarchy(pathBeforeRenaming, pathAfterRenaming);
    }

    public void testMailboxRenamingInDeepHierarchyForMNT9055() throws Exception
    {
        reauthenticate(USER_NAME, USER_PASSWORD);
        AlfrescoImapUser poweredUser = new AlfrescoImapUser((USER_NAME + "@alfresco.com"), USER_NAME, USER_PASSWORD);

        NodeRef root = findCompanyHomeNodeRef();

        NodeRef parent = root;

        FileInfo targetNode = null;
        String targetNodeName = IMAP_ROOT;
        StringBuilder fullPath = new StringBuilder();
        List<String> fullPathList = new LinkedList<String>();
        String renamedNodeName = "Renamed-" + System.currentTimeMillis();

        for (int i = 0; i < 10; i++)
        {
            fullPath.append(targetNodeName).append(AlfrescoImapConst.HIERARCHY_DELIMITER);
            targetNodeName = new StringBuilder("Test").append(i).append("-").append(System.currentTimeMillis()).toString();

            if (i < 9)
            {
                fullPathList.add(targetNodeName);
            }

            targetNode = fileFolderService.create(parent, targetNodeName, ContentModel.TYPE_FOLDER);
            assertNotNull(targetNode);

            parent = targetNode.getNodeRef();
            assertNotNull(parent);
        }

        String path = fullPath.toString();
        String targetNodePath = path + targetNodeName;
        String renamedNodePath = path + renamedNodeName;

        List<String> renamedFullPathList = new LinkedList<String>(fullPathList);
        renamedFullPathList.add(renamedNodeName);
        fullPathList.add(targetNodeName);

        assertMailboxRenaming(poweredUser, root, targetNodePath, renamedFullPathList, renamedNodePath, targetNode);

        AlfrescoImapFolder mailbox = imapService.getOrCreateMailbox(poweredUser, renamedNodePath, true, false);
        assertMailboxNotNull(mailbox);

        List<String> pathAfterRenaming = fileFolderService.getNameOnlyPath(root, mailbox.getFolderInfo().getNodeRef());

        assertPathHierarchy(fullPathList, pathAfterRenaming);

        FileInfo renamedNode = fileFolderService.resolveNamePath(root, pathAfterRenaming);
        assertNotNull(renamedNode);
        assertEquals(targetNode.getNodeRef(), renamedNode.getNodeRef());
    }

    public void testMiddleMailboxRenamingInDeepHierarchyForMNT9055() throws Exception
    {
        reauthenticate(USER_NAME, USER_PASSWORD);
        AlfrescoImapUser poweredUser = new AlfrescoImapUser((USER_NAME + "@alfresco.com"), USER_NAME, USER_PASSWORD);

        NodeRef root = findCompanyHomeNodeRef();

        NodeRef parent = root;

        String middleName = null; // Name of the middle folder
        FileInfo targetNode = null; // A leaf folder
        FileInfo middleNode = null; // A middle folder
        String targetNodeName = IMAP_ROOT; // Current folder name
        StringBuilder fullPath = new StringBuilder(); // Path from the middle to the leaf folder
        StringBuilder fullLeftPath = new StringBuilder(); // Path to the middle folder
        List<String> fullLeftPathList = new LinkedList<String>(); // Path elements to the middle folder
        List<String> fullRightPathList = new LinkedList<String>(); // Path from the middle to the leaf folder
        String renamedNodeName = "Renamed-" + System.currentTimeMillis();

        for (int i = 0; i < 10; i++)
        {
            if (i <= 5)
            {
                fullLeftPath.append(targetNodeName).append(AlfrescoImapConst.HIERARCHY_DELIMITER);
            }
            else
            {
                if (i > 6)
                {
                    fullPath.append(targetNodeName).append(AlfrescoImapConst.HIERARCHY_DELIMITER);
                }
            }

            targetNodeName = new StringBuilder("Test").append(i).append("-").append(System.currentTimeMillis()).toString();

            if (i < 5)
            {
                fullLeftPathList.add(targetNodeName);
            }
            else
            {
                if (i > 5)
                {
                    fullRightPathList.add(targetNodeName);
                }
            }

            targetNode = fileFolderService.create(parent, targetNodeName, ContentModel.TYPE_FOLDER);
            assertNotNull(targetNode);
            assertNotNull(targetNode.getNodeRef());

            parent = targetNode.getNodeRef();

            if (5 == i)
            {
                middleName = targetNodeName;
                middleNode = targetNode;
            }
        }

        String path = fullLeftPath.toString();
        String targetNodePath = path + middleName;
        String renamedNodePath = path + renamedNodeName;

        List<String> renamedFullLeftPathList = new LinkedList<String>(fullLeftPathList);
        renamedFullLeftPathList.add(renamedNodeName);
        fullLeftPathList.add(middleName);

        assertMailboxRenaming(poweredUser, root, targetNodePath, renamedFullLeftPathList, renamedNodePath, middleNode);

        AlfrescoImapFolder mailbox = imapService.getOrCreateMailbox(poweredUser, renamedNodePath, true, false);
        assertMailboxNotNull(mailbox);

        List<String> pathAfterRenaming = fileFolderService.getNameOnlyPath(root, mailbox.getFolderInfo().getNodeRef());

        assertPathHierarchy(fullLeftPathList, pathAfterRenaming);

        FileInfo renamedNode = fileFolderService.resolveNamePath(root, pathAfterRenaming);
        assertNotNull(renamedNode);
        assertEquals(middleNode.getNodeRef(), renamedNode.getNodeRef());

        String fullRenamedPath = new StringBuilder(path).append(renamedNodeName).append(AlfrescoImapConst.HIERARCHY_DELIMITER).append(fullPath).append(targetNodeName).toString();
        mailbox = imapService.getOrCreateMailbox(poweredUser, fullRenamedPath, true, false);
        assertMailboxNotNull(mailbox);

        assertEquals(targetNode.getNodeRef(), mailbox.getFolderInfo().getNodeRef());

        pathAfterRenaming = fileFolderService.getNameOnlyPath(middleNode.getNodeRef(), targetNode.getNodeRef());
        assertPathHierarchy(fullRightPathList, pathAfterRenaming);
    }

    public void testMailboxMoving() throws Exception
    {
        reauthenticate(USER_NAME, USER_PASSWORD);
        AlfrescoImapUser poweredUser = new AlfrescoImapUser((USER_NAME + "@alfresco.com"), USER_NAME, USER_PASSWORD);

        NodeRef root = findCompanyHomeNodeRef();

        FileInfo targetNode = null;
        String imapUserHomePath = "";
        StringBuilder fullPath = new StringBuilder();
        List<String> fullPathList = new LinkedList<String>(Arrays.asList(new String[] { TEST_IMAP_FOLDER_NAME, USER_NAME }));
        String targetNodeName = new StringBuilder(IMAP_ROOT).append(AlfrescoImapConst.HIERARCHY_DELIMITER).append(TEST_IMAP_FOLDER_NAME).append(
                AlfrescoImapConst.HIERARCHY_DELIMITER).append(USER_NAME).toString();

        for (int i = 0; i < 4; i++)
        {
            fullPath.append(targetNodeName).append(AlfrescoImapConst.HIERARCHY_DELIMITER);
            targetNodeName = new StringBuilder("Test").append(i).append("-").append(System.currentTimeMillis()).toString();

            if (i < 3)
            {
                fullPathList.add(targetNodeName);
            }

            AlfrescoImapFolder mailbox = imapService.getOrCreateMailbox(poweredUser, (imapUserHomePath + targetNodeName), false, true);
            assertNotNull(mailbox);

            targetNode = mailbox.getFolderInfo();
            imapUserHomePath = fullPath.toString() + targetNodeName + AlfrescoImapConst.HIERARCHY_DELIMITER;
        }

        String path = fullPath.toString();
        String targetNodePath = path + targetNodeName;
        String newName = "Renamed-" + System.currentTimeMillis();
        String renamedNodeName = IMAP_ROOT + AlfrescoImapConst.HIERARCHY_DELIMITER + newName;

        imapService.renameMailbox(poweredUser, targetNodePath, renamedNodeName);

        AlfrescoImapFolder mailbox = imapService.getOrCreateMailbox(poweredUser, renamedNodeName, true, false);
        assertMailboxNotNull(mailbox);

        targetNodePath = renamedNodeName;
        renamedNodeName = path + newName;
        imapService.renameMailbox(poweredUser, targetNodePath, renamedNodeName);

        mailbox = imapService.getOrCreateMailbox(poweredUser, renamedNodeName, true, false);
        assertMailboxNotNull(mailbox);

        assertMailboxInUserImapHomeDirectory(root, mailbox.getFolderInfo().getNodeRef());

        List<String> pathAfterRenaming = fileFolderService.getNameOnlyPath(root, targetNode.getNodeRef());
        fullPathList.add(newName);
        assertPathHierarchy(fullPathList, pathAfterRenaming);
    }

    /**
     * Test for MNT-12259
     * There is a 5s gap to run the test, see {@link ImapServiceImpl#hideAndDelete}
     * 
     * @throws Exception 
     */
    public void testMoveViaDeleteAndAppend() throws Exception
    {
        AlfrescoImapUser poweredUser = new AlfrescoImapUser((USER_NAME + "@alfresco.com"), USER_NAME, USER_PASSWORD);
        String fileName = "testfile" + GUID.generate();
        String destinationName = "testFolder" + GUID.generate();
        String destinationPath = IMAP_ROOT + AlfrescoImapConst.HIERARCHY_DELIMITER + destinationName;
        String nodeContent = "test content";
        NodeRef root = findCompanyHomeNodeRef();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create node and destination folder
        FileInfo origFile = fileFolderService.create(root, fileName, ContentModel.TYPE_CONTENT);
        ContentWriter contentWriter = contentService.getWriter(origFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype("text/plain");
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent(nodeContent);

        FileInfo destinationNode = fileFolderService.create(root, destinationName, ContentModel.TYPE_FOLDER);
        nodeService.addAspect(origFile.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT, null);

        // Save the message and ensure the message id is set
        SimpleStoredMessage origMessage = imapService.getMessage(origFile);
        origMessage.getMimeMessage().saveChanges();

        imapService.setFlag(origFile, Flags.Flag.DELETED, true);
        // Delete the node
        imapService.expungeMessage(origFile);

        // Append the message to destination
        AlfrescoImapFolder destinationMailbox = imapService.getOrCreateMailbox(poweredUser, destinationPath, true, false);
        destinationMailbox.appendMessage(origMessage.getMimeMessage(), flags, null);

        // Check the destination has the original file and only this file
        FileInfo movedNode = fileFolderService.getFileInfo(origFile.getNodeRef());
        assertNotNull("The file should exist.", movedNode);
        assertEquals("The file name should not change.", fileName, movedNode.getName());
        NodeRef newParentNodeRef = nodeService.getPrimaryParent(origFile.getNodeRef()).getParentRef();
        assertEquals("The parent should change to destination.", destinationNode.getNodeRef(), newParentNodeRef);
        assertEquals("There should be only one node in the destination folder", 1, nodeService.getChildAssocs(destinationNode.getNodeRef()).size());
    }
    
    /**
     * @param mailbox - {@link AlfrescoImapFolder} instance which should be checked
     */
    private void assertMailboxNotNull(AlfrescoImapFolder mailbox)
    {
        assertNotNull(mailbox);
        assertNotNull(mailbox.getFolderInfo());
        assertNotNull(mailbox.getFolderInfo().getNodeRef());
    }

    /**
     * @param user - {@link AlfrescoImapUser} instance, which determines a user who has enough permissions to rename a node
     * @param root - {@link NodeRef} instance, which determines <code>Alfresco IMAP</code> root node
     * @param targetNodePath - {@link String} value, which determines a path in IMAP notation to a node which should be renamed
     * @param targetNode - {@link FileInfo} instance, which determines a node, located at the <code>targetNodePath</code> path
     * @throws FileNotFoundException
     */
    private void assertMailboxRenaming(AlfrescoImapUser user, NodeRef root, String targetNodePath, List<String> renamedNodeName, String renamedNodePath, FileInfo targetNode)
            throws FileNotFoundException
    {
        AlfrescoImapFolder mailbox = imapService.getOrCreateMailbox(user, targetNodePath, true, false);
        assertNotNull(("Just created mailbox can't be received by full path via the ImapService. Path: '" + targetNodePath + "'"), mailbox);
        assertNotNull(mailbox.getFolderInfo());
        assertNotNull(mailbox.getFolderInfo().getNodeRef());

        imapService.renameMailbox(user, targetNodePath, renamedNodePath);

        NodeRef actualNode = null;

        if (null != targetNode)
        {
            FileInfo actualFileInfo = fileFolderService.resolveNamePath(root, renamedNodeName);
            assertNotNull(actualFileInfo);
            assertNotNull(actualFileInfo.getNodeRef());
            actualNode = actualFileInfo.getNodeRef();
        }
        else
        {
            mailbox = imapService.getOrCreateMailbox(user, renamedNodePath, true, false);
            assertNotNull(mailbox);
            actualNode = mailbox.getFolderInfo().getNodeRef();
        }

        assertNotNull(("Can't receive renamed node by full path: '" + renamedNodePath + "'"), actualNode);

        if (null != targetNode)
        {
            assertEquals(targetNode.getNodeRef(), actualNode);
        }
        else
        {
            assertMailboxInUserImapHomeDirectory(root, actualNode);
        }
    }

    /**
     * @param root - {@link NodeRef} instance, which determines <code>Alfresco IMAP</code> root node
     * @param actualNode - {@link NodeRef} instance, which determines mailbox in actual state
     * @throws FileNotFoundException
     */
    private void assertMailboxInUserImapHomeDirectory(NodeRef root, NodeRef actualNode) throws FileNotFoundException
    {
        List<String> path = fileFolderService.getNameOnlyPath(root, actualNode);

        int satisfactionFlag = 0;
        for (String element : path)
        {
            if (TEST_IMAP_FOLDER_NAME.equals(element) || USER_NAME.equals(element))
            {
                satisfactionFlag++;
            }

            if (satisfactionFlag > 1)
            {
                break;
            }
        }

        assertTrue(satisfactionFlag > 1);
    }

    /**
     * @param pathBeforeRenaming - {@link List}&lt;{@link String}&gt; instance, which represents a path to some node <b>before</b> some action
     * @param pathAfterRenaming - {@link List}&lt;{@link String}&gt; instance, which represents a path to some node <b>after</b> some action
     */
    private void assertPathHierarchy(List<String> pathBeforeRenaming, List<String> pathAfterRenaming)
    {
        assertNotNull(pathAfterRenaming);
        assertNotNull(pathBeforeRenaming);
        assertEquals(pathBeforeRenaming.size(), pathAfterRenaming.size());

        Iterator<String> before = pathBeforeRenaming.iterator();
        Iterator<String> after = pathAfterRenaming.iterator();

        for (int i = 0; i < (pathAfterRenaming.size() - 2); i++)
        {
            assertTrue(before.hasNext());
            assertTrue(after.hasNext());

            assertEquals(before.next(), after.next());
        }
    }

    /**
     * @return {@link NodeRef} instance of '<code>/app:company_home</code>' node
     */
    private NodeRef findCompanyHomeNodeRef()
    {
        NodeRef storeRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, APP_COMPANY_HOME, null, namespaceService, false);
        return nodeRefs.get(0);
    }
}
