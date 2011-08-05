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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;

/**
 * @author andyh
 */
public class PagingLuceneResultSet implements ResultSet, Serializable
{
    ResultSet wrapped;

    SearchParameters searchParameters;
    
    NodeService nodeService;
    
    public PagingLuceneResultSet(ResultSet wrapped, SearchParameters searchParameters, NodeService nodeService)
    {
        this.wrapped = wrapped;
        this.searchParameters = searchParameters;
        this.nodeService = nodeService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSet#close()
     */
    public void close()
    {
        wrapped.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSet#getRow(int)
     */
    public ResultSetRow getRow(int i)
    {
        return wrapped.getRow(getStart() + i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSet#hasMore()
     */
    public boolean hasMore()
    {

        if (wrapped.getResultSetMetaData().getLimitedBy() != LimitBy.UNLIMITED)
        {
            return true;
        }
        else
        {
            if (wrapped.length() - getStart() > getLength())
            {
                return true;
            }
            else
            {
                return false;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSet#length()
     */
    public int getLength()
    {

        int max = searchParameters.getMaxItems();
        int skip = searchParameters.getSkipCount();
        if ((max >= 0) && (max < (wrapped.length() - skip)))
        {
            return searchParameters.getMaxItems();
        }
        else
        {
            return wrapped.length() - skip;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSet#start()
     */
    public int getStart()
    {
        return searchParameters.getSkipCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<ResultSetRow> iterator()
    {
        return new PagingLuceneResultSetRowIteratorImpl(this);
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        NodeRef nodeRef = getNodeRef(n);
        return nodeService.getPrimaryParent(nodeRef);
    }

    public List<ChildAssociationRef> getChildAssocRefs()
    {
        ArrayList<ChildAssociationRef> cars = new ArrayList<ChildAssociationRef>(length());
        for (ResultSetRow row : this)
        {
            cars.add(row.getChildAssocRef());
        }
        return cars;
    }

    public NodeRef getNodeRef(int n)
    {
        return wrapped.getNodeRef(getStart() + n);
    }

    public List<NodeRef> getNodeRefs()
    {
        ArrayList<NodeRef> nodeRefs = new ArrayList<NodeRef>(length());
        for (ResultSetRow row : this)
        {
            nodeRefs.add(row.getNodeRef());
        }
        return nodeRefs;
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return wrapped.getResultSetMetaData();
    }

    public float getScore(int n)
    {
        return wrapped.getScore(getStart() + n);
    }

    public int length()
    {
        return getLength();
    }

    /**
     * Get the underlying result Set
     * @return the underlying result set
     */
    public ResultSet getWrapped()
    {
        return wrapped;
    }

    /**
     * Bulk fetch results in the cache
     * 
     * @param bulkFetch
     */
    public boolean setBulkFetch(boolean bulkFetch)
    {
    	return wrapped.setBulkFetch(bulkFetch);
    }

    /**
     * Do we bulk fetch
     * 
     * @return - true if we do
     */
    public boolean getBulkFetch()
    {
        return wrapped.getBulkFetch();
    }

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize
     */
    public int setBulkFetchSize(int bulkFetchSize)
    {
    	return wrapped.setBulkFetchSize(bulkFetchSize);
    }

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    public int getBulkFetchSize()
    {
        return wrapped.getBulkFetchSize();
    }
    
    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        return wrapped.getFieldFacet(field);
    }
}
