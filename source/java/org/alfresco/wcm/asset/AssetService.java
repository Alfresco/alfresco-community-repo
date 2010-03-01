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
package org.alfresco.wcm.asset;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;



/**
 * Asset Service fundamental API.
 * <p>
 * This service API is designed to support the public facing Asset APIs. 
 * 
 * @author janv
 */
public interface AssetService
{
    /**
     * Create folder within given sandbox and webApp
     * 
     * @param sbStoreId
     * @param webApp
     * @param parentFolderPathRelativeToWebApp
     * @param name
     */
    public void createFolderWebApp(String sbStoreId, String webApp, String parentFolderPathRelativeToWebApp, String name);
    
    /**
     * Create folder within given sandbox
     * 
     * @param sbStoreId
     * @param parentFolderPath
     * @param name
     * @param properties
     */
    public void createFolder(String sbStoreId, String parentFolderPath, String name, Map<QName, Serializable> properties);
    
    /**
     * Create (empty) file within given sandbox and webApp, return content writer for file contents
     * 
     * @param sbStoreId
     * @param webApp
     * @param parentFolderPath
     * @param name
     * @return
     */
    public ContentWriter createFileWebApp(String sbStoreId, String webApp, String parentFolderPath, String name);
    
    /**
     * Create (empty) file within given sandbox, return content writer for file contents
     * 
     * @param sbStoreId
     * @param parentFolderPath
     * @param name
     * @param properties
     * @return
     */
    public ContentWriter createFile(String sbStoreId, String parentFolderPath, String name, Map<QName, Serializable> properties);
    
    /**
     * Get asset (file or folder) for given sandbox, webApp and path (within webApp)
     * <p>
     * Returns null if the asset can not be found
     * 
     * @param sbStoreId
     * @param webApp
     * @param pathRelativeToWebApp
     * @return
     */
    public AssetInfo getAssetWebApp(String sbStoreId, String webApp, String pathRelativeToWebApp);
    
    /**
     * Get asset (file or folder) for given sandbox, webApp and path (within webApp), optionally include deleted assets
     * <p>
     * Returns null if the asset can not be found
     * 
     * @param sbStoreId
     * @param webApp
     * @param pathRelativeToWebApp
     * @param includeDeleted
     * @return AssetInfo  asset info
     */
    public AssetInfo getAssetWebApp(String sbStoreId, String webApp, String pathRelativeToWebApp, boolean includeDeleted);
    
    /**
     * Get asset (file or folder) for given sandbox and path
     * <p>
     * Returns null if the asset can not be found
     * 
     * @param sbStoreId   sandbox store id
     * @param path        asset path (eg. /www/avm_webapps/ROOT/myFile)
     * @return AssetInfo  asset info
     */
    public AssetInfo getAsset(String sbStoreId, String path);
    
    /**
     * Get asset (file or folder) for given sandbox version and path, optionally include deleted assets
     * <p>
     * Returns null if the asset can not be found
     * 
     * @param sbStoreId
     * @param version
     * @param path
     * @param includeDeleted
     * @return AssetInfo  asset info
     */
    public AssetInfo getAsset(String sbStoreId, int version, String path, boolean includeDeleted);
    
    /**
     * Get content writer for given file asset, to allow file contents to be written or updated
     * 
     * @param asset
     * @return
     */
    public ContentWriter getContentWriter(AssetInfo fileAsset);
    
    /**
     * Get content reader for given file asset, to allow file contents to be read
     * 
     * @param asset
     * @return
     */
    public ContentReader getContentReader(AssetInfo fileAsset);
    
    /**
     * Get asset properties
     * 
     * @param asset
     * @return
     */
    public Map<QName, Serializable> getAssetProperties(AssetInfo asset);
    
    /**
     * Set asset properties (will replace all existing properties)
     * 
     * @param asset
     * @param properties
     */
    public void setAssetProperties(AssetInfo asset, Map<QName, Serializable> properties);
    
    /**
     * Update asset properties (will replace given set of properties, if they already exist)
     * 
     * @param asset
     * @param properties
     */
    public void updateAssetProperties(AssetInfo asset, Map<QName, Serializable> properties);
    
    /**
     * Apply aspect to asset, with given properties (can be null)
     * 
     * @param asset
     * @param aspectName
     * @param properties
     */
    public void addAspect(AssetInfo asset, QName aspectName, Map<QName, Serializable> properties);
    
    /**
     * Remove aspect from asset, and any related properties
     * 
     * @param asset
     * @param aspectName
     */
    public void removeAspect(AssetInfo asset, QName aspectName);
    
    /**
     * Get set of aspects applied to asset
     * 
     * @param asset
     * @return list of aspects
     */
    public Set<QName> getAspects(AssetInfo asset);
    
    /**
     * True, if asset has given aspect applied
     * 
     * @param asset
     * @param aspectName
     * @return
     */
    public boolean hasAspect(AssetInfo asset, QName aspectName);
    
    /**
     * List assets within given sandbox and webApp and path (within webApp), optionally include deleted
     * 
     * @param sbStoreId
     * @param webApp
     * @param parentFolderPathRelativeToWebApp
     * @param includeDeleted
     * @return list of assets
     */
    public List<AssetInfo> listAssetsWebApp(String sbStoreId, String webApp, String parentFolderPathRelativeToWebApp, boolean includeDeleted);
    
    /**
     * List assets within given sandbox and path, optionally include deleted
     * 
     * @param sbStoreId
     * @param parentFolderPath
     * @param includeDeleted
     * @return list of assets
     */
    public List<AssetInfo> listAssets(String sbStoreId, String parentFolderPath, boolean includeDeleted);
    
    /**
     * List assets within given sandbox version and path, optionally include deleted
     * 
     * @param sbStoreId
     * @param version
     * @param parentFolderPath
     * @param includeDeleted
     * @return list of assets
     */
    public List<AssetInfo> listAssets(String sbStoreId, int version, String parentFolderPath, boolean includeDeleted);
    
    /**
     * Delete asset
     * 
     * @param asset
     */
    public void deleteAsset(AssetInfo asset);
    
    /**
     * Rename asset
     * 
     * @param asset
     * @param newName
     * @return AssetInfo  asset info
     */
    public AssetInfo renameAsset(AssetInfo asset, String newName);
    
    /**
     * Copy asset(s) within sandbox
     * <p> 
     * Note: folder asset will be recursively copied
     * Note: file asset(s) must have content
     * 
     * @param asset
     * @param parentFolderPath
     * @return AssetInfo  asset info
     */
    public AssetInfo copyAsset(AssetInfo asset, String parentFolderPath);
    
    // TODO - copy asset to different sandbox ?
    
    /**
     * Move asset within sandbox
     * 
     * @param asset
     * @param parentFolderPath
     * @return AssetInfo  asset info
     */
    public AssetInfo moveAsset(AssetInfo asset, String parentFolderPath);
    
    /**
     * Bulk import assets into sandbox
     * 
     * @param sbStoreId
     * @param parentFolderPath
     * @param zipFile
     */
    public void bulkImport(String sbStoreId, String parentFolderPath, File zipFile, boolean isHighByteZip);
    
    /**
     * Runtime check to get lock (and owner) for asset - null if not locked
     * 
     * @param asset
     * @return String lock owner (null if path not locked)
     */
    public String getLockOwner(AssetInfo fileAsset);
    
    /**
     * Runtime check to check if the current user can perform (write) operations on the asset when locked
     * 
     * @param asset
     * @return boolean  true if current user has write access
     */
    public boolean hasLockAccess(AssetInfo fileAsset);
}
