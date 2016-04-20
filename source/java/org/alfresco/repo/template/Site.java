/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.template;

import org.alfresco.repo.admin.SysAdminParamsImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.UrlUtil;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Site support in FreeMarker templates.
 * 
 * @author Mike Hatfield
 * @author Kevin Roast
 */
public class Site extends BaseTemplateProcessorExtension
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    private SiteService siteService;
    
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry	the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

    /**
     * Set the site service
     * 
     * @param siteService The siteService to set.
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Gets the SiteInfo given the shortName
     * 
     * @param shortName  the shortName of the Site to get
     * @return the Site or null if no such site exists 
     */
    public SiteInfo getSiteInfo(String shortName)
    {
        ParameterCheck.mandatoryString("shortName", shortName);
        return siteService.getSite(shortName);
    }
    
    /**
     * This method returns a URL stem which resolves to the configured Alfresco Share URL.
     * <p>
     * @see SysAdminParamsImpl#setAlfrescoHost(String)
     * @see SysAdminParamsImpl#setShareHost(String)
     */
    public String getShareUrl()
    {
        return UrlUtil.getShareUrl(services.getSysAdminParams());
    }
    
    /**
     * This method returns a URL which resolves to the configured Alfresco Share site URL.
     * <p>
     * @see SysAdminParamsImpl#setAlfrescoHost(String)
     * @see SysAdminParamsImpl#setShareHost(String)
     * @param siteShortName  the shortName of the Site to build URL for
     * @return the Site or null if no such site exists 
     */
    public String getShareSiteUrl(String siteShortName)
    {
        StringBuilder result = new StringBuilder(UrlUtil.getShareUrl(services.getSysAdminParams()));
        result.append("/page");
        if (siteShortName != null)
        {
            result.append("/site/").append(siteShortName);
        }
        
        return result.toString();
    }
}
