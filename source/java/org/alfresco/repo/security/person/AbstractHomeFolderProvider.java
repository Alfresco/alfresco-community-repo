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
package org.alfresco.repo.security.person;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common support for creating home folders This is hooked into node creation events from Person type objects via the
 * homeFolderManager. Provider must all be wired up to the homeFolderManager.
 * 
 * @author Andy Hind
 */
public abstract class AbstractHomeFolderProvider implements HomeFolderProvider, BeanNameAware, InitializingBean
{
    /**
     * The provider name
     */
    private String name;

    /**
     * The home folder manager
     */
    private HomeFolderManager homeFolderManager;

    /**
     * The store ref in which to conduct searches
     */
    private StoreRef storeRef;

    /**
     * Service registry to get hold of public services (so taht actions are audited)
     */
    private ServiceRegistry serviceRegistry;

    /**
     * Tenant service - required for MT-enabled environment, else optional
     */
    private TenantService tenantService;
    
    /**
     * The path to a folder
     */
    private String path;

    /**
     * Cache the result of the path look up.
     */
    private Map<String, NodeRef> pathNodeRefs; // MT-aware

    /**
     * The owner to set on creation of a home folder (if unset this will be the uid).
     */
    private String ownerOnCreate;

    private PermissionsManager onCreatePermissionsManager;

    private PermissionsManager onReferencePermissionsManager;

    public AbstractHomeFolderProvider()
    {
        super();
        
        pathNodeRefs = new ConcurrentHashMap<String, NodeRef>();
    }

    /**
     * Register with the homeFolderManagewr
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "homeFolderManager", homeFolderManager);
        homeFolderManager.addProvider(this);
    }

    // === //
    // IOC //
    // === //

    /**
     * Get the home folder manager.
     */
    protected HomeFolderManager getHomeFolderManager()
    {
        return homeFolderManager;
    }

    /**
     * Set the home folder manager.
     * 
     * @param homeFolderManager
     */
    public void setHomeFolderManager(HomeFolderManager homeFolderManager)
    {
        this.homeFolderManager = homeFolderManager;
    }

    /**
     * Get the provider name
     */
    public String getName()
    {
        return name;
    }

    /**
     * The provider name is taken from the bean name
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Get the path
     */
    protected String getPath()
    {
        return path;
    }

    /**
     * Set the path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Get the store ref
     */
    protected StoreRef getStoreRef()
    {
        return storeRef;
    }

    /**
     * Set the store ref
     */
    public void setStoreRef(StoreRef storeRef)
    {
        this.storeRef = storeRef;
    }

    /**
     * Set the store from the string url.
     */
    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    /**
     * Get the service registry.
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
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
     * Set the permission manager
     */
    public void setOnCreatePermissionsManager(PermissionsManager onCreatePermissionsManager)
    {
        this.onCreatePermissionsManager = onCreatePermissionsManager;
    }

    public void setOnReferencePermissionsManager(PermissionsManager onReferencePermissionsManager)
    {
        this.onReferencePermissionsManager = onReferencePermissionsManager;
    }

    /**
     * Set the authority to use as the owner of all home folder nodes.
     */
    public void setOwnerOnCreate(String ownerOnCreate)
    {
        this.ownerOnCreate = ownerOnCreate;
    }

    /**
     * Cache path to node resolution
     */
    protected NodeRef getPathNodeRef()
    {
        String tenantDomain = (tenantService != null ? tenantService.getCurrentUserDomain() : TenantService.DEFAULT_DOMAIN);
        
        NodeRef pathNodeRef = pathNodeRefs.get(tenantDomain);
        if (pathNodeRef == null)
        {
            pathNodeRef = resolvePath(path);
            pathNodeRefs.put(tenantDomain, pathNodeRef);
        }
        return pathNodeRef;
    }

    /**
     * Utility metho to resolve paths to nodes.
     */
    protected NodeRef resolvePath(String pathToResolve)
    {
        List<NodeRef> refs = serviceRegistry.getSearchService().selectNodes(serviceRegistry.getNodeService().getRootNode(storeRef), pathToResolve, null,
                serviceRegistry.getNamespaceService(), false);
        if (refs.size() != 1)
        {
            throw new IllegalStateException("Non-unique path: found : " + pathToResolve + " " + refs.size());
        }
        return refs.get(0);
    }

    /**
     * The implementation of the policy binding. Run as the system user for auditing.
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        AuthenticationUtil.RunAsWork<NodeRef> action = new OnCreateNode(childAssocRef);
        AuthenticationUtil.runAs(action, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Abstract implementation to find/create the approriate home space.
     */
    protected abstract HomeSpaceNodeRef getHomeFolder(NodeRef person);

    /**
     * Helper class to encapsulate the createion settinhg permissions etc
     * 
     * @author Andy Hind
     */
    private class OnCreateNode implements AuthenticationUtil.RunAsWork<NodeRef>
    {
        ChildAssociationRef childAssocRef;

        OnCreateNode(ChildAssociationRef childAssocRef)
        {
            this.childAssocRef = childAssocRef;
        }

        public NodeRef doWork() throws Exception
        {

            // Find person
            NodeRef personNodeRef = childAssocRef.getChildRef();
            // Get home folder
            HomeSpaceNodeRef homeFolder = getHomeFolder(personNodeRef);
            // If it exists
            if (homeFolder.getNodeRef() != null)
            {
                // Get uid and keep
                String uid = DefaultTypeConverter.INSTANCE.convert(String.class, serviceRegistry.getNodeService().getProperty(personNodeRef, ContentModel.PROP_USERNAME));

                // If created or found then set (other wise it was already set correctly)
                if (homeFolder.getStatus() != HomeSpaceNodeRef.Status.VALID)
                {
                    serviceRegistry.getNodeService().setProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER, homeFolder.getNodeRef());
                }

                String ownerToSet = ownerOnCreate == null ? uid : ownerOnCreate;
                // If created..
                if (homeFolder.getStatus() == HomeSpaceNodeRef.Status.CREATED)
                {
                    if (onCreatePermissionsManager != null)
                    {
                        onCreatePermissionsManager.setPermissions(homeFolder.getNodeRef(), ownerToSet, uid);
                    }
                }
                else
                {
                    if (onReferencePermissionsManager != null)
                    {
                        onReferencePermissionsManager.setPermissions(homeFolder.getNodeRef(), ownerToSet, uid);
                    }
                }

            }
            return homeFolder.getNodeRef();

        }
    }

}
