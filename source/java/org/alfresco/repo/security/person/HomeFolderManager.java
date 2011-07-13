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
package org.alfresco.repo.security.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Manage home folder creation by binding to events from the cm:person type.
 *  
 * @author Andy Hind,
 *         Alan Davis (support v1 and v2 HomeFolderProviders - code from
 *                     v1 HomeFolderProviders moved into HomeFolderManager).
 */
public class HomeFolderManager implements NodeServicePolicies.OnCreateNodePolicy
{
    private PolicyComponent policyComponent;

    private NodeService nodeService;
    
    private boolean enableHomeFolderCreationAsPeopleAreCreated = false;

    private ServiceRegistry serviceRegistry;

    private TenantService tenantService;
    
    /**
     * A default provider
     */
    private HomeFolderProvider2 defaultProvider;

    /**
     * Original Providers (now depreciated) that have registered and are looked up by bean name.
     */
    @SuppressWarnings("deprecation")
    private Map<String, HomeFolderProvider> v1Providers = new HashMap<String, HomeFolderProvider>();

    /**
     * Providers that have registered and are looked up by bean name.
     */
    private Map<String, HomeFolderProvider2> v2Providers = new HashMap<String, HomeFolderProvider2>();

    /**
     * Cache the result of the path look up.
     */
    private Map<String, Map<String, NodeRef>> rootPathNodeRefMaps =
        new ConcurrentHashMap<String, Map<String, NodeRef>>();

    /**
     * Bind the class behaviour to this implementation
     */
    public void init() throws Exception
    {
        if (enableHomeFolderCreationAsPeopleAreCreated)
        {
            policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode"));
        }
    }

    public void setEnableHomeFolderCreationAsPeopleAreCreated(boolean enableHomeFolderCreationAsPeopleAreCreated)
    {
        this.enableHomeFolderCreationAsPeopleAreCreated = enableHomeFolderCreationAsPeopleAreCreated;
    }

    /**
     * Set the policy component.
     * 
     * @param policyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the node service.
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the service registry.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Set the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Register a home folder provider.
     * 
     * @param provider
     */
    @SuppressWarnings("deprecation")
    public void addProvider(HomeFolderProvider provider)
    {
        v1Providers.put(provider.getName(), provider);
    }

    /**
     * Register a home folder provider.
     * 
     * @param provider
     */
    public void addProvider(HomeFolderProvider2 provider)
    {
        v2Providers.put(provider.getName(), provider);
    }
    
    /**
     * Returns the version 1 HomeFolderProvider with the given name.
     */
    @SuppressWarnings("deprecation")
    public HomeFolderProvider getHomeFolderProvider1(String providerName)
    {
        return v1Providers.get(providerName);
    }
    
    /**
     * Returns the version 2 HomeFolderProvider2 with the given name.
     */
    public HomeFolderProvider2 getHomeFolderProvider2(String providerName)
    {
        return v2Providers.get(providerName);
    }

    /**
     * Set the default home folder provider (user which none is specified or when one is not found)
     * @param defaultProvider
     */
    public void setDefaultProvider(HomeFolderProvider2 defaultProvider)
    {
        this.defaultProvider = defaultProvider;
    }

    /**
     * Find the provider and call if eager home folder creation is enabled.
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        if (enableHomeFolderCreationAsPeopleAreCreated)
        {
            makeHomeFolder(childAssocRef);
        }
    }

    /**
     * Find the provider and call.
     */
    @SuppressWarnings("deprecation")
    public void makeHomeFolder(ChildAssociationRef childAssocRef)
    {
        HomeFolderProvider2 v2Provider = defaultProvider;
        HomeFolderProvider v1Provider = null;
        String providerName = DefaultTypeConverter.INSTANCE.convert(
                String.class, nodeService.getProperty(childAssocRef
                .getChildRef(), ContentModel.PROP_HOME_FOLDER_PROVIDER));
        if (providerName != null)
        {
            v2Provider = getHomeFolderProvider2(providerName);
            if (v2Provider == null)
            {
                v1Provider = getHomeFolderProvider1(providerName);
                if (v1Provider == null)
                {
                    v2Provider = defaultProvider;
                }
            }
        }
        else
        {
            providerName = defaultProvider.getName();
            nodeService.setProperty(childAssocRef.getChildRef(),
                    ContentModel.PROP_HOME_FOLDER_PROVIDER, providerName);
        }
        if (v2Provider != null)
        {
            // If a V2Adaptor we still must call onCreateNode just like a
            // v1 HomeFolderProvider in case it has been overridden
            if (v2Provider instanceof AbstractHomeFolderProvider.V2Adaptor)
            {
                ((AbstractHomeFolderProvider.V2Adaptor)v2Provider).onCreateNode(childAssocRef);
            }
            else
            {
                homeFolderCreateAndSetPermissions(v2Provider, childAssocRef.getChildRef());
            }
        }
        else if (v1Provider != null)
        {
            v1Provider.onCreateNode(childAssocRef);
        }
    }

    void homeFolderCreateAndSetPermissions(HomeFolderProvider2 provider, NodeRef personNodeRef)
    {
        AuthenticationUtil.RunAsWork<NodeRef> action =
            new RunAsCreateAndSetPermissions(provider, personNodeRef);
        AuthenticationUtil.runAs(action, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Helper class to encapsulate the creation and setting permissions etc
     */
    private class RunAsCreateAndSetPermissions implements AuthenticationUtil.RunAsWork<NodeRef>
    {
        NodeRef personNodeRef;
        HomeFolderProvider2 provider;

        RunAsCreateAndSetPermissions(HomeFolderProvider2 provider, NodeRef personNodeRef)
        {
            this.personNodeRef = personNodeRef;
            this.provider = provider;
        }

        public NodeRef doWork() throws Exception
        {
            // Get home folder
            HomeSpaceNodeRef homeFolder = provider.getHomeFolder(personNodeRef);
            
            // If it exists
            if (homeFolder.getNodeRef() != null)
            {
                // Get uid and keep
                String uid = DefaultTypeConverter.INSTANCE.convert(String.class,
                        serviceRegistry.getNodeService().getProperty(
                        personNodeRef, ContentModel.PROP_USERNAME));

                // If created or found then set (other wise it was already set correctly)
                if (homeFolder.getStatus() != HomeSpaceNodeRef.Status.VALID)
                {
                    serviceRegistry.getNodeService().setProperty(
                            personNodeRef, ContentModel.PROP_HOMEFOLDER, homeFolder.getNodeRef());
                }

                final String providerSuppliedOwner = provider.getOwner();
                String owner = (providerSuppliedOwner == null) ? uid : providerSuppliedOwner;
                // If created..
                if (homeFolder.getStatus() == HomeSpaceNodeRef.Status.CREATED)
                {
                    PermissionsManager onCreatePermissionsManager =
                        provider.getOnCreatePermissionsManager();
                    if (onCreatePermissionsManager != null)
                    {
                        onCreatePermissionsManager.setPermissions(
                                homeFolder.getNodeRef(), owner, uid);
                    }
                }
                else
                {
                    PermissionsManager onReferencePermissionsManager =
                        provider.getOnReferencePermissionsManager();
                    if (onReferencePermissionsManager != null)
                    {
                        onReferencePermissionsManager.setPermissions(
                                homeFolder.getNodeRef(), owner, uid);
                    }
                }
            }
            return homeFolder.getNodeRef();
        }
    }
    
    private StoreRef getStoreRef(HomeFolderProvider2 provider)
    {
        // Could check to see if provider is a V2Adaptor to avoid
        // object creation, but there is little point.
        return new StoreRef(provider.getStoreUrl());
    }
    
    /**
     * Helper method for {@link HomeFolderProvider2.getHomeFolder} (so that it
     * does not need its own NodeService) that returns a person property value.
     */
    public String getPersonProperty(NodeRef person, QName name)
    {
        String value = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(person, name));
        
        if(value == null || value.length() == 0)
        {
            throw new PersonException("Can not create a home folder when the "+name+" property is null or empty");
        }
        return value;
    }
    
    void clearCaches(HomeFolderProvider2 provider)
    {
        getRootPathNodeRefMap(provider).clear();
    }
    
    NodeRef getRootPathNodeRef(HomeFolderProvider2 provider)
    {
        String rootPath = provider.getRootPath();
        String tenantDomain = (tenantService != null ? tenantService.getCurrentUserDomain() : TenantService.DEFAULT_DOMAIN);
        Map<String, NodeRef> rootPathNodeRefMap = getRootPathNodeRefMap(provider);
        NodeRef rootPathNodeRef = rootPathNodeRefMap.get(tenantDomain);
        if (rootPathNodeRef == null)
        {
            // ok with race condition for initial construction
            rootPathNodeRef = resolvePath(provider, rootPath);
            rootPathNodeRefMap.put(tenantDomain, rootPathNodeRef);
        }
        return rootPathNodeRef;
    }
    
    private Map<String, NodeRef> getRootPathNodeRefMap(HomeFolderProvider2 provider)
    {
        String name = provider.getName();
        Map<String, NodeRef> rootPathNodeRefMap = rootPathNodeRefMaps.get(name);
        if (rootPathNodeRefMap == null)
        {
            // ok with race condition for initial construction
            rootPathNodeRefMap = new ConcurrentHashMap<String, NodeRef>();
            rootPathNodeRefMaps.put(name, rootPathNodeRefMap);
        }
        return rootPathNodeRefMap;
    }

    /**
     * Utility method to resolve paths to nodes.
     */
    NodeRef resolvePath(HomeFolderProvider2 provider, String pathToResolve)
    {
        List<NodeRef> refs = serviceRegistry.getSearchService().selectNodes(
                serviceRegistry.getNodeService().getRootNode(getStoreRef(provider)),
                pathToResolve, null,
                serviceRegistry.getNamespaceService(), false);
        if (refs.size() != 1)
        {
            throw new IllegalStateException("Non-unique path: found : " +
                    pathToResolve + " " + refs.size());
        }
        return refs.get(0);
    }

    /**
     * Helper method for {@link HomeFolderProvider2.getHomeFolder(NodeRef)}
     * implementations to return a {@link HomeSpaceNodeRef}
     * @param referenceRootNode indicates that a reference to the root node
     *        should be returned if the home folder property on the person
     *        has not yet been set.
     */
    public HomeSpaceNodeRef getHomeFolder(HomeFolderProvider2 provider, NodeRef person, boolean referenceRootNode)
    {
        HomeSpaceNodeRef homeSpaceNodeRef = null;
        NodeRef existingHomeFolder = DefaultTypeConverter.INSTANCE.convert(
                NodeRef.class, serviceRegistry.getNodeService().getProperty(
                person, ContentModel.PROP_HOMEFOLDER));
        if (existingHomeFolder != null)
        {
            homeSpaceNodeRef = new HomeSpaceNodeRef(existingHomeFolder,
                    HomeSpaceNodeRef.Status.VALID);
        }
        else if (referenceRootNode)
        {
            homeSpaceNodeRef = new HomeSpaceNodeRef(getRootPathNodeRef(provider),
                    HomeSpaceNodeRef.Status.REFERENCED);
        }
        else
        {
            FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
            List<String> homeFolderPath = provider.getHomeFolderPath(person);
            
            FileInfo fileInfo;

            // Test if it already exists
            NodeRef existing = getExisting(provider, fileFolderService, homeFolderPath);
            if (existing != null)
            {
                fileInfo = fileFolderService.getFileInfo(existing);
            }
            else
            {
                fileInfo = createTree(provider, getRootPathNodeRef(provider), homeFolderPath,
                        provider.getTemplateNodeRef(), fileFolderService);
            }
            NodeRef homeFolderNodeRef = fileInfo.getNodeRef();
            return new HomeSpaceNodeRef(homeFolderNodeRef, HomeSpaceNodeRef.Status.CREATED);
        }
        return homeSpaceNodeRef;
    }

    private NodeRef getExisting(HomeFolderProvider2 provider, FileFolderService fileFolderService,
            List<String> homeFolderPath)
    {
        NodeRef existing;
        try
        {
            FileInfo existingFileInfo = fileFolderService.resolveNamePath(getRootPathNodeRef(provider), homeFolderPath);
            existing = existingFileInfo.getNodeRef();
        }
        catch (FileNotFoundException fnfe)
        {
            existing = null;// home folder noderef doesn't exist yet
        }
        return existing;
    }

    /**
     * creates a tree of folder nodes based on the path elements provided.
     */
    private FileInfo createTree(HomeFolderProvider2 provider, NodeRef root,
            List<String> homeFolderPath, NodeRef templateNodeRef,
            FileFolderService fileFolderService)
    {
        NodeRef newParent = createNewParentIfRequired(root, homeFolderPath, fileFolderService);
        String homeFolderName = homeFolderPath.get(homeFolderPath.size()-1);
        FileInfo fileInfo;
        if (templateNodeRef == null)
        {
            fileInfo = fileFolderService.create(
                    newParent,
                    homeFolderName,
                    ContentModel.TYPE_FOLDER);
        }
        else
        {
            try
            {
                fileInfo = fileFolderService.copy(
                        templateNodeRef,
                        newParent,
                        homeFolderName);
            }
            catch (FileNotFoundException e)
            {
                throw new PersonException("Invalid template to create home space");
            }
        }
        return fileInfo;
    }
    
    private NodeRef createNewParentIfRequired(NodeRef root,
            List<String> homeFolderPath, FileFolderService fileFolderService)
    {
        if (homeFolderPath.size() > 1)
        {
            List<String> parentPath = new ArrayList<String>(homeFolderPath);
            parentPath.remove(parentPath.size()-1);
            return FileFolderUtil.makeFolders(fileFolderService, root,
                    parentPath, ContentModel.TYPE_FOLDER).getNodeRef();
        }
        else
        {
            return root;
        }
    }
}
