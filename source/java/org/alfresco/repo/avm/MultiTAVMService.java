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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * An MT-aware wrapper of AVMService
 * 
 * @author janv
 */
public class MultiTAVMService implements AVMService
{
    private AVMService fService;
    private TenantService tenantService;
    
    Boolean enabled = null;

    public void setAvmService(AVMService fService)
    {
        this.fService = fService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#addAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void addAspect(String path, QName aspectName)
    {
        fService.addAspect(getTenantPath(path), aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#copy(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void copy(int srcVersion, String srcPath, String dstPath, String name)
    {
        fService.copy(srcVersion, getTenantPath(srcPath), getTenantPath(dstPath), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(int version, String srcPath, String dstPath, String name)
    {
        fService.createBranch(version, getTenantPath(srcPath), getTenantPath(dstPath), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createDirectory(java.lang.String, java.lang.String)
     */
    public void createDirectory(String path, String name)
    {
        fService.createDirectory(getTenantPath(path), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String)
     */
    public OutputStream createFile(String path, String name)
    {
        return fService.createFile(getTenantPath(path), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String, java.io.InputStream)
     */
    public void createFile(String path, String name, InputStream in)
    {
        fService.createFile(getTenantPath(path), name, in);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createLayeredDirectory(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(String targetPath, String parentPath, String name)
    {
        fService.createLayeredDirectory(getTenantPath(targetPath), getTenantPath(parentPath), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(String targetPath, String parentPath, String name)
    {
        fService.createLayeredFile(getTenantPath(targetPath), getTenantPath(parentPath), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createSnapshot(java.lang.String, java.lang.String, java.lang.String)
     */
    public Map<String, Integer> createSnapshot(String storeName, String tag, String description)
    {
        // TODO
        return fService.createSnapshot(getTenantStoreName(storeName), tag, description);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createStore(java.lang.String)
     */
    public void createStore(String storeName)
    {
        fService.createStore(getTenantStoreName(storeName));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createStore(java.lang.String, java.util.Map)
     */
    public void createStore(String name, Map<QName, PropertyValue> props)
    {
        fService.createStore(name, props);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#deleteNodeProperties(java.lang.String)
     */
    public void deleteNodeProperties(String path)
    {
        fService.deleteNodeProperties(getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#deleteNodeProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteNodeProperty(String path, QName name)
    {
        fService.deleteNodeProperty(getTenantPath(path), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#deleteStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteStoreProperty(String storeName, QName name)
    {
        fService.deleteStoreProperty(getTenantStoreName(storeName), name);
    }

    public void updateLink(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        fService.updateLink(getTenantPath(parentPath), name, toLink);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#forceCopy(java.lang.String)
     */
    public AVMNodeDescriptor forceCopy(String path)
    {
        return getBaseNode(fService.forceCopy(getTenantPath(path)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAPath(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc)
    {
        return getBaseVPath(fService.getAPath(getTenantNode(desc)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPathsInStoreVersion(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String, int)
     */
    public List<String> getPathsInStoreVersion(AVMNodeDescriptor desc, String storeName, int version)
    {
        return getBasePaths(fService.getPathsInStoreVersion(getTenantNode(desc), getTenantStoreName(storeName), version));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAspects(int, java.lang.String)
     */
    public Set<QName> getAspects(int version, String path)
    {
        return fService.getAspects(version, getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAspects(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Set<QName> getAspects(AVMNodeDescriptor desc)
    {
        return fService.getAspects(getTenantNode(desc));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getCommonAncestor(org.alfresco.service.cmr.avm.AVMNodeDescriptor, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left, AVMNodeDescriptor right)
    {
        return getBaseNode(fService.getCommonAncestor(getTenantNode(left), getTenantNode(right)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForRead(int, java.lang.String)
     */
    public ContentData getContentDataForRead(int version, String path)
    {
        return fService.getContentDataForRead(version, getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForRead(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public ContentData getContentDataForRead(AVMNodeDescriptor desc)
    {
        return fService.getContentDataForRead(getTenantNode(desc));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForWrite(java.lang.String)
     */
    public ContentData getContentDataForWrite(String path)
    {
        return fService.getContentDataForWrite(getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentReader(int, java.lang.String)
     */
    public ContentReader getContentReader(int version, String path)
    {
        return fService.getContentReader(version, getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentWriter(java.lang.String, boolean)
     */
    public ContentWriter getContentWriter(String path, boolean update)
    {
        return fService.getContentWriter(getTenantPath(path), update);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDeleted(int, java.lang.String)
     */
    public List<String> getDeleted(int version, String path)
    {
        return fService.getDeleted(version, getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path)
    {
        return getBaseNodes(fService.getDirectoryListing(version, getTenantPath(path)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(int, java.lang.String, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path, boolean includeDeleted)
    {
        return getBaseNodes(fService.getDirectoryListing(version, getTenantPath(path), includeDeleted));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir)
    {
        return getBaseNodes(fService.getDirectoryListing(getTenantNode(dir)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListing(org.alfresco.service.cmr.avm.AVMNodeDescriptor, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return getBaseNodes(fService.getDirectoryListing(getTenantNode(dir), includeDeleted));
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, String childNamePattern)
    {
        return getBaseNodes(fService.getDirectoryListing(getTenantNode(dir), childNamePattern));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingArray(int, java.lang.String, boolean)
     */
    public AVMNodeDescriptor[] getDirectoryListingArray(int version, String path, boolean includeDeleted)
    {
        return getBaseNodes(fService.getDirectoryListingArray(version, getTenantPath(path), includeDeleted));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingArray(org.alfresco.service.cmr.avm.AVMNodeDescriptor, boolean)
     */
    public AVMNodeDescriptor[] getDirectoryListingArray(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return getBaseNodes(fService.getDirectoryListingArray(getTenantNode(dir), includeDeleted));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingDirect(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(int version, String path)
    {
        return getBaseNodes(fService.getDirectoryListingDirect(version, getTenantPath(path)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingDirect(int, java.lang.String, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(int version, String path, boolean includeDeleted)
    {
        return getBaseNodes(fService.getDirectoryListingDirect(version, getTenantPath(path), includeDeleted));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getDirectoryListingDirect(org.alfresco.service.cmr.avm.AVMNodeDescriptor, boolean)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return getBaseNodes(fService.getDirectoryListingDirect(getTenantNode(dir), includeDeleted));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getFileInputStream(int, java.lang.String)
     */
    public InputStream getFileInputStream(int version, String path)
    {
        return fService.getFileInputStream(version, getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getFileInputStream(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc)
    {
        return fService.getFileInputStream(getTenantNode(desc));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getFileOutputStream(java.lang.String)
     */
    public OutputStream getFileOutputStream(String path)
    {
        return fService.getFileOutputStream(getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getHeadPaths(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc)
    {
        return fService.getHeadPaths(getTenantNode(desc));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getHistory(org.alfresco.service.cmr.avm.AVMNodeDescriptor, int)
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count)
    {
        return getBaseNodes(fService.getHistory(getTenantNode(desc), count));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getIndirectionPath(int, java.lang.String)
     */
    public String getIndirectionPath(int version, String path)
    {
        return getBasePath(fService.getIndirectionPath(version, getTenantPath(path)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getLatestSnapshotID(java.lang.String)
     */
    public int getLatestSnapshotID(String storeName)
    {
        return fService.getLatestSnapshotID(getTenantStoreName(storeName));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getLayeringInfo(int, java.lang.String)
     */
    public LayeringDescriptor getLayeringInfo(int version, String path)
    {
        return fService.getLayeringInfo(version, getTenantPath(path));
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
        return fService.getNodeProperties(version, getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNodeProperties(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Map<QName, PropertyValue> getNodeProperties(AVMNodeDescriptor desc)
    {
        // TODO - review
        return fService.getNodeProperties(getTenantNode(desc));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNodeProperty(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        // TODO - review
        return fService.getNodeProperty(version, getTenantPath(path), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPaths(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc)
    {
        return getBaseVPaths(fService.getPaths(getTenantNode(desc)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPathsInStoreHead(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String)
     */
    public List<Pair<Integer, String>> getPathsInStoreHead(AVMNodeDescriptor desc, String storeName)
    {
        return getBaseVPaths(fService.getPathsInStoreHead(getTenantNode(desc), getTenantStoreName(storeName)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStore(java.lang.String)
     */
    public AVMStoreDescriptor getStore(String storeName)
    {
        return getBaseStore(fService.getStore(getTenantStoreName(storeName)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreProperties(java.lang.String)
     */
    public Map<QName, PropertyValue> getStoreProperties(String storeName)
    {
        return fService.getStoreProperties(getTenantStoreName(storeName));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getStoreProperty(String storeName, QName name)
    {
        return fService.getStoreProperty(getTenantStoreName(storeName), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreRoot(int, java.lang.String)
     */
    public AVMNodeDescriptor getStoreRoot(int version, String storeName)
    {
        return getBaseNode(fService.getStoreRoot(version, getTenantStoreName(storeName)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreVersions(java.lang.String)
     */
    public List<VersionDescriptor> getStoreVersions(String storeName)
    {
        // TODO - review
        return fService.getStoreVersions(getTenantStoreName(storeName));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreVersions(java.lang.String, java.util.Date, java.util.Date)
     */
    public List<VersionDescriptor> getStoreVersions(String storeName, Date from, Date to)
    {
        // TODO - review
        return fService.getStoreVersions(getTenantStoreName(storeName), from, to);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStores()
     */
    public List<AVMStoreDescriptor> getStores()
    {
        List<AVMStoreDescriptor> allAvmStores = fService.getStores();
        if (isTenantServiceEnabled())
        {
            List<AVMStoreDescriptor> convertedValues = new ArrayList<AVMStoreDescriptor>();
            for (AVMStoreDescriptor store : allAvmStores)
            {
                try
                {
                    // MT: return tenant stores only (although for super System return all stores)
                    String runAsUser = AuthenticationUtil.getRunAsUser();
                    if (! EqualsHelper.nullSafeEquals(runAsUser, AuthenticationUtil.SYSTEM_USER_NAME))
                    {
                        tenantService.checkDomain(store.getName());
                        store = getBaseStore(store);
                    }
                    
                    convertedValues.add(store);
                }
                catch (RuntimeException re)
                {
                    // deliberately ignore - stores in different domain will not be listed
                }
            }
            
            return convertedValues;
        }
        else
        {
            return allAvmStores;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getSystemStore()
     */
    public AVMStoreDescriptor getSystemStore()
    {
        return getBaseStore(fService.getSystemStore());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#hasAspect(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public boolean hasAspect(int version, String path, QName aspectName)
    {
        return fService.hasAspect(version, getTenantPath(path), aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#link(java.lang.String, java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        fService.link(getTenantPath(parentPath), name, getTenantNode(toLink));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(int, java.lang.String)
     */
    public AVMNodeDescriptor lookup(int version, String path)
    {
        return getBaseNode(fService.lookup(version, getTenantPath(path)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(int, java.lang.String, boolean)
     */
    public AVMNodeDescriptor lookup(int version, String path, boolean includeDeleted)
    {
        return getBaseNode(fService.lookup(version, getTenantPath(path), includeDeleted));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name)
    {
        return getBaseNode(fService.lookup(getTenantNode(dir), name));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String, boolean)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name, boolean includeDeleted)
    {
        return getBaseNode(fService.lookup(getTenantNode(dir), name, includeDeleted));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#makePrimary(java.lang.String)
     */
    public void makePrimary(String path)
    {
        fService.makePrimary(getTenantPath(path));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#makeTransparent(java.lang.String, java.lang.String)
     */
    public void makeTransparent(String dirPath, String name)
    {
        fService.makeTransparent(getTenantPath(dirPath), name);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#purgeStore(java.lang.String)
     */
    public void purgeStore(String storeName)
    {
        fService.purgeStore(getTenantStoreName(storeName));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#purgeVersion(int, java.lang.String)
     */
    public void purgeVersion(int version, String storeName)
    {
        fService.purgeVersion(version, getTenantStoreName(storeName));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#queryStorePropertyKey(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String storeName, QName keyPattern)
    {
        return fService.queryStorePropertyKey(getTenantStoreName(storeName), keyPattern);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#queryStoresPropertyKeys(org.alfresco.service.namespace.QName)
     */
    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKeys(QName keyPattern)
    {
        // TODO - review
        return fService.queryStoresPropertyKeys(keyPattern);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#removeAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void removeAspect(String path, QName aspectName)
    {
        fService.removeAspect(getTenantPath(path), aspectName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(String parent, String name)
    {
        fService.removeNode(getTenantPath(parent), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#removeNode(java.lang.String)
     */
    public void removeNode(String path)
    {
        fService.removeNode(getTenantPath(path));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(String srcParent, String srcName, String dstParent, String dstName)
    {
        fService.rename(getTenantPath(srcParent), srcName, getTenantPath(dstParent), dstName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#renameStore(java.lang.String, java.lang.String)
     */
    public void renameStore(String sourceStoreName, String destStoreName)
    {
        fService.renameStore(getTenantStoreName(sourceStoreName), getTenantStoreName(destStoreName));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#retargetLayeredDirectory(java.lang.String, java.lang.String)
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        fService.retargetLayeredDirectory(getTenantPath(path), target);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#revert(java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void revert(String path, AVMNodeDescriptor toRevertTo)
    {
        fService.revert(getTenantPath(path), getTenantNode(toRevertTo));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setContentData(java.lang.String, org.alfresco.service.cmr.repository.ContentData)
     */
    public void setContentData(String path, ContentData data)
    {
        fService.setContentData(getTenantPath(path), data);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setEncoding(java.lang.String, java.lang.String)
     */
    public void setEncoding(String path, String encoding)
    {
        fService.setEncoding(getTenantPath(path), encoding);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setGuid(java.lang.String, java.lang.String)
     */
    public void setGuid(String path, String guid)
    {
        fService.setGuid(getTenantPath(path), guid);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setMetaDataFrom(java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void setMetaDataFrom(String path, AVMNodeDescriptor from)
    {
        fService.setMetaDataFrom(getTenantPath(path), getTenantNode(from));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setMimeType(java.lang.String, java.lang.String)
     */
    public void setMimeType(String path, String mimeType)
    {
        fService.setMimeType(getTenantPath(path), mimeType);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setNodeProperties(java.lang.String, java.util.Map)
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties)
    {
        fService.setNodeProperties(getTenantPath(path), properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setNodeProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        fService.setNodeProperty(getTenantPath(path), name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setOpacity(java.lang.String, boolean)
     */
    public void setOpacity(String path, boolean opacity)
    {
        fService.setOpacity(getTenantPath(path), opacity);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setStoreProperties(java.lang.String, java.util.Map)
     */
    public void setStoreProperties(String storeName, Map<QName, PropertyValue> props)
    {
        fService.setStoreProperties(getTenantStoreName(storeName), props);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setStoreProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setStoreProperty(String storeName, QName name, PropertyValue value)
    {
        fService.setStoreProperty(getTenantStoreName(storeName), name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#uncover(java.lang.String, java.lang.String)
     */
    public void uncover(String dirPath, String name)
    {
        fService.uncover(getTenantPath(dirPath), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createDirectory(java.lang.String, java.lang.String, java.util.List, java.util.Map)
     */
    public void createDirectory(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        fService.createDirectory(getTenantPath(path), name, aspects, properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String, java.io.InputStream, java.util.List, java.util.Map)
     */
    public void createFile(String path, String name, InputStream in, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        fService.createFile(getTenantPath(path), name, in, aspects, properties);
    }
    
    
    public List<VersionDescriptor> getStoreVersionsTo(String name, int version)
    {
        // TODO - review
        return fService.getStoreVersionsTo(name, version);
    }

    public List<VersionDescriptor> getStoreVersionsFrom(String name, int version)
    {
        // TODO - review
        return fService.getStoreVersionsFrom(name, version);
    }
    
    public List<VersionDescriptor> getStoreVersionsBetween(String name, int from, int to)
    {
     // TODO - review
        return fService.getStoreVersionsBetween(name, from, to);
    }
    
    private String getTenantStoreName(String avmStoreName)
    {
        if ((avmStoreName == null) || (! isTenantServiceEnabled()))
        { 
            return avmStoreName; 
        }
        
        return tenantService.getName(avmStoreName);
    }
    
    private String getBaseStoreName(String avmStoreName)
    {
        if ((avmStoreName == null) || (! isTenantServiceEnabled()))
        { 
            return avmStoreName; 
        }
        
        return tenantService.getBaseName(avmStoreName);
    }
    
    private String getTenantPath(String avmPath)
    {
        if ((avmPath == null) || (! isTenantServiceEnabled()) || avmPath.equals("UNKNOWN/UNKNOWN"))
        { 
            return avmPath; 
        }

        String[] storePath = splitPath(avmPath);
        return tenantService.getName(storePath[0]) + ':' + storePath[1];
    }
    
    private String getBasePath(String avmPath)
    {
        // note: ALFCOM-2893 - getCommonAncestor can return node with path = "/"
        if ((avmPath == null) || (! isTenantServiceEnabled()) || (avmPath.equals("/")) || avmPath.equals("UNKNOWN/UNKNOWN"))
        { 
            return avmPath; 
        }

        String[] storePath = splitPath(avmPath);
        return tenantService.getBaseName(storePath[0]) + ':' + storePath[1];
    }
    
    private Pair<Integer, String> getBaseVPath(Pair<Integer, String> p)
    {
        if ((p == null) || (! isTenantServiceEnabled()))
        { 
            return p;
        }
        
        return new Pair<Integer, String>(p.getFirst(), getBasePath(p.getSecond()));
    }
    
    private List<Pair<Integer, String>> getBaseVPaths(List<Pair<Integer, String>> paths)
    {
        if ((paths == null) || (! isTenantServiceEnabled()))
        { 
            return paths;
        }
        
        List<Pair<Integer, String>> convertedPaths = new ArrayList<Pair<Integer, String>>(paths.size());
        for (Pair<Integer, String> path : paths)
        {
            convertedPaths.add(getBaseVPath(path));
        }
        return convertedPaths;
    }
    
    private List<String> getBasePaths(List<String> paths)
    {
        if ((paths == null) || (! isTenantServiceEnabled()))
        { 
            return paths;
        }
        
        List<String> convertedPaths = new ArrayList<String>(paths.size());
        for (String path : paths)
        {
            convertedPaths.add(getBasePath(path));
        }
        return convertedPaths;
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
    
    private AVMNodeDescriptor getTenantNode(AVMNodeDescriptor node)
    {
        if ((node == null) || (! isTenantServiceEnabled()))
        { 
            return node;
        }
        
        return new AVMNodeDescriptor(
                getTenantPath(node.getPath()),
                node.getName(),
                node.getType(),
                node.getCreator(),
                node.getOwner(),
                node.getLastModifier(),
                node.getCreateDate(),
                node.getModDate(),
                node.getAccessDate(),
                node.getId(),
                node.getGuid(),
                node.getVersionID(),
                node.getIndirection(),
                node.getIndirectionVersion(),
                node.isPrimary(),
                node.getLayerID(),
                node.getOpacity(),
                node.getLength(), 
                node.getDeletedType());
    }
    
    private AVMNodeDescriptor getBaseNode(AVMNodeDescriptor node)
    {
        if ((node == null) || (! isTenantServiceEnabled()))
        { 
            return node;
        }
        
        return new AVMNodeDescriptor(
                getBasePath(node.getPath()),
                node.getName(),
                node.getType(),
                node.getCreator(),
                node.getOwner(),
                node.getLastModifier(),
                node.getCreateDate(),
                node.getModDate(),
                node.getAccessDate(),
                node.getId(),
                node.getGuid(),
                node.getVersionID(),
                node.getIndirection(),
                node.getIndirectionVersion(),
                node.isPrimary(),
                node.getLayerID(),
                node.getOpacity(),
                node.getLength(), 
                node.getDeletedType());
    }
    
    private AVMStoreDescriptor getBaseStore(AVMStoreDescriptor store)
    {
        if ((store == null) || (! isTenantServiceEnabled()))
        { 
            return store;
        }
        
        return new AVMStoreDescriptor(
                store.getId(),
                getBaseStoreName(store.getName()),
                store.getCreator(),
                store.getCreateDate());
    }
    
    private SortedMap<String, AVMNodeDescriptor> getBaseNodes(SortedMap<String, AVMNodeDescriptor> nodes)
    {
        if ((nodes == null) || (! isTenantServiceEnabled()))
        { 
            return nodes;
        }
        
        SortedMap<String, AVMNodeDescriptor> convertedNodes = new TreeMap<String, AVMNodeDescriptor>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, AVMNodeDescriptor> entry : nodes.entrySet())
        {
            convertedNodes.put(entry.getKey(), getBaseNode(entry.getValue()));
        }
        return convertedNodes;
    }
    
    private AVMNodeDescriptor[] getBaseNodes(AVMNodeDescriptor[] nodes)
    {
        if ((nodes == null) || (! isTenantServiceEnabled()))
        { 
            return nodes;
        }
        
        AVMNodeDescriptor[] convertedNodes = new AVMNodeDescriptor[nodes.length];
        for (int i = 0; i < nodes.length; i++)
        {
            convertedNodes[i] = getBaseNode(nodes[i]);
        }
        return convertedNodes;
    }
    
    private List<AVMNodeDescriptor> getBaseNodes(List<AVMNodeDescriptor> nodes)
    {
        if ((nodes == null) || (! isTenantServiceEnabled()))
        { 
            return nodes;
        }
        
        List<AVMNodeDescriptor> convertedNodes = new ArrayList<AVMNodeDescriptor>(nodes.size());
        for (AVMNodeDescriptor node : nodes)
        {
            convertedNodes.add(getBaseNode(node));
        }
        return convertedNodes;
    }
    
    private boolean isTenantServiceEnabled()
    {
        if (enabled == null)
        {
            enabled = tenantService.isEnabled();
        }
        
        return enabled;
    }

}
