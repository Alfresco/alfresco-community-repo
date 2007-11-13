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

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Base patch for reindexing nodes in a store based on a lucene query 
 * 
 * @author Kevin Roast
 * @author gavinc
 */
public abstract class BaseReindexingPatch extends AbstractPatch
{
    protected IndexerAndSearcher indexerAndSearcher;

    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }
    
    /**
     * Reindex a collection of nodes in a store based on a Lucene query
     * 
     * @param query     The Lucene query to execute to return nodes
     * @param store     The Store containing the nodes to reindex
     */
    protected void reindex(String query, StoreRef store)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(store);
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            for(ResultSetRow row : rs)
            {
                Indexer indexer = indexerAndSearcher.getIndexer(row.getNodeRef().getStoreRef());
                indexer.updateNode(row.getNodeRef());
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
    }
}
