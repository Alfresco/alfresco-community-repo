/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.wcm.webproject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
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
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.DNSNameMangler;
import org.alfresco.wcm.preview.PreviewURIServiceRegistry;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.wcm.sandbox.SandboxFactory;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxFactory.UserRoleWrapper;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

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
    private boolean isSetWebProjectsRootNodeRef;
    
    /** Services */
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private AVMService avmService;
    private AuthorityService authorityService;
    private PermissionService permissionService;
    private PersonService personService;
    private SandboxFactory sandboxFactory;
    private VirtServerRegistry virtServerRegistry;
    private PreviewURIServiceRegistry previewURIProviderRegistry;
    private TransactionService transactionService;
    private AVMLockingService avmLockingService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
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
    
    public void setPreviewURIServiceRegistry(PreviewURIServiceRegistry previewURIProviderRegistry)
    {
        this.previewURIProviderRegistry = previewURIProviderRegistry;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setAvmLockingService(AVMLockingService avmLockingService)
    {
        this.avmLockingService = avmLockingService;
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
        return createWebProject(new WebProjectInfoImpl(dnsName, name, title, description, defaultWebApp, useAsTemplate, sourceNodeRef, null));
    }
    
    public WebProjectInfo createWebProject(WebProjectInfo wpInfo)
    {
        long start = System.currentTimeMillis();
        
        String wpStoreId = wpInfo.getStoreId();
        String name = wpInfo.getName();
        String title = wpInfo.getTitle();
        String description = wpInfo.getDescription();
        boolean useAsTemplate = wpInfo.isTemplate();
        NodeRef sourceNodeRef = wpInfo.getNodeRef();
        String defaultWebApp = wpInfo.getDefaultWebApp();
        String previewProviderName = wpInfo.getPreviewProviderName();
        
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        ParameterCheck.mandatoryString("name", name);
        
        // Generate web project store id (an AVM store name)
        wpStoreId = DNSNameMangler.MakeDNSName(wpStoreId);
        
        if (wpStoreId.indexOf(WCMUtil.STORE_SEPARATOR) != -1)
        {
            throw new IllegalArgumentException("Unexpected store id '"+wpStoreId+"' - should not contain '"+WCMUtil.STORE_SEPARATOR+"'");
        }
        
        if (wpStoreId.indexOf(AVMUtil.AVM_STORE_SEPARATOR_CHAR) != -1)
        {
            throw new IllegalArgumentException("Unexpected store id '"+wpStoreId+"' - should not contain '"+AVMUtil.AVM_STORE_SEPARATOR_CHAR+"'");
        }
        
        if (previewProviderName == null)
        {
            // default preview URI service provider
            previewProviderName = previewURIProviderRegistry.getDefaultProviderName();
        }
        else if (! previewURIProviderRegistry.getPreviewURIServiceProviders().keySet().contains(previewProviderName))
        {
            throw new AlfrescoRuntimeException("Cannot update web project '" + wpInfo.getStoreId() + "' - unknown preview URI service provider ("+previewProviderName+")");
        }
        
        // default webapp name
        defaultWebApp = (defaultWebApp != null && defaultWebApp.length() != 0) ? defaultWebApp : WCMUtil.DIR_ROOT;
        
        // create the website space in the correct parent folder
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);
        props.put(WCMAppModel.PROP_ISSOURCE, useAsTemplate);
        props.put(WCMAppModel.PROP_DEFAULTWEBAPP, defaultWebApp);
        props.put(WCMAppModel.PROP_AVMSTORE, wpStoreId); // reference to the root AVM store
        props.put(WCMAppModel.PROP_PREVIEW_PROVIDER, previewProviderName);
        
        NodeRef webProjectsRoot = getWebProjectsRoot();

        // ALF-906: ensure that DM rules are not inherited by web projects
    	if(!nodeService.hasAspect(webProjectsRoot, RuleModel.ASPECT_IGNORE_INHERITED_RULES))
    	{
    		nodeService.addAspect(webProjectsRoot, RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);
    	}

        ChildAssociationRef childAssocRef = nodeService.createNode(
       	      webProjectsRoot,
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
        
        // branch from source web project, if supplied
        String branchStoreId = null;
        if (sourceNodeRef != null)
        {
           branchStoreId = (String)nodeService.getProperty(sourceNodeRef, WCMAppModel.PROP_AVMSTORE);
        }
        
        // create the AVM staging store to represent the newly created location website
        sandboxFactory.createStagingSandbox(wpStoreId, wpNodeRef, branchStoreId); // ignore return, fails if web project already exists
        
        String stagingStore = WCMUtil.buildStagingStoreName(wpStoreId);
        
        // create the default webapp folder under the hidden system folders
        if (branchStoreId == null)
        {
           String stagingStoreRoot = WCMUtil.buildSandboxRootPath(stagingStore);
           avmService.createDirectory(stagingStoreRoot, defaultWebApp);
           avmService.addAspect(AVMNodeConverter.ExtendAVMPath(stagingStoreRoot, defaultWebApp), WCMAppModel.ASPECT_WEBAPP);
        }
        
        // now the sandbox is created set the permissions masks for the store
        sandboxFactory.setStagingPermissionMasks(wpStoreId);
        
        // set preview provider on staging store (used for preview lookup)
        avmService.setStoreProperty(stagingStore,
                SandboxConstants.PROP_WEB_PROJECT_PREVIEW_PROVIDER,
                new PropertyValue(DataTypeDefinition.TEXT, previewProviderName));
        
        // Snapshot the store with the empty webapp
        avmService.createSnapshot(wpStoreId, null, null);
        
        // break the permissions inheritance on the web project node so that only assigned users can access it
        permissionService.setInheritParentPermissions(wpNodeRef, false);
        
        // TODO: Currently auto-creates author sandbox for creator of web project (eg. an admin or a DM contributor to web projects root space)
        // NOTE: JSF client does not yet allow explicit creation of author sandboxes 
        inviteWebUser(wpNodeRef, AuthenticationUtil.getFullyAuthenticatedUser(), WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // Bind the post-commit transaction listener with data required for virtualization server notification
        CreateWebAppTransactionListener tl = new CreateWebAppTransactionListener(wpStoreId, WCMUtil.DIR_ROOT);
        AlfrescoTransactionSupport.bindListener(tl);
        
        if (logger.isDebugEnabled())
        {
           logger.debug("Created web project: " + wpNodeRef + " in "+(System.currentTimeMillis()-start)+" ms (store id: " + wpStoreId + ")");
        }
        
        // Return created web project info
        return new WebProjectInfoImpl(wpStoreId, name, title, description, defaultWebApp, useAsTemplate, wpNodeRef, previewProviderName);
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
        long start = System.currentTimeMillis();
        
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
            
            CreateWebAppTransactionListener tl = new CreateWebAppTransactionListener(wpInfo.getStoreId(), webAppName);
            AlfrescoTransactionSupport.bindListener(tl);
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Created web app: "+webAppName+" in "+(System.currentTimeMillis()-start)+" ms (store id: "+wpInfo.getStoreId()+")");
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
        long start = System.currentTimeMillis();
        
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
            
            WCMUtil.removeVServerWebapp(virtServerRegistry, WCMUtil.buildStoreWebappPath(wpStoreId, webAppName), true);
            
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
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Deleted web app: "+webAppName+" in "+(System.currentTimeMillis()-start)+" ms (store id: "+wpStoreId+")");
            }
        }
        else
        {
            throw new AccessDeniedException("Only content managers may delete webapp '"+webAppName+"' (web project: "+wpNodeRef+")");
        }
    }

    /*
     * @see org.alfresco.wcm.webproject.WebProjectService#hasWebProjectsRoot()
     */
    public boolean hasWebProjectsRoot()
    {
        return getWebProjectsRootOrNull() != null;
    }

    private NodeRef getWebProjectsRootOrNull()
    {
        if (!this.isSetWebProjectsRootNodeRef)
        {
            // Get the root 'web projects' folder
            List<NodeRef> results = this.searchService.selectNodes(this.nodeService.getRootNode(WEBPROJECT_STORE),
                    getWebProjectsPath(), null, this.namespaceService, false);
            int size = results.size();
            if (size > 1)
            {
                // More than one root web projects folder exits
                throw new AlfrescoRuntimeException("More than one root 'Web Projects' folder exists");
            }
            if (size > 0)
            {
                this.webProjectsRootNodeRef = results.get(0);
            }
            this.isSetWebProjectsRootNodeRef = true;
        }
        
        return this.webProjectsRootNodeRef;
    }

    /**
     * Get the node reference that is the web projects root
     * 
     * @return  NodeRef     node reference
     */
    public NodeRef getWebProjectsRoot()
    {
        NodeRef result = getWebProjectsRootOrNull();
        if (result == null)
        {
            // No root web projects folder exists
            throw new AlfrescoRuntimeException("No root 'Web Projects' folder exists (is WCM enabled ?)");
        }
        return result;
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
     * @see org.alfresco.wcm.webproject.WebProjectService#getPreviewProvider(java.lang.String)
     */
    public String getPreviewProvider(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        String previewProviderName = null;
        
        try
        {
            String stagingStoreId = WCMUtil.buildStagingStoreName(wpStoreId);
            PropertyValue pValue = avmService.getStoreProperty(stagingStoreId, SandboxConstants.PROP_WEB_PROJECT_PREVIEW_PROVIDER);
            
            if (pValue != null)
            {
                previewProviderName = (String)pValue.getValue(DataTypeDefinition.TEXT);
            }
        }
        catch (AVMNotFoundException nfe)
        {
            logger.warn(wpStoreId + " is not a web project: " + nfe);
        }
        
        if (previewProviderName == null)
        {
            previewProviderName = previewURIProviderRegistry.getDefaultProviderName();
        }
        
        return previewProviderName;
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
        String previewProvider = (String)properties.get(WCMAppModel.PROP_PREVIEW_PROVIDER);
        
        // Create and return the web project info
        WebProjectInfo wpInfo = new WebProjectInfoImpl(wpStoreId, name, title, description, defaultWebApp, useAsTemplate, wpNodeRef, previewProvider);
        return wpInfo;
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#updateWebProject(org.alfresco.wcm.webproject.WebProjectInfo)
     */
    public void updateWebProject(WebProjectInfo wpInfo)
    {
        long start = System.currentTimeMillis();
        
        NodeRef wpNodeRef = getWebProjectNodeFromStore(wpInfo.getStoreId());
        if (wpNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Cannot update web project '" + wpInfo.getStoreId() + "' - it does not exist.");
        }
        
        if (! listWebApps(wpNodeRef).contains(wpInfo.getDefaultWebApp()))
        {
            throw new AlfrescoRuntimeException("Cannot update web project '" + wpInfo.getStoreId() + "' - unknown default web app  ("+wpInfo.getDefaultWebApp()+")");
        }
        
        if (wpInfo.getPreviewProviderName() == null)
        {
            wpInfo.setPreviewProviderName(previewURIProviderRegistry.getDefaultProviderName());
        }
        else if (! previewURIProviderRegistry.getPreviewURIServiceProviders().keySet().contains(wpInfo.getPreviewProviderName()))
        {
            throw new AlfrescoRuntimeException("Cannot update web project '" + wpInfo.getStoreId() + "' - unknown preview URI service provider ("+wpInfo.getPreviewProviderName()+")");
        }
        
        // Note: the site preset and short name can not be updated
        
        // Update the properties of the site - note: cannot change storeId or wpNodeRef
        Map<QName, Serializable> properties = this.nodeService.getProperties(wpNodeRef);
        
        properties.put(ContentModel.PROP_NAME, wpInfo.getName());
        properties.put(ContentModel.PROP_TITLE, wpInfo.getTitle());
        properties.put(ContentModel.PROP_DESCRIPTION, wpInfo.getDescription());
        properties.put(WCMAppModel.PROP_DEFAULTWEBAPP, wpInfo.getDefaultWebApp());
        properties.put(WCMAppModel.PROP_ISSOURCE, wpInfo.isTemplate());
        properties.put(WCMAppModel.PROP_PREVIEW_PROVIDER, wpInfo.getPreviewProviderName());
        
        this.nodeService.setProperties(wpNodeRef, properties);
        
        // set preview provider on staging store (used for preview lookup)
        String stagingStore = WCMUtil.buildStagingStoreName(wpInfo.getStoreId());
        
        avmService.deleteStoreProperty(stagingStore, SandboxConstants.PROP_WEB_PROJECT_PREVIEW_PROVIDER);
        avmService.setStoreProperty(stagingStore,
                SandboxConstants.PROP_WEB_PROJECT_PREVIEW_PROVIDER,
                new PropertyValue(DataTypeDefinition.TEXT, wpInfo.getPreviewProviderName()));
        
        if (logger.isDebugEnabled())
        {
           logger.debug("Updated web project: " + wpNodeRef + " in "+(System.currentTimeMillis()-start)+" ms (store id: " + wpInfo.getStoreId() + ")");
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
        long start = System.currentTimeMillis();
        
        if (! isContentManager(wpNodeRef))
        {
            // the current user is not a content manager since the web project does not exist (or is not visible)
            throw new AccessDeniedException("Only content managers may delete web project");
        }
        
        // delete all attached website sandboxes in reverse order to the layering
        final String wpStoreId = (String)nodeService.getProperty(wpNodeRef, WCMAppModel.PROP_AVMSTORE);
        
        if (wpStoreId != null)
        {
            // Notify virtualization server about removing this website
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

            final String sandbox = WCMUtil.buildStagingStoreName(wpStoreId);
            String path = WCMUtil.buildStoreWebappPath(sandbox, "/ROOT");
            
            WCMUtil.removeAllVServerWebapps(virtServerRegistry, path, true);
            
            try
            {
                RetryingTransactionCallback<Object> deleteWebProjectWork = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil.runAs(new RunAsWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                List<SandboxInfo> sbInfos = sandboxFactory.listAllSandboxes(wpStoreId, true, true);
                                
                                for (SandboxInfo sbInfo : sbInfos)
                                {
                                    String sbStoreId = sbInfo.getSandboxId();
                                    
                                    if (WCMUtil.isLocalhostDeployedStore(wpStoreId, sbStoreId))
                                    {
                                        if (getWebProject(WCMUtil.getWebProjectStoreId(sbStoreId)) != null)
                                        {
                                            continue;
                                        }
                                    }
                                    
                                    // delete sandbox (and associated preview sandbox, if it exists)
                                    sandboxFactory.deleteSandbox(sbInfo, false, false);
                                }
                                
                                // delete all web project locks in one go (ie. all those currently held against staging store)
                                avmLockingService.removeLocks(wpStoreId);
                                
                                StoreRef archiveStoreRef = nodeService.getStoreArchiveNode(wpNodeRef.getStoreRef()).getStoreRef();
                                
                                // delete the web project node itself
                                nodeService.deleteNode(wpNodeRef);
                                nodeService.deleteNode(new NodeRef(archiveStoreRef, wpNodeRef.getId()));
                                
                                sandboxFactory.removeGroupsForStore(sandbox);
                                
                                return null;
                            }
                        }, AuthenticationUtil.getSystemUserName());
                        
                        return null;
                    }
                };
                
                transactionService.getRetryingTransactionHelper().doInTransaction(deleteWebProjectWork);
                
                if (logger.isDebugEnabled())
                {
                   logger.debug("Deleted web project: " + wpNodeRef + " in "+(System.currentTimeMillis()-start)+" ms (store id: " + wpStoreId + ")");
                }
            }
            catch (Throwable err)
            {
                throw new AlfrescoRuntimeException("Failed to delete web project: ", err);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isContentManager(java.lang.String)
     */
    public boolean isContentManager(String storeName)
    {
        return isContentManager(storeName, AuthenticationUtil.getFullyAuthenticatedUser());
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
        return isContentManager(wpNodeRef, AuthenticationUtil.getFullyAuthenticatedUser());
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
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebUser(java.lang.String)
     */
    public boolean isWebUser(String wpStoreId)
    {
        return isWebUser(getWebProjectNodeFromStore(wpStoreId));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebUser(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isWebUser(NodeRef wpNodeRef)
    {
        // note: admin is an implied web user (content manager) although will not appear in listWebUsers unless explicitly invited
        return (permissionService.hasPermission(wpNodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebUser(java.lang.String, java.lang.String)
     */
    public boolean isWebUser(String wpStoreId, String username)
    {
        return isWebUser(getWebProjectNodeFromStore(wpStoreId), username);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#isWebUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public boolean isWebUser(final NodeRef wpNodeRef, String userName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                return isWebUser(wpNodeRef);
            }
        }, userName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.webproject.WebProjectService#getWebUserCount(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int getWebUserCount(NodeRef wpNodeRef)
    {
        long start = System.currentTimeMillis();
        
        int cnt = WCMUtil.listWebUserRefs(nodeService, wpNodeRef, false).size();
        
        if (logger.isTraceEnabled())
        {
           logger.trace("Get web user cnt: " + wpNodeRef + "(" + cnt + ") in "+(System.currentTimeMillis()-start)+" ms");
        }
        
        return cnt;
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
        long start = System.currentTimeMillis();
        
        // special case: allow System - eg. to allow user to create their own sandbox on-demand (createAuthorSandbox)
        if (isContentManager(wpNodeRef) 
           || (AuthenticationUtil.getRunAsUser().equals(AuthenticationUtil.getSystemUserName())
           || (permissionService.hasPermission(wpNodeRef, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED)))
        {
            Map<String, String> users = WCMUtil.listWebUsers(nodeService, wpNodeRef);
            
            if (logger.isTraceEnabled())
            {
               logger.trace("List web users: " + wpNodeRef + "(" + users.size() + ") in "+(System.currentTimeMillis()-start)+" ms");
            }
            
            return users;
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
        NodeRef userRef = getWebUserRef(wpNodeRef, userName);
        String userRole = null;
        
        if (userRef != null)
        {
            userRole = (String)nodeService.getProperty(userRef, WCMAppModel.PROP_WEBUSERROLE);
        }
       
        return userRole;
    }
    
    private NodeRef getWebUserRef(NodeRef wpNodeRef, String userName)
    {
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
    
        // Lucene indexing may strip certain international characters or treat them as equivalent so we do string
        // comparisons on the results to ensure an exact match
        Iterator<NodeRef> i = nodes.iterator();
        while (i.hasNext())
        {
            if (!nodeService.getProperty(i.next(), WCMAppModel.PROP_WEBUSERNAME).equals(userName))
            {
                i.remove();                    
            }
        }

        if (nodes.size() == 1)
        {
            return nodes.get(0);
        }
        else if (nodes.size() == 0)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("getWebUserRef: web user ("+userName+") not found in web project: "+wpNodeRef);
            }
        }
        else
        {
            logger.error("getWebUserRef: more than one web user ("+userName+") found in web project: "+wpNodeRef);
        }

        return null;
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
        long start = System.currentTimeMillis();
        
        if (! (isContentManager(wpNodeRef) ||
                permissionService.hasPermission(wpNodeRef, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED))
        {
            throw new AccessDeniedException("Only content managers may invite web users");
        }
        
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        String wpStoreId = wpInfo.getStoreId();
        
        // build a list of managers who will have full permissions on ALL staging areas
        List<String> managers = new ArrayList<String>(4);
        Map<String, NodeRef> webSiteUsers = new HashMap<String, NodeRef>(8);
        List<String> managersToRemove = new LinkedList<String>();
        List<UserRoleWrapper> usersToUpdate = new LinkedList<UserRoleWrapper>();
        
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
       
        List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(wpNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
        
        for (ChildAssociationRef ref : userInfoRefs)
        {
            NodeRef userInfoRef = ref.getChildRef();
            String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
            String userrole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
            
            if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userrole) && managers.contains(username) == false)
            {
                managers.add(username);
            }
              
            // add each existing user to the map which will be rechecked for update changed user permissions
            webSiteUsers.put(username, userInfoRef);
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
                if (webSiteUsers.keySet().contains(userAuth) == false)
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
                    // TODO - split out into separate 'change role'
                    // if user role have been changed then update required properties etc.
                    NodeRef userRef = webSiteUsers.get(userAuth);
                    String oldUserRole = (String)nodeService.getProperty(userRef, WCMAppModel.PROP_WEBUSERROLE);
                    
                    if (!role.equals(oldUserRole))
                    {
                        // change in role
                        Map<QName, Serializable> props = nodeService.getProperties(userRef);
                        props.put(WCMAppModel.PROP_WEBUSERNAME, userAuth);
                        props.put(WCMAppModel.PROP_WEBUSERROLE, role);
                        nodeService.setProperties(userRef, props);
                        
                        if (WCMUtil.ROLE_CONTENT_MANAGER.equals(role))
                        {
                            managersUpdateRequired = true;
                        }
                        else if (WCMUtil.ROLE_CONTENT_MANAGER.equals(oldUserRole))
                        {
                                managersToRemove.add(userAuth);
                        }
                        
                        usersToUpdate.add(sandboxFactory.new UserRoleWrapper(userAuth, oldUserRole, role));
                        
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(userAuth +"'s role has been changed from '" + oldUserRole +
                                         "' to '" + role + "'");
                        }
                    }
                }
           }
        }
          
        // Bind the post-commit transaction listener with data required for virtualization server notification
        CreateSandboxTransactionListener tl = new CreateSandboxTransactionListener(sandboxInfoList, listWebApps(wpNodeRef));
        AlfrescoTransactionSupport.bindListener(tl);
        
        if (managersUpdateRequired == true)
        {
            sandboxFactory.updateSandboxManagers(wpStoreId, managers);
        }
        
        // TODO - split out into separate 'change role'
        // remove ex-managers from sandboxes
        if (managersToRemove.size() != 0)
        {
            sandboxFactory.removeSandboxManagers(wpStoreId, managersToRemove);
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
        
        // TODO - split out into separate 'change role'
        // update user's roles
        if (usersToUpdate.size() != 0)
        {
            sandboxFactory.updateSandboxRoles(wpStoreId, usersToUpdate, perms);
        }
        
        if (logger.isDebugEnabled())
        {
           logger.debug("Invited "+invitedCount+" web users in "+(System.currentTimeMillis()-start)+" ms (store id: "+wpStoreId+")");
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
        long start = System.currentTimeMillis();
        
        if (! (isContentManager(wpNodeRef) ||
               permissionService.hasPermission(wpNodeRef, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED))
        {
            throw new AccessDeniedException("Only content managers may invite web user");
        }
        
        WebProjectInfo wpInfo = getWebProject(wpNodeRef);
        final String wpStoreId = wpInfo.getStoreId();
        
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
        
        // get permissions and roles for a web project folder type
        Set<String> perms = permissionService.getSettablePermissions(WCMAppModel.TYPE_AVMWEBFOLDER);
        
        NodeRef userRef = getWebUserRef(wpNodeRef, userAuth);
        if (userRef != null)
        {
            // TODO - split out into separate 'change role'
            // if user role has been changed then update required properties etc. 
            String oldUserRole = (String)nodeService.getProperty(userRef, WCMAppModel.PROP_WEBUSERROLE);
            if (!role.equals(oldUserRole))
            {
                // change in role
                Map<QName, Serializable> props = nodeService.getProperties(userRef);
                props.put(WCMAppModel.PROP_WEBUSERNAME, userAuth);
                props.put(WCMAppModel.PROP_WEBUSERROLE, role);
                nodeService.setProperties(userRef, props);
                
                if (WCMUtil.ROLE_CONTENT_MANAGER.equals(role))
                {
                    managers.add(userAuth);
                    sandboxFactory.updateSandboxManagers(wpStoreId, managers);
                }
                else if (WCMUtil.ROLE_CONTENT_MANAGER.equals(oldUserRole))
                {
                    List<String> managersToRemove = new LinkedList<String>();
                    managersToRemove.add(userAuth);
                    
                    sandboxFactory.removeSandboxManagers(wpStoreId, managersToRemove);
                }
                
                List<UserRoleWrapper> usersToUpdate = new LinkedList<UserRoleWrapper>();
                usersToUpdate.add(sandboxFactory.new UserRoleWrapper(userAuth, oldUserRole, role));
                
                sandboxFactory.updateSandboxRoles(wpStoreId, usersToUpdate, perms);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Web user "+userAuth +"'s role has been changed from '" + oldUserRole + "' to '" + role + "' in "+(System.currentTimeMillis()-start)+" ms (store id: "+wpStoreId+")");
                }
            }
        }
        else
        {
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
                sandboxFactory.updateSandboxManagers(wpStoreId, managers);
            }
            
            sandboxFactory.addStagingAreaUser(wpStoreId, userAuth, role);
             
            // create an app:webuser instance for the user and assoc to the web project node
            createWebUser(wpNodeRef, userAuth, role);
            
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
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Invited web user: "+userAuth+" in "+(System.currentTimeMillis()-start)+" ms (store id: "+wpStoreId+")");
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
        long start = System.currentTimeMillis();
        
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
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Uninvited web user: "+userAuth+" in "+(System.currentTimeMillis()-start)+" ms (store id: "+wpStoreId+")");
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
     * Create WebProject/WebApp Transaction listener - invoked after commit
     */
    private class CreateWebAppTransactionListener extends TransactionListenerAdapter
    {
        private String wpStoreId;
        private String webApp;

        public CreateWebAppTransactionListener(String wpStoreId, String webApp)
        {
            this.wpStoreId = wpStoreId;
            this.webApp = webApp;
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
               // update the virtualisation server with webapp
               // performed after the main txn has committed successfully
               String newStoreName = WCMUtil.buildStagingStoreName(wpStoreId);
               
               String path = WCMUtil.buildStoreWebappPath(newStoreName, webApp);
               
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
