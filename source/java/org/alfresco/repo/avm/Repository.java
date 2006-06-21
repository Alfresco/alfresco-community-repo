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

/**
 * The repository interface.  Methods for filesystem like, versioning,
 * and layering operations.
 * @author britt
 */
interface Repository
{
    /**
     * This returns the next version in this repository that will be snapshotted.
     * @return The next version to be snapshotted.
     */
    public int getNextVersionID();

    /**
     * Set a new root for this repository.
     * @param root The root to set.
     */
    public void setNewRoot(DirectoryNode root);

    /**
     * Snapshots this repository.  This sets all nodes in the
     * the repository to the should be copied state, and creates
     * a new version root.
     */
    public void createSnapshot();

    /**
     * Create a new directory.
     * @param path The path to the parent directory.
     * @param name The name to give the new directory.
     */
    public void createDirectory(String path, String name);

    /**
     * Create a new layered directory.
     * @param srcPath The path that the new layered directory will point at.
     * @param dstPath The path to the directory to create the new layered directory in.
     * @param name The name of the new layered directory.
     */
    public void createLayeredDirectory(String srcPath, String dstPath,
                                       String name);

    /**
     * Create a new file. The designated file cannot already exist.
     * @param path The path to the directory to contain the new file.
     * @param name The name to give the new file.
     * @param source An InputStream of data to put in the file.  May be null.
     */
    public OutputStream createFile(String path, String name);

    /**
     * Create a new layered file.
     * @param srcPath The target path for the new file.
     * @param dstPath The path to the directory to make the new file in.
     * @param name The name of the new file.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name);

    /**
     * Get an InputStream from a file.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return An InputStream
     */
    public InputStream getInputStream(int version, String path);

    /**
     * Get a listing of the designated directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A listing.
     */
    public Map<String, AVMNodeDescriptor> getListing(int version, String path);

    /**
     * Get an output stream to a file.
     * @param path The path to the file.
     * @return An OutputStream
     */
    public OutputStream getOutputStream(String path);
    
    /**
     * Get a random access file to the given file.
     * @param version The version id (read-only if not -1)
     * @param path The path to the file.
     * @param access The access for RandomAccessFile.
     * @return A RandomAccessFile.
     */
    public RandomAccessFile getRandomAccess(int version, String path, String access);

    /**
     * Remove a node and all of its contents.
     * @param path The path to the node's parent directory.
     * @param name The name of the node to remove.
     */
    public void removeNode(String path, String name);

    /**
     * Uncover a whited out node.
     * @param dirPath The path to the directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name);

    // TODO This is problematic.  As time goes on this returns
    // larger and larger data sets.  Perhaps what we should do is
    // provide methods for getting versions by date range, n most 
    // recent etc.
    /**
     * Get all the version for this repository.
     * @return A Set of all versions.
     */
    public List<VersionDescriptor> getVersions();
    
    /**
     * Get the versions from between the given dates. From or to 
     * may be null but not both.
     * @param from The earliest date.
     * @param to The latest date.
     * @return The Set of matching version IDs.
     */
    public List<VersionDescriptor> getVersions(Date from, Date to);

    /**
     * Get the super repository.
     * @return The SuperRepository.
     */
    public SuperRepository getSuperRepository();

    /**
     * Lookup a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param write Whether this is in a write context.
     * @return A Lookup object.
     */
    public Lookup lookup(int version, String path, boolean write);

    /**
     * Lookup a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @param write Whether this is in a write context.
     * @return A Lookup object.
     */
    public Lookup lookupDirectory(int version, String path, boolean write);

    /**
     * For a layered node, get its indirection.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The indirection.
     */
    public String getIndirectionPath(int version, String path);

    /**
     * Make the indicated directory a primary indirection.
     * @param path The Repository relative path.
     */
    public void makePrimary(String path);

    /**
     * Change the target of a layered directory.
     * @param path The path to the layered directory.
     * @param target The new target path.
     */
    public void retargetLayeredDirectory(String path, String target);
    
    /**
     * Get the root directory of this Repository.
     * @return The root directory.
     */
    public DirectoryNode getRoot();

    /**
     * Get the specified root as a descriptor.
     * @param version The version to get (-1 for head).
     * @return The specified root or null.
     */
    public AVMNodeDescriptor getRoot(int version);
    
    /**
     * Get the name of this repository.
     * @return The name.
     */
    public String getName();
    
    /**
     * Purge all the nodes reachable only by the given version.
     * @param version
     */
    public void purgeVersion(int version);
}