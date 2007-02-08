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
package org.alfresco.repo.importer.view;

import org.alfresco.service.namespace.QName;


/**
 * Represents Property Context
 * 
 * @author David Caruana
 *
 */
public class NodeItemContext extends ElementContext
{
    private NodeContext nodeContext;
    
    /**
     * Construct
     * 
     * @param elementName
     * @param dictionary
     * @param importer
     */
    public NodeItemContext(QName elementName, NodeContext nodeContext)
    {
        super(elementName, nodeContext.getDictionaryService(), nodeContext.getImporter());
        this.nodeContext = nodeContext;
    }
    
    /**
     * Gets the Node Context
     */
    public NodeContext getNodeContext()
    {
        return nodeContext;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "NodeItemContext[nodeContext=" + nodeContext.toString() + "]";
    }
 

}
