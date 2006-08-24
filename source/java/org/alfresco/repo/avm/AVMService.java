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

package org.alfresco.repo.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.QName;

/**
 * This is the service interface for the [Alfresco|Addled|Advanced|Aleatoric|Apotheosed|Awful] 
 * Versioning Model.  It specifies methods that are close in functionality to the underlying
 * implementation, and is intended as both a first class Alfresco service and an
 * aid in creating new implementations of existing services.
 * Paths are of the form storename:/foo/bar/baz.  
 * @author britt
 */
public interface AVMService
{
    /**
     * Get an InputStream for a file node.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return An InputStream for the designated file.
     * @throws AVMNotFoundException If <code>path</code> is not found.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * component that is not a Directory of if it does not point at a file.
     */
    public InputStream getFileInputStream(int version, String path);
    
    /**
     * Get an output stream to a file node.  The file must already exist.
     * @param path The simple absolute path to the file node.
     * @throws AVMNotFoundException If <code>path</code> is not found.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * component that is not a directory, or if it is not pointing to a file.
     */
    public OutputStream getFileOutputStream(String path);
    
    /**
     * Get a listing of a Folder by name.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException If <code>path</code> is not found.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * component that is not a directory, or if <code>path</code> is not pointing
     * at a directory.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path);
    
    /**
     * Get the listing of nodes contained directly in a directory. This is the
     * same as getDirectoryListing for PlainDirectories, but returns only those that
     * are directly contained in a layered directory.
     * @param version The version to look up.
     * @param path The full path to get listing for.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains any non-directory
     * elements.
     */
    public SortedMap<String, AVMNodeDescriptor> 
        getDirectoryListingDirect(int version, String path);

    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     * @throws AVMNotFoundException If the descriptor is stale.
     * @throws AVMWrongTypeException If the descriptor does not point at
     * a directory.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir);
    
    /**
     * Get the names of nodes that have been deleted in a directory.
     * @param version The version to look under.
     * @param path The path of the directory.
     * @return A List of names.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains any elements
     * that are not directories.
     */
    public List<String> getDeleted(int version, String path);
    
    /**
     * Create a new File. Fails if the file already exists.
     * @param path The simple absolute path to the parent.
     * @param name The name to give the file.
     * @throws AVMNotFound If <code>path</code> is non existent.
     * @throws AVMExists If <code>name</code> already exists.
     * @throws AVMWrongType If some component of <code>path</code> is not
     * a directory.
     */
    public OutputStream createFile(String path, String name);
    
    /**
     * Create a new File. Guarantees that the entire contents of the
     * input stream will be loaded atomically.
     * @param path The path to the parent directory.
     * @param name The name for the new file. 
     * @param in An input stream with data for the file.
     * @throws AVMNotFound If <code>path</code> is non existent.
     * @throws AVMExists If <code>name</code> already exists.
     * @throws AVMWrongType If some component of <code>path</code> is not
     * a directory.
     */
    public void createFile(String path, String name, InputStream in);
    
    /**
     * Create a new directory.
     * @param path The simple absolute path to the parent.
     * @param name The name to give the folder.
     * @throws AVMNotFound If <code>path</code> is non existent.
     * @throws AVMExists If <code>name</code> already exists.
     * @throws AVMWrongType If some component of <code>path</code> is not
     * a directory.
     */
    public void createDirectory(String path, String name);
    
    /**
     * Create a new layered file.
     * @param targetPath The simple absolute path that the new file will point at.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new file.
     * @throws AVMNotFound If <code>parent</code> is non existent.
     * @throws AVMExists If <code>name</code> already exists.
     * @throws AVMWrongType If some component of <code>parent</code> is not
     * a directory.
     */
    public void createLayeredFile(String targetPath, String parent, String name);
    
    /**
     * Create a new layered directory.
     * @param targetPath The simple absolute path that the new folder will point at.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new folder.
     * @throws AVMNotFound If <code>parent</code> is non existent.
     * @throws AVMExists If <code>name</code> already exists.
     * @throws AVMWrongType If some component of <code>parent</code> is not
     * a directory.
     */
    public void createLayeredDirectory(String targetPath, String parent, String name);

    /**
     * Retarget a layered directory.
     * @param path Path to the layered directory.
     * @param target The new target to aim at.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * component that is not a directory or if it does not point at a retargetable 
     * directory.
     */
    public void retargetLayeredDirectory(String path, String target);
    
    /**
     * Create a new AVMStore.  All AVMStores are top level in a hierarchical
     * sense.
     * @param name The name to give the AVMStore.
     * @throws AVMExistsException If <code>name</code> is an already existing store.
     */
    public void createAVMStore(String name);
    
    /**
     * Create a branch from a given version and path.
     * @param version The version number from which to make the branch.
     * @param srcPath The path to the node to branch from.
     * @param dstPath The path to the directory to contain the 
     * new branch.
     * @param name The name to give the new branch.
     * @throws AVMNotFoundException If either <code>srcPath</code> or
     * <code>dstPath</code> does not exist.
     * @throws AVMExistsException If <code>name</code> already exists.
     * @throws AVMWrongTypeException If <code>srcPath</code> or <code>dstPath</code>
     * contains a non-terminal element that is not a directory.
     */
    public void createBranch(int version, String srcPath, String dstPath, String name);
    
    /**
     * Remove a child from its parent.
     * @param parent The simple absolute path to the parent directory. 
     * @param name The name of the child to remove.
     * @throws AVMNotFoundException If <code>parent</code> or <code>name</code>
     * does not exist.
     * @throws AVMWrongTypeException If <code>parent</code> contains a non-terminal
     * element that is not a directory.
     */
    public void removeNode(String parent, String name);
    
    /**
     * Rename a node.
     * @param srcParent The simple absolute path to the parent folder.
     * @param srcName The name of the node in the src Folder.
     * @param dstParent The simple absolute path to the destination folder.
     * @param dstName The name that the node will have in the destination folder.
     * @throws AVMNotFoundException If <code>srcParent</code>, 
     * <code>srcName</code>, or <code>dstParent</code> do not exist.
     * @throws AVMExistsException If <code>dstName</code> already exists.
     * @throws AVMWrongTypeException If <code>srcParent</code> or
     * <code>dstParent</code> contain non-terminal elements that are not directories
     * or if either do not point at directories.
     */
    public void rename(String srcParent, String srcName, String dstParent, String dstName);
    
    /**
     * Uncover a name in a layered directory.  That is, if the layered
     * directory has a deleted entry of the given name remove that
     * name from the deleted list.
     * @param dirPath The path to the layered directory.
     * @param name The name to uncover.
     * @throws AVMNotFoundException If <code>dirPath</code> does not exist.
     * @throws AVMWrongTypeExceptiont If <code>dirPath</code> contains a non-terminal
     * element that is not a directory.
     */
    public void uncover(String dirPath, String name);

    /**
     * Get the latest version id of the AVMStore.
     * @param storeName The name of the AVMStore.
     * @return The latest version id of the AVMStore.
     * @throws AVMNotFoundException If <code>storeName</code> does not exist.
     */
    public int getLatestVersionID(String storeName);
    
    /**
     * Snapshot the given AVMStores.  When this is called everything that has been added,
     * deleted, or modified since the last time this function was called, is marked 
     * as needing to be copied, so that further modifications will trigger copy on write
     * semantics.
     * @param stores The names of the AVMStores to snapshot.
     * @return A List of the version ids of the newly created snapshots.
     * @throws AVMNotFoundException If any of the stores do not exist.
     */
    public List<Integer> createSnapshot(List<String> stores);
    
    /**
     * Snapshot the given AVMStore.
     * @param store The name of the AVMStore to snapshot.
     * @throws AVMNotFoundException If <code>store</code> does not exist.
     */
    public int createSnapshot(String store);
    
    /**
     * Get the set of versions in an AVMStore
     * @param name The name of the AVMStore.
     * @return A Set of version IDs
     * @throws AVMNotFoundException If <code>name</code> does not exist.
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name);
    
    /**
     * Get AVMStore version descriptors by creation date. Either from or
     * to can be null but not both.
     * @param name The name of the AVMStore.
     * @param from Earliest date of version to include.
     * @param to Latest date of version to include.
     * @return The Set of version IDs that match.
     * @throws AVMNotFoundException If <code>name</code> does not exist.
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name, Date from, Date to);
    
    /**
     * Get the descriptors of all AVMStores. 
     * @return A List of all AVMStores.
     */
    public List<AVMStoreDescriptor> getAVMStores();

    /**
     * Get a descriptor for an AVMStore.
     * @param name The AVMStore's name.
     * @return A Descriptor.
     * @throws AVMNotFoundException If <code>name</code> does not exist.
     */
    public AVMStoreDescriptor getAVMStore(String name);
    
    /**
     * Get the specified root of an AVMStore.
     * @param version The version to look up.
     * @param name The name of the AVMStore.
     * @return A descriptor for the specified root.
     * @throws AVMNotFoundException If <code>name</code> does not exist or
     * if <code>version</code> does not exist.
     */
    public AVMNodeDescriptor getAVMStoreRoot(int version, String name);
        
    /**
     * Lookup a node by version ids and path.
     * @param version The version id to look under.
     * @param path The simple absolute path to the parent directory.
     * @return An AVMNodeDescriptor.
     * @throws AVMNotFoundException If <code>path</code> does not exist or
     * if <code>version</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal 
     * element that is not a directory.
     */
    public AVMNodeDescriptor lookup(int version, String path);
    
    /**
     * Lookup a node from a directory node.
     * @param dir The descriptor for the directory node.
     * @param name The name to lookup.
     * @return The descriptor for the child.
     * @throws AVMNotFoundException If <code>name</code> does not exist or
     * if <code>dir</code> is dangling.
     * @throws AVMWrongTypeException If <code>dir</code> does not refer to a directory.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name);
    
    /**
     * Get the indirection path for a layered file or directory.
     * @param version The version number to get.
     * @param path The path to the node of interest.
     * @return The indirection path.
     * @throws AVMNotFoundException If <code>path</code> does not exist or
     * if <code>version</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory or if it does not refer to a layered
     * node.
     */
    public String getIndirectionPath(int version, String path);
    
    /**
     * Purge an AVMStore.  This is a complete wipe of an AVMStore.
     * @param name The name of the AVMStore.
     * @throws AVMNotFoundException If <code>name</code> does not exist.
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
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory or if it refers to a node that can't be made
     * primary.
     */
    public void makePrimary(String path);
    
    /**
     * Get a list of all the ancestor versions of a node.
     * @param desc The version of a node to find ancestors for.
     * @param count How many. -1 means all.
     * @return A List of ancestors starting with the most recent.
     * @throws AVMNotFoundException If <code>desc</code> is dangling.
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count);
    
    /**
     * Set the opacity of a layered directory.  An opaque layered directory
     * hides the contents of its indirection.
     * @param path The path to the layered directory.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory or if it refers to a node that cannot have
     * its opacity set.
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
     * @throws AVMNotFoundException If <code>left</code> or <code>right</code>
     * do not exist.
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
                                               AVMNodeDescriptor right);
    
    /**
     * Get layering information about a path.
     * @param version The version to look under.
     * @param path The full AVM path.
     * @return A LayeringDescriptor.
     * @throws AVMNotFoundException If <code>path</code> or <code>version</code>
     * do not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory.
     */
    public LayeringDescriptor getLayeringInfo(int version, String path);
    
    /**
     * Set a property on a node.
     * @param path The path to the node to set the property on. 
     * @param name The QName of the property.
     * @param value The property to set.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory.
     */
    public void setNodeProperty(String path, QName name, PropertyValue value);
    
    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of properties to set.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory.
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties);
    
    /**
     * Get a property of a node by QName.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The QName.
     * @return The PropertyValue or null if it doesn't exist.
     * @throws AVMNotFoundException If <code>path</code> or <code>version</code>
     * do not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory.
     */
    public PropertyValue getNodeProperty(int version, String path, QName name);
    
    /**
     * Get all the properties associated with a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     * @throws AVMNotFoundException If <code>path</code> or <code>version</code>
     * do not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory.
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path);
    
    /**
     * Delete a property.
     * @param path The path to the node.
     * @param name The QName of the property to delete.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * element that is not a directory.
     */
    public void deleteNodeProperty(String path, QName name);
    
    /**
     * Delete all the properties attached to an AVM node.
     * @param path The path to the node.
     */
    public void deleteNodeProperties(String path);
    
    /**
     * Set a property on a store. If the property exists it will be overwritten.
     * @param store The store to set the property on.
     * @param name The name of the property.
     * @param value The value of the property.
     * @throws AVMNotFoundException If <code>store</code>
     * does not exist.
     */
    public void setStoreProperty(String store, QName name, PropertyValue value);
    
    /**
     * Set a group of properties on a store. Existing properties will be overwritten.
     * @param store The name of the store.
     * @param props A Map of the properties to set.
     * @throws AVMNotFoundException If <code>store</code>
     * does not exist.
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props);
    
    /**
     * Get a property from a store.
     * @param store The name of the store.
     * @param name The name of the property.
     * @return A PropertyValue or null if non-existent.
     * @throws AVMNotFoundException If <code>store</code>
     * does not exist.
     */
    public PropertyValue getStoreProperty(String store, QName name);
    
    /**
     * Get all the properties associated with a store.
     * @param store The name of the store.
     * @return A Map of the stores properties.
     * @throws AVMNotFoundException If <code>store</code>
     * does not exist.
     */
    public Map<QName, PropertyValue> getStoreProperties(String store);
    
    /**
     * Delete a property on a store by name.
     * @param store The name of the store.
     * @param name The name of the property to delete.
     * @throws AVMNotFoundException If <code>store</code>
     * does not exist.
     */
    public void deleteStoreProperty(String store, QName name);
    
    /**
     * Get the ContentData for a node. Only applies to a file.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The ContentData object.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> does not
     * point to a file.
     */
    public ContentData getContentDataForRead(int version, String path);
    
    /**
     * Get the ContentData for a node.
     * @param path The path to the node.
     * @return The ContentData object.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> does not point
     * to a file.
     */
    public ContentData getContentDataForWrite(String path);
    
    /**
     * Set the content data on a file. 
     * @param path The path to the file.
     * @param data The ContentData to set.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> does not point
     * to a file.
     */
    public void setContentData(String path, ContentData data);
    
    /**
     * Add an aspect to an AVM node.
     * @param path The path to the node.
     * @param aspectName The QName of the aspect.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMExistsException If the aspect already exists.
     */
    public void addAspect(String path, QName aspectName);
    
    /**
     * Get all the aspects on an AVM node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of the QNames of the aspects.
     */
    public List<QName> getAspects(int version, String path);
    
    /**
     * Remove an aspect and its properties from a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     */
    public void removeAspect(String path, QName aspectName);
    
    /**
     * Does a node have a particular aspect.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param aspectName The aspect name to check.
     * @return Whether the given node has the given aspect.
     */
    public boolean hasAspect(int version, String path, QName aspectName);
}
