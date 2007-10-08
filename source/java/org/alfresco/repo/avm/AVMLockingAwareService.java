/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingException;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * An AVMLockingService aware implemantation of AVMService.
 * @author britt
 */
public class AVMLockingAwareService implements AVMService, ApplicationContextAware
{
    private AVMService fService;

    private AVMLockingService fLockingService;

    private AuthenticationService fAuthenticationService;

    private ApplicationContext fContext;

    public AVMLockingAwareService()
    {
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        fContext = applicationContext;
    }

    public void init()
    {
        fService = (AVMService)fContext.getBean("avmService");
        fLockingService = (AVMLockingService)fContext.getBean("avmLockingService");
        fAuthenticationService = (AuthenticationService)fContext.getBean("authenticationService");
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#addAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void addAspect(String path, QName aspectName)
    {
        grabLock(path);
        fService.addAspect(path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#copy(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void copy(int srcVersion, String srcPath, String dstPath, String name)
    {
        fService.copy(srcVersion, srcPath, dstPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(int version, String srcPath, String dstPath,
            String name)
    {
        fService.createBranch(version, srcPath, dstPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createDirectory(java.lang.String, java.lang.String)
     */
    public void createDirectory(String path, String name)
    {
        fService.createDirectory(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String)
     */
    public OutputStream createFile(String path, String name)
    {
        grabLock(path + '/' + name);
        return fService.createFile(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String, java.io.InputStream)
     */
    public void createFile(String path, String name, InputStream in)
    {
        grabLock(path + '/' + name);
        fService.createFile(path, name, in);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createLayeredDirectory(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(String targetPath, String parent,
            String name)
    {
        fService.createLayeredDirectory(targetPath, parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(String targetPath, String parent, String name)
    {
        grabLock(parent + '/' + name);
        fService.createLayeredFile(targetPath, parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createSnapshot(java.lang.String, java.lang.String, java.lang.String)
     */
    public Map<String, Integer> createSnapshot(String store, String tag, String description)
    {
        return fService.createSnapshot(store, tag, description);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createStore(java.lang.String)
     */
    public void createStore(String name)
    {
        fService.createStore(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#deleteNodeProperties(java.lang.String)
     */
    public void deleteNodeProperties(String path)
    {
        grabLock(path);
        fService.deleteNodeProperties(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#deleteNodeProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteNodeProperty(String path, QName name)
    {
        grabLock(path);
        fService.deleteNodeProperty(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#deleteStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteStoreProperty(String store, QName name)
    {
        fService.deleteStoreProperty(store, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#forceCopy(java.lang.String)
     */
    public AVMNodeDescriptor forceCopy(String path)
    {
        grabLock(path);
        return fService.forceCopy(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAPath(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc)
    {
        return fService.getAPath(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPathsInStoreVersion(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String, int)
     */
    public List<String> getPathsInStoreVersion(AVMNodeDescriptor desc, String store, int version)
    {
        return fService.getPathsInStoreVersion(desc, store, version);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAspects(int, java.lang.String)
     */
    public Set<QName> getAspects(int version, String path)
    {
        return fService.getAspects(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAspects(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Set<QName> getAspects(AVMNodeDescriptor desc)
    {
        return fService.getAspects(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getCommonAncestor(org.alfresco.service.cmr.avm.AVMNodeDescriptor, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
            AVMNodeDescriptor right)
    {
        return fService.getCommonAncestor(left, right);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForRead(int, java.lang.String)
     */
    public ContentData getContentDataForRead(int version, String path)
    {
        return fService.getContentDataForRead(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForRead(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public ContentData getContentDataForRead(AVMNodeDescriptor desc)
    {
        return fService.getContentDataForRead(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForWrite(java.lang.String)
     */
    public ContentData getContentDataForWrite(String path)
    {
        grabLock(path);
        return fService.getContentDataForWrite(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentReader(int, java.lang.String)
     */
    public ContentReader getContentReader(int version, String path)
    {
        return fService.getContentReader(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentWriter(java.lang.String)
     */
    public ContentWriter getContentWriter(String path)
    {
        grabLock(path);
        return fService.getContentWriter(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDeleted(int, java.lang.String)
     */
    public List<String> getDeleted(int version, String path)
    {
        return fService.getDeleted(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            int version, String path)
    {
        return fService.getDirectoryListing(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(int, java.lang.String, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            int version, String path, boolean includeDeleted)
    {
        return fService.getDirectoryListing(version, path, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir)
    {
        return fService.getDirectoryListing(dir);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(org.alfresco.service.cmr.avm.AVMNodeDescriptor, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return fService.getDirectoryListing(dir, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingArray(int, java.lang.String, boolean)
     */
    public AVMNodeDescriptor[] getDirectoryListingArray(int version,
            String path, boolean includeDeleted)
    {
        return fService.getDirectoryListingArray(version, path, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingArray(org.alfresco.service.cmr.avm.AVMNodeDescriptor, boolean)
     */
    public AVMNodeDescriptor[] getDirectoryListingArray(AVMNodeDescriptor dir,
            boolean includeDeleted)
    {
        return fService.getDirectoryListingArray(dir, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingDirect(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            int version, String path)
    {
        return fService.getDirectoryListingDirect(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingDirect(int, java.lang.String, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            int version, String path, boolean includeDeleted)
    {
        return fService.getDirectoryListingDirect(version, path, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingDirect(org.alfresco.service.cmr.avm.AVMNodeDescriptor, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return fService.getDirectoryListingDirect(dir, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getFileInputStream(int, java.lang.String)
     */
    public InputStream getFileInputStream(int version, String path)
    {
        return fService.getFileInputStream(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getFileInputStream(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc)
    {
        return fService.getFileInputStream(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getFileOutputStream(java.lang.String)
     */
    public OutputStream getFileOutputStream(String path)
    {
        grabLock(path);
        return fService.getFileOutputStream(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getHeadPaths(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc)
    {
        return fService.getHeadPaths(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getHistory(org.alfresco.service.cmr.avm.AVMNodeDescriptor, int)
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count)
    {
        return fService.getHistory(desc, count);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getIndirectionPath(int, java.lang.String)
     */
    public String getIndirectionPath(int version, String path)
    {
        return fService.getIndirectionPath(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getLatestSnapshotID(java.lang.String)
     */
    public int getLatestSnapshotID(String storeName)
    {
        return fService.getLatestSnapshotID(storeName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getLayeringInfo(int, java.lang.String)
     */
    public LayeringDescriptor getLayeringInfo(int version, String path)
    {
        return fService.getLayeringInfo(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNextVersionID(java.lang.String)
     */
    public int getNextVersionID(String storeName)
    {
        return fService.getNextVersionID(storeName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNodeProperties(int, java.lang.String)
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path)
    {
        return fService.getNodeProperties(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNodeProperties(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Map<QName, PropertyValue> getNodeProperties(AVMNodeDescriptor desc)
    {
        return fService.getNodeProperties(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNodeProperty(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        return fService.getNodeProperty(version, path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPaths(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc)
    {
        return fService.getPaths(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPathsInStoreHead(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String)
     */
    public List<Pair<Integer, String>> getPathsInStoreHead(
            AVMNodeDescriptor desc, String store)
    {
        return fService.getPathsInStoreHead(desc, store);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStore(java.lang.String)
     */
    public AVMStoreDescriptor getStore(String name)
    {
        return fService.getStore(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreProperties(java.lang.String)
     */
    public Map<QName, PropertyValue> getStoreProperties(String store)
    {
        return fService.getStoreProperties(store);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getStoreProperty(String store, QName name)
    {
        return fService.getStoreProperty(store, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreRoot(int, java.lang.String)
     */
    public AVMNodeDescriptor getStoreRoot(int version, String name)
    {
        return fService.getStoreRoot(version, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreVersions(java.lang.String)
     */
    public List<VersionDescriptor> getStoreVersions(String name)
    {
        return fService.getStoreVersions(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreVersions(java.lang.String, java.util.Date, java.util.Date)
     */
    public List<VersionDescriptor> getStoreVersions(String name, Date from,
            Date to)
    {
        return fService.getStoreVersions(name, from, to);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStores()
     */
    public List<AVMStoreDescriptor> getStores()
    {
        return fService.getStores();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getSystemStore()
     */
    public AVMStoreDescriptor getSystemStore()
    {
        return fService.getSystemStore();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#hasAspect(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public boolean hasAspect(int version, String path, QName aspectName)
    {
        return fService.hasAspect(version, path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#link(java.lang.String, java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        // TODO Does this need a lock?  I don't think so, but revisit.
        fService.link(parentPath, name, toLink);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(int, java.lang.String)
     */
    public AVMNodeDescriptor lookup(int version, String path)
    {
        return fService.lookup(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(int, java.lang.String, boolean)
     */
    public AVMNodeDescriptor lookup(int version, String path,
            boolean includeDeleted)
    {
        return fService.lookup(version, path, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name)
    {
        return fService.lookup(dir, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String, boolean)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name,
            boolean includeDeleted)
    {
        return fService.lookup(dir, name, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#makePrimary(java.lang.String)
     */
    public void makePrimary(String path)
    {
        fService.makePrimary(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#makeTransparent(java.lang.String, java.lang.String)
     */
    public void makeTransparent(String dirPath, String name)
    {
        fService.makeTransparent(dirPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#purgeStore(java.lang.String)
     */
    public void purgeStore(String name)
    {
        fService.purgeStore(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#purgeVersion(int, java.lang.String)
     */
    public void purgeVersion(int version, String name)
    {
        fService.purgeVersion(version, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#queryStorePropertyKey(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store,
            QName keyPattern)
    {
        return fService.queryStorePropertyKey(store, keyPattern);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#queryStoresPropertyKeys(org.alfresco.service.namespace.QName)
     */
    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKeys(
            QName keyPattern)
    {
        return fService.queryStoresPropertyKeys(keyPattern);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#removeAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void removeAspect(String path, QName aspectName)
    {
        grabLock(path);
        fService.removeAspect(path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(String parent, String name)
    {
        grabLock(parent + '/' + name);
        fService.removeNode(parent, name);
        String[] storePath = parent.split(":");
        fService.createSnapshot(storePath[0], null, null);
        fLockingService.removeLocksInDirectory(getWebProject(storePath[0]), storePath[0],
                                               storePath[1] + '/' + name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#removeNode(java.lang.String)
     */
    public void removeNode(String path)
    {
        grabLock(path);
        fService.removeNode(path);
        String[] storePath = path.split(":");
        fService.createSnapshot(storePath[0], null, null);
        fLockingService.removeLocksInDirectory(getWebProject(storePath[0]), storePath[0], storePath[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(String srcParent, String srcName, String dstParent,
            String dstName)
    {
        // TODO Unresolved: how to deal with directory level locking.
        // TODO This assumes that the rename occurs within the same web project.
        grabLock(srcParent + '/' + srcName);
        fService.rename(srcParent, srcName, dstParent, dstName);
        String[] srcStorePath = splitPath(srcParent + '/' + srcName);
        String[] dstStorePath = splitPath(dstParent + '/' + dstName);
        String webProject = getWebProject(dstStorePath[0]);
        if (webProject != null)
        {
            fLockingService.modifyLock(webProject, srcStorePath[1], dstStorePath[1], dstStorePath[0], null, null);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#renameStore(java.lang.String, java.lang.String)
     */
    public void renameStore(String sourceName, String destName)
    {
        fService.renameStore(sourceName, destName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#retargetLayeredDirectory(java.lang.String, java.lang.String)
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        // TODO This assumes that directories are not locked.
        fService.retargetLayeredDirectory(path, target);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#revert(java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void revert(String path, AVMNodeDescriptor toRevertTo)
    {
        grabLock(path);
        fService.revert(path, toRevertTo);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setContentData(java.lang.String, org.alfresco.service.cmr.repository.ContentData)
     */
    public void setContentData(String path, ContentData data)
    {
        grabLock(path);
        fService.setContentData(path, data);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setEncoding(java.lang.String, java.lang.String)
     */
    public void setEncoding(String path, String encoding)
    {
        grabLock(path);
        fService.setEncoding(path, encoding);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setGuid(java.lang.String, java.lang.String)
     */
    public void setGuid(String path, String guid)
    {
        grabLock(path);
        fService.setGuid(path, guid);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setMetaDataFrom(java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void setMetaDataFrom(String path, AVMNodeDescriptor from)
    {
        grabLock(path);
        fService.setMetaDataFrom(path, from);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setMimeType(java.lang.String, java.lang.String)
     */
    public void setMimeType(String path, String mimeType)
    {
        grabLock(path);
        fService.setMimeType(path, mimeType);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setNodeProperties(java.lang.String, java.util.Map)
     */
    public void setNodeProperties(String path,
            Map<QName, PropertyValue> properties)
    {
        grabLock(path);
        fService.setNodeProperties(path, properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setNodeProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        grabLock(path);
        fService.setNodeProperty(path, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setOpacity(java.lang.String, boolean)
     */
    public void setOpacity(String path, boolean opacity)
    {
        // TODO Assumes no directory locking.
        fService.setOpacity(path, opacity);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setStoreProperties(java.lang.String, java.util.Map)
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props)
    {
        fService.setStoreProperties(store, props);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setStoreProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setStoreProperty(String store, QName name, PropertyValue value)
    {
        fService.setStoreProperty(store, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#uncover(java.lang.String, java.lang.String)
     */
    public void uncover(String dirPath, String name)
    {
        // TODO What about when this is a directory?
        grabLock(dirPath + '/' + name);
        fService.uncover(dirPath, name);
    }

    private String[] splitPath(String path)
    {
        String[] storePath = path.split(":");
        if (storePath.length != 2)
        {
            throw new AVMBadArgumentException("Invalid Path: " + path);
        }
        return storePath;
    }

    private String getWebProject(String name)
    {
        Map<QName, PropertyValue> results = fService.queryStorePropertyKey(name, QName.createQName(null, ".dns%"));
        if (results.size() != 1)
        {
            return null;
        }
        String dnsString = results.keySet().iterator().next().getLocalName();
        return dnsString.substring(dnsString.lastIndexOf('.') + 1, dnsString.length());
    }

    private void grabLock(String path)
    {
        AVMNodeDescriptor desc = fService.lookup(-1, path, false);
        if (desc != null && desc.isDirectory())
        {
            return;
        }
        String[] storePath = splitPath(path);
        String webProject = getWebProject(storePath[0]);
        if (webProject != null && webProject.equals(storePath[0]))
        {
            // Don't do locking in staging.
            return;
        }
        if (webProject != null)
        {
            String userName = fAuthenticationService.getCurrentUserName();
            if (!fLockingService.hasAccess(webProject, path, userName))
            {
                throw new AVMLockingException("avmlockservice.locked", new Object[]{path});
            }
            if (fLockingService.getLock(webProject, storePath[1]) == null)
            {
                List<String> owners = new ArrayList<String>(1);
                owners.add(userName);
                AVMLock lock = new AVMLock(webProject, storePath[0], storePath[1], AVMLockingService.Type.DISCRETIONARY, owners);
                fLockingService.lockPath(lock);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createDirectory(java.lang.String, java.lang.String, java.util.List, java.util.Map)
     */
    public void createDirectory(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        fService.createDirectory(path, name, aspects, properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String, java.io.InputStream, java.util.List, java.util.Map)
     */
    public void createFile(String path, String name, InputStream in, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        grabLock(path + '/' + name);
        fService.createFile(path, name, in, aspects, properties);
    }
}
