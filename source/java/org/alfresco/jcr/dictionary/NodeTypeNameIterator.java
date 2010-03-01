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
