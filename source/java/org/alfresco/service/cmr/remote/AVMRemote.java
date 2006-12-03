/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.service.cmr.remote;

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
import org.alfresco.service.namespace.QName;

/**
 * Remote interface for AVM.
 * @author britt
 */
public interface AVMRemote
{
    /**
     * Get an input handle. A handle is an opaque reference
     * to a server side input stream.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getFileInputStream(int version, String path);
    
    /**
     * Get an InputStream from a descriptor directly.
     * @param desc The descriptor.
     * @return An InputStream.
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc);
    
    /**
     * Get an opaque handle to a server side output stream.
     * @param path The path to the existing file.
     * @return An opaque handle.
     */
    public OutputStream getFileOutputStream(String path);
    
    /**
     * Get a listing of a directories direct contents.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A sorted listing.
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(int version, String path);
    
    /**
     * Get a listing of a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A sorted listing.
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListing(int version, String path);
    
    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A sorted listing.
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListing(AVMNodeDescriptor dir);
    
    /**
     * Get the names of nodes that have been deleted in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A list of deleted names.
     */
    public List<String> getDeleted(int version, String path);
    
    /**
     * Create a file and return a handle to an output stream.
     * @param path The path to the file.
     * @param name The name of the file to create.
     * @return An opaque handle to a server side output stream.
     */
    public OutputStream createFile(String path, String name);
    
    /**
     * Create a directory.
     * @param path The path to the containing directory.
     * @param name The name for the new directory.
     */
    public void createDirectory(String path, String name);
    
    /**
     * Create a new layered file.
     * @param targetPath The path that is targeted.
     * @param parent The path to the parent directory.
     * @param name The name for the new file.
     */
    public void createLayeredFile(String targetPath, String parent, String name);
    
    /**
     * Create a layered directory.
     * @param targetPath The path that is targeted.
     * @param parent The parent directory.
     * @param name The name of the new directory.
     */
    public void createLayeredDirectory(String targetPath, String parent, String name);
    
    /**
     * Set a layered directory node to point at a different target.
     * @param path The path to the layered directory node.
     * @param target The new target.
     */
    public void retargetLayeredDirectory(String path, String target);
    
    /**
     * Create a new AVMStore.
     * @param name The name to give the new store.
     */
    public void createAVMStore(String name);
    
    /**
     * Create a new branch.
     * @param version The version to look under for the source node.
     * @param srcPath The path to the source node.
     * @param dstPath The path to the destination directory.
     * @param name The name of the new branch.
     */
    public void createBranch(int version, String srcPath, String dstPath, String name);
    
    /**
     * Remove a node.
     * @param parent The path to the parent directory.
     * @param name The name of the node to remove.
     */
    public void removeNode(String parent, String name);
    
    /**
     * Rename a node.
     * @param srcParent The source directory path.
     * @param srcName The source node name.
     * @param dstParent The destination directory path.
     * @param dstName The destination name for the node.
     */
    public void rename(String srcParent, String srcName, String dstParent, String dstName);

    /**
     * Uncover a name in a layered directory.
     * @param dirPath The path to the directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name);

    /**
     * Get the latest version id of the given AVMStore.
     * @param storeName The name of the AVMStore.
     * @return The latest version id.
     */
    public int getLatestVersionID(String storeName);

    /**
     * Get the id of the latest extant snpashot.
     * @param storeName The name of the store.
     * @return The id.
     */
    public int getLatestSnapshotID(String storeName);
    
    /**
     * Snapshot an AVMStore.
     * @param store The name of the AVMStore to snapshot.
     * @return The version id of the new snapshot.
     */
    public int createSnapshot(String store, String label, String comment);
    
    /**
     * Get a List of all versions in a given store.
     * @param name The name of the store.
     * @return A List of VersionDescriptors.
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name);
    
    /**
     * Get AVMStore versions between given dates.
     * @param name The name of the store.
     * @param from The date from which (inclusive).
     * @param to The date to which (inclusive).
     * @return A List of VersionDescriptors.
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name, Date from, Date to);
    
    /**
     * Get a list of all AVM stores.
     * @return A List of AVMStoreDescriptors.
     */
    public List<AVMStoreDescriptor> getAVMStores();
    
    /**
     * Get the descriptor for a given AVMStore.
     * @param name The name of the store.
     * @return An AVMStoreDescriptor.
     */
    public AVMStoreDescriptor getAVMStore(String name);
    
    /**
     * Get the specified root of the specified store.
     * @param version The version number to fetch.
     * @param name The name of the store.
     * @return The AVMNodeDescriptor for the root.
     */
    public AVMNodeDescriptor getAVMStoreRoot(int version, String name);
    
    /**
     * Get a descriptor for the specified node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return An AVMNodeDescriptor.
     */
    public AVMNodeDescriptor lookup(int version, String path);
    
    /**
     * Get a descriptor for the specified node.
     * @param dir The descriptor for the directory node.
     * @param name The name of the node to lookup.
     * @return An AVMNodeDescriptor.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name);
    
    /**
     * Get the indirection path for a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The indirection path/target.
     */
    public String getIndirectionPath(int version, String path);
    
    /**
     * Purge an AVMStore.
     * @param name The name of the store to purge.
     */
    public void purgeAVMStore(String name);
    
    /**
     * Purge a given version from a given store.
     * @param version The version id.
     * @param name The name of the store.
     */
    public void purgeVersion(int version, String name);
    
    /**
     * Turn a directory into a primary indirection node.
     * @param path The path to the directory.
     */
    public void makePrimary(String path);
    
    /**
     * Get a list of ancestors of a node.
     * @param desc The descriptor of the node whose history is to be fetched.
     * @param count The maximum number of ancestors that will be returned.
     * @return A List of descriptors for ancestors starting most recent first.
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count);
    
    /**
     * Turn on or off a directory's opacity.
     * @param path The path to the directory.
     * @param opacity Whether the directory should be opaque or not.
     */
    public void setOpacity(String path, boolean opacity);
    
    /**
     * Get the most recent common ancestor of two nodes.
     * @param left One node.
     * @param right The other node.
     * @return The common ancestor.
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left, AVMNodeDescriptor right);
    
    /**
     * Get layering information about a path.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A LayeringDescriptor.
     */
    public LayeringDescriptor getLayeringInfo(int version, String path);
    
    /**
     * Set a property on a node.
     * @param path The path to the node.
     * @param name The name of the property.
     * @param value The value to give the property.
     */
    public void setNodeProperty(String path, QName name, PropertyValue value);
    
    /**
     * Set a group of properties on a node.
     * @param path The path to the node.
     * @param properties A Map of QNames to PropertyValues to set.
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties);
    
    /**
     * Get the value of a node property.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The name of the property.
     * @return A PropertyValue.
     */
    public PropertyValue getNodeProperty(int version, String path, QName name);
    
    /**
     * Get all properties of a node.
     * @param version The version.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path);
    
    /**
     * Delete a property from a node.
     * @param path The path to the node.
     * @param name The name of the property.
     */
    public void deleteNodeProperty(String path, QName name);
    
    /**
     * Delete all properties from a node.
     * @param path The path to the node.
     */
    public void deleteNodeProperties(String path);
    
    /**
     * Set a property on a store.
     * @param store The name of the store.
     * @param name The name of the property to set.
     * @param value The value of the property to set.
     */
    public void setStoreProperty(String store, QName name, PropertyValue value);
    
    /**
     * Set a group of properties on a store.
     * @param store The name of the store.
     * @param props A Map of QNames to PropertyValues to set.
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props);
    
    /**
     * Get a property from a store.
     * @param store The name of the store.
     * @param name The name of the property.
     * @return A PropertyValue.
     */
    public PropertyValue getStoreProperty(String store, QName name);
    
    /**
     * Query a store for keys that match a pattern.
     * @param store The store name.
     * @param keyPattern The sql 'like' pattern.
     * @return A Map of keys to values.
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store, QName keyPattern);
    
    /**
     * Query all stores for keys that match a pattern.
     * @param keyPattern The sql 'like' pattern.
     * @return A Map of store names to Maps of matching keys to values.
     */
    public Map<String, Map<QName, PropertyValue>> queryStoresPropertyKey(QName keyPattern);
    
    /**
     * Get all the properties on a store.
     * @param store The name of the store.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getStoreProperties(String store);
    
    /**
     * Delete a property from a store.
     * @param store The name of the store.
     * @param name The name of the property.
     */
    public void deleteStoreProperty(String store, QName name);
    
    /**
     * Rename a store.
     * @param sourceName The original name.
     * @param destName The new name.
     */
    public void renameStore(String sourceName, String destName);
}
