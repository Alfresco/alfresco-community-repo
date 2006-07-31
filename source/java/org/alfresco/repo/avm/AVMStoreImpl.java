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
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * A Repository contains a current root directory and a list of
 * root versions.  Each root version corresponds to a separate snapshot
 * operation.
 * @author britt
 */
class AVMStoreImpl implements AVMStore, Serializable
{
    static final long serialVersionUID = -1485972568675732904L;

    /**
     * The name of this AVMStore.
     */
    private String fName;
    
    /**
     * The current root directory.
     */
    private DirectoryNode fRoot;
    
    /**
     * The next version id.
     */
    private int fNextVersionID;
    
    /**
     * The version (for concurrency control).
     */
    private long fVers;
    
    /**
     * The AVMRepository.
     */
    transient private AVMRepository fAVMRepository;
    
    /**
     * Default constructor.
     */
    protected AVMStoreImpl()
    {
        fAVMRepository = AVMRepository.GetInstance();
    }
    
    /**
     * Make a brand new AVMStore.
     * @param repo The AVMRepository.
     * @param name The name of the AVMStore.
     */
    public AVMStoreImpl(AVMRepository repo, String name)
    {
        // Make ourselves up and save.
        fAVMRepository = repo;
        fName = name;
        fNextVersionID = 0;
        fRoot = null;
        AVMContext.fgInstance.fAVMStoreDAO.save(this);
        setProperty(ContentModel.PROP_CREATOR, new PropertyValue(null, "britt"));
        setProperty(ContentModel.PROP_CREATED, new PropertyValue(null, new Date(System.currentTimeMillis())));
        // Make up the initial version record and save.
        long time = System.currentTimeMillis();
        fRoot = new PlainDirectoryNodeImpl(this);
        fRoot.setIsRoot(true);
        AVMContext.fgInstance.fAVMNodeDAO.save(fRoot);
        VersionRoot versionRoot = new VersionRootImpl(this,
                                                      fRoot,
                                                      fNextVersionID,
                                                      time,
                                                      "britt");
        fNextVersionID++;
        AVMContext.fgInstance.fVersionRootDAO.save(versionRoot);
    }
    
    /**
     * Set a new root for this.
     * @param root
     */
    public void setNewRoot(DirectoryNode root)
    {
        fRoot = root;
        fRoot.setIsRoot(true);
    }

    /**
     * Snapshot this store.  This creates a new version record.
     * @return The version id of the new snapshot.
     */
    @SuppressWarnings("unchecked")
    public int createSnapshot()
    {
        // If the root isn't new, we can't take a snapshot since nothing has changed.
        if (!fRoot.getIsNew())
        {
            throw new AVMExistsException("Already snapshotted.");
        }
        // Clear out the new nodes.
        List<NewInAVMStore> newInRep = AVMContext.fgInstance.fNewInAVMStoreDAO.getByAVMStore(this);
        for (NewInAVMStore newGuy : newInRep)
        {
            AVMContext.fgInstance.fNewInAVMStoreDAO.delete(newGuy);
        }
        // Make up a new version record.
        VersionRoot versionRoot = new VersionRootImpl(this,
                                                      fRoot,
                                                      fNextVersionID,
                                                      System.currentTimeMillis(),
                                                      "britt");
        AVMContext.fgInstance.fVersionRootDAO.save(versionRoot);
        // Increment the version id.
        fNextVersionID++;
        return fNextVersionID - 1;
    }

    /**
     * Create a new directory.
     * @param path The path to the containing directory.
     * @param name The name of the new directory.
     */
    public void createDirectory(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1, true) != null)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        DirectoryNode newDir = null;
        if (lPath.isLayered())  // Creating a directory in a layered context creates
                                // a LayeredDirectoryNode that gets its indirection from
                                // its parent.
        {
            newDir = new LayeredDirectoryNodeImpl((String)null, this);
            ((LayeredDirectoryNodeImpl)newDir).setPrimaryIndirection(false);
            ((LayeredDirectoryNodeImpl)newDir).setLayerID(lPath.getTopLayer().getLayerID());
        }
        else
        {
            newDir = new PlainDirectoryNodeImpl(this);
        }
        newDir.setVersionID(getNextVersionID());
        dir.putChild(name, newDir);
        dir.updateModTime();
    }

    /**
     * Create a new layered directory.
     * @param srcPath The target indirection for a layered node.
     * @param dstPath The containing directory for the new node.
     * @param name The name of the new node.
     */
    public void createLayeredDirectory(String srcPath, String dstPath,
                                       String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath, true);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1, true) != null)
        {
            throw new AVMExistsException("Child exists: " +  name);
        }
        LayeredDirectoryNode newDir =
            new LayeredDirectoryNodeImpl(srcPath, this);
        if (lPath.isLayered())
        {
            // When a layered directory is made inside of a layered context,
            // it gets its layer id from the topmost layer in its lookup
            // path.
            LayeredDirectoryNode top = lPath.getTopLayer();
            newDir.setLayerID(top.getLayerID());
        }
        else
        {
            // Otherwise we issue a brand new layer id.
            newDir.setLayerID(fAVMRepository.issueLayerID());
        }
        dir.putChild(name, newDir);
        dir.updateModTime();
        newDir.setVersionID(getNextVersionID());
    }

    /**
     * Create a new file.
     * @param path The path to the directory to contain the new file.
     * @param name The name to give the new file.
     * initial content.
     */
    public OutputStream createFile(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1, true) != null)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        PlainFileNodeImpl file = new PlainFileNodeImpl(this);
        file.setVersionID(getNextVersionID());
        dir.putChild(name, file);
        dir.updateModTime();
        return file.getContentForWrite().getOutputStream();
    }

    /**
     * Create a file with the given contents.
     * @param path The path to the containing directory.
     * @param name The name to give the new file.
     * @param data The contents.
     */
    public void createFile(String path, String name, File data)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1, true) != null)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        PlainFileNodeImpl file = new PlainFileNodeImpl(this, data);
        file.setVersionID(getNextVersionID());
        dir.putChild(name, file);
        dir.updateModTime();
    }

    /**
     * Create a new layered file.
     * @param srcPath The target indirection for the layered file.
     * @param dstPath The path to the directory to contain the new file.
     * @param name The name of the new file.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath, true);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1, true) != null)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        // TODO Reexamine decision to not check validity of srcPath.
        LayeredFileNodeImpl newFile =
            new LayeredFileNodeImpl(srcPath, this);
        dir.putChild(name, newFile);
        dir.updateModTime();
        newFile.setVersionID(getNextVersionID());
    }

    /**
     * Get an input stream from a file.
     * @param version The version id to look under.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getInputStream(int version, String path)
    {
        Lookup lPath = lookup(version, path, false);
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE &&
            node.getType() != AVMNodeType.LAYERED_FILE)
        {
            throw new AVMExistsException("Not a file: " + path + " r " + version);
        }
        FileNode file = (FileNode)node;
        FileContent content = file.getContentForRead();
        return content.getInputStream();
    }

    /**
     * Get a listing from a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of FolderEntries.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(int version, String path)
    {
        Lookup lPath = lookupDirectory(version, path, false);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        Map<String, AVMNode> listing = dir.getListing(lPath);
        return translateListing(listing, lPath);
    }

    /**
     * Get the list of nodes directly contained in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A Map of names to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListingDirect(int version, String path)
    {
        Lookup lPath = lookupDirectory(version, path, false);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        Map<String, AVMNode> listing = dir.getListingDirect(lPath);
        return translateListing(listing, lPath);
    }

    /**
     * Helper to convert an internal representation of a directory listing
     * to an external representation.
     * @param listing The internal listing, a Map of names to nodes.
     * @param lPath The Lookup for the directory.
     * @return A Map of names to descriptors.
     */
    private SortedMap<String, AVMNodeDescriptor> 
        translateListing(Map<String, AVMNode> listing, Lookup lPath)
    {
        SortedMap<String, AVMNodeDescriptor> results = new TreeMap<String, AVMNodeDescriptor>();
        for (String name : listing.keySet())
        {
            AVMNode child = listing.get(name);
            AVMNodeDescriptor desc = child.getDescriptor(lPath, name);
            results.put(name, desc);
        }
        return results;
    }

    /**
     * Get the names of the deleted nodes in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of names.
     */
    public List<String> getDeleted(int version, String path)
    {
        Lookup lPath = lookupDirectory(version, path, false);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        return dir.getDeletedNames();
    }

    /**
     * Get an output stream to a file.
     * @param path The path to the file.
     * @return An OutputStream.
     */
    public OutputStream getOutputStream(String path)
    {
        Lookup lPath = lookup(-1, path, true);
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE &&
            node.getType() != AVMNodeType.LAYERED_FILE)
        {
            throw new AVMWrongTypeException("Not a file: " + path);
        }
        FileNode file = (FileNode)node;
        FileContent content = file.getContentForWrite();
        file.updateModTime();
        return content.getOutputStream();
    }

    /**
     * Get a RandomAccessFile to a file node.
     * @param version The version.
     * @param path The path to the file.
     * @param access The access mode for RandomAccessFile.
     * @return A RandomAccessFile.
     */
    public RandomAccessFile getRandomAccess(int version, String path, String access)
    {
        boolean write = access.indexOf("rw") == 0;
        if (write && version >= 0)
        {
            throw new AVMException("Access denied: " + path);
        }
        Lookup lPath = lookup(version, path, write);
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE &&
            node.getType() != AVMNodeType.LAYERED_FILE)
        {
            throw new AVMWrongTypeException("Not a file: " + path);
        }
        FileNode file = (FileNode)node;
        FileContent content = null;
        if (write)
        {
            content = file.getContentForWrite();
            file.updateModTime();
        }
        else
        {
            content = file.getContentForRead();
        }
        return content.getRandomAccess(access);
    }

    /**
     * Remove a node and everything underneath it.
     * @param path The path to the containing directory.
     * @param name The name of the node to remove.
     */
    public void removeNode(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1, true) == null)
        {
            throw new AVMNotFoundException("Does not exist: " + name);
        }
        dir.removeChild(name);
        dir.updateModTime();
    }

    /**
     * Allow a name which has been deleted to be visible through that layer.
     * @param dirPath The path to the containing directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name)
    {
        Lookup lPath = lookup(-1, dirPath, true);
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.LAYERED_DIRECTORY)
        {
            throw new AVMWrongTypeException("Not a layered directory: " + dirPath);
        }
        ((LayeredDirectoryNode)node).uncover(lPath, name);
        node.updateModTime();
    }

    // TODO This is problematic.  As time goes on this returns
    // larger and larger data sets.  Perhaps what we should do is
    // provide methods for getting versions by date range, n most 
    // recent etc.
    /**
     * Get the set of all extant versions for this AVMStore.
     * @return A Set of version ids.
     */
    @SuppressWarnings("unchecked")
    public List<VersionDescriptor> getVersions()
    {
        List<VersionRoot> versions = AVMContext.fgInstance.fVersionRootDAO.getAllInAVMStore(this);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc = 
                new VersionDescriptor(fName,
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate());
            descs.add(desc);
        }
        return descs;
    }

    /**
     * Get the versions between the given dates (inclusive). From or
     * to may be null but not both.
     * @param from The earliest date.
     * @param to The latest date.
     * @return The Set of matching version IDs.
     */
    @SuppressWarnings("unchecked")
    public List<VersionDescriptor> getVersions(Date from, Date to)
    {
        List<VersionRoot> versions = AVMContext.fgInstance.fVersionRootDAO.getByDates(this, from, to);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc =
                new VersionDescriptor(fName,
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate());
            descs.add(desc);
        }
        return descs;
    }

    /**
     * Get the AVMRepository.
     * @return The AVMRepository
     */
    public AVMRepository getAVMRepository()
    {
        return fAVMRepository;
    }

    /**
     * Lookup up a path.
     * @param version The version to look in.
     * @param path The path to look up.
     * @param write Whether this is in the context of a write.
     * @return A Lookup object.
     */
    public Lookup lookup(int version, String path, boolean write)
    {
        // Make up a Lookup to hold the results.
        Lookup result = new Lookup(this, fName);
        if (path.length() == 0)
        {
            throw new AVMException("Invalid path: " + path);
        }
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        while (path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }
        String[] pathElements = path.split("/+");
        // Grab the root node to start the lookup.
        DirectoryNode dir = null;
        // Versions less than 0 mean get current.
        if (version < 0)
        {
            dir = fRoot;
        }
        else
        {
            dir = AVMContext.fgInstance.fAVMNodeDAO.getAVMStoreRoot(this, version);
        }
        // Add an entry for the root.
        result.add(dir, "", write);
        dir = (DirectoryNode)result.getCurrentNode();
        if (pathElements.length == 1 && pathElements[0].equals(""))
        {
            return result;
        }
        // Now look up each path element in sequence up to one
        // before the end.
        for (int i = 0; i < pathElements.length - 1; i++)
        {
            AVMNode child = dir.lookupChild(result, pathElements[i], version, write);
            if (child == null)
            {
                throw new AVMNotFoundException("Not found: " + pathElements[i]);
            }
            // Every element that is not the last needs to be a directory.
            if (child.getType() != AVMNodeType.PLAIN_DIRECTORY &&
                child.getType() != AVMNodeType.LAYERED_DIRECTORY)
            {
                throw new AVMWrongTypeException("Not a directory: " + pathElements[i]);
            }
            result.add(child, pathElements[i], write);
            dir = (DirectoryNode)result.getCurrentNode();
        }
        // Now look up the last element.
        AVMNode child = dir.lookupChild(result, pathElements[pathElements.length - 1], version, write);
        if (child == null)
        {
            throw new AVMNotFoundException("Not found: " + pathElements[pathElements.length - 1]);
        }
        result.add(child, pathElements[pathElements.length - 1], write);
        return result;
    }

    /**
     * Get the root node descriptor.
     * @param version The version to get.
     * @return The descriptor.
     */
    public AVMNodeDescriptor getRoot(int version)
    {
        AVMNode root = null;
        if (version < 0)
        {
            root = fRoot;
        }
        else
        {
            root = AVMContext.fgInstance.fAVMNodeDAO.getAVMStoreRoot(this, version);
        }            
        return root.getDescriptor("main:", "", null);
    }

    /**
     * Lookup a node and insist that it is a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @param write Whether this is in a write context.
     * @return A Lookup object.
     */
    public Lookup lookupDirectory(int version, String path, boolean write)
    {
        // Just do a regular lookup and assert that the last element
        // is a directory.
        Lookup lPath = lookup(version, path, write);
        if (lPath.getCurrentNode().getType() != AVMNodeType.PLAIN_DIRECTORY &&
            lPath.getCurrentNode().getType() != AVMNodeType.LAYERED_DIRECTORY)
        {
            throw new AVMWrongTypeException("Not a directory: " + path);
        }
        return lPath;
    }

    /**
     * Get the effective indirection path for a layered node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The effective indirection.
     */
    public String getIndirectionPath(int version, String path)
    {
        Lookup lPath = lookup(version, path, false);
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            return ((LayeredDirectoryNode)node).getUnderlying(lPath);
        }
        if (node.getType() == AVMNodeType.LAYERED_FILE)
        {
            return ((LayeredFileNode)node).getUnderlying(lPath);
        }
        throw new AVMWrongTypeException("Not a layered node: " + path);
    }
    
    /**
     * Make the indicated node a primary indirection.
     * @param path The path to the node.
     */
    public void makePrimary(String path)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!lPath.isLayered())
        {
            throw new AVMException("Not in a layered context: " + path);
        }
        dir.turnPrimary(lPath);
        dir.updateModTime();
    }

    /**
     * Change the indirection of a layered directory.
     * @param path The path to the layered directory.
     * @param target The target indirection to set.
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!lPath.isLayered())
        {
            throw new AVMException("Not in a layered context: " + path);
        }
        dir.retarget(lPath, target);
        dir.updateModTime();
    }
    
    /**
     * Set the name of this AVMStore.  Hibernate.
     * @param name
     */
    protected void setName(String name)
    {
        fName = name;
    }
    
    /**
     * Get the name of this AVMStore.
     * @return The name.
     */
    public String getName()
    {
        return fName;
    }
    
    /**
     * Set the next version id.
     * @param nextVersionID
     */
    protected void setNextVersionID(int nextVersionID)
    {
        fNextVersionID = nextVersionID;
    }
    
    /**
     * Get the next version id.
     * @return The next version id.
     */
    public int getNextVersionID()
    {
        return fNextVersionID;
    }
    
    /**
     * Set the root directory.  Hibernate.
     * @param root
     */
    protected void setRoot(DirectoryNode root)
    {
        fRoot = root;
    }
    
    /**
     * Get the root directory.
     * @return The root directory.
     */
    public DirectoryNode getRoot()
    {
        return fRoot;
    }
    
    /**
     * Set the version (for concurrency control). Hibernate.
     * @param vers
     */
    protected void setVers(long vers)
    {
        fVers = vers;
    }
    
    /**
     * Get the version (for concurrency control). Hibernate.
     * @return The version.
     */
    protected long getVers()
    {
        return fVers;
    }

    /**
     * Equals override.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMStore))
        {
            return false;
        }
        return fName.equals(((AVMStore)obj).getName());
    }

    /**
     * Get a hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fName.hashCode();
    }

    /**
     * Purge all nodes reachable only via this version and repostory.
     * @param version
     */
    @SuppressWarnings("unchecked")
    public void purgeVersion(int version)
    {
        if (version == 0)
        {
            throw new AVMBadArgumentException("Cannot purge initial version");
        }
        VersionRoot vRoot = AVMContext.fgInstance.fVersionRootDAO.getByVersionID(this, version);
        if (vRoot == null)
        {
            throw new AVMNotFoundException("Version not found.");
        }
        AVMNode root = vRoot.getRoot();
        root.setIsRoot(false);
        AVMContext.fgInstance.fAVMNodeDAO.update(root);
        AVMContext.fgInstance.fVersionRootDAO.delete(vRoot);
        if (root.equals(fRoot))
        {
            // We have to set a new current root.
            // TODO More hibernate goofiness to compensate for: fSuper.getSession().flush();
            vRoot = AVMContext.fgInstance.fVersionRootDAO.getMaxVersion(this);
            fRoot = vRoot.getRoot();
            AVMContext.fgInstance.fAVMStoreDAO.update(this);
        }
    }

    /**
     * Get the descriptor for this.
     * @return An AVMStoreDescriptor
     */
    public AVMStoreDescriptor getDescriptor()
    {
        return new AVMStoreDescriptor(fName, 
                getProperty(ContentModel.PROP_CREATOR).getStringValue(),
                ((Date)getProperty(ContentModel.PROP_CREATED).getValue(DataTypeDefinition.DATE)).getTime());
    }

    /**
     * Set the opacity of a layered directory. An opaque directory hides
     * what is pointed at by its indirection.
     * @param path The path to the layered directory.
     * @param opacity True is opaque; false is not.
     */
    public void setOpacity(String path, boolean opacity)
    {
        Lookup lPath = lookup(-1, path, true);
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof LayeredDirectoryNode))
        {
            throw new AVMWrongTypeException("Not a LayeredDirectoryNode.");
        }
        ((LayeredDirectoryNode)node).setOpacity(opacity);
        node.updateModTime();
    }
    
    /**
     * Set a property on a node.
     * @param path The path to the node.
     * @param name The name of the property.
     * @param value The value to set.
     */
    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        Lookup lPath = lookup(-1, path, true);
        AVMNode node = lPath.getCurrentNode();
        node.setProperty(name, value);
    }
    
    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of QNames to PropertyValues.
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties)
    {
        Lookup lPath = lookup(-1, path, true);
        AVMNode node = lPath.getCurrentNode();
        node.setProperties(properties);
    }
    
    /**
     * Get a property by name.
     * @param version The version to lookup.
     * @param path The path to the node.
     * @param name The name of the property.
     * @return A PropertyValue or null if not found.
     */
    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        Lookup lPath = lookup(version, path, false);
        AVMNode node = lPath.getCurrentNode();
        return node.getProperty(name);
    }
    
    /**
     * Get all the properties associated with a node.
     * @param version The version to lookup.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path)
    {
        Lookup lPath = lookup(version, path, false);
        AVMNode node = lPath.getCurrentNode();
        return node.getProperties();
    }
    
    /**
     * Delete a single property from a node.
     * @param path The path to the node.
     * @param name The name of the property.
     */
    public void deleteNodeProperty(String path, QName name)
    {
        Lookup lPath = lookup(-1, path, true);
        AVMNode node = lPath.getCurrentNode();
        node.deleteProperty(name);
    }
    
    /**
     * Delete all properties from a node.
     * @param path The path to the node.
     */
    public void deleteNodeProperties(String path)
    {
        Lookup lPath = lookup(-1, path, true);
        AVMNode node = lPath.getCurrentNode();
        node.deleteProperties();
    }

    /**
     * Set a property on this store. Replaces if property already exists.
     * @param name The QName of the property.
     * @param value The actual PropertyValue.
     */
    public void setProperty(QName name, PropertyValue value)
    {
        AVMStoreProperty prop = new AVMStorePropertyImpl();
        prop.setStore(this);
        prop.setName(name);
        prop.setValue(value);
        AVMContext.fgInstance.fAVMStorePropertyDAO.save(prop);
    }
    
    /**
     * Set a group of properties on this store. Replaces any property that exists.
     * @param properties A Map of QNames to PropertyValues to set.
     */
    public void setProperties(Map<QName, PropertyValue> properties)
    {
        for (QName name : properties.keySet())
        {
            setProperty(name, properties.get(name));
        }
    }
    
    /**
     * Get a property by name.
     * @param name The QName of the property to fetch.
     * @return The PropertyValue or null if non-existent.
     */
    public PropertyValue getProperty(QName name)
    {
        return AVMContext.fgInstance.fAVMStorePropertyDAO.get(this, name).getValue();
    }
    
    /**
     * Get all the properties associated with this node.
     * @return A Map of the properties.
     */
    public Map<QName, PropertyValue> getProperties()
    {
        List<AVMStoreProperty> props = 
            AVMContext.fgInstance.fAVMStorePropertyDAO.get(this);
        Map<QName, PropertyValue> retVal = new HashMap<QName, PropertyValue>();
        for (AVMStoreProperty prop : props)
        {
            retVal.put(prop.getName(), prop.getValue());
        }
        return retVal;
    }
    
    /**
     * Delete a property.
     * @param name The name of the property to delete.
     */
    public void deleteProperty(QName name)
    {
        AVMContext.fgInstance.fAVMStorePropertyDAO.delete(this, name);
    }
}
