/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.site.script;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.site.SiteInfo;
import org.alfresco.repo.site.SiteService;
import org.alfresco.service.ServiceRegistry;


/**
 * Script object representing the site service.
 * 
 * @author Roy Wetherall
 */
public class ScriptSiteService extends BaseScopableProcessorExtension
{
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
    /** The site service */
    private SiteService siteService;

    /**
     * Sets the Service Registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Set the site service
     * 
     * @param siteService   the site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Create a new site.
     * <p>
     * The site short name will be used to uniquely identify the site so it must be unique.
     * 
     * @param sitePreset    site preset
     * @param shortName     site short name
     * @param title         site title
     * @param description    site description
     * @param isPublic      whether the site is public or not
     * @return Site         the created site
     */
    public Site createSite(String sitePreset, String shortName, String title, String description, boolean isPublic)
    {                
        SiteInfo siteInfo = this.siteService.createSite(sitePreset, shortName, title, description, isPublic);
        return new Site(siteInfo, this.serviceRegistry, this.siteService, getScope());
    }
    
    /**
     * List the sites available in the repository.  The returned list can optionally be filtered by name and site
     * preset.
     * <p>
     * If no filters are specified then all the available sites are returned.
     * 
     * @param nameFilter        name filter
     * @param sitePresetFilter  site preset filter
     * @return Site[]           a list of the site filtered as appropriate             
     */
    public Site[] listSites(String nameFilter, String sitePresetFilter)
    {
        List<SiteInfo> siteInfos = this.siteService.listSites(nameFilter, sitePresetFilter);
        List<Site> sites = new ArrayList<Site>(siteInfos.size());
        for (SiteInfo siteInfo : siteInfos)
        {
            sites.add(new Site(siteInfo, this.serviceRegistry, this.siteService, getScope()));
        }
        return (Site[])sites.toArray(new Site[sites.size()]);
    }  
    
    /**
     * List all the sites that the specified user has an explicit membership to.
     * 
     * @param userName      user name
     * @return Site[]       a list of sites the user has an explicit membership to
     */
    public Site[] listUserSites(String userName)
    {
        List<SiteInfo> siteInfos = this.siteService.listSites(userName);
        List<Site> sites = new ArrayList<Site>(siteInfos.size());
        for (SiteInfo siteInfo : siteInfos)
        {
            sites.add(new Site(siteInfo, this.serviceRegistry, this.siteService, getScope()));
        }
        return (Site[])sites.toArray(new Site[sites.size()]);
    }
    
    /**
     * Get a site for a provided site short name.
     * <p>
     * Returns null if the site does not exist.
     * 
     * @param shortName     short name of the site
     * @return Site         the site, null if does not exist
     */
    public Site getSite(String shortName)
    {
        Site site = null;
        SiteInfo siteInfo = this.siteService.getSite(shortName);
        if (siteInfo != null)
        {
            site = new Site(siteInfo, this.serviceRegistry, this.siteService, getScope());
        }
        return site;
    }
}
