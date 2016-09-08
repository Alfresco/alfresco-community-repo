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

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 Saved Search Patch
 * 
 * 
 * @author Roy Wetherall
 */
public class RMv2SavedSearchPatch extends AbstractModuleComponent 
                                  implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RMv2SavedSearchPatch.class);  
    
    /** RM site id */
    private static final String RM_SITE_ID = "rm";
    
    /** Records management search service */
    private RecordsManagementSearchService recordsManagementSearchService;
    
    /** Site service */
    private SiteService siteService;
    
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
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("RM Module RMv2SavedSearchPatch ...");
        }
        
        if (siteService.getSite(RM_SITE_ID) != null)
        {
            // get the saved searches
            List<SavedSearchDetails> savedSearches = recordsManagementSearchService.getSavedSearches(RM_SITE_ID);
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... updating " + savedSearches.size() + " saved searches");
            }
            
            for (SavedSearchDetails savedSearchDetails : savedSearches)
            {
                // re-save each search so that the query is regenerated correctly
                recordsManagementSearchService.deleteSavedSearch(RM_SITE_ID, savedSearchDetails.getName());
                recordsManagementSearchService.saveSearch(RM_SITE_ID, 
                                                          savedSearchDetails.getName(), 
                                                          savedSearchDetails.getDescription(), 
                                                          savedSearchDetails.getSearch(), 
                                                          savedSearchDetails.getSearchParameters(), 
                                                          savedSearchDetails.isPublic());            
            }
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("   ... complete");
        }        
    }
}
