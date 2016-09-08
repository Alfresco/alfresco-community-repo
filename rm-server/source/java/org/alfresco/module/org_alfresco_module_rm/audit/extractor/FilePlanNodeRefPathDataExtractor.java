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
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * An extractor that extracts the NodeRef path from the RM root down to
 * - and including - the node itself.  This will only extract data if the
 * node is a {@link RecordsManagementModel#ASPECT_FILE_PLAN_COMPONENT fileplan component}.
 * 
 * @see RecordsManagementService#getNodeRefPath(NodeRef)
 * 
 * @author Derek Hulley
 * @since 1.0
 */
public final class FilePlanNodeRefPathDataExtractor extends AbstractDataExtractor
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
        if (data == null || !(data instanceof NodeRef))
        {
            return false;
        }
        return nodeService.hasAspect((NodeRef)data, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
    }

    public Serializable extractData(Serializable value) throws Throwable
    {
        NodeRef nodeRef = (NodeRef) value;
        
        // Get path from the RM root
        List<NodeRef> nodeRefPath = filePlanService.getNodeRefPath(nodeRef);
        
        // Done
        return (Serializable) nodeRefPath;
    }
}
