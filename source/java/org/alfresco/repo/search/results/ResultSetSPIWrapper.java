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
package org.alfresco.repo.search.results;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.ResultSetSPI;
import org.alfresco.util.Pair;

/**
 * Wrap an SPI result set with the basic interface 
 * 
 * @author andyh
 *
 * @param <ROW>
 * @param <MD>
 */
public class ResultSetSPIWrapper<ROW extends ResultSetRow, MD extends ResultSetMetaData> implements ResultSet
{
    private ResultSetSPI<ROW, MD> wrapped;

    /**
     * Create a wrapped result set
     * @param wrapped
     */
    public ResultSetSPIWrapper(ResultSetSPI<ROW, MD> wrapped)
    {
        this.wrapped = wrapped;
    }

    public void close()
    {
        wrapped.close();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        return wrapped.getChildAssocRef(n);
    }

    public List<ChildAssociationRef> getChildAssocRefs()
    {
        return wrapped.getChildAssocRefs();
    }

    public NodeRef getNodeRef(int n)
    {
        return wrapped.getNodeRef(n);
    }

    public List<NodeRef> getNodeRefs()
    {
        return wrapped.getNodeRefs();
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return wrapped.getResultSetMetaData();
    }

    public ResultSetRow getRow(int i)
    {
        return wrapped.getRow(i);
    }

    public float getScore(int n)
    {
        return wrapped.getScore(n);
    }

    public int getStart()
    {
        return wrapped.getStart();
    }

    public boolean hasMore()
    {
        return wrapped.hasMore();
    }

    public int length()
    {
        return wrapped.length();
    }
    
    public Iterator<ResultSetRow> iterator()
    {
        return new WrappedIterator<ROW>(wrapped.iterator());
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
    
    private static class WrappedIterator<ROW extends ResultSetRow> implements Iterator<ResultSetRow>
    {
        private Iterator<ROW> wrapped;

        WrappedIterator(Iterator<ROW> wrapped)
        {
            this.wrapped = wrapped;
        }

        public boolean hasNext()
        {
            return wrapped.hasNext();
        }

        public ResultSetRow next()
        {
            return wrapped.next();
        }

        public void remove()
        {
            wrapped.remove();
        }

    }
}
