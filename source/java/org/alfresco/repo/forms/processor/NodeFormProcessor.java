/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.forms.processor;

import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FormProcessor implementation that can generate and persist Form objects
 * for repository nodes.
 *
 * @author Gavin Cornwell
 */
public class NodeFormProcessor extends AbstractFormProcessorByHandlers
{
    /** Logger */
    private static Log logger = LogFactory.getLog(NodeFormProcessor.class);
    
    /** Services */
    protected NodeService nodeService;
    
    /**
     * Sets the node service 
     * 
     * @param nodeService The NodeService instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.AbstractFormProcessorByHandlers#getTypedItem(org.alfresco.repo.forms.Item)
     */
    @Override
    protected Object getTypedItem(Item item)
    {
        // create NodeRef representation, the id could already be in a valid
        // NodeRef format or it may be in a URL friendly format
        NodeRef nodeRef = null;
        if (NodeRef.isNodeRef(item.getId()))
        {
            nodeRef = new NodeRef(item.getId());
        }
        else
        {
            // split the string into the 3 required parts
            String[] parts = item.getId().split("/");
            if (parts.length == 3)
            {
                try
                {
                    nodeRef = new NodeRef(parts[0], parts[1], parts[2]);
                }
                catch (IllegalArgumentException iae)
                {
                    // ignored for now, dealt with below
                    
                    if (logger.isDebugEnabled())
                        logger.debug("NodeRef creation failed for: " + item.getId(), iae);
                }
            }
        } 
        
        // check we have a valid node ref
        if (nodeRef == null)
        {
            throw new FormNotFoundException(item, 
                        new IllegalArgumentException(item.getId()));
        }
        
        // check the node itself exists
        if (this.nodeService.exists(nodeRef) == false)
        {
            throw new FormNotFoundException(item, 
                        new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef));
        }
        else
        {
            // all Node based handlers can expect to get a NodeRef
            return nodeRef;
        }
    }
}
