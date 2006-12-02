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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;

/**
 * The store interface.  Methods for filesystem like, versioning,
 * and layering operations.
 * @author britt
 */
public interface AVMStore
{
    /**
     * Get the primary key.
     */
    public long getId();
    
    /**
     * This returns the next version in this store that will be snapshotted.
     * @return The next version to be snapshotted.
     */
    public int getNextVersionID();
    
    /**
     * This gets the last extant version id.
     */
    public int getLastVersionID();

    /**
     * Set a new root for this store.
     * @param root The root to set.
     */
    public void setNewRoot(DirectoryNode root);

    /**
     * Snapshots this store.  This sets all nodes in the
     * the store to the should be copied state, and creates
     * a new version root.
     * @param tag The short description.
     * @param description The long description.
     * @return The version id of the newly created snapshot.
     */
    public int createSnapshot(String tag, String Description);

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
     * @return An OutputStream.
     */
    public OutputStream createFile(String path, String name);
    
    /**
     * Create a file with the given contents.
     * @param path The path to the containing directory.
     * @param name The name to give the file.
     * @param data The contents of the file.
     */
    public void createFile(String path, String name, File data);

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
     * Get a ContentReader from a file.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return A ContentReader
     */
    public ContentReader getContentReader(int version, String path);    
    
    /**
     * Get a listing of the designated directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @param includeDeleted Whether to see Deleted nodes.
     * @return A listing.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(int version, String path, 
                                                           boolean includeDeleted);
    
    /**
     * Get the list of nodes directly contained in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @param includeDeleted Whether to see Deleted nodes.
     * @return A Map of names to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListingDirect(int version, String path,
                                                                 boolean includeDeleted);

    /**
     * Get the names of the deleted nodes in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of names.
     */
    public List<String> getDeleted(int version, String path);
    
    /**
     * Get an output stream to a file.
     * @param path The path to the file.
     * @return An OutputStream
     */
    public OutputStream getOutputStream(String path);
    
    /**
     * Get a ContentWriter to a file.
     * @param path The path to the file.
     * @return A ContentWriter.
     */
    public ContentWriter createContentWriter(String path);
    
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
     * Get all the version for this AVMStore.
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
     * Get the AVMRepository.
     * @return The AVMRepository.
     */
    public AVMRepository getAVMRepository();

    /**
     * Lookup a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param write Whether this is in a write context.
     * @param includeDeleted Whether to see Deleted nodes.
     * @return A Lookup object.
     */
    public Lookup lookup(int version, String path, boolean write, boolean includeDeleted);

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
     * @param path The AVMRepository relative path.
     */
    public void makePrimary(String path);

    /**
     * Change the target of a layered directory.
     * @param path The path to the layered directory.
     * @param target The new target path.
     */
    public void retargetLayeredDirectory(String path, String target);
    
    /**
     * Get the root directory of this AVMStore.
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
     * Get the name of this store.
     * @return The name.
     */
    public String getName();
    
    /**
     * Set the name of the store.
     * @param name To Set.
     */
    public void setName(String name);
    
    /**
     * Purge all the nodes reachable only by the given version.
     * @param version
     */
    public void purgeVersion(int version);
    
    /**
     * Get the descriptor for this.
     * @return The descriptor.
     */
    public AVMStoreDescriptor getDescriptor();
    
    /**
     * Set the opacity of a layered directory. An opaque directory hides
     * what is pointed at by its indirection.
     * @param path The path to the layered directory.
     * @param opacity True is opaque; false is not.
     */
    public void setOpacity(String path, boolean opacity);
    
    /**
     * Set a property on a node.
     * @param path The path to the node.
     * @param name The name of the property.
     * @param value The value to set.
     */
    public void setNodeProperty(String path, QName name, PropertyValue value);
    
    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of QNames to PropertyValues.
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties);
    
    /**
     * Get a property by name.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The name of the property.
     * @return A PropertyValue or null if not found.
     */
    public PropertyValue getNodeProperty(int version, String path, QName name);
    
    /**
     * Delete a single property from a node.
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
     * Get all the properties associated with a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path);
    
    /**
     * Set a property on this store. Replaces if property already exists.
     * @param name The QName of the property.
     * @param value The actual PropertyValue.
     */
    public void setProperty(QName name, PropertyValue value);
    
    /**
     * Set a group of properties on this store. Replaces any property that exists.
     * @param properties A Map of QNames to PropertyValues to set.
     */
    public void setProperties(Map<QName, PropertyValue> properties);
    
    /**
     * Get a property by name.
     * @param name The QName of the property to fetch.
     * @return The PropertyValue or null if non-existent.
     */
    public PropertyValue getProperty(QName name);
    
    /**
     * Get all the properties associated with this node.
     * @return A Map of the properties.
     */
    public Map<QName, PropertyValue> getProperties();
    
    /**
     * Delete a property.
     * @param name The name of the property to delete.
     */
    public void deleteProperty(QName name);
    
    /**
     * Get the ContentData on a file.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return The ContentData corresponding to the file.
     */
    public ContentData getContentDataForRead(int version, String path);
    
    /**
     * Get the ContentData for writing.
     * @param path The path to the file.
     * @return The ContentData object.
     */
    public ContentData getContentDataForWrite(String path);
    
    /**
     * Set the ContentData for a file.
     * @param path The path to the file.
     * @param data The ContentData to set.
     */
    public void setContentData(String path, ContentData data);
    
    /**
     * Set meta data, aspects, properties, acls, from another node.
     * @param path The path to the node to set metadata on.
     * @param from The node to get the metadata from.
     */
    public void setMetaDataFrom(String path, AVMNode from);
    
    /**
     * Add an aspect to a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     */
    public void addAspect(String path, QName aspectName);
    
    /**
     * Get all aspects on a given node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of the QNames of the aspects.
     */
    public List<QName> getAspects(int version, String path);
    
    /**
     * Remove an aspect and all its properties from a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     */
    public void removeAspect(String path, QName aspectName);
    
    /**
     * Does a given node have a given aspect.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     * @return Whether the node has the aspect.
     */
    public boolean hasAspect(int version, String path, QName aspectName);
    
    /**
     * Set the ACL on a node.
     * @param path The path to the node.
     * @param acl The ACL to set.
     */
    public void setACL(String path, DbAccessControlList acl);
    
    /**
     * Get the ACL on a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The ACL.
     */
    public DbAccessControlList getACL(int version, String path);
    
    /**
     * Link a node intro a directory, directly.
     * @param parentPath The path to the directory.
     * @param name The name to give the node.
     * @param toLink The node to link.
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink);
}