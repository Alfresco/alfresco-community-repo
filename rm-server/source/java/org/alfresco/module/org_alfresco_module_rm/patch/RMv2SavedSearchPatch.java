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
package org.alfresco.module.org_alfresco_module_rm.patch;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 Saved Search Patch
 * 
 * 
 * @author Roy Wetherall
 */
public class RMv2SavedSearchPatch extends ModulePatchComponent 
                                  implements BeanNameAware, RecordsManagementModel, DOD5015Model
{    
    /** RM site id */
    private static final String RM_SITE_ID = "rm";
    
    /** Records management search service */
    private RecordsManagementSearchService recordsManagementSearchService;
    
    /** Site service */
    private SiteService siteService;
    
    /** Content service */
    private ContentService contentService;
    
    /**
     * @param recordsManagementSearchService    records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
        this.recordsManagementSearchService = recordsManagementSearchService;
    }
    
    /**
     * @param siteService   site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch() throws Throwable
    {
        if (siteService.getSite(RM_SITE_ID) != null)
        {
            // get the saved searches
            List<SavedSearchDetails> savedSearches = recordsManagementSearchService.getSavedSearches(RM_SITE_ID);
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("  ... updating " + savedSearches.size() + " saved searches");
            }
            
            for (SavedSearchDetails savedSearchDetails : savedSearches)
            {
                // refresh the query
                String refreshedJSON = savedSearchDetails.toJSONString();
                NodeRef nodeRef = savedSearchDetails.getNodeRef(); 
 
                if (nodeRef != null)
                {
                    ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                    writer.putContent(refreshedJSON);
                    
                    
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("    ... updated saved search " + savedSearchDetails.getName() + " (nodeRef=" + nodeRef.toString() + ")");
                    }
                }                
            }
        }       
    }
}
