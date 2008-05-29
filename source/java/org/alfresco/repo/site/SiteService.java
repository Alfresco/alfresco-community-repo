package org.alfresco.repo.site;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
    
    /**
     * Gets site information based on the short name of a site.
     * <p>
     * Returns null if the site can not be found.
     * 
     * @param shortName     the site short name
     * @return SiteInfo     the site information
     */
    SiteInfo getSite(String shortName);
    
    /**
     * Update the site information.
     * <P>
     * Note that the shortname and sitepreset of a site can not be updated once the site has been created.
     * 
     * @param siteInfo  site information
     */
    void updateSite(SiteInfo siteInfo);
    
    /**
     * Delete the site.
     * 
     * @param shortName     site short name
     */
    void deleteSite(String shortName);
    
    /**
     * List the memebers of the site.
     * <p>
     * Name and role filters are optional and if not specified all the memebers of the site are returned.
     * 
     * @param shortName     site short name
     * @param nameFilter    name filter
     * @param roleFilter    role filter
     * @return Map<String, String>  the username and their role
     */
    Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter);
    
    /**
     * 
     * @param shortName
     * @param userName
     * @return
     */
    String getMembersRole(String shortName, String userName);
    
    /**
     * 
     * @param shortName
     * @param userName
     * @return
     */
    boolean isMember(String shortName, String userName);
    
    /**
     * 
     * @param shortName
     * @param userName
     * @param role
     */
    void setMembership(String shortName, String userName, String role);
    
    /**
     * 
     * @param shortName
     * @param userName
     */
    void removeMembership(String shortName, String userName);
    
    
    /**
     * Gets (or creates, if it doesn't exist) the "container" folder for the specified
     * component.
     *
     * @param shortName  short name of site
     * @param componentId  component id
     * @param folderType  type of folder to create (if null, creates standard folder)
     * @return  noderef of container
     */
    NodeRef getContainer(String shortName, String componentId, QName folderType);

    /**
     * Determines if a "container" folder for the specified component exists.
     * 
     * @param shortName  short name of site
     * @param componentId  component id
     * @return  true => "container" folder exists for component
     */
    boolean hasContainer(String shortName, String componentId);
    
}
