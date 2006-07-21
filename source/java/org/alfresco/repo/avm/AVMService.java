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
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * This is the service interface for the [Alfresco|Addled|Advanced|Aleatoric|Apotheosed|Awful] 
 * Versioning Model.  It specifies methods that are close in functionality to the underlying
 * implementation, and is intended as both a first class Alfresco service and an
 * aid in creating new implementations of existing services.
 * Paths are of the form storename:/foo/bar/baz
 * @author britt
 */
public interface AVMService
{
    /**
     * Get an InputStream for a file node.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return An InputStream for the designated file.
     */
    public InputStream getFileInputStream(int version, String path);
    
    /**
     * Get an input stream from a particular version of a file.
     * @param desc The node descriptor pointing at the node.
     * @return The InputStream.
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc);

    /**
     * Get an output stream to a file node.  The file must already exist.
     * @param path The simple absolute path to the file node.
     */
    public OutputStream getFileOutputStream(String path);
    
    /**
     * Get a random access file to the given path.
     * @param version The version to find.
     * @param path The path to the file.
     * @param access The access mode for RandomAccessFile.
     * @return A RandomAccessFile
     */
    public RandomAccessFile getRandomAccess(int version, String path, String access);
    
    /**
     * Get a listing of a Folder by name.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return A List of FolderEntrys.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path);
    
    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir);
    
    /**
     * Create a new File. Fails if the file already exists.
     * @param path The simple absolute path to the parent.
     * @param name The name to give the file.
     */
    public OutputStream createFile(String path, String name);
    
    /**
     * Create a new File. Guarantees that the entire contents of the
     * input stream will be loaded atomically.
     * @param path The path to the parent directory.
     * @param name The name for the new file. 
     * @param in An input stream with data for the file.
     */
    public void createFile(String path, String name, InputStream in);
    
    /**
     * Create a new directory.
     * @param path The simple absolute path to the parent.
     * @param name The name to give the folder.
     */
    public void createDirectory(String path, String name);
    
    /**
     * Create a new layered file.
     * @param srcPath The simple absolute path that the new file will point at.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new file.
     */
    public void createLayeredFile(String srcPath, String parent, String name);
    
    /**
     * Create a new layered directory.
     * @param srcPath The simple absolute path that the new folder will point at.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new folder.
     */
    public void createLayeredDirectory(String srcPath, String parent, String name);

    /**
     * Retarget a layered directory.
     * @param path Path to the layered directory.
     * @param target The new target to aim at.
     */
    public void retargetLayeredDirectory(String path, String target);
    
    /**
     * Create a new AVMStore.  All AVMStores are top level in a hierarchical
     * sense.
     * @param name The name to give the AVMStore.
     */
    public void createAVMStore(String name);
    
    /**
     * Create a branch from a given version and path.
     * @param version The version number from which to make the branch.
     * @param srcPath The path to the node to branch from.
     * @param dstPath The path to the directory to contain the 
     * new branch.
     * @param name The name to give the new branch.
     */
    public void createBranch(int version, String srcPath, String dstPath, String name);
    
    /**
     * Remove a child from its parent.
     * @param parent The simple absolute path to the parent directory. 
     * @param name The name of the child to remove.
     */
    public void removeNode(String parent, String name);
    
    /**
     * Rename a node.
     * @param srcParent The simple absolute path to the parent folder.
     * @param srcName The name of the node in the src Folder.
     * @param dstParent The simple absolute path to the destination folder.
     * @param dstName The name that the node will have in the destination folder.
     */
    public void rename(String srcParent, String srcName, String dstParent, String dstName);
    
    /**
     * Uncover a name in a layered directory.  That is, if the layered
     * directory has a deleted entry of the given name remove that
     * name from the deleted list.
     * @param dirPath The path to the layered directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name);

    /**
     * Get the latest version id of the AVMStore.
     * @param storeName The name of the AVMStore.
     * @return The latest version id of the AVMStore.
     */
    public int getLatestVersionID(String storeName);
    
    /**
     * Snapshot the given AVMStores.  When this is called everything that has been added,
     * deleted, or modified since the last time this function was called, is marked 
     * as needing to be copied, so that further modifications will trigger copy on write
     * semantics.
     * @param stores The names of the AVMStores to snapshot.
     */
    public List<Integer> createSnapshot(List<String> repositories);
    
    /**
     * Snapshot the given AVMStore.
     * @param store The name of the AVMStore to snapshot.
     */
    public int createSnapshot(String store);
    
    /**
     * Get the set of versions in an AVMStore
     * @param name The name of the AVMStore.
     * @return A Set of version IDs
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name);
    
    /**
     * Get AVMStore version descriptors by creation date. Either from or
     * to can be null but not both.
     * @param name The name of the AVMStore.
     * @param from Earliest date of version to include.
     * @param to Latest date of version to include.
     * @return The Set of version IDs that match.
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
     */
    public AVMStoreDescriptor getAVMStore(String name);
    
    /**
     * Get the specified root of an AVMStore.
     * @param version The version to look up.
     * @param name The name of the AVMStore.
     * @return A descriptor for the specified root.
     */
    public AVMNodeDescriptor getAVMStoreRoot(int version, String name);
        
    /**
     * Lookup a node by version ids and path.
     * @param version The version id to look under.
     * @param path The simple absolute path to the parent directory.
     * @return An AVMNodeDescriptor.
     */
    public AVMNodeDescriptor lookup(int version, String path);
    
    /**
     * Lookup a node from a directory node.
     * @param dir The descriptor for the directory node.
     * @param name The name to lookup.
     * @return The descriptor for the child.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name);
    
    /**
     * Get the indirection path for a layered file or directory.
     * @param version The version number to get.
     * @param path The path to the node of interest.
     * @return The indirection path.
     */
    public String getIndirectionPath(int version, String path);
    
    /**
     * Purge an AVMStore.  This is a complete wipe of an AVMStore.
     * @param name The name of the AVMStore.
     */
    public void purgeAVMStore(String name);
    
    /**
     * Purge a version from an AVMStore.  Deletes everything that lives in
     * the given version only.
     * @param version The version to purge.
     * @param name The name of the AVMStore from which to purge it.
     */
    public void purgeVersion(int version, String name);
    
    /**
     * Make a directory into a primary indirection node.
     * @param path The full path.
     */
    public void makePrimary(String path);
    
    /**
     * Get a list of all the ancestor versions of a node.
     * @param desc The version of a node to find ancestors for.
     * @param count How many. -1 means all.
     * @return A List of ancestors starting with the most recent.
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count);
    
    /**
     * Set the opacity of a layered directory.  An opaque layered directory
     * hides the contents of its indirection.
     * @param path The path to the layered directory.
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
     */
    public AVMNodeDescriptor getCommonAncestor(AVMNodeDescriptor left,
                                               AVMNodeDescriptor right);
    
    /**
     * Get layering information about a path.
     * @param version The version to look under.
     * @param path The full AVM path.
     * @return A LayeringDescriptor.
     */
    public LayeringDescriptor getLayeringInfo(int version, String path);
    
    /**
     * Set a property on a node.
     * @param path The path to the node to set the property on. 
     * @param name The QName of the property.
     * @param value The property to set.
     */
    public void setProperty(String path, QName name, PropertyValue value);
    
    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of properties to set.
     */
    public void setProperties(String path, Map<QName, PropertyValue> properties);
    
    /**
     * Get a property of a node by QName.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The QName.
     * @return The PropertyValue or null if it doesn't exist.
     */
    public PropertyValue getProperty(int version, String path, QName name);
    
    /**
     * Get all the properties associated with a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getProperties(int version, String path);
}
