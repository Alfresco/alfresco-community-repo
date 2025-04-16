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
package org.alfresco.repo.imap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.icegreen.greenmail.store.SimpleStoredMessage;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.alfresco.util.testing.category.LuceneTests;

/**
 * Unit test for cache implementation in the ImapServiceImpl. Based on ImapServiceImplTest, but we need this separate test because we need to get transactions to commit to trigger behaviours in ImapServiceImpl.
 * 
 * @author ArsenyKo
 */
@Category({OwnJVMTestsCategory.class, LuceneTests.class})
public class ImapServiceImplCacheTest extends TestCase
{
    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";

    private static final String TEST_IMAP_FOLDER_NAME = "aaa";

    private ApplicationContext ctx;
    private NodeService nodeService;
    private MutableAuthenticationService authenticationService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private FileInfo oldFile;

    private ImapService imapService;

    private NodeRef testImapFolderNodeRef;

    @Override
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        nodeService = serviceRegistry.getNodeService();
        authenticationService = serviceRegistry.getAuthenticationService();
        imapService = serviceRegistry.getImapService();
        searchService = serviceRegistry.getSearchService();
        namespaceService = serviceRegistry.getNamespaceService();
        fileFolderService = serviceRegistry.getFileFolderService();
        contentService = serviceRegistry.getContentService();

        authenticationService.authenticate(USER_NAME, USER_PASSWORD.toCharArray());

        String storePath = "workspace://SpacesStore";
        String companyHomePathInStore = "/app:company_home";

        StoreRef storeRef = new StoreRef(storePath);

        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
        NodeRef companyHomeNodeRef = nodeRefs.get(0);

        ChildApplicationContextFactory imap = (ChildApplicationContextFactory) ctx.getBean("imap");
        ApplicationContext imapCtx = imap.getApplicationContext();
        final ImapServiceImpl imapServiceImpl = (ImapServiceImpl) imapCtx.getBean("imapService");

        // Creating IMAP test folder for IMAP root
        LinkedList<String> folders = new LinkedList<String>();
        folders.add(TEST_IMAP_FOLDER_NAME);
        FileInfo folder = FileFolderUtil.makeFolders(fileFolderService, companyHomeNodeRef, folders, ContentModel.TYPE_FOLDER);
        oldFile = fileFolderService.create(folder.getNodeRef(), "oldFile", ContentModel.TYPE_CONTENT);

        // Setting IMAP root
        RepositoryFolderConfigBean imapHome = new RepositoryFolderConfigBean();
        imapHome.setStore(storePath);
        imapHome.setRootPath(companyHomePathInStore);
        imapHome.setFolderPath(NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_FOLDER_NAME);
        imapServiceImpl.setImapHome(imapHome);

        // Starting IMAP
        imapServiceImpl.startupInTxn(true);

        nodeRefs = searchService.selectNodes(storeRootNodeRef,
                companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_FOLDER_NAME,
                null,
                namespaceService,
                false);
        testImapFolderNodeRef = nodeRefs.get(0);

    }

    public void tearDown() throws Exception
    {
        fileFolderService.delete(testImapFolderNodeRef);
    }

    public void testRepoBehaviourWithFoldersCache() throws Exception
    {
        AlfrescoImapUser localUser = new AlfrescoImapUser(USER_NAME + "@alfresco.com", USER_NAME, USER_PASSWORD);
        String folderName = "ALF9361";
        String mailbox = "Alfresco IMAP" + AlfrescoImapConst.HIERARCHY_DELIMITER +
                TEST_IMAP_FOLDER_NAME + AlfrescoImapConst.HIERARCHY_DELIMITER +
                folderName;
        int contentItemsCount = 3;
        // Create a tree like ALF9361/ALF9361_0/sub_0
        // Mailbox path with default mount point should be like 'Alfresco IMAP/aaa/ALF9361/ALF9361_0/sub_0
        FileInfo localRootFolder = fileFolderService.create(testImapFolderNodeRef, folderName, ContentModel.TYPE_FOLDER);
        List<FileInfo> subFolders = new ArrayList<FileInfo>(10);
        for (int i = 0; i < 3; i++)
        {
            String childMailbox = folderName + "_" + i;
            FileInfo subFolder = fileFolderService.create(localRootFolder.getNodeRef(), childMailbox, ContentModel.TYPE_FOLDER);
            for (int j = 0; j < 3; j++)
            {
                String subChildMailbox = "sub_" + j;
                fileFolderService.create(subFolder.getNodeRef(), subChildMailbox, ContentModel.TYPE_FOLDER);
            }
            subFolders.add(subFolder);
        }
        // Create content within 'Alfresco IMAP/aaa/ALF9361'
        createTestContent(localRootFolder, contentItemsCount);
        // Load the cache
        imapService.listMailboxes(localUser, "*", false);
        imapService.listMailboxes(localUser, "*", true);
        // Get the folder to examine
        AlfrescoImapFolder folder = imapService.getOrCreateMailbox(localUser, mailbox, true, false);
        // Check the folder exist via IMAP
        assertNotNull("Folder wasn't successfully gotten from IMAP", folder);
        assertEquals(contentItemsCount, folder.getMessageCount());
        // Check UIDVALIDITY
        long uidValidityBefore = folder.getUidValidity();
        // Move in an old file with a smaller UID
        fileFolderService.move(oldFile.getNodeRef(), folder.getFolderInfo().getNodeRef(), folder.getName());
        // Get the folder once more and check it was changed since an old child was moved in
        folder = imapService.getOrCreateMailbox(localUser, mailbox, true, false);
        // Content count should be increased
        assertEquals(++contentItemsCount, folder.getMessageCount());
        long uidValidity = folder.getUidValidity();
        assertTrue("UIDVALIDITY wasn't incremented", (uidValidity - uidValidityBefore) > 0);
        // Delete first childMailbox 'ALF9361/ALF9361_0'
        // System.out.println(" --------------------- DELETE FOLDER --------------------");
        // System.out.println(" Parent " + localRootFolder.getNodeRef());
        fileFolderService.delete(subFolders.get(0).getNodeRef());
        uidValidityBefore = uidValidity;
        // Try to get deleted child
        try
        {
            String subFolderName = mailbox + AlfrescoImapConst.HIERARCHY_DELIMITER + folderName + "_0";
            folder = imapService.getOrCreateMailbox(localUser, subFolderName, true, false);
            fail("The folder still in the cache");
        }
        catch (RuntimeException e)
        {
            // expected
        }
        // Try to get deleted sub child. If the cache wasn't invalidated we will get it
        // But it should be connected to AlfrescoImapFolder.isStale() method.
        // ArsenyKo: I think we should avoid repo API invocation like isStale...
        try
        {
            String subSubFolderName = mailbox + AlfrescoImapConst.HIERARCHY_DELIMITER + mailbox + "_0" + AlfrescoImapConst.HIERARCHY_DELIMITER + "sub_0";
            folder = imapService.getOrCreateMailbox(localUser, subSubFolderName, true, false);
            fail("The folder still in the cache");
        }
        catch (RuntimeException e)
        {
            // expected
        }
        // Do manipulations with a content in the folder to check the cache behaviour
        folder = imapService.getOrCreateMailbox(localUser, mailbox, true, false);
        SimpleStoredMessage message = folder.getMessages().get(0);
        AbstractMimeMessage alfrescoMessage = (AbstractMimeMessage) message.getMimeMessage();
        long uid = message.getUid();
        // System.out.println(" --------------------- DELETE FILE --------------------");
        // System.out.println(" Parent " + folder.getFolderInfo().getNodeRef());
        // Delete a content
        fileFolderService.delete(alfrescoMessage.getMessageInfo().getNodeRef());
        // Get a folder once again. We expect that the folder would be retrieved from the repo,
        // since its' cache should be invalidated
        folder = imapService.getOrCreateMailbox(localUser, mailbox, true, false);
        // Additional check whether messages cache is valid. Messages cache should be recreated
        // with the new inctance of AlfrescoImapMessage
        assertTrue("Messages cache is stale", contentItemsCount > folder.getMessageCount());
        long[] uids = folder.getMessageUids();
        Arrays.sort(uids);
        assertFalse("Messages msn cache is stale", Arrays.binarySearch(uids, uid) > 0);
        assertNull("Message is still in the messages cache", folder.getMessage(uid));
        // System.out.println(" --------------------- THE END --------------------");
        fileFolderService.delete(localRootFolder.getNodeRef());

    }

    private List<FileInfo> createTestContent(FileInfo parent, int count)
    {
        List<FileInfo> result = new ArrayList<FileInfo>(count);
        for (int i = 0; i < count; i++)
        {
            FileInfo contentItem = fileFolderService.create(parent.getNodeRef(), "content_" + i, ContentModel.TYPE_CONTENT, ContentModel.ASSOC_CONTAINS);
            ContentWriter contentWriter = contentService.getWriter(contentItem.getNodeRef(), ContentModel.PROP_CONTENT, false);
            contentWriter.setEncoding("UTF-8");
            contentWriter.putContent("TEST" + i);
        }
        return result;
    }

}
