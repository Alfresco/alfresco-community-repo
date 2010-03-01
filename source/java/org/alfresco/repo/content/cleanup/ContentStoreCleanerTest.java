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
package org.alfresco.repo.content.cleanup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.content.cleanup.ContentStoreCleaner
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanerTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentService contentService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private JobLockService jobLockService;
    private ContentStoreCleaner cleaner;
    private EagerContentStoreCleaner eagerCleaner;
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
        transactionService = serviceRegistry.getTransactionService();
        jobLockService = serviceRegistry.getJobLockService();
        TransactionService transactionService = serviceRegistry.getTransactionService();
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        NodeDaoService nodeDaoService = (NodeDaoService) ctx.getBean("nodeDaoService");
        AVMNodeDAO avmNodeDAO = (AVMNodeDAO) ctx.getBean("newAvmNodeDAO");
        ContentDataDAO contentDataDAO = (ContentDataDAO) ctx.getBean("contentDataDAO");
        
        // we need a store
        store = (ContentStore) ctx.getBean("fileContentStore");
        // and a listener
        listener = new DummyCleanerListener();
        // initialise record of deleted URLs
        deletedUrls = new ArrayList<String>(5);
        
        // Construct the test cleaners
        eagerCleaner = (EagerContentStoreCleaner) ctx.getBean("eagerContentStoreCleaner");
        eagerCleaner.setEagerOrphanCleanup(false);
        eagerCleaner.setStores(Collections.singletonList(store));
        eagerCleaner.setListeners(Collections.singletonList(listener));
        
        cleaner = new ContentStoreCleaner();
        cleaner.setEagerContentStoreCleaner(eagerCleaner);
        cleaner.setJobLockService(jobLockService);
        cleaner.setContentDataDAO(contentDataDAO);
        cleaner.setTransactionService(transactionService);
        cleaner.setDictionaryService(dictionaryService);
        cleaner.setContentService(contentService);
        cleaner.setNodeDaoService(nodeDaoService);
        cleaner.setAvmNodeDAO(avmNodeDAO);
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testEagerCleanupOnCommit() throws Exception
    {
        eagerCleaner.setEagerOrphanCleanup(true);
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
            if (e.getMessage().equals("FAIL"))
            {
                // Expected
            }
            else
            {
                // Ooops
                throw e;
            }
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
     * Create ContentData set it on a Node, delete the Node, then set the ContentData on a new node
     * and check that the content is preserved during eager cleanup.
     */
    public void testEagerCleanupDereferencing() throws Exception
    {
        eagerCleaner.setEagerOrphanCleanup(true);
        
        final StoreRef storeRef = nodeService.createStore("test", getName() + "-" + GUID.generate());
        RetryingTransactionCallback<ContentData> testCallback = new RetryingTransactionCallback<ContentData>()
        {
            public ContentData execute() throws Throwable
            {
                // Create some content
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(13);
                properties.put(ContentModel.PROP_NAME, (Serializable)"test.txt");
                NodeRef contentNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_CONTENT,
                        properties).getChildRef();
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("INITIAL CONTENT");
                ContentData contentData = writer.getContentData();
               
                // Delete the first node
                nodeService.deleteNode(contentNodeRef);
               
                ContentReader reader = contentService.getRawReader(contentData.getContentUrl());
                assertNotNull(reader);
                assertTrue("Content was cleaned before end of transaction", reader.exists());

                // Make a new copy using the same ContentData
                properties.put(ContentModel.PROP_NAME, (Serializable)"test2.txt");
                properties.put(ContentModel.PROP_CONTENT, contentData);
                contentNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_CONTENT,
                        properties).getChildRef();
                
                reader = contentService.getRawReader(contentData.getContentUrl());
                assertNotNull(reader);
                assertTrue("Content was cleaned before end of transaction", reader.exists());

                // Done
                return contentData;
            }
        };
        ContentData contentData = transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
        // Make sure that the content URL still exists
        ContentReader reader = contentService.getRawReader(contentData.getContentUrl());
        assertNotNull(reader);
        assertTrue("Content was cleaned despite being re-referenced in the transaction", reader.exists());
    }

    public void testImmediateRemoval() throws Exception
    {
        eagerCleaner.setEagerOrphanCleanup(false);
        
        final StoreRef storeRef = nodeService.createStore("test", getName() + "-" + GUID.generate());
        RetryingTransactionCallback<ContentData> testCallback = new RetryingTransactionCallback<ContentData>()
        {
            public ContentData execute() throws Throwable
            {
                // Create some content
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(13);
                properties.put(ContentModel.PROP_NAME, (Serializable)"test.txt");
                NodeRef contentNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_CONTENT,
                        properties).getChildRef();
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("INITIAL CONTENT");
                ContentData contentData = writer.getContentData();
               
                // Delete the first node
                nodeService.deleteNode(contentNodeRef);

                // Done
                return contentData;
            }
        };
        ContentData contentData = transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
        // Make sure that the content URL still exists
        ContentReader reader = contentService.getRawReader(contentData.getContentUrl());
        assertNotNull(reader);
        assertTrue("Content should not have been eagerly deleted.", reader.exists());
        
        // fire the cleaner
        cleaner.setProtectDays(0);
        cleaner.execute();
        
        reader = contentService.getRawReader(contentData.getContentUrl());
        // the content should have disappeared as it is not in the database
        assertFalse("Unprotected content was not deleted", reader.exists());
        assertTrue("Content listener was not called", deletedUrls.contains(reader.getContentUrl()));
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
    private class DummyCleanerListener implements ContentStoreCleanerListener
    {
        public void beforeDelete(ContentStore store, String contentUrl) throws ContentIOException
        {
            deletedUrls.add(contentUrl);
        }
    }
}
