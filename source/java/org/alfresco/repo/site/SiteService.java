package org.alfresco.repo.site;

import java.util.List;
import java.util.Map;

/**
 * Site service fundamental API.
 * <p>
 * This service API is designed to support the public facing Site APIs
 * 
 * @author Roy Wetherall
 */
public interface SiteService
{
    /**
     * Create a new site.
     * 
     * @param sitePreset    site preset name
     * @param shortName     site short name, must be unique
     * @param title         site title
     * @param description   site description
     * @param isPublic      whether the site is public or not
     * @return SiteInfo     information about the created site
     */
    // TODO ... audit information
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, boolean isPublic);
    
    /**
     * List the available sites.  This list can optionally be filtered by site name and/or site preset.
     * 
     * @param nameFilter            name filter
     * @param sitePresetFilter      site preset filter
     * @return List<SiteInfo>       list of site information
     */
    // TODO audit information
    List<SiteInfo> listSites(String nameFilter, String sitePresetFilter);
    
    SiteInfo getSite(String shortName);
    
    void updateSite(SiteInfo siteInfo);
    
    void deleteSite(String shortName);
    
    Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter);
    
    void setMembership(String shortName, String userName, String role);
    
    void removeMembership(String shortName, String userName);
    
    
    
    
}
