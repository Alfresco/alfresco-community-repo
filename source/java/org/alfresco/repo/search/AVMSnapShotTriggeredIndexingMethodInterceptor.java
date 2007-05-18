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
package org.alfresco.repo.search;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Method interceptor for atomic indexing of AVM entries
 * 
 * @author andyh
 */
public class AVMSnapShotTriggeredIndexingMethodInterceptor implements MethodInterceptor
{
    private AVMService avmService;

    private IndexerAndSearcher indexerAndSearcher;

    private boolean enableIndexing = true;

    public Object invoke(MethodInvocation mi) throws Throwable
    {
        if (enableIndexing)
        {
            if (mi.getMethod().getName().equals("createSnapshot"))
            {
                String store = (String) mi.getArguments()[0];
                int before = avmService.getLatestSnapshotID(store);
                Object returnValue = mi.proceed();
                int after = avmService.getLatestSnapshotID(store);
                StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
                Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.index(store, before, after);
                }
                return returnValue;
            }
            // TODO: Purge store
            else if (mi.getMethod().getName().equals("purgeStore"))
            {
                String store = (String) mi.getArguments()[0];
                Object returnValue = mi.proceed();
                StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
                Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.deleteIndex(store);
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("createStore"))
            {
                String store = (String) mi.getArguments()[0];
                Object returnValue = mi.proceed();
                StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
                Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.createIndex(store);
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("renameStore"))
            {
                String from = (String) mi.getArguments()[0];
                String to = (String) mi.getArguments()[1];
                Object returnValue = mi.proceed();
                int after = avmService.getLatestSnapshotID(to);

                StoreRef fromRef = AVMNodeConverter.ToStoreRef(from);
                StoreRef toRef = AVMNodeConverter.ToStoreRef(to);

                Indexer indexer = indexerAndSearcher.getIndexer(fromRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.deleteIndex(from);
                }

                indexer = indexerAndSearcher.getIndexer(toRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.createIndex(to);
                    avmIndexer.index(to, 0, after);
                }

                return returnValue;
            }
            else
            {
                return mi.proceed();
            }
        }
        else
        {
            return mi.proceed();
        }
    }

    /**
     * Set the AVM service
     * 
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * Set the AVM indexer and searcher
     * 
     * @param indexerAndSearcher
     */
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    /**
     * Enable or disable indexing
     * 
     * @param enableIndexing
     */
    public void setEnableIndexing(boolean enableIndexing)
    {
        this.enableIndexing = enableIndexing;
    }
    
    

}
