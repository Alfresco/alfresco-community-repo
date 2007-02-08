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
package org.alfresco.jcr.item;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.service.cmr.repository.ChildAssociationRef;


/**
 * Alfresco implementation of a Node Iterator
 * 
 * @author David Caruana
 */
public class ChildAssocNodeIteratorImpl extends AbstractRangeIterator
    implements NodeIterator
{
    private SessionImpl sessionImpl;
    private List<ChildAssociationRef> childAssocs;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodes  node list
     */
    public ChildAssocNodeIteratorImpl(SessionImpl sessionImpl, List<ChildAssociationRef> childAssocs)
    {
        this.sessionImpl = sessionImpl;
        this.childAssocs = childAssocs;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.NodeIterator#nextNode()
     */
    public Node nextNode()
    {
        long position = skip();
        ChildAssociationRef childAssocRef = childAssocs.get((int)position);
        NodeImpl nodeImpl = new NodeImpl(sessionImpl, childAssocRef.getChildRef());
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return childAssocs.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextNode();
    }

}
