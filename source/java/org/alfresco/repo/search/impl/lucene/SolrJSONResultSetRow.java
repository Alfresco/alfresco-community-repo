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
import java.util.Map;

import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 */
public class SolrJSONResultSetRow extends AbstractResultSetRow
{

    /**
     * @param resultSet
     * @param index
     */
    public SolrJSONResultSetRow(ResultSet resultSet, int index)
    {
        super(resultSet, index);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRef(java.lang.String)
     */
    @Override
    public NodeRef getNodeRef(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRefs()
     */
    @Override
    public Map<String, NodeRef> getNodeRefs()
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScore(java.lang.String)
     */
    @Override
    public float getScore(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScores()
     */
    @Override
    public Map<String, Float> getScores()
    {
        throw new UnsupportedOperationException();
    }
    
    protected Map<QName, Serializable> getDirectProperties()
    {
        SolrJSONResultSet rs = (SolrJSONResultSet) getResultSet();
        return rs.getNodeService().getProperties(rs.getNodeRef(getIndex()));
    }

}
