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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeBulkLoader;
import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.ResultSetRowIterator;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.lucene.index.CachingIndexReader;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Searcher;

/**
 * Implementation of a ResultSet on top of Lucene Hits class.
 * 
 * @author andyh
 */
public class LuceneResultSet extends AbstractResultSet
{
    private static int DEFAULT_BULK_FETCH_SIZE = 1000;
    
    /**
     * The underlying hits
     */
    Hits hits;

    private Searcher searcher;

    private NodeService nodeService;

    private TenantService tenantService;

    private SearchParameters searchParameters;

    private LuceneConfig config;

    private BitSet prefetch;

    private boolean bulkFetch = true;

    private int bulkFetchSize = DEFAULT_BULK_FETCH_SIZE;
    
    /**
     * Wrap a lucene seach result with node support
     * 
     * @param storeRef
     * @param hits
     */
    public LuceneResultSet(Hits hits, Searcher searcher, NodeService nodeService, TenantService tenantService, SearchParameters searchParameters,
            LuceneConfig config)
    {
        super();
        this.hits = hits;
        this.searcher = searcher;
        this.nodeService = nodeService;
        this.tenantService = tenantService;
        this.searchParameters = searchParameters;
        this.config = config;
        prefetch = new BitSet(hits.length());
    }

    /*
     * ResultSet implementation
     */

    public ResultSetRowIterator iterator()
    {
        return new LuceneResultSetRowIterator(this);
    }

    public int length()
    {
        return hits.length();
    }

    public NodeRef getNodeRef(int n)
    {
        try
        {
            prefetch(n);
            // We have to get the document to resolve this
            // It is possible the store ref is also stored in the index
            if (searcher instanceof ClosingIndexSearcher)
            {
                ClosingIndexSearcher cis = (ClosingIndexSearcher) searcher;
                IndexReader reader = cis.getReader();
                if (reader instanceof CachingIndexReader)
                {
                    int id = hits.id(n);
                    CachingIndexReader cir = (CachingIndexReader) reader;
                    String sid = cir.getId(id);
                    return tenantService.getBaseName(new NodeRef(sid));
                }
            }

            Document doc = hits.doc(n);
            String id = doc.get("ID");
            return tenantService.getBaseName(new NodeRef(id));
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading reading node ref from the result set", e);
        }
    }

    public float getScore(int n) throws SearcherException
    {
        try
        {
            return hits.score(n);
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading score from the result set", e);
        }
    }

    public Document getDocument(int n)
    {
        try
        {
            prefetch(n);
            Document doc = hits.doc(n);
            return doc;
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading reading document from the result set", e);
        }
    }

    private void prefetch(int n) throws IOException
    {
        NodeBulkLoader bulkLoader = config.getBulkLoader();
        if (!getBulkFetch() || (bulkLoader == null))
        {
            // No prefetching
            return;
        }
        if (prefetch.get(n))
        {
            // The document was already processed
            return;
        }
        // Start at 'n' and process the the next bulk set
        int bulkFetchSize = getBulkFetchSize();
        List<NodeRef> fetchList = new ArrayList<NodeRef>(bulkFetchSize);
        int totalHits = hits.length();
        for (int i = 0; i < bulkFetchSize; i++)
        {
            int next = n + i;
            if (next >= totalHits)
            {
                // We've hit the end
                break;
            }
            if (prefetch.get(next))
            {
                // This one is in there already
                continue;
            }
            // We store the node and mark it as prefetched
            prefetch.set(next);
            Document doc = hits.doc(next);
            String nodeRefStr = doc.get("ID");
            try
            {
                NodeRef nodeRef = tenantService.getBaseName(new NodeRef(nodeRefStr));
                fetchList.add(nodeRef);
            }
            catch (AlfrescoRuntimeException e)
            {
                // Ignore IDs that don't parse as NodeRefs, e.g. FTSREF docs
            }
        }
        // Now bulk fetch
        if (fetchList.size() > 1)
        {
            bulkLoader.cacheNodes(fetchList);
        }
    }

    public void close()
    {
        try
        {
            searcher.close();
        }
        catch (IOException e)
        {
            throw new SearcherException(e);
        }
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public ResultSetRow getRow(int i)
    {
        if (i < length())
        {
            return new LuceneResultSetRow(this, i);
        }
        else
        {
            throw new SearcherException("Invalid row");
        }
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        return tenantService.getBaseName(getRow(n).getChildAssocRef());
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, searchParameters);
    }

    public int getStart()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasMore()
    {
        throw new UnsupportedOperationException();
    }
    
    public TenantService getTenantService()
    {
        return tenantService;
    }
    
    /**
     * Bulk fetch results in the cache
     * 
     * @param bulkFetch
     */
    @Override
    public boolean setBulkFetch(boolean bulkFetch)
    {
    	boolean oldBulkFetch = this.bulkFetch;
        this.bulkFetch = bulkFetch;
        return oldBulkFetch;
    }

    /**
     * Do we bulk fetch
     * 
     * @return - true if we do
     */
    @Override
    public boolean getBulkFetch()
    {
        return bulkFetch;
    }

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize
     */
    @Override
    public int setBulkFetchSize(int bulkFetchSize)
    {
    	int oldBulkFetchSize = this.bulkFetchSize;
        this.bulkFetchSize = bulkFetchSize;
        return oldBulkFetchSize;
    }

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    @Override
    public int getBulkFetchSize()
    {
        return bulkFetchSize;
    }
    
}
