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
package org.alfresco.jcr.item;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Alfresco implementation of a Node Iterator
 * 
 * @author David Caruana
 */
public class NodeRefNodeIteratorImpl extends AbstractRangeIterator
    implements NodeIterator
{
    private SessionImpl sessionImpl;
    private List<NodeRef> nodeRefs;
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodes  node list
     */
    public NodeRefNodeIteratorImpl(SessionImpl sessionImpl, List<NodeRef> nodeRefs)
    {
        this.sessionImpl = sessionImpl;
        this.nodeRefs = nodeRefs;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.NodeIterator#nextNode()
     */
    public Node nextNode()
    {
        long position = skip();
        NodeRef nodeRef = nodeRefs.get((int)position);
        NodeImpl nodeImpl = new NodeImpl(sessionImpl, nodeRef);
        return nodeImpl.getProxy();
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
        return nextNode();
    }

}
