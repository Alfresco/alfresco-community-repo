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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Asset Service fundamental API.
 * <p>
 * This service API is designed to support the public facing Asset APIs. 
 * 
 * @author janv
 */
public class AssetServiceImpl implements AssetService
{
    private static char PATH_SEPARATOR = '/';
    
    private static final int BUFFER_SIZE = 16384;
    
    private AVMService avmService;
    private AVMLockingService avmLockingService;
    private NodeService avmNodeService; // AVM node service (ML-aware)
    private VirtServerRegistry virtServerRegistry;
    
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    public void setAvmLockingService(AVMLockingService avmLockingService)
    {
        this.avmLockingService = avmLockingService;
    }
    
    public void setNodeService(NodeService avmNodeService)
    {
        this.avmNodeService = avmNodeService;
    }
    
    public void setVirtServerRegistry(VirtServerRegistry virtServerRegistry)
    {
        this.virtServerRegistry = virtServerRegistry;
    }
    
    private void checkMandatoryPath(String path)
    {
        ParameterCheck.mandatoryString("path", path);
        
        if (path.indexOf(AVMUtil.AVM_STORE_SEPARATOR_CHAR) != -1)
        {
            throw new IllegalArgumentException("Unexpected path '"+path+"' - should not contain '"+WCMUtil.AVM_STORE_SEPARATOR+"'");
        }
    }
    
    private boolean isWebProjectStagingSandbox(String sbStoreId)
    {
        PropertyValue propVal = avmService.getStoreProperty(sbStoreId, SandboxConstants.PROP_WEB_PROJECT_NODE_REF);
        return ((propVal != null) && (WCMUtil.isStagingStore(sbStoreId)));
    }
    
    public void createFolderWebApp(String sbStoreId, String webApp, String parentFolderPathRelativeToWebApp, String name)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        checkMandatoryPath(parentFolderPathRelativeToWebApp);
        ParameterCheck.mandatoryString("name", name);
        
        if (! isWebProjectStagingSandbox(sbStoreId))
        {
            parentFolderPathRelativeToWebApp = AVMUtil.addLeadingSlash(parentFolderPathRelativeToWebApp);
            
            String avmParentPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp) + parentFolderPathRelativeToWebApp;
            
            createFolderAVM(avmParentPath, name, null);
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + sbStoreId);
        }
    }
    
    public void createFolder(String sbStoreId, String parentFolderPath, String name, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("parentFolderPath", parentFolderPath);
        ParameterCheck.mandatoryString("name", name);
        
        String avmParentPath = AVMUtil.buildAVMPath(sbStoreId, parentFolderPath);
        
        createFolderAVM(avmParentPath, name, properties);
    }
    
    private void createFolderAVM(String avmParentPath, String name, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatoryString("avmParentPath", avmParentPath);
        ParameterCheck.mandatoryString("name", name);
        
        String sbStoreId = WCMUtil.getSandboxStoreId(avmParentPath);
        if (! isWebProjectStagingSandbox(sbStoreId))
        {
            avmService.createDirectory(avmParentPath, name);
            
            String avmPath = avmParentPath + PATH_SEPARATOR + name;
            
            // for WCM Web Client (Alfresco Explorer)
            avmService.addAspect(avmPath, ApplicationModel.ASPECT_UIFACETS);
            
            if ((properties != null) && (properties.size() > 0))
            {
                setProperties(avmPath, properties);
            }
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + sbStoreId);
        }
    }
    
    public ContentWriter createFileWebApp(String sbStoreId, String webApp, String parentFolderPathRelativeToWebApp, String name)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        ParameterCheck.mandatoryString("parentFolderPathRelativeToWebApp", parentFolderPathRelativeToWebApp);
        ParameterCheck.mandatoryString("name", name);
        
        parentFolderPathRelativeToWebApp = AVMUtil.addLeadingSlash(parentFolderPathRelativeToWebApp);
        
        String avmParentPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp) + parentFolderPathRelativeToWebApp;
        
        createFileAVM(avmParentPath, name);
        
        String avmPath = avmParentPath + PATH_SEPARATOR + name;
        
        return avmService.getContentWriter(avmPath, true);
    }
    
    public ContentWriter createFile(String sbStoreId, String parentFolderPath, String name, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("parentFolderPath", parentFolderPath);
        ParameterCheck.mandatoryString("name", name);
        
        String avmParentPath = AVMUtil.buildAVMPath(sbStoreId, parentFolderPath);
        
        createFileAVM(avmParentPath, name);
        
        String avmPath = avmParentPath + PATH_SEPARATOR + name;
        
        if ((properties != null) && (properties.size() > 0))
        {
            setProperties(avmPath, properties);
        }
        
        return avmService.getContentWriter(avmPath, true);
    }
    
    private void createFileAVM(String avmParentPath, String name)
    {
        ParameterCheck.mandatoryString("avmParentPath", avmParentPath);
        ParameterCheck.mandatoryString("name", name);
        
        String sbStoreId = WCMUtil.getSandboxStoreId(avmParentPath);
        if (! isWebProjectStagingSandbox(sbStoreId))
        {
            try
            {
                avmService.createFile(avmParentPath, name).close();
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("I/O Error.", e);
            }
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + sbStoreId);
        }
    }
    
    private void createFileAVM(String avmParentPath, String name, InputStream in)
    {
        ParameterCheck.mandatoryString("avmParentPath", avmParentPath);
        
        String sbStoreId = WCMUtil.getSandboxStoreId(avmParentPath);
        if (! isWebProjectStagingSandbox(sbStoreId))
        {
            avmService.createFile(avmParentPath, name,  in, null, null);
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + sbStoreId);
        }
    }
    
    public ContentWriter getContentWriter(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        if (! isWebProjectStagingSandbox(asset.getSandboxId()))
        {
            return avmService.getContentWriter(asset.getAvmPath(), true);
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + asset.getSandboxId());
        }
    }
    
    public ContentReader getContentReader(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        return avmService.getContentReader(asset.getSandboxVersion(), asset.getAvmPath());
    }
    
    public AssetInfo getAssetWebApp(String sbStoreId, String webApp, String pathRelativeToWebApp)
    {
        return getAssetWebApp(sbStoreId, webApp, pathRelativeToWebApp, false);
    }
    
    public AssetInfo getAssetWebApp(String sbStoreId, String webApp, String pathRelativeToWebApp, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        ParameterCheck.mandatoryString("pathRelativeToWebApp", pathRelativeToWebApp);
        
        pathRelativeToWebApp = AVMUtil.addLeadingSlash(pathRelativeToWebApp);
        
        String avmPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp) + pathRelativeToWebApp;
        
        return getAssetAVM(-1, avmPath, includeDeleted);
    }
    
    public AssetInfo getAsset(String sbStoreId, String path)
    {
        return getAsset(sbStoreId, -1, path, false);
    }
    
    public AssetInfo getAsset(String sbStoreId, int version, String path, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("path", path);
        
        String avmPath = AVMUtil.buildAVMPath(sbStoreId, path);
        
        return getAssetAVM(version, avmPath, includeDeleted);
    }
    
    private AssetInfo getAssetAVM(int version, String avmPath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("avmPath", avmPath);
        
        AVMNodeDescriptor node = avmService.lookup(version, avmPath, includeDeleted);
        
        AssetInfo asset = null;
        
        if (node != null)
        {
            String lockOwner = null;
            if (avmLockingService != null)
            {
                String wpStoreId = WCMUtil.getWebProjectStoreIdFromPath(avmPath);
                String[] parts = WCMUtil.splitPath(avmPath);
                lockOwner = getLockOwner(wpStoreId, parts[1]);
            }
            
            asset = new AssetInfoImpl(version, node, lockOwner);
        }
        
        return asset;
    }
    
    public String getLockOwner(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        return getLockOwner(WCMUtil.getWebProjectStoreId(asset.getSandboxId()), asset.getPath());
    }
    
    private String getLockOwner(String wpStoreId, String filePath)
    {
        return avmLockingService.getLockOwner(wpStoreId, filePath);
    }
    
    public boolean hasLockAccess(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        return avmLockingService.hasAccess(
                WCMUtil.getWebProjectStoreId(asset.getSandboxId()),
                asset.getAvmPath(),
                AuthenticationUtil.getFullyAuthenticatedUser());
    }
    
    public void updateAssetProperties(AssetInfo asset, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("asset", asset);
        ParameterCheck.mandatory("properties", properties);
        
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(-1, asset.getAvmPath());
        for (Map.Entry<QName, Serializable> prop : properties.entrySet())
        {
            avmNodeService.setProperty(avmNodeRef, prop.getKey(), prop.getValue());
        }
    }
    
    public void setAssetProperties(AssetInfo asset, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("asset", asset);
        ParameterCheck.mandatory("properties", properties);
         
        setProperties(asset.getAvmPath(), properties);
    }
    
    private void setProperties(String avmPath, Map<QName, Serializable> properties)
    {
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(-1, avmPath);
        avmNodeService.setProperties(avmNodeRef, properties);
    }
    
    public void addAspect(AssetInfo asset, QName aspectName, Map<QName, Serializable> properties)
    {
        addAspect(asset.getAvmPath(), aspectName, properties);
    }
    
    private void addAspect(String avmPath, QName aspect, Map<QName, Serializable> properties)
    {
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(-1, avmPath);
        avmNodeService.addAspect(avmNodeRef, aspect, properties);
    }
    
    public void removeAspect(AssetInfo asset, QName aspectName)
    {
        ParameterCheck.mandatory("asset", asset);
        
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(-1, asset.getAvmPath());
        avmNodeService.removeAspect(avmNodeRef, aspectName);
    }
    
    public Set<QName> getAspects(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(asset.getSandboxVersion(), asset.getAvmPath());
        return avmNodeService.getAspects(avmNodeRef);
    }
    
    public boolean hasAspect(AssetInfo asset, QName aspectName)
    {
        ParameterCheck.mandatory("asset", asset);
        
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(asset.getSandboxVersion(), asset.getAvmPath());
        return avmNodeService.hasAspect(avmNodeRef, aspectName);
    }
    
    public Map<QName, Serializable> getAssetProperties(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        return getProperties(asset.getSandboxVersion(), asset.getAvmPath());
    }
    
    private Map<QName, Serializable> getProperties(int version, String avmPath)
    {
        NodeRef avmNodeRef = AVMNodeConverter.ToNodeRef(version, avmPath);
        return avmNodeService.getProperties(avmNodeRef); // note: includes built-in properties
    }
    
    public List<AssetInfo> listAssetsWebApp(String sbStoreId, String webApp, String parentFolderPathRelativeToWebApp, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        ParameterCheck.mandatoryString("parentFolderPathRelativeToWebApp", parentFolderPathRelativeToWebApp);
        
        parentFolderPathRelativeToWebApp = AVMUtil.addLeadingSlash(parentFolderPathRelativeToWebApp);
        
        String avmPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp) + parentFolderPathRelativeToWebApp;
        
        return listAssetsAVM(-1, avmPath, includeDeleted);
    }
    
    public List<AssetInfo> listAssets(String sbStoreId, String parentFolderPath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("parentFolderPath", parentFolderPath);
        
        String avmPath = AVMUtil.buildAVMPath(sbStoreId, parentFolderPath);
        
        return listAssetsAVM(-1, avmPath, includeDeleted);
    }
    
    public List<AssetInfo> listAssets(String sbStoreId, int version, String parentFolderPath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("parentFolderPath", parentFolderPath);
        
        String avmPath = AVMUtil.buildAVMPath(sbStoreId, parentFolderPath);
        
        return listAssetsAVM(version, avmPath, includeDeleted);
    }
    
    private List<AssetInfo> listAssetsAVM(int version, String avmPath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("avmPath", avmPath);
        
        Map<String, AVMNodeDescriptor> nodes = avmService.getDirectoryListing(version, avmPath, includeDeleted);
        
        List<AssetInfo> assets = new ArrayList<AssetInfo>(nodes.size());

        for (AVMNodeDescriptor node : nodes.values())
        { 
            String lockOwner = null;
            if (avmLockingService != null)
            {
                String wpStoreId = WCMUtil.getWebProjectStoreIdFromPath(avmPath);
                String[] parts = WCMUtil.splitPath(avmPath);
                lockOwner = getLockOwner(wpStoreId, parts[1]);
            }
            
            assets.add(new AssetInfoImpl(version, node, lockOwner));
        }
        
        return assets;
    }
    
    public void deleteAsset(AssetInfo asset)
    {
        ParameterCheck.mandatory("asset", asset);
        
        if (! isWebProjectStagingSandbox(asset.getSandboxId()))
        {
            avmService.removeNode(asset.getAvmPath());
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + asset.getSandboxId());
        }
    }
    
    public AssetInfo renameAsset(AssetInfo asset, String newName)
    {
        ParameterCheck.mandatory("asset", asset);
        
        if (! isWebProjectStagingSandbox(asset.getSandboxId()))
        {
            String avmParentPath = AVMUtil.splitBase(asset.getAvmPath())[0];
            String oldName = asset.getName();
            
            avmService.rename(avmParentPath, oldName, avmParentPath, newName);
            
            return getAsset(asset.getSandboxId(), WCMUtil.getStoreRelativePath(avmParentPath)+"/"+newName);
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + asset.getSandboxId());
        }
    }
    
    public AssetInfo moveAsset(AssetInfo asset, String parentFolderPath)
    {
        ParameterCheck.mandatory("asset", asset);
        
        if (! isWebProjectStagingSandbox(asset.getSandboxId()))
        {
            String avmDstPath = AVMUtil.buildAVMPath(asset.getSandboxId(), parentFolderPath);
            
            String avmSrcPath = AVMUtil.splitBase(asset.getAvmPath())[0];
            String name = asset.getName();
            
            avmService.rename(avmSrcPath, name, avmDstPath, name);
            
            return getAsset(asset.getSandboxId(), WCMUtil.getStoreRelativePath(avmDstPath)+"/"+name);
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + asset.getSandboxId());
        }
    }
    
    public AssetInfo copyAsset(AssetInfo asset, String parentFolderPath)
    {
        ParameterCheck.mandatory("asset", asset);
        
        if (! isWebProjectStagingSandbox(asset.getSandboxId()))
        {
            String avmDstParentPath = AVMUtil.buildAVMPath(asset.getSandboxId(), parentFolderPath);
            
            String avmSrcPath = asset.getAvmPath();
            String name = asset.getName();
            
            avmService.copy(-1, avmSrcPath, avmDstParentPath, name);
            
            return getAsset(asset.getSandboxId(), WCMUtil.getStoreRelativePath(avmDstParentPath+"/"+name));
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + asset.getSandboxId());
        }
    }
    
    // TODO should this be in sandbox service ?
    public void bulkImport(String sbStoreId, String parentFolderPath, File zipFile, boolean isHighByteZip)
    {
        if (! isWebProjectStagingSandbox(sbStoreId))
        {
            String avmDstPath = AVMUtil.buildAVMPath(sbStoreId, parentFolderPath);
            
            // convert the AVM path to a NodeRef so we can use the NodeService to perform import
            NodeRef importRef = AVMNodeConverter.ToNodeRef(-1, avmDstPath);
            processZipImport(zipFile, isHighByteZip, importRef);
            
            // After a bulk import, snapshot the store
            avmService.createSnapshot(sbStoreId, "Import of file: " + zipFile.getName(), null);
            
            // Bind the post-commit transaction listener with data required for virtualization server notification
            UpdateSandboxTransactionListener tl = new UpdateSandboxTransactionListener(avmDstPath);
            AlfrescoTransactionSupport.bindListener(tl);
        }
        else
        {
            throw new AccessDeniedException("Not allowed to write in: " + sbStoreId);
        }
        
    }
    
    /**
     * Process ZIP file for import into an AVM repository store location
     *
     * @param file       ZIP format file
     * @param rootRef    Root reference of the AVM location to import into
     */
    private void processZipImport(File file, boolean isHighByteZip, NodeRef rootRef)
    {
       try
       {
          // NOTE: This encoding allows us to workaround bug:
          //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
          // We also try to use the extra encoding information if present
          ZipFile zipFile = new ZipFile(file, isHighByteZip ? "Cp437" : null, true);
          File alfTempDir = TempFileProvider.getTempDir();
          // build a temp dir name based on the name of the file we are importing
          File tempDir = new File(alfTempDir.getPath() + File.separatorChar + file.getName() + "_unpack");
          try
          {
             ImporterActionExecuter.extractFile(zipFile, tempDir.getPath());
             importDirectory(tempDir.getPath(), rootRef);
          }
          finally
          {
             if (tempDir.exists())
             {
                ImporterActionExecuter.deleteDir(tempDir);
             }
          }
       }
       catch (IOException e)
       {
          throw new AlfrescoRuntimeException("Unable to process Zip file. File may not be of the expected format.", e);
       }
    }
    
    /**
     * Recursively import a directory structure into the specified root node
     *
     * @param dir     The directory of files and folders to import
     * @param root    The root node to import into
     */
    private void importDirectory(String dir, NodeRef root)
    {
       File topdir = new File(dir);
       if (!topdir.exists()) return;
       for (File file : topdir.listFiles())
       {
          try
          {
             if (file.isFile())
             {
                // Create a file in the AVM store
                String avmPath = AVMNodeConverter.ToAVMVersionPath(root).getSecond();
                String fileName = file.getName();

                Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
                titledProps.put(ContentModel.PROP_TITLE, fileName);
                
                createFileAVM(avmPath, fileName, new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE));
                
                addAspect(avmPath, ContentModel.ASPECT_TITLED, titledProps);
             }
             else
             {
                // Create a directory in the AVM store
                String avmPath = AVMNodeConverter.ToAVMVersionPath(root).getSecond();

                createFolderAVM(avmPath, file.getName(), null);

                String folderPath = avmPath + '/' + file.getName();
                NodeRef folderRef = AVMNodeConverter.ToNodeRef(-1, folderPath);
                importDirectory(file.getPath(), folderRef);
             }
          }
          catch (FileNotFoundException e)
          {
             // TODO: add failed file info to status message?
             throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
          }
          catch (FileExistsException e)
          {
             // TODO: add failed file info to status message?
             throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
          }
       }
    }
    
    /**
     * Update Sandbox Transaction listener - invoked after bulk import
     */
    private class UpdateSandboxTransactionListener extends TransactionListenerAdapter
    {
        private String virtUpdatePath;
        
        public UpdateSandboxTransactionListener(String virtUpdatePath)
        {
            this.virtUpdatePath = virtUpdatePath;
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
         */
        @Override
        public void afterCommit()
        {
            // Reload virtualisation server as required
            if (this.virtUpdatePath != null)
            {
               WCMUtil.updateVServerWebapp(virtServerRegistry, this.virtUpdatePath, true);
            }
        }
    }
}
