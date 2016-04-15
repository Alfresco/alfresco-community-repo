/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.admin;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.RegexQNamePattern;
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
    
    /**
     * Set the index recovert mode
     * @param indexRecoveryMode RecoveryMode
     */
    public void setIndexRecoveryMode(RecoveryMode indexRecoveryMode)
    {
        this.indexRecoveryMode = indexRecoveryMode;
    }



    /** 
     * Set the node service
     * @param nodeService NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    /**
     * Set the search service
     * @param searchService SearchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    @Override
    public List<StoreRef> checkIndexConfiguration()
    {
        // get all root nodes from the NodeService, i.e. database
        List<StoreRef> storeRefs = nodeService.getStores();
        List<StoreRef> missingIndexStoreRefs = new ArrayList<StoreRef>(0);
        for (StoreRef storeRef : storeRefs)
        {
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
            
            // Are we creating the store - in which case we do not check
            // See MNT-11612
            int countChildAssoc = 0;
            if (storeRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                // AVM does not support nodeService.countChildAssocs()
                long start = 0;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Counting childAssocs for store: " + storeRef);
                    start = System.currentTimeMillis();
                }
                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(rootNodeRef,
                        RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, 1, false);
                countChildAssoc = childAssocs.size();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Time for counting childAssocs for : " + storeRef + " time="
                            + (System.currentTimeMillis() - start));
                }
            }
            else
            {
                long start = 0;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Counting childAssocs for store: " + storeRef);
                    start = System.currentTimeMillis();
                }
                countChildAssoc = nodeService.countChildAssocs(rootNodeRef, true);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Time for counting childAssocs for : " + storeRef + " time="
                            + (System.currentTimeMillis() - start));
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Counting childAssocs for store: " + storeRef + " countChildAssoc = " + countChildAssoc);
            }
            if (countChildAssoc == 0)
            {
                continue;
            }
            
            if (indexRecoveryMode != RecoveryMode.FULL)
            {
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
