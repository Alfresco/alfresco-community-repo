/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * An extractor that extracts the <b>cm:name</b> path from the RM root down to
 * - and including - the node's own name.  This will only extract data if the
 * node is a {@link RecordsManagementModel#ASPECT_FILE_PLAN_COMPONENT fileplan component}.
 *
 * @see FilePlanService#getNodeRefPath(NodeRef)
 *
 * @author Derek Hulley
 * @since 3.2
 */
public final class FilePlanNamePathDataExtractor extends AbstractDataExtractor
{
    private NodeService nodeService;
    private FilePlanService filePlanService;

    /**
     * Used to check that the node in the context is a fileplan component
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
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
        return nodeService.hasAspect((NodeRef)data, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
    }

    /**
     * @see org.alfresco.repo.audit.extractor.DataExtractor#extractData(java.io.Serializable)
     */
    public Serializable extractData(Serializable value)
    {
        NodeRef nodeRef = (NodeRef) value;

        // Get path from the RM root
        List<NodeRef> nodeRefPath = filePlanService.getNodeRefPath(nodeRef);

        StringBuilder sb = new StringBuilder(128);
        for (NodeRef pathNodeRef : nodeRefPath)
        {
            String name = (String)nodeService.getProperty(pathNodeRef, ContentModel.PROP_NAME);
            sb.append("/").append(name);
        }

        // Done
        return sb.toString();
    }
}
