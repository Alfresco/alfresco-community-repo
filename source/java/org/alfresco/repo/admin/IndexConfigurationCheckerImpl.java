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
package org.alfresco.repo.admin;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Andy
 *
 */
public class IndexConfigurationCheckerImpl implements IndexConfigurationChecker
{
    private static Log logger = LogFactory.getLog(IndexConfigurationCheckerImpl.class);

    private static final String ERR_DUPLICATE_ROOT_NODE = "system.config_check.err.indexes.duplicate_root_node";
    
    private RecoveryMode indexRecoveryMode;
    private NodeService nodeService;
    private SearchService searchService;
    private AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;
    
    /**
     * Set the index recovert mode
     * @param indexRecoveryMode
     */
    public void setIndexRecoveryMode(RecoveryMode indexRecoveryMode)
    {
        this.indexRecoveryMode = indexRecoveryMode;
    }



    /** 
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    /**
     * Set the search service
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * avm trigger 
     * @param avmSnapShotTriggeredIndexingMethodInterceptor
     */
    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }



    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.IndexConfigurationChecker#checkIndexConfiguration()
     */
    @Override
    public List<StoreRef> checkIndexConfiguration()
    {
        // get all root nodes from the NodeService, i.e. database
        List<StoreRef> storeRefs = nodeService.getStores();
        List<StoreRef> missingIndexStoreRefs = new ArrayList<StoreRef>(0);
        for (StoreRef storeRef : storeRefs)
        {
            @SuppressWarnings("unused")
            NodeRef rootNodeRef = null;
            try
            {
                rootNodeRef = nodeService.getRootNode(storeRef);
            }
            catch (InvalidStoreRefException e)
            {
                // the store is invalid and will therefore not have a root node entry
                continue;
            }
            if (indexRecoveryMode != RecoveryMode.FULL)
            {
                if (storeRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
                {
                    if (avmSnapShotTriggeredIndexingMethodInterceptor.isIndexingEnabled())
                    {
                        IndexMode storeIndexMode = avmSnapShotTriggeredIndexingMethodInterceptor.getIndexMode(storeRef.getIdentifier());
                        if (storeIndexMode.equals(IndexMode.UNINDEXED))
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Skipping index check for store: " + storeRef + " (unindexed AVM store)");
                            }
                            continue;
                        }
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Skipping index check for store: " + storeRef + " (AVM indexing is disabled)");
                        }
                        continue;
                    }
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Checking index for store: " + storeRef);
                }
                
                // perform a Lucene query for the root node
                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery("ISROOT:T");
                
                ResultSet results = null;
                int size = 0;
                try
                {
                    results = searchService.query(sp);
                    size = results.length();
                }
                finally
                {
                    try { results.close(); } catch (Throwable e) {}
                }
                
                if (size == 0)
                {
                    // indexes missing for root node
                    missingIndexStoreRefs.add(storeRef);
                    // debug
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Index missing for store: \n" +
                                "   store: " + storeRef);
                    }
                }
                else if (size > 1)
                {
                    // there are duplicates
                    String msg = I18NUtil.getMessage(ERR_DUPLICATE_ROOT_NODE, storeRef);
                    throw new AlfrescoRuntimeException(msg);
                }
            }
        }
        return missingIndexStoreRefs;
    }

}
