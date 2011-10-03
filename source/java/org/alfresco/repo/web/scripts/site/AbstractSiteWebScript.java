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
package org.alfresco.repo.web.scripts.site;

import java.util.Map;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractSiteWebScript extends DeclarativeWebScript
{
    protected SiteService siteService;
    protected AuthorityService authorityService;
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    
    protected static String buildSiteGroup(SiteInfo site)
    {
        return "GROUP_site_" + site.getShortName();
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
          Status status, Cache cache) 
    {
       // Grab the site
       String siteName = 
           req.getServiceMatch().getTemplateVars().get("shortname");
       SiteInfo site = siteService.getSite(siteName);
       if (site == null)
       {
           throw new WebScriptException(
                   Status.STATUS_NOT_FOUND, 
                   "No Site found with that short name");
       }
       
       // Process
       return executeImpl(site, req, status, cache);
    }
    protected abstract Map<String, Object> executeImpl(SiteInfo site,
          WebScriptRequest req, Status status, Cache cache);
}