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
package org.alfresco.repo.search.results;

import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.ResultSetSPI;

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
