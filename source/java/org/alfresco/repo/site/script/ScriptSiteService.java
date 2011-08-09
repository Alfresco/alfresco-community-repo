/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.site.script;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;


/**
 * Script object representing the site service.
 * 
 * @author Roy Wetherall
 */
public class ScriptSiteService extends BaseScopableProcessorExtension
{
    /** Visibility helper constants */
    public static final String PUBLIC_SITE = "PUBLIC";
    public static final String MODERATED_SITE = "MODERATED";
    public static final String PRIVATE_SITE = "PRIVATE";
    
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
     * @see {@link #createSite(String, String, String, String, String)}
     * 
     * @param sitePreset    site preset
     * @param shortName     site short name
     * @param title         site title
     * @param description   site description
     * @param isPublic      whether the site is public or not
     * @return Site         the created site
     * @deprecated          as of version 3.2, replaced by {@link #createSite(String, String, String, String, String)}
     */    
    public Site createSite(String sitePreset, String shortName, String title, String description, boolean isPublic)
    {                
        SiteInfo siteInfo = this.siteService.createSite(sitePreset, shortName, title, description, isPublic);
        return new Site(siteInfo, this.serviceRegistry, this.siteService, getScope());
    }
    
    /**
     * Create a new site.
     * <p>
     * The site short name will be used to uniquely identify the site so it must be unique.
     * 
     * @param sitePreset    site preset
     * @param shortName     site short name
     * @param title         site title
     * @param description   site description
     * @param visibility    visibility of the site (public|moderated|private)
     * @return Site         the created site
     */
    public Site createSite(String sitePreset, String shortName, String title, String description, String visibility)
    { 
        ParameterCheck.mandatoryString("visibility", visibility);
        SiteVisibility siteVisibility = SiteVisibility.valueOf(visibility);
        SiteInfo siteInfo = this.siteService.createSite(sitePreset, shortName, title, description, siteVisibility);
        return new Site(siteInfo, this.serviceRegistry, this.siteService, getScope());
    }
    
    /**
     * Create a new site.
     * <p>
     * The site short name will be used to uniquely identify the site so it must be unique.
     * 
     * @param sitePreset    site preset
     * @param shortName     site short name
     * @param title         site title
     * @param description   site description
     * @param visibility    visibility of the site (public|moderated|private)
     * @param siteType      qname of site type to create
     * @return Site         the created site
     */
    public Site createSite(String sitePreset, String shortName, String title, String description, String visibility, String siteType)
    {     	
        ParameterCheck.mandatoryString("visibility", visibility);
        SiteVisibility siteVisibility = SiteVisibility.valueOf(visibility);
        QName siteTypeQName = QName.createQName(siteType);
        SiteInfo siteInfo = this.siteService.createSite(sitePreset, shortName, title, description, siteVisibility, siteTypeQName);
        return new Site(siteInfo, this.serviceRegistry, this.siteService, getScope());
    }
    
    /**
     * This method checks if the currently authenticated user has permission to create sites.
     * 
     * @return <code>true</code> if the current user is a Contributor to "Sites" node, else <code>false</code>.
     * @since 3.4
     */
    public boolean hasCreateSitePermissions()
    {
    	return siteService.hasCreateSitePermissions();
    }
    
    /**
     * Determines if the current user is a manager of the given site.
     * 
     * @param siteId The short name of the site to check
     * @return true if the current user is a manager of the given site
     */
    public boolean isSiteManager(String siteId)
    {
        String role = siteService.getMembersRole(siteId, AuthenticationUtil.getFullyAuthenticatedUser());
        return (SiteServiceImpl.SITE_MANAGER.equals(role));
    }
    
    /**
     * Retrieves the sites available in the repository. The returned list can optionally be filtered by name and site
     * preset. If no filters are specified then all the available sites are returned.
     * 
     * NOTE: If the filter starts with a * a Lucene based search will be performed, this may discover a wider range
     * of results i.e. those sites that contain the search term as opposed to those that start with the search term,
     * but newly created sites may not be found until the underlying search indexes are updated.
     * 
     * @param filter            inclusion filter for returned sites. Only sites whose cm:name OR cm:title
     *                          OR cm:description start with the filter string will be returned.
     * @param sitePresetFilter  site preset filter
     * @param size              max results size crop if >0
     * @return Site[]           a list of the site filtered as appropriate
     */
    public Site[] getSites(String filter, String sitePresetFilter, int size)
    {
        // reset filter if necessary
        if (filter != null && (filter.length() == 0 || filter.equals("*")))
        {
            filter = null;
        }
        
        if (filter != null && (filter.startsWith("*")))
        {
            // findSites will add the wildcard so remove here
            filter = filter.substring(1, filter.length());
            
            // use findSites to do a "contains" search
            return findSites(filter, sitePresetFilter, size);
        }
        else
        {
            // use listSites to do a canned query (will provide consistent results)
            return listSites(filter, sitePresetFilter, size);
        }
    }
    
    /**
     * List the sites available in the repository.  The returned list can optionally be filtered by name and site
     * preset.
     * <p>
     * If no filters are specified then all the available sites are returned.
     * 
     * @param filter            inclusion filter for returned sites. Only sites whose cm:name OR cm:title
     *                          OR cm:description start with the filter string will be returned.
     * @param sitePresetFilter  site preset filter
     * @return Site[]           a list of the site filtered as appropriate
     * 
     * @see SiteService#listSites(String, String, int) for a description of the limitations of this method.
     */
    public Site[] listSites(String filter, String sitePresetFilter)
    {
        return listSites(filter, sitePresetFilter, -1);
    }
    
    /**
     * List the sites available in the repository.  The returned list can optionally be filtered by name and site
     * preset.
     * <p/>
     * If no filters are specified then all the available sites are returned.
     * 
     * @param filter            inclusion filter for returned sites. Only sites whose cm:name OR cm:title
     *                          OR cm:description start with the filter string will be returned.
     * @param sitePresetFilter  site preset filter
     * @param size              max results size crop if >0
     * @return Site[]           a list of the site filtered as appropriate
     * 
     * @see SiteService#listSites(String, String, int) for a description of the limitations of this method.
     */
    public Site[] listSites(String filter, String sitePresetFilter, int size)
    {
        List<SiteInfo> siteInfos = this.siteService.listSites(filter, sitePresetFilter, size);
        return makeSitesArray(siteInfos);
    }
    
    /**
     * Find (search) the sites available in the repository.  The returned list can optionally be filtered by name and site
     * preset.
     * <p/>
     * If no filters are specified then all the available sites are returned.
     * 
     * @param filter            inclusion filter for returned sites. Only sites whose cm:name OR cm:title
     *                          OR cm:description CONTAIN the filter string will be returned.
     * @param sitePresetFilter  site preset filter
     * @param size              max results size crop if >0
     * 
     * @return Site[]           a list of the site filtered as appropriate
     * 
     * @see SiteService#findSites(String, String, int) for a description of the limitations of this method.
     * @since 4.0
     */
    public Site[] findSites(String filter, String sitePresetFilter, int size)
    {
        List<SiteInfo> siteInfos = this.siteService.findSites(filter, sitePresetFilter, size);
        return makeSitesArray(siteInfos);
    }
    
    /**
     * Converts the given List of SiteInfo objects to a JavaScript friendly array
     * of Site objects.
     * 
     * @param siteInfos
     * @return Array of Site objects
     */
    protected Site[] makeSitesArray(List<SiteInfo> siteInfos)
    {
        List<Site> sites = new ArrayList<Site>(siteInfos.size());
        for (SiteInfo siteInfo : siteInfos)
        {
            sites.add(new Site(siteInfo, this.serviceRegistry, this.siteService, getScope()));
        }
        
        return sites.toArray(new Site[sites.size()]);
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
        return sites.toArray(new Site[sites.size()]);
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
    
    /**
     * Returns an array of all the roles that can be assigned to a member of a site.
     * 
     * @return  String[]    roles available to assign to a member of a site
     */
    public String[] listSiteRoles()
    {
        List<String> roles = this.siteService.getSiteRoles();
        return roles.toArray(new String[roles.size()]);
    }
    
    /**
     * It removes permissions which pertain to sites other than the node's current site.
     * 
     * @param targetNode the root node which is to have its permissions cleaned.
     * @see SiteService#cleanSitePermissions(NodeRef, SiteInfo)
     */
    public void cleanSitePermissions(NodeRef targetNode)
    {
        this.siteService.cleanSitePermissions(targetNode, null);
    }

    /**
     * This method cleans up the permissions on the specified node and all its primary children.
     * It removes permissions which pertain to sites other than the node's current site.
     * 
     * @since 3.4.2
     * @see SiteService#cleanSitePermissions(NodeRef, SiteInfo)
     */
    public void cleanSitePermissions(ScriptNode targetNode)
    {
        this.cleanSitePermissions(targetNode.getNodeRef());
    }

    /**
     * Returns an array of all the roles that can be assigned to a member of a 
     *  specific site.
     * 
     * @return  String[]    roles available to assign to a member of a site
     */
    public String[] listSiteRoles(String shortName)
    {
        List<String> roles = this.siteService.getSiteRoles(shortName);
        return (String[])roles.toArray(new String[roles.size()]);
    }
}
