/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMCycleException;
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
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileNameValidator;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the AVMService.
 * @author britt
 */
public class AVMServiceImpl implements AVMService
{
    /** The i18n'ized messages */
    private static final String MSG_CYCLE_ON_CREATE = "avm.cycle.create";
    
    public static final String SYSTEM = "system";
    
    private static Log logger = LogFactory.getLog(AVMServiceImpl.class);
    
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
     * Get an InputStream from a descriptor.
     * @param desc The descriptor.
     * @return An InputStream.
     * @throws AVMNotFoundException
     */
    public InputStream getFileInputStream(AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        return fAVMRepository.getInputStream(desc);
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
     * Get a content reader from a file node.
     * @param version The version of the file.
     * @param path The path to the file.
     * @return A ContentReader.
     */
    public ContentReader getContentReader(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getContentReader(version, path);
    }
    
    /**
     * Get a ContentWriter to a file node.
     * @param path The path to the file.
     * @param update true if the property must be updated atomically when the content write
     *      stream is closed (attaches a listener to the stream); false if the client code
     *      will perform the updates itself.
     * @return A ContentWriter.
     */
    public ContentWriter getContentWriter(String path, boolean update)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.createContentWriter(path, update);
    }

    /**
     * Get a directory listing.
     * @param version The version id to lookup.
     * @param path The path to lookup.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path)
    {
        return getDirectoryListing(version, path, false);
    }

    /**
     * Get a listing of a Folder by name, with the option of seeing
     * Deleted Nodes.
     * @param version The version id to look in.
     * @param path The simple absolute path to the file node.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException If <code>path</code> is not found.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal
     * component that is not a directory, or if <code>path</code> is not pointing
     * at a directory.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(int version, String path,
                                                                    boolean includeDeleted)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getListing(version, path, includeDeleted);        
    }

    /**
     * Get a directory listing as an Array of AVMNodeDescriptors.
     * @param version The version to look under.
     * @param path The path to the directory to be listed.
     * @param includeDeleted Whether to include ghosts.
     * @return An array of AVMNodeDescriptors.
     */
    public AVMNodeDescriptor [] getDirectoryListingArray(int version, String path,
                                                         boolean includeDeleted)
    {
        Map<String, AVMNodeDescriptor> listing =
            getDirectoryListing(version, path, includeDeleted);
        AVMNodeDescriptor [] result = new AVMNodeDescriptor[listing.size()];
        int off = 0;
        for (AVMNodeDescriptor desc : listing.values())
        {
            result[off++] = desc;
        }
        return result;
    }

    /**
     * Get a directory listing as an Array of node descriptors.
     * @param dir The descriptor pointing at the directory to list.
     * @param includeDeleted Whether to show ghosts.
     * @return An array of AVMNodeDescriptors.
     */
    public AVMNodeDescriptor [] getDirectoryListingArray(AVMNodeDescriptor dir,
                                                         boolean includeDeleted)
    {
        Map<String, AVMNodeDescriptor> listing = 
            getDirectoryListing(dir, includeDeleted);
        AVMNodeDescriptor [] result = new AVMNodeDescriptor[listing.size()];
        int off = 0;
        for (AVMNodeDescriptor desc : listing.values())
        {
            result[off++] = desc;
        }
        return result;
    }

    /**
     * Get a listing of all the directly contained children of a directory.
     * @param dir The directory descriptor.
     * @param includeDeleted Whether to include deleted children.
     * @return A Map of Strings to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        if (dir == null)
        {
            throw new AVMBadArgumentException("Illegal null descriptor.");
        }
        return fAVMRepository.getListingDirect(dir, includeDeleted);
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
        return getDirectoryListingDirect(version, path, false);
    }
    
    /**
     * Get the listing of nodes contained directly in a directory. This is the
     * same as getDirectoryListing for PlainDirectories, but returns only those that
     * are directly contained in a layered directory. This has the option of
     * seeing Deleted Nodes.
     * @param version The version to look up.
     * @param path The full path to get listing for.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return A Map of names to descriptors.
     * @throws AVMNotFoundException If <code>path</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains any non-directory
     * elements.
     */
    public SortedMap<String, AVMNodeDescriptor>
        getDirectoryListingDirect(int version, String path, boolean includeDeleted)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getListingDirect(version, path, includeDeleted);
    }
    
    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir)
    {
        return getDirectoryListing(dir, false);
    }


    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, String childPattern)
    {
        return getDirectoryListing(dir, childPattern, false);
    }

    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(AVMNodeDescriptor dir, String childNamePattern, boolean includeDeleted)
    {
        if (dir == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        return fAVMRepository.getListing(dir, childNamePattern, includeDeleted);     
    }
    
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
                                                                    boolean includeDeleted)
    {
        if (dir == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        return fAVMRepository.getListing(dir, includeDeleted);        
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
        if (path == null || name == null || !FileNameValidator.isValid(name))
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
        createFile(path, name, in, null, null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createFile(java.lang.String, java.lang.String, java.io.InputStream, java.util.List, java.util.Map)
     */
    public void createFile(String path, String name, InputStream in, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        if (path == null || name == null || in == null)
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        
        if(!FileNameValidator.isValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument, name:" + name);
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
            fAVMRepository.createFile(path, name, temp, aspects, properties);
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
        createDirectory(path, name, null, null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#createDirectory(java.lang.String, java.lang.String, java.util.List, java.util.Map)
     */
    public void createDirectory(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        if (path == null || name == null || !FileNameValidator.isValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.createDirectory(path, name, aspects, properties);
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
            !FileNameValidator.isValid(name))
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
            !FileNameValidator.isValid(name))
        {
            throw new AVMBadArgumentException("Illegal argument.");
        }
        fAVMRepository.createLayeredDirectory(srcPath, parent, name);
        
        // check for cycle (note: optimised to skip when creating WCM sandbox layer)
        String[] pathParts = AVMUtil.splitPath(parent);
        if ((WCMUtil.getWebProject(this, pathParts[0]) == null) || 
            (! (pathParts[1].equals("/") && name.equals(JNDIConstants.DIR_DEFAULT_WWW))))
        {
            long start = System.currentTimeMillis();
            
            if (checkForLDCycle(srcPath, AVMUtil.extendAVMPath(parent, name)))
            {
                throw new AVMCycleException(MSG_CYCLE_ON_CREATE);
            }
            
            if (logger.isDebugEnabled())
            {
                logger.debug("createLayeredDirectory: cycle check: "+parent+"/"+name+" -> "+srcPath+" (in "+(System.currentTimeMillis()-start)+" msecs)");
            }
        }
    }
    
    private boolean checkForLDCycle(String srcPath, String dstDirPath)
    {
        boolean found = false;
        SortedMap<String, AVMNodeDescriptor> listing = getDirectoryListing(-1, dstDirPath, false);
        for (AVMNodeDescriptor node : listing.values())
        {
            if (node.isDirectory())
            {
                if (node.isLayeredDirectory() && node.isPrimary() && node.getIndirection().equals(srcPath))
                {
                    return true;
                }
                if (checkForLDCycle(srcPath, node.getPath()))
                {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
    
    /**
     * Create an AVMStore with the given name (it must not exist).
     * @param name The name to give the AVMStore.   
     */
    public void createStore(String name)
    {
        if (name == null || !FileNameValidator.isValid(name))
        {
            throw new AVMBadArgumentException("Bad Name.");
        }
        fAVMRepository.createAVMStore(name);
    }
    
    /**
     * Create an AVMStore with the given name (it must not exist) and set store properties.
     * @param name The name to give the AVMStore.
     * @param props A Map of the properties to set.
     */
    public void createStore(String name, Map<QName, PropertyValue> props)
    {
        if (name == null || !FileNameValidator.isValid(name))
        {
            throw new AVMBadArgumentException("Bad Name.");
        }
        fAVMRepository.createAVMStore(name, props);
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
            !FileNameValidator.isValid(name))
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
     * Remove a node by full path.
     * @param path The full path to the node.
     */
    public void removeNode(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        String [] basePath = AVMNodeConverter.SplitBase(path);
        if (basePath[0] == null)
        {
            throw new AVMBadArgumentException("Cannot remove root node: " + path);
        }
        fAVMRepository.remove(basePath[0], basePath[1]);
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
            !FileNameValidator.isValid(dstName))
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
     * Make name in dirPath transparent to what was underneath it. That is, this
     * removes the offending node from its layered directory parent's direct ownership.
     * @param dirPath The path to the layered directory.
     * @param name The name of the item to flatten.
     */
    public void makeTransparent(String dirPath, String name)
    {
        if (dirPath == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.flatten(dirPath, name);
    }

    /**
     * Get the Latest Version ID for an AVMStore.
     * @param repName The name of the AVMStore.
     * @return The Latest Version ID.
     */
    public int getNextVersionID(String repName)
    {
        if (repName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getLatestVersionID(repName);
    }

    /**
     * Get the latest snapshot id of a store. 
     * @param storeName The store name.
     * @return The id of the latest extant version of the store.
     * @throws AVMNotFoundException If <code>storeName</code> does not exist.
     */
    public int getLatestSnapshotID(String storeName)
    {
        if (storeName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getLatestSnapshotID(storeName);
    }

    /*
     * Snapshot an AVMRepository.
     * @param store The name of the AVMStore.
     * @param tag The short description.
     * @param description The thick description.
     * @return Map of implicitly and explicitly snapshotted stores.
     */
    public Map<String, Integer> createSnapshot(String store, String tag, String description)
    {
        if (store == null)
        {
            throw new AVMBadArgumentException("Store is null.");
        }
        return fAVMRepository.createSnapshot(store, tag, description);
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
            throw new AVMBadArgumentException("Illegal null path.");
        }
        return lookup(version, path, false);
    }

    /**
     * Lookup a node by version ids and path, with the option of 
     * seeing Deleted Nodes.
     * @param version The version id to look under.
     * @param path The simple absolute path to the parent directory.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return An AVMNodeDescriptor.
     * @throws AVMNotFoundException If <code>path</code> does not exist or
     * if <code>version</code> does not exist.
     * @throws AVMWrongTypeException If <code>path</code> contains a non-terminal 
     * element that is not a directory.
     */
    public AVMNodeDescriptor lookup(int version, String path, boolean includeDeleted)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        try
        {
            Lookup lookup = fAVMRepository.lookup(version, path, includeDeleted);
            if (lookup == null)
            {
                return null;
            }
            return lookup.getCurrentNode().getDescriptor(lookup);
        }
        catch (AVMNotFoundException e)
        {
            return null;
        }
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
        return lookup(dir, name, false);
    }
    
    /**
     * Lookup a node from a directory node, with the option of seeing 
     * Deleted Nodes.
     * @param dir The descriptor for the directory node.
     * @param name The name to lookup.
     * @param includeDeleted Whether to see Deleted Nodes.
     * @return The descriptor for the child.
     * @throws AVMNotFoundException If <code>name</code> does not exist or
     * if <code>dir</code> is dangling.
     * @throws AVMWrongTypeException If <code>dir</code> does not refer to a directory.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name, boolean includeDeleted)
    {
        if (dir == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        try
        {
            return fAVMRepository.lookup(dir, name, includeDeleted);
        }
        catch (AVMNotFoundException e)
        {
            return null;
        }
    }
    
    /**
     * Get a list of all paths that a given node has.
     * @param desc The node descriptor to get paths for.
     * @return A List of version, path Pairs.
     */
    public List<Pair<Integer, String>> getPaths(AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Descriptor is null.");
        }
        return fAVMRepository.getPaths(desc);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAPath(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Pair<Integer, String> getAPath(AVMNodeDescriptor desc) 
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Descriptor is null.");
        }
        return fAVMRepository.getAPath(desc);
    }

    /**
     * Get all paths that a given node has that are in the head version.
     * @param desc The node descriptor to get paths for.
     * @return A List of version, path Pairs.
     */
    public List<Pair<Integer, String>> getHeadPaths(AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Descriptor is null.");
        }
        return fAVMRepository.getHeadPaths(desc);
    }

    /**
     * Get all paths to a node starting at the HEAD version of a store.
     * @param desc The node descriptor.
     * @param store The store.
     * @return A List of all paths meeting the criteria.
     */
    public List<Pair<Integer, String>> getPathsInStoreHead(AVMNodeDescriptor desc, String store)
    {
        if (desc == null || store == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getPathsInStoreHead(desc, store);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getPathsInStoreVersion(org.alfresco.service.cmr.avm.AVMNodeDescriptor, java.lang.String, int)
     */
    public List<String> getPathsInStoreVersion(AVMNodeDescriptor desc, String store, int version)
    {
        if (desc == null || store == null || version < 1)
        {
            throw new AVMBadArgumentException("Illegal null argument or invalid version.");
        }
        return fAVMRepository.getPathsInStoreVersion(desc, store, version);
    }

    /**
     * Purge an AVMStore.  Permanently delete everything that 
     * is only referenced in that AVMStore.
     * @param name The name of the AVMStore to purge.
     */
    public void purgeStore(String name)
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
    public List<VersionDescriptor> getStoreVersions(String name)
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
    public List<VersionDescriptor> getStoreVersions(String name, Date from, Date to)
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
    public List<AVMStoreDescriptor> getStores()
    {
        return fAVMRepository.getAVMStores();
    }

    /**
     * Get a reposotory.
     * @param name The name of the AVMStore to get.
     * @return The AVMStore.
     */
    public AVMStoreDescriptor getStore(String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Null Store Name.");
        }
        try
        {
            return fAVMRepository.getAVMStore(name);
        }
        catch (AVMNotFoundException e)
        {
            return null;
        }
    }

    /**
     * Get (and create if necessary) the system store. This store houses things
     * like workflow packages.
     * @return The descriptor.
     */
    public AVMStoreDescriptor getSystemStore()
    {
        AVMStoreDescriptor store = getStore(SYSTEM);
        if (store == null)
        {
            createStore(SYSTEM);
            return getStore(SYSTEM);
        }
        return store;
    }
    
    /**
     * Get a descriptor for the specified AVMStore root.
     * @param version The version to get.
     * @param name The name of the AVMStore.
     * @return The root descriptor.
     */
    public AVMNodeDescriptor getStoreRoot(int version, String name)
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

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getNodeProperties(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Map<QName, PropertyValue> getNodeProperties(AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        return fAVMRepository.getNodeProperties(desc);
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
     * Queries a given store for properties with keys that match a given pattern. 
     * @param store The name of the store.
     * @param keyPattern The sql 'like' pattern, inserted into a QName.
     * @return A Map of the matching key value pairs.
     */
    public Map<QName, PropertyValue> queryStorePropertyKey(String store, QName keyPattern)
    {
        if (store == null || keyPattern == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.queryStorePropertyKey(store, keyPattern);
    }

    /**
     * Queries all AVM stores for properties with keys that matcha given pattern. 
     * @param keyPattern The sql 'like' pattern, inserted into a QName.
     * @return A List of Pairs of Store name, Map.Entry.
     */
    public Map<String, Map<QName, PropertyValue>>
        queryStoresPropertyKeys(QName keyPattern)
    {
        if (keyPattern == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.queryStoresPropertyKeys(keyPattern);
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
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getContentDataForRead(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public ContentData getContentDataForRead(AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        return fAVMRepository.getContentDataForRead(desc);
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
     * Set all metadata on a node from another node. Aspects, properties, ACLs.
     * @param path The path to the node to set.
     * @param from The descriptor for the node to get metadata from.
     */
    public void setMetaDataFrom(String path, AVMNodeDescriptor from)
    {
        if (path == null || from == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        fAVMRepository.setMetaDataFrom(path, from);
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
    public Set<QName> getAspects(int version, String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        return fAVMRepository.getAspects(version, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getAspects(org.alfresco.service.cmr.avm.AVMNodeDescriptor)
     */
    public Set<QName> getAspects(AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Null descriptor: " + desc);
        }
        return fAVMRepository.getAspects(desc);
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
    
    /**
     * This inserts a node into a parent directly.
     * @param parentPath The path to the parent directory.
     * @param name The name to give the node.
     * @param toLink A descriptor for the node to insert.
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        if (parentPath == null || name == null || toLink == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.link(parentPath, name, toLink);
    }
    
    /**
     * This replaces a node into a parent directly.
     * @param parentPath The path to the parent directory.
     * @param name The name to give the node.
     * @param toLink A descriptor for the node to insert.
     */
    public void updateLink(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        if (parentPath == null || name == null || toLink == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.updateLink(parentPath, name, toLink);
    }

    /**
     * Force copy on write of a path.
     * @param path The path to force.
     */
    public AVMNodeDescriptor forceCopy(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Path.");
        }
        return fAVMRepository.forceCopy(path);
    }
    
    /**
     * Copy (possibly recursively) the source into the destination
     * directory.
     * @param srcVersion The version of the source.
     * @param srcPath The path to the source.
     * @param dstPath The destination directory.
     * @param name The name to give the copy.
     */
    public void copy(int srcVersion, String srcPath, String dstPath, String name)
    {
        if (srcPath == null || dstPath == null)
        {
            throw new AVMBadArgumentException("Null Path.");
        }
        if (srcVersion < 0)
        {
            String canonicalSrc = 
                AVMNodeConverter.ToAVMVersionPath(
                        AVMNodeConverter.ToNodeRef(srcVersion, srcPath)).getSecond();
            String canonicalDst =
                AVMNodeConverter.ToAVMVersionPath(
                        AVMNodeConverter.ToNodeRef(-1, dstPath)).getSecond();
            if (!canonicalSrc.endsWith("/"))
            {
                canonicalSrc = canonicalSrc + "/";
            }
            if (canonicalDst.indexOf(canonicalSrc) == 0)
            {
                throw new AVMBadArgumentException("Infinite Copy.");
            }
        }
        if (!FileNameValidator.isValid(name))
        {
            throw new AVMBadArgumentException("Illegal name.");
        }
        AVMNodeDescriptor srcDesc = lookup(srcVersion, srcPath);
        recursiveCopy(srcVersion, srcDesc, dstPath, name);
    }
    
    
    private Acl getAclAsSystem(final int version, final String path)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Acl>(){

            public Acl doWork() throws Exception
            {
                return fAVMRepository.getACL(version, path);
            }}, AuthenticationUtil.getSystemUserName());
    }
    
    private void setAclAsSystem(final String path, final Acl acl)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>(){

            public Object doWork() throws Exception
            {
                fAVMRepository.setACL(path, acl);
                return null;
            }}, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Do the actual work of copying.
     * @param desc The src descriptor.
     * @param path The destination parent path.
     * @param name The name to give the copy.
     */
    private void recursiveCopy(int version, AVMNodeDescriptor desc, String path, String name)
    {
        String newPath = path + '/' + name;
        AVMNodeDescriptor existing = lookup(-1, newPath);
        Acl parentAcl = getAclAsSystem(-1, path);
        Long parentAclId = null;
        if(parentAcl != null)
        {
            parentAclId = parentAcl.getId();
        }
        Acl acl = getAclAsSystem(version, desc.getPath());
        
        if (desc.isFile())
        {
            InputStream in = getFileInputStream(version, desc.getPath());
            if (existing != null)
            {
                removeNode(newPath);   
            }
            // Set the acl underneath
            createFile(path, name, in);
            if (acl != null)
            {
                setAclAsSystem(newPath, AVMDAOs.Instance().fAclDAO.getAclCopy(acl.getId(), parentAclId, ACLCopyMode.COPY));
            }
            ContentData cd = getContentDataForRead(version, desc.getPath());
            setEncoding(newPath, cd.getEncoding());
            setMimeType(newPath, cd.getMimetype());
        }
        else // desc is a directory.
        {
            if (existing != null && !existing.isDirectory())
            {
                removeNode(newPath);
                existing = null;
            }
            
            Map<String, AVMNodeDescriptor> listing = getDirectoryListing(desc);
            
            if (existing == null)
            {
                createDirectory(path, name);
                // Set acl before creating children as acls inherit :-)
                if (acl != null)
                {
                    setAclAsSystem(newPath, AVMDAOs.Instance().fAclDAO.getAclCopy(acl.getId(), parentAclId, ACLCopyMode.COPY));
                }
            }
            
            for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
            {
                recursiveCopy(version, entry.getValue(), newPath, entry.getKey()); 
            }
        }
        // In either case copy properties, aspects, and acls.
        Map<QName, PropertyValue> props = getNodeProperties(version, desc.getPath());
        setNodeProperties(newPath, props);
        Set<QName> aspects = getAspects(version, desc.getPath());
        for (QName aspect : aspects)
        {
            addAspect(newPath, aspect);
        }
    }

    /**
     * Rename a store.
     * @param sourceName The original name.
     * @param destName The new name.
     * @throws AVMNotFoundException
     * @throws AVMExistsException
     */
    public void renameStore(String sourceName, String destName)
    {
        if (sourceName == null || destName == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.renameStore(sourceName, destName);
    }

    /**
     * Revert a head path to a given version. This works by cloning
     * the version to revert to, and then linking that new version into head.
     * The reverted version will have the previous head version as ancestor.
     * @param path The path to the node to revert.
     * @param toRevertTo The descriptor of the version to revert to.
     * @throws AVMNotFoundException
     */
    public void revert(String path, AVMNodeDescriptor toRevertTo)
    {
        if (path == null || toRevertTo == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        String [] baseName = AVMNodeConverter.SplitBase(path);
        if (baseName.length != 2)
        {
            throw new AVMBadArgumentException("Cannot revert store root: " + path);
        }
        fAVMRepository.revert(baseName[0], baseName[1], toRevertTo);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setGuid(java.lang.String, java.lang.String)
     */
    public void setGuid(String path, String guid) 
    {
        if (path == null || guid == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.setGuid(path, guid);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setEncoding(java.lang.String, java.lang.String)
     */
    public void setEncoding(String path, String encoding)
    {
        if (path == null || encoding == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.setEncoding(path, encoding);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#setMimeType(java.lang.String, java.lang.String)
     */
    public void setMimeType(String path, String mimeType)
    {
        if (path == null || mimeType == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        fAVMRepository.setMimeType(path, mimeType);
    }

    public List<VersionDescriptor> getStoreVersionsTo(String name, int version)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getAVMStoreVersionsTo(name, version);
    }

    public List<VersionDescriptor> getStoreVersionsFrom(String name, int version)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getAVMStoreVersionsFrom(name, version);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.AVMService#getStoreVersionsBetween(java.lang.String, int, int)
     */
    public List<VersionDescriptor> getStoreVersionsBetween(String name, int startVersion, int endVersion)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        return fAVMRepository.getAVMStoreVersionsBetween(name, startVersion, endVersion);
    }
    
    
    
    
}
