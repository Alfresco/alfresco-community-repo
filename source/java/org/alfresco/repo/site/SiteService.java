package org.alfresco.repo.site;

import java.io.Serializable;
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
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, boolean isPublic);
    
    /**
     * List the available sites.  This list can optionally be filtered by site name and/or site preset.
     * 
     * @param nameFilter            name filter
     * @param sitePresetFilter      site preset filter
     * @return List<SiteInfo>       list of site information
     */
    List<SiteInfo> listSites(String nameFilter, String sitePresetFilter);
    
    /**
     * List all the sites that the specified user has a explicit membership to.
     *
     * @param userName          user name
     * @return List<SiteInfo>   list of site information
     */
    List<SiteInfo> listSites(String userName);
    
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
     * Gets the role of the specified user
     * 
     * @param shortName     site short name
     * @param userName      user name
     * @return String       site role, null if none
     */
    String getMembersRole(String shortName, String userName);
    
    /**
     * Inidiactes whether a user is a member of a site or not
     * 
     * @param shortName     site short name
     * @param userName      user name
     * @return boolean      true if the user is a member of the site, false otherwise
     */
    boolean isMember(String shortName, String userName);
    
    /**
     * Sets the role of a user withint a site
     * 
     * @param shortName     site short name
     * @param userName      user name
     * @param role          site role
     */
    void setMembership(String shortName, String userName, String role);
    
    /**
     * Clears a users role within a site
     * 
     * @param shortName     site short name
     * @param userName      user name
     */
    void removeMembership(String shortName, String userName);
    
    /**
     * Creates a container for a component is a site of the given container type (must be a sub-type of st:siteContainer)
     * <p>
     * If no container type is specified then a node of type st:siteContainer is created.
     * <p>
     * The map of container properties are set on the created container node.  Null can be provided when no properties
     * need to be set.
     * 
     * @param shortName                 site short name
     * @param componentId               component id
     * @param containerType             container type to create (can be null)
     * @param containerProperties       container property values (can be null)
     * @return
     */
    NodeRef createContainer(String shortName, String componentId, QName containerType, Map<QName, Serializable> containerProperties);
    
    /**
     * Gets the "container" folder for the specified
     * component.
     *
     * @param shortName  short name of site
     * @param componentId  component id
     * @param folderType  type of folder to create (if null, creates standard folder)
     * @return  noderef of container
     */
    NodeRef getContainer(String shortName, String componentId);

    /**
     * Determines if a "container" folder for the specified component exists.
     * 
     * @param shortName  short name of site
     * @param componentId  component id
     * @return  true => "container" folder exists for component
     */
    boolean hasContainer(String shortName, String componentId);
    
    /**
     * Gets the sites group.  All members of the site are contained within this group.
     * 
     * @param shortName     site short name
     * @return String       group name
     */
    String getSiteGroup(String shortName);
    
    /**
     * Gets the sites role group.  All members assigned the given role will be memebers of 
     * the returned group.
     * 
     * @param shortName     site short name
     * @param role          membership role
     * @return String       group name
     */
    String getSiteRoleGroup(String shortName, String role);
    
}
