/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Records Management saved search DELETE web script
 * 
 * @author Roy Wetherall
 */
public class RMSavedSearchesDelete extends DeclarativeWebScript
{
    /** Records management search service */
    protected RecordsManagementSearchService recordsManagementSearchService;
    
    /** Site service */
    protected SiteService siteService;
    
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
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();        
        
        // Get the site id and confirm it's valid
        String siteId = templateVars.get("site");
        if (siteId == null || siteId.length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Site id not provided.");
        }
        if (siteService.getSite(siteId) == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Site not found.");
        }
        
        // Get the name of the saved search
        String name = templateVars.get("name");
        if (name ==  null || name.length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Saved search name not provided.");
        }
        
        // Delete the saved search
        recordsManagementSearchService.deleteSavedSearch(siteId, name);               
        
        // Indicate success in the model
        Map<String, Object> model = new HashMap<>(1);
        model.put("success", true);
        return model;
    }
}
