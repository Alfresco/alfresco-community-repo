/**
 * 
 */
package org.alfresco.repo.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.remote.AVMRemoteTransport;
import org.alfresco.service.namespace.QName;

/**
 * Client side wrapper for AVMRemoteTransport.
 * @author britt
 */
public class AVMRemoteImpl implements AVMRemote 
{
    /**
     * The reference to the AVMRemoteTransport instance.
     */
    private AVMRemoteTransport fTransport;

    /**
     * Default constructor.
     */
    public AVMRemoteImpl()
    {
    }
    
    /**
     * Set the remote transport.
     */
    public void setAvmRemoteTransport(AVMRemoteTransport transport)
    {
        fTransport = transport;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createAVMStore(java.lang.String)
     */
    public void createStore(String name) 
    {
        fTransport.createStore(ClientTicketHolder.GetTicket(), name);   
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(int version, String srcPath, String dstPath,
            String name) 
    {
        fTransport.createBranch(ClientTicketHolder.GetTicket(), version,
                                srcPath, dstPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createDirectory(java.lang.String, java.lang.String)
     */
    public void createDirectory(String path, String name)
    {
        fTransport.createDirectory(ClientTicketHolder.GetTicket(), path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createFile(java.lang.String, java.lang.String)
     */
    public OutputStream createFile(String path, String name) 
    {
        return new AVMRemoteOutputStream(fTransport.createFile(ClientTicketHolder.GetTicket(), path, name), fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createLayeredDirectory(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(String targetPath, String parent,
            String name) 
    {
        fTransport.createLayeredDirectory(ClientTicketHolder.GetTicket(), targetPath, parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(String targetPath, String parent, String name) 
    {
        fTransport.createLayeredFile(ClientTicketHolder.GetTicket(), targetPath, parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#createSnapshot(java.lang.String)
     */
    public int createSnapshot(String store, String label, String comment) 
    {
        return fTransport.createSnapshot(ClientTicketHolder.GetTicket(), store, label, comment);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#deleteNodeProperties(java.lang.String)
     */
    public void deleteNodeProperties(String path) 
    {
        fTransport.deleteNodeProperties(ClientTicketHolder.GetTicket(), path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#deleteNodeProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteNodeProperty(String path, QName name) 
    {
        fTransport.deleteNodeProperty(ClientTicketHolder.GetTicket(), path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#deleteStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void deleteStoreProperty(String store, QName name) 
    {
        fTransport.deleteStoreProperty(ClientTicketHolder.GetTicket(), store, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStore(java.lang.String)
     */
    public AVMStoreDescriptor getStore(String name) 
    {
        return fTransport.getStore(ClientTicketHolder.GetTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStoreRoot(int, java.lang.String)
     */
    public AVMNodeDescriptor getStoreRoot(int version, String name) 
    {
        return fTransport.getStoreRoot(ClientTicketHolder.GetTicket(), version, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStoreVersions(java.lang.String)
     */
    public List<VersionDescriptor> getStoreVersions(String name) 
    {
        return fTransport.getStoreVersions(ClientTicketHolder.GetTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStoreVersions(java.lang.String, java.util.Date, java.util.Date)
     */
    public List<VersionDescriptor> getStoreVersions(String name, Date from,
            Date to) 
    {
        return fTransport.getStoreVersions(ClientTicketHolder.GetTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getAVMStores()
     */
    public List<AVMStoreDescriptor> getStores() 
    {
        return fTransport.getStores(ClientTicketHolder.GetTicket());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getCommonAncestor(org.alfresco.service.cmr.avm.AVMNodeDescriptor, org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
            AVMNodeDescriptor right)
    {
        return fTransport.getCommonAncestor(ClientTicketHolder.GetTicket(), left, right);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDeleted(int, java.lang.String)
     */
    public List<String> getDeleted(int version, String path) 
    {
        return fTransport.getDeleted(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDirectoryListing(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            int version, String path) 
    {
        return fTransport.getDirectoryListing(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDirectoryListing(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(
            AVMNodeDescriptor dir)
    {
        return fTransport.getDirectoryListing(ClientTicketHolder.GetTicket(), dir);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getDirectoryListingDirect(int, java.lang.String)
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListingDirect(
            int version, String path) 
    {
        return fTransport.getDirectoryListing(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getFileInputStream(int, java.lang.String)
     */
    public InputStream getFileInputStream(int version, String path) 
    {
        return new AVMRemoteInputStream(fTransport.getInputHandle(ClientTicketHolder.GetTicket(), version, path),
                                        fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getFileInputStream(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc)
    {
        return new AVMRemoteInputStream(fTransport.getInputHandle(ClientTicketHolder.GetTicket(), desc),
                                        fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getFileOutputStream(java.lang.String)
     */
    public OutputStream getFileOutputStream(String path)
    {
        return new AVMRemoteOutputStream(fTransport.getOutputHandle(ClientTicketHolder.GetTicket(), path),
                                         fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getHistory(org.alfresco.service.cmr.avm.AVMNodeDescriptor, int)
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count) 
    {
        return fTransport.getHistory(ClientTicketHolder.GetTicket(), desc, count);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getIndirectionPath(int, java.lang.String)
     */
    public String getIndirectionPath(int version, String path) 
    {
        return fTransport.getIndirectionPath(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getLatestSnapshotID(java.lang.String)
     */
    public int getLatestSnapshotID(String storeName) 
    {
        return fTransport.getLatestSnapshotID(ClientTicketHolder.GetTicket(), storeName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getLatestVersionID(java.lang.String)
     */
    public int getNextVersionID(String storeName) 
    {
        return fTransport.getNextVersionID(ClientTicketHolder.GetTicket(), storeName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getLayeringInfo(int, java.lang.String)
     */
    public LayeringDescriptor getLayeringInfo(int version, String path) 
    {
        return fTransport.getLayeringInfo(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getNodeProperties(int, java.lang.String)
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path) 
    {
        return fTransport.getNodeProperties(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getNodeProperty(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getNodeProperty(int version, String path, QName name) 
    {
        return fTransport.getNodeProperty(ClientTicketHolder.GetTicket(), version, path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getStoreProperties(java.lang.String)
     */
    public Map<QName, PropertyValue> getStoreProperties(String store) 
    {
        return fTransport.getStoreProperties(ClientTicketHolder.GetTicket(), store);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#getStoreProperty(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public PropertyValue getStoreProperty(String store, QName name) 
    {
        return fTransport.getStoreProperty(ClientTicketHolder.GetTicket(), store, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#lookup(int, java.lang.String)
     */
    public AVMNodeDescriptor lookup(int version, String path) 
    {
        return fTransport.lookup(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#lookup(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String)
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name) 
    {
        return fTransport.lookup(ClientTicketHolder.GetTicket(), dir, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#makePrimary(java.lang.String)
     */
    public void makePrimary(String path) 
    {
        fTransport.makePrimary(ClientTicketHolder.GetTicket(), path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#purgeAVMStore(java.lang.String)
     */
    public void purgeStore(String name) 
    {
        fTransport.purgeStore(ClientTicketHolder.GetTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#purgeVersion(int, java.lang.String)
     */
    public void purgeVersion(int version, String name) 
    {
        fTransport.purgeVersion(ClientTicketHolder.GetTicket(), version, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#queryStorePropertyKey(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store,
            QName keyPattern) 
    {
        return fTransport.queryStorePropertyKey(ClientTicketHolder.GetTicket(), store, keyPattern);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#queryStoresPropertyKey(org.alfresco.service.namespace.QName)
     */
    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKey(
            QName keyPattern) 
    {
        return fTransport.queryStoresPropertyKey(ClientTicketHolder.GetTicket(), keyPattern);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(String parent, String name) 
    {
        fTransport.removeNode(ClientTicketHolder.GetTicket(), parent, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(String srcParent, String srcName, String dstParent,
            String dstName) 
    {
        fTransport.rename(ClientTicketHolder.GetTicket(), srcParent, srcName, dstParent, dstName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#retargetLayeredDirectory(java.lang.String, java.lang.String)
     */
    public void retargetLayeredDirectory(String path, String target) 
    {
        fTransport.retargetLayeredDirectory(ClientTicketHolder.GetTicket(), path, target);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setNodeProperties(java.lang.String, java.util.Map)
     */
    public void setNodeProperties(String path,
            Map<QName, PropertyValue> properties) 
    {
        fTransport.setNodeProperties(ClientTicketHolder.GetTicket(), path, properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setNodeProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setNodeProperty(String path, QName name, PropertyValue value) 
    {
        fTransport.setNodeProperty(ClientTicketHolder.GetTicket(), path, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setOpacity(java.lang.String, boolean)
     */
    public void setOpacity(String path, boolean opacity) 
    {
        fTransport.setOpacity(ClientTicketHolder.GetTicket(), path, opacity);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setStoreProperties(java.lang.String, java.util.Map)
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props) 
    {
        fTransport.setStoreProperties(ClientTicketHolder.GetTicket(), store, props);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#setStoreProperty(java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void setStoreProperty(String store, QName name, PropertyValue value) 
    {
        fTransport.setStoreProperty(ClientTicketHolder.GetTicket(), store, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#uncover(java.lang.String, java.lang.String)
     */
    public void uncover(String dirPath, String name) 
    {
        fTransport.uncover(ClientTicketHolder.GetTicket(), dirPath, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMRemote#renameStore(java.lang.String, java.lang.String)
     */
    public void renameStore(String sourceName, String destName) 
    {
        fTransport.renameStore(ClientTicketHolder.GetTicket(), sourceName, destName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#addAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void addAspect(String path, QName aspectName) 
    {
        fTransport.addAspect(ClientTicketHolder.GetTicket(), path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#getAspects(int, java.lang.String)
     */
    public List<QName> getAspects(int version, String path) 
    {
        return fTransport.getAspects(ClientTicketHolder.GetTicket(), version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#hasAspect(int, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public boolean hasAspect(int version, String path, QName aspectName) 
    {
        return fTransport.hasAspect(ClientTicketHolder.GetTicket(), version, path, aspectName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AVMRemote#removeAspect(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public void removeAspect(String path, QName aspectName) 
    {
        fTransport.removeAspect(ClientTicketHolder.GetTicket(), path, aspectName);
    }
}
