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
package org.alfresco.repo.avm;

import org.alfresco.repo.model.filefolder.FileFolderPerformanceTester;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @see FileFolderPerformanceTester
 */
public class AVMFileFolderPerformanceTester extends FileFolderPerformanceTester
{
    @Override
    protected NodeService getNodeService()
    {
        // note: vanilla AVM stores are defined as indexed by default, hence 
        // use "avmLockingAwareNodeService" for now (which, unlike "avmNodeService", is also indexing aware)
        // so that index is created when store is created (else config check will fail on restart)
        return (NodeService)ctx.getBean("avmLockingAwareNodeService"); 
    }
    
    protected NodeRef getOrCreateRootFolder()
    {
        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                String storeName = "AVMFileFolderPerformanceTester-"+System.currentTimeMillis();
                StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_AVM, storeName);
                return nodeService.getRootNode(storeRef);
            }
        };
        return retryingTransactionHelper.doInTransaction(callback);
    }
}
