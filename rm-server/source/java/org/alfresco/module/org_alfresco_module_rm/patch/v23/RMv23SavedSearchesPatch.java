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
package org.alfresco.module.org_alfresco_module_rm.patch.v23;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_SAVED_SEARCH;
import static org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType.DEFAULT_SITE_NAME;

import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.service.cmr.repository.NodeService;


/**
 * RM v2.3 patch that adds the saved search aspect.
 *
 * @author Ross Gale
 * @since 2.3
 */
public class RMv23SavedSearchesPatch extends AbstractModulePatch
{
    /** records management search service */
    private RecordsManagementSearchService recordsManagementSearchService;

    /** node service */
    private NodeService nodeService;

    /**
     * @param recordsManagementSearchService	records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
		this.recordsManagementSearchService = recordsManagementSearchService;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Retrieves all saved searches for the records management site and adds ASPECT_SAVED_SEARCH
     */
    @Override
    public void applyInternal()
    {
       for(SavedSearchDetails savedSearchDetails : recordsManagementSearchService.getSavedSearches(DEFAULT_SITE_NAME))
        {
            nodeService.addAspect(savedSearchDetails.getNodeRef(), ASPECT_SAVED_SEARCH,null);
        }
    }
}
