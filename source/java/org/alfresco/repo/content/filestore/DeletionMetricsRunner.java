/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.content.filestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.cleanup.ContentStoreCleaner;
import org.alfresco.repo.content.cleanup.ContentStoreCleanerListener;
import org.alfresco.repo.content.cleanup.EagerContentStoreCleaner;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
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

public class DeletionMetricsRunner
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private ContentService contentService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private JobLockService jobLockService;
    private ContentStoreCleaner cleaner;
    private EagerContentStoreCleaner eagerCleaner;
    private FileContentStore store;
    private ContentStoreCleanerListener listener;
    private int deletedUrls;

    private final int numOrphans = 1000;
    
    
    
    public DeletionMetricsRunner()
    {
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
        store = (FileContentStore) ctx.getBean("fileContentStore");
        
        
        // and a listener
        List<ContentStoreCleanerListener> listeners = new ArrayList<ContentStoreCleanerListener>(2);
        listener = new CleanerListener();
        listeners.add(listener);
        
        // Construct the test cleaners
        eagerCleaner = (EagerContentStoreCleaner) ctx.getBean("eagerContentStoreCleaner");
        eagerCleaner.setEagerOrphanCleanup(false);
        eagerCleaner.setStores(Collections.singletonList((ContentStore) store));
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


    public static void main(String[] args)
    {
        DeletionMetricsRunner metrics = new DeletionMetricsRunner();
        metrics.run();
    }
    
    
    public void run()
    {
        setUp(true);
        time("Deleting empty parent dirs");
        tearDown();
        
        setUp(false);
        time("Ignoring empty parent dirs");
        tearDown();
    }
    
    
    
    private void setUp(boolean deleteEmptyDirs)
    {
        AuthenticationUtil.setRunAsUserSystem();
        store.setDeleteEmptyDirs(deleteEmptyDirs);
        deletedUrls = 0;
    }
    
    
    private void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();    
        System.out.println("Deleted " + deletedUrls + " URLs.");
    }
 
    
    
    private void time(String description)
    {
        long beforeClean = System.currentTimeMillis();
        
        createContent();
        cleanContent();
        
        long afterClean = System.currentTimeMillis();
        double timeTaken = afterClean - beforeClean;
        System.out.println();
        System.out.println(String.format("%s took %6.0fms", description, timeTaken));
    }

    
    private void createContent()
    {
        final StoreRef storeRef = nodeService.createStore("test", "timings-" + GUID.generate());
        RetryingTransactionCallback<ContentData> testCallback = new RetryingTransactionCallback<ContentData>()
        {
            public ContentData execute() throws Throwable
            {
                ContentData contentData = null;
                
                for (int i = 0; i < numOrphans; i++)
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
                    
                    contentData = writer.getContentData();
                   
                    // Delete the first node, bypassing archive
                    nodeService.addAspect(contentNodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    nodeService.deleteNode(contentNodeRef);
                }
                
                // Done
                return contentData;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
    }
    
    
    private void cleanContent()
    {
        // fire the cleaner
        cleaner.setProtectDays(0);
        cleaner.execute();
        
        if (deletedUrls < numOrphans)
            throw new IllegalStateException("Not all the orphans were cleaned.");
    }
    
    
    private class CleanerListener implements ContentStoreCleanerListener
    {
        public void beforeDelete(ContentStore store, String contentUrl) throws ContentIOException
        {
            deletedUrls++;
        }
    }
    
}