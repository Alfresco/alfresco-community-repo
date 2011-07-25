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

import java.util.List;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common support for creating home folders This is hooked into node creation events from Person type objects via the
 * homeFolderManager. Provider must all be wired up to the homeFolderManager.
 * 
 * @deprecated 
 * Depreciated since 4.0. {@link AbstractHomeFolderProvider2} should now be used.
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
    private PortableHomeFolderManager homeFolderManager;

    /**
     * The store ref in which to conduct searches
     */
    private StoreRef storeRef;

    /**
     * Service registry to get hold of public services (so that actions are audited)
     */
    private ServiceRegistry serviceRegistry;

    /**
     * The path to a folder
     */
    private String path;

    /**
     * The owner to set on creation of a home folder (if unset this will be the uid).
     */
    private String ownerOnCreate;

    /**
     * PermissionsManager used on creating the home folder
     */
    private PermissionsManager onCreatePermissionsManager;

    /**
     * PermissionsManager used on referencing the home folder
     */
    private PermissionsManager onReferencePermissionsManager;

    /**
     * Adaptor for this instance to be a HomeFolderProvider2
     */
    private V2Adaptor v2Adaptor = new V2Adaptor(this);

    /**
     * Register with the homeFolderManagewr
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "homeFolderManager", homeFolderManager);
        homeFolderManager.addProvider(v2Adaptor);
    }

    /**
     * Get the home folder manager.
     */
    protected PortableHomeFolderManager getHomeFolderManager()
    {
        return homeFolderManager;
    }

    /**
     * Set the home folder manager.
     * @param homeFolderManager
     */
    public void setHomeFolderManager(PortableHomeFolderManager homeFolderManager)
    {
        this.homeFolderManager = homeFolderManager;
    }

    /**
     * Get the provider name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * The provider name is taken from the bean name
     */
    @Override
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
        boolean reset = this.path != null;
        this.path = path;
        
        // If a reset need to clear caches
        if (reset)
        {
            homeFolderManager.clearCaches(v2Adaptor);
        }
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
        // keep class signature but no longer use value
    }

    /**
     * Set the permission manager
     */
    public void setOnCreatePermissionsManager(PermissionsManager onCreatePermissionsManager)
    {
        this.onCreatePermissionsManager = onCreatePermissionsManager;
    }

    /**
     * Gets the PermissionsManager used on creating the home folder
     */
    public PermissionsManager getOnCreatePermissionsManager()
    {
        return onCreatePermissionsManager;
    }

    public void setOnReferencePermissionsManager(PermissionsManager onReferencePermissionsManager)
    {
        this.onReferencePermissionsManager = onReferencePermissionsManager;
    }

    /**
     * Gets the PermissionsManager used on referencing the home folder
     */
    public PermissionsManager getOnReferencePermissionsManager()
    {
        return onReferencePermissionsManager;
    }

    /**
     * Set the authority to use as the owner of all home folder nodes.
     */
    public void setOwnerOnCreate(String ownerOnCreate)
    {
        this.ownerOnCreate = ownerOnCreate;
    }

    /**
     * Get the authority to use as the owner of all home folder nodes.
     */
    public String getOwnerOnCreate()
    {
        return ownerOnCreate;
    }
    
    /**
     * Cache path to node resolution
     */
    protected NodeRef getPathNodeRef()
    {
        return homeFolderManager.getRootPathNodeRef(v2Adaptor);
    }

    /**
     * Utility method to resolve paths to nodes.
     */
    protected NodeRef resolvePath(String pathToResolve)
    {
        return homeFolderManager.resolvePath(v2Adaptor, pathToResolve);
    }

    /**
     * The implementation of the policy binding. Run as the system user for auditing.
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        homeFolderManager.homeFolderCreateAndSetPermissions(v2Adaptor, childAssocRef.getChildRef());
    }

    /**
     * Abstract implementation to find/create the appropriate home space.
     */
    protected abstract HomeSpaceNodeRef getHomeFolder(NodeRef person);
    
    /**
     * Get adaptor for this instance to be a HomeFolderProvider2
     */
    protected V2Adaptor getV2Adaptor()
    {
        return v2Adaptor;
    }
    
    /**
     * Adaptor to the HomeFolderProvider2 interface.
     */
    public class V2Adaptor implements HomeFolderProvider2
    {
        AbstractHomeFolderProvider abstractHomeFolderProvider;
        
        public V2Adaptor(AbstractHomeFolderProvider abstractHomeFolderProvider)
        {
            this.abstractHomeFolderProvider = abstractHomeFolderProvider;
            abstractHomeFolderProvider.v2Adaptor = this;
        }

        @Override
        public String getName()
        {
            return abstractHomeFolderProvider.getName();
        }

        @Override
        public String getStoreUrl()
        {
            return abstractHomeFolderProvider.getStoreRef().toString();
        }

        @Override
        public String getRootPath()
        {
            return abstractHomeFolderProvider.getPath();
        }

        @Override
        public List<String> getHomeFolderPath(NodeRef person)
        {
            return (abstractHomeFolderProvider instanceof UIDBasedHomeFolderProvider)
            ? ((UIDBasedHomeFolderProvider)abstractHomeFolderProvider).getHomeFolderPath(person)
            : null;
        }

        @Override
        public NodeRef getTemplateNodeRef()
        {
            return (abstractHomeFolderProvider instanceof UIDBasedHomeFolderProvider)
            ? ((UIDBasedHomeFolderProvider)abstractHomeFolderProvider).getTemplateNodeRef()
            : null;
        }

        @Override
        public String getOwner()
        {
            return abstractHomeFolderProvider.getOwnerOnCreate();
        }

        @Override
        public PermissionsManager getOnCreatePermissionsManager()
        {
            return abstractHomeFolderProvider.getOnReferencePermissionsManager();
        }

        @Override
        public PermissionsManager getOnReferencePermissionsManager()
        {
            return abstractHomeFolderProvider.getOnReferencePermissionsManager();
        }

        @Override
        public HomeSpaceNodeRef getHomeFolder(NodeRef person)
        {
            return abstractHomeFolderProvider.getHomeFolder(person);
        }

        // The old way to create the home folder, so must still call it in case
        // the method is overridden
        public void onCreateNode(ChildAssociationRef childAssocRef)
        {
            abstractHomeFolderProvider.onCreateNode(childAssocRef);
        }
    }
}
