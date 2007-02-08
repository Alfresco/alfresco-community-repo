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
package org.alfresco.jcr.dictionary;

import java.util.List;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.service.namespace.QName;


/**
 * Alfresco implementation of a Node Type Iterator
 * 
 * @author David Caruana
 */
public class NodeTypeNameIterator extends AbstractRangeIterator
    implements NodeTypeIterator
{
    private NodeTypeManagerImpl typeManager;
    private List<QName> nodeTypeNames;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodeTypes  node type list
     */
    public NodeTypeNameIterator(NodeTypeManagerImpl typeManager, List<QName> nodeTypeNames)
    {
        this.typeManager = typeManager;
        this.nodeTypeNames = nodeTypeNames;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeTypeIterator#nextNodeType()
     */
    public NodeType nextNodeType()
    {
        long position = skip();
        QName name = nodeTypeNames.get((int)position);
        return typeManager.getNodeTypeImpl(name);
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return nodeTypeNames.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextNodeType();
    }

}
