/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.security.person;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common support for creating home folders This is hooked into node creation events from Person type objects via the homeFolderManager. Provider must all be wired up to the
 * homeFolderManager.
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
     * The path to a folder
     */
    private String path;

    /**
     * Cache the result of the path look up.
     */
    private NodeRef pathNodeRef;

    /**
     * The owner to set on creation of a home folder (if unset this will be the uid).
     */
    private String ownerOnCreate;

    /**
     * Set if permissions are inherited when nodes are created.
     */
    private boolean inheritsPermissionsOnCreate = false;

    /**
     * A set of permissions to set for the owner when a home folder is created
     */
    private Set<String> ownerPemissionsToSetOnCreate;

    /**
     * General permissions to set on the node Map<(String)uid, Set<(String)permission>>.
     */
    private Map<String, Set<String>> permissionsToSetOnCreate;

    /**
     * Permissions to set for the user - on create and reference.
     */
    private Set<String> userPemissions;

    /**
     * Clear existing permissions on new home folders (useful of created from a template.
     */
    private boolean clearExistingPermissionsOnCreate = false;

    public AbstractHomeFolderProvider()
    {
        super();
    }

    /**
     * Register with the homeFolderManagewr
     */
    public void afterPropertiesSet() throws Exception
    {
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
     * 
     * @return
     */
    protected String getPath()
    {
        return path;
    }

    /**
     * Set the path
     * 
     * @param path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Get the store ref
     * 
     * @return
     */
    protected StoreRef getStoreRef()
    {
        return storeRef;
    }

    /**
     * Set the store ref
     * 
     * @param storeRef
     */
    public void setStoreRef(StoreRef storeRef)
    {
        this.storeRef = storeRef;
    }

    /**
     * Set the store from the string url.
     * 
     * @param storeUrl
     */
    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    /**
     * Get the service registry.
     * 
     * @return
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /**
     * Set the service registry.
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Inherit permissions when home folder are created?
     * 
     * @param inheritsPermissionsOnCreate
     */
    public void setInheritsPermissionsOnCreate(boolean inheritsPermissionsOnCreate)
    {
        this.inheritsPermissionsOnCreate = inheritsPermissionsOnCreate;
    }

    /**
     * The owner to set on create.
     * 
     * @param ownerOnCreate
     */
    public void setOwnerOnCreate(String ownerOnCreate)
    {
        this.ownerOnCreate = ownerOnCreate;
    }

    /**
     * The owner permissions to set on create.
     * 
     * @param ownerPemissionsToSetOnCreate
     */
    public void setOwnerPemissionsToSetOnCreate(Set<String> ownerPemissionsToSetOnCreate)
    {
        this.ownerPemissionsToSetOnCreate = ownerPemissionsToSetOnCreate;
    }

    /**
     * General permissions to set on create.
     * 
     * @param permissionsToSetOnCreate
     */
    public void setPermissionsToSetOnCreate(Map<String, Set<String>> permissionsToSetOnCreate)
    {
        this.permissionsToSetOnCreate = permissionsToSetOnCreate;
    }

    /**
     * User permissions to set on create and on reference.
     * 
     * @param userPemissions
     */
    public void setUserPemissions(Set<String> userPemissions)
    {
        this.userPemissions = userPemissions;
    }

    /**
     * Clear exising permissions on create. Useful to clear permissions from a template.
     * 
     * @param clearExistingPermissionsOnCreate
     */
    public void setClearExistingPermissionsOnCreate(boolean clearExistingPermissionsOnCreate)
    {
        this.clearExistingPermissionsOnCreate = clearExistingPermissionsOnCreate;
    }

    /**
     * Cache path to node resolution/
     * 
     * @return
     */
    protected synchronized NodeRef getPathNodeRef()
    {
        if (pathNodeRef == null)
        {
            pathNodeRef = resolvePath(path);
        }
        return pathNodeRef;
    }

    /**
     * Utility metho to resolve paths to nodes.
     * 
     * @param pathToResolve
     * @return
     */
    protected NodeRef resolvePath(String pathToResolve)
    {
        List<NodeRef> refs = serviceRegistry.getSearchService().selectNodes(
                serviceRegistry.getNodeService().getRootNode(storeRef), pathToResolve, null,
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
     * 
     * @param person
     * @return
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
                String uid = DefaultTypeConverter.INSTANCE.convert(String.class, serviceRegistry.getNodeService()
                        .getProperty(personNodeRef, ContentModel.PROP_USERNAME));

                // If created or found then set (other wise it was already set correctly)
                if (homeFolder.getStatus() != HomeSpaceNodeRef.Status.VALID)
                {
                    serviceRegistry.getNodeService().setProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER,
                            homeFolder.getNodeRef());
                }

                // If created..
                if (homeFolder.getStatus() == HomeSpaceNodeRef.Status.CREATED)
                {
                    // Set to a specified owner or make owned by the person.
                    if (ownerOnCreate != null)
                    {
                        serviceRegistry.getOwnableService().setOwner(homeFolder.getNodeRef(), ownerOnCreate);
                    }
                    else
                    {

                        serviceRegistry.getOwnableService().setOwner(homeFolder.getNodeRef(), uid);
                    }

                    // clear permissions - useful of not required from a template

                    if (clearExistingPermissionsOnCreate)
                    {
                        serviceRegistry.getPermissionService().deletePermissions(homeFolder.getNodeRef());
                    }

                    // inherit permissions

                    serviceRegistry.getPermissionService().setInheritParentPermissions(homeFolder.getNodeRef(),
                            inheritsPermissionsOnCreate);

                    // Set owner permissions

                    if (ownerPemissionsToSetOnCreate != null)
                    {
                        for (String permission : ownerPemissionsToSetOnCreate)
                        {
                            serviceRegistry.getPermissionService().setPermission(homeFolder.getNodeRef(),
                                    PermissionService.OWNER_AUTHORITY, permission, true);
                        }
                    }

                    // Add other permissions

                    if (permissionsToSetOnCreate != null)
                    {
                        for (String user : permissionsToSetOnCreate.keySet())
                        {
                            Set<String> set = permissionsToSetOnCreate.get(user);
                            if (set != null)
                            {
                                for (String permission : set)
                                {
                                    serviceRegistry.getPermissionService().setPermission(homeFolder.getNodeRef(), user,
                                            permission, true);
                                }
                            }
                        }
                    }
                }

                // Add user permissions on create and reference

                if (userPemissions != null)
                {
                    for (String permission : userPemissions)
                    {
                        serviceRegistry.getPermissionService().setPermission(homeFolder.getNodeRef(), uid, permission,
                                true);
                    }
                }
            }
            return homeFolder.getNodeRef();

        }
    }

}
