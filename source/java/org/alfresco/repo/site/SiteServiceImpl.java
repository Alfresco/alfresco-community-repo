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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.AbstractLifecycleBean;
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
    private AuthenticationService authenticationService;
    private PermissionService permissionService;
    private AuthenticationComponent authenticationComponent;
    private AVMRepository AVMRepository;
    private RetryingTransactionHelper retryingTransactionHelper;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    public void setAVMRepository(AVMRepository repository)
    {
        AVMRepository = repository;
    }
    
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    public SiteInfo createSite(String sitePreset, String shortName, String title, String description, boolean isPublic)
    {
        // TODO:
        // 1.  Check that the site preset exists
        // 2.  Check that the short name of the site isn't a duplicate
        // 3.  AVM create:
        //     3a.   Find the site preset AVM folder
        //     3b.   Create a new site folder in the correct location (named by the short name)
        //     3c.   Copy the contents of the site preset folder into the new site folder
        //     3d.   Mangle files as needed during copy ??
        // 4.  DM create:
        //     4a.   Find the site preset DM folder ??
        //     4b.   Create a new site in the correct location (named by short name)
        //     4c.   Set meta-data
        //     4d.   Set up memberships (copying from site preset DM folder??)
        //     4e.   Set up another details (rules) on site from DM preset folder ??
        // 5. Return created site information
        
        
        
        // 4. DM create .. create the DM object that represents the site
        
        // Get the site parent node reference
        NodeRef siteParent = getDMSiteParent(shortName);
        
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
       this.permissionService.setInheritParentPermissions(siteNodeRef, false);
       if (isPublic == true)
       {
           this.permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
       }
        
       // 5. Return created site information
       SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic);
       return siteInfo;
    }
    
    private NodeRef getDMSiteParent(String shortName)
    {
        // TODO
        // For now just return the site root, later we may build folder structure based on the shortname to
        // spread the sites about
        return getDMSiteRoot();
    }
    
    private NodeRef getDMSiteRoot()
    {
        // Get the root 'sites' folder
        ResultSet resultSet = this.searchService.query(SITE_DM_STORE, SearchService.LANGUAGE_LUCENE, "PATH:\"cm:sites\"");
        if (resultSet.length() == 0)
        {
            // TODO
            throw new RuntimeException("No root sites folder exists");
        }
        else if (resultSet.length() != 1)
        {
            // TODO
            throw new RuntimeException("More than one root sites folder exists");
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
        // - should we be taking the list from the AVM store, since we can have an AVM site pointing to
        //   the default DM data site 
        
        // TODO
        // For now just return the list of sites present under the site root
        NodeRef siteRoot = getDMSiteRoot();
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
        
        // Create and return the site information
        SiteInfo siteInfo = new SiteInfo(sitePreset, shortName, title, description, isPublic);
        return siteInfo;
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
            this.nodeService.createNode(rootNode, 
                                        ContentModel.ASSOC_CHILDREN, 
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sites"), 
                                        ContentModel.TYPE_FOLDER);
        }
    }
}
