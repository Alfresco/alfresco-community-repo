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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

/**
 * A Repository contains a current root directory and a list of
 * root versions.  Each root version corresponds to a separate snapshot
 * operation.
 * @author britt
 */
class RepositoryImpl implements Repository, Serializable
{
    static final long serialVersionUID = -1485972568675732904L;

    /**
     * The name of this repository.
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
     * The super repository.
     */
    transient private SuperRepository fSuper;
    
    /**
     * Default constructor.
     */
    protected RepositoryImpl()
    {
        fSuper = SuperRepository.GetInstance();
    }
    
    /**
     * Make a brand new repository.
     * @param superRepo The SuperRepository.
     * @param name The name of the Repository.
     */
    public RepositoryImpl(SuperRepository superRepo, String name)
    {
        // Make ourselves up and save.
        fSuper = superRepo;
        fName = name;
        fNextVersionID = 0;
        fRoot = null;
        fSuper.getSession().save(this);
        // Make up the initial version record and save.
        long time = System.currentTimeMillis();
        fRoot = new PlainDirectoryNodeImpl(this);
        fRoot.setIsNew(false);
        fRoot.setIsRoot(true);
        fSuper.getSession().save(fRoot);
        VersionRoot versionRoot = new VersionRootImpl(this,
                                                              fRoot,
                                                              fNextVersionID,
                                                              time,
                                                              "britt");
        fNextVersionID++;
        fSuper.getSession().save(versionRoot);
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
     * Snapshot this repository.  This creates a new version record.
     */
    @SuppressWarnings("unchecked")
    public void createSnapshot()
    {
        // Clear out the new nodes.
        Query query = 
            fSuper.getSession().getNamedQuery("AVMNode.ByNewInRepo");
        query.setEntity("repo", this);
        for (AVMNode newNode : (List<AVMNode>)query.list())
        {
            newNode.setIsNew(false);
        }
        // Make up a new version record.
        VersionRoot versionRoot = new VersionRootImpl(this,
                                                      fRoot,
                                                      fNextVersionID,
                                                      System.currentTimeMillis(),
                                                      "britt");
        fSuper.getSession().save(versionRoot);
        // Increment the version id.
        fNextVersionID++;
    }

    /**
     * Create a new directory.
     * @param path The path to the containing directory.
     * @param name The name of the new directory.
     */
    public void createDirectory(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
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
        dir.addChild(name, newDir, lPath);
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
        Lookup lPath = lookupDirectory(-1, dstPath);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
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
            newDir.setLayerID(fSuper.issueLayerID());
        }
        dir.addChild(name, newDir, lPath);
        newDir.setVersionID(getNextVersionID());
    }

    /**
     * Create a new file.
     * @param path The path to the directory to contain the new file.
     * @param name The name to give the new file.
     * @param source A (possibly null) InputStream from which to get
     * initial content.
     */
    public OutputStream createFile(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        PlainFileNodeImpl file = new PlainFileNodeImpl(this);
        file.setVersionID(getNextVersionID());
        dir.addChild(name, file, lPath);
        return file.getContentForWrite(this).getOutputStream(fSuper);
    }

    /**
     * Create a new layered file.
     * @param srcPath The target indirection for the layered file.
     * @param dstPath The path to the directory to contain the new file.
     * @param name The name of the new file.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        // TODO Reexamine decision to not check validity of srcPath.
        LayeredFileNodeImpl newFile =
            new LayeredFileNodeImpl(srcPath, this);
        dir.addChild(name, newFile, lPath);
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
        Lookup lPath = lookup(version, path);
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE &&
            node.getType() != AVMNodeType.LAYERED_FILE)
        {
            throw new AVMExistsException("Not a file: " + path + " r " + version);
        }
        FileNode file = (FileNode)node;
        FileContent content = file.getContentForRead(this);
        return content.getInputStream(fSuper);
    }

    /**
     * Get a listing from a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of FolderEntries.
     */
    public List<FolderEntry> getListing(int version, String path)
    {
        Lookup lPath = lookupDirectory(version, path);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        Map<String, AVMNode> listing = dir.getListing(lPath);
        ArrayList<FolderEntry> results = new ArrayList<FolderEntry>();
        for (String name : listing.keySet())
        {
            AVMNode child = listing.get(name);
            FolderEntry item = new FolderEntry();
            item.setName(name);
            item.setType(child.getType());
            results.add(item);
        }
        return results;
    }

    /**
     * Get an output stream to a file.
     * @param path The path to the file.
     * @return An OutputStream.
     */
    public OutputStream getOutputStream(String path)
    {
        Lookup lPath = lookup(-1, path);
//        lPath.acquireLocks();
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE &&
            node.getType() != AVMNodeType.LAYERED_FILE)
        {
            throw new AVMWrongTypeException("Not a file: " + path);
        }
        FileNode file = (FileNode)node;
        file = (FileNode)file.copyOnWrite(lPath);
        FileContent content = file.getContentForWrite(this); 
        return content.getOutputStream(fSuper);    // TODO Do we really need fSuper?
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
        Lookup lPath = lookup(version, path);
        if (write)
        {
//            lPath.acquireLocks();
        }
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
            file = (FileNode)file.copyOnWrite(lPath);
            content = file.getContentForWrite(this);
        }
        else
        {
            content = file.getContentForRead(this);
        }
        return content.getRandomAccess(fSuper, access);
    }

    /**
     * Remove a node and everything underneath it.
     * @param path The path to the containing directory.
     * @param name The name of the node to remove.
     */
    public void removeNode(String path, String name)
    {
        // TODO Are we double checking for existence?
        Lookup lPath = lookupDirectory(-1, path);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) == null)
        {
            throw new AVMNotFoundException("Does not exist: " + name);
        }
        dir.removeChild(name, lPath);
    }

    /**
     * Allow a name which has been deleted to be visible through that layer.
     * @param dirPath The path to the containing directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name)
    {
        Lookup lPath = lookup(-1, dirPath);
//        lPath.acquireLocks();
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.LAYERED_DIRECTORY)
        {
            throw new AVMWrongTypeException("Not a layered directory: " + dirPath);
        }
        ((LayeredDirectoryNode)node).uncover(lPath, name);
    }

    // TODO This is problematic.  As time goes on this returns
    // larger and larger data sets.  Perhaps what we should do is
    // provide methods for getting versions by date range, n most 
    // recent etc.
    /**
     * Get the set of all extant versions for this Repository.
     * @return A Set of version ids.
     */
    @SuppressWarnings("unchecked")
    public List<VersionDescriptor> getVersions()
    {
        Query query = fSuper.getSession().createQuery("from VersionRootImpl v order by v.versionID");
        List<VersionRoot> versions = (List<VersionRoot>)query.list();
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
        Query query;
        if (from == null)
        {
            query = 
                fSuper.getSession().createQuery("from VersionRootImpl vr where vr.createDate <= :to " +
                                                "order by vr.versionID");
            query.setLong("to", to.getTime());
        }
        else if (to == null)
        {
            query =
                fSuper.getSession().createQuery("from VersionRootImpl vr " +
                                                "where vr.createDate >= :from " +
                                                "order by vr.versionID");
            query.setLong("from", from.getTime());
        }
        else
        {
            query =
                fSuper.getSession().createQuery("from VersionRootImpl vr "+ 
                                                "where vr.createDate between :from and :to " +
                                                "order by vr.versionID");
            query.setLong("from", from.getTime());
            query.setLong("to", to.getTime());
        }
        List<VersionRoot> versions = (List<VersionRoot>)query.list();
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
     * Get the SuperRepository.
     * @return The SuperRepository
     */
    public SuperRepository getSuperRepository()
    {
        return fSuper;
    }

    /**
     * Lookup up a path.
     * @param version The version to look in.
     * @param path The path to look up.
     * @return A Lookup object.
     */
    public Lookup lookup(int version, String path)
    {
        // Make up a Lookup to hold the results.
        Lookup result = new Lookup(this, fName);
        if (path.length() == 0)
        {
            throw new AVMException("Invalid path: " + path);
        }
        if (path.length() > 1)
        {
            path = path.substring(1);
        }
        String[] pathElements = path.split("/");
        // Grab the root node to start the lookup.
        DirectoryNode dir = null;
        // Versions less than 0 mean get current.
        if (version < 0)
        {
            dir = (DirectoryNode)AVMNodeUnwrapper.Unwrap(fRoot);
        }
        else
        {
            Query query = 
                fSuper.getSession().getNamedQuery("VersionRoot.GetVersionRoot");
            query.setEntity("rep", this);
            query.setInteger("version", version);
            dir = (DirectoryNode)AVMNodeUnwrapper.Unwrap((AVMNode)query.uniqueResult());
        }
//        fSuper.getSession().lock(dir, LockMode.READ);
        // Add an entry for the root.
        result.add(dir, "");
        if (pathElements.length == 0)
        {
            return result;
        }
        // Now look up each path element in sequence up to one
        // before the end.
        for (int i = 0; i < pathElements.length - 1; i++)
        {
            AVMNode child = dir.lookupChild(result, pathElements[i], version);
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
            dir = (DirectoryNode)child;
//            fSuper.getSession().lock(dir, LockMode.READ);
            result.add(dir, pathElements[i]);
        }
        // Now look up the last element.
        AVMNode child = dir.lookupChild(result, pathElements[pathElements.length - 1], version);
        if (child == null)
        {
            throw new AVMNotFoundException("Not found: " + pathElements[pathElements.length - 1]);
        }
//        fSuper.getSession().lock(child, LockMode.READ);
        result.add(child, pathElements[pathElements.length - 1]);
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
            Query query = 
                fSuper.getSession().getNamedQuery("VersionRoot.GetVersionRoot");
            query.setEntity("rep", this);
            query.setInteger("version", version);
            root = (AVMNode)query.uniqueResult();
            if (root == null)
            {
                throw new AVMException("Invalid version: " + version);
            }
        }            
        return root.getDescriptor("main:", "", null);
    }

    /**
     * Lookup a node and insist that it is a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A Lookup object.
     */
    public Lookup lookupDirectory(int version, String path)
    {
        // Just do a regular lookup and assert that the last element
        // is a directory.
        Lookup lPath = lookup(version, path);
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
        Lookup lPath = lookup(version, path);
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
        Lookup lPath = lookupDirectory(-1, path);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!lPath.isLayered())
        {
            throw new AVMException("Not in a layered context: " + path);
        }
        dir.turnPrimary(lPath);
    }

    /**
     * Change the indirection of a layered directory.
     * @param path The path to the layered directory.
     * @param target The target indirection to set.
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        Lookup lPath = lookupDirectory(-1, path);
//        lPath.acquireLocks();
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!lPath.isLayered())
        {
            throw new AVMException("Not in a layered context: " + path);
        }
        dir.retarget(lPath, target);
    }
    
    /**
     * Set the name of this repository.  Hibernate.
     * @param name
     */
    protected void setName(String name)
    {
        fName = name;
    }
    
    /**
     * Get the name of this Repository.
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
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof Repository))
        {
            return false;
        }
        return fName.equals(((Repository)obj).getName());
    }

    /**
     * @return
     */
    @Override
    public int hashCode()
    {
        // TODO Auto-generated method stub
        return super.hashCode();
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
        Query query = fSuper.getSession().getNamedQuery("VersionRoot.VersionByID");
        query.setEntity("rep", this);
        query.setInteger("version", version);
        VersionRoot vRoot = (VersionRoot)query.uniqueResult();
        AVMNode root = vRoot.getRoot();
        root.setIsRoot(false);
        fSuper.getSession().delete(vRoot);
        if (root.equals(fRoot))
        {
            // We have to set a new current root.
            fSuper.getSession().flush();
            query = fSuper.getSession().createQuery("select max(vr.versionID) from VersionRootImpl vr");
            int latest = (Integer)query.uniqueResult();
            query = fSuper.getSession().getNamedQuery("VersionRoot.VersionByID");
            query.setEntity("rep", this);
            query.setInteger("version", latest);
            vRoot = (VersionRoot)query.uniqueResult();
            fRoot = vRoot.getRoot();
        }
    }
}
