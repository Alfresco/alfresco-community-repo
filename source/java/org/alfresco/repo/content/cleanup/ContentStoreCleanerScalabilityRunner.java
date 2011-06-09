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

import java.io.File;
import java.util.Collections;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.EmptyContentReader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.tools.Repository;
import org.alfresco.tools.ToolException;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.lang.mutable.MutableInt;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Loads the repository up with orphaned content and then runs the cleaner.
 * <p>
 * A null content store produces ficticious content URLs.  The DB is loaded with ficticious URLs.
 * The process is kicked off. 
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public class ContentStoreCleanerScalabilityRunner extends Repository
{
    private VmShutdownListener vmShutdownListener = new VmShutdownListener("ContentStoreCleanerScalabilityRunner");
    
    private ApplicationContext ctx;
    private NodeHelper nodeHelper;
    private TransactionService transactionService;
    private NodeDAO nodeDAO;
    private ContentStore contentStore;
    private ContentStoreCleaner cleaner;
    
    /**
     * Do the load and cleanup.
     */
    public static void main(String[] args)
    {
        new ContentStoreCleanerScalabilityRunner().start(args);
    }
    
    @Override
    protected synchronized int execute() throws ToolException
    {
        ctx = super.getApplicationContext();
        
        nodeHelper = new NodeHelper();
        
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        nodeDAO = (NodeDAO) ctx.getBean("nodeDAO");
        
        int orphanCount = 1000;
        
        contentStore = new NullContentStore(10000);
        
        loadData(orphanCount);
    
        System.out.println("Ready to clean store: " + contentStore);
        synchronized(this)
        {
            try { this.wait(10000L); } catch (InterruptedException e) {}
        }
        
        long beforeClean = System.currentTimeMillis();
        clean();
        long afterClean = System.currentTimeMillis();
        double aveClean = (double) (afterClean - beforeClean) / (double) orphanCount / 1000D;
        
        System.out.println();
        System.out.println(String.format("Cleaning took %3f per 1000 content URLs in DB", aveClean));
        
        return 0;
    }
    
    private void loadData(final int maxCount)
    {
        final MutableInt doneCount = new MutableInt(0);
        // Batches of 1000 objects
        RetryingTransactionCallback<Integer> makeNodesCallback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                for (int i = 0; i < 1000; i++)
                {
                    // We don't need to write anything
                    String contentUrl = FileContentStore.createNewFileStoreUrl();
                    ContentData contentData = new ContentData(contentUrl, MimetypeMap.MIMETYPE_TEXT_PLAIN, 10, "UTF-8");
                    nodeHelper.makeNode(contentData);
                    
                    int count = doneCount.intValue();
                    count++;
                    doneCount.setValue(count);
                    
                    // Do some reporting
                    if (count % 1000 == 0)
                    {
                        System.out.println(String.format("   " + (new Date()) + "Total created: %6d", count));
                    }
                    
                    // Double check for shutdown
                    if (vmShutdownListener.isVmShuttingDown())
                    {
                        break;
                    }
                }
                return maxCount;
            }
        };
        int repetitions = (int) Math.floor((double)maxCount / 1000.0);
        for (int i = 0; i < repetitions; i++)
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(makeNodesCallback);
        }
    }
    
    private void clean()
    {
        ContentStoreCleanerListener listener = new ContentStoreCleanerListener()
        {
            private int count = 0;
            public void beforeDelete(ContentStore store, String contentUrl) throws ContentIOException
            {
                count++;
                if (count % 1000 == 0)
                {
                    System.out.println(String.format("   Total deleted: %6d", count));
                }
            }
        };
        // We use the default cleaners, but fix them up a bit
        EagerContentStoreCleaner eagerCleaner = (EagerContentStoreCleaner) ctx.getBean("eagerContentStoreCleaner");
        eagerCleaner.setListeners(Collections.singletonList(listener));
        eagerCleaner.setStores(Collections.singletonList(contentStore));
        cleaner = (ContentStoreCleaner) ctx.getBean("contentStoreCleaner");
        cleaner.setProtectDays(0);
        
        // The cleaner has its own txns
        cleaner.execute();
    }
    
    private class NullContentStore extends AbstractContentStore
    {
        private ThreadLocal<File> hammeredFile;
        private int count;
        private int deletedCount;
        
        private NullContentStore(int count)
        {
            hammeredFile = new ThreadLocal<File>();
            this.count = count;
        }
        
        public boolean isWriteSupported()
        {
            return true;
        }

        /**
         * Returns a writer to a thread-unique file.  It's always the same file per thread so you must
         * use and close the writer before getting another.
         */
        @Override
        protected ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
        {
            File file = hammeredFile.get();
            if (file == null)
            {
                file = TempFileProvider.createTempFile("NullContentStore", ".txt");
                hammeredFile.set(file);
            }
            return new FileContentWriter(file);
        }

        @Override
        public void getUrls(Date createdAfter, Date createdBefore, ContentUrlHandler handler) throws ContentIOException
        {
            // Make up it up
            for (int i = 0; i < count; i++)
            {
                String contentUrl = FileContentStore.createNewFileStoreUrl() + "-imaginary";
                handler.handle(contentUrl);
            }
        }

        public ContentReader getReader(String contentUrl)
        {
            File file = hammeredFile.get();
            if (file == null)
            {
                return new EmptyContentReader(contentUrl);
            }
            else
            {
                return new FileContentReader(file);
            }
        }

        @Override
        public boolean delete(String contentUrl)
        {
            deletedCount++;
            if (deletedCount % 1000 == 0)
            {
                System.out.println(String.format("   Deleted %6d files", deletedCount));
            }
            return true;
        }
    }
    
    private class NodeHelper
    {
        private QName contentQName;
        
        public NodeHelper()
        {
            contentQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "realContent");
        }
        /**
         * Creates a node with two properties
         */
        public void makeNode(ContentData contentData)
        {
            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
            Long rootNodeId = nodeDAO.newStore(storeRef).getFirst();
            ChildAssocEntity assoc = nodeDAO.newNode(
                    rootNodeId,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    storeRef,
                    null,
                    ContentModel.TYPE_CONTENT,
                    I18NUtil.getLocale(),
                    null,
                    null);
            Long nodeId = assoc.getChildNode().getId();
            nodeDAO.addNodeProperty(nodeId, contentQName, contentData);
        }
    }
}
