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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

/**
 * This or Repository are
 * the implementors of the operations specified by AVMService.
 * @author britt
 */
class SuperRepository
{
    /**
     * The single instance of SuperRepository.
     */
    private static SuperRepository fgInstance;
    
    /**
     * The current lookup count.
     */
    private ThreadLocal<Integer> fLookupCount;
    
    /**
     * The node id issuer.
     */
    private Issuer fNodeIssuer;
    
    /**
     * The content id issuer;
     */
    private Issuer fContentIssuer;
    
    /**
     * The layer id issuer.
     */
    private Issuer fLayerIssuer;

    /**
     * The file storage directory.
     */
    private String fStorage;
    
    /**
     * Create a new one.  It's given issuers and a storage directory.
     * @param nodeIssuer
     * @param contentIssuer
     * @param layerIssuer
     * @param storage
     */
    public SuperRepository(Issuer nodeIssuer,
                           Issuer contentIssuer,
                           Issuer layerIssuer,
                           String storage)
    {
        fStorage = storage;
        fNodeIssuer = nodeIssuer;
        fContentIssuer = contentIssuer;
        fLayerIssuer = layerIssuer;
        fLookupCount = new ThreadLocal<Integer>();
        fgInstance = this;
    }

    /**
     * Create a file.
     * @param path The path to the containing directory.
     * @param name The name for the new file.
     */
    public OutputStream createFile(String path, String name)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
//        fSession.get().lock(rep, LockMode.UPGRADE);
        return rep.createFile(pathParts[1], name);
    }

    /**
     * Create a file with the given File as content.
     * @param path The path to the containing directory.
     * @param name The name to give the file.
     * @param data The file contents.
     */
    public void createFile(String path, String name, File data)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        rep.createFile(pathParts[1], name, data);
    }
    
    /**
     * Create a new directory.
     * @param path The path to the containing directory.
     * @param name The name to give the directory.
     */
    public void createDirectory(String path, String name)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        rep.createDirectory(pathParts[1], name);
    }

    /**
     * Create a new layered directory.
     * @param srcPath The target indirection for the new layered directory.
     * @param dstPath The path to the containing directory.
     * @param name The name for the new directory.
     */
    public void createLayeredDirectory(String srcPath, String dstPath,
            String name)
    {
        if (dstPath.indexOf(srcPath) == 0)
        {
            throw new AVMCycleException("Cycle would be created.");
        }
        fLookupCount.set(1);
        String[] pathParts = SplitPath(dstPath);
        Repository rep = getRepositoryByName(pathParts[0], true);
//        fSession.get().lock(rep, LockMode.UPGRADE);
        rep.createLayeredDirectory(srcPath, pathParts[1], name);
    }

    /**
     * Create a new layered file.
     * @param srcPath The target indirection for the new layered file.
     * @param dstPath The path to the containing directory.
     * @param name The name of the new layered file.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(dstPath);
        Repository rep = getRepositoryByName(pathParts[0], true);
        rep.createLayeredFile(srcPath, pathParts[1], name);
    }

    /**
     * Create a new repository.
     * @param name The name to give the new repository.
     */
    public void createRepository(String name)
    {
        try
        {
            getRepositoryByName(name, false);
            throw new AVMExistsException("Repository exists: " + name);
        }
        catch (AVMNotFoundException anf)
        {
            // Do nothing.
        }
        // Newing up the object causes it to be written to the db.
        @SuppressWarnings("unused") 
        Repository rep = new RepositoryImpl(this, name);
        // Special handling for repository creation.
        NewInRepository newInRep = AVMContext.fgInstance.fNewInRepositoryDAO.getByNode(rep.getRoot());
        AVMContext.fgInstance.fNewInRepositoryDAO.delete(newInRep);
    }

    /**
     * Create a new branch.
     * @param version The version to branch off.
     * @param srcPath The path to make a branch from.
     * @param dstPath The containing directory.
     * @param name The name of the new branch.
     */
    public void createBranch(int version, String srcPath, String dstPath, String name)
    {
        if (dstPath.indexOf(srcPath) == 0)
        {
            throw new AVMCycleException("Cycle would be created.");
        }
        // Lookup the src node.
        fLookupCount.set(1);
        String [] pathParts = SplitPath(srcPath);
        Repository srcRepo = getRepositoryByName(pathParts[0], false);
        Lookup sPath = srcRepo.lookup(version, pathParts[1], false);
        // Lookup the destination directory.
        fLookupCount.set(1);
        pathParts = SplitPath(dstPath);
        Repository dstRepo = getRepositoryByName(pathParts[0], true);
        Lookup dPath = dstRepo.lookupDirectory(-1, pathParts[1], true);
        DirectoryNode dirNode = (DirectoryNode)dPath.getCurrentNode();
        AVMNode srcNode = sPath.getCurrentNode();
        AVMNode dstNode = null;
        // We do different things depending on what kind of thing we're 
        // branching from. I'd be considerably happier if we disallowed
        // certain scenarios, but Jon won't let me :P (bhp).
        if (srcNode.getType() == AVMNodeType.PLAIN_DIRECTORY)
        {
            dstNode = new PlainDirectoryNodeImpl((PlainDirectoryNode)srcNode, dstRepo);
        }
        else if (srcNode.getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            dstNode = 
                new LayeredDirectoryNodeImpl((LayeredDirectoryNode)srcNode, dstRepo);
            ((LayeredDirectoryNode)dstNode).setLayerID(issueLayerID());
        }
        else if (srcNode.getType() == AVMNodeType.LAYERED_FILE)
        {
            dstNode = new LayeredFileNodeImpl((LayeredFileNode)srcNode, dstRepo);
        }
        else // This is a plain file.
        {
            dstNode = new PlainFileNodeImpl((PlainFileNode)srcNode, dstRepo);
        }
        dstNode.setVersionID(dstRepo.getNextVersionID());
        dstNode.setAncestor(srcNode);
        dirNode.putChild(name, dstNode);
        dirNode.updateModTime();
    }
    
    /**
     * Get an output stream to a file.
     * @param path The full path to the file.
     * @return An OutputStream.
     */
    public OutputStream getOutputStream(String path)
    {
        fLookupCount.set(1);
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        return rep.getOutputStream(pathParts[1]);
    }
    
    /**
     * Get a random access file from a file node.
     * @param version The version id (read-only if not -1)
     * @param path The path to the file.
     * @param access The access mode for RandomAccessFile.
     * @return A RandomAccessFile.
     */
    public RandomAccessFile getRandomAccess(int version, String path, String access)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        return rep.getRandomAccess(version, pathParts[1], access);
    }

    /**
     * Rename a node.
     * @param srcPath Source containing directory.
     * @param srcName Source name.
     * @param dstPath Destination containing directory.
     * @param dstName Destination name.
     */
    public void rename(String srcPath, String srcName, String dstPath,
                       String dstName)
    {
        // This is about as ugly as it gets.  
        if (dstPath.indexOf(srcPath + srcName) == 0)
        {
            throw new AVMCycleException("Cyclic rename.");
        }
        fLookupCount.set(1);
        String [] pathParts = SplitPath(srcPath);
        Repository srcRepo = getRepositoryByName(pathParts[0], true);
        Lookup sPath = srcRepo.lookupDirectory(-1, pathParts[1], true);
        DirectoryNode srcDir = (DirectoryNode)sPath.getCurrentNode();
        AVMNode srcNode = srcDir.lookupChild(sPath, srcName, -1, true);
        if (srcNode == null)
        {
            throw new AVMNotFoundException("Not found: " + srcName);
        }
        fLookupCount.set(1);
        pathParts = SplitPath(dstPath);
        Repository dstRepo = getRepositoryByName(pathParts[0], true);
        Lookup dPath = dstRepo.lookupDirectory(-1, pathParts[1], true);
        DirectoryNode dstDir = (DirectoryNode)dPath.getCurrentNode();
        AVMNode dstNode = dstDir.lookupChild(dPath, dstName, -1, true);
        if (dstNode != null)
        {
            throw new AVMExistsException("Node exists: " + dstName);
        }
        // We've passed the check, so we can go ahead and do the rename.
        if (srcNode.getType() == AVMNodeType.PLAIN_DIRECTORY)
        {
            // If the source is layered then the renamed thing needs to be layered also.
            if (sPath.isLayered())
            {
                // If this is a rename happening in the same layer we make a new 
                // OverlayedDirectoryNode that is not a primary indirection layer.
                // Otherwise we do make the new OverlayedDirectoryNode a primary
                // Indirection layer.  This complexity begs the question of whether
                // we should allow renames from within one layer to within another
                // layer.  Allowing it makes the logic absurdly complex.
                if (dPath.isLayered() && dPath.getTopLayer().equals(sPath.getTopLayer()))
                {
                    dstNode = new LayeredDirectoryNodeImpl((PlainDirectoryNode)srcNode, dstRepo, sPath, false);
                    ((LayeredDirectoryNode)dstNode).setLayerID(sPath.getTopLayer().getLayerID());
                }
                else
                {
                    dstNode = new LayeredDirectoryNodeImpl((DirectoryNode)srcNode, dstRepo, sPath, srcName);
                    ((LayeredDirectoryNode)dstNode).setLayerID(issueLayerID());
                }
            }
            else
            {
                dstNode = new PlainDirectoryNodeImpl((PlainDirectoryNode)srcNode, dstRepo);
            }
        }
        else if (srcNode.getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            // TODO I think I need to subdivide this logic again.
            // based on whether the destination is a layer or not.
            if (!sPath.isLayered() || (sPath.isInThisLayer() &&
                srcDir.getType() == AVMNodeType.LAYERED_DIRECTORY &&
                ((LayeredDirectoryNode)srcDir).directlyContains(srcNode)))
            {
                // Use the simple 'copy' constructor.
                dstNode =
                    new LayeredDirectoryNodeImpl((LayeredDirectoryNode)srcNode, dstRepo);
                ((LayeredDirectoryNode)dstNode).setLayerID(((LayeredDirectoryNode)srcNode).getLayerID());
            }
            else
            {
                // If the source node is a primary indirection, then the 'copy' constructor
                // is used.  Otherwise the alternate constructor is called and its
                // indirection is calculated from it's source context.
                if (((LayeredDirectoryNode)srcNode).getPrimaryIndirection())
                {
                    dstNode =
                        new LayeredDirectoryNodeImpl((LayeredDirectoryNode)srcNode, dstRepo);
                }
                else
                {
                    dstNode =
                        new LayeredDirectoryNodeImpl((DirectoryNode)srcNode, dstRepo, sPath, srcName);
                }
                // What needs to be done here is dependent on whether the
                // rename is to a layered context.  If so then it should get the layer id 
                // of its destination parent.  Otherwise it should get a new layer
                // id.
                if (dPath.isLayered())
                {
                    ((LayeredDirectoryNode)dstNode).setLayerID(dPath.getTopLayer().getLayerID());
                }
                else
                {
                    ((LayeredDirectoryNode)dstNode).setLayerID(issueLayerID());
                }
            }
        }
        else if (srcNode.getType() == AVMNodeType.LAYERED_FILE)
        {
            dstNode = new LayeredFileNodeImpl((LayeredFileNode)srcNode, dstRepo);
        }
        else // This is a plain file node.
        {
            dstNode = new PlainFileNodeImpl((PlainFileNode)srcNode, dstRepo);
        }
        srcDir.removeChild(srcName);
        srcDir.updateModTime();
        dstNode.setVersionID(dstRepo.getNextVersionID());
        dstDir.putChild(dstName, dstNode);
        dstDir.updateModTime();
        dstNode.setAncestor(srcNode);
    }

    /**
     * Uncover a deleted name in a layered directory.
     * @param dirPath The path to the layered directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name)
    {
        fLookupCount.set(1);
        String [] pathParts = SplitPath(dirPath);
        Repository repo = getRepositoryByName(pathParts[0], true);
        repo.uncover(pathParts[1], name);
    }

    /**
     * Snapshot the given repositories.
     * @param repositories The list of repository name to snapshot.
     * @return A List of version ids for each newly snapshotted repository.
     */
    public List<Integer> createSnapshot(List<String> repositories)
    {
        List<Integer> result = new ArrayList<Integer>();
        for (String repName : repositories)
        {
            Repository repo = getRepositoryByName(repName, true);
            result.add(repo.createSnapshot());
        }
        return result;
    }

    /**
     * Create a snapshot of a single repository.
     * @param repository The name of the repository.
     * @return The version id of the newly snapshotted repository.
     */
    public int createSnapshot(String repository)
    {
        Repository repo = getRepositoryByName(repository, true);
        return repo.createSnapshot();
    }

    /**
     * Remove a node and everything underneath it.
     * @param path The path to the containing directory.
     * @param name The name of the node to remove.
     */
    public void remove(String path, String name)
    {
        fLookupCount.set(1);
        String [] pathParts = SplitPath(path);
        Repository repo = getRepositoryByName(pathParts[0], true);
        repo.removeNode(pathParts[1], name);
    }

    /**
     * Get rid of all content that lives only in the given repository.
     * Also removes the repository.
     * @param name The name of the repository to purge.
     */
    @SuppressWarnings("unchecked")
    public void purgeRepository(String name)
    {
        Repository rep = getRepositoryByName(name, true);
        AVMNode root = rep.getRoot();
        root.setIsRoot(false);
        VersionRootDAO vrDAO = AVMContext.fgInstance.fVersionRootDAO;
        List<VersionRoot> vRoots = vrDAO.getAllInRepository(rep);
        for (VersionRoot vr : vRoots)
        {
            AVMNode node = vr.getRoot();
            node.setIsRoot(false);
            vrDAO.delete(vr);
        }
        List<NewInRepository> newGuys = AVMContext.fgInstance.fNewInRepositoryDAO.getByRepository(rep);
        for (NewInRepository newGuy : newGuys)
        {
            AVMContext.fgInstance.fNewInRepositoryDAO.delete(newGuy);
        }
        AVMContext.fgInstance.fRepositoryDAO.delete(rep);
    }
    
    /**
     * Remove all content specific to a repository and version.
     * @param name The name of the repository.
     * @param version The version to purge.
     */
    public void purgeVersion(String name, int version)
    {
        Repository rep = getRepositoryByName(name, true);
        rep.purgeVersion(version);
    }

    /**
     * Get an input stream from a file.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getInputStream(int version, String path)
    {
        fLookupCount.set(1);
        String [] pathParts = SplitPath(path);
        Repository repo = getRepositoryByName(pathParts[0], false);
        return repo.getInputStream(version, pathParts[1]);
    }

    /**
     * Get an InputStream from a given version of a file.
     * @param desc The node descriptor.
     * @return The InputStream.
     */
    public InputStream getInputStream(AVMNodeDescriptor desc)
    {
        fLookupCount.set(1);
        AVMNode node = AVMContext.fgInstance.fAVMNodeDAO.getByID(desc.getId());
        if (node == null)
        {
            throw new AVMNotFoundException("Not found.");
        }
        if (node.getType() != AVMNodeType.PLAIN_FILE &&
            node.getType() != AVMNodeType.LAYERED_FILE)
        {
            throw new AVMWrongTypeException("Not a file.");
        }
        FileNode file = (FileNode)node;
        return file.getContentForRead().getInputStream();
    }
    
    /**
     * Get a listing of a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of FolderEntries.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(int version, String path)
    {
        fLookupCount.set(1);
        String [] pathParts = SplitPath(path);
        Repository repo = getRepositoryByName(pathParts[0], false);
        return repo.getListing(version, pathParts[1]);
    }

    /**
     * Get a directory listing from a directory node descriptor.
     * @param dir The directory node descriptor.
     * @return A SortedMap listing.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(AVMNodeDescriptor dir)
    {
        fLookupCount.set(1);
        AVMNode node = AVMContext.fgInstance.fAVMNodeDAO.getByID(dir.getId());
        if (node.getType() != AVMNodeType.LAYERED_DIRECTORY &&
            node.getType() != AVMNodeType.PLAIN_DIRECTORY)
        {
            throw new AVMWrongTypeException("Not a directory.");
        }
        DirectoryNode dirNode = (DirectoryNode)node;
        return dirNode.getListing(dir);
    }
    
    /**
     * Get the names of all repositories.
     * @return A list of names.
     */
    @SuppressWarnings("unchecked")
    public List<RepositoryDescriptor> getRepositories()
    {
        List<Repository> l = AVMContext.fgInstance.fRepositoryDAO.getAll();
        List<RepositoryDescriptor> result = new ArrayList<RepositoryDescriptor>();
        for (Repository rep : l)
        {
            result.add(rep.getDescriptor());
        }
        return result;
    }

    /**
     * Get a descriptor for a repository.
     * @param name The name to get.
     * @return The descriptor.
     */
    public RepositoryDescriptor getRepository(String name)
    {
        Repository rep = getRepositoryByName(name, false);
        return rep.getDescriptor();
    }
    
    /**
     * Get all version for a given repository.
     * @param name The name of the repository.
     * @return A Set will all the version ids.
     */
    public List<VersionDescriptor> getRepositoryVersions(String name)
    {
        Repository rep = getRepositoryByName(name, false);
        return rep.getVersions();
    }

    /**
     * Get the set of versions between (inclusive) of the given dates. 
     * From or to may be null but not both.
     * @param name The name of the repository.
     * @param from The earliest date.
     * @param to The latest date.
     * @return The Set of version IDs.
     */
    public List<VersionDescriptor> getRepositoryVersions(String name, Date from, Date to)
    {
        Repository rep = getRepositoryByName(name, false);
        return rep.getVersions(from, to);
    }
    
    /**
     * Issue a node id.
     * @return The new id.
     */
    public long issueID()
    {
        return fNodeIssuer.issue();
    }

    /**
     * Issue a content id.
     * @return The new id.
     */
    public long issueContentID()
    {
        return fContentIssuer.issue();
    }

    /**
     * Issue a new layer id.
     * @return The new id.
     */
    public long issueLayerID()
    {
        return fLayerIssuer.issue();
    }
    
    /**
     * Get the indirection path for a layered node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The indirection path.
     */
    public String getIndirectionPath(int version, String path)
    {
        fLookupCount.set(1);
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], false);
        return rep.getIndirectionPath(version, pathParts[1]);
    }

    /**
     * Get the next version id for the given repository.
     * @param name The name of the repository.
     * @return The next version id.
     */
    public int getLatestVersionID(String name)
    {
        Repository rep = getRepositoryByName(name, false);
        return rep.getNextVersionID();
    }
    
    /**
     * Get a repository by name.
     * @param name The name of the repository.
     * @param write Whether this is called for a write operation.
     * @return The Repository.
     */
    private Repository getRepositoryByName(String name, boolean write)
    {
        Repository rep = AVMContext.fgInstance.fRepositoryDAO.getByName(name);
        if (rep == null)
        {
            throw new AVMNotFoundException("Repository not found: " + name);
        }
        return rep;
    }

    /**
     * Get a descriptor for a repository root.
     * @param version The version to get.
     * @param name The name of the repository.
     * @return The descriptor for the root.
     */
    public AVMNodeDescriptor getRepositoryRoot(int version, String name)
    {
        Repository rep = getRepositoryByName(name, false);
        if (rep == null)
        {
            throw new AVMNotFoundException("Not found: " + name);
        }
        return rep.getRoot(version);
    }
 
    // TODO Fix this awful mess regarding cycle detection.
    /**
     * Lookup a node.
     * @param version The version to look under.
     * @param path The path to lookup.
     * @return A lookup object.
     */
    public Lookup lookup(int version, String path)
    {
        Integer count = fLookupCount.get();
        if (count == null)
        {
            fLookupCount.set(1);
        }
        else
        {
            fLookupCount.set(count + 1);
        }
        if (fLookupCount.get() > 50)
        {
            throw new AVMCycleException("Cycle in lookup.");
        }
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], false);
        Lookup result = rep.lookup(version, pathParts[1], false);
        if (count == null)
        {
            fLookupCount.set(null);
        }
        return result;
    }
    
    /**
     * Lookup a descriptor from a directory descriptor.
     * @param dir The directory descriptor.
     * @param name The name of the child to lookup.
     * @return The child's descriptor.
     */
    public AVMNodeDescriptor lookup(AVMNodeDescriptor dir, String name)
    {
        fLookupCount.set(0);
        AVMNode node = AVMContext.fgInstance.fAVMNodeDAO.getByID(dir.getId());
        if (node == null)
        {
            throw new AVMNotFoundException("Not found: " + dir.getId());
        }
        if (node.getType() != AVMNodeType.LAYERED_DIRECTORY &&
            node.getType() != AVMNodeType.PLAIN_DIRECTORY)
        {
            throw new AVMWrongTypeException("Not a directory.");
        }
        DirectoryNode dirNode = (DirectoryNode)node;
        return dirNode.lookupChild(dir, name);
    }
    
    /**
     * Lookup a directory specifically.
     * @param version The version to look under.
     * @param path The path to lookup.
     * @return A lookup object.
     */
    public Lookup lookupDirectory(int version, String path)
    {
        fLookupCount.set(fLookupCount.get() + 1);
        if (fLookupCount.get() > 50)
        {
            throw new AVMCycleException("Cycle in lookup.");
        }
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], false);
        return rep.lookupDirectory(version, pathParts[1], false);
    }

    /**
     * Utility to split a path, foo:bar/baz into its repository and path parts.
     * @param path The fully qualified path.
     * @return The repository name and the repository path.
     */
    private String[] SplitPath(String path)
    {
        String [] pathParts = path.split(":");
        if (pathParts.length != 2)
        {
            throw new AVMException("Invalid path: " + path);
        }
        return pathParts;
    }

    /**
     * Get the path to file storage.
     * @return The root path of file storage.
     */
    public String getStorageRoot()
    {
        return fStorage;
    }
    
    /**
     * Make a directory into a primary indirection.
     * @param path The full path.
     */
    public void makePrimary(String path)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        rep.makePrimary(pathParts[1]);
    }

    /**
     * Change what a layered directory points at.
     * @param path The full path to the layered directory.
     * @param target The new target path.
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        rep.retargetLayeredDirectory(pathParts[1], target);
    }
    
    /**
     * Get the history chain for a node.
     * @param desc The node to get history of.
     * @param count The maximum number of ancestors to traverse.  Negative means all.
     * @return A List of ancestors.
     */
    public List<AVMNodeDescriptor> getHistory(AVMNodeDescriptor desc, int count)
    {
        AVMNode node = AVMContext.fgInstance.fAVMNodeDAO.getByID(desc.getId());
        if (node == null)
        {
            throw new AVMNotFoundException("Not found.");
        }
        if (count < 0)
        {
            count = Integer.MAX_VALUE;
        }
        List<AVMNodeDescriptor> history = new ArrayList<AVMNodeDescriptor>();
        for (int i = 0; i < count; i++)
        {
            node = node.getAncestor();
            if (node == null)
            {
                break;
            }
            history.add(node.getDescriptor("UNKNOWN", "UNKNOWN", "UNKNOWN"));
        }
        return history;
    }
    
    /**
     * Set the opacity of a layered directory. An opaque directory hides
     * the things it points to via indirection.
     * @param path The path to the layered directory.
     * @param opacity True is opaque; false is not.
     */
    public void setOpacity(String path, boolean opacity)
    {
        fLookupCount.set(1);
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0], true);
        rep.setOpacity(pathParts[1], opacity);
    }
        
    /**
     * Get the RepositoryDescriptor for a Repository.
     * @param name The name of the Repository.
     * @return The descriptor.
     */
    public RepositoryDescriptor getRepositoryDescriptor(String name)
    {
        return getRepositoryByName(name, false).getDescriptor();
    }
    
    /**
     * Get the common ancestor of two nodes if one exists. Unfortunately
     * this is a quadratic problem, taking time proportional to the product
     * of the lengths of the left and right history chains.
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
        AVMNode lNode = AVMContext.fgInstance.fAVMNodeDAO.getByID(left.getId());
        AVMNode rNode = AVMContext.fgInstance.fAVMNodeDAO.getByID(right.getId());
        if (lNode == null || rNode == null)
        {
            throw new AVMNotFoundException("Node not found.");
        }
        List<AVMNode> leftHistory = new ArrayList<AVMNode>();
        while (lNode != null)
        {
            leftHistory.add(lNode);
            lNode = lNode.getAncestor();
        }
        List<AVMNode> rightHistory = new ArrayList<AVMNode>();
        while (rNode != null)
        {
            rightHistory.add(rNode);
            rNode = rNode.getAncestor();
        }
        for (AVMNode l : leftHistory)
        {
            for (AVMNode r : rightHistory)
            {
                if (l.equals(r))
                {
                    return l.getDescriptor("", "", "");
                }
            }
        }
        return null;
    }

    /**
     * Get the single instance of SuperRepository.
     * @return The single instance.
     */
    public static SuperRepository GetInstance()
    {
        return fgInstance;
    }
}
