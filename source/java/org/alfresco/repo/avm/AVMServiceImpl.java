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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.log4j.Logger;

/**
 * Implements the AVMService.
 * @author britt
 */
public class AVMServiceImpl implements AVMService
{
    private static Logger fgLogger = Logger.getLogger(AVMServiceImpl.class);
    
    /**
     * The AVMRepository for each service thread.
     */
    private AVMRepository fAVMRepository;
    
    /**
     * Basic constructor for the service.
     */
    public AVMServiceImpl()
    {
    }
    
    /**
     * Set the repository reference. For Spring.
     * @param avmRepository The repository reference.
     */
    public void setAvmRepository(AVMRepository avmRepository)
    {
        fAVMRepository = avmRepository;
    }

    /**
     * Get an InputStream from a file.
     * @param version The version to look under.
     * @param path The absolute path.
     * @return An InputStream
     * @throws AVMNotFoundException When the path is invalid.
     */
    public InputStream getFileInputStream(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getInputStream(version, path);
    }

    /**
     * Get an output stream to a file. Triggers versioning.
     */
    public OutputStream getFileOutputStream(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getOutputStream(path);
    }

    /**
     * Get a directory listing.
     * @param version The version id to lookup.
     * @param path The path to lookup.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getListing(version, path);
    }

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
        getDirectoryListingDirect(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getListingDirect(version, path);
    }

    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir)
    {
        if (dir == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        return fAVMRepository.getListing(dir);
    }

    /**
     * Get the names of nodes that have been deleted in a directory.
     * @param version The version to look under.
     * @param path The path of the directory.
     * @return A List of names.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains any elements
     * that are not directories.
     */
    public List<String> getDeleted(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getDeleted(version, path);
    }

    /**
     * Create a new file. The file must not exist.
     * @param path The path to the containing directory.
     * @param name The name of the file.
     * @return An output stream to the file.
     */
    public OutputStream createFile(String path, String name)
    {
        if (path == null || name == null || !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        return fAVMRepository.createFile(path, name);
    }

    /**
     * Create a file with content specified by the InputStream.
     * Guaranteed to be created atomically.
     * @param path The path to the containing directory.
     * @param name The name to give the file.
     * @param in An InputStream containing data for file.
     */
    public void createFile(String path, String name, InputStream in)
    {
        if (path == null || name == null || in == null || !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        // Save the contents to temp space.
        File temp;
        try
        {
            temp = TempFileProvider.createTempFile("alf", "tmp");
            OutputStream out = new FileOutputStream(temp);
            byte [] buff = new byte[8192];
            int read;
            while ((read = in.read(buff)) != -1)
            {
                out.write(buff, 0, read);
            }
            out.close();
            in.close();
        }
        catch (IOException ie)
        {
            throw new AVMException("I/O Error.");
        }
        try
        {
            fAVMRepository.createFile(path, name, temp);
        }
        finally
        {
            temp.delete();
        }
    }

    /**
     * Create a directory. The directory must not exist.
     * @param path The path to the containing directory.
     * @param name The name of the new directory.
     */
    public void createDirectory(String path, String name)
    {
        if (path == null || name == null || !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.createDirectory(path, name);
    }

    /**
     * Create a new layered file.  It must not exist.
     * @param srcPath The src path.  Ie the target for the layering.
     * @param parent The path to the parent directory.
     * @param name The name to give the new file.
     */
    public void createLayeredFile(String srcPath, String parent, String name)
    {
        if (srcPath == null || parent == null || name == null || 
            !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.createLayeredFile(srcPath, parent, name);
    }

    /**
     * Create a new layered directory.  It must not exist.
     * @param srcPath The src path. Ie the target for layering.
     * @param parent The path to the parent directory.
     * @param name The name for the new directory.
     */
    public void createLayeredDirectory(String srcPath, String parent, String name)
    {
        if (srcPath == null || parent == null || name == null ||
            !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.createLayeredDirectory(srcPath, parent, name);
    }

    /**
     * Create an AVMStore with the given name.  It must not exist.
     * @param name The name to give the AVMStore.   
     */
    public void createAVMStore(String name)
    {
        if (name == null || !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Bad Name.");
        }
        fAVMRepository.createAVMStore(name);
    }

    /**
     * Create a branch.
     * @param version The version to branch from.
     * @param srcPath The path to the thing to branch from.
     * @param dstPath The path to the destination containing directory.
     * @param name The name of the new branch.
     */
    public void createBranch(int version, String srcPath, String dstPath,
            String name)
    {
        if (srcPath == null || dstPath == null || name == null ||
            !FileNameValidator.IsValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.createBranch(version, srcPath, dstPath, name);
    }

    /**
     * Remove a node. Beware, the node can be a directory and 
     * this acts recursively.
     * @param parent The path to the parent.
     * @param name The name of the node to remove.
     */
    public void removeNode(String parent, String name)
    {
        if (parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.remove(parent, name);
    }

    /**
     * Rename a node.
     * @param srcParent The path to the source parent.
     * @param srcName The name of the source node.
     * @param dstParent The path to the destination parent.
     * @param dstName The name to give the renamed node.
     */
    public void rename(String srcParent, String srcName, String dstParent,
            String dstName)
    {
        if (srcParent == null || srcName == null || dstParent == null || dstName == null ||
            !FileNameValidator.IsValid(dstName))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.rename(srcParent, srcName, dstParent, dstName);
    }

    /**
     * Uncover a deleted name in a layered directory.
     * @param dirPath The path to the layered directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name)
    {
        if (dirPath == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.uncover(dirPath, name);
    }

    /**
     * Get the Latest Version ID for an AVMStore.
     * @param repName The name of the AVMStore.
     * @return The Latest Version ID.
     */
    public int getLatestVersionID(String repName)
    {
        if (repName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getLatestVersionID(repName);
    }

    /**
     * Create snapshots of a group of AVMStores.
     * @param stores A List of AVMStore names.
     * @return A List of the new version ids.
     */
    public List<Integer> createSnapshot(List<String> stores)
    {
        if (stores == null)
        {
            throw new AVMBadArgumentException("Stores is null.");
        }
        return fAVMRepository.createSnapshot(stores);
    }

    /**
     * Snapshot an AVMRepository.
     * @param store The name of the AVMStore.
     * @return The id of the new version.
     */
    public int createSnapshot(String store)
    {
        if (store == null)
        {
            throw new AVMBadArgumentException("Store is null.");
        }
        return fAVMRepository.createSnapshot(store);
    }

    /**
     * Look up information about a node.
     * @param version The version to look up.
     * @param path The path to look up.
     * @return A Descriptor.
     */
    public AVMNodeDescriptor lookup(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        Lookup lookup = fAVMRepository.lookup(version, path);
        return lookup.getCurrentNode().getDescriptor(lookup);
    }

    /**
     * Lookup a node descriptor from a directory node descriptor.
     * @param dir The node descriptor of the directory.
     * @param name The name to lookup.
     * @return The node descriptor of the child.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name)
    {
        if (dir == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.lookup(dir, name);
    }

    /**
     * Purge an AVMStore.  Permanently delete everything that 
     * is only referenced in that AVMStore.
     * @param name The name of the AVMStore to purge.
     */
    public void purgeAVMStore(String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        fAVMRepository.purgeAVMStore(name);
    }

    /**
     * Purge a particular version from a repository.
     * @param version The id of the version to purge.
     * @param name The name of the repository.
     */
    public void purgeVersion(int version, String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        fAVMRepository.purgeVersion(name, version);
    }

    /**
     * Get the indirection path of a layered node.
     * @param version The version to lookup.
     * @param path The path to lookup.
     * @return The indirection path (target) of the layered node.
     */
    public String getIndirectionPath(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        return fAVMRepository.getIndirectionPath(version, path);
    }

    /**
     * Get the extant version ids for an AVMStore.
     * @param name The name of the AVMStore.
     * @return A List of VersionDescriptors.
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        return fAVMRepository.getAVMStoreVersions(name);
    }

    /**
     * Get version IDs by creation date.  From or to may be null but not
     * both.
     * @param name The name of the AVMStore to search.
     * @param from The earliest versions to return.
     * @param to The latest versions to return.
     * @return The Set of matching version IDs.
     */
    public List<VersionDescriptor> getAVMStoreVersions(String name, Date from, Date to)
    {
        if (name == null || (from == null && to == null))
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getAVMStoreVersions(name, from, to);
    }

    /**
     * Change what a layered directory points to.
     * @param path The path to the layered directory.
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        if (path == null || target == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.retargetLayeredDirectory(path, target);
    }

    /**
     * Make the indicated directory a primary indirection.
     * @param path The absolute path.
     */
    public void makePrimary(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        fAVMRepository.makePrimary(path);
    }

    /**
     * Get a list of all AVMStores.
     * @return The AVMStores.
     */
    public List<AVMStoreDescriptor> getAVMStores()
    {
        return fAVMRepository.getAVMStores();
    }

    /**
     * Get a reposotory.
     * @param name The name of the AVMStore to get.
     * @return The AVMStore.
     */
    public AVMStoreDescriptor getAVMStore(String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Null Store Name.");
        }
        return fAVMRepository.getAVMStore(name);
    }

    /**
     * Get a descriptor for the specified AVMStore root.
     * @param version The version to get.
     * @param name The name of the AVMStore.
     * @return The root descriptor.
     */
    public AVMNodeDescriptor getAVMStoreRoot(int version, String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        return fAVMRepository.getAVMStoreRoot(version, name);
    }

    /**
     * Get the history of a node.
     * @param desc The node to get history from.
     * @param count The number of ancestors to fallow back. -1 means all.
     * @return A List of ancestors most recent first.
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        return fAVMRepository.getHistory(desc, count);
    }

    /**
     * Set the opacity of a layered directory.  An opaque layer hides what
     * its indirection points to.
     * @param path The path to the layered directory.
     * @param opacity True is opaque false is not.
     */
    public void setOpacity(String path, boolean opacity)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        fAVMRepository.setOpacity(path, opacity);
    }

    /**
     * Get layering information about a path.
     * @param version The version to look under.
     * @param path The full AVM path.
     * @return A LayeringDescriptor.
     */
    public LayeringDescriptor getLayeringInfo(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path: " + path);
        }
        return fAVMRepository.getLayeringInfo(version, path);
    }

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
                                               AVMNodeDescriptor right)
    {
        if (left == null || right == null)
        {
            throw new AVMBadArgumentException("Null node descriptor.");
        }
        return fAVMRepository.getCommonAncestor(left, right);
    }

    /**
     * Set a property on a node.
     * @param path The path to the node to set the property on. 
     * @param name The QName of the property.
     * @param value The property to set.
     */
    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        if (path == null || name == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.setNodeProperty(path, name, value);
    }

    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of properties to set.
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties)
    {
        if (path == null || properties == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.setNodeProperties(path, properties);
    }

    /**
     * Get a property of a node by QName.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The QName.
     * @return The PropertyValue or null if it doesn't exist.
     */
    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getNodeProperty(version, path, name);
    }
    
    /**
     * Get all the properties associated with a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of AVMNodeProperties.
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getNodeProperties(version, path);
    }

    /**
     * Delete a property.
     * @param path The path to the node.
     * @param name The QName of the property to delete.
     */
    public void deleteNodeProperty(String path, QName name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.deleteNodeProperty(path, name);
    }

    /**
     * Delete all the properties attached to an AVM node.
     * @param path The path to the node.
     */
    public void deleteNodeProperties(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        fAVMRepository.deleteNodeProperties(path);
    }

    /**
     * Set a property on a store. If the property exists it will be overwritten.
     * @param store The store to set the property on.
     * @param name The name of the property.
     * @param value The value of the property.
     */
    public void setStoreProperty(String store, QName name, PropertyValue value)
    {
        if (store == null || name == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.setStoreProperty(store, name, value);
    }
    
    /**
     * Set a group of properties on a store. Existing properties will be overwritten.
     * @param store The name of the store.
     * @param props A Map of the properties to set.
     */
    public void setStoreProperties(String store, Map<QName, PropertyValue> props)
    {
        if (store == null || props == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.setStoreProperties(store, props);
    }
    
    /**
     * Get a property from a store.
     * @param store The name of the store.
     * @param name The name of the property.
     * @return A PropertyValue or null if non-existent.
     */
    public PropertyValue getStoreProperty(String store, QName name)
    {
        if (store == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getStoreProperty(store, name);
    }
    
    /**
     * Get all the properties associated with a store.
     * @param store The name of the store.
     * @return A Map of the stores properties.
     */
    public Map<QName, PropertyValue> getStoreProperties(String store)
    {
        if (store == null)
        {
            throw new AVMBadArgumentException("Null store name.");
        }
        return fAVMRepository.getStoreProperties(store);
    }
    
    /**
     * Delete a property on a store by name.
     * @param store The name of the store.
     * @param name The name of the property to delete.
     */
    public void deleteStoreProperty(String store, QName name)
    {
        if (store == null || name == null)
        {
            throw new AVMBadArgumentException("Invalid null argument.");
        }
        fAVMRepository.deleteStoreProperty(store, name);
    }
    
    /**
     * Get the ContentData for a node. Only applies to a file.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The ContentData object.
     */
    public ContentData getContentDataForRead(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Path.");
        }
        return fAVMRepository.getContentDataForRead(version, path);
    }
    
    /**
     * Get the Content data for writing.
     * @param path The path to the node.
     * @return The ContentData object.
     */
    public ContentData getContentDataForWrite(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Path.");
        }
        return fAVMRepository.getContentDataForWrite(path);
    }

    /**
     * Set the content data on a file. 
     * @param path The path to the file.
     * @param data The ContentData to set.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> does not point
     * to a file.
     */
    public void setContentData(String path, ContentData data)
    {
        if (path == null || data == null)
        {
            throw new AVMBadArgumentException("Null Path.");
        }
        fAVMRepository.setContentData(path, data);
    }

    /**
     * Add an aspect to an AVM node.
     * @param path The path to the node.
     * @param aspectName The QName of the aspect.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMExistsException If the aspect already exists.
     */
    public void addAspect(String path, QName aspectName)
    {
        if (path == null || aspectName == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.addAspect(path, aspectName);
    }
    
    /**
     * Get all the aspects on an AVM node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of the QNames of the aspects.
     */
    public List<QName> getAspects(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getAspects(version, path);
    }

    /**
     * Remove an aspect and its properties from a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     */
    public void removeAspect(String path, QName aspectName)
    {
        if (path == null || aspectName == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        fAVMRepository.removeAspect(path, aspectName);
    }
    
    /**
     * Does a node have a particular aspect.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param aspectName The aspect name to check.
     * @return Whether the given node has the given aspect.
     */
    public boolean hasAspect(int version, String path, QName aspectName)
    {
        if (path == null || aspectName == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        return fAVMRepository.hasAspect(version, path, aspectName);
    }
}
