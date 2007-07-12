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
package org.alfresco.repo.node.index;

import java.util.List;

import org.alfresco.repo.search.impl.lucene.AbstractLuceneIndexerImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This component attempts to reindex 
 * 
 * @author Derek Hulley
 */
public class MissingContentReindexComponent extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(MissingContentReindexComponent.class);
    
    /** keep track of whether the FTS indexer thread has been poked */
    private boolean ftsIndexerCalled;
    
    public MissingContentReindexComponent()
    {
        ftsIndexerCalled = false;
    }

    /**
     * If this object is currently busy, then it just nothing
     */
    @Override
    public void reindexImpl()
    {
        List<StoreRef> storeRefs = nodeService.getStores();
        int count = 0;
        for (StoreRef storeRef : storeRefs)
        {
            // prompt the FTS reindexing
            if (!ftsIndexerCalled)
            {
                ftsIndexer.requiresIndex(storeRef);
            }
            // reindex missing content
            count += reindexMissingContent(storeRef);
            // check if we have to break out
            if (isShuttingDown())
            {
                break;
            }
        }
        
        // The FTS indexer only needs to be prompted once
        ftsIndexerCalled = true;

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Missing content indexing touched " + count + " content nodes");
        }
    }
    
    /**
     * @param storeRef the store to check for missing content
     * @return Returns the number of documents reindexed
     */
    private int reindexMissingContent(StoreRef storeRef)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(storeRef);

        // search for it in the index, sorting with youngest first
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TEXT:" + AbstractLuceneIndexerImpl.NOT_INDEXED_CONTENT_MISSING);
        sp.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_DESCENDING);
        ResultSet results = null;
        try
        {
            results = searcher.query(sp);
            
            int count = 0;
            // iterate over the nodes and prompt for reindexing
            for (ResultSetRow row : results)
            {
                final NodeRef childNodeRef = row.getNodeRef();
                // prompt for a reindex - it might fail again, but we just keep plugging away
                RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>()
                {
                    public Object execute()
                    {
                        indexer.updateNode(childNodeRef);
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork);
                // check if we have to break out
                if (isShuttingDown())
                {
                    break;
                }
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Reindexed missing content: \n" +
                        "   store: " + storeRef + "\n" +
                        "   node count: " + count);
            }
            return count;
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
    }
}