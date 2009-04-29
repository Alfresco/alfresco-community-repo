/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content.cleanup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.domain.ContentUrlDAO;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @see org.alfresco.repo.content.cleanup.ContentStoreCleaner
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanerTest extends TestCase
{
    private static ConfigurableApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentService contentService;
    private NodeService nodeService;
    private CopyService copyService;
    private TransactionService transactionService;
    private ContentStoreCleaner cleaner;
    private ContentStore store;
    private ContentStoreCleanerListener listener;
    private List<String> deletedUrls;
    
    @Override
    public void setUp() throws Exception
    {
        AuthenticationUtil.setRunAsUserSystem();
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        contentService = serviceRegistry.getContentService();
        nodeService = serviceRegistry.getNodeService();
        copyService = serviceRegistry.getCopyService();
        transactionService = serviceRegistry.getTransactionService();
        TransactionService transactionService = serviceRegistry.getTransactionService();
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        PolicyComponent policyComponent = (PolicyComponent) ctx.getBean("policyComponent");
        NodeDaoService nodeDaoService = (NodeDaoService) ctx.getBean("nodeDaoService");
        AVMNodeDAO avmNodeDAO = (AVMNodeDAO) ctx.getBean("avmNodeDAO");
        ContentUrlDAO contentUrlDAO = (ContentUrlDAO) ctx.getBean("contentUrlDAO");
        
        // we need a store
        store = new FileContentStore(ctx, TempFileProvider.getTempDir().getAbsolutePath());
        // and a listener
        listener = new DummyCleanerListener();
        // initialise record of deleted URLs
        deletedUrls = new ArrayList<String>(5);
        
        // construct the test cleaner
        cleaner = new ContentStoreCleaner();
        cleaner.setTransactionService(transactionService);
        cleaner.setDictionaryService(dictionaryService);
        cleaner.setPolicyComponent(policyComponent);
        cleaner.setContentService(contentService);
        cleaner.setNodeDaoService(nodeDaoService);
        cleaner.setAvmNodeDAO(avmNodeDAO);
        cleaner.setContentUrlDAO(contentUrlDAO);
        cleaner.setStores(Collections.singletonList(store));
        cleaner.setListeners(Collections.singletonList(listener));
        cleaner.setEagerOrphanCleanup(false);
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testEagerCleanupOnCommit() throws Exception
    {
        // Get the context-defined cleaner
        ContentStoreCleaner cleaner = (ContentStoreCleaner) ctx.getBean("contentStoreCleaner");
        // Force eager cleanup
        cleaner.setEagerOrphanCleanup(true);
        cleaner.init();
        // Create a new file
        RetryingTransactionCallback<NodeRef> makeContentCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create some content
                StoreRef storeRef = nodeService.createStore("test", "testEagerCleanupOnCommit-" + GUID.generate());
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                Map<QName, Serializable> properties = Collections.singletonMap(ContentModel.PROP_NAME, (Serializable)"test.txt");
                NodeRef contentNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_CONTENT,
                        properties).getChildRef();
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("INITIAL CONTENT");
                // Done
                return contentNodeRef;
            }
        };
        final NodeRef contentNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(makeContentCallback);
        ContentReader contentReader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertTrue("Expect content to exist", contentReader.exists());
        
        // Now update the node, but force a failure i.e. txn rollback
        final List<String> newContentUrls = new ArrayList<String>();
        RetryingTransactionCallback<String> failUpdateCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("CONTENT FOR FAIL");
                // This will have updated the metadata, so we can fail now
                newContentUrls.add(writer.getContentUrl());
                // Done
                throw new RuntimeException("FAIL");
            }
        };
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(failUpdateCallback);
            fail("Transaction was meant to fail");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        // Make sure that the new content is not there
        // The original content must still be there
        assertEquals("Expected one content URL to play with", 1, newContentUrls.size());
        ContentReader readerMissing = contentService.getRawReader(newContentUrls.get(0));
        assertFalse("Newly created content should have been removed.", readerMissing.exists());
        assertTrue("Original content should still be there.", contentReader.exists());
        
        // Now update the node successfully
        RetryingTransactionCallback<String> successUpdateCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("CONTENT FOR SUCCESS");
                // Done
                return writer.getContentUrl();
            }
        };
        String newContentUrl = transactionService.getRetryingTransactionHelper().doInTransaction(successUpdateCallback);
        // Make sure that the new content is there
        // The original content was disposed of
        ContentReader contentReaderNew = contentService.getRawReader(newContentUrl);
        assertTrue("Newly created content should be present.", contentReaderNew.exists());
        assertFalse("Original content should have been removed.", contentReader.exists());
        
        // Now delete the node
        RetryingTransactionCallback<Object> deleteNodeCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                nodeService.deleteNode(contentNodeRef);
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteNodeCallback);
        // The new content must have disappeared
        assertFalse("Newly created content should be removed.", contentReaderNew.exists());
    }
    
    /**
     * TODO: This test must be replaced with one that checks that the raw content binary lives
     *       as long as there is a reference to it.  Once the RM-hacks are removed, content
     *       will once again be shared and must therefore be cleaned up based on unlinking of
     *       references.
     */
    public void testEagerCleanupAfterCopy() throws Exception
    {
        // Get the context-defined cleaner
        ContentStoreCleaner cleaner = (ContentStoreCleaner) ctx.getBean("contentStoreCleaner");
        // Force eager cleanup
        cleaner.setEagerOrphanCleanup(true);
        cleaner.init();
        // Create a new file, copy it
        RetryingTransactionCallback<Pair<NodeRef, NodeRef>> copyFileCallback = new RetryingTransactionCallback<Pair<NodeRef, NodeRef>>()
        {
            public Pair<NodeRef, NodeRef> execute() throws Throwable
            {
                // Create some content
                StoreRef storeRef = nodeService.createStore("test", "testEagerCleanupAfterCopy-" + GUID.generate());
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                Map<QName, Serializable> properties = Collections.singletonMap(ContentModel.PROP_NAME, (Serializable)"test.txt");
                NodeRef contentNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_CONTENT,
                        properties).getChildRef();
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("INITIAL CONTENT");
                // Now copy it
                NodeRef copiedNodeRef = copyService.copy(
                        contentNodeRef,
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN);
                // Done
                return new Pair<NodeRef, NodeRef>(contentNodeRef, copiedNodeRef);
            }
        };
        Pair<NodeRef, NodeRef> nodeRefPair = transactionService.getRetryingTransactionHelper().doInTransaction(copyFileCallback);
        // Check that the readers of the content have different URLs
        ContentReader contentReaderSource = contentService.getReader(nodeRefPair.getFirst(), ContentModel.PROP_CONTENT);
        assertNotNull("Expected reader for source cm:content", contentReaderSource);
        assertTrue("Expected content for source cm:content", contentReaderSource.exists());
        ContentReader contentReaderCopy = contentService.getReader(nodeRefPair.getSecond(), ContentModel.PROP_CONTENT);
        assertNotNull("Expected reader for copy cm:content", contentReaderCopy);
        assertTrue("Expected content for copy cm:content", contentReaderCopy.exists());
        String contentUrlSource = contentReaderSource.getContentUrl();
        String contentUrlCopy = contentReaderCopy.getContentUrl();
        assertFalse("Source and copy must have different URLs",
                EqualsHelper.nullSafeEquals(contentUrlSource, contentUrlCopy));
    }
    
    public void testImmediateRemoval() throws Exception
    {
        cleaner.setProtectDays(0);
        // add some content to the store
        ContentWriter writer = store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.putContent("ABC");
        String contentUrl = writer.getContentUrl();
        
        // fire the cleaner
        cleaner.execute();
        
        // the content should have disappeared as it is not in the database
        assertFalse("Unprotected content was not deleted", store.exists(contentUrl));
        assertTrue("Content listener was not called", deletedUrls.contains(contentUrl));
    }
    
    public void testProtectedRemoval() throws Exception
    {
        cleaner.setProtectDays(1);
        // add some content to the store
        ContentWriter writer = store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.putContent("ABC");
        String contentUrl = writer.getContentUrl();
        
        // fire the cleaner
        cleaner.execute();
        
        // the content should have disappeared as it is not in the database
        assertTrue("Protected content was deleted", store.exists(contentUrl));
        assertFalse("Content listener was called with deletion of protected URL", deletedUrls.contains(contentUrl));
    }
    
//    public void testConcurrentRemoval() throws Exception
//    {
//        int threadCount = 2;
//        final CountDownLatch endLatch = new CountDownLatch(threadCount);
//        // Kick off the threads
//        for (int i = 0; i < threadCount; i++)
//        {
//            Thread thread = new Thread()
//            {
//                @Override
//                public void run()
//                {
//                    cleaner.execute();
//                    // Notify of completion
//                    endLatch.countDown();
//                }
//            };
//            thread.start();
//        }
//        // Wait for them all to be done
//        endLatch.await();
//    }
//    
    private class DummyCleanerListener implements ContentStoreCleanerListener
    {
        public void beforeDelete(ContentStore store, String contentUrl) throws ContentIOException
        {
            deletedUrls.add(contentUrl);
        }
    }
}
