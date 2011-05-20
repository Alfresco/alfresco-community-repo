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
package org.alfresco.service.cmr.site;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Site service fundamental API.
 * <p>
 * This service API is designed to support the public facing Site APIs
 * 
 * @author Roy Wetherall
 */
@PublicService
public interface SiteService
{
    /**
     * Create a new site.
     * 
     * @param sitePreset    site preset name
     * @param shortName     site short name, must be unique
     * @param title         site title
     * @param description   site description
     * @param isPublic      whether the site is public or not (true = public, false = private)
     * @return SiteInfo     information about the created site
     * @deprecated          since version 3.2, replaced by {@link #createSite(String, String, String, String, SiteVisibility)}
     */
    @Auditable(parameters = {"sitePreset", "shortName"})
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, boolean isPublic);
    
    /**
     * Create a new site.
     * 
     * @param sitePreset    site preset name
     * @param shortName     site short name, must be unique
     * @param title         site title
     * @param description   site description
     * @param visibility    site visibility (public|moderated|private)
     * @return SiteInfo     information about the created site
     */
    @Auditable(parameters = {"sitePreset", "shortName"})
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, SiteVisibility visibility);
    
    /**
     * This method checks if the currently authenticated user has permission to create sites.
     * 
     * @return <code>true</code> if current user can create sites, else <code>false</code>.
     * @since 3.4
     */
    @NotAuditable
    boolean hasCreateSitePermissions();
    
    /**
     * List the available sites.  This list can optionally be filtered by site name and/or site preset.
     * 
     * @param nameFilter            name filter
     * @param sitePresetFilter      site preset filter
     * @param size                  list maximum size or zero for all
     * @return List<SiteInfo>       list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String nameFilter, String sitePresetFilter, int size);
    
    /**
     * List the available sites.  This list can optionally be filtered by site name and/or site preset.
     * 
     * @param nameFilter            name filter
     * @param sitePresetFilter      site preset filter
     * @return List<SiteInfo>       list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String nameFilter, String sitePresetFilter);
    
    /**
     * List all the sites that the specified user has a explicit membership to.
     *
     * @param userName          user name
     * @return List<SiteInfo>   list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String userName);
    
    /**
     * Gets site information based on the short name of a site.
     * <p>
     * Returns null if the site can not be found.
     * 
     * @param shortName     the site short name
     * @return SiteInfo     the site information
     */
    @NotAuditable
    SiteInfo getSite(String shortName);
    
    /**
     * This method gets the {@link SiteInfo} for the Share Site which contains the given NodeRef.
     * If the given NodeRef is not contained within a Share Site, then <code>null</code> is returned.
     * 
     * @param nodeRef   the node whose containing site's info is to be found.
     * @return SiteInfo  site information for the containing site or <code>null</code> if node is not in a site.
     */
    @NotAuditable
    SiteInfo getSite(NodeRef nodeRef);
    
    /**
     * Update the site information.
     * <P>
     * Note that the short name and site preset of a site can not be updated once the site has been created.
     * 
     * @param siteInfo  site information
     */
    @Auditable
    void updateSite(SiteInfo siteInfo);
    
    /**
     * Delete the site.
     * 
     * @param shortName     site short name
     */
    @Auditable(parameters = {"shortName"})
    void deleteSite(String shortName);
    
    /**
     * List the members of the site.  This includes both users and groups.
     * <p>
     * Name and role filters are optional and if not specified all the members of the site are returned.
     * 
     * @param shortName     site short name
     * @param nameFilter    name filter
     * @param roleFilter    role filter
     * @param size          max results size crop if >0
     * @return Map<String, String>  the authority name and their role
     */
    @NotAuditable
    Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size);
    
    /**
     * List the members of the site.  This includes both users and groups if collapseGroups is set to false, otherwise all
     * groups that are members are collapsed into their component users and listed.
     * 
     * @param shortName         site short name
     * @param nameFilter        name filter
     * @param roleFilter        role filter
     * @param size          max results size crop if >0
     * @param collapseGroups    true if collapse member groups into user list, false otherwise
     * @return Map<String, String>  the authority name and their role
     */
    @NotAuditable
    Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size, boolean collapseGroups);
    
    /**
     * Gets the role of the specified user.
     * 
     * @param shortName     site short name
     * @param authorityName full authority name (so if it's a group then its prefixed with 'GROUP_')
     * @return String       site role, null if none
     */
    @NotAuditable
    String getMembersRole(String shortName, String authorityName);
    
    /**
     * Indicates whether an authority is a member of a site or not
     * 
     * @param shortName     site short name
     * @param authorityName authority name (so if it's a group then its prefixed with 'GROUP_')
     * @return boolean      true if the authority is a member of the site, false otherwise
     */
    @NotAuditable
    boolean isMember(String shortName, String authorityName);
    
    /**
     * Sets the role of an authority within a site
     * 
     * @param shortName     site short name
     * @param authorityName authority name (so if it's a group then its prefixed with 'GROUP_')
     * @param role          site role
     */
    @Auditable(parameters = {"shortName", "authorityName", "role"})
    void setMembership(String shortName, String authorityName, String role);
    
    /**
     * Clears an authorities role within a site
     * 
     * @param shortName     site short name
     * @param authorityName authority name (so if it's a group then its prefixed with 'GROUP_')
     */
    @Auditable(parameters = {"shortName", "authorityName"})
    void removeMembership(String shortName, String authorityName);
    
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
     * @return noderef of container or null if a container can't be created.
     */
    @NotAuditable
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
    @NotAuditable
    NodeRef getContainer(String shortName, String componentId);

    /**
     * Determines if a "container" folder for the specified component exists.
     * 
     * @param shortName  short name of site
     * @param componentId  component id
     * @return  true => "container" folder exists for component
     */
    @NotAuditable
    boolean hasContainer(String shortName, String componentId);
    
    /**
     * Gets a list of all the currently available roles that a user can perform on a site
     * 
     * @return  List<String>    list of available roles
     */
    @NotAuditable
    List<String> getSiteRoles();
     
    /**
     * Gets the sites group.  All members of the site are contained within this group.
     * 
     * @param shortName     site short name
     * @return String       group name
     */
    @NotAuditable
    String getSiteGroup(String shortName);
    
    /**
     * Gets the sites role group.  All members assigned the given role will be memebers of 
     * the returned group.
     * 
     * @param shortName     site short name
     * @param role          membership role
     * @return String       group name
     */
    @NotAuditable
    String getSiteRoleGroup(String shortName, String role);
    
    /**
     * This method cleans the permissions on the specified node. It is intended to be used after a
     * node is moved or copied from one site to another. Permissions relating to the former site are
     * removed and the node is given the default permissions for its new site.
     * 
     * @param relocatedNode
     * @param containingSite
     * @since 3.4.2
     */
    public void cleanSitePermissions(NodeRef relocatedNode, SiteInfo containingSite);
}
