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

package org.alfresco.service.cmr.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * This is the service interface for the [Alfresco|Addled|Advanced|Aleatoric|Apotheosed|Awful] 
 * Versioning Model. Paths are of the form storename:/foo/bar/baz. Since the AVM is
 * a versioning repository all references to AVM nodes are via a version id (implied for write
 * operations) an a path.  The special version id -1 refers to the latest read write version of 
 * a store.  All positive versions refer to read only snapshots of a store created
 * explicitly by a call to createSnapshot() or implicitly by certain other calls
 * in this interface or the AVMSyncService interface.  
 * @author britt
 */
public interface AVMService
{
    /**
     * Get an InputStream for a file node.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return An InputStream for the designated file.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public InputStream getFileInputStream(int version, String path);
    
    /**
     * Get an output stream to a file node.  The file must already exist.
     * @param path The simple absolute path to the file node.
     * @throws AVMNotFoundException    
     * @throws AVMWrongTypeException 
     */
    public OutputStream getFileOutputStream(String path);
    
    /**
     * Get a content reader from a file node.
     * @param version The version of the file.
     * @param path The path to the file.
     * @return A ContentReader.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentReader getContentReader(int version, String path);
    
    /**
     * Get a ContentWriter to a file node.
     * @param path The path to the file.
     * @return A ContentWriter.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentWriter createContentWriter(String path);   
    
    /**
     * Get a listing of a Folder by name.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path);

    /**
     * Get a listing of a Folder by name, with the option of seeing
     * Deleted Nodes.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException 
     * @throws AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path,
                                                                    boolean includeDeleted);
    
    /**
     * Get the listing of nodes contained directly in a directory. This is the
     * same as getDirectoryListing for PlainDirectories, but returns only those that
     * are directly contained in a layered directory.
     * @param version The version to look up.
     * @param path The full path to get listing for.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException 
     */
    public SortedMap<String, AVMNodeDescriptor> 
        getDirectoryListingDirect(int version, String path);
    
    /**
     * Get the listing of nodes contained directly in a directory. This is the
     * same as getDirectoryListing for PlainDirectories, but returns only those that
     * are directly contained in a layered directory. This has the option of
     * seeing Deleted Nodes.
     * @param version The version to look up.
     * @param path The full path to get listing for.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException 
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(int version, String path, boolean includeDeleted);

    /**
     * Get a directory listing as an Array of AVMNodeDescriptors.
     * @param version The version to look under.
     * @param path The path to the directory to be listed.
     * @param includeDeleted Whether to include ghosts.
     * @return An array of AVMNodeDescriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public AVMNodeDescriptor [] getDirectoryListingArray(int version, String path,
                                                         boolean includeDeleted);
    
    /**
     * Get a listing of all the directly contained children of a directory.
     * @param dir The directory descriptor.
     * @param includeDeleted Whether to include deleted children.
     * @return A Map of Strings to descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(AVMNodeDescriptor dir, boolean includeDeleted);
    
    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir);
    
    /**
     * Get a directory listing from a node descriptor, with the option of
     * seeing deleted nodes.
     * @param dir The directory node descriptor.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return A Map of names to node descriptors.
     * @throws AVMNotFoundException If the descriptor is stale.
     * @throws AVMWrongTypeException If the descriptor does not point at a directory.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir,
                                                                    boolean includeDeleted);
    
    /**
     * Get a directory listing as an Array of node descriptors.
     * @param dir The descriptor pointing at the directory to list.
     * @param includeDeleted Whether to show ghosts.
     * @return An array of AVMNodeDescriptors.
     * @throws AVMNotFoundException 
     * @throws AVMWrongTypeException
     */
    public AVMNodeDescriptor [] getDirectoryListingArray(AVMNodeDescriptor dir,
                                                         boolean includeDeleted);
    
    /**
     * Get the names of nodes that have been deleted in a directory.
     * @param version The version to look under.
     * @param path The path of the directory.
     * @return A List of names.
     * @throws AVMNotFoundException 
     * @throws AVMWrongTypeException 
     */
    public List<String> getDeleted(int version, String path);
    
    /**
     * Create a new File. Fails if the file already exists.
     * @param path The simple absolute path to the parent.
     * @param name The name to give the file.
     * @throws AVMNotFound 
     * @throws AVMExists
     * @throws AVMWrongType
     */
    public OutputStream createFile(String path, String name);
    
    /**
     * Create a new File. Guarantees that the entire contents of the
     * input stream will be loaded atomically.
     * @param path The path to the parent directory.
     * @param name The name for the new file. 
     * @param in An input stream with data for the file.
     * @throws AVMNotFound
     * @throws AVMExists
     * @throws AVMWrongType
     */
    public void createFile(String path, String name, InputStream in);
    
    /**
     * Create a new directory. The new directory will be a plain 
     * directory if it is created outside of a layer, it will be a 
     * layered directory if created within a layer.
     * @param path The simple absolute path to the parent.
     * @param name The name to give the folder.
     * @throws AVMNotFound
     * @throws AVMExists
     * @throws AVMWrongType
     */  
    public void createDirectory(String path, String name);
    
    /**
     * Create a new layered file.
     * @param targetPath The simple absolute path that the new file will point at.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new file.
     * @throws AVMNotFound
     * @throws AVMExists
     * @throws AVMWrongType
     */
    public void createLayeredFile(String targetPath, String parent, String name);
    
    /**
     * Create a new layered directory. In whatever context this is created, this
     * will be a layered directory that has a primary indirection.
     * @param targetPath The simple absolute path that the new folder will point at.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new folder.
     * @throws AVMNotFound
     * @throws AVMExists
     * @throws AVMWrongType
     */
    public void createLayeredDirectory(String targetPath, String parent, String name);

    /**
     * Retarget a layered directory. Change the path that a layered directory points
     * to.  This has the side effect of making the layered directory a primary indirection.
     * @param path Path to the layered directory.
     * @param target The new target to aim at.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void retargetLayeredDirectory(String path, String target);
    
    /**
     * Create a new AVMStore.  All AVMStores are top level in a hierarchical
     * sense.
     * @param name The name to give the AVMStore.
     * @throws AVMExistsException
     */
    public void createAVMStore(String name);
    
    /**
     * Create a branch from a given version and path.  As a side effect 
     * the store that contains the node that is being branched from is 
     * automatically snapshotted.
     * @param version The version number from which to make the branch.
     * @param srcPath The path to the node to branch from.
     * @param dstPath The path to the directory to contain the 
     * new branch.
     * @param name The name to give the new branch.
     * @throws AVMNotFoundException
     * @throws AVMExistsException
     * @throws AVMWrongTypeException
     */
    public void createBranch(int version, String srcPath, String dstPath, String name);
    
    /**
     * Remove a child from its parent. Caution this removes directories even
     * if they are not empty.
     * @param parent The simple absolute path to the parent directory. 
     * @param name The name of the child to remove.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void removeNode(String parent, String name);
    
    /**
     * Remove a node by its full path. Sugar coating on removeNode(parent, name).
     * @param path The full path to the node to remove.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void removeNode(String path);
    
    /**
     * Rename a node. There are a number of things to note about the
     * interaction of rename and layering. If you rename a layered directory
     * into a non layered context, the layered directory continues to point 
     * to the same place it did before it was renamed and automatically becomes
     * a primary indirection node.  If a plain directory is renamed into a layered
     * context it remains a plain, thus acting as an opaque node in its new 
     * layered home. Renaming a layered node into a layered node leaves the renamed 
     * node pointing to the same place it did before the rename and makes it automatically
     * a primary indirection node.
     * @param srcParent The simple absolute path to the parent folder.
     * @param srcName The name of the node in the src Folder.
     * @param dstParent The simple absolute path to the destination folder.
     * @param dstName The name that the node will have in the destination folder.
     * @throws AVMNotFoundException 
     * @throws AVMExistsException 
     */
    public void rename(String srcParent, String srcName, String dstParent, String dstName);
    
    /**
     * Uncover a name in a layered directory.  That is, if the layered
     * directory has a deleted entry of the given name remove that
     * name from the deleted list.
     * @param dirPath The path to the layered directory.
     * @param name The name to uncover.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void uncover(String dirPath, String name);

    /**
     * Make name in dirPath transparent to what was underneath it. That is, this
     * removes the offending node from its layered directory parent's direct ownership.
     * @param dirPath The path to the layered directory.
     * @param name The name of the item to flatten.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void flatten(String dirPath, String name);
    
    /**
     * Gets the id that the next snapshotted version of a store 
     * will have.
     * @param storeName The name of the AVMStore.
     * @return The next version id of the AVMStore.
     * @throws AVMNotFoundException
     */
    public int getNextVersionID(String storeName);
    
    /**
     * Get the latest snapshot id of a store. 
     * @param storeName The store name.
     * @return The id of the latest extant version of the store.
     * @throws AVMNotFoundException
     */
    public int getLatestSnapshotID(String storeName);
    
    /**
     * Snapshot the given AVMStores.  When this is called everything that has been added,
     * deleted, or modified since the last time this function was called, is marked 
     * as needing to be copied, so that further modifications will trigger copy on write
     * semantics.
     * @param stores The names of the AVMStores to snapshot.
     * @return A List of the version ids of the newly created snapshots.
     * @throws AVMNotFoundException If any of the stores do not exist.
     */
    // public List<Integer> createSnapshot(List<String> stores);
    
    /**
     * Snapshot the given AVMStore.
     * @param store The name of the AVMStore to snapshot.
     * @param tag The short description.
     * @param description The thick description.
     * @throws AVMNotFoundException
     */
    public int createSnapshot(String store, String tag, String description);
    
    /**
     * Get the set of versions in an AVMStore. Since the number of version 
     * that a store can contain can become large this call can be a resource
     * pig to the point of causing Out of Memory exceptions.
     * @param name The name of the AVMStore.
     * @return A Set of version descriptors.
     * @throws AVMNotFoundException
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name);
    
    /**
     * Get AVMStore version descriptors by creation date. Either from or
     * to can be null but not both.  If from is null then all versions before
     * to will be returned.  If to is null then all versions later than from
     * will be returned.
     * @param name The name of the AVMStore.
     * @param from Earliest date of version to include.
     * @param to Latest date of version to include.
     * @return The Set of version descriptors that match.
     * @throws AVMNotFoundException
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name, Date from, Date to);
    
    /**
     * Get the descriptors of all AVMStores. 
     * @return A List of all AVMStores.
     */
    public List<AVMStoreDescriptor> getAVMStores();

    /**
     * Get (and create if necessary) the system store. This store houses things
     * like workflow packages. The system store is intended to be a scratch store.
     * It isn't used internally at this time but may be in the future.
     * @return The descriptor.
     */
    public AVMStoreDescriptor getAVMSystemStore();
    
    /**
     * Get a descriptor for an AVMStore.
     * @param name The AVMStore's name.
     * @return A Descriptor, or null if not foun.
     */
    public AVMStoreDescriptor getAVMStore(String name);
    
    /**
     * Get the specified root of an AVMStore.
     * @param version The version to look up.
     * @param name The name of the AVMStore.
     * @return A descriptor for the specified root.
     * @throws AVMNotFoundException
     */
    public AVMNodeDescriptor getAVMStoreRoot(int version, String name);
        
    /**
     * Lookup a node by version ids and path.
     * @param version The version id to look under.
     * @param path The simple absolute path to the parent directory.
     * @return An AVMNodeDescriptor, or null if the node does not exist.
     */
    public AVMNodeDescriptor lookup(int version, String path);
    
    /**
     * Lookup a node by version ids and path, with the option of 
     * seeing Deleted Nodes.
     * @param version The version id to look under.
     * @param path The simple absolute path to the parent directory.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return An AVMNodeDescriptor, or null if the version does not exist.
     */
    public AVMNodeDescriptor lookup(int version, String path, boolean includeDeleted);
    
    /**
     * Lookup a node from a directory node.
     * @param dir The descriptor for the directory node.
     * @param name The name to lookup.
     * @return The descriptor for the child.
     * @throws AVMWrongTypeException If <code>dir</code> does not refer to a directory.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name);
    
    /**
     * Lookup a node from a directory node, with the option of seeing 
     * Deleted Nodes.
     * @param dir The descriptor for the directory node.
     * @param name The name to lookup.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return The descriptor for the child, null if the child doesn't exist.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name, boolean includeDeleted);
    
    /**
     * Get a list of all paths that a given node has.  This can be an expensive 
     * operation.
     * @param desc The node descriptor to get paths for.
     * @return A List of version, path Pairs.
     * @throws AVMNotFoundException
     */
    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc);
    
    /**
     * Get all paths that a given node has that are in the head version.
     * This can be an expensive operation but less so than getPaths().
     * @param desc The node descriptor to get paths for.
     * @return A List of version, path Pairs.
     * @throws AVMNotFoundException
     */
    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc);
    
    /**
     * Get all paths to a node starting at the HEAD version of a store.
     * This can be an expensive operation but less so than getHeadPaths().
     * @param desc The node descriptor.
     * @param store The store.
     * @return A List of all paths meeting the criteria.
     * @throws AVMNotFoundException
     */
    public List<Pair<Integer, String>> getPathsInStoreHead(AVMNodeDescriptor desc, String store);
    
    /**
     * Get the indirection path for a node in a layered context.
     * @param version The version number to get.
     * @param path The path to the node of interest.
     * @return The indirection path, or null if the path is not in a layered context.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public String getIndirectionPath(int version, String path);
    
    /**
     * Purge an AVMStore.  This is a complete wipe of an AVMStore.
     * @param name The name of the AVMStore.
     * @throws AVMNotFoundException 
     */
    public void purgeAVMStore(String name);
    
    /**
     * Purge a version from an AVMStore.  Deletes everything that lives in
     * the given version only.
     * @param version The version to purge.
     * @param name The name of the AVMStore from which to purge it.
     * @throws AVMNotFoundException If <code>name</code> or <code>version</code>
     * do not exist.
     */
    public void purgeVersion(int version, String name);
    
    /**
     * Make a directory into a primary indirection node.
     * @param path The full path.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void makePrimary(String path);
    
    /**
     * Get a list of up to count nodes in the history chain of a node.
     * @param desc The descriptor for a node to find ancestors for.
     * @param count How many. -1 means all.
     * @return A List of ancestors starting with the most recent.
     * @throws AVMNotFoundException
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count);
    
    /**
     * Set the opacity of a layered directory.  An opaque layered directory
     * hides the contents of its indirection.
     * @param path The path to the layered directory.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void setOpacity(String path, boolean opacity);
    
    /**
     * Get the common ancestor of two nodes if one exists.
     * @param left The first node.
     * @param right The second node.
     * @return The common ancestor. There are four possible results. Null means
     * that there is no common ancestor.  Left returned means that left is strictly
     * an ancestor of right.  Right returned means that right is strictly an
     * ancestor of left.  Any other non null return is the common ancestor and
     * indicates that left and right are in conflict.
     * @throws AVMNotFoundException 
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
                                               AVMNodeDescriptor right);
    
    /**
     * Get layering information about a path.
     * @param version The version to look under.
     * @param path The full AVM path.
     * @return A LayeringDescriptor.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public LayeringDescriptor getLayeringInfo(int version, String path);
    
    /**
     * Set a property on a node.
     * @param path The path to the node to set the property on. 
     * @param name The QName of the property.
     * @param value The property to set.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void setNodeProperty(String path, QName name, PropertyValue value);
    
    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of properties to set.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties);
    
    /**
     * Get a property of a node by QName.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The QName.
     * @return The PropertyValue or null if it doesn't exist.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public PropertyValue getNodeProperty(int version, String path, QName name);
    
    /**
     * Get all the properties associated with a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     * @throws AVMNotFoundException
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path);
    
    /**
     * Delete a property.
     * @param path The path to the node.
     * @param name The QName of the property to delete.
     * @throws AVMNotFoundException
     */
    public void deleteNodeProperty(String path, QName name);
    
    /**
     * Delete all the properties attached to an AVM node.
     * @param path The path to the node.
     * @throws AVMNotFoundException
     */
    public void deleteNodeProperties(String path);
    
    /**
     * Set a property on a store. If the property exists it will be overwritten.
     * @param store The store to set the property on.
     * @param name The name of the property.
     * @param value The value of the property.
     * @throws AVMNotFoundException
     */
    public void setStoreProperty(String store, QName name, PropertyValue value);
    
    /**
     * Set a group of properties on a store. Existing properties will be overwritten.
     * @param store The name of the store.
     * @param props A Map of the properties to set.
     * @throws AVMNotFoundException
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props);
    
    /**
     * Get a property from a store.
     * @param store The name of the store.
     * @param name The name of the property.
     * @return A PropertyValue or null if non-existent.
     * @throws AVMNotFoundException
     */
    public PropertyValue getStoreProperty(String store, QName name);
    
    /**
     * Get all the properties associated with a store.
     * @param store The name of the store.
     * @return A Map of the stores properties.
     * @throws AVMNotFoundException
     */
    public Map<QName, PropertyValue> getStoreProperties(String store);

    /**
     * Queries a given store for properties with keys that match a given pattern. 
     * @param store The name of the store.
     * @param keyPattern The sql 'like' pattern, inserted into a QName.
     * @return A Map of the matching key value pairs.
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store, QName keyPattern);
    
    /**
     * Queries all AVM stores for properties with keys that matcha given pattern. 
     * @param keyPattern The sql 'like' pattern, inserted into a QName.
     * @return A Map of store names to Maps of property key value pairs that match
     * the pattern.
     */
    public Map<String, Map<QName, PropertyValue>>
        queryStoresPropertyKeys(QName keyPattern);
    
    /**
     * Delete a property on a store by name.
     * @param store The name of the store.
     * @param name The name of the property to delete.
     * @throws AVMNotFoundException
     */
    public void deleteStoreProperty(String store, QName name);
    
    /**
     * Get the ContentData for a node in a read context. Only applies to a file.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The ContentData object.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentData getContentDataForRead(int version, String path);
    
    /**
     * Get the ContentData for a node in a write context.
     * @param path The path to the node.
     * @return The ContentData object.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentData getContentDataForWrite(String path);
    
    /**
     * Set the content data on a file. 
     * @param path The path to the file.
     * @param data The ContentData to set.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void setContentData(String path, ContentData data);
    
    /**
     * Set all metadata on a node from another node. Aspects, properties, ACLs.
     * @param path The path to the node to set.
     * @param from The descriptor for the node to get metadata from.
     * @throws AVMNotFoundException
     */
    public void setMetaDataFrom(String path, AVMNodeDescriptor from);
    
    /**
     * Add an aspect to an AVM node.
     * @param path The path to the node.
     * @param aspectName The QName of the aspect.
     * @throws AVMNotFoundException
     * @throws AVMExistsException
     */
    public void addAspect(String path, QName aspectName);
    
    /**
     * Get all the aspects on an AVM node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of the QNames of the aspects.
     * @throws AVMNotFoundException   
     */
    public List<QName> getAspects(int version, String path);
    
    /**
     * Remove an aspect and its properties from a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     * @throws AVMNotFoundException
     */
    public void removeAspect(String path, QName aspectName);
    
    /**
     * Does a node have a particular aspect.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param aspectName The aspect name to check.
     * @return Whether the given node has the given aspect.
     * @throws AVMNotFoundException
     */
    public boolean hasAspect(int version, String path, QName aspectName);
    
    /**
     * This inserts a node into a parent directly. This is not something
     * one generally wants to do as the results are often not what you think.
     * Used internally by the AVMSyncService.update() method. This may disappear
     * from the public interface.
     * @param parentPath The path to the parent directory.
     * @param name The name to give the node.
     * @param toLink A descriptor for the node to insert.
     * @throws AVMNotFoundException
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink);
    
    /**
     * Force copy on write of a path. Forces a copy on write event
     * on the given node.  Not usually needed, and may disappear from the
     * public interface.
     * @param path The path to force.
     * @throws AVMNotFoundException
     */
    public AVMNodeDescriptor forceCopy(String path);
    
    /**
     * Copy (possibly recursively) the source into the destination
     * directory. This copies all node properties, acls, and aspects.
     * @param srcVersion The version of the source.
     * @param srcPath The path to the source.
     * @param dstPath The destination directory.
     * @param name The name to give the destination.
     * @throws AVMNotFoundException
     */
    public void copy(int srcVersion, String srcPath, String dstPath, String name);
}
