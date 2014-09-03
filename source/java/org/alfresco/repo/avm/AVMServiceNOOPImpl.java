/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

// Sparta: disable WCM/AVM - temporary (until WCM/AVM has been fully removed)
public class AVMServiceNOOPImpl implements AVMService
{
    /**
     * Basic constructor for the service.
     */
    public AVMServiceNOOPImpl()
    {
    }

    @Override
    public InputStream getFileInputStream(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getFileInputStream(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getFileOutputStream(String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir, String childNamePattern)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getDeleted(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream createFile(String path, String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createDirectory(String path, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createLayeredFile(String targetPath, String parent, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createLayeredDirectory(String targetPath, String parent,
            String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void retargetLayeredDirectory(String path, String target)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createStore(String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createStore(String name, Map<QName, PropertyValue> props)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createBranch(int version, String srcPath, String dstPath,
            String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeNode(String parent, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rename(String srcParent, String srcName, String dstParent,
            String dstName)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void uncover(String dirPath, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getNextVersionID(String storeName)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLatestSnapshotID(String storeName)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Integer> createSnapshot(String store, String tag,
            String description)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VersionDescriptor> getStoreVersions(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VersionDescriptor> getStoreVersions(String name, Date from,
            Date to)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AVMStoreDescriptor> getStores()
    {
        return Collections.emptyList();
    }

    @Override
    public AVMStoreDescriptor getStore(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AVMNodeDescriptor getStoreRoot(int version, String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AVMNodeDescriptor lookup(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AVMNodeDescriptor lookup(int version, String path,
            boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name,
            boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIndirectionPath(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void purgeStore(String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void purgeVersion(int version, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void makePrimary(String path)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOpacity(String path, boolean opacity)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
            AVMNodeDescriptor right)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LayeringDescriptor getLayeringInfo(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNodeProperties(String path,
            Map<QName, PropertyValue> properties)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<QName, PropertyValue> getNodeProperties(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteNodeProperty(String path, QName name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteNodeProperties(String path)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setStoreProperty(String store, QName name, PropertyValue value)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setStoreProperties(String store, Map<QName, PropertyValue> props)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PropertyValue getStoreProperty(String store, QName name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<QName, PropertyValue> getStoreProperties(String store)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<QName, PropertyValue> queryStorePropertyKey(String store,
            QName keyPattern)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteStoreProperty(String store, QName name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addAspect(String path, QName aspectName)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Set<QName> getAspects(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeAspect(String path, QName aspectName)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasAspect(int version, String path, QName aspectName)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void renameStore(String sourceName, String destName)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revert(String path, AVMNodeDescriptor toRevertTo)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setGuid(String path, String guid)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMimeType(String path, String mimeType)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setEncoding(String path, String encoding)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKeys(
            QName keyPattern)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createDirectory(String path, String name, List<QName> aspects,
            Map<QName, PropertyValue> properties)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createFile(String path, String name, InputStream in)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createFile(String path, String name, InputStream in,
            List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir, boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public AVMNodeDescriptor[] getDirectoryListingArray(AVMNodeDescriptor dir,
            boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            int version, String path, boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AVMNodeDescriptor[] getDirectoryListingArray(int version,
            String path, boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            AVMNodeDescriptor dir, boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            int version, String path, boolean includeDeleted)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeNode(String path)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void makeTransparent(String dirPath, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AVMStoreDescriptor getSystemStore()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMetaDataFrom(String path, AVMNodeDescriptor from)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void link(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateLink(String parentPath, String name,
            AVMNodeDescriptor toLink)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AVMNodeDescriptor forceCopy(String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void copy(int srcVersion, String srcPath, String dstPath, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<Integer, String>> getPathsInStoreHead(
            AVMNodeDescriptor desc, String store)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getPathsInStoreVersion(AVMNodeDescriptor desc,
            String store, int version)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<QName, PropertyValue> getNodeProperties(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<QName> getAspects(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentReader getContentReader(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentWriter getContentWriter(String path, boolean update)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentData getContentDataForRead(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentData getContentDataForRead(AVMNodeDescriptor desc)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentData getContentDataForWrite(String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setContentData(String path, ContentData data)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<VersionDescriptor> getStoreVersionsFrom(String name, int version)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VersionDescriptor> getStoreVersionsTo(String name, int version)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VersionDescriptor> getStoreVersionsBetween(String name,
            int startVersion, int endVersion)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
