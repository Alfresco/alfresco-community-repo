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
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.util.ISO9075;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationEvent;

/**
 * Bootstraps the site AVN and DM stores
 * 
 * @author Roy Wetherall
 */
public class SiteServiceImpl extends AbstractLifecycleBean implements SiteService, SiteModel
{
    public static final String SITE_AVM_STORE = "SiteStore";
    public static final StoreRef SITE_DM_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SiteStore");
    
    private NodeService nodeService;
    private SearchService searchService;
    private PermissionService permissionService;
    private AuthenticationComponent authenticationComponent;
    private RetryingTransactionHelper retryingTransactionHelper;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
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
       SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic);
       return siteInfo;
    }
    
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
        ResultSet resultSet = this.searchService.query(SITE_DM_STORE, SearchService.LANGUAGE_LUCENE, "PATH:\"cm:sites\"");
        if (resultSet.length() == 0)
        {
            // TODO
            throw new AlfrescoRuntimeException("No root sites folder exists");
        }
        else if (resultSet.length() != 1)
        {
            // TODO
            throw new AlfrescoRuntimeException("More than one root sites folder exists");
        }        
        NodeRef sitesRoot = resultSet.getNodeRef(0);
        
        // TODO
        // In time we will use some sort of algorithm to spread the creation of sites across an arbitary structure
                
        return sitesRoot;
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
        SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic);
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
    
    private NodeRef getSiteNodeRef(String shortName)
    {
        NodeRef result = null;
        ResultSet resultSet = this.searchService.query(SITE_DM_STORE, SearchService.LANGUAGE_LUCENE, "PATH:\"cm:sites/cm:" + ISO9075.encode(shortName) + "\"");
        if (resultSet.length() == 1)
        {
            result = resultSet.getNodeRef(0);
        }
        return result;
    }

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
                // CHeck to see if we already have an entry for the user in the map
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
        
        // TODO if this is the only site manager do not downgrade their permissions
        
        // Clear any existing permissions
        this.permissionService.clearPermission(siteNodeRef, userName);
        
        // Set the permissions
        this.permissionService.setPermission(siteNodeRef, userName, role, true);
    }
    
    /**
     * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Ensure execution occures in a transaction
        this.retryingTransactionHelper.doInTransaction(
            new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {        
                public Object execute() throws Throwable
                {
                    String currentUserName = SiteServiceImpl.this.authenticationComponent.getCurrentUserName();
                    SiteServiceImpl.this.authenticationComponent.setSystemUserAsCurrentUser();
                    try
                    {
                        // Bootstrap the site stores
                        bootstrapSiteStore(SITE_DM_STORE);
                    }
                    finally
                    {
                        if (currentUserName != null)
                        {
                            SiteServiceImpl.this.authenticationComponent.setCurrentUser(currentUserName);
                        }
                        else
                        {
                            SiteServiceImpl.this.authenticationComponent.clearCurrentSecurityContext();
                        }
                    }
                    
                    return null;
                }                
            });        
    }
    
    /**
     * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Do nothing
    }
    
    /**
     * Bootstrap the DM site store
     * 
     * @param storeRef  the store reference
     */
    private void bootstrapSiteStore(StoreRef storeRef)
    {
        // Check to see if the sotre exists
        if (this.nodeService.exists(storeRef) == false)
        {
            // Create the store
            this.nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
            NodeRef rootNode = this.nodeService.getRootNode(storeRef);
            
            // Create the root folder where sites will be stored
            NodeRef rootStoreNode = this.nodeService.createNode(rootNode, 
                                        ContentModel.ASSOC_CHILDREN, 
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sites"), 
                                        ContentModel.TYPE_FOLDER).getChildRef();
            
            // Set the permissions for the root store node
            this.permissionService.setInheritParentPermissions(rootStoreNode, false);
            this.permissionService.setPermission(rootStoreNode, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
            this.permissionService.setPermission(rootStoreNode, PermissionService.ALL_AUTHORITIES, PermissionService.CREATE_CHILDREN, true);
        }
    }
}
