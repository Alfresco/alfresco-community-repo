/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.PropertyCheck;

/**
 * An extractor that pulls out the {@link ContentModel#PROP_NAME <b>cm:name</b>} property from a node.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class NodeNameDataExtractor extends AbstractDataExtractor
{
    private NodeService nodeService;
    
    /**
     * Set the service to get the property from
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "nodeService", nodeService);
    }

    /**
     * @return          Returns <tt>true</tt> if the data is a {@link NodeRef}
     */
    public boolean isSupported(Serializable data)
    {
        return (data != null && data instanceof NodeRef);
    }

    /**
     * Gets the <b>cm:name</b> property from the node
     */
    public Serializable extractData(Serializable in) throws Throwable
    {
        NodeRef nodeRef = (NodeRef) in;
        String nodeName = null;
        if (!nodeService.exists(nodeRef))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Extractor can't pull value from non-existent node: " + nodeRef);
            }
        }
        else
        {
            nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        }
        return nodeName;
    }
}
