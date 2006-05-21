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

import org.alfresco.repo.avm.hibernate.RepositoryBean;


/**
 * This is the interface between low level repository objects
 * and the high level implementation of the repository.
 * @author britt
 */
public interface Repository
{
    /**
     * Inform this Repository that a Node is new and should
     * therefore not be copied.
     * @param node The node that is reporting itself.
     */
    public void setNew(AVMNode node);
    
    /**
     * Get the latest version id.
     * @return The latest version.
     */
    public int getLatestVersion();
    
    /**
     * Inform this Repository that the root node is new.
     * @param root The new root directory node.
     */
    public void setNewRoot(DirectoryNode root);
    
    /**
     * Make a snapshot.  Equivalent of subversion end of commit.  A new
     * version number is issued.
     */
    public void createSnapshot();

    /**
     * Create a new directory.
     * @param path The path to the directory for creation.
     * @param name The name for the new directory.
     */
    public void createDirectory(String path, String name);

    /**
     * Create a layered directory over srcPath at dstPath/name.
     * @param srcPath Fully qualified path.
     * @param dstPath Repository path to target directory.
     * @param name What the new layered directory should be called.
     */
    public void createLayeredDirectory(String srcPath, String dstPath, String name);

    /**
     * Create a new empty file.
     * @param path The path to the directory for creation.
     * @param name The name for the new file.
     */
    public void createFile(String path, String name);

    /**
     * Create a layered file over srcPath at dstPath/name
     * @param srcPath Fully qualified path.
     * @param dstPath Repository path.
     * @param name The name the new layered file should have.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name);
    
    /**
     * Get an input stream from an existing file.
     * TODO Figure out nio way of doing things.
     * @param version The version id to look under.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getInputStream(int version, String path);
    
    /**
     * Get a directory listing.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of FolderEntries.
     */
    public List<FolderEntry> getListing(int version, String path);
    
    /**
     * Get an OutputStream to an existing file.  This may trigger a copy.
     * @param path The path to the file.
     */
    public OutputStream getOutputStream(String path);
    
    /**
     * Remove a node from a directory.
     * @param path The path to the directory.
     * @param name The name of the node to remove.
     */
    public void removeNode(String path, String name);
    
    /**
     * This moves a node from one place in a layer to another place in 
     * the same layer without leaving a deleted entry in the source directory.
     * @param srcPath The path containing the src node.
     * @param srcName The name of the src node.
     * @param dstPath The destination container.
     * @param dstName The name of the destination node.
     */
    public void slide(String srcPath, String srcName, String dstPath, String dstName);
    
    /**
     * Get the version ids that this Repository has.
     * @return A Set of Version IDs.
     */
    public Set<Integer> getVersions();
    
    /**
     * Get the data bean.
     * @return The data bean.
     */
    public RepositoryBean getDataBean();
    
    /**
     * Get the super repository.
     * @return The SuperRepository.
     */
    public SuperRepository getSuperRepository();
    
    /**
     * Get a lookup object for a path.
     * @param version The version to look under.
     * @param path The Repository path.
     * @return The Lookup.
     */
    public Lookup lookup(int version, String path);
    
    /**
     * Get a lookup object for a path.  Directory only.
     * @param version The version to look under.
     * @param path The Repository path.
     * @return The Lookup.
     */
    public Lookup lookupDirectory(int version, String path);
    
    /**
     * Get the indirection path of a layered node.
     * @param version The version id.
     * @param path The Repository path.
     * @return The indirection.
     */
    public String getIndirectionPath(int version, String path);
}
