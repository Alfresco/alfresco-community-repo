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
package org.alfresco.service.cmr.site;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.site.SiteGroupMembership;
import org.alfresco.repo.site.SiteMembership;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Site service fundamental API.
 * <p>
 * This service API is designed to support the public facing Site APIs
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface SiteService
{
    static String DOCUMENT_LIBRARY = "documentLibrary";

    public enum SortFields
    {
        LastName, FirstName, Role, SiteShortName, SiteTitle, Username, DisplayName
    };

    public interface SiteMembersCallback
    {

        /**
         * @deprecated from 7.0.0 onwards, use #siteMember(String, String, boolean) instead A site member along with his/her Site permission
         */
        public void siteMember(String authority, String permission);

        /* A site member along with his/her Site permission */
        public default void siteMember(String authority, String permission, boolean isMemberOfGroup)
        {
            siteMember(authority, permission);
        }

        /**
         * Return true to break out of the loop early.
         * 
         * @return boolean
         */
        public boolean isDone();
    }

    /**
     * Create a new site.
     * 
     * @param sitePreset
     *            site preset name
     * @param shortName
     *            site short name, must be unique
     * @param title
     *            site title
     * @param description
     *            site description
     * @param isPublic
     *            whether the site is public or not (true = public, false = private)
     * @return SiteInfo information about the created site
     * @deprecated since version 3.2, replaced by {@link #createSite(String, String, String, String, SiteVisibility)}
     */
    @Auditable(parameters = {"sitePreset", "shortName"})
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, boolean isPublic);

    /**
     * Can the current user add the authority "authorityName" to the site "shortName" with role "role"?
     * 
     * @param shortName
     *            site short name, must be unique
     * @param authorityName
     *            authority to add
     * @param role
     *            site role
     *
     * @return true if the current user can add the authority to the site, false otherwise
     */
    @NotAuditable
    boolean canAddMember(String shortName, String authorityName, String role);

    /**
     * Create a new site.
     * 
     * @param sitePreset
     *            site preset name
     * @param shortName
     *            site short name, must be unique
     * @param title
     *            site title
     * @param description
     *            site description
     * @param visibility
     *            site visibility (public|moderated|private)
     * @return SiteInfo information about the created site
     */
    @Auditable(parameters = {"sitePreset", "shortName"})
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, SiteVisibility visibility);

    /**
     * Create a new site.
     * 
     * @param sitePreset
     *            site preset name
     * @param shortName
     *            site short name, must be unique
     * @param title
     *            site title
     * @param description
     *            site description
     * @param visibility
     *            site visibility (public|moderated|private)
     * @param siteType
     *            type of site to create, must be a sub-type of st:site
     * @return SiteInfo information about the created site
     */
    @Auditable(parameters = {"sitePreset", "shortName"})
    SiteInfo createSite(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, QName siteType);

    /**
     * This method checks if the currently authenticated user has permission to create sites.
     * 
     * @return <code>true</code> if current user can create sites, else <code>false</code>.
     * @since 3.4
     */
    @NotAuditable
    boolean hasCreateSitePermissions();

    /**
     * This method will find all {@link SiteInfo sites} available to the currently authenticated user based on the specified site filter, site preset filter and result set size. The filter parameter will match any sites whose {@link ContentModel#PROP_NAME cm:name}, {@link ContentModel#PROP_TITLE cm:title} or {@link ContentModel#PROP_DESCRIPTION cm:description} <i>contain</i> the specified string (ignoring case).
     * <p/>
     * Note that this method uses <a href="http://wiki.alfresco.com/wiki/Search">Alfresco Full Text Search</a> to retrieve results and depending on server Lucene, SOLR configuration may only offer eventually consistent results.
     * 
     * @param filter
     *            Any supplied filter will be wrapped in asterisks (e.g. '*foo*') and used to match the sites' cm:name, cm:title or cm:description.
     * @param sitePresetFilter
     *            a site preset filter name to match against.
     * @param size
     *            this parameter specifies a maximum result set size.
     * @return Site objects for all matching sites up to the maximum result size.
     * 
     * @since 4.0
     */
    @NotAuditable
    List<SiteInfo> findSites(String filter, String sitePresetFilter, int size);

    /**
     * This method will find all {@link SiteInfo sites} available to the currently authenticated user based on the specified site filter and result set size. The filter parameter will match any sites whose {@link ContentModel#PROP_NAME cm:name}, {@link ContentModel#PROP_TITLE cm:title} or {@link ContentModel#PROP_DESCRIPTION cm:description} <i>contain</i> the specified string (ignoring case).
     * <p/>
     * Note that this method uses <a href="http://wiki.alfresco.com/wiki/Search">Alfresco Full Text Search</a> to retrieve results and depending on server Lucene, SOLR configuration may only offer eventually consistent results.
     * 
     * @param filter
     *            Any supplied filter will be wrapped in asterisks (e.g. 'foo*') and used to match the sites' cm:name, cm:title or cm:description.
     * @param size
     *            this parameter specifies a maximum result set size.
     * @return Site objects for all matching sites up to the maximum result size.
     * 
     * @since 5.0
     */
    @NotAuditable
    List<SiteInfo> findSites(String filter, int size);

    /**
     * List the available sites. This list can optionally be filtered by site name/title/description and/or site preset.
     * <p/>
     * Note: Starting with Alfresco 4.0, the filter parameter will only match sites whose {@link ContentModel#PROP_NAME cm:name} or {@link ContentModel#PROP_TITLE cm:title} or {@link ContentModel#PROP_DESCRIPTION cm:description} <i>start with</i> the specified string (ignoring case). The listing of sites whose cm:names (or titles or descriptions) <i>contain</i> the specified string is no longer supported. To retrieve sites whose cm:names etc contain a substring, {@link SiteService#findSites(String, String, int)} should be used instead.
     * <p/>
     * <b>THIS METHOD CAN RETURN INCOMPLETE RESULTS WHILE CACHES CATCH UP WITH REALITY</b> (<a href=https://issues.alfresco.com/jira/browse/ACE-196>BM-0012: Run v420b1494_01: (CMIS) GetSites is Slow</a>).
     * 
     * @param filter
     *            filter (sites whose cm:name, cm:title or cm:description START WITH filter)
     * @param sitePresetFilter
     *            site preset filter (sites whose preset EQUALS sitePresetFilter)
     * @param size
     *            list maximum size or zero for all
     * @return list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String filter, String sitePresetFilter, int size);

    /**
     * List the available sites. This list can optionally be filtered by site name/title/description and/or site preset.
     * <p/>
     * Note: Starting with Alfresco 4.0, the filter parameter will only match sites whose {@link ContentModel#PROP_NAME cm:name} or {@link ContentModel#PROP_TITLE cm:title} or {@link ContentModel#PROP_DESCRIPTION cm:description} <i>start with</i> the specified string (ignoring case). The listing of sites whose cm:names (or titles or descriptions) <i>contain</i> the specified string is no longer supported. To retrieve sites whose cm:names etc contain a substring, {@link SiteService#findSites(String, String, int)} should be used instead.
     * 
     * @param filter
     *            filter
     * @param sitePresetFilter
     *            site preset filter
     * @return list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String filter, String sitePresetFilter);

    /**
     * List all the sites that the specified user has a explicit membership to.
     *
     * @param userName
     *            user name
     * @return list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String userName);

    /**
     * This method returns {@link PagingResults paged result sets} of {@link SiteInfo} objects, which should be more efficient than the unpaged methods also available on this interface. It is also guaranteed to return fully consistent results.
     * 
     * @param filterProps
     *            property filters
     * @param sortProps
     *            sorting options
     * @param pagingRequest
     *            paging options
     * 
     * @return a page of SiteInfo objects.
     * @since 4.0
     */
    @NotAuditable
    PagingResults<SiteInfo> listSites(List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    /**
     * Lists all the memberships in sites that the specified user is in.
     * 
     * @param userName
     *            String
     * @param size
     *            list maximum size or zero for all
     * @return a list of SiteMembership objects
     */
    @NotAuditable
    List<SiteMembership> listSiteMemberships(String userName, int size);

    /**
     * List all the sites that the specified user has a explicit membership to.
     *
     * @param userName
     *            user name
     * @param size
     *            list maximum size or zero for all
     * @return list of site information
     */
    @NotAuditable
    List<SiteInfo> listSites(String userName, int size);

    /**
     * Gets site information based on the short name of a site.
     * <p>
     * Returns null if the site can not be found.
     * 
     * @param shortName
     *            the site short name
     * @return SiteInfo the site information
     */
    @NotAuditable
    SiteInfo getSite(String shortName);

    /**
     * This method gets the {@link SiteInfo} for the Share Site which contains the given NodeRef. If the given NodeRef is not contained within a Share Site, then <code>null</code> is returned.
     * 
     * @param nodeRef
     *            the node whose containing site's info is to be found.
     * @return SiteInfo site information for the containing site or <code>null</code> if node is not in a site.
     */
    @NotAuditable
    SiteInfo getSite(NodeRef nodeRef);

    /**
     * This method gets the shortName for the Share Site which contains the given NodeRef. If the given NodeRef is not contained within a Share Site, then <code>null</code> is returned.
     * 
     * @param nodeRef
     *            the node whose containing site's info is to be found.
     * @return String site short name for the containing site or <code>null</code> if node is not in a site.
     */
    @NotAuditable
    String getSiteShortName(NodeRef nodeRef);

    /**
     * Returns true if the site exists. This allows create scripts to confirm the existence of private sites - they would not normally be returned from getSite() if the user does not have permission on the site noderef.
     * 
     * @param shortName
     *            the site short name
     * @return true if the site exists, false otherwise
     */
    @NotAuditable
    boolean hasSite(String shortName);

    /**
     * Update the site information.
     * <P>
     * Note that the short name and site preset of a site can not be updated once the site has been created.
     * 
     * @param siteInfo
     *            site information
     */
    @Auditable
    void updateSite(SiteInfo siteInfo);

    /**
     * Delete the site.
     * 
     * @param shortName
     *            site short name
     */
    @Auditable(parameters = {"shortName"})
    void deleteSite(String shortName);

    /**
     * @deprecated from 7.0.0, use #listMembers(String, String, String, boolean, boolean, boolean, SiteMembersCallback) instead List the members of the site. This includes both users and groups.
     *             <p>
     *             Name and role filters are optional and if not specified all the members of the site are returned.
     * 
     * @param shortName
     *            site short name
     * @param nameFilter
     *            name filter
     * @param roleFilter
     *            role filter
     * @param collapseGroups
     *            true if includes group member into user list, false otherwise
     * @param callback
     *            callback
     */
    @NotAuditable
    void listMembers(String shortName, final String nameFilter, final String roleFilter, boolean collapseGroups, SiteMembersCallback callback);

    /**
     * List the members of the site. This includes both users and groups. Users and groups can be controlled by passing params
     * <p>
     * Name and role filters are optional and if not specified all the members of the site are returned.
     *
     * @param shortName
     *            site short name
     * @param nameFilter
     *            name filter
     * @param roleFilter
     *            role filter
     * @param includeUsers
     *            includes the users
     * @param includeGroups
     *            include the groups
     * @param expandGroups
     *            true if expand group member into user list, false otherwise
     * @param callback
     *            callback
     */
    @NotAuditable
    void listMembers(String shortName, final String nameFilter, final String roleFilter, final boolean includeUsers, final boolean includeGroups, final boolean expandGroups, SiteMembersCallback callback);

    /**
     * List the members of the site. This includes both users and groups.
     * <p>
     * Name and role filters are optional and if not specified all the members of the site are returned.
     * 
     * @param shortName
     *            site short name
     * @param nameFilter
     *            name filter
     * @param roleFilter
     *            role filter
     * @param size
     *            max results size crop if >0
     * @return the authority name and their role
     */
    @NotAuditable
    Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size);

    /**
     * List the members of the site. This includes both users and groups if collapseGroups is set to false, otherwise all groups that are members are collapsed into their component users and listed.
     * 
     * @param shortName
     *            site short name
     * @param nameFilter
     *            name filter
     * @param roleFilter
     *            role filter
     * @param size
     *            max results size crop if >0
     * @param collapseGroups
     *            true if includes group member into user list, false otherwise
     * @return the authority name and their role
     */
    @NotAuditable
    Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size, boolean collapseGroups);

    /**
     * List the members of the site. This includes both users and groups if collapseGroups is set to false, otherwise all groups that are members are collapsed into their component users and listed.
     * 
     * 
     * @param shortName
     *            site short name
     * @param nameFilter
     *            name filter
     * @param roleFilter
     *            role filter
     * @param size
     *            max results size crop if >0
     * @param collapseGroups
     *            true if includes group member into user list, false otherwise
     * @return List of site authorities’ information objects
     */
    @NotAuditable
    List<SiteMemberInfo> listMembersInfo(String shortName, String nameFilter, String roleFilter, int size, boolean collapseGroups);

    /**
     * Gets the role of the specified user. Returns a paged list of the members of the site. This includes both users and groups if collapseGroups is set to false, otherwise all groups that are members are collapsed into their component users and listed.
     * 
     * @param shortName
     *            site short name
     * @param collapseGroups
     *            true if collapse member groups into user list, false otherwise
     * @param pagingRequest
     *            the paging request
     *
     * @return the authority name and their role
     */
    @NotAuditable
    PagingResults<SiteMembership> listMembersPaged(String shortName, boolean collapseGroups, List<Pair<SiteService.SortFields, Boolean>> sortProps, PagingRequest pagingRequest);

    /**
     * Gets the extended role information of the specified user.
     * 
     * @param shortName
     *            site short name
     * @param authorityName
     *            full authority name (so if it's a group then its prefixed with 'GROUP_')
     * @return SiteMemberInfo site role information, null if none
     */
    @NotAuditable
    SiteMemberInfo getMembersRoleInfo(String shortName, String authorityName);

    /**
     * Indicates whether an authority is a member of a site or not
     * 
     * @param shortName
     *            site short name
     * @param authorityName
     *            authority name (so if it's a group then its prefixed with 'GROUP_')
     * @return boolean true if the authority is a member of the site, false otherwise
     */
    @NotAuditable
    boolean isMember(String shortName, String authorityName);

    /**
     * Sets the role of an authority within a site
     * 
     * @param shortName
     *            site short name
     * @param authorityName
     *            authority name (so if it's a group then its prefixed with 'GROUP_')
     * @param role
     *            site role
     * @throws UnknownAuthorityException
     *             if the site role is not supported.
     */
    @Auditable(parameters = {"shortName", "authorityName", "role"})
    void setMembership(String shortName, String authorityName, String role);

    /**
     * Clears an authorities role within a site
     * 
     * @param shortName
     *            site short name
     * @param authorityName
     *            authority name (so if it's a group then its prefixed with 'GROUP_')
     */
    @Auditable(parameters = {"shortName", "authorityName"})
    void removeMembership(String shortName, String authorityName);

    /**
     * Creates a container for a component is a site of the given container type (must be a sub-type of st:siteContainer)
     * <p>
     * If no container type is specified then a node of type st:siteContainer is created.
     * <p>
     * The map of container properties are set on the created container node. Null can be provided when no properties need to be set.
     * 
     * @param shortName
     *            site short name
     * @param componentId
     *            component id
     * @param containerType
     *            container type to create (can be null)
     * @param containerProperties
     *            container property values (can be null)
     * @return noderef of container or null if a container can't be created.
     */
    @NotAuditable
    NodeRef createContainer(String shortName, String componentId, QName containerType, Map<QName, Serializable> containerProperties);

    /**
     * Gets the "container" folder for the specified component.
     *
     * @param shortName
     *            short name of site
     * @param componentId
     *            component id
     * @return noderef of container
     */
    @NotAuditable
    NodeRef getContainer(String shortName, String componentId);

    /**
     * Returns a paged list of top level containers for the site
     *
     * @param shortName
     *            short name of site
     * @param pagingRequest
     *            paging request
     * 
     * @return paged list of top level containers
     */
    @NotAuditable
    PagingResults<FileInfo> listContainers(String shortName, PagingRequest pagingRequest);

    /**
     * Determines if a "container" folder for the specified component exists.
     * 
     * @param shortName
     *            short name of site
     * @param componentId
     *            component id
     * @return true => "container" folder exists for component
     */
    @NotAuditable
    boolean hasContainer(String shortName, String componentId);

    /**
     * Gets a list of all the currently available roles that a user can perform on all sites
     * 
     * @return list of available roles
     */
    @NotAuditable
    List<String> getSiteRoles();

    /**
     * Gets a list of all the currently available roles that a user can perform on a specific site. This will generally only differ from {@link #getSiteRoles()} if your site is of a custom type.
     * 
     * @return list of available roles
     */
    @NotAuditable
    List<String> getSiteRoles(String shortName);

    /**
     * Gets the sites group. All members of the site are contained within this group.
     * 
     * @param shortName
     *            site short name
     * @return String group name
     */
    @NotAuditable
    String getSiteGroup(String shortName);

    /**
     * Gets the sites role group. All members assigned the given role will be members of the returned group.
     * 
     * @param shortName
     *            site short name
     * @param role
     *            membership role
     * @return String group name
     */
    @NotAuditable
    String getSiteRoleGroup(String shortName, String role);

    /**
     * Gets the reference to the folder that is the Site root node.
     * 
     * @return site root node.
     */
    @NotAuditable
    NodeRef getSiteRoot();

    /**
     * This method cleans the permissions on the specified node. It is intended to be used after a node is moved or copied from one site to another. Permissions relating to the former site are removed and the node is given the default permissions for its new site.
     * 
     * @param relocatedNode
     *            NodeRef
     * @param containingSite
     *            SiteInfo
     * @since 3.4.2
     */
    public void cleanSitePermissions(NodeRef relocatedNode, SiteInfo containingSite);

    /**
     * List all the sites that the specified user has a explicit membership to.
     *
     * @param userName
     *            user name
     * @return paged list of site information
     */
    @NotAuditable
    PagingResults<SiteMembership> listSitesPaged(final String userName, List<Pair<SiteService.SortFields, Boolean>> sortProps, final PagingRequest pagingRequest);

    @NotAuditable
    String resolveSite(String group);

    @NotAuditable
    String getMembersRole(String shortName, String authorityName);

    @NotAuditable
    int countAuthoritiesWithRole(String shortName, String role);

    /**
     * Indicates whether the specified user is a site administrator or not.
     * <p>
     * Note: The super/repo admin is considered to be a site administrator too.
     * 
     * @param userName
     *            The user name
     * @return true if the specified user is a 'site administrator', false otherwise
     */
    @NotAuditable
    boolean isSiteAdmin(String userName);

    /**
     * Returns a paged list of the groups for the site.
     *
     * @param shortName
     *            site short name
     * @param sortProps
     *            sorting options
     * @param pagingRequest
     *            the paging request
     *
     * @return the authority name and their role
     */
    @NotAuditable
    PagingResults<SiteGroupMembership> listGroupMembersPaged(String shortName, List<Pair<SortFields, Boolean>> sortProps, PagingRequest pagingRequest);
}
