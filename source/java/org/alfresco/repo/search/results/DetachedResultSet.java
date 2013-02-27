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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Detached result set
 * @author andyh
 *
 */
public class DetachedResultSet extends AbstractResultSet
{
    List<ResultSetRow> rows = null;
    
    ResultSetMetaData rsmd;
    
    long numberFound;
    
    /**
     * Detached result set based on that provided
     * @param resultSet
     */
    public DetachedResultSet(ResultSet resultSet)
    {
        super();
        rsmd = resultSet.getResultSetMetaData();
        rows = new ArrayList<ResultSetRow>(resultSet.length());
        for (ResultSetRow row : resultSet)
        {
            rows.add(new DetachedResultSetRow(this, row));
        }
        numberFound = resultSet.getNumberFound();
    }

    public int length()
    {
        return rows.size();
    }

    public NodeRef getNodeRef(int n)
    {
        return rows.get(n).getNodeRef();
    }

    public ResultSetRow getRow(int i)
    {
        return rows.get(i);
    }

    public Iterator<ResultSetRow> iterator()
    {
       return rows.iterator();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        return rows.get(n).getChildAssocRef();
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(rsmd.getLimitedBy(), PermissionEvaluationMode.EAGER, rsmd.getSearchParameters());
    }

    public int getStart()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasMore()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
       return numberFound;
    }

}
