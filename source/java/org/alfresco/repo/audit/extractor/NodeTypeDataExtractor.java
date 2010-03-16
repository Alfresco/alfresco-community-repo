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
package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * An extractor that pulls out the type of a node.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class NodeTypeDataExtractor extends AbstractDataExtractor
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
        QName nodeType = null;
        if (!nodeService.exists(nodeRef))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Extractor can't pull value from non-existent node: " + nodeRef);
            }
        }
        else
        {
            nodeType = nodeService.getType(nodeRef);
        }
        return nodeType;
    }
}
