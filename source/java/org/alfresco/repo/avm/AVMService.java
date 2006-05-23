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
import java.util.List;
import java.util.Set;

/**
 * This is the service interface for the [Alfresco|Addled|Advanced|Apotheosed] Versioning
 * Model.  It specifies methods that are close in functionality to the underlying
 * implementation, and is intended as both a first class Alfresco service and an
 * aid in creating new implementations of existing services.
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
     * Get an output stream to a file node.
     * @param path The simple absolute path to the file node.
     */
    public OutputStream getFileOutputStream(String path);
    
    /**
     * Get a listing of a Folder by name.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @return A List of FolderEntrys.
     */
    public List<FolderEntry> getDirectoryListing(int version, String path);
    
    /**
     * Create a new File.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the file.
     */
    public void createFile(String path, String name);
    
    /**
     * Create a new Folder.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the folder.
     */
    public void createDirectory(String path, String name);
    
    /**
     * Create a new layered file.
     * @param srcPath The simple absolute path that the new file will shadow.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new file.
     */
    public void createLayeredFile(String srcPath, String parent, String name);
    
    /**
     * Create a new layered folder.
     * @param srcPath The simple absolute path that the new folder will shadow.
     * @param parent The simple absolute path to the parent.
     * @param name The name to give the new folder.
     */
    public void createLayeredDirectory(String srcPath, String parent, String name);

    /**
     * Retarget a layered folder.
     * @param path Path to the layered directory.
     * @param target The new target to aim at.
     */
    public void retargetLayeredFolder(String path, String target);
    
    /**
     * Create a new Repository.  All Repositories are top level in a hierarchical
     * sense.
     * @param name The name to give the virtual repository.
     */
    public void createRepository(String name);
    
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
     * Slide a directory from one place in a layer to another uncovering what was
     * underneath it.
     * @param srcParent The simple absolute path to the parent folder.
     * @param srcName The name of the node in the src Folder.
     * @param dstParent The simple absolute path to the destination folder.
     * @param dstName The name that the node will have in the destination folder.
     */
    public void slide(String srcParent, String srcName, String dstParent, String dstName);

    /**
     * Get the latest version id of the repository.
     * @param repName The name of the repository.
     * @return The latest version id of the repository.
     */
    public int getLatestVersionID(String repName);
    
    /**
     * Snapshot the given repositories.  When this is called everything that has been added,
     * deleted, or modified since the last time this function was called, is marked 
     * as needing to be copied, so that further modifications will trigger copy on write
     * semantics.
     * @param repositories The names of the repositories to snapshot.
     */
    public void createSnapshot(List<String> repositories);
    
    /**
     * Snapshot the given repository.
     * @param repository The name of the repository to snapshot.
     */
    public void createSnapshot(String repository);
    
    /**
     * Get the set of version IDs in a Repository
     * @param name The name of the Repository.
     * @return A Set of version IDs
     */
    public Set<Integer> getRepositoryVersions(String name);
    
    /**
     * Lookup a node by version ids and path.
     * @param version The version id to look under.
     * @param path The simple absolute path to the parent directory.
     * @return A Lookup object.
     */
    public Lookup lookup(int version, String path);
    
    /**
     * Get the indirection path for a layered file or directory.
     * @param version The version number to get.
     * @param path The path to the node of interest.
     * @return The indirection path.
     */
    public String getIndirectionPath(int version, String path);
    
    /**
     * Destroy a repository.  This is a complete wipe of a Repository.
     * @param name The name of the Repository.
     */
    public void destroyRepository(String name);
    
    /**
     * Purge a version from a repository.  Deletes everything that lives in
     * the given version only.
     * @param version The version to purge.
     * @param name The name of the Repository from which to purge it.
     */
    public void purgeVersion(int version, String name);
}
