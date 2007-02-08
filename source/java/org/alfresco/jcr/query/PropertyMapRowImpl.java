/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.query;

import java.io.Serializable;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Row;

import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.ValueImpl;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Node Ref based Row
 * 
 * @author David Caruana
 */
public class PropertyMapRowImpl implements Row
{
    private SessionImpl session;
    private Map<QName, PropertyDefinition> columns;
    private NodeRef nodeRef;
    private Map<QName, Serializable> properties;

    
    /**
     * Construct
     * 
     * @param session
     * @param columnNames
     * @param properties
     */
    public PropertyMapRowImpl(SessionImpl session, Map<QName, PropertyDefinition> columns, NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        this.session = session;
        this.columns = columns;
        this.nodeRef = nodeRef;
        this.properties = properties;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.query.Row#getValues()
     */
    public Value[] getValues() throws RepositoryException
    {
        Value[] values = new Value[columns.size() + 2];
        
        int i = 0;
        for (QName propertyName : columns.keySet())
        {
            values[i++] = createValue(propertyName);
        }
        return values;
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.Row#getValue(java.lang.String)
     */
    public Value getValue(String propertyName) throws ItemNotFoundException, RepositoryException
    {
        QName propertyQName = QName.createQName(propertyName, session.getNamespaceResolver());
        if (!columns.containsKey(propertyQName))
        {
            throw new ItemNotFoundException("Column " + propertyName + " does not exist");
        }
        return createValue(propertyQName);
    }
    
    /**
     * Create a Value for specified property name
     * 
     * @param propertyName
     * @return
     * @throws RepositoryException
     */
    private Value createValue(QName propertyName)
        throws RepositoryException
    {
        Value value = null;
        if (propertyName.equals(QueryManagerImpl.JCRPATH_COLUMN))
        {
            // derive path from node ref
            Node node = new NodeImpl(session, nodeRef).getProxy();
            value = new ValueImpl(session, PropertyType.STRING, node.getPath());
        }
        else if (propertyName.equals(QueryManagerImpl.JCRSCORE_COLUMN))
        {
            // TODO:
            // create dummy score
            value = new ValueImpl(session, PropertyType.LONG, (long)0);
        }
        else
        {
            // create value from node properties
            Object objValue = properties.get(propertyName);
            if (objValue != null)
            {
                PropertyDefinition propDef = columns.get(propertyName);
                value = new ValueImpl(session, propDef.getRequiredType(), objValue);
            }        
        }
        return value;
    }
    
}
