/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.audit.extractor;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * An extractor that gets a node's {@link RecordsManagementModel#PROP_IDENTIFIER identifier} property.
 * This will only extract data if the node is a
 * {@link RecordsManagementModel#ASPECT_RECORD_COMPONENT_ID Record component identifier}.
 *
 * @author Derek Hulley
 * @since 3.2
 */
public final class FilePlanIdentifierDataExtractor extends AbstractDataExtractor
{
    private NodeService nodeService;

    /**
     * Used to check that the node in the context is a fileplan component
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @return              Returns <tt>true</tt> if the data is a NodeRef and it represents
     *                      a fileplan component
     */
    public boolean isSupported(Serializable data)
    {
        if (!(data instanceof NodeRef))
        {
            return false;
        }
        return nodeService.hasAspect((NodeRef)data, RecordsManagementModel.ASPECT_RECORD_COMPONENT_ID);
    }

    public Serializable extractData(Serializable value) throws Throwable
    {
        NodeRef nodeRef = (NodeRef) value;

        String identifier = (String) nodeService.getProperty(nodeRef, RecordsManagementModel.PROP_IDENTIFIER);

        // Done
        return identifier;
    }
}
