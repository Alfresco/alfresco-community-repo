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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;


/**
 * Row Iterator based on a list of Node References
 * 
 * @author David Caruana
 */
public class NodeRefRowIteratorImpl extends AbstractRangeIterator implements RowIterator
{
    private SessionImpl session;
    private Map<QName, PropertyDefinition> columns;
    private List<NodeRef> nodeRefs;
    private RowIterator proxy = null;
    
    /**
     * Construct
     * 
     * @param session
     * @param columnNames
     * @param nodeRefs
     */
    public NodeRefRowIteratorImpl(SessionImpl session, Map<QName, PropertyDefinition> columns, List<NodeRef> nodeRefs)
    {
        this.session = session;
        this.columns = columns;
        this.nodeRefs = nodeRefs;
    }

    /**
     * Get proxied JCR Query
     * 
     * @return  proxy
     */
    public RowIterator getProxy()
    {
        if (proxy == null)
        {
            proxy = (RowIterator)JCRProxyFactory.create(this, RowIterator.class, session);
        }
        return proxy;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.query.RowIterator#nextRow()
     */
    public Row nextRow()
    {
        long position = skip();
        NodeRef nodeRef = nodeRefs.get((int)position);
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();        
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return new PropertyMapRowImpl(session, columns, nodeRef, properties);
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return nodeRefs.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextRow();
    }
    
}
