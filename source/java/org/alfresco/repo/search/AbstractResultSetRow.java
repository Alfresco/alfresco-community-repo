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
package org.alfresco.repo.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

/**
 * Common support for a row in a result set
 * 
 * @author andyh
 */
public abstract class AbstractResultSetRow implements ResultSetRow
{

    /**
     * The containing result set
     */
    private ResultSet resultSet;

    /**
     * The current position in the containing result set
     */
    private int index;

    /**
     * The direct properties of the current node Used by those implementations that can cache the whole set.
     */

    protected Map<String, Serializable> properties;

    /**
     * The row needs the result set and the index for lookup.
     * 
     * @param resultSet
     * @param index
     */
    public AbstractResultSetRow(ResultSet resultSet, int index)
    {
        super();
        this.resultSet = resultSet;
        this.index = index;
    }

    public ResultSet getResultSet()
    {
        return resultSet;
    }

    public int getIndex()
    {
        return index;
    }

    public NodeRef getNodeRef()
    {
        return getResultSet().getNodeRef(getIndex());
    }

    public QName getQName()
    {
        return getResultSet().getChildAssocRef(getIndex()).getQName();
    }

    public ChildAssociationRef getChildAssocRef()
    {
        return getResultSet().getChildAssocRef(getIndex());
    }

    public float getScore()
    {
        return getResultSet().getScore(getIndex());
    }

    public Map<String, Serializable> getValues()
    {
        if (properties == null)
        {
            properties = new HashMap<String, Serializable>();
            setProperties(getDirectProperties());
        }
        return Collections.unmodifiableMap(properties);
    }

    public Serializable getValue(String columnName)
    {
        return properties.get(columnName);
    }

    protected Map<QName, Serializable> getDirectProperties()
    {
        return Collections.<QName, Serializable> emptyMap();
    }

    protected void setProperties(Map<QName, Serializable> byQname)
    {
        for (QName qname : byQname.keySet())
        {
            Serializable value = byQname.get(qname);
            properties.put(qname.toString(), value);
        }
    }

    public Serializable getValue(QName qname)
    {
        return getValues().get(qname.toString());
    }

}
