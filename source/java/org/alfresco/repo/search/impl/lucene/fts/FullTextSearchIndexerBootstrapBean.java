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
package org.alfresco.repo.search.impl.lucene.fts;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

public class FullTextSearchIndexerBootstrapBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(FullTextSearchIndexerBootstrapBean.class);

    private FullTextSearchIndexer fullTextSearchIndexer;

    private NodeService nodeService;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        List<StoreRef> storeRefs = nodeService.getStores();
        for (StoreRef storeRef : storeRefs)
        {
            if (storeRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                // nothing to do
            }
            else
            {
                fullTextSearchIndexer.requiresIndex(storeRef);
            }
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

    public FullTextSearchIndexer getFullTextSearchIndexer()
    {
        return fullTextSearchIndexer;
    }

    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer)
    {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    
    
}
