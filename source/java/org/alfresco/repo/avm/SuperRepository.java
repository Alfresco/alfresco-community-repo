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

import org.hibernate.Session;

/**
 * A SuperRepository is responsible for the high level implemenation of all
 * operations on Repositories.  It is responsible for issuing Node ids, branch ids,
 * and layer ids.  Repositories themselves are responsible for issuing version ids.
 * Paths in super repositories are of the form "repositoryname:/a/b/c/d
 * @author britt
 */
public interface SuperRepository
{
    // Modify operations.
    
    /**
     * Create a new file.
     * @param path The path to create the file in.
     * @param name The name to give the new file.
     */
    public void createFile(String path, String name);
    
    /**
     * Create a new Directory.
     * @param path The path to create the directory in.
     * @param name The name to give the new directory.
     */
    public void createDirectory(String path, String name);
    
    /**
     * Create a new LayeredDirectory.
     * @param srcPath The source path that the LayeredDirectory refers to.
     * @param dstPath The path to create the new LayeredDirectory in.
     * @param name The name to give the new LayeredDirectory.
     */
    public void createLayeredDirectory(String srcPath, String dstPath, String name);
    
    /**
     * Create a new LayeredFile.
     * @param srcPath The source path that the LayeredFile refers to.
     * @param dstPath The path to create the new LayeredFile in.
     * @param name The name to give the new LayeredFile.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name);
    
    /**
     * Create a new Repository.
     * @param name The name to give the repository.
     */
    public void createRepository(String name);
    
    /**
     * Create a branch.
     * @param version The version to look under.
     * @param srcPath The path to the source node for the branch.
     * @param dstPath The directory to put the branch in.
     * @param name The name for the new branch.
     */
    public void createBranch(int version, String srcPath, String dstPath, String name);
    
    // Modify operations.
    
    /**
     * Get an OutputStream to a file.
     * @param path The path to the file.
     * @return An OutputStream.
     */
    public OutputStream getOutputStream(String path);
    
    /**
     * Rename a node.
     * @param srcPath The source path.
     * @param srcName The name of the node to rename.
     * @param dstPath The destination directory for the rename.
     * @param dstName The name to give the renamed node.
     */
    public void rename(String srcPath, String srcName, String dstPath, String dstName);
    
    /**
     * Slide a directory in a layer to another location in the same 
     * layer uncovering what was originally shadowed.
     * @param srcPath The source path.
     * @param srcName The name of the directory to slide.
     * @param dstPath The destination directory for the slide.
     * @param dstName The name of the directory after sliding.
     */
    public void slide(String srcPath, String srcName, String dstPath, String dstName);

    /**
     * Create a snapshot of the given repositories.
     * @param repositories A List of Repository names.
     */
    public void createSnapshot(List<String> repositories);
    
    // Different flavors of deletions.
    
    /**
     * Delete a node.
     * @param path The path to the containing directory.
     * @param name The name of the node to remove.
     */
    public void remove(String path, String name);
    
    /**
     * Purge a repository.
     * @param name
     */
    public void destroyRepository(String name);
    
    /**
     * Purge a version in a repository.
     * @param name The name of the repository.
     * @param version The id of the version to purge.
     */
    public void purgeVersion(String name, int version);

    // Read operations.
    
    /**
     * Get an InputStream from a File.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getInputStream(int version, String path);
    
    /**
     * Get the listing for a directory.
     * @param version The version to get a listing of.
     * @param path The path to the directory.
     * @return A List of FolderEntries.
     */
    public List<FolderEntry> getListing(int version, String path);
    
    /**
     * Get a listing of all repository names.
     * @return A List of repository names.
     */
    public List<String> getRepositoryNames();

    /**
     * Get a Set of version IDs for a given Repository.
     * @param name The name of the repository.
     * @return A Set of IDs.
     */
    public Set<Integer> getRepositoryVersions(String name);
    
    /**
     * Issue a unique identifier for a new node.
     * @return A new identifier.
     */
    public long issueID();
    
    /**
     * Issue an ID for content.
     * @return A new content ID.
     */
    public long issueContentID();
    
    /**
     * Get the current Hibernate Session.
     * @return The Hibernate Session object.
     */
    public Session getSession();
    
    /**
     * Return an OutputStream for the content object of the given id.  This
     * creates the directory path if needed.
     * @param path The path to the file.
     * @return An OutputStream.
     */
    public OutputStream createContentOutputStream(String path);
    
    /**
     * Gets an input stream from the content with the given id.
     * @param version The version id.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getContentInputStream(int version, String path);
    
    /**
     * Get the latest version id for a given repository.
     * @param name The name of the repository.
     * @return The latest version id for the given repository.
     */
    public long getLatestVersionID(String name);
    
    /**
     * Get the indirection path for a layered node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The indirection path.
     */
    public String getIndirectionPath(int version, String path);
    
    /**
     * Get a lookup object for a path.
     * @param version The version to look under.
     * @param path The full path.
     * @return The Lookup.
     */
    public Lookup lookup(int version, String path);
    
    /**
     * Get a lookup object for a path.  Directory only.
     * @param version The version to look under.
     * @param path The full path.
     * @return The Lookup.
     */
    public Lookup lookupDirectory(int version, String path);    
}
