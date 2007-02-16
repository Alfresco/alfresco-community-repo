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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.alfresco.jcr.item.NodeRefNodeIteratorImpl;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;


/**
 * Query Result based a NodeRef List
 * 
 * @author David Caruana
 */
public class NodeRefListQueryResultImpl implements QueryResult
{
    /** Session */
    private SessionImpl session;

    /** The node refs in the result set */
    private List<NodeRef> nodeRefs;
    
    /** Node Service */
    private NodeService nodeService;
    
    /** Column Names */
    private Map<QName, PropertyDefinition> columns = null; 
    
    /** Proxy */
    private QueryResult proxy = null;
    
    
    /**
     * Construct
     * 
     * @param nodeRefs  list of node references
     */
    public NodeRefListQueryResultImpl(SessionImpl session, List<NodeRef> nodeRefs)
    {
        this.session = session;
        this.nodeRefs = nodeRefs;
        this.nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();        
    }

    /**
     * Get proxied JCR Query Result
     * 
     * @return  proxy
     */
    public QueryResult getProxy()
    {
        if (proxy == null)
        {
            proxy = (QueryResult)JCRProxyFactory.create(this, QueryResult.class, session);
        }
        return proxy;
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.QueryResult#getColumnNames()
     */
    public String[] getColumnNames() throws RepositoryException
    {
        Map<QName, PropertyDefinition> columns = getColumnDefinitions();
        String[] names = new String[columns.size()];
        int i = 0;
        for (QName columnName : columns.keySet())
        {
            names[i++] = columnName.toPrefixString(session.getNamespaceResolver());
        }
        return names;
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.QueryResult#getRows()
     */
    public RowIterator getRows() throws RepositoryException
    {
        return new NodeRefRowIteratorImpl(session, getColumnDefinitions(), nodeRefs).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.QueryResult#getNodes()
     */
    public NodeIterator getNodes() throws RepositoryException
    {
        return new NodeRefNodeIteratorImpl(session, nodeRefs);
    }

    
    /**
     * Get list of column definitions
     *  
     * @return  list of column definitions
     */
    private Map<QName, PropertyDefinition> getColumnDefinitions()
        throws RepositoryException
    {
        if (columns == null)
        {
            columns = new HashMap<QName, PropertyDefinition>();
            
            // build list of column names from result set
            if (nodeRefs.size() > 0)
            {
                // Base column list on first node ref
                // TODO: determine on a more formal basis
                QName type = nodeService.getType(nodeRefs.get(0));
                NodeType nodeType = session.getTypeManager().getNodeType(type.toPrefixString(session.getNamespaceResolver()));
                PropertyDefinition[] propDefs = nodeType.getPropertyDefinitions();
                for (PropertyDefinition propDef : propDefs)
                {
                    if (!propDef.isMultiple())
                    {
                        columns.put(QName.createQName(propDef.getName(), session.getNamespaceResolver()), propDef);
                    }
                }
                Set<QName>aspects = nodeService.getAspects(nodeRefs.get(0));
                for (QName aspect : aspects)
                {
                    NodeType nodeAspect = session.getTypeManager().getNodeType(aspect.toPrefixString(session.getNamespaceResolver()));
                    propDefs = nodeAspect.getPropertyDefinitions();
                    for (PropertyDefinition propDef : propDefs)
                    {
                        if (!propDef.isMultiple())
                        {
                            columns.put(QName.createQName(propDef.getName(), session.getNamespaceResolver()), propDef);
                        }
                    }
                }
            }
            
            // add JCR required columns
            columns.put(QueryManagerImpl.JCRPATH_COLUMN, null);
            columns.put(QueryManagerImpl.JCRSCORE_COLUMN, null);
        }
        return columns;        
    }
    
}
