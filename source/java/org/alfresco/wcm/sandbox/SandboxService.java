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
package org.alfresco.wcm.sandbox;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.PublicService;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;


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
    @Auditable(parameters={"wpStoreId"})
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
    @Auditable(parameters={"wpStoreId", "userName"})
    public SandboxInfo createAuthorSandbox(String wpStoreId, String userName);
    
    /**
     * List the available sandboxes for the current user and given web project
     * 
     * @param wpStoreId           web project store id
     * @return List<SandboxInfo>  list of sandbox info
     */
    @NotAuditable
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
    @NotAuditable
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
    @NotAuditable
    public boolean isSandboxType(String sbStoreId, QName sandboxType);
    
    /**
     * Get sandbox info
     * 
     * @param sbStoreId     sandbox store id
     * @return SandboxInfo  null if sandbox does not exist or is not visible to the current user
     */
    @NotAuditable
    public SandboxInfo getSandbox(String sbStoreId);
    
    /**
     * Gets author/user sandbox info for the current user
     * <p>
     * Returns null if the author sandbox can not be found
     * 
     * @param wpStoreId      web project store id
     * @return SandboxInfo   author sandbox info
     */
    @NotAuditable
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
    @NotAuditable
    public SandboxInfo getAuthorSandbox(String wpStoreId, String userName);
    
    /**
     * Gets staging sandbox info
     * <p>
     * Returns null if the staging sandbox can not be found
     * 
     * @param wpStoreId      web project store id
     * @return SandboxInfo   staging sandbox info
     */
    @NotAuditable
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
    @Auditable(parameters={"sbStoreId"})
    public void deleteSandbox(String sbStoreId);
    
    /**
     * List all changed assets for given sandbox (eg. for user sandbox compared to staging sandbox)
     * <p>
     * Note: This will list all new/modified/deleted assets from the sandbox root directory (eg. /www/avm_webapps) - ie. across all web apps
     * 
     * @param sbStoreId                 sandbox store id
     * @param  includeDeleted           if true, include deleted assets as well as new/modified assets
     * @return List<AssetInfo>          list of all changed assets
     */
    @NotAuditable
    public List<AssetInfo> listChangedAll(String sbStoreId, boolean includeDeleted);
    
    /**
     * List changed assets for given sandbox and web app (eg. in user sandbox)
     * <p>
     * Note: This will list new/modified/deleted assets for the given web app
     *
     * @param sbStoreId                 sandbox store id
     * @param webApp                    web app to filter by
     * @param  includeDeleted           if true, include deleted assets as well as new/modified assets
     * @return List<AssetInfo>          list of changed assets
     */
    @NotAuditable
    public List<AssetInfo> listChangedWebApp(String sbStoreId, String webApp, boolean includeDeleted);
    
    /**
     * List changed assets for given sandbox path (eg. between user sandbox and staging sandbox)
     * <p>
     * Note: This will list new/modified/deleted assets from the directory and below. The destination path will be dervied.
     *
     * @param sbStoreId                 sandbox store id
     * @param relativePath              relative path to filter by (eg. /www/avm_webapps/ROOT/MyFolderToList)
     * @param  includeDeleted           if true, include deleted assets as well as new/modified assets
     * @return List<AssetInfo>          list of changed assets
     */
    @NotAuditable
    public List<AssetInfo> listChanged(String sbStoreId, String relativePath, boolean includeDeleted);
    
    /**
     * List changed (new/modified/deleted) assets between any two sandbox paths
     * 
     * @param srcSandboxStoreId         source sandbox store id
     * @param srcRelativePath           source relative path to filter by (eg. /www/avm_webapps/ROOT/MyFolderToList)
     * @param dstSandboxStoreId         destination sandbox store id
     * @param dstRelativePath           destination relative path to filter by (eg. /www/avm_webapps/ROOT/MyFolderToList)
     * @param  includeDeleted           if true, include deleted assets as well as new/modified assets
     * @return List<AssetInfo>          list of changed assets
     */
    @NotAuditable
    public List<AssetInfo> listChanged(String srcSandboxStoreId, String srcRelativePath, String dstSandboxStoreId, String dstRelativePath, boolean includeDeleted);
    
    /**
     * Submit all changed assets for given sandbox (eg. from user sandbox to staging sandbox)
     * <p>
     * Note: This will submit all new/modified/deleted assets from the sandbox root directory (eg. /www/avm_webapps) - ie. across all web apps
     * <p>
     * @param sbStoreId          sandbox store id
     * @param submitLabel        label for submitted snapshot
     * @param submitDescription  description for submitted snapshot
     */
    @Auditable(parameters={"sbStoreId", "submitLabel", "submitDescription"})
    public void submitAll(String sbStoreId, String submitLabel, String submitDescription);
    
    /**
     * Submit changed assets for given sandbox and web app (eg. in user sandbox)
     * <p>
     * Note: This will submit new/modified/deleted assets for the given web app
     * 
     * @param sbStoreId          sandbox store id
     * @param webApp             web app to filter by
     * @param submitLabel        label for submitted snapshot
     * @param submitDescription  description for submitted snapshot
     */
    @Auditable(parameters={"sbStoreId", "webApp", "submitLabel", "submitDescription"})
    public void submitWebApp(String sbStoreId, String webApp, String submitLabel, String submitDescription);
    
    /**
     * Submit changed asset(s) for given sandbox path (eg. in user sandbox)
     * <p>
     * Note: This will submit new/modified/deleted asset(s) for given path (either file or directory and below)
     * 
     * @param sbStoreId          sandbox store id
     * @param relativePath       relative path to filter by (eg. /www/avm_webapps or /www/avm_webapps/ROOT/MyFolderToSubmit)
     * @param submitLabel        label for submitted snapshot
     * @param submitDescription  description for submitted snapshot
     */
    @Auditable(parameters={"sbStoreId", "relativePath", "submitLabel", "submitDescription"})
    public void submit(String sbStoreId, String relativePath, String submitLabel, String submitDescription);
    
    /**
     * Submit list of changed assets for given sandbox (eg. in user sandbox)
     * 
     * @param sbStoreId          sandbox store id
     * @param assetPaths         list of assets, as relative paths (eg. /www/avm_webapps/ROOT/MyFolderToSubmit)
     * @param submitLabel        label for submitted snapshot
     * @param submitDescription  description for submitted snapshot
     */
    @Auditable(parameters={"sbStoreId", "relativePath", "submitLabel", "submitDescription"})
    public void submitList(String sbStoreId, List<String> relativePaths, String submitLabel, String submitDescription);
    
    /**
     * Submit list of changed assets for given sandbox (eg. from user sandbox to staging sandbox)
     * 
     * @param sbStoreId          sandbox store id
     * @param assetNodes         list of assets
     * @param submitLabel        label for submitted snapshot
     * @param submitDescription  description for submitted snapshot
     */
    @Auditable(parameters={"sbStoreId", "assets", "submitLabel", "submitDescription"})
    public void submitListAssets(String sbStoreId, List<AssetInfo> assets, String submitLabel, String submitDescription);
    
    /**
     * Submit list of changed assets for given sandbox (eg. from user sandbox to staging sandbox)
     * 
     * NOTE: for backwards compatibility - subject to change - hence deprecated for now
     * 
     * @param sbStoreId          sandbox store id
     * @param assetPaths         list of assets, as relative paths (eg. /www/avm_webapps/ROOT/MyFolderToSubmit)
     * @param workflowName       selected workflow name - if null, will use default submit direct workflow
     * @param workflowParams     configured workflow params
     * @param submitLabel        label for submitted snapshot
     * @param submitDescription  description for submitted snapshot
     * @param expirationDates    optional map of <path, date> for those assets set with an expiration date, or can be null (if no expiration dates)
     * @param launchDate         optional launch date
     * @param autoDeploy         if true then will auto-deploy on workflow approval
     * 
     * @deprecated subject to change
     */
    @NotAuditable
    public void submitListAssets(String sbStoreId, List<String> relativePaths,
                                 String workflowName, Map<QName, Serializable> workflowParams, 
                                 String submitLabel, String submitDescription,
                                 Map<String, Date> expirationDates, Date launchDate, boolean autoDeploy);
    
    /**
     * Revert all changed assets for given sandbox (eg. in user sandbox)
     * <p>
     * Note: This will revert all new/modified/deleted assets from the sandbox store root directory (eg. /www/avm_webapps) - ie. across all web apps
     *
     * @param sbStoreId  sandbox store id
     */
    @Auditable(parameters={"sbStoreId"})
    public void revertAll(String sbStoreId);
    
    /**
     * Revert changed assets for given sandbox and web app (eg. in user sandbox)
     * <p>
     * Note: This will revert new/modified/deleted assets for the given web app
     * 
     * @param sbStoreId  sandbox store id
     * @param webApp     web app to filter by
     */
    @Auditable(parameters={"sbStoreId", "webApp"})
    public void revertWebApp(String sbStoreId, String webApp);
    
    /**
     * Revert changed asset(s) for given sandbox path (eg. in user sandbox)
     * <p>
     * Note: This will revert new/modified/deleted asset(s) for given path (either file or directory and below)
     * 
     * @param sbStoreId     sandbox store id
     * @param relativePath  relative path to filter by (eg. /www/avm_webapps/ROOT/MyFolderToRevert)
     */
    @NotAuditable
    public void revert(String sbStoreId, String relativePath);
    
    /**
     * Revert list of changed assets for given sandbox (eg. in user sandbox)
     * 
     * @param sbStoreId      sandbox store id
     * @param assetPaths     list of assets, as relative paths (eg. /www/avm_webapps/ROOT/MyFolderToRevert)
     */
    @NotAuditable
    public void revertList(String sbStoreId, List<String> relativePaths);
    
    /**
     * Revert list of changed assets for given sandbox (eg. in user sandbox)
     * 
     * @param assets         list of assets
     */
    @NotAuditable
    public void revertListAssets(String sbStoreId, List<AssetInfo> assets);
    
    /**
     * Revert sandbox to a specific snapshot version ID (ie. for staging sandbox)
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param sbStoreId  staging sandbox store id
     * @param version    version
     */
    @NotAuditable
    public void revertSnapshot(String sbStoreId, int version);
    
    /**
     * List all snapshots (sandbox versions) for the given sandbox (ie. for staging sandbox)
     * <p>
     * Current user must be a content manager for the web project
     *  
     * @param sbStoreId                 staging sandbox store id
     * @param includeSystemGenerated    if false will ignore system generated snapshots else true to get all snapshots
     * @return List<SandboxVersion>     list of sandbox versions
     */
    @NotAuditable
    public List<SandboxVersion> listSnapshots(String sbStoreId, boolean includeSystemGenerated);
    
    /**
     * List snapshots (sandbox versions) for the given sandbox between given dates (ie. for staging sandbox)
     * <p>
     * Current user must be a content manager for the web project
     * 
     * @param sbStoreId                 staging sandbox store id
     * @param from                      from date
     * @param to                        to date
     * @param includeSystemGenerated    if false will ignore system generated snapshots else true to get all snapshots
     * @return List<SandboxVersion>     list of sandbox versions
     */
    @NotAuditable
    public List<SandboxVersion> listSnapshots(String sbStoreId, Date from, Date to, boolean includeSystemGenerated);
}
