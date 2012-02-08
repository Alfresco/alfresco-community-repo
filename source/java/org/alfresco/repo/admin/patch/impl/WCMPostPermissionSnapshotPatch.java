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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Snap shot all stores after applying the staging are permissions patch
 * 
 * @author andyh
 */
public class WCMPostPermissionSnapshotPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.wcmPostPermissionSnapshotPatch.result";

    private static final String AVM_SITE_STORE_NAME = "sitestore";
    
    private AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;
    
    private AVMService avmService;
    
    private PatchDAO patchDAO;
    
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        
        Thread progressThread = null;

        Long toDo = patchDAO.getAVMNodesCountWhereNewInStore();

        List<AVMLuceneIndexer> indexers = new ArrayList<AVMLuceneIndexer>(stores.size());
        for (AVMStoreDescriptor storeDesc : stores)
        {
            // post 4.0 we can safely skip "sitestore" no longer used by share
            if(storeDesc.getName().equals(AVM_SITE_STORE_NAME))
            {
                continue;
            }
            
            AVMLuceneIndexer indexer = avmSnapShotTriggeredIndexingMethodInterceptor.getIndexer(storeDesc.getName());
            indexers.add(indexer);
        }
        
        progressThread = new Thread(new ProgressWatcher(toDo, indexers), "WCMPactchProgressWatcher");
        progressThread.start();

        for (AVMStoreDescriptor storeDesc : stores)
        {
            // post 4.0 we can safely skip "sitestore" no longer used by share
            if(storeDesc.getName().equals(AVM_SITE_STORE_NAME))
            {
                continue;
            }
            
            if (avmService.getStoreRoot(-1, storeDesc.getName()).getLayerID() == -1)
            {
                avmService.createSnapshot(storeDesc.getName(), "PermissionPatch", "Snapshot after 2.2 permission patch");
                AVMLuceneIndexer indexer = avmSnapShotTriggeredIndexingMethodInterceptor.getIndexer(storeDesc.getName());
                indexer.flushPending();
            }
        }

        progressThread.interrupt();
        progressThread.join();

        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        // done
        return msg;
    }

    private class ProgressWatcher implements Runnable
    {
        private boolean running = true;

        Long toDo;

        List<AVMLuceneIndexer> indexers;
        
        ProgressWatcher(Long toDo, List<AVMLuceneIndexer>  indexers)
        {
            this.toDo = toDo;
            this.indexers = indexers;
        }

        public void run()
        {
            while (running)
            {
                try
                {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e)
                {
                    running = false;
                }

                if (running)
                {
                    long done = 0;
                    for(AVMLuceneIndexer indexer : indexers)
                    {
                        if(indexer != null)
                        {
                            done += indexer.getIndexedDocCount(); 
                        }
                    }

                    reportProgress(toDo, done);
                }
            }
        }

    }
}
