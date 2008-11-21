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
package org.alfresco.wcm.sandbox;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.namespace.QName;


/**
 * Sandbox Service fundamental API.
 * <p>
 * This service API is designed to support the public facing Sandbox APIs. 
 * 
 * @author janv
 */
public interface SandboxService
{
    /**
     * Create author/user sandbox within a web project for the current user
     * <p>
     * If the author sandbox already exists for this web project then it will be returned
     *
     * @param wpStoreId     web project store id
     * @return SandboxInfo  the created user sandbox info
     */
    public SandboxInfo createAuthorSandbox(String wpStoreId);
    
    /**
     * Create author/user sandbox within a web project for the given user
     * <p>
     * If the author sandbox already exists for this web project then it will be returned
     * <p>
     * Current user must be a content manager for the web project
     *
     * @param wpStoreId     web project store id
     * @param userName      user name
     * @return SandboxInfo  the created user sandbox info
     */
    public SandboxInfo createAuthorSandbox(String wpStoreId, String userName);
    
    /**
     * List the available sandboxes for the current user and given web project
     * 
     * @param wpStoreId           web project store id
     * @return List<SandboxInfo>  list of sandbox info
     */
    public List<SandboxInfo> listSandboxes(String wpStoreId);
    
    /**
     * List the available sandboxes for the given user and web project
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param wpStoreId           web project store id
     * @param userName            user name
     * @return List<SandboxInfo>  list of sandbox info
     */
    public List<SandboxInfo> listSandboxes(String wpStoreId, String userName);
    
    /**
     * Return true if sandbox is visible to user and is of given type
     * <p>
     * eg. isSandboxType("test123--myusername", SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN)
     * 
     * @param sbStoreId    sandbox store id
     * @param sandboxType  sandbox type (see SandboxConstants)
     * @return boolean     true, if sandbox exists with given type
     */
    public boolean isSandboxType(String sbStoreId, QName sandboxType);
    
    /**
     * Get sandbox info
     * 
     * @param sbStoreId     sandbox store id
     * @return SandboxInfo  null if sandbox does not exist or is not visible to the current user
     */
    public SandboxInfo getSandbox(String sbStoreId);
    
    /**
     * Gets author/user sandbox info for the current user
     * <p>
     * Returns null if the author sandbox can not be found
     * 
     * @param wpStoreId      web project store id
     * @return SandboxInfo   author sandbox info
     */
    public SandboxInfo getAuthorSandbox(String wpStoreId);
    
    /**
     * Gets author/user sandbox info for the given user
     * <p>
     * Returns null if the user sandbox can not be found
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param wpStoreId      web project store id
     * @param userName       userName
     * @return SandboxInfo   author sandbox info
     */
    public SandboxInfo getAuthorSandbox(String wpStoreId, String userName);
    
    /**
     * Gets staging sandbox info
     * <p>
     * Returns null if the staging sandbox can not be found
     * 
     * @param wpStoreId      web project store id
     * @return SandboxInfo   staging sandbox info
     */
    public SandboxInfo getStagingSandbox(String wpStoreId);
    
    /**
     * Delete the sandbox
     * <p>
     * If the sandbox does not exist, will log a warning and succeed
     * <p>
     * Current user must be a content manager for the web project (associated with the sandbox)
     * 
     * @param sbStoreId  sandbox store id
     */
    public void deleteSandbox(String sbStoreId);
    
    /**
     * List changed items for given sandbox (eg. for user sandbox compared to staging sandbox)
     * <p>
     * Note: This will list new/modified/deleted items from the root directory and below, including all web apps
     * 
     * @param sbStoreId                 sandbox store id
     * @param  includeDeleted           if true, include deleted items as well as new/modified items
     * @return List<AVMNodeDescriptor>  list of changed items
     */
    public List<AVMNodeDescriptor> listChangedItems(String sbStoreId, boolean includeDeleted);
    
    /**
     * List changed items for given sandbox and web app (eg. in user sandbox)
     * <p>
     * Note: This will list new/modified/deleted items for the given web app
     *
     * @param sbStoreId                 sandbox store id
     * @param webApp                    web app to filter by
     * @param  includeDeleted           if true, include deleted items as well as new/modified items
     * @return List<AVMNodeDescriptor>  list of changed items
     */
    public List<AVMNodeDescriptor> listChangedItemsWebApp(String sbStoreId, String webApp, boolean includeDeleted);
    
    /**
     * List changed items for given sandbox path (eg. between user sandbox and staging sandbox)
     * <p>
     * Note: This will list new/modified/deleted items from the directory and below. The destination path will be dervied.
     *
     * @param  avmSrcPath               source sandbox path (an AVM path)
     * @param  includeDeleted           if true, include deleted items as well as new/modified items
     * @return List<AVMNodeDescriptor>  list of changed items
     */
    public List<AVMNodeDescriptor> listChangedItemsDir(String avmSrcPath, boolean includeDeleted);
    
    /**
     * List changed (new/modified/deleted) items between any two sandbox paths
     * 
     * @param  avmSrcPath               source sandbox path (an AVM path)
     * @param  avmDstPath               destination sandbox path (an AVM path)
     * @param  includeDeleted           if true, include deleted items as well as new/modified items
     * @return List<AVMNodeDescriptor>  list of changed items
     */
    public List<AVMNodeDescriptor> listChangedItems(String avmSrcPath, String avmDstPath, boolean includeDeleted);
    
    /**
     * Submit all changed items for given sandbox (eg. from user sandbox to staging sandbox)
     * <p>
     * Note: This will submit new/modified/deleted items from the root directory and below, including all web apps
     * <p>
     * @param sbStoreId  sandbox store id
     */
    public void submitAll(String sbStoreId, String submitLabel, String submitComment);
    
    /**
     * Submit all changed items for given sandbox and web app (eg. in user sandbox)
     * <p>
     * Note: This will submit new/modified/deleted items for the given web app
     * 
     * @param sbStoreId  sandbox store id
     * @param webApp     web app to filter by
     */
    public void submitAllWebApp(String sbStoreId, String webApp, String submitLabel, String submitComment);
    
    /**
     * Submit all changed items for given sandbox path (eg. in user sandbox)
     * <p>
     * Note: This will submit new/modified/deleted items from the directory and below
     * 
     * @param avmDirectoryPath  path to filter by
     */
    public void submitAllDir(String avmDirectoryPath, String submitLabel, String submitComment);
    
    /**
     * Submit list of changed items for given sandbox (eg. from user sandbox to staging sandbox)
     * 
     * @param sbStoreId  sandbox store id
     * @param items      list of AVM node descriptors
     */
    public void submitList(String sbStoreId, List<AVMNodeDescriptor> items, String submitLabel, String submitComment);
    
    /**
     * Submit list of changed items for given sandbox (eg. from user sandbox to staging sandbox)
     * 
     * @param sbStoreId        sandbox store id
     * @param items            list of AVM node descriptors
     * @param expirationDates  map of <path, date> for those items set with an expiration date, or can be null (if no expiration dates)
     */
    public void submitList(String sbStoreId, List<AVMNodeDescriptor> items, Map<String, Date> expirationDates, final String submitLabel, final String submitComment);
    
    /**
     * Revert all changed items for given sandbox (eg. in user sandbox)
     * <p>
     * Note: This will revert new/modified/deleted items from the root directory and below, including all web apps
     *
     * @param sbStoreId  sandbox store id
     */
    public void revertAll(String sbStoreId);
    
    /**
     * Revert all changed items for given sandbox and web app (eg. in user sandbox)
     * <p>
     * Note: This will revert new/modified/deleted items for the given web app
     * 
     * @param sbStoreId  sandbox store id
     * @param webApp     web app to filter by
     */
    public void revertAllWebApp(String sbStoreId, String webApp);
    
    /**
     * Revert all changed items for given sandbox path (eg. in user sandbox)
     * <p>
     * Note: This will revert new/modified/deleted items from the directory and below
     * 
     * @param avmDirectoryPath  path to filter by
     */
    public void revertAllDir(String avmDirectoryPath);
    
    /**
     * Revert list of changed items for given sandbox (eg. in user sandbox)
     * 
     * @param items             list of AVM node descriptors
     */
    public void revertList(String sbStoreId, List<AVMNodeDescriptor> items);
    
    /**
     * Revert sandbox to a specific snapshot version ID (ie. for staging sandbox)
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param sbStoreId  staging sandbox store id
     * @param version    version
     */
    public void revertSnapshot(String sbStoreId, int version);
    
    /**
     * List all snapshots (sandbox versions) for the given sandbox (ie. for staging sandbox)
     * <p>
     * Current user must be a content manager for the web project
     *  
     * @param sbStoreId                 staging sandbox store id
     * @param includeSystemGenerated    if false will ignore system generated snapshots else true to get all snapshots
     * @return List<VersionDescriptor>  list of AVM version descriptors
     */
    public List<VersionDescriptor> listSnapshots(String sbStoreId, boolean includeSystemGenerated);
    
    /**
     * List snapshots (sandbox versions) for the given sandbox between given dates (ie. for staging sandbox)
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param sbStoreId                 staging sandbox store id
     * @param from                      from date
     * @param to                        to date
     * @param includeSystemGenerated    if false will ignore system generated snapshots else true to get all snapshots
     * @return List<VersionDescriptor>  list of AVM version descriptors
     */
    public List<VersionDescriptor> listSnapshots(String sbStoreId, Date from, Date to, boolean includeSystemGenerated);
    
}
