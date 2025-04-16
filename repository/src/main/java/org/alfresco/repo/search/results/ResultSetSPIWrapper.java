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
package org.alfresco.repo.search.results;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.ResultSetSPI;
import org.alfresco.service.cmr.search.SpellCheckResult;
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
     *            boolean
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
     *            int
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

    @Override
    public Map<String, Integer> getFacetQueries()
    {
        return wrapped.getFacetQueries();
    }

    @Override
    public SpellCheckResult getSpellCheckResult()
    {
        return wrapped.getSpellCheckResult();
    }

    @Override
    public Map<NodeRef, List<Pair<String, List<String>>>> getHighlighting()
    {
        return wrapped.getHighlighting();
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

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound() */
    @Override
    public long getNumberFound()
    {
        return wrapped.getNumberFound();
    }
}
