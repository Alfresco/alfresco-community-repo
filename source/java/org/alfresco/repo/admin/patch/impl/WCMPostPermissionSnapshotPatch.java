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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;

/**
 * Snap shot all stores after applying the staging are permissions patch
 * 
 * @author andyh
 */
public class WCMPostPermissionSnapshotPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.wcmPostPermissionSnapshotPatch.result";

    AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

    AVMService avmService;

    AclDaoComponent aclDaoComponent;

    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }

    public void setAclDaoComponent(AclDaoComponent aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        
        Thread progressThread = null;

        Long toDo = aclDaoComponent.getNewInStore();

        List<AVMLuceneIndexer> indexers = new ArrayList<AVMLuceneIndexer>(stores.size());
        for (AVMStoreDescriptor storeDesc : stores)
        {
            AVMLuceneIndexer indexer = avmSnapShotTriggeredIndexingMethodInterceptor.getIndexer(storeDesc.getName());
            indexers.add(indexer);
        }
        
        progressThread = new Thread(new ProgressWatcher(toDo, indexers), "WCMPactchProgressWatcher");
        progressThread.start();

        for (AVMStoreDescriptor storeDesc : stores)
        {
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
