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
