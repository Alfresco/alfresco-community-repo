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

package org.alfresco.service.cmr.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Service interface for the Alfresco Versioning Model (AVM).<br>
 * For the remote API, see: {@link org.alfresco.service.cmr.remote.AVMRemote AVMRemote}.
 * <p>
 *
 * Because the AVM is a
 * <a href="http://wiki.alfresco.com/wiki/Versioned_Directories">
 * versioning repository</a>, 
 * fully explicit references to the nodes within it consist of an 
 * absolute AVM path, and a version ID.  Absolute AVM paths are of
 * the form:&nbsp;&nbsp;<em>&lt;store-name&gt;</em>:<em>&lt;store-relative-path&gt;</em>.  
 * <p>
 * For example:&nbsp;&nbsp;<code>mystore:/www/avm_webapps/ROOT/x/y/z.html</code>
 * <p>
 * Within AVMService, whenever an API takes a <em>path</em> 
 * and a <em>name</em>, the <em>path</em> is the parent 
 * directory in which the <em>name</em> appears.
 * Whenever just a <em>path</em> is needed by an API, it is an absolute 
 * path to a file or directory.
 * <p>
 * The special version ID <strong><code> -1 </code></strong> (negative one)
 * refers to the latest read/write version at the given absolute AVM path.  
 * Non-negative version ID values refer to read-only snapshots of a store. 
 * For this reason, the version ID <strong><code> -1 </code></strong> 
 * is implicit for all write operations.  Sometimes, 
 * <strong><code> -1 </code></strong> is referred to as the
 * <strong><code>HEAD</code></strong> version (a term that should be
 * already be familiar to users of other versioning systems like
 * <a href="http://en.wikipedia.org/wiki/Concurrent_Versions_System">CVS</a>
 * and <a href="http://en.wikipedia.org/wiki/Subversion_(software)">SVN</a>).
 * <p>
 * Snapshots can be created explicitly via a call to 
 * {@link #createSnapshot(String store, String tag, String description) createSnapshot},
 * or implicitly by various APIs in this interface and in
 * {@link org.alfresco.service.cmr.avmsync.AVMSyncService AVMSyncService}.
 * Although a new snapshot of a store will have version a ID one higher 
 * than the previous snapshot in that store, the history of an AMV store
 * does not necessarily contain a continuous range of version ID values,
 * because {@link #purgeVersion(int version, String name) purgeVersion}
 * may have been called.  Regardless of whether 
 * {@link #purgeVersion(int version, String name) purgeVersion}
 * has been called, the AVM never recycles version ID values
 * within a store.
 *
 * @author britt
 */
public interface AVMService
{
    /**
     * Get an InputStream for reading the contents of a 
     * file identified by its version ID and AVM path.
     * This method can be used for either plain or layered files.
     *
     * @param  version The version ID to look in.
     * @param  path    The absolute path to the file.
     * @return         An InputStream for the designated file.
     * @throws         AVMNotFoundException
     * @throws         AVMWrongTypeException
     */
    public InputStream getFileInputStream(int version, String path);
    
    /**
     * Get an InputStream for reading the contents of a 
     * file node identified by its descriptor.
     * This method can be used for either plain or layered files.
     *
     * @param desc The descriptor.
     * @return An InputStream.
     * @throws AVMNotFoundException
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc);
    
    /**
     * Get an output stream to write to a file 
     * identified by an AVM path.  This file must already exist.  
     * This method can be used for either plain or layered files.
     *
     * To create a plain file, see:
     * {@link #createFile(String path, String name) createFile}.
     * To create a layered file, see:
     * {@link #createLayeredFile(String targetPath, String parent, String name) createLayeredFile}.
     *
     * @param  path The absolute path to the file.
     * @throws      AVMNotFoundException    
     * @throws      AVMWrongTypeException 
     */
    public OutputStream getFileOutputStream(String path);


    /**
     * Get a non-recursive listing of a directory
     * identified by its version ID and path.
     * If <code>path</code> does not refer to a directory
     * node, <code>AVMWrongTypeException</code> is thrown.
     * 
     * @param  version The version ID to look in.
     * @param  path    The absolute AVM path to the file.
     * @return         A Map of names to descriptors.
     * @throws         AVMNotFoundException
     * @throws         AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path);
    
    /**
     * Get a non-recursive listing of nodes contained by a directory identified
     * by its version ID and path, but exclude all nodes that are only 
     * contained "indirectly" via layering.
     * <p>
     * If this function is called on a "plain" (non-layered) directory,
     * it is equivalent to 
     * {@link #getDirectoryListing(int version, String path) getDirectoryListing}.
     *
     * @param version The version to look up.
     * @param path The full path to get listing for.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException 
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(int version, String path);

    /**
     * Get a non-recursive directory listing of a directory node
     * identified by a node descriptor.
     *
     * @param dir The directory node descriptor.
     * @return A sorted Map of names to node descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir);
    
    /**
     * Get a non-recursive directory listing of a directory node
     * identified by a node descriptor. Only return children matching the given pattern.
     * '*' match any number of characters - equivalent to SQL '%'
     * '?' match a single character - equivalent to SQL '_'
     * '\' escape - valid sequences "\\", "\*" and "\?" 
     *
     * @param dir The directory node descriptor.
     * @param childPattern
     * @return A sorted Map of names to node descriptors.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, String childNamePattern);

    /**
     * Non-recursively get the names of nodes that have been deleted in 
     * a directory identified by a version ID and a path
     *
     * @param version The version to look under.
     * @param path    The path of the directory.
     * @return        A List of names.
     * @throws        AVMNotFoundException 
     * @throws        AVMWrongTypeException 
     */
    public List<String> getDeleted(int version, String path);
    

    /**
     * Create a new "plain" (non-layered) file within a path. 
     * This function fails if the file already exists,
     * or if the directory identified by <code>path</code>
     * does not exist.
     *
     * @param path The path of the directory containing the created file.
     * @param name The name of the new file
     * @throws     AVMNotFound 
     * @throws     AVMExists
     * @throws     AVMWrongType
     * @return An opaque handle to a server side output stream.
     */
    public OutputStream createFile(String path, String name);
    
    /**
     * Create a new directory. 
     * If <code>path</code> is within a layer, the new directory will be a layered directory;
     * otherwise, the new directory will be a plain directory.
     *
     * @param path The simple absolute path to the parent.
     * @param name The name to give the directory.
     * @throws     AVMNotFound
     * @throws     AVMExists
     * @throws     AVMWrongType
     */  
    public void createDirectory(String path, String name);

    /**
     * Create a new layered file. 
     * <p>
     * Note: the target of the indirection does not need to exist at 
     * the time the layered file node is created.
     * 
     * @param targetPath The absolute path of the underlying file being pointed at
     * @param parent     The absolute path of the directory containing layered file to be created
     * @param name       The name of the layered file to be created
     * @throws           AVMNotFound
     * @throws           AVMExists
     * @throws           AVMWrongType
     * 
     * @deprecated
     */
    public void createLayeredFile(String targetPath, String parent, String name);
    

    /**
     * Create a new layered directory. In whatever context this is created, this
     * will be a layered directory that has a primary indirection.
     * <p>
     * Note: a "primary" indirection is one in which the target is explicitly set;
     * "non-primary" indirect nodes compute their effective target dynamically
     * on the basis of their relative position to the closest "primary"
     * indirect node that contains them. Therefore, changing the target of a 
     * "primary" layered directory node immediately alters the indirection 
     * targets computed by the "non-primary" layered nodes it contains.
     * <p>
     * Note: the target of the indirection does not need to exist at 
     * the time the layered directory node is created.
     *
     * @param targetPath The absolute path to the underlying directory that 
     *                   the layered directory being created will point at.
     * @param parent     The absolute path to directory containing the layered directory being created.
     * @param name       The name of the layered directory being created
     * @throws           AVMNotFound
     * @throws           AVMExists
     * @throws           AVMWrongType
     */
    public void createLayeredDirectory(String targetPath, String parent, String name);


    /**
     * Retarget a layered directory. 
     * Change the target pointed to by a layered directory node.
     * This has the side effect of making the layered directory 
     * a primary indirection if the layered directory's indirection
     * was "non-primary".
     *
     * @param path   Path to the layered directory.
     * @param target The new indirection target of the layered directory
     * @throws       AVMNotFoundException
     * @throws       AVMWrongTypeException
     */
    public void retargetLayeredDirectory(String path, String target);
    

    /**
     * Create a new AVMStore.  
     * All stores are top level objects within the AVM repository.
     * The AVM is a forest of versioned trees;  each versioned
     * tree is contained within a AVM store with a unique 
     * name.  If a store is removed via 
     * {@link purgeStore(String name) purgeStore}, the name of
     * the deleted store can be reused in a later call to 
     * {@link createStore(String name) createStore}.
     * <p>
     * The store name must be non-null, cannot be the empty string,
     * and must not contain characters that are illegal in 
     * normal file names.
     *
     * @param name The name of the new AVMStore.
     * @throws     AVMExistsException
     */
    public void createStore(String name);
    
    /**
     * Create a new AVMStore with store properties (equivalent to createStore + setProperties)
     * 
     * @param name The name of the new AVMStore.
     * @param props A Map of the properties to set.
     * @throws     AVMExistsException
     * @since 3.2
     */
    public void createStore(String name, Map<QName, PropertyValue> props);
    

    /**
     * Create a branch from a given version and path.  As a side effect,
     * an automatic snapshot is taken of the store that contains the 
     * node that is being branched from.
     *
     * @param version The version number from which to make the branch.
     * @param srcPath The path to the node to branch from.
     * @param dstPath The path to the directory to contain the new branch.
     * @param name    The name to give the new branch.
     * @throws        AVMNotFoundException
     * @throws        AVMExistsException
     * @throws        AVMWrongTypeException
     */
    public void createBranch(int version, String srcPath, String dstPath, String name);
    

    /**
     * Remove a file or directory from its parent directory.
     * In a layered context, the newly deleted node will hide 
     * a file of the same name in the corresponding layer below.
     * <p>
     * If instead you want to make the file in the lower layer visible
     * via transparency, see: 
     * {@link uncover(String dirPath, String name) uncover}.
     * If you want to perform a removal and an uncover
     * operation atomically, see:
     * {@link #makeTransparent(String dirPath, String name) makeTransparent}.
     * 
     *
     * <p>
     * Caution:  this removes directories even if they are not empty.
     *
     * <p>
     * Note: developers of records management systems must also
     * be aware that the AVMNode corresponding to the 
     * <code>parent</code> directory and <code>name</code>
     * provided might still be accessible via different
     * path lookups after this function has completed;
     * this because branching and versioning operations create
     * manifestations of nodes in a manner that is similar
     * to a UNIX hard link.  If you need to discover every 
     * possible path that could retrieve the associated AVMNode, see:
     * {@link #getPaths(AVMNodeDescriptor desc) getPaths},
     * {@link #getHeadPaths(AVMNodeDescriptor desc) getHeadPaths}, and
     * {@link #getPathsInStoreHead(AVMNodeDescriptor desc) getPathsInStoreHead}.
     *
     * @param parent The absolute path to the parent directory. 
     * @param name   The name of the child to remove.
     * @throws       AVMNotFoundException
     * @throws       AVMWrongTypeException
     */
    public void removeNode(String parent, String name);

    

    /**
     * Rename a file or directory.
     * There are a number of things to note about the
     * interaction of rename and layering:
     * <p>
     * <ul>
     *   <li> If you rename a layered directory into a non layered context, 
     *        the layered directory continues to point to the same place 
     *        it did before it was renamed, and automatically becomes a 
     *        primary indirection node.  
     *   </li>
     *
     *   <li> If a plain directory is renamed into a layered context it 
     *        remains a plain directory, thus acting as an opaque node 
     *        in its new layered home.
     *   </li>
     *  
     *   <li>  Renaming a layered node into a layered node leaves the renamed 
     *         node pointing to the same place it did before the rename and 
     *         makes it automatically a primary indirection node.
     *   </li>
     *
     *   <li>  After a layered node or directory is renamed, 
     *         the directory that contained it acquires a 
     *         "deleted" node in its place;  therefore, 
     *         if a layer beneath it contains a file or directory
     *         with the same name as the "deleted" node, it
     *         will not be visible via transparency
     *         (unless an  {@link uncover(String dirPath, String name) uncover}
     *         operation is performed afterwards).
     *   </li>
     * </ul>
     * <p>
     *
     * Note: if instead you want to rename an AVM store, see 
     * {@link #renameStore(String sourceName, String destName) renameStore}.
     *
     * @param srcParent The absolute path to the parent directory.
     * @param srcName   The name of the node in the src directory.
     * @param dstParent The absolute path to the destination directory.
     * @param dstName   The name that the node will have in the destination directory.
     * @throws          AVMNotFoundException 
     * @throws          AVMExistsException 
     */
    public void rename(String srcParent, String srcName, String dstParent, String dstName);


    /**
     * If a layered directory <code>dirPath</code> 
     * has a deleted entry of the given <code>name</code>, 
     * remove that name from the deleted list,
     * so that if a layer below it contains an entry
     * of this name, it can be seen via transparency
     * from <code>dirPath</code>.  
     * <p>
     * Note: if you are looking for an atomic operation
     * that first deletes an object, then performs
     * an "uncover" operation to make it transparent, see 
     * {@link #makeTransparent(String dirPath, String name) makeTransparent}.
     *
     * @param dirPath The path to the layered directory.
     * @param name    The name to uncover.
     * @throws        AVMNotFoundException
     * @throws        AVMWrongTypeException
     */
    public void uncover(String dirPath, String name);


    /**
     * Gets the ID that the next snapshotted version of a store 
     * will have.
     *
     *<p>
     *  Note: unless the operations that require this value
     *  to be valid are performed within a transaction,
     *  this value can become "stale".
     *
     * @param storeName The name of the AVMStore.
     * @return          The next version ID of the AVMStore.
     * @throws          AVMNotFoundException
     */
    public int getNextVersionID(String storeName);


    /**
     * Get the latest snapshot ID of a store. 
     * Note:  All stores have at least one snapshot ID:  0;
     *        this is the "empty" snapshot taken when 
     *        the store is first created.
     *
     * @param storeName The store name.
     * @return          The ID of the latest extant version of the store.
     * @throws          AVMNotFoundException
     */
    public int getLatestSnapshotID(String storeName);

    
    /**
     * Snapshot the given AVMStore.
     * When files have been modified since the previous snapshot,
     * a new snapshot version is created;  otherwise, no extra
     * snapshot is actually taken.
     * <p>
     * When no snapshot is actually taken, but either 'tag'
     * or 'store' are non-null, they will override the value for 
     * the last snapshot (i.e.:  the old values will be discarded);
     * however, if both 'tag' and 'description' are null then
     * invoking createSnapshot when no files have been modified
     * becomes a true no-op.
     *
     * @param store The name of the AVMStore to snapshot.
     * @param tag The short description.
     * @param description The thick description.
     * @return A Map of all implicitly or explicitly snapshotted stores to last 
     * version id.
     * @throws AVMNotFoundException
     */
    public Map<String, Integer> createSnapshot(String store, String tag, String description);
    

    /**
     * Get the set of versions in an AVMStore. The version ID values
     * within this list will always be may appear out of order, 
     * and may contain missing values (due to the possibility that
     * {@link #purgeStore(String name) purgeStore} operations have
     * been performed).
     *
     * Because the number of versions that a store can contain 
     * may become large, this call can be a resource-intensive, 
     * and may even causing Out of Memory exceptions.
     *
     * @param name The name of the AVMStore.
     * @return     A Set of version descriptors.
     * @throws     AVMNotFoundException
     */
    public List<VersionDescriptor> getStoreVersions(String name);
    

    /**
     * Get AVMStore version descriptors by creation date. Either 
     * <code>from</code> or <code>to</code> can be null but not both.  
     * <p>
     * <ul>
     *     <li>
     *         If <code>from</code> is null, all versions earlier than 
     *         <code>to</code> will be returned.  
     *     </li>
     *
     *     <li>
     *         If <code>to</code> is null, all versions later than 
     *         <code>from</code> will be returned.  
     *     </li>
     * </ul>
     * <p>
     *
     * The order of the values returned is not guaranteed, nor are the version 
     * IDs necessarily free of "missing" values (due to the possibility that 
     * {@link #purgeStore(String name) purgeStore} operations have
     * been performed).
     *
     * <p>
     * Note: for portability, all dates are stored as 64-bit longs, with a
     * time resolution of one millisecond.  Therefore, aliasing/wrapping 
     * are not a concern unless you need to plan 292.4 million years ahead.
     * If so, please contact your system administrator.
     *
     * @param name The name of the AVMStore.
     * @param from Earliest date of version to include.
     * @param to   Latest date of version to include.
     * @return     The Set of version descriptors that match.
     * @throws     AVMNotFoundException
     */
    public List<VersionDescriptor> getStoreVersions(String name, Date from, Date to);
    
    /**
     * Get the descriptors of all AVMStores in the repository. 
     *
     * @return A List of all AVMStores.
     */
    public List<AVMStoreDescriptor> getStores();
    

    /**
     * Get a descriptor for an AVMStore.
     * @param name The AVMStore's name.
     * @return     A Descriptor, or null if not found.
     */
    public AVMStoreDescriptor getStore(String name);


    /**
     * A convenience method for getting the specified 
     * root directory of an AVMStore (e.g.:  <code>"mysite:/"</code>).
     *
     * @param version The version to look up.
     * @param name The name of the AVMStore.
     * @return A descriptor for the specified root.
     * @throws AVMNotFoundException
     */
    public AVMNodeDescriptor getStoreRoot(int version, String name);


    /**
     * Lookup a node identified by version ID and path.
     *
     * @param version The version ID to look under.
     * @param path The simple absolute path to the parent directory.
     * @return An AVMNodeDescriptor, or null if the node does not exist.
     */
    public AVMNodeDescriptor lookup(int version, String path);

    /**
     * Lookup a node identified by version ID and path; optionally,
     * if the node is deleted, its descriptor can still
     * be retrieved. 
     *
     * @param version        The version ID to look under.
     * @param path           The simple absolute path to the parent directory.
     * @param includeDeleted Whether to allow a deleted node to be retrieved
     * @return               An AVMNodeDescriptor, or null if the version does not exist.
     */
    public AVMNodeDescriptor lookup(int version, String path, boolean includeDeleted);
    
    /**
     * Lookup a node identified by the directory node that contains it, and its name.
     *
     * @param dir  The descriptor for the directory node.
     * @param name The name to lookup.
     * @return     The descriptor for the child.
     * @throws     AVMWrongTypeException If <code>dir</code> does not refer to a directory.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name);


    /**
     * Lookup a node identified by the directory that contains it, and its name;
     * optionally, the lookup can retrive the descriptor of a node even if 
     * it has been deleted from its containing directory.
     *
     * @param dir            The descriptor for the directory node.
     * @param name           The name to lookup.
     * @param includeDeleted Whether to allow a deleted node to be retrieved via the lookup
     * @return               The descriptor for the child, null if the child doesn't exist.
     * @throws               AVMNotFoundException
     * @throws               AVMWrongTypeException
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name, boolean includeDeleted);

    /**
     * Get a single valid path to a given node.
     * @param desc The descriptor of the node to which a version and path will be fetched.
     * @return version and path.
     * @throws AVMNotFoundException
     */
    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc);


    /**
     * Get the indirection path for a node in a layered context
     * whether that indirection path is primary or non-primary
     * (or seen via transparency).
     *
     * If you call getIndirectionPath on a layered node, 
     * you'll fetch its explicitly set target; if you call 
     * this function on a non-primary indirection node or a 
     * node seen via transparency, you'll get back the path 
     * to the corresponding node in the underlying target.
     * <p>
     * For example, if "mysite--alice:/www" is a layered
     * directory that targets "mysite:/www", and "mysite--alice"
     * contains no content directly, then if the path
     * path "mysite:/www/avm_webapps/ROOT/x/y/z" is valid,
     * calling <code>getIndirectionPath</code> on
     * "mysite--alice:/www/avm_webapps/ROOT/x/y/z" will yield
     * "mysite:/www/avm_webapps/ROOT/x/y/z".
     * 
     * @param version The version number to get.
     * @param path    The path to the node of interest.
     * @return        The indirection path, or null if the path is not in a layered context.
     * @throws        AVMNotFoundException
     * @throws        AVMWrongTypeException
     */
    public String getIndirectionPath(int version, String path);


    /**
     * Purge an AVMStore.  
     * This completely removes an AVMStore.
     * <p>
     * Note:  while the store being purged disappears from view 
     * immediately, any nodes that become unreachable as a result 
     * are deleted asynchronously.
     * 
     * @param  name The name of the AVMStore.
     * @throws      AVMNotFoundException 
     */
    public void purgeStore(String name);
    
    /**
     * Purge a version from an AVMStore.  
     * Deletes everything that lives in the given version only.
     *
     * @param version                The version to purge.
     * @param name                   The name of the AVMStore from which to purge it.
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
     * The initial element of the list returned will be <code>desc</code>
     * (as long as the count is non-zero).
     *
     * @param desc  The descriptor for a node to find ancestors for.
     * @param count maximum number of ancestors to return in the list 
     *              (the value <strong><code> -1 </code></strong> means 
     *              "no limit -- return them all")
     * @return      A List of ancestors starting with the most recent.
     * @throws      AVMNotFoundException
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count);
    
    /**
     * Set the opacity of a layered directory.  
     * An opaque layered directory hides the contents of its indirection.
     *
     * @param path The path to the layered directory.
     * @throws     AVMNotFoundException
     * @throws     AVMWrongTypeException
     */
    public void setOpacity(String path, boolean opacity);
    
    /**
     * Get the common ancestor of two nodes if a common ancestor exists.
     * This function is useful for detecting and merging conflicts.
     *
     * @param left  The first node.
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
     * The LayeringDescriptor returned can be used to determine
     * whether a node is in the background (and if so, which 
     * AVM store contains it directly), or if it is directly
     * contained by the AVM store referenced by <code>path</code>.
     * 
     *
     * @param version The version to look under.
     * @param path    The absolute AVM path.
     * @return        A LayeringDescriptor.
     * @throws        AVMNotFoundException
     * @throws        AVMWrongTypeException
     */
    public LayeringDescriptor getLayeringInfo(int version, String path);
    
    /**
     * Set a property on a node.
     *
     * @param path  The path to the node to set the property on. 
     * @param name  The QName of the property.
     * @param value The property to set.
     * @throws      AVMNotFoundException
     * @throws      AVMWrongTypeException
     */
    public void setNodeProperty(String path, QName name, PropertyValue value);
    
    /**
     * Set a collection of properties on a node.
     *
     * @param path       The path to the node.
     * @param properties The Map of properties to set.
     * @throws           AVMNotFoundException
     * @throws           AVMWrongTypeException
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties);
    
    /**
     * Get a property of a node by QName.
     *
     * @param version The version to look under.
     * @param path    The path to the node.
     * @param name    The QName.
     * @return        The PropertyValue or null if it doesn't exist.
     * @throws        AVMNotFoundException
     * @throws        AVMWrongTypeException
     */
    public PropertyValue getNodeProperty(int version, String path, QName name);
    
    /**
     * Get all the properties associated with a node that is identified
     * by a version ID and a path.
     *
     * @param version The version to look under.
     * @param path    The path to the node.
     * @return        A Map of QNames to PropertyValues.
     * @throws        AVMNotFoundException
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path);


    /**
     * Delete a property.
     * <p>
     * Note: to remove an apsect, see: {@link #removeAspect(String path, QName aspectName) removeAspect}
     *
     * @param path The path to the node.
     * @param name The QName of the property to delete.
     * @throws     AVMNotFoundException
     */
    public void deleteNodeProperty(String path, QName name);
    
    /**
     * Delete all the properties attached to an AVM node.
     * <p>
     * Note: to remove an apsect, see: {@link #removeAspect(String path, QName aspectName) removeAspect}
     *
     * @param path The path to the node.
     * @throws AVMNotFoundException
     */
    public void deleteNodeProperties(String path);
    
    /**
     * Set a property on a store. If the property exists it will be overwritten.
     *
     * @param store The store to set the property on.
     * @param name  The name of the property.
     * @param value The value of the property.
     * @throws      AVMNotFoundException (if store does not exist)
     */
    public void setStoreProperty(String store, QName name, PropertyValue value);
    
    /**
     * Set a group of properties on a store. Existing properties will be overwritten.
     *
     * @param store The name of the store.
     * @param props A Map of the properties to set.
     * @throws      AVMNotFoundException (if store does not exist)
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props);
    
    /**
     * Get a property from a store.
     *
     * @param store The name of the store.
     * @param name  The name of the property.
     * @return      A PropertyValue or null if non-existent.
     * @throws      AVMNotFoundException (if store does not exist)
     */
    public PropertyValue getStoreProperty(String store, QName name);

    /**
     * Get all the properties associated with a store.
     *
     * @param store The name of the store.
     * @return      A Map of the stores properties.
     * @throws      AVMNotFoundException (if store does not exist)
     */
    public Map<QName, PropertyValue> getStoreProperties(String store);

    /**
     * Queries a given store for properties with keys that match a given pattern. 
     *
     * @param store      The name of the store.
     * @param keyPattern The sql 'like' pattern, inserted into a QName.
     * @return           A Map of the matching key value pairs.
     * 
     * @deprecated
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store, QName keyPattern);
    
    
    /**
     * Delete a property on a store by name.
     * <p>
     * Note: to remove an apsect, see: {@link #removeAspect(String path, QName aspectName) removeAspect}
     *
     * @param store The name of the store.
     * @param name  The name of the property to delete.
     * @throws      AVMNotFoundException
     */
    public void deleteStoreProperty(String store, QName name);
    

    /**
     * Add an aspect to an AVM node.
     *
     * @param path       The path to the node.
     * @param aspectName The QName of the aspect.
     * @throws           AVMNotFoundException
     * @throws           AVMExistsException
     */
    public void addAspect(String path, QName aspectName);

    /**
     * Get all the aspects on an AVM node.
     *
     * @param version The version to look under.
     * @param path    The path to the node.
     * @return        A Set of the QNames of the aspects.
     * @throws        AVMNotFoundException   
     */
    public Set<QName> getAspects(int version, String path);
    

    /**
     * Remove an aspect and its properties from a node.
     *
     * @param path       The path to the node.
     * @param aspectName The name of the aspect.
     * @throws           AVMNotFoundException
     */
    public void removeAspect(String path, QName aspectName);
    
    /**
     * Determines whether a node has a particular aspect.
     *
     * @param version    The version to look under.
     * @param path       The path to the node.
     * @param aspectName The aspect name to check.
     * @return           Whether the given node has the given aspect.
     * @throws           AVMNotFoundException
     */
    public boolean hasAspect(int version, String path, QName aspectName);


    /**
     * Rename a store.
     *
     * @param sourceName The original name.
     * @param destName   The new name.
     * @throws           AVMNotFoundException
     * @throws           AVMExistsException
     */
    public void renameStore(String sourceName, String destName);
    
    /**
     * Revert a <strong><code>HEAD</code></strong> path to a given version. 
     * This works by cloning the version to revert to, and then linking 
     * that new version into <strong><code>HEAD</code></strong>.
     * The reverted version will have the previous 
     * <strong><code>HEAD</code></strong> version as ancestor.
     *
     * @param path       The path to the node to revert.
     * @param toRevertTo The descriptor of the version to revert to.
     * @throws            AVMNotFoundException
     */
    public void revert(String path, AVMNodeDescriptor toRevertTo);


    /**
     * Set the GUID on a node. The GUID of a node uniquely identifies 
     * the state of a node, i.e. its content, metadata, and aspects.
     * @param path The path to the node.
     * @param guid The GUID to set.
     */
    public void setGuid(String path, String guid);

    /**
     * Set the mime type.
     * @param path The path of the file.
     * @param mimeType The mime type.
     */
    public void setMimeType(String path, String mimeType);
    
    /**
     * Set the encoding.
     * @param path The path of the file.
     * @param encoding The encoding.
     */
    public void setEncoding(String path, String encoding);

    /**
     * Queries all AVM stores for properties with keys that match a given pattern. 
     *
     * @param keyPattern The sql 'like' pattern, inserted into a QName.
     * @return           A Map of store names to Maps of property key value pairs that match
     * the pattern.
     * 
     * @deprecated
     */
    public Map<String, Map<QName, PropertyValue>>
        queryStoresPropertyKeys(QName keyPattern);

    //-------------------------------------------------------------------------
    // NOTE:    The functions below should probably be included in AVMRemote
    //          but currently are not:
    //-------------------------------------------------------------------------

    /**
     * Create a new directory with aspects and properties.
     * If <code>path</code> is within a layer, the new directory will be a layered directory;
     * otherwise, the new directory will be a plain directory.
     *
     * @param path The simple absolute path to the parent.
     * @param name The name to give the directory.
     * @param aspects A list of aspects to add.
     * @param properties A Map of properties to add.
     * @throws     AVMNotFound
     * @throws     AVMExists
     * @throws     AVMWrongType
     */  

    public void createDirectory(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties);
    /**
     * Create a new "plain" (non-layered) file.  
     * Guarantees that the entire contents of the
     * input stream will be loaded atomically.
     * The directory identified by <code>path</code> must already exist.
     *
     * @param path The path of the directory containing the created file.
     * @param name The name of the new file
     * @param in   An input stream with data for the file.
     * @throws     AVMNotFound
     * @throws     AVMExists
     * @throws     AVMWrongType
     */
    public void createFile(String path, String name, InputStream in);
    
    
    /**
     * Create a new "plain" (non-layered) file with aspects and properties.
     * Guarantees that the entire contents of the
     * input stream will be loaded atomically.
     * The directory identified by <code>path</code> must already exist.
     *
     * @param path The path of the directory containing the created file.
     * @param name The name of the new file
     * @param in   An input stream with data for the file.
     * @param aspect A list of aspects to give the file.
     * @param properties A map of properties to give the file.
     * @throws     AVMNotFound
     * @throws     AVMExists
     * @throws     AVMWrongType
     */
    public void createFile(String path, String name, InputStream in, List<QName> aspects, Map<QName, PropertyValue> properties);

    /**
     * Get a non-recursive directory listing of a directory identified
     * by a node descriptor;  optionally, deleted nodes can be included 
     * in this listing.
     *
     * @param dir            The directory node descriptor.
     * @param includeDeleted Whether to include deleted nodes.
     * @return               A Map of names to node descriptors.
     * @throws               AVMNotFoundException If the descriptor is stale.
     * @throws               AVMWrongTypeException If the descriptor does not point at a directory.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir,
                                                                    boolean includeDeleted);
    
    /**
     * A convenience method to get a non-recursive directory listing 
     * as an Array from a directory identified by a node descriptor;  
     * optionally, deleted nodes can be included in this listing.  
     *
     * This function is identical to 
     * {@link getDirectoryListing(AVMNodeDescriptor dir,boolean includeDeleted)  getDirectoryListing}
     * except that it returns an array.
     *
     * @param dir            The descriptor pointing at the directory to list.
     * @param includeDeleted Whether include deleted nodes
     * @return               An array of AVMNodeDescriptors.
     * @throws               AVMNotFoundException 
     * @throws               AVMWrongTypeException
     */
    public AVMNodeDescriptor [] getDirectoryListingArray(AVMNodeDescriptor dir,
                                                         boolean includeDeleted);
    
    /**
     * Get a non-recursive listing of nodes contained directly,
     * but exclude all nodes that are only contained "indirectly"
     * (i.e.: via layering).  This function is identical to 
     * {@link #getDirectoryListingDirect(int version, String path) getDirectoryListingDirect}
     * but it has the option of including deleted nodes in the listing.
     * 
     * @param version        The version to look up.
     * @param path           The absolute AVM directory path to get a listing for.
     * @param includeDeleted Whether to include deleted nodes.
     * @return               A Map of names to descriptors.
     * @throws               AVMNotFoundException
     * @throws               AVMWrongTypeException 
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(int version, String path, boolean includeDeleted);


    /**
     * A convenience method to get a directory listing 
     * as an Array of AVMNodeDescriptors; this function
     * is otherwise equivalent to 
     * {@link #getDirectoryListing(int version, String path, boolean includeDeleted) getDirectoryListing}
     *
     * @param version        The version to look under.
     * @param path           The path to the directory to be listed.
     * @param includeDeleted Whether to include deleted nodes
     * @return               An array of AVMNodeDescriptors.
     * @throws               AVMNotFoundException
     * @throws               AVMWrongTypeException
     */
    public AVMNodeDescriptor [] getDirectoryListingArray(int version, String path,
                                                         boolean includeDeleted);
    
    /**
     * Get a non-recursive listing of all the nodes contained by a directory
     * identified by an AVMNodeDescriptor, excluding those that are only 
     * present "indirectly" via layering; optionally, deleted nodes that
     * are directly contained can be included this listing.
     *
     * @param dir            The directory descriptor.
     * @param includeDeleted Whether to include directly contained deleted nodes
     * @return               A Map of Strings to descriptors.
     * @throws               AVMNotFoundException
     * @throws               AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(AVMNodeDescriptor dir, boolean includeDeleted);
    

    /**
     * Get a non-recursive listing of a directory node identified by its
     * version ID and path; optionally, deleted notes can be included in
     * this listing.
     * <p>
     * If instead, you wish to obtain a list of <em>only</em> 
     * the deleted nodes within a directory, see:
     * {@link #getDeleted(int version, String path) getDeleted}.
     *
     * @param version        The version ID to look in.
     * @param path           The absolute AVM path to the file.
     * @param includeDeleted Whether to include deleted nodes
     * @return               A Map of names to descriptors.
     * @throws               AVMNotFoundException 
     * @throws               AVMWrongTypeException
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path,
                                                                    boolean includeDeleted);

    /**
     * A convenience method that removes a node specified by an AVM path.
     * It merely parses an absolute path into a parent directory and a child
     * name, then calls {@link #removeNode(parent, name) removeNode}.
     *
     * @param path The full path to the node to remove.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void removeNode(String path);

    /**
     * Atomically delete <code>name</code> within <code>dirPath</code> 
     * and {@link uncover(String dirPath, String name) uncover}
     * it so whatever is underneath can be seen via transparency.
     * If <code>name</code> corresponds to a deletion already,
     * then the deletion step is skipped, and the "uncover"
     * operation is performed.
     *
     * @param dirPath The path to the layered directory.
     * @param name    The name of the item this method will 
     *             {@link org.alfresco.service.cmr.avmsync.AVMSyncService#flatten(String layerPath, String underlyingPath) flatten}
     *
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public void makeTransparent(String dirPath, String name);

    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp;
     * Retrieve the reserved "system" store.  
     * This method isn't currently used,
     * but may be in some future release.
     *
     * @return The descriptor.
     */
    public AVMStoreDescriptor getSystemStore();
    
    /**
     * Set all metadata on a node from another node. Aspects, properties, ACLs.
     *
     * @param path The path to the node to set.
     * @param from The descriptor for the node to get metadata from.
     * @throws     AVMNotFoundException
     */
    public void setMetaDataFrom(String path, AVMNodeDescriptor from);
    
    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; Insert a node 
     * into a parent directly.  Caution: this is not something 
     * one ordinary applications should do, but it is used 
     * internally by the AVMSyncService.update() method. 
     * This function may disappear from the public interface.
     *
     * @param parentPath The path to the parent directory.
     * @param name       The name to give the node.
     * @param toLink     A descriptor for the node to insert.
     * @throws           AVMNotFoundException
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink);
    
    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; replace a node 
     * in a parent directly.  Caution: this is not something 
     * one ordinary applications should do, but it is used 
     * internally by the AVMSyncService.update() method. 
     * This function may disappear from the public interface.
     *
     * @param parentPath The path to the parent directory.
     * @param name       The name to give the node.
     * @param toLink     A descriptor for the node to insert.
     * @throws           AVMNotFoundException
     */
    public void updateLink(String parentPath, String name, AVMNodeDescriptor toLink);
    
    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp;  Force a copy on write 
     * write event on the given node.  This function is not usually 
     * needed, and may be removed from the public interface.
     *
     * @param path The path to force.
     * @throws     AVMNotFoundException
     */
    public AVMNodeDescriptor forceCopy(String path);

    /**
     * Perform a non-virtual (heavy-weight), and potentially recursive
     * copy of the source into the destination directory. 
     * All node properties, access control lists (ACLs), 
     * and aspects are copied.
     *
     * @param srcVersion The version of the source.
     * @param srcPath    The path of the source directory.
     * @param dstPath    The directory containing the destination directory.
     * @param name       The name of the destination within <code>dstPath</code>.
     * @throws           AVMNotFoundException
     */
    public void copy(int srcVersion, String srcPath, String dstPath, String name);
    

    /**
     * Get a list of all paths that a given node has.  
     * This can be an extremely expensive operation due to the large number
     * of paths to an AVMNodeDescriptor that can be generated
     * via branching and versioning.  For example, if an asset
     * is present in the initial version of a store, and
     * that store has been versioned 10,000 times, 
     * there are a minimum of 10,000 paths that lead to it.
     * The effect of branching is multiplicative.
     * <p>
     * Note:  paths that only access <code>desc</code>
     * via transparency are not returned by this function;
     * only "direct" containment relationships are considered.
     * <p>
     * For those concerned with records management applications,
     * it's worth noting that once every path to an asset has
     * been deleted, the system will purge it entirely in an
     * asynchronous manner.
     *
     * @param desc The node descriptor to get paths for.
     * @return     A List of version, path Pairs.
     * @throws     AVMNotFoundException
     */
    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc);
    
    /**
     * Get all paths that a given node has that are in the 
     * <strong><code>HEAD</code></strong> version (<strong><code> -1 </code></strong>).
     * This can be an expensive operation but less so than getPaths().
     *
     * @param desc The node descriptor to get paths for.
     * @return     A List of version, path Pairs.
     * @throws     AVMNotFoundException
     */
    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc);
    
    /**
     * Get all paths to a node starting at the 
     * <strong><code>HEAD</code></strong> version (<strong><code> -1 </code></strong>)
     * of a store.  This can be an expensive operation but less so than getHeadPaths().
     *
     * @param desc The node descriptor.
     * @param      store The store.
     * @return     A List of all paths meeting the criteria.
     * @throws     AVMNotFoundException
     */
    public List<Pair<Integer, String>> getPathsInStoreHead(AVMNodeDescriptor desc, String store);
    
    /**
     * Get all paths to a given node in a single store in a single non-head version.
     * @param desc The node descriptor.
     * @param store The name of the store.
     * @param version The version.
     * @return A List of AVM paths.
     */
    public List<String> getPathsInStoreVersion(AVMNodeDescriptor desc, String store, int version);
    
    /**
     * Get all the properties associated with a node identified by a descriptor.
     * @param desc The node descriptor
     * @return A Map of the properties.
     * @throws AVMNotFoundException
     */
    public Map<QName, PropertyValue> getNodeProperties(AVMNodeDescriptor desc);
    
    /**
     * Get all the aspects from a node descriptor.
     * @param desc The node descriptor.
     * @return The Set of Aspects.
     */
    public Set<QName> getAspects(AVMNodeDescriptor desc);
    




    //-------------------------------------------------------------------------
    // NOTE:    The functions from here down will never be part of AVMRemote
    //-------------------------------------------------------------------------

    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; Fetch a content reader for a file node.
     * 
     * This function is similar to 
     * {@link getFileInputStream(int version, String path) getFileInputStream};
     * however, it can be used to fetch either InputStream or a 
     * random-access nio channel.
     *
     * @param  version The version ID of the file.
     * @param  path    The absolute AVM path to the file.
     * @return         A ContentReader.
     * @throws         AVMNotFoundException
     * @throws         AVMWrongTypeException
     */
    public ContentReader getContentReader(int version, String path);

    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; Fetch a ContentWriter to a file node.
     *
     * @param path The path to the file.
     * @param update true if the property must be updated atomically when the content write
     *      stream is closed (attaches a listener to the stream); false if the client code
     *      will perform the updates itself.
     * @return A ContentWriter.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentWriter getContentWriter(String path, boolean update);   

    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; Get the ContentData for 
     * a node in a read context.  Only applies to a file.
     *
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The ContentData object.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentData getContentDataForRead(int version, String path);
    
    /**
     * Get ContentData using only a node descriptor.
     * @param desc The node descriptor.
     * @return The ContentData
     * @throws AVMNotFoundException
     */
    public ContentData getContentDataForRead(AVMNodeDescriptor desc);
    

    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; Get the ContentData for 
     * a node in a write context.
     *
     * @param path The path to the node.
     * @return The ContentData object.
     * @throws AVMNotFoundException
     * @throws AVMWrongTypeException
     */
    public ContentData getContentDataForWrite(String path);

    /**
     * <em>Low-level internal function</em>:&nbsp;&nbsp; Set the content data on a file. 
     *
     * @param path The path to the file.
     * @param data The ContentData to set.
     * @throws     AVMNotFoundException
     * @throws     AVMWrongTypeException
     */
    public void setContentData(String path, ContentData data);
    
    /**
     * Get all versions from the last down to and including the one specified
     * Returned in descending version id.
     * @param name
     * @param version
     * @return list of version descriptors
     */
    public List<VersionDescriptor> getStoreVersionsFrom(String name, int version);
    
    /**
     * Get all versions from the first up to and including the one specified
     * Returned in ascending version id order.
     * @param name
     * @param version
     * @return list of version descriptors
     */
    public List<VersionDescriptor> getStoreVersionsTo(String name, int version);
    
    /**
     * Get all versions from an including startVersion up to but NOT including endVersion 
     * @param name
     * @param startVersion
     * @param endVersion
     * @return list of version descriptors
     */
    public List<VersionDescriptor> getStoreVersionsBetween(String name, int startVersion, int endVersion);

}
