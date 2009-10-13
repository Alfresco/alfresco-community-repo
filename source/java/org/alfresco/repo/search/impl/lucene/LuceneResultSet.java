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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

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
            Document doc = hits.doc(n);
            if (!prefetch.get(n))
            {
                fetch(n);
            }
            return doc;
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading reading document from the result set", e);
        }
    }

    private void fetch(int n)
    {
        if (searchParameters.getBulkFetch() && (config.getBulkLoader() != null))
        {
            while (!prefetch.get(n))
            {
                fetch();
            }
        }
    }

    private void fetch()
    {
        try
        {
            if (searchParameters.getBulkFetch() && (config.getBulkLoader() != null))
            {
                for (int i = 0; (i < hits.length()); i += searchParameters.getBulkFecthSize())
                {
                    if (!prefetch.get(i))
                    {
                        ArrayList<NodeRef> fetchList = new ArrayList<NodeRef>(searchParameters.getBulkFecthSize());
                        for (int j = i; (j < i + searchParameters.getBulkFecthSize()) && (j < hits.length()); j++)
                        {
                            Document doc = hits.doc(j);
                            String id = doc.get("ID");
                            NodeRef nodeRef = tenantService.getBaseName(new NodeRef(id));
                            fetchList.add(nodeRef);
                        }
                        config.getBulkLoader().loadIntoCache(fetchList);
                        for (int j = i; j < i + searchParameters.getBulkFecthSize(); j++)
                        {
                            prefetch.set(j);
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading reading document from the result set", e);
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
    
}
