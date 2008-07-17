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
     * @see org.alfresco.repo.site.SiteService#createSite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public SiteInfo createSite(String sitePreset, String shortName, String title, String description, boolean isPublic)
    {
        /// TODO check for shortname duplicates
        
        // Get the site parent node reference
        NodeRef siteParent = getSiteParent(shortName);
        
        // Create the site node
        PropertyMap properties = new PropertyMap(4);
        properties.put(ContentModel.PROP_NAME, shortName);
        properties.put(SiteModel.PROP_SITE_PRESET, sitePreset);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        NodeRef siteNodeRef = this.nodeService.createNode(
                siteParent, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, shortName), 
                SiteModel.TYPE_SITE,
                properties).getChildRef();
        
        // Make the new site a tag scope
        this.taggingService.addTagScope(siteNodeRef);
        
       // Set the memberhips details
       //    - give all authorities read permissions if site is public
       //    - give all authorities read permission on permissions so memberships can be calculated
       //    - give current user role of site manager
       this.permissionService.setInheritParentPermissions(siteNodeRef, false);
       if (isPublic == true)
       {
           this.permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
       }
       this.permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ_PERMISSIONS, true);
       this.permissionService.setPermission(siteNodeRef, authenticationComponent.getCurrentUserName(), SiteModel.SITE_MANAGER, true);
        
       // Return created site information
       SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic, siteNodeRef);
       return siteInfo;
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
    
    private NodeRef getSiteRoot()
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
     
        return resultSet.getNodeRef(0);
    }
    
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
                
        String query = "+TYPE:\"st:site\" +@cm\\:name:\"" + shortName + "\"";
        ResultSet resultSet = this.searchService.query(SITE_STORE, SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.length() == 1)
        {
            result = resultSet.getNodeRef(0);
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
    public void deleteSite(String shortName)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not delete site " + shortName + " because it does not exist.");
        }
        
        this.nodeService.deleteNode(siteNodeRef);        
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
        Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(siteNodeRef);
        for (AccessPermission permission : permissions)
        {
            String authority = permission.getAuthority();      
            if (permission.getAuthority().startsWith(PermissionService.GROUP_PREFIX) == true)
            {
                // TODO .. collapse groups into users
            }                
            else
            {
                // Check to see if we already have an entry for the user in the map
                if (members.containsKey(authority) == true)
                {
                    // TODO .. we need to resolve the permission in the map to the 'highest'
                    //         for now do nothing as we shouldn't have more than on anyhow
                }
                else
                {
                    // Add the user and permission to the map
                    members.put(authority, permission.getPermission());
                }
            }
        }
        
        return members;
    }

    /**
     * @see org.alfresco.repo.site.SiteService#getMembersRole(java.lang.String, java.lang.String)
     */
    public String getMembersRole(String shortName, String userName)
    {
        Map<String, String> members = listMembers(shortName, null, null);
        return members.get(userName);
    }

    /**
     * @see org.alfresco.repo.site.SiteService#isMember(java.lang.String, java.lang.String)
     */
    public boolean isMember(String shortName, String userName)
    {
        return (getMembersRole(shortName, userName) != null);
    }
    
    /**
     * @see org.alfresco.repo.site.SiteService#removeMembership(java.lang.String, java.lang.String)
     */
    public void removeMembership(String shortName, String userName)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }
     
        // TODO what do we do about the user if they are in a group that has rights to the site?        
        // TODO do not remove the only site manager
        
        // Clear the permissions for the user 
        this.permissionService.clearPermission(siteNodeRef, userName);

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

    /**
     * @see org.alfresco.repo.site.SiteService#setMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setMembership(String shortName, String userName, String role)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Site " + shortName + " does not exist.");
        }
        
        boolean alreadyMember = false;
        Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(siteNodeRef);
        for (AccessPermission permission : permissions)
        {
            String authority = permission.getAuthority();
            if (authority.equals(userName))
            {
                alreadyMember = true;
                break;
            }
        }
        
        // TODO if this is the only site manager do not downgrade their permissions
        
        // Clear any existing permissions
        this.permissionService.clearPermission(siteNodeRef, userName);
        
        // Set the permissions
        this.permissionService.setPermission(siteNodeRef, userName, role, true);
        
        if (! alreadyMember)
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
