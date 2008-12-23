/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.wcm.webproject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.DNSNameMangler;
import org.alfresco.util.ParameterCheck;
import org.alfresco.wcm.sandbox.SandboxFactory;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web Project Service Implementation
 * 
 * @author janv
 */
public class WebProjectServiceImpl extends WCMUtil implements WebProjectService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(WebProjectServiceImpl.class);
    
    /** The DM store where web projects are kept */
    public static final StoreRef WEBPROJECT_STORE = new StoreRef("workspace://SpacesStore");
    
    /** The web projects root node reference */
    private NodeRef webProjectsRootNodeRef; // note: WCM is not currently MT-enabled (so this is OK)
    
    /** Services */
    private NodeService nodeService;
    private SearchService searchService;
    private AVMService avmService;
    private AVMLockingService avmLockingService;
    private AuthorityService authorityService;
    private PermissionService permissionService;
    private PersonService personService;
    
    private SandboxFactory sandboxFactory;
    private VirtServerRegistry virtServerRegistry;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    public void setAvmLockingService(AVMLockingService avmLockingService)
    {
        this.avmLockingService = avmLockingService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setSandboxFactory(SandboxFactory sandboxFactory)
    {
        this.sandboxFactory = sandboxFactory;
    }

    public void setVirtServerRegistry(VirtServerRegistry virtServerRegistry)
    {
        this.virtServerRegistry = virtServerRegistry;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.wcm.WebProjectService#createWebProject(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public WebProjectInfo createWebProject(String dnsName, String name, String title, String description)
    {
    	return createWebProject(dnsName, name, title, description, null, false, null);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.WebProjectService#createWebProject(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public WebProjectInfo createWebProject(String dnsName, String name, String title, String description, NodeRef sourceNodeRef)
    {
        return createWebProject(dnsName, name, title, description, null, false, sourceNodeRef);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.WebProjectService#createWebProject(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, org.alfresco.service.cmr.repository.NodeRef)
     */
    public WebProjectInfo createWebProject(String dnsName, String name, String title, String description, String defaultWebApp, boolean useAsTemplate, NodeRef sourceNodeRef)
    {
        // Generate web project store id (an AVM store name)
        String wpStoreId = DNSNameMangler.MakeDNSName(dnsName);
        
        if (wpStoreId.indexOf(WCMUtil.STORE_SEPARATOR) != -1)
        {
            throw new IllegalArgumentException("Unexpected store id '"+wpStoreId+"' - should not contain '"+WCMUtil.STORE_SEPARATOR+"'");
        }
        
        if (wpStoreId.indexOf(WCMUtil.AVM_STORE_SEPARATOR) != -1)
        {
            throw new IllegalArgumentException("Unexpected store id '"+wpStoreId+"' - should not contain '"+WCMUtil.AVM_STORE_SEPARATOR+"'");
        }
        
        // create the website space in the correct parent folder
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);
        
        ChildAssociationRef childAssocRef = nodeService.createNode(
              getWebProjectsRoot(),
              ContentModel.ASSOC_CONTAINS,
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
              WCMAppModel.TYPE_AVMWEBFOLDER,
              props);
        
        NodeRef wpNodeRef = childAssocRef.getChildRef();
        
        
        // apply the uifacets aspect - icon, title and description props
        Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(4);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, WCMUtil.SPACE_ICON_WEBSITE);
        uiFacetsProps.put(ContentModel.PROP_TITLE, title);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, description);
        nodeService.addAspect(wpNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
        
        // use as template source flag
        nodeService.setProperty(wpNodeRef, WCMAppModel.PROP_ISSOURCE, useAsTemplate);
        
        // set the default webapp name for the project
        defaultWebApp = (defaultWebApp != null && defaultWebApp.length() != 0) ? defaultWebApp : WCMUtil.DIR_ROOT;
        nodeService.setProperty(wpNodeRef, WCMAppModel.PROP_DEFAULTWEBAPP, defaultWebApp);

        // set the property on the node to reference the root AVM store
        nodeService.setProperty(wpNodeRef, WCMAppModel.PROP_AVMSTORE, wpStoreId);

        // branch from source web project, if supplied
        String branchStoreId = null;
        if (sourceNodeRef != null)
        {
           branchStoreId = (String)nodeService.getProperty(sourceNodeRef, WCMAppModel.PROP_AVMSTORE);
        }
        
        // create the AVM staging store to represent the newly created location website
        sandboxFactory.createStagingSandbox(wpStoreId, wpNodeRef, branchStoreId); // ignore return, fails if web project already exists
        
        // create the default webapp folder under the hidden system folders
        if (branchStoreId == null)
        {
           String stagingStore = WCMUtil.buildStagingStoreName(wpStoreId);
           String stagingStoreRoot = WCMUtil.buildSandboxRootPath(stagingStore);
           avmService.createDirectory(stagingStoreRoot, defaultWebApp);
           avmService.addAspect(AVMNodeConverter.ExtendAVMPath(stagingStoreRoot, defaultWebApp), WCMAppModel.ASPECT_WEBAPP);
        }
        
        // now the sandbox is created set the permissions masks for the store
        sandboxFactory.setStagingPermissionMasks(wpStoreId);
        
        // set the property on the node to reference the root AVM store
        nodeService.setProperty(wpNodeRef, WCMAppModel.PROP_AVMSTORE, wpStoreId);
        
        // inform the locking service about this new instance
        avmLockingService.addWebProject(wpStoreId);
        
        // Snapshot the store with the empty webapp
        avmService.createSnapshot(wpStoreId, null, null);
        
        // break the permissions inheritance on the web project node so that only assigned users can access it
        permissionService.setInheritParentPermissions(wpNodeRef, false);
        
        // TODO: Currently auto-creates author sandbox for creator of web project (eg. an admin or a DM contributor to web projects root space)
        // NOTE: JSF client does not yet allow explicit creation of author sandboxes 
        inviteWebUser(wpNodeRef, AuthenticationUtil.getRunAsUser(), WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // Bind the post-commit transaction listener with data required for virtualization server notification
        CreateWebProjectTransactionListener tl = new CreateWebProjectTransactionListener(wpStoreId);
        AlfrescoTransactionSupport.bindListener(tl);
        
        if (logger.isInfoEnabled())
        {
           logger.info("Created web project: " + wpNodeRef + " (store id: " + wpStoreId + ")");
        }
        
        // Return created web project info
        WebProjectInfo wpInfo = new WebProjectInfoImpl(name, title, description, wpStoreId, defaultWebApp, useAsTemplate, wpNodeRef);
        return wpInfo;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#createWebApp(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createWebApp(String wpStoreId, String webAppName, String webAppDescription)
    {
        createWebApp(getWebProjectNodeFromStore(wpStoreId), webAppName, webAppDescription);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#createWebApp(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void createWebApp(NodeRef wpNodeRef, final String webAppName, final String webAppDescription)
    {
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        
        if (isContentManager(wpNodeRef))
        {
            // get AVM store name of the staging sandbox
            final String stagingStoreId = wpInfo.getStagingStoreName();
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    final String parent = WCMUtil.buildSandboxRootPath(stagingStoreId);
                    avmService.createDirectory(parent, webAppName);
                    
                    String path = AVMNodeConverter.ExtendAVMPath(parent, webAppName);
                    avmService.addAspect(path, ApplicationModel.ASPECT_UIFACETS);
                    avmService.addAspect(path, WCMAppModel.ASPECT_WEBAPP);
                    
                    if (webAppDescription != null && webAppDescription.length() != 0)
                    {
                        avmService.setNodeProperty(path, 
                                                   ContentModel.PROP_DESCRIPTION, 
                                                   new PropertyValue(DataTypeDefinition.TEXT,
                                                   webAppDescription));
                    }

                    // Snapshot the store with the empty webapp
                    avmService.createSnapshot(stagingStoreId, null, null);
                    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
            
            if (logger.isInfoEnabled())
            {
               logger.info("Created web app: "+webAppName+" (store id: "+wpInfo.getStoreId()+")");
            }
        }
        else
        {
            throw new AccessDeniedException("Only content managers may create new webapp '"+webAppName+"' (store id: "+wpInfo.getStoreId()+")");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#listWebApps(java.lang.String)
     */
    public List<String> listWebApps(String wpStoreId)
    {
        return listWebApps(getWebProjectNodeFromStore(wpStoreId));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#listWebApps(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<String> listWebApps(NodeRef wpNodeRef)
    {
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        
        String path = WCMUtil.buildSandboxRootPath(wpInfo.getStagingStoreName());
        Map<String, AVMNodeDescriptor> folders = avmService.getDirectoryListing(-1, path);
        
        List<String> webAppNames = new ArrayList<String>(folders.size());
        webAppNames.addAll(folders.keySet());
        return webAppNames;
    }
          
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#deleteWebApp(java.lang.String, java.lang.String)
     */
    public void deleteWebApp(String wpStoreId, String webAppName)
    {
        deleteWebApp(getWebProjectNodeFromStore(wpStoreId), webAppName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#deleteWebApp(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void deleteWebApp(NodeRef wpNodeRef, final String webAppName)
    {
        ParameterCheck.mandatoryString("webAppName", webAppName);
        
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        
        if (webAppName.equals(wpInfo.getDefaultWebApp()))
        {
            throw new AlfrescoRuntimeException("Cannot delete default webapp '"+webAppName+"' (store id: "+wpInfo.getStoreId()+")");
        }
        else if (isContentManager(wpInfo.getNodeRef()))
        {
            // get AVM store name of the staging sandbox
            final String wpStoreId = wpInfo.getStoreId();
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    final String parent = WCMUtil.buildSandboxRootPath(wpStoreId);
                    
                    avmService.removeNode(parent, webAppName);

                    // Snapshot the store with the webapp removed
                    avmService.createSnapshot(wpStoreId, null, null);
                    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
            
            if (logger.isInfoEnabled())
            {
               logger.info("Deleted web app: "+webAppName+" (store id: "+wpStoreId+")");
            }
        }
        else
        {
            throw new AccessDeniedException("Only content managers may delete webapp '"+webAppName+"' (web project: "+wpNodeRef+")");
        }
    }
        
    /**
     * Get the node reference that is the web projects root
     * 
     * @return  NodeRef     node reference
     */
    public NodeRef getWebProjectsRoot()
    {
        if (this.webProjectsRootNodeRef == null)
        {
            // Get the root 'web projects' folder
            ResultSet resultSet = null;
            try
            {
                resultSet = this.searchService.query(WEBPROJECT_STORE, SearchService.LANGUAGE_LUCENE, "PATH:\""+getWebProjectsPath()+"\"");
                if (resultSet.length() == 0)
                {
                    // No root web projects folder exists
                    throw new AlfrescoRuntimeException("No root 'Web Projects' folder exists (is WCM enabled ?)");
                }
                else if (resultSet.length() != 1)
                {
                    // More than one root web projects folder exits
                    throw new AlfrescoRuntimeException("More than one root 'Web Projects' folder exists");
                }
            }
            finally
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            }
            
            this.webProjectsRootNodeRef = resultSet.getNodeRef(0);
        }
        
        return this.webProjectsRootNodeRef;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#listWebProjects()
     */
    public List<WebProjectInfo> listWebProjects()
    {
        NodeRef wpRoot = getWebProjectsRoot();
        
        Set<QName> nodeTypeQNames = new HashSet<QName>(1);
        nodeTypeQNames.add(WCMAppModel.TYPE_AVMWEBFOLDER);
        List<ChildAssociationRef> webProjects = nodeService.getChildAssocs(wpRoot, nodeTypeQNames);
        
        List<WebProjectInfo> result = new ArrayList<WebProjectInfo>(webProjects.size());
        for (ChildAssociationRef childAssocRefs : webProjects)
        {
            result.add(getWebProject(childAssocRefs.getChildRef()));
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#listWebProjects(java.lang.String)
     */
    public List<WebProjectInfo> listWebProjects(String userName)
    {
        List<WebProjectInfo> webProjects = listWebProjects();
        List<WebProjectInfo> result = new ArrayList<WebProjectInfo>(webProjects.size());
        for (WebProjectInfo webProject : webProjects)
        {
            if (isWebUser(webProject.getNodeRef(), userName) == true)
            {
                result.add(webProject);
            }
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebProject(java.lang.String)
     */
    public boolean isWebProject(String wpStoreId)
    {
        NodeRef wpNodeRef = getWebProjectNodeFromStore(wpStoreId);
        if (wpNodeRef == null)
        {
            return false;
        }
        return isWebProject(wpNodeRef);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebProject(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isWebProject(NodeRef wpNodeRef)
    {
        if (wpNodeRef == null)
        {
            return false;
        }
        
        try
        {
            return (WCMAppModel.TYPE_AVMWEBFOLDER.equals(nodeService.getType(wpNodeRef)));
        }
        catch (InvalidNodeRefException e)
        {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebProject(java.lang.String)
     */
    public WebProjectInfo getWebProject(String wpStoreId)
    {
        WebProjectInfo result = null;
        
        // Get the web project node
        NodeRef wpNodeRef = getWebProjectNodeFromStore(wpStoreId);
        if (wpNodeRef != null)
        {
            // Create the web project info
            result = getWebProject(wpNodeRef);
        }
        
        // Return the web project info
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebProject(org.alfresco.service.cmr.repository.NodeRef)
     */
    public WebProjectInfo getWebProject(NodeRef wpNodeRef)
    {
        if (! isWebProject(wpNodeRef))
        {
            throw new IllegalArgumentException(wpNodeRef + " is not a web project");
        }
        
        // Get the properties
        Map<QName, Serializable> properties = this.nodeService.getProperties(wpNodeRef);
        
        String name = (String)properties.get(ContentModel.PROP_NAME);
        String title = (String)properties.get(ContentModel.PROP_TITLE);
        String description = (String)properties.get(ContentModel.PROP_DESCRIPTION);
        String wpStoreId = (String)properties.get(WCMAppModel.PROP_AVMSTORE);
        String defaultWebApp = (String)properties.get(WCMAppModel.PROP_DEFAULTWEBAPP);
        Boolean useAsTemplate = (Boolean)properties.get(WCMAppModel.PROP_ISSOURCE);
        
        // Create and return the web project info
        WebProjectInfo wpInfo = new WebProjectInfoImpl(name, title, description, wpStoreId, defaultWebApp, useAsTemplate, wpNodeRef);
        return wpInfo;
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#updateWebProject(org.alfresco.wcm.webproject.WebProjectInfo)
     */
    public void updateWebProject(WebProjectInfo wpInfo)
    {
        NodeRef wpNodeRef = getWebProjectNodeFromStore(wpInfo.getStoreId());
        if (wpNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Cannot update web project '" + wpInfo.getStoreId() + "' because it does not exist.");
        }
        
        // Note: the site preset and short name can not be updated
        
        // Update the properties of the site - note: cannot change storeId or wpNodeRef
        Map<QName, Serializable> properties = this.nodeService.getProperties(wpNodeRef);
        
        properties.put(ContentModel.PROP_NAME, wpInfo.getName());
        properties.put(ContentModel.PROP_TITLE, wpInfo.getTitle());
        properties.put(ContentModel.PROP_DESCRIPTION, wpInfo.getDescription());
        properties.put(WCMAppModel.PROP_DEFAULTWEBAPP, wpInfo.getDefaultWebApp());
        properties.put(WCMAppModel.PROP_ISSOURCE, wpInfo.isTemplate());
        
        this.nodeService.setProperties(wpNodeRef, properties);
        
        if (logger.isDebugEnabled())
        {
           logger.debug("Updated web project: " + wpNodeRef + " (store id: " + wpInfo.getStoreId() + ")");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#deleteWebProject(java.lang.String)
     */
    public void deleteWebProject(String wpStoreId)
    {
        NodeRef wpNodeRef = getWebProjectNodeFromStore(wpStoreId);
        if (wpNodeRef != null)
        {
            deleteWebProject(wpNodeRef);
        }
        else
        {
            // by definition, the current user is not a content manager since the web project does not exist (or is not visible)
            throw new AccessDeniedException("Only content managers may delete a web project");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#deleteWebProject(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void deleteWebProject(final NodeRef wpNodeRef)
    {
        if (! isContentManager(wpNodeRef))
        {
            // the current user is not a content manager since the web project does not exist (or is not visible)
            throw new AccessDeniedException("Only content managers may delete web project");
        }
        
        // delete all attached website sandboxes in reverse order to the layering
        final String wpStoreId = (String)nodeService.getProperty(wpNodeRef, WCMAppModel.PROP_AVMSTORE);

        if (wpStoreId != null)
        {
            // Notifiy virtualization server about removing this website
            //
            // Implementation note:
            //
            // Because the removal of virtual webapps in the virtualization
            // server is recursive, it only needs to be given the name of
            // the main staging store.
            //
            // This notification must occur *prior* to purging content
            // within the AVM because the virtualization server must list
            // the avm_webapps dir in each store to discover which
            // virtual webapps must be unloaded. The virtualization
            // server traverses the sandbox's stores in most-to-least
            // dependent order, so clients don't have to worry about
            // accessing a preview layer whose main layer has been torn
            // out from under it.
            //
            // It does not matter what webapp name we give here, so "/ROOT"
            // is as sensible as anything else. It's all going away.

            String sandbox = WCMUtil.buildStagingStoreName(wpStoreId);
            String path = WCMUtil.buildStoreWebappPath(sandbox, "/ROOT");
            
            WCMUtil.removeAllVServerWebapps(virtServerRegistry, path, true);

            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    List<SandboxInfo> sbInfos = sandboxFactory.listSandboxes(wpStoreId, AuthenticationUtil.getSystemUserName());
                    
                    for (SandboxInfo sbInfo : sbInfos)
                    {
                        // delete sandbox
                        sandboxFactory.deleteSandbox(sbInfo.getSandboxId());
                    }
                    
                    // TODO delete workflow sandboxes !
                    
                    // delete the web project node itself
                    nodeService.deleteNode(wpNodeRef);
                    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
            
            if (logger.isInfoEnabled())
            {
               logger.info("Deleted web project: " + wpNodeRef + " (store id: " + wpStoreId + ")");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isContentManager(java.lang.String)
     */
    public boolean isContentManager(String storeName)
    {
        return isContentManager(storeName, AuthenticationUtil.getRunAsUser());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.WebProjectService#isContentManager(java.lang.String, java.lang.String)
     */
    public boolean isContentManager(String wpStoreId, String userName)
    {
        return isContentManager(getWebProjectNodeFromStore(wpStoreId), userName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isContentManager(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isContentManager(NodeRef wpNodeRef)
    {
        return isContentManager(wpNodeRef, AuthenticationUtil.getRunAsUser());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isContentManager(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public boolean isContentManager(NodeRef wpNodeRef, String userName)
    {
       String userRole = getWebUserRole(wpNodeRef, userName);
       return WCMUtil.ROLE_CONTENT_MANAGER.equals(userRole);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebUser(java.lang.String, java.lang.String)
     */
    public boolean isWebUser(String wpStoreId, String username)
    {
        ParameterCheck.mandatoryString("username", username);
        
        return isWebUser(getWebProjectNodeFromStore(wpStoreId), username);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public boolean isWebUser(NodeRef wpNodeRef, String userName)
    {
        ParameterCheck.mandatoryString("userName", userName);
        
        return (getWebUserRoleImpl(wpNodeRef, userName) != null);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebUserCount(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int getWebUserCount(NodeRef wpNodeRef)
    {
        return WCMUtil.listWebUsers(nodeService, wpNodeRef).size();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#listWebUsers(java.lang.String)
     */
    public Map<String, String> listWebUsers(String wpStoreId)
    {
        return listWebUsers(getWebProjectNodeFromStore(wpStoreId));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#listWebUsers(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, String> listWebUsers(NodeRef wpNodeRef)
    {
        // special case: allow System - eg. to allow user to create their own sandbox on-demand (createAuthorSandbox)
        if (isContentManager(wpNodeRef) || (AuthenticationUtil.getRunAsUser().equals(AuthenticationUtil.getSystemUserName())))
        {
            return WCMUtil.listWebUsers(nodeService, wpNodeRef);
        }
        else
        {
            throw new AccessDeniedException("Only content managers may list users in a web project");
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebUserRole(java.lang.String, java.lang.String)
     */
    public String getWebUserRole(String wpStoreId, String userName)
    {
        return getWebUserRole(getWebProjectNodeFromStore(wpStoreId), userName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebUserRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public String getWebUserRole(NodeRef wpNodeRef, String userName)
    {
        ParameterCheck.mandatoryString("userName", userName);
        
        String userRole = null;
   
        if (! isWebProject(wpNodeRef))
        {
            logger.warn(wpNodeRef + " is not a web project");
            return null;
        }
        
        if (authorityService.isAdminAuthority(userName))
        {
            // fake the Content Manager role for an admin user
            userRole = WCMUtil.ROLE_CONTENT_MANAGER;
        }
        else
        {
            userRole = getWebUserRoleImpl(wpNodeRef, userName);
        }

        return userRole;
    }
    
    private String getWebUserRoleImpl(NodeRef wpNodeRef, String userName)
    {
        long start = System.currentTimeMillis();
        String userRole = null;

        StringBuilder query = new StringBuilder(128);
        query.append("+PARENT:\"").append(wpNodeRef).append("\" ");
        query.append("+TYPE:\"").append(WCMAppModel.TYPE_WEBUSER).append("\" ");
        query.append("+@").append(NamespaceService.WCMAPP_MODEL_PREFIX).append("\\:username:\"");
        query.append(userName);
        query.append("\"");
   
        ResultSet resultSet = null;
        List<NodeRef> nodes = null;
        try
        {
            resultSet = searchService.query(
                    WEBPROJECT_STORE,
                    SearchService.LANGUAGE_LUCENE,
                    query.toString());
            nodes = resultSet.getNodeRefs();   
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    
        if (nodes.size() == 1)
        {
            userRole = (String)nodeService.getProperty(nodes.get(0), WCMAppModel.PROP_WEBUSERROLE);
        }
        else if (nodes.size() == 0)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("getWebProjectUserRole: user role not found for " + userName);
            }
        }
        else
        {
            logger.warn("getWebProjectUserRole: more than one user role found for " + userName);
        }
   
        if (logger.isTraceEnabled())
        {
            logger.trace("getWebProjectUserRole: "+userName+" "+userRole+" in "+(System.currentTimeMillis()-start)+" ms");
        }
   
        return userRole;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#findWebProjectNodeFromPath(java.lang.String)
     */
    public NodeRef getWebProjectNodeFromPath(String absoluteAVMPath)
    {
        return getWebProjectNodeFromStore(WCMUtil.getWebProjectStoreIdFromPath(absoluteAVMPath));
    }
    
    /*(non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebProjectNodeFromStore(java.lang.String)
     */
    public NodeRef getWebProjectNodeFromStore(String wpStoreId)
    {
       ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
       
       return WCMUtil.getWebProjectNodeFromWebProjectStore(avmService, wpStoreId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#inviteWebUsersGroups(java.lang.String, java.util.Map)
     */
    public void inviteWebUsersGroups(String wpStoreId, Map<String, String> userGroupRoles)
    {
        inviteWebUsersGroups(getWebProjectNodeFromStore(wpStoreId), userGroupRoles, false);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#inviteWebUsersGroups(java.lang.String, java.util.Map, boolean)
     */
    public void inviteWebUsersGroups(String wpStoreId, Map<String, String> userGroupRoles, boolean autoCreateAuthorSandbox)
    {
        inviteWebUsersGroups(getWebProjectNodeFromStore(wpStoreId), userGroupRoles, autoCreateAuthorSandbox);
    }
    
    public void inviteWebUsersGroups(NodeRef wpNodeRef, Map<String, String> userGroupRoles, boolean autoCreateAuthorSandbox)
    {
        if (! isContentManager(wpNodeRef))
        {
            throw new AccessDeniedException("Only content managers may invite web users");
        }
        
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        String wpStoreId = wpInfo.getStoreId();
        
        // build a list of managers who will have full permissions on ALL staging areas
        List<String> managers = new ArrayList<String>(4);
        Set<String> existingUsers = new HashSet<String>(8);
        
        // retrieve the list of managers from the existing users
        for (Map.Entry<String, String> userRole : userGroupRoles.entrySet())
        {
            String authority = userRole.getKey();
            String role = userRole.getValue();
                
            for (String userAuth : findNestedUserAuthorities(authority))
            {
                if (WCMUtil.ROLE_CONTENT_MANAGER.equals(role))
                {
                    managers.add(userAuth);
                }
            }
        }
       
        Map<String, String> existingUserRoles = listWebUsers(wpNodeRef);
        for (Map.Entry<String, String> userRole : existingUserRoles.entrySet())
        {
            String username = userRole.getKey();
            String userrole = userRole.getValue();
              
            if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userrole) && managers.contains(username) == false)
            {
                managers.add(username);
            }
              
            // add each existing user to the exclude this - we cannot add them more than once!
            existingUsers.add(username);
        }
        
        List<SandboxInfo> sandboxInfoList = new LinkedList<SandboxInfo>();
        
        int invitedCount = 0;
        boolean managersUpdateRequired = false;
        
        for (Map.Entry<String, String> userRole : userGroupRoles.entrySet())
        {
            String authority = userRole.getKey();
            String role = userRole.getValue();
            
            for (String userAuth : findNestedUserAuthorities(authority))
            {
                if (existingUsers.contains(userAuth) == false)
                {
                    if (autoCreateAuthorSandbox)
                    {
                        // create a sandbox for the user with permissions based on role
                        SandboxInfo sbInfo = sandboxFactory.createUserSandbox(wpStoreId, managers, userAuth, role);
                        sandboxInfoList.add(sbInfo);
                    }
                    
                    sandboxFactory.addStagingAreaUser(wpStoreId, userAuth, role);
                 
                    // create an app:webuser instance for each authority and assoc to the web project node
                    createWebUser(wpNodeRef, userAuth, role);
                 
                    // if this new user is a manager, we'll need to update the manager permissions applied
                    // to each existing user sandbox - to ensure that new managers have access to them
                    managersUpdateRequired |= (WCMUtil.ROLE_CONTENT_MANAGER.equals(role));
                    
                    invitedCount++;
                }
                else
                {
                    logger.warn("User '"+userAuth+"' already invited to web project: "+wpNodeRef+"(store id: "+wpStoreId+")");
                }
           }
        }
        
        // Bind the post-commit transaction listener with data required for virtualization server notification
        CreateSandboxTransactionListener tl = new CreateSandboxTransactionListener(sandboxInfoList, listWebApps(wpNodeRef));
        AlfrescoTransactionSupport.bindListener(tl);
        
        if (managersUpdateRequired == true)
        {
            sandboxFactory.updateSandboxManagers(wpStoreId, wpNodeRef, managers);
        }
        
        // get permissions and roles for a web project folder type
        Set<String> perms = permissionService.getSettablePermissions(WCMAppModel.TYPE_AVMWEBFOLDER);
        
        // set permissions for each user
        for (Map.Entry<String, String> userRole : userGroupRoles.entrySet())
        {
            String authority = userRole.getKey();
            String role = userRole.getValue();
           
            for (String permission : perms)
            {
                if (role.equals(permission))
                {
                    permissionService.setPermission(wpNodeRef,
                                                    authority,
                                                    permission,
                                                    true);
                    break;
                }
            }
        }
        
        if (logger.isInfoEnabled())
        {
           logger.info("Invited "+invitedCount+" web users (store id: "+wpStoreId+")");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#inviteWebUser(java.lang.String, java.lang.String, java.lang.String)
     */
    public void inviteWebUser(String wpStoreId, String userAuth, String role)
    {
        inviteWebUser(getWebProjectNodeFromStore(wpStoreId), userAuth, role, false);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#inviteWebUser(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public void inviteWebUser(String wpStoreId, String userAuth, String role, boolean autoCreateAuthorSandbox)
    {
        inviteWebUser(getWebProjectNodeFromStore(wpStoreId), userAuth, role, autoCreateAuthorSandbox);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#inviteWebUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, boolean)
     */
    public void inviteWebUser(NodeRef wpNodeRef, String userAuth, String role, boolean autoCreateAuthorSandbox)
    {
        if (! isContentManager(wpNodeRef))
        {
            throw new AccessDeniedException("Only content managers may invite web user");
        }
        
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        final String wpStoreId = wpInfo.getStoreId();

        if (isWebUser(wpNodeRef, userAuth))        
        {
            logger.warn("User '"+userAuth+"' already invited to web project: "+wpNodeRef+" (store id: "+wpStoreId+")");
            return;
        }
        else
        {
            // build a list of managers who will have full permissions on ALL staging areas
            List<String> managers = new ArrayList<String>(4);
            
            // retrieve the list of managers from the existing users
            Map<String, String> existingUserRoles = listWebUsers(wpNodeRef);
            for (Map.Entry<String, String> userRole : existingUserRoles.entrySet())
            {
                String username = userRole.getKey();
                String userrole = userRole.getValue();
                
                if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userrole) && managers.contains(username) == false)
                {
                    managers.add(username);
                }
            }
            
            if (autoCreateAuthorSandbox)
            {
                // create a sandbox for the user with permissions based on role
                SandboxInfo sbInfo = sandboxFactory.createUserSandbox(wpStoreId, managers, userAuth, role);
                
                List<SandboxInfo> sandboxInfoList = new LinkedList<SandboxInfo>();
                sandboxInfoList.add(sbInfo);
                
                // Bind the post-commit transaction listener with data required for virtualization server notification
                CreateSandboxTransactionListener tl = new CreateSandboxTransactionListener(sandboxInfoList, listWebApps(wpNodeRef));
                AlfrescoTransactionSupport.bindListener(tl);
            }
            
            // if this new user is a manager, we'll need to update the manager permissions applied
            // to each existing user sandbox - to ensure that new user has access to them
            if (WCMUtil.ROLE_CONTENT_MANAGER.equals(role))
            {
                managers.add(userAuth);
                sandboxFactory.updateSandboxManagers(wpStoreId, wpNodeRef, managers);
            }
            
            sandboxFactory.addStagingAreaUser(wpStoreId, userAuth, role);
             
            // create an app:webuser instance for the user and assoc to the web project node
            createWebUser(wpNodeRef, userAuth, role);
            
            // get permissions and roles for a web project folder type
            Set<String> perms = permissionService.getSettablePermissions(WCMAppModel.TYPE_AVMWEBFOLDER);
            
            // set permissions for the user
            for (String permission : perms)
            {
                if (role.equals(permission))
                {
                    permissionService.setPermission(wpNodeRef,
                                                    userAuth,
                                                    permission,
                                                    true);
                    break;
                }
            }
            
            if (logger.isInfoEnabled())
            {
               logger.info("Invited web user: "+userAuth+" (store id: "+wpStoreId+")");
            }
        }
    }
    
    private void createWebUser(NodeRef wpNodeRef, String userName, String userRole)
    {
        // create an app:webuser instance for the user and assoc to the web project node
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
        props.put(WCMAppModel.PROP_WEBUSERNAME, userName);
        props.put(WCMAppModel.PROP_WEBUSERROLE, userRole);
        nodeService.createNode(wpNodeRef,
                               WCMAppModel.ASSOC_WEBUSER,
                               WCMAppModel.ASSOC_WEBUSER,
                               WCMAppModel.TYPE_WEBUSER,
                               props);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#uninviteWebUser(java.lang.String, java.lang.String)
     */
    public void uninviteWebUser(String wpStoreId, String userAuth)
    {
        uninviteWebUser(getWebProjectNodeFromStore(wpStoreId), userAuth, false);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#uninviteWebUser(java.lang.String, java.lang.String, boolean)
     */
    public void uninviteWebUser(String wpStoreId, String userAuth, boolean autoDeleteAuthorSandbox)
    {
        uninviteWebUser(getWebProjectNodeFromStore(wpStoreId), userAuth, autoDeleteAuthorSandbox);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#uninviteWebUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, boolean)
     */
    public void uninviteWebUser(NodeRef wpNodeRef, String userAuth, boolean autoDeleteAuthorSandbox)
    {
        if (! isContentManager(wpNodeRef))
        {
            throw new AccessDeniedException("Only content managers may uninvite web user '"+userAuth+"' from web project: "+wpNodeRef);
        }
        
        ParameterCheck.mandatory("wpNodeRef", wpNodeRef);
        ParameterCheck.mandatoryString("userAuth", userAuth);
        
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        String wpStoreId = wpInfo.getStoreId();
        String userMainStore = WCMUtil.buildUserMainStoreName(wpStoreId, userAuth);
       
        if (autoDeleteAuthorSandbox)
        {
            sandboxFactory.deleteSandbox(userMainStore);
        }
        
        // remove the store reference from the website folder meta-data (see also WCMUtil.listWebUsers)
        List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(wpNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
        
        // retrieve the list of managers from the existing users
        List<String> managers = new ArrayList<String>(4);
        for (ChildAssociationRef ref : userInfoRefs)
        {
            NodeRef userInfoRef = ref.getChildRef();
            String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
            String userrole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
              
            if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userrole) && managers.contains(username) == false)
            {
                managers.add(username);
            }
        }
        
        for (ChildAssociationRef ref : userInfoRefs)
        {
            NodeRef userInfoRef = ref.getChildRef();
            String user = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
          
            if (userAuth.equals(user))
            {
                // remove the association to this web project user meta-data
                nodeService.removeChild(wpNodeRef, ref.getChildRef());
                 
                // remove permission for the user (also fixes ETWOONE-338)
                permissionService.clearPermission(wpNodeRef, userAuth);
                 
                if (logger.isInfoEnabled())
                {
                    logger.info("Uninvited web user: "+userAuth+" (store id: "+wpStoreId+")");
                }
                 
                break; // for loop
            }
       }
    }

    /**
     * Find all nested user authorities contained with an authority
     * 
     * @param authority     The authority to search, USER authorities are returned immediately, GROUP authorites
     *                      are recursively scanned for contained USER authorities.
     * 
     * @return a Set of USER authorities
     */
    private Set<String> findNestedUserAuthorities(String authority)
    {
       Set<String> users;
       
       AuthorityType authType = AuthorityType.getAuthorityType(authority);
       if (authType.equals(AuthorityType.USER))
       {
          users = new HashSet<String>(1, 1.0f);
          if (personService.personExists(authority) == true)
          {
             users.add(authority); 
          }
       }
       else if (authType.equals(AuthorityType.GROUP))
       {
          // walk each member of the group
          users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
          for (String userAuth : users)
          {
             if (personService.personExists(userAuth) == false)
             {
                users.remove(authType);
             }
          }
       }
       else
       {
          users = Collections.<String>emptySet();
       }
       
       return users;
    }
    
    private String getWebProjectsPath()
    {
       return "/"+SPACES_COMPANY_HOME_CHILDNAME+"/"+SPACES_WCM_CHILDNAME;
    }
    
    private static final String SPACES_COMPANY_HOME_CHILDNAME = "app:company_home"; // should match repository property: spaces.company_home.childname
    private static final String SPACES_WCM_CHILDNAME          = "app:wcm";          // should match repository property: spaces.wcm.childname
    
    
    /**
     * Transaction listener - invoked after commit
     */
    private class CreateWebProjectTransactionListener extends TransactionListenerAdapter
    {
        private String wpStoreId;

        public CreateWebProjectTransactionListener(String wpStoreId)
        {
            this.wpStoreId = wpStoreId;
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
         */
        @Override
        public void afterCommit()
        {
            // post-commit
            if (wpStoreId != null)
            {
               // update the virtualisation server with the default ROOT webapp path
               // performed after the main txn has committed successfully
               String newStoreName = WCMUtil.buildStagingStoreName(wpStoreId);
               
               String path = WCMUtil.buildStoreWebappPath(newStoreName, WCMUtil.DIR_ROOT);
               
               WCMUtil.updateVServerWebapp(virtServerRegistry, path, true);
            }     
        }
    }
    
    /**
     * Create Sandbox Transaction listener - invoked after commit
     */
    private class CreateSandboxTransactionListener extends TransactionListenerAdapter
    {
        private List<SandboxInfo> sandboxInfoList;
        private List<String> webAppNames;
        
        public CreateSandboxTransactionListener(List<SandboxInfo> sandboxInfoList, List<String> webAppNames)
        {
            this.sandboxInfoList = sandboxInfoList;
            this.webAppNames = webAppNames;
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
         */
        @Override
        public void afterCommit()
        {
            // Handle notification to the virtualization server 
            // (this needs to occur after the sandboxes are created in the main txn)
            
            // reload virtualisation server for webapp(s) in this web project
            for (SandboxInfo sandboxInfo : this.sandboxInfoList)
            {
                String newlyInvitedStoreName = WCMUtil.buildStagingStoreName(sandboxInfo.getMainStoreName());
                
                for (String webAppName : webAppNames)
                {
                    String path = WCMUtil.buildStoreWebappPath(newlyInvitedStoreName, webAppName);
                    WCMUtil.updateVServerWebapp(virtServerRegistry, path, true);
                }
            }
        }
    }
}
