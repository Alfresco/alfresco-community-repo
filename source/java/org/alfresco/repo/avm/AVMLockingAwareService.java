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
package org.alfresco.repo.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingException;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService.LockState;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.wcm.util.WCMUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * An AVMLockingService aware implementation of AVMService.
 * @author britt
 */
public class AVMLockingAwareService implements AVMService, ApplicationContextAware
{
    public static final String STORE_SEPARATOR = "--";
    
    public static final String STORE_WORKFLOW = "workflow";
    
    private AVMService fService;

    private AVMLockingService fLockingService;

    private PermissionService permissionService;

    private ApplicationContext fContext;

    public AVMLockingAwareService()
    {
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        fContext = applicationContext;
    }

    public void init()
    {
        fService = (AVMService)fContext.getBean("avmService");
        fLockingService = (AVMLockingService)fContext.getBean("avmLockingService");
        permissionService = (PermissionService) fContext.getBean("PermissionService");
    }

    public void addAspect(String path, QName aspectName)
    {
        grabLock(path);
        fService.addAspect(path, aspectName);
    }

    public void copy(int srcVersion, String srcPath, String dstPath, String name)
    {
        fService.copy(srcVersion, srcPath, dstPath, name);
    }

    public void createBranch(int version, String srcPath, String dstPath,
            String name)
    {
        fService.createBranch(version, srcPath, dstPath, name);
    }

    public void createDirectory(String path, String name)
    {
        fService.createDirectory(path, name);
    }

    public OutputStream createFile(String path, String name)
    {
        grabLock(AVMUtil.extendAVMPath(path, name));
        return fService.createFile(path, name);
    }

    public void createFile(String path, String name, InputStream in)
    {
        grabLock(AVMUtil.extendAVMPath(path, name));
        fService.createFile(path, name, in);
    }

    public void createLayeredDirectory(String targetPath, String parent,
            String name)
    {
        fService.createLayeredDirectory(targetPath, parent, name);
    }

    public void createLayeredFile(String targetPath, String parent, String name)
    {
        grabLock(AVMUtil.extendAVMPath(parent, name));
        fService.createLayeredFile(targetPath, parent, name);
    }

    public Map<String, Integer> createSnapshot(String store, String tag, String description)
    {
        return fService.createSnapshot(store, tag, description);
    }

    public void createStore(String name)
    {
        fService.createStore(name);
    }
    
    public void createStore(String name, Map<QName, PropertyValue> props)
    {
        fService.createStore(name, props);
    }
    
    public void deleteNodeProperties(String path)
    {
        grabLock(path);
        fService.deleteNodeProperties(path);
    }

    public void deleteNodeProperty(String path, QName name)
    {
        grabLock(path);
        fService.deleteNodeProperty(path, name);
    }

    public void deleteStoreProperty(String store, QName name)
    {
        fService.deleteStoreProperty(store, name);
    }

    public AVMNodeDescriptor forceCopy(String path)
    {
        grabLock(path);
        return fService.forceCopy(path);
    }

    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc)
    {
        return fService.getAPath(desc);
    }

    public List<String> getPathsInStoreVersion(AVMNodeDescriptor desc, String store, int version)
    {
        return fService.getPathsInStoreVersion(desc, store, version);
    }

    public Set<QName> getAspects(int version, String path)
    {
        return fService.getAspects(version, path);
    }

    public Set<QName> getAspects(AVMNodeDescriptor desc)
    {
        return fService.getAspects(desc);
    }

    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
            AVMNodeDescriptor right)
    {
        return fService.getCommonAncestor(left, right);
    }

    public ContentData getContentDataForRead(int version, String path)
    {
        return fService.getContentDataForRead(version, path);
    }

    public ContentData getContentDataForRead(AVMNodeDescriptor desc)
    {
        return fService.getContentDataForRead(desc);
    }

    public ContentData getContentDataForWrite(String path)
    {
        grabLock(path);
        return fService.getContentDataForWrite(path);
    }

    public ContentReader getContentReader(int version, String path)
    {
        return fService.getContentReader(version, path);
    }

    public ContentWriter getContentWriter(String path, boolean update)
    {
        grabLock(path);
        return fService.getContentWriter(path, update);
    }

    public List<String> getDeleted(int version, String path)
    {
        return fService.getDeleted(version, path);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path)
    {
        return fService.getDirectoryListing(version, path);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path, boolean includeDeleted)
    {
        return fService.getDirectoryListing(version, path, includeDeleted);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir)
    {
        return fService.getDirectoryListing(dir);
    }
    
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, String childNamePattern)
    {
        return fService.getDirectoryListing(dir, childNamePattern);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return fService.getDirectoryListing(dir, includeDeleted);
    }

    public AVMNodeDescriptor[] getDirectoryListingArray(int version, String path, boolean includeDeleted)
    {
        return fService.getDirectoryListingArray(version, path, includeDeleted);
    }

    public AVMNodeDescriptor[] getDirectoryListingArray(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return fService.getDirectoryListingArray(dir, includeDeleted);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(int version, String path)
    {
        return fService.getDirectoryListingDirect(version, path);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(int version, String path, boolean includeDeleted)
    {
        return fService.getDirectoryListingDirect(version, path, includeDeleted);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return fService.getDirectoryListingDirect(dir, includeDeleted);
    }

    public InputStream getFileInputStream(int version, String path)
    {
        return fService.getFileInputStream(version, path);
    }

    public InputStream getFileInputStream(AVMNodeDescriptor desc)
    {
        return fService.getFileInputStream(desc);
    }

    public OutputStream getFileOutputStream(String path)
    {
        grabLock(path);
        return fService.getFileOutputStream(path);
    }

    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc)
    {
        return fService.getHeadPaths(desc);
    }

    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count)
    {
        return fService.getHistory(desc, count);
    }

    public String getIndirectionPath(int version, String path)
    {
        return fService.getIndirectionPath(version, path);
    }

    public int getLatestSnapshotID(String storeName)
    {
        return fService.getLatestSnapshotID(storeName);
    }

    public LayeringDescriptor getLayeringInfo(int version, String path)
    {
        return fService.getLayeringInfo(version, path);
    }

    public int getNextVersionID(String storeName)
    {
        return fService.getNextVersionID(storeName);
    }

    public Map<QName, PropertyValue> getNodeProperties(int version, String path)
    {
        return fService.getNodeProperties(version, path);
    }

    public Map<QName, PropertyValue> getNodeProperties(AVMNodeDescriptor desc)
    {
        return fService.getNodeProperties(desc);
    }

    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        return fService.getNodeProperty(version, path, name);
    }

    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc)
    {
        return fService.getPaths(desc);
    }

    public List<Pair<Integer, String>> getPathsInStoreHead(
            AVMNodeDescriptor desc, String store)
    {
        return fService.getPathsInStoreHead(desc, store);
    }

    public AVMStoreDescriptor getStore(String name)
    {
        return fService.getStore(name);
    }

    public Map<QName, PropertyValue> getStoreProperties(String store)
    {
        return fService.getStoreProperties(store);
    }

    public PropertyValue getStoreProperty(String store, QName name)
    {
        return fService.getStoreProperty(store, name);
    }

    public AVMNodeDescriptor getStoreRoot(int version, String name)
    {
        return fService.getStoreRoot(version, name);
    }

    public List<VersionDescriptor> getStoreVersions(String name)
    {
        return fService.getStoreVersions(name);
    }

    public List<VersionDescriptor> getStoreVersions(String name, Date from, Date to)
    {
        return fService.getStoreVersions(name, from, to);
    }

    public List<AVMStoreDescriptor> getStores()
    {
        return fService.getStores();
    }

    public AVMStoreDescriptor getSystemStore()
    {
        return fService.getSystemStore();
    }

    public boolean hasAspect(int version, String path, QName aspectName)
    {
        return fService.hasAspect(version, path, aspectName);
    }

    public void link(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        // TODO Does this need a lock?  I don't think so, but revisit.
        fService.link(parentPath, name, toLink);
    }
    
    public void updateLink(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        // TODO Does this need a lock?  I don't think so, but revisit.
        fService.updateLink(parentPath, name, toLink);
    }

    public AVMNodeDescriptor lookup(int version, String path)
    {
        return fService.lookup(version, path);
    }

    public AVMNodeDescriptor lookup(int version, String path,
            boolean includeDeleted)
    {
        return fService.lookup(version, path, includeDeleted);
    }

    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name)
    {
        return fService.lookup(dir, name);
    }

    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name,
            boolean includeDeleted)
    {
        return fService.lookup(dir, name, includeDeleted);
    }

    public void makePrimary(String path)
    {
        fService.makePrimary(path);
    }

    public void makeTransparent(String dirPath, String name)
    {
        fService.makeTransparent(dirPath, name);
    }

    public void purgeStore(String name)
    {
        fService.purgeStore(name);
        
        String webProject = WCMUtil.getWebProject(fService, name);
        if (webProject != null)
        {
            fLockingService.removeLocks(name);
        }
    }

    public void purgeVersion(int version, String name)
    {
        fService.purgeVersion(version, name);
    }

    public Map<QName, PropertyValue> queryStorePropertyKey(String store, QName keyPattern)
    {
        return fService.queryStorePropertyKey(store, keyPattern);
    }

    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKeys(QName keyPattern)
    {
        return fService.queryStoresPropertyKeys(keyPattern);
    }

    public void removeAspect(String path, QName aspectName)
    {
        grabLock(path);
        fService.removeAspect(path, aspectName);
    }

    public void removeNode(String parent, String name)
    {
        String path = AVMUtil.extendAVMPath(parent, name);
        grabLock(path);
        fService.removeNode(parent, name);
        String[] storePath = AVMUtil.splitPath(parent);
        String avmStore = storePath[0];
        fService.createSnapshot(avmStore, null, "Removed "+path);
        String webProject = WCMUtil.getWebProject(fService, avmStore);
        if (webProject != null)
        {
            Map<String, String> lockDataToMatch = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, avmStore);
            String relPath = AVMUtil.extendAVMPath(storePath[1], name); // store-relative path, eg. /www/avm_webapps/ROOT/my.txt
            fLockingService.removeLocks(webProject, relPath, lockDataToMatch);
        }
    }

    public void removeNode(String path)
    {
        grabLock(path);
        fService.removeNode(path);
        String[] storePath = AVMUtil.splitPath(path);
        String avmStore = storePath[0];
        String relPath = storePath[1]; // store-relative path, eg. /www/avm_webapps/ROOT/my.txt
        fService.createSnapshot(avmStore, null, "Removed "+path);
        String webProject = WCMUtil.getWebProject(fService, avmStore);
        if (webProject != null)
        {
            Map<String, String> lockDataToMatch = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, avmStore);
            fLockingService.removeLocks(webProject, relPath, lockDataToMatch);
        }
    }

    public void rename(String srcParent, String srcName, String dstParent, String dstName)
    {
        String srcPath = AVMUtil.extendAVMPath(srcParent, srcName);
        String dstPath = AVMUtil.extendAVMPath(dstParent, dstName);
        
        AVMNodeDescriptor desc = fService.lookup(-1, srcPath, false);
        if (! (desc != null && desc.isDirectory()))
        {
            grabLock(srcPath);
        }
        
        fService.rename(srcParent, srcName, dstParent, dstName);
        
        if (! (desc != null && desc.isDirectory()))
        {
            String[] srcStorePath = AVMUtil.splitPath(srcPath);
            String[] dstStorePath = AVMUtil.splitPath(dstPath);
            
            String srcWebProject = WCMUtil.getWebProject(fService, srcStorePath[0]);
            String dstWebProject = WCMUtil.getWebProject(fService, dstStorePath[0]);
            
            if ((dstWebProject != null) && (dstWebProject.equals(srcWebProject)))
            {
                // Make sure we hold the lock already
                grabLock(dstPath);
            }
            else
            {
                // Remove the old lock and take the new
                fLockingService.removeLock(srcWebProject, srcStorePath[1]);
                grabLock(dstPath);
            }
        }
    }

    public void renameStore(String sourceName, String destName)
    {
        fService.renameStore(sourceName, destName);
    }

    public void retargetLayeredDirectory(String path, String target)
    {
        // TODO This assumes that directories are not locked.
        fService.retargetLayeredDirectory(path, target);
    }

    public void revert(String path, AVMNodeDescriptor toRevertTo)
    {
        grabLock(path);
        fService.revert(path, toRevertTo);
    }

    public void setContentData(String path, ContentData data)
    {
        grabLock(path);
        fService.setContentData(path, data);
    }

    public void setEncoding(String path, String encoding)
    {
        grabLock(path);
        fService.setEncoding(path, encoding);
    }

    public void setGuid(String path, String guid)
    {
        grabLock(path);
        fService.setGuid(path, guid);
    }

    public void setMetaDataFrom(String path, AVMNodeDescriptor from)
    {
        grabLock(path);
        fService.setMetaDataFrom(path, from);
    }

    public void setMimeType(String path, String mimeType)
    {
        grabLock(path);
        fService.setMimeType(path, mimeType);
    }

    public void setNodeProperties(String path,
            Map<QName, PropertyValue> properties)
    {
        grabLock(path);
        fService.setNodeProperties(path, properties);
    }

    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        grabLock(path);
        fService.setNodeProperty(path, name, value);
    }

    public void setOpacity(String path, boolean opacity)
    {
        // TODO Assumes no directory locking.
        fService.setOpacity(path, opacity);
    }

    public void setStoreProperties(String store, Map<QName, PropertyValue> props)
    {
        fService.setStoreProperties(store, props);
    }

    public void setStoreProperty(String store, QName name, PropertyValue value)
    {
        fService.setStoreProperty(store, name, value);
    }

    public void uncover(String dirPath, String name)
    {
        // TODO What about when this is a directory?
        grabLock(AVMUtil.extendAVMPath(dirPath, name));
        fService.uncover(dirPath, name);
    }

    public void createDirectory(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        fService.createDirectory(path, name, aspects, properties);
    }

    public void createFile(String path, String name, InputStream in, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        grabLock(AVMUtil.extendAVMPath(path, name));
        fService.createFile(path, name, in, aspects, properties);
    }
    
    private void grabLock(String path)
    {
        AVMNodeDescriptor desc = fService.lookup(-1, path, false);
        if (desc != null && desc.isDirectory())
        {
            return;
        }
        String[] storePath = AVMUtil.splitPath(path);
        String avmStore = storePath[0];
        String webProject = WCMUtil.getWebProject(fService, storePath[0]);
        if (webProject != null && webProject.equals(avmStore))
        {
            // Don't do locking in staging.
            return;
        }
        if (avmStore.indexOf(STORE_SEPARATOR + STORE_WORKFLOW) != -1)
        {
            //Allow lock in workflow store if user has "Write" permission
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
            if (permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
            {
                String errorMessage = I18NUtil.getMessage("avmlockservice.accessdenied", AuthenticationUtil.getFullyAuthenticatedUser());
                throw new AccessDeniedException(errorMessage);
            }
        }
        else if (webProject != null)
        {
            String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            LockState lockState = fLockingService.getLockState(webProject, storePath[1], userName);
            switch (lockState)
            {
            case LOCK_NOT_OWNER:
                String lockOwner = fLockingService.getLockOwner(webProject, storePath[1]);
                throw new AVMLockingException("avmlockservice.locked", path, lockOwner);
            case NO_LOCK:
                Map<String, String> lockAttributes = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, avmStore);
                fLockingService.lock(webProject, storePath[1], userName, lockAttributes);
                break;
            case LOCK_OWNER:
                // Nothing to do
                break;
            }
        }
    }

    public List<VersionDescriptor> getStoreVersionsTo(String name, int version)
    {
        return fService.getStoreVersionsTo(name, version);
    }

    public List<VersionDescriptor> getStoreVersionsFrom(String name, int version)
    {
        return fService.getStoreVersionsFrom(name, version);
    }
    
    public List<VersionDescriptor> getStoreVersionsBetween(String name, int from, int to)
    {
        return fService.getStoreVersionsBetween(name, from, to);
    }
    
}
