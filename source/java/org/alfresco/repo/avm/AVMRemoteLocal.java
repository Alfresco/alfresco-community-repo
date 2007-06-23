/**
 * 
 */
package org.alfresco.repo.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * A loopback implementation of the AVMRemote interface?
 * @author britt
 */
public class AVMRemoteLocal implements AVMRemote 
{
    /**
     * The AVMService instance.
     */
    private AVMService fService;
    
    /**
     * Default constructor.
     */
    public AVMRemoteLocal()
    {
    }
    
    /**
     * Setter for the AVMService instance.
     */
    public void setAvmService(AVMService service)
    {
        fService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createAVMStore(java.lang.String)
     */
    public void createStore(String name) 
    {
        fService.createStore(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(int version, String srcPath, String dstPath,
            String name) 
    {
        fService.createBranch(version, srcPath, dstPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createDirectory(java.lang.String, java.lang.String)
     */
    public void createDirectory(String path, String name) 
    {
        fService.createDirectory(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createFile(java.lang.String, java.lang.String)
     */
    public OutputStream createFile(String path, String name) 
    {
        return fService.createFile(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createLayeredDirectory(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(String targetPath, String parent,
            String name) 
    {
        fService.createLayeredDirectory(targetPath, parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(String targetPath, String parent, String name) 
    {
        fService.createLayeredFile(targetPath, parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createSnapshot(java.lang.String)
     */
    public Map<String, Integer> createSnapshot(String store, String label, String comment) 
    {
        return fService.createSnapshot(store, label, comment);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#deleteNodeProperties(java.lang.String)
     */
    public void deleteNodeProperties(String path) 
    {
        fService.deleteNodeProperties(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#deleteNodeProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteNodeProperty(String path, QName name) 
    {
        fService.deleteNodeProperty(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#deleteStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteStoreProperty(String store, QName name) 
    {
        fService.deleteStoreProperty(store, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStore(java.lang.String)
     */
    public AVMStoreDescriptor getStore(String name) 
    {
        return fService.getStore(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStoreRoot(int, java.lang.String)
     */
    public AVMNodeDescriptor getStoreRoot(int version, String name) 
    {
        return fService.getStoreRoot(version, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStoreVersions(java.lang.String)
     */
    public List<VersionDescriptor> getStoreVersions(String name) 
    {
        return fService.getStoreVersions(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStoreVersions(java.lang.String, java.util.Date, java.util.Date)
     */
    public List<VersionDescriptor> getStoreVersions(String name, Date from,
            Date to) 
    {
        return fService.getStoreVersions(name, from, to);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStores()
     */
    public List<AVMStoreDescriptor> getStores() 
    {
        return fService.getStores();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getCommonAncestor(org.alfresco.service.cmr.avm.AVMNodeDescriptor, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
            AVMNodeDescriptor right) 
    {
        return fService.getCommonAncestor(left, right);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDeleted(int, java.lang.String)
     */
    public List<String> getDeleted(int version, String path) 
    {
        return fService.getDeleted(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDirectoryListing(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            int version, String path) 
    {
        return fService.getDirectoryListing(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDirectoryListing(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir) 
    {
        return fService.getDirectoryListing(dir);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDirectoryListingDirect(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            int version, String path) 
    {
        return fService.getDirectoryListingDirect(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getFileInputStream(int, java.lang.String)
     */
    public InputStream getFileInputStream(int version, String path) 
    {
        return fService.getFileInputStream(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getFileInputStream(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc) 
    {
        return fService.getFileInputStream(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getFileOutputStream(java.lang.String)
     */
    public OutputStream getFileOutputStream(String path) 
    {
        return fService.getFileOutputStream(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getHistory(org.alfresco.service.cmr.avm.AVMNodeDescriptor, int)
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count) 
    {
        return fService.getHistory(desc, count);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getIndirectionPath(int, java.lang.String)
     */
    public String getIndirectionPath(int version, String path) 
    {
        return fService.getIndirectionPath(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getLatestSnapshotID(java.lang.String)
     */
    public int getLatestSnapshotID(String storeName) 
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getLatestVersionID(java.lang.String)
     */
    public int getNextVersionID(String storeName) 
    {
        return fService.getNextVersionID(storeName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getLayeringInfo(int, java.lang.String)
     */
    public LayeringDescriptor getLayeringInfo(int version, String path) 
    {
        return fService.getLayeringInfo(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getNodeProperties(int, java.lang.String)
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path) 
    {
        return fService.getNodeProperties(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getNodeProperty(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getNodeProperty(int version, String path, QName name) 
    {
        return fService.getNodeProperty(version, path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getStoreProperties(java.lang.String)
     */
    public Map<QName, PropertyValue> getStoreProperties(String store) 
    {
        return fService.getStoreProperties(store);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getStoreProperty(String store, QName name) 
    {
        return fService.getStoreProperty(store, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#lookup(int, java.lang.String)
     */
    public AVMNodeDescriptor lookup(int version, String path) 
    {
        return fService.lookup(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#lookup(int, java.lang.String, boolean)
     */
    public AVMNodeDescriptor lookup(int version, String path, boolean includeDeleted) 
    {
        return fService.lookup(version, path, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name) 
    {
        return fService.lookup(dir, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String, boolean includeDeleted)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name, boolean includeDeleted) 
    {
        return fService.lookup(dir, name, includeDeleted);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#makePrimary(java.lang.String)
     */
    public void makePrimary(String path) 
    {
        fService.makePrimary(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#purgeAVMStore(java.lang.String)
     */
    public void purgeStore(String name) 
    {
        fService.purgeStore(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#purgeVersion(int, java.lang.String)
     */
    public void purgeVersion(int version, String name) 
    {
        fService.purgeVersion(version, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#queryStorePropertyKey(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store,
            QName keyPattern) 
    {
        return fService.queryStorePropertyKey(store, keyPattern);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#queryStoresPropertyKey(org.alfresco.service.namespace.QName)
     */
    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKey(
            QName keyPattern)
    {
        return fService.queryStoresPropertyKeys(keyPattern);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(String parent, String name) 
    {
        fService.removeNode(parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(String srcParent, String srcName, String dstParent,
            String dstName) 
    {
        fService.rename(srcParent, srcName, dstParent, dstName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#retargetLayeredDirectory(java.lang.String, java.lang.String)
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        fService.retargetLayeredDirectory(path, target);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setNodeProperties(java.lang.String, java.util.Map)
     */
    public void setNodeProperties(String path,
            Map<QName, PropertyValue> properties) 
    {
        fService.setNodeProperties(path, properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setNodeProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setNodeProperty(String path, QName name, PropertyValue value) 
    {
        fService.setNodeProperty(path, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setOpacity(java.lang.String, boolean)
     */
    public void setOpacity(String path, boolean opacity) 
    {
        fService.setOpacity(path, opacity);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setStoreProperties(java.lang.String, java.util.Map)
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props) 
    {
        fService.setStoreProperties(store, props);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setStoreProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setStoreProperty(String store, QName name, PropertyValue value) 
    {
        fService.setStoreProperty(store, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#uncover(java.lang.String, java.lang.String)
     */
    public void uncover(String dirPath, String name) 
    {
        fService.uncover(dirPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#renameStore(java.lang.String, java.lang.String)
     */
    public void renameStore(String sourceName, String destName) 
    {
        fService.renameStore(sourceName, destName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#addAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void addAspect(String path, QName aspectName) 
    {
        fService.addAspect(path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#getAspects(int, java.lang.String)
     */
    public List<QName> getAspects(int version, String path) 
    {
        return fService.getAspects(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#hasAspect(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public boolean hasAspect(int version, String path, QName aspectName) 
    {
        return fService.hasAspect(version, path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#removeAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void removeAspect(String path, QName aspectName) 
    {
        fService.removeAspect(path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#revert(java.lang.String, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public void revert(String path, AVMNodeDescriptor toRevertTo) 
    {
        fService.revert(path, toRevertTo);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#getAPath(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc) 
    {
        return fService.getAPath(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#setGuid(java.lang.String, java.lang.String)
     */
    public void setGuid(String path, String guid) 
    {
        fService.setGuid(path, guid);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#setEncoding(java.lang.String, java.lang.String)
     */
    public void setEncoding(String path, String encoding)
    {
        fService.setEncoding(path, encoding);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#setMimeType(java.lang.String, java.lang.String)
     */
    public void setMimeType(String path, String mimeType)
    {
        fService.setMimeType(path, mimeType);
    }
}
