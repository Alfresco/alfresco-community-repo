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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.lock.JobLockService;
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
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.content.cleanup.ContentStoreCleaner
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
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
        AVMNodeDAO avmNodeDAO = (AVMNodeDAO) ctx.getBean("newAvmNodeDAO");
        ContentDataDAO contentDataDAO = (ContentDataDAO) ctx.getBean("contentDataDAO");
        
        // we need a store
        store = (ContentStore) ctx.getBean("fileContentStore");
        // and a listener
        List<ContentStoreCleanerListener> listeners = new ArrayList<ContentStoreCleanerListener>(2);
        listener = new DummyCleanerListener();
        listeners.add(listener);
        listeners.add(new DummyUnsupportiveCleanerListener());
        // initialise record of deleted URLs
        deletedUrls = new ArrayList<String>(5);
        
        // Construct the test cleaners
        eagerCleaner = (EagerContentStoreCleaner) ctx.getBean("eagerContentStoreCleaner");
        eagerCleaner.setEagerOrphanCleanup(false);
        eagerCleaner.setStores(Collections.singletonList(store));
        eagerCleaner.setListeners(listeners);
        
        cleaner = new ContentStoreCleaner();
        cleaner.setEagerContentStoreCleaner(eagerCleaner);
        cleaner.setJobLockService(jobLockService);
        cleaner.setContentDataDAO(contentDataDAO);
        cleaner.setTransactionService(transactionService);
        cleaner.setDictionaryService(dictionaryService);
        cleaner.setContentService(contentService);
        cleaner.setAvmNodeDAO(avmNodeDAO);
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    private void checkForExistence(Set<String> urls, boolean mustExist)
    {
        for (String url : urls)
        {
            ContentReader rawReader = contentService.getRawReader(url);
            if (mustExist && !rawReader.exists())
            {
                fail("Content URL should have existed but did not: " + url);
            }
            else if (!mustExist && rawReader.exists())
            {
                fail("Content URL should not have existed but did: " + url);
            }
        }
    }
    
    public void testEagerCleanupOnCommit() throws Exception
    {
        eagerCleaner.setEagerOrphanCleanup(true);
        final Set<String> urlsToExist = new HashSet<String>();
        final Set<String> urlsToMiss = new HashSet<String>();
        
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
                // Store the URL
                urlsToExist.add(writer.getContentUrl());
                // Done
                return contentNodeRef;
            }
        };
        final NodeRef contentNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(makeContentCallback);
        checkForExistence(urlsToExist, true);
        checkForExistence(urlsToMiss, false);
        
        // Now update the node, but force a failure i.e. txn rollback
        RetryingTransactionCallback<String> failUpdateCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("CONTENT FOR FAIL");
                // This will have updated the metadata, so we can fail now
                urlsToMiss.add(writer.getContentUrl());
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
        checkForExistence(urlsToExist, true);
        checkForExistence(urlsToMiss, false);
        
        // Now update the node successfully
        RetryingTransactionCallback<String> successUpdateCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("CONTENT FOR SUCCESS");
                // We expect the URL to be there, but the original URL must be removed
                urlsToMiss.addAll(urlsToExist);
                urlsToExist.clear();
                urlsToExist.add(writer.getContentUrl());
                // Done
                return writer.getContentUrl();
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(successUpdateCallback);
        // Make sure that the new content is there
        // The original content was disposed of
        checkForExistence(urlsToExist, true);
        checkForExistence(urlsToMiss, false);
        
        // Now get/set ContentData without a change
        RetryingTransactionCallback<ContentData> pointlessUpdateCallback = new RetryingTransactionCallback<ContentData>()
        {
            public ContentData execute() throws Throwable
            {
                ContentData contentData = (ContentData) nodeService.getProperty(contentNodeRef, ContentModel.PROP_CONTENT);
                nodeService.setProperty(contentNodeRef, ContentModel.PROP_CONTENT, contentData);
                // Done
                return contentData;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(pointlessUpdateCallback);
        // Expect no change from before
        checkForExistence(urlsToExist, true);
        checkForExistence(urlsToMiss, false);
        
        // Now delete the node
        RetryingTransactionCallback<Object> deleteNodeCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                nodeService.deleteNode(contentNodeRef);
                // All URLs must be cleaned up
                urlsToMiss.addAll(urlsToExist);
                urlsToExist.clear();
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteNodeCallback);
        // The new content must have disappeared
        checkForExistence(urlsToExist, true);
        checkForExistence(urlsToMiss, false);
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
               
                // Delete the first node, bypassing archive
                nodeService.addAspect(contentNodeRef, ContentModel.ASPECT_TEMPORARY, null);
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
    
    /**
     * Test forced and immediate shredding of content
     * <p/>
     * There is no validation that the file is affected.  It's an example of how to wire
     * different listeners together to do neat things to the files before deletion.
     */
    public void testForcedImmediateShredding() throws Exception
    {
        // An example of an eager cleaner that will wipe files before deleting their contents
        // This is very much like a listener, but listeners are only called by the standard,
        // scheduled cleaner.
        final Set<String> wipedUrls = new HashSet<String>(3);
        final EagerContentStoreCleaner wipingEagerCleaner = new EagerContentStoreCleaner()
        {
            final FileWipingContentCleanerListener fileWiper = new FileWipingContentCleanerListener();
            @Override
            protected boolean deleteFromStore(String contentUrl, ContentStore store)
            {
                fileWiper.beforeDelete(store, contentUrl);
                wipedUrls.add(contentUrl);
                return true;
            }
        };
        wipingEagerCleaner.setStores(Collections.singletonList(store));
        /*
         * Note that we don't need to wire the 'wipingEagerCleaner' into anything.
         * You can if you want it to wipe for all use cases.  In this case, we're just
         * going to manually force it to clean.
         */
        
        // Create a node with content
        final StoreRef storeRef = nodeService.createStore("test", getName() + "-" + GUID.generate());
        RetryingTransactionCallback<NodeRef> testCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
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
               
                // Done
                return contentNodeRef;
            }
        };
        final NodeRef contentNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
        
        // Now, force the node to be deleted and make sure it gets cleaned up directly
        // This can be used where some sensitive data has been identified and, before deletion,
        // the URL can be marked for immediate cleanup (in the post-commit phase, of course!)
        RetryingTransactionCallback<String> deleteCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                // Let's pretend we're in 'beforeDeleteNode'
                ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
                String contentUrl = reader.getContentUrl();
                wipingEagerCleaner.registerOrphanedContentUrl(contentUrl, true);
                
                nodeService.deleteNode(contentNodeRef);
                // Done
                return contentUrl;
            }
        };
        String contentUrl = transactionService.getRetryingTransactionHelper().doInTransaction(deleteCallback);
        
        // So, we don't fire the cleaner, but notice that the eager cleaner has 'wiped' the content
        assertTrue("Expected our URL to have been wiped.", wipedUrls.contains(contentUrl));
        cleaner.execute();
    }
    
    /**
     * Test basic wiping of file contents on normal orphan cleanup
     */
    public void testShreddingCleanup() throws Exception
    {
        eagerCleaner.setEagerOrphanCleanup(false);
        cleaner.setProtectDays(0);
        
        // Add in a the Wiping cleaner listener
        FileWipingContentCleanerListener fileWiper = new FileWipingContentCleanerListener();
        List<ContentStoreCleanerListener> listeners = new ArrayList<ContentStoreCleanerListener>(1);
        listeners.add(fileWiper);
        eagerCleaner.setListeners(listeners);
        
        // Create a node with content
        final StoreRef storeRef = nodeService.createStore("test", getName() + "-" + GUID.generate());
        RetryingTransactionCallback<NodeRef> testCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
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
               
                // Done
                return contentNodeRef;
            }
        };
        final NodeRef contentNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
        
        // Simple delete
        RetryingTransactionCallback<Void> deleteCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(contentNodeRef);
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteCallback);
        
        // It's orphaned now.  Fire the cleaner.
        cleaner.execute();
    }

    private class DummyCleanerListener implements ContentStoreCleanerListener
    {
        public void beforeDelete(ContentStore store, String contentUrl) throws ContentIOException
        {
            deletedUrls.add(contentUrl);
        }
    }
    /**
     * Cleaner listener that doesn't support the URLs passed in
     */
    private class DummyUnsupportiveCleanerListener implements ContentStoreCleanerListener
    {
        public void beforeDelete(ContentStore store, String contentUrl) throws ContentIOException
        {
            throw new UnsupportedContentUrlException(store, contentUrl);
        }
    }
}
