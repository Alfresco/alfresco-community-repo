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
package org.alfresco.repo.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Site Service Implementation. Also bootstraps the site AVM and DM stores.
 * 
 * @author Roy Wetherall
 */
public class SiteServiceImpl implements SiteService, SiteModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(SiteServiceImpl.class);
    
    /** The DM store where site's are kept */
    public static final StoreRef SITE_STORE = new StoreRef("workspace://SpacesStore");
    
    /** Activiti tool */
    private static final String ACTIVITY_TOOL = "siteService";
    
    /** Services */
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private PermissionService permissionService;
    private ActivityService activityService;
    private PersonService personService;
    private AuthenticationComponent authenticationComponent;
    private TaggingService taggingService;
    private AuthorityService authorityService;
    
    /** The site root node reference */
    private NodeRef siteRootNodeRef;
    
    /**
     * Set node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set file folder service
     * 
     * @param fileFolderService     file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * Set search service
     * 
     * @param searchService     search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set permission service
     * 
     * @param permissionService     permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Set activity service
     * 
     * @param activityService   activity service
     */
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    /**
     * Set person service
     * 
     * @param personService     person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Set authentication component
     * 
     * @param authenticationComponent   authententication component
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    /**
     * Set the taggin service
     * 
     * @param taggingService    tagging service
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    /**
     * Set the authority service
     * 
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#createSite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public SiteInfo createSite(final String sitePreset, String passedShortName, final String title, final String description, final boolean isPublic)
    {
        // Remove spaces from shortName
        final String shortName = passedShortName.replaceAll(" ", "");
        
        // Check to see if we already have a site of this name
        NodeRef existingSite = getSiteNodeRef(shortName);
        if (existingSite != null)
        {
            // Throw an exception since we have a duplicate site name
            throw new AlfrescoRuntimeException("Unable to create site because the site short name '" + shortName + "' is already in use.  Site short names must be unique.");
        }
        
        // Get the site parent node reference
        NodeRef siteParent = getSiteParent(shortName);
        
        // Create the site node
        PropertyMap properties = new PropertyMap(4);
        properties.put(ContentModel.PROP_NAME, shortName);
        properties.put(SiteModel.PROP_SITE_PRESET, sitePreset);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        final NodeRef siteNodeRef = this.nodeService.createNode(
                siteParent, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, shortName), 
                SiteModel.TYPE_SITE,
                properties).getChildRef();
        
        // Make the new site a tag scope
        this.taggingService.addTagScope(siteNodeRef);
        
        // Clear the sites inherited permissions
        this.permissionService.setInheritParentPermissions(siteNodeRef, false);        
        
        // Get the current user
        final String currentUser = authenticationComponent.getCurrentUserName();
        
        // Create the relevant groups and assign permissions
        AuthenticationUtil.runAs(
            new AuthenticationUtil.RunAsWork<Object>()
            {
                public String doWork() throws Exception
                {
                    // Create the site's groups
                    String siteGroup = authorityService.createAuthority(AuthorityType.GROUP, null, getSiteGroup(shortName, false));
                    Set<String> permissions = permissionService.getSettablePermissions(SiteModel.TYPE_SITE);
                    for (String permission : permissions)
                    {
                        // Create a group for the permission
                        String permissionGroup = authorityService.createAuthority(AuthorityType.GROUP, siteGroup, getSiteRoleGroup(shortName, permission, false));
                        
                        // Assign the group the relevant permission on the site
                        permissionService.setPermission(siteNodeRef, permissionGroup, permission, true);
                    }
                    
                    // Set the memberhips details
                    //    - give all authorities read permissions if site is public
                    //    - give all authorities read permission on permissions so memberships can be calculated
                    //    - add the current user to the site manager group
                    if (isPublic == true)
                    {
                        permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
                    }
                    permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ_PERMISSIONS, true);
                    authorityService.addAuthority(getSiteRoleGroup(shortName, SiteModel.SITE_MANAGER, true), currentUser);                  
                    
                    // Return nothing
                    return null;
                }
                    
            }, AuthenticationUtil.getSystemUserName());               
           
        // Return created site information
        SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic, siteNodeRef);
        return siteInfo;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#getSiteGroup(java.lang.String)
     */
    public String getSiteGroup(String shortName)
    {
        return getSiteGroup(shortName, true);
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#getSiteRoleGroup(java.lang.String, java.lang.String)
     */
    public String getSiteRoleGroup(String shortName, String role)
    {
        return getSiteRoleGroup(shortName, role, true);
    }
    
    /**
     * Helper method to get the name of the site group 
     * 
     * @param shortName     site short name
     * @return String       site group name
     */
    public String getSiteGroup(String shortName, boolean withGroupPrefix)
    {
        StringBuffer sb = new StringBuffer(64);
        if (withGroupPrefix == true)
        {
            sb.append(PermissionService.GROUP_PREFIX);
        }
        sb.append("site_");
        sb.append(shortName);
        return sb.toString();
    }
    
    /**
     * Helper method to get the name of the site permission group
     * 
     * @param shortName     site short name
     * @param permission    permission name
     * @return String       site permission group name
     */
    public String getSiteRoleGroup(String shortName, String permission, boolean withGroupPrefix)
    {
        return getSiteGroup(shortName, withGroupPrefix) + "_" + permission;
    }
    
    /**
     * Gets a sites parent folder based on it's short name
     * ]
     * @param shortName     site short name
     * @return NodeRef      the site's parent
     */
    private NodeRef getSiteParent(String shortName)
    {
        // TODO
        // For now just return the site root, later we may build folder structure based on the shortname to
        // spread the sites about
        return getSiteRoot();
    }
    
    /**
     * Get the node reference that is the site root
     * 
     * @return  NodeRef     node reference
     */
    private NodeRef getSiteRoot()
    {
        if (this.siteRootNodeRef == null)
        {
            // Get the root 'sites' folder
            ResultSet resultSet = this.searchService.query(SITE_STORE, SearchService.LANGUAGE_LUCENE, "TYPE:\"st:sites\"");
            if (resultSet.length() == 0)
            {
                // No root site folder exists
                throw new AlfrescoRuntimeException("No root sites folder exists");
            }
            else if (resultSet.length() != 1)
            {
                // More than one root site folder exits
                throw new AlfrescoRuntimeException("More than one root sites folder exists");
            }        
         
            this.siteRootNodeRef = resultSet.getNodeRef(0);
        }
        
        return this.siteRootNodeRef;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#listSites(java.lang.String, java.lang.String)
     */
    public List<SiteInfo> listSites(String nameFilter, String sitePresetFilter)
    {
        // TODO 
        // - take into consideration the filters set
        // - take into consideration that the sites may not just be in a flat list under the site root
        
        // TODO
        // For now just return the list of sites present under the site root
        NodeRef siteRoot = getSiteRoot();
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(siteRoot, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        List<SiteInfo> result = new ArrayList<SiteInfo>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            result.add(createSiteInfo(assoc.getChildRef()));
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#listSites(java.lang.String)
     */
    public List<SiteInfo> listSites(String userName)
    {        
        List<SiteInfo> sites = listSites(null, null);
        List<SiteInfo> result = new ArrayList<SiteInfo>(sites.size());
        for (SiteInfo site : sites)
        {
            if (isMember(site.getShortName(), userName) == true)
            {
                result.add(site);
            }
        }
        return result;
    }
  
    /**
     * Creates a site informatoin object given a site node reference
     * 
     * @param siteNodeRef   site node reference
     * @return SiteInfo     site information object
     */
    private SiteInfo createSiteInfo(NodeRef siteNodeRef)
    {
        // Get the properties
        Map<QName, Serializable> properties = this.nodeService.getProperties(siteNodeRef);
        String shortName = (String)properties.get(ContentModel.PROP_NAME);
        String sitePreset = (String)properties.get(PROP_SITE_PRESET);
        String title = (String)properties.get(ContentModel.PROP_TITLE);
        String description = (String)properties.get(ContentModel.PROP_DESCRIPTION);
        
        // Determine whether the space is public or not
        boolean isPublic = isSitePublic(siteNodeRef);
        
        // Create and return the site information
        SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic, siteNodeRef);
        return siteInfo;
    }   
    
    /**
     * Indicates whether a site is public or not
     * 
     * @param siteNodeRef       site node reference
     * @return boolean          true if the site is public, false otherwise
     */
    private boolean isSitePublic(NodeRef siteNodeRef)
    {
        boolean isPublic = false;
        Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(siteNodeRef);
        for (AccessPermission permission : permissions)
        {
            if (permission.getAuthority().equals(PermissionService.ALL_AUTHORITIES) == true &&
                permission.getPermission().equals(SITE_CONSUMER) == true)
            {
                isPublic = true;
                break;
            }                
        }
        return isPublic;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#getSite(java.lang.String)
     */
    public SiteInfo getSite(String shortName)
    {
        SiteInfo result = null;
        
        // Get the site node
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef != null)
        {
            // Create the site info
            result = createSiteInfo(siteNodeRef);
        }
        
        // Return the site information
        return result;
    }
    
    /**
     * Gets the site's node reference based on its short name
     * 
     * @param shortName     short name
     * @return NodeRef      node reference
     */
    private NodeRef getSiteNodeRef(String shortName)
    {
        NodeRef result = null;        
        NodeRef siteRoot = getSiteParent(shortName);        
        List<ChildAssociationRef> assoc = this.nodeService.getChildAssocs(
                siteRoot,
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, shortName));
        if (assoc.size() == 1)
        {
            result = assoc.get(0).getChildRef();
        }
        return result;
    }

    /**
     * @see org.alfresco.repo.site.SiteService#updateSite(org.alfresco.repo.site.SiteInfo)
     */
    public void updateSite(SiteInfo siteInfo)
    {
        NodeRef siteNodeRef = getSiteNodeRef(siteInfo.getShortName());
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not update site " + siteInfo.getShortName() + " because it does not exist.");
        }
        
        // Note: the site preset and short name can not be updated
        
        // Update the properties of the site
        Map<QName, Serializable> properties = this.nodeService.getProperties(siteNodeRef);
        properties.put(ContentModel.PROP_TITLE, siteInfo.getTitle());
        properties.put(ContentModel.PROP_DESCRIPTION, siteInfo.getDescription());
        this.nodeService.setProperties(siteNodeRef, properties);
        
        // Update the isPublic flag
        boolean isPublic = isSitePublic(siteNodeRef);
        if (isPublic != siteInfo.getIsPublic());
        {
            if (siteInfo.getIsPublic() == true)
            {
                // Add the permission
                this.permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
            }
            else
            {
                // Remove the permission
                this.permissionService.deletePermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER);
            }
        }        
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#deleteSite(java.lang.String)
     */
    public void deleteSite(final String shortName)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not delete site " + shortName + " because it does not exist.");
        }
        
        // Delete the node
        this.nodeService.deleteNode(siteNodeRef);

        // Delete the associatated group's
        AuthenticationUtil.runAs(
            new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    authorityService.deleteAuthority(getSiteGroup(shortName, true));
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#listMembers(java.lang.String, java.lang.String, java.lang.String)
     */
    public Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }
        
        Map<String, String> members = new HashMap<String, String>(23);
        
        Set<String> permissions = permissionService.getSettablePermissions(SiteModel.TYPE_SITE);
        for (String permission : permissions)
        {
            String groupName = getSiteRoleGroup(shortName, permission, true);
            Set<String> users = this.authorityService.getContainedAuthorities(AuthorityType.USER, groupName, true);
            for (String user : users)
            {
                // Add the user and their permission to the returned map
                members.put(user, permission);
            }
        }        
        
        return members;
    }

    /**
     * @see org.alfresco.repo.site.SiteService#getMembersRole(java.lang.String, java.lang.String)
     */
    public String getMembersRole(String shortName, String userName)
    {
        String result = null;
        String group = getPermissionGroup(shortName, userName);
        if (group != null)
        {
            int index = group.lastIndexOf('_');
            if (index != -1)
            {
                result = group.substring(index+1);
            }
        }
        return result;
    }
    
    /**
     * Helper method to get the permission group for a given user on a site.
     * Returns null if the user does not have a explicit membership to the site.
     * 
     * @param siteShortName     site short name
     * @param userName          user name
     * @return String           permission group, null if no explicit membership set
     */
    private String getPermissionGroup(String siteShortName, String userName)
    {
        String result = null;
        Set<String> groups = this.authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, true);
        for (String group : groups)
        {
            if (group.startsWith(PermissionService.GROUP_PREFIX + "site_" + siteShortName) == true)
            {
                result = group;
                break;
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.repo.site.SiteService#isMember(java.lang.String, java.lang.String)
     */
    public boolean isMember(String shortName, String userName)
    {
        return (getPermissionGroup(shortName, userName) != null);
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#removeMembership(java.lang.String, java.lang.String)
     */
    public void removeMembership(final String shortName, final String userName)
    {
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }
     
        // TODO what do we do about the user if they are in a group that has rights to the site?        
        
        // Determine whether the site is private or not
        boolean isPublic = isSitePublic(siteNodeRef);
        
        // Get the current user
        String currentUserName = AuthenticationUtil.getCurrentUserName();
        String currentUserRole = getMembersRole(shortName, currentUserName);
        
        // Get the user current role 
        final String role = getMembersRole(shortName, userName);        
        if (role != null)
        {       
            // Check that we are not about to remove the last site manager
            if (SiteModel.SITE_MANAGER.equals(role) == true)
            {
                Set<String> siteMangers = this.authorityService.getContainedAuthorities(
                        AuthorityType.USER, 
                        getSiteRoleGroup(shortName, SITE_MANAGER, true), 
                        true);
                if (siteMangers.size() == 1)
                {
                    throw new AlfrescoRuntimeException("A site requires at least one site manager.  You can not remove '" + userName + "' from the site memebership because they are currently the only site manager.");
                }
            }
            
            // If ...
            //  -- the current user is a site manager
            // or
            //  -- the site is public and
            //  -- the user is ourselves and
            //  -- the users current role is consumer
            if ((currentUserRole != null &&
                 SiteModel.SITE_MANAGER.equals(currentUserRole) == true)
                || 
                (isPublic == true &&
                 currentUserName.equals(userName) == true &&
                 role.equals(SiteModel.SITE_CONSUMER) == true))
            {
                // Run as system user
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // Remove the user from the current permission group
                        String currentGroup = getSiteRoleGroup(shortName, role, true);
                        authorityService.removeAuthority(currentGroup, userName);
                        
                        return null;
                    }
                }, AuthenticationUtil.SYSTEM_USER_NAME);
                
                // Raise events
                if (AuthorityType.getAuthorityType(userName) == AuthorityType.USER)
                {
                    activityService.postActivity(ActivityType.SITE_USER_REMOVED, shortName, ACTIVITY_TOOL, getActivityData(userName, ""));
                }
                else
                {
                    // TODO - update this, if sites support groups
                    logger.error("setMembership - failed to post activity: unexpected authority type: " + AuthorityType.getAuthorityType(userName));
                }
            }
            else
            {
                // Throw a permission exception
                throw new AlfrescoRuntimeException("Access denied, user does not have permissions to delete membership details of the site '" + shortName + "'");
            }
        }        
    }

    /**
     * @see org.alfresco.repo.site.SiteService#setMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setMembership(final String shortName, final String userName, final String role)
    {
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }
        
        // Get the user's current role
        final String currentRole = getMembersRole(shortName, userName);
        
        if (currentRole == null || role.equals(currentRole) == false)
        {        
            // Determine whether the site is private or not
            boolean isPublic = isSitePublic(siteNodeRef);
            
            // TODO if this is the only site manager do not downgrade their permissions
            
            // If we are ...
            // -- the site manager
            // or we are ...
            // -- refering to a public site and
            // -- the role being set is consumer and
            // -- the user being added is ourselves and
            // -- the member does not already have permissions 
            // ... then we can set the permissions as system user
            final String currentUserName = AuthenticationUtil.getCurrentUserName();
            final String currentUserRole = getMembersRole(shortName, currentUserName);
            if  ((currentUserRole != null && 
                  SiteModel.SITE_MANAGER.equals(currentUserRole) == true) 
                  ||
                 (isPublic == true &&
                  role.equals(SiteModel.SITE_CONSUMER) == true &&
                  userName.equals(currentUserName) == true &&
                  currentRole == null))
            {
                // Check that we are not about to remove the last site manager
                if (SiteModel.SITE_MANAGER.equals(currentRole) == true)
                {
                    Set<String> siteMangers = this.authorityService.getContainedAuthorities(
                            AuthorityType.USER, 
                            getSiteRoleGroup(shortName, SITE_MANAGER, true), 
                            true);
                    if (siteMangers.size() == 1)
                    {
                        throw new AlfrescoRuntimeException("A site requires at least one site manager.  You can not change '" + userName + "' role from the site memebership because they are currently the only site manager.");
                    }
                }
                
                // Run as system user
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        if (currentRole != null)
                        {
                            // Remove the user from the current permission group
                            String currentGroup = getSiteRoleGroup(shortName, currentRole, true);
                            authorityService.removeAuthority(currentGroup, userName);
                        }
                        
                        // Add the user to the new permission group
                        String newGroup = getSiteRoleGroup(shortName, role, true);
                        authorityService.addAuthority(newGroup, userName);
                        
                        return null;
                    }
                        
                }, AuthenticationUtil.SYSTEM_USER_NAME);
                
                if (currentRole == null)
                {
                    if (AuthorityType.getAuthorityType(userName) == AuthorityType.USER)
                    {
                        activityService.postActivity(ActivityType.SITE_USER_JOINED, shortName, ACTIVITY_TOOL, getActivityData(userName, role));
                    }
                    else
                    {
                        // TODO - update this, if sites support groups
                        logger.error("setMembership - failed to post activity: unexpected authority type: " + AuthorityType.getAuthorityType(userName));
                    }
                }
                else
                {
                    if (AuthorityType.getAuthorityType(userName) == AuthorityType.USER)
                    {
                        activityService.postActivity(ActivityType.SITE_USER_ROLE_UPDATE, shortName, ACTIVITY_TOOL, getActivityData(userName, role));
                    }
                    else
                    {
                        // TODO - update this, if sites support groups
                        logger.error("setMembership - failed to post activity: unexpected authority type: " + AuthorityType.getAuthorityType(userName));
                    }
                }                
            }
            else
            {        
                // Raise a permission exception
                throw new AlfrescoRuntimeException("Access denied, user does not have permissions to modify membership details of the site '" + shortName + "'");
            }
        }
    }    

    /**
     * @see org.alfresco.repo.site.SiteService#createContainer(java.lang.String, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public NodeRef createContainer(String shortName, String componentId, QName containerType, Map<QName, Serializable> containerProperties)
    {
        // Check for the component id
        if (componentId == null || componentId.length() ==0)
        {
            throw new AlfrescoRuntimeException("Component id not provided");
        }
    
        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }

        // retrieve component folder within site
        NodeRef containerNodeRef = null;
        try
        {
            containerNodeRef = findContainer(siteNodeRef, componentId);            
        }
        catch(FileNotFoundException e)
        {
        }
        
        // create the container node reference
        if (containerNodeRef == null)
        {
            if (containerType == null)
            {
                containerType = ContentModel.TYPE_FOLDER;
            }
            
            // create component folder
            FileInfo fileInfo = fileFolderService.create(siteNodeRef, componentId, containerType);
            
            // Get the created container 
            containerNodeRef = fileInfo.getNodeRef();
            
            // Set the properties if they have been provided
            if (containerProperties != null)
            {
                Map<QName, Serializable> props = this.nodeService.getProperties(containerNodeRef);
                props.putAll(containerProperties);
                this.nodeService.setProperties(containerNodeRef, props);
            }
            
            // Add the container aspect
            Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(1);
            aspectProps.put(SiteModel.PROP_COMPONENT_ID, componentId);
            this.nodeService.addAspect(containerNodeRef, ASPECT_SITE_CONTAINER, aspectProps);
            
            // Make the container a tag scope
            this.taggingService.addTagScope(containerNodeRef);
        }
       
        return containerNodeRef;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#getContainer(java.lang.String)
     */
    public NodeRef getContainer(String shortName, String componentId)
    {
        if (componentId == null || componentId.length() ==0)
        {
        	throw new AlfrescoRuntimeException("Component id not provided");
        }
        
        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }

        // retrieve component folder within site
        // NOTE: component id is used for folder name
        NodeRef containerNodeRef = null;
        try
        {
        	containerNodeRef = findContainer(siteNodeRef, componentId);
        }
        catch(FileNotFoundException e)
        {        	
        }
        
        return containerNodeRef;
    }

    /**
     * @see org.alfresco.repo.site.SiteService#hasContainer(java.lang.String)
     */
    public boolean hasContainer(String shortName, String componentId)
    {
        if (componentId == null || componentId.length() ==0)
        {
        	throw new AlfrescoRuntimeException("Component id not provided");
        }
        
        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }

        // retrieve component folder within site
        // NOTE: component id is used for folder name
        boolean hasContainer = false;
        try
        {
        	findContainer(siteNodeRef, componentId);
        	hasContainer = true;
        }
        catch(FileNotFoundException e)
        {
        }
        
        return hasContainer;
    }
    
    /**
     * Locate site "container" folder for component
     * 
     * @param siteNodeRef  site
     * @param componentId  component id
     * @return  "container" node ref, if it exists
     * @throws FileNotFoundException
     */
    private NodeRef findContainer(NodeRef siteNodeRef, String componentId)
    	throws FileNotFoundException
    {
        List<String> paths = new ArrayList<String>(1);
        paths.add(componentId);
        FileInfo fileInfo = fileFolderService.resolveNamePath(siteNodeRef, paths);
    	if (!fileInfo.isFolder())
    	{
    		throw new AlfrescoRuntimeException("Site container " + fileInfo.getName() + " does not refer to a folder ");
    	}
        return fileInfo.getNodeRef();
    }
    
    private String getActivityData(String userName, String role)
    {
        String memberFN = "";
        String memberLN = "";
        NodeRef person = personService.getPerson(userName);
        if (person != null)
        {
            memberFN = (String)nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
            memberLN = (String)nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
        }
        
        try
        {
            JSONObject activityData = new JSONObject();
            activityData.put("role", role);
            activityData.put("memberFirstName", memberFN);
            activityData.put("memberLastName", memberLN);
            return activityData.toString();
        }
        catch (JSONException je)
        {
            // log error, subsume exception
            logger.error("Failed to get activity data: " + je);
            return "";
        }
    }
}
