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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface for home folder providers. Instances work with the 
 * {@link HomeFolderManager} (which performs most of the work)
 * to allow it to create home folders in custom locations.
 * 
 * The home folder may be a simple structure where all users share a root folder (See
 * {@link ExistingPathBasedHomeFolderProvider2}), or all home folders are in the same root
 * folder (See {@link UsernameHomeFolderProvider}) or in a tree of sub folders to
 * avoids any single directory containing too many home directories which might cause
 * performance issues (See {@link RegexHomeFolderProvider}).<p>
 * 
 * If the HomeFolderProvider is changed, home folders may be
 * moved by using the {@link HomeFolderProviderSynchronizer} which optionally runs on
 * restart.
 * 
 * @author Andy Hind, Alan Davis (support v1 and v2 HomeFolderProviders)
 */
public interface HomeFolderProvider2
{
    /**
     * Get the name of the provider (the bean name).
     */
    String getName();
    
    /**
     * Get the URL String of the node store that will be used.
     */
    String getStoreUrl();

    /**
     * Get the root path in the store under which all home folders will be located.
     */
    String getRootPath();

    /**
     * Returns a preferred path (a list of folder names) for the home folder relative to
     * the root path. If all users share the root, the returned value should be an empty
     * List or {@code null}. When all users have their own folder under the root
     * there should be just one element in the List. Multiple elements should be returned
     * when a nested folder structure is preferred.
     * @param person NodeRef from which a property (normally the userName) is used as a
     *        hash key to create a nested directory structure.
     * @return the path to be used. 
     */
    List<String> getHomeFolderPath(NodeRef person);
    
    /**
     * Returns a node to copy (a template) for the home folder.
     * Only used by HomeFolderProviders that create home folders rather 
     * than just reference existing folders.
     * @return the node to copy or {@code null} if not required.
     */
    NodeRef getTemplateNodeRef();
    
    /**
     * Set the authority to use as the owner of all home folder nodes.
     * If {@code null} the {@link ContentModel.PROP_USERNAME} value of
     * the person is used.
     */
    String getOwner();
    
    /**
     * Gets the PermissionsManager used on creating the home folder
     */
    PermissionsManager getOnCreatePermissionsManager();

    /**
     * Gets the PermissionsManager used on referencing the home folder
     */
    PermissionsManager getOnReferencePermissionsManager();

    /**
     * Callback from {@link HomeFolderManager} to locate or create a home folder.
     * Implementations normally call {@link HomeFolderManager.getHomeFolder}.
     */
    HomeSpaceNodeRef getHomeFolder(NodeRef person);
}
