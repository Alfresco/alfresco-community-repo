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

package org.alfresco.repo.avm.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.DirectoryNode;
import org.alfresco.repo.avm.FileNode;
import org.alfresco.repo.avm.FolderEntry;
import org.alfresco.repo.avm.LayeredDirectoryNode;
import org.alfresco.repo.avm.LayeredFileNode;
import org.alfresco.repo.avm.Lookup;
import org.alfresco.repo.avm.PlainDirectoryNode;
import org.alfresco.repo.avm.PlainFileNode;
import org.alfresco.repo.avm.Repository;
import org.alfresco.repo.avm.SuperRepository;
import org.alfresco.repo.avm.hibernate.Issuer;
import org.alfresco.repo.avm.hibernate.RepositoryBean;
import org.alfresco.repo.avm.hibernate.RepositoryBeanImpl;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Implementation of a SuperRepository.  This is a per thread
 * class.
 * @author britt
 */
public class SuperRepositoryImpl implements SuperRepository
{
    /**
     * The Hibernate Session associated with the current operation.
     */
    private Session fSession;
    
    /**
     * The node id issuer.
     */
    private Issuer fNodeIssuer;
    
    /**
     * The content id issuer;
     */
    private Issuer fContentIssuer;
    
    /**
     * The branch id issuer.
     */
    private Issuer fBranchIssuer;
    
    /**
     * The layer id issuer.
     */
    private Issuer fLayerIssuer;

    /**
     * The file storage directory.
     */
    private String fStorage;
    
    // TODO Issuers are handled in a repugnant manner here.  Something better
    // would be nice.
    /**
     * Make a new one, initialized with the session.
     * @param session The session for this operation.
     * @param storage Where file data gets stored.
     */
    public SuperRepositoryImpl(Session session, String storage)
    {
        fSession = session;
        fStorage = storage;
        fNodeIssuer = null;
        fContentIssuer = null;
        fBranchIssuer = null;
        fLayerIssuer = null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createFile(java.lang.String, java.lang.String)
     */
    public void createFile(String path, String name)
    {
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        rep.createFile(pathParts[1], name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createDirectory(java.lang.String, java.lang.String)
     */
    public void createDirectory(String path, String name)
    {
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        rep.createDirectory(pathParts[1], name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createLayeredDirectory(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(String srcPath, String dstPath,
            String name)
    {
        String[] pathParts = SplitPath(dstPath);
        Repository rep = getRepositoryByName(pathParts[0]);
        rep.createLayeredDirectory(srcPath, pathParts[1], name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createLayerdFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(String srcPath, String dstPath, String name)
    {
        String[] pathParts = SplitPath(dstPath);
        Repository rep = getRepositoryByName(pathParts[0]);
        rep.createLayeredFile(srcPath, pathParts[1], name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createRepository(java.lang.String)
     */
    public void createRepository(String name)
    {
        // Newing up the object causes it to be written to the db.
        @SuppressWarnings("unused") 
        Repository rep = new RepositoryImpl(this, name);
        // Special handling for repository creation.
        rep.getDataBean().getRoot().setIsNew(false);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(int version, String srcPath, String dstPath, String name)
    {
        // Lookup the src node.
        String [] pathParts = SplitPath(srcPath);
        Repository srcRepo = getRepositoryByName(pathParts[0]);
        Lookup sPath = srcRepo.lookup(version, pathParts[1]);
        // Lookup the destination directory.
        pathParts = SplitPath(dstPath);
        Repository dstRepo = getRepositoryByName(pathParts[0]);
        Lookup dPath = dstRepo.lookupDirectory(-1, pathParts[1]);
        DirectoryNode dirNode = (DirectoryNode)dPath.getCurrentNode();
        AVMNode srcNode = sPath.getCurrentNode();
        AVMNode dstNode = null;
        // We do different things depending on what kind of thing we're 
        // branching from. I'd be considerably happier if we disallowed
        // certain scenarios, but Jon won't let me :P (bhp).
        if (srcNode instanceof PlainDirectoryNode)
        {
            dstNode = new PlainDirectoryNode((PlainDirectoryNode)srcNode, dstRepo);
        }
        else if (srcNode instanceof LayeredDirectoryNode)
        {
            dstNode = 
                new LayeredDirectoryNode((LayeredDirectoryNode)srcNode, dstRepo);
        }
        else if (srcNode instanceof LayeredFileNode)
        {
            dstNode = new LayeredFileNode((LayeredFileNode)srcNode, dstRepo);
        }
        else // This is a plain file.
        {
            dstNode = new PlainFileNode((PlainFileNode)srcNode, dstRepo);
        }
        dstNode.setVersion(dstRepo.getLatestVersion() + 1);
        dstRepo.setNew(dstNode);
        dstNode.setAncestor(srcNode);
        dstNode.setBranchID(issueBranchID());
        dirNode.addChild(name, dstNode, dPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getOutputStream(java.lang.String)
     */
    public OutputStream getOutputStream(String path)
    {
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        return rep.getOutputStream(pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(String srcPath, String srcName, String dstPath,
            String dstName)
    {
        // This is about as ugly as it gets.  
        String [] pathParts = SplitPath(srcPath);
        Repository srcRepo = getRepositoryByName(pathParts[0]);
        Lookup sPath = srcRepo.lookupDirectory(-1, pathParts[1]);
        DirectoryNode srcDir = (DirectoryNode)sPath.getCurrentNode();
        AVMNode srcNode = srcDir.lookupChild(sPath, srcName, -1);
        if (srcNode == null)
        {
            throw new AlfrescoRuntimeException("Not found: " + srcName);
        }
        pathParts = SplitPath(dstPath);
        Repository dstRepo = getRepositoryByName(pathParts[0]);
        Lookup dPath = dstRepo.lookupDirectory(-1, pathParts[1]);
        DirectoryNode dstDir = (DirectoryNode)dPath.getCurrentNode();
        AVMNode dstNode = dstDir.lookupChild(dPath, dstName, -1);
        if (dstNode != null)
        {
            throw new AlfrescoRuntimeException("Node exists: " + dstName);
        }
        // We've passed the check, so we can go ahead and do the rename.
        if (srcNode instanceof PlainDirectoryNode)
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
                if (dPath.isLayered() && dPath.getTopLayer() == sPath.getTopLayer())
                {
                    dstNode = new LayeredDirectoryNode((PlainDirectoryNode)srcNode, dstRepo, sPath);
                }
                else
                {
                    dstNode = new LayeredDirectoryNode((DirectoryNode)srcNode, dstRepo, sPath, srcName);
                }
            }
            else
            {
                dstNode = new PlainDirectoryNode((PlainDirectoryNode)srcNode, dstRepo);
            }
        }
        else if (srcNode instanceof LayeredDirectoryNode)
        {
            // TODO I think I need to subdivide this logic again.
            // based on whether the destination is a layer or not.
            if (!sPath.isLayered() || (sPath.isInThisLayer() &&
                srcDir instanceof LayeredDirectoryNode &&
                ((LayeredDirectoryNode)srcDir).directlyContains(srcNode)))
            {
                // Use the simple 'copy' constructor.
                dstNode =
                    new LayeredDirectoryNode((LayeredDirectoryNode)srcNode, dstRepo);
                ((LayeredDirectoryNode)dstNode).setLayerID(((LayeredDirectoryNode)srcNode).getLayerID());
            }
            else
            {
                // The thing we are renaming does not belong to sPath's layer and therefore
                // we need to compute the indirection path for this layer after the rename.
                dstNode =
                    new LayeredDirectoryNode((DirectoryNode)srcNode, dstRepo, sPath, srcName);
                ((LayeredDirectoryNode)dstNode).setLayerID(issueLayerID());
            }
        }
        else if (srcNode instanceof LayeredFileNode)
        {
            if (!sPath.isLayered() || (sPath.isInThisLayer() &&
                srcDir instanceof LayeredDirectoryNode &&
                ((LayeredDirectoryNode)srcDir).directlyContains(srcNode)))
            {
                // Use the simple 'copy' constructor.
                dstNode =
                    new LayeredFileNode((LayeredFileNode)srcNode, dstRepo);
            }
            else
            {   
                // Calculate the indirection path, because srcNode was not in this layer.
                dstNode = 
                    new LayeredFileNode((FileNode)srcNode, dstRepo, sPath, srcName);
            }
        }
        else // This is a plain file node.
        {
            dstNode = new PlainFileNode((PlainFileNode)srcNode, dstRepo);
        }
        dstNode.setVersion(dstRepo.getLatestVersion() + 1);
        dstRepo.setNew(dstNode);
        dstDir.addChild(dstName, dstNode, dPath);
        dstNode.setAncestor(srcNode);
        pathParts = SplitPath(srcPath);
        sPath = srcRepo.lookup(-1, pathParts[1]);
        srcDir = (DirectoryNode)sPath.getCurrentNode();
        srcDir.removeChild(srcName, sPath);
    }

    // TODO Should we allow cross-repository sliding.  Tentatively no, because
    // it serves no earthly purpose. God knows we need to trim the combinatorial
    // tree of possibilities.
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#slide(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void slide(String srcPath, String srcName, String dstPath,
            String dstName)
    {
        String[] srcPathParts = SplitPath(srcPath);
        String[] dstPathParts = SplitPath(dstPath);
        if (!srcPathParts[0].equals(dstPathParts[0]))
        {
            throw new AlfrescoRuntimeException("Argument must be in same Repository: " + srcPathParts[0] + "!=" +
                                                                                         dstPathParts[0]);
        }
        Repository repo = getRepositoryByName(srcPathParts[0]);
        repo.slide(srcPathParts[1], srcName, dstPathParts[1], dstName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createSnapshot(java.util.List)
     */
    public void createSnapshot(List<String> repositories)
    {
        for (String repName : repositories)
        {
            Repository repo = getRepositoryByName(repName);
            repo.createSnapshot();
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createSnapshot(java.lang.String)
     */
    public void createSnapshot(String repository)
    {
        Repository repo = getRepositoryByName(repository);
        repo.createSnapshot();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#remove(java.lang.String, java.lang.String)
     */
    public void remove(String path, String name)
    {
        String [] pathParts = SplitPath(path);
        Repository repo = getRepositoryByName(pathParts[0]);
        repo.removeNode(pathParts[1], name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#purgeRepository(java.lang.String)
     */
    public void purgeRepository(String name)
    {
        // TODO Leave until later.  Need to set up GC thread to handle this.
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#purgeVersion(java.lang.String, int)
     */
    public void purgeVersion(String name, int version)
    {
        // TODO Leave until later.  Need to set up GC thread to handle this.
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getInputStream(java.lang.String)
     */
    public InputStream getInputStream(int version, String path)
    {
        String [] pathParts = SplitPath(path);
        Repository repo = getRepositoryByName(pathParts[0]);
        return repo.getInputStream(version, pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getListing(java.lang.String)
     */
    public List<FolderEntry> getListing(int version, String path)
    {
        String [] pathParts = SplitPath(path);
        Repository repo = getRepositoryByName(pathParts[0]);
        return repo.getListing(version, pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getRepositoryNames()
     */
    @SuppressWarnings("unchecked")
    public List<String> getRepositoryNames()
    {
        Query query = fSession.createQuery("select r.name from RepositoryBeanImpl r");
        return (List<String>)query.list();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getRepositoryVersions(java.lang.String)
     */
    public Set<Integer> getRepositoryVersions(String name)
    {
        Repository rep = getRepositoryByName(name);
        return rep.getVersions();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#issueID()
     */
    public long issueID()
    {
        if (fNodeIssuer == null)
        {
            fNodeIssuer = (Issuer)fSession.get(Issuer.class, "node");
        }
        return fNodeIssuer.issue();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#issueContentID()
     */
    public long issueContentID()
    {
        if (fContentIssuer == null)
        {
            fContentIssuer = (Issuer)fSession.get(Issuer.class, "content");
        }
        return fContentIssuer.issue();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#issueLayerID()
     */
    public long issueLayerID()
    {
        if (fLayerIssuer == null)
        {
            fLayerIssuer = (Issuer)fSession.get(Issuer.class, "layer");
        }
        return fLayerIssuer.issue();
    }
    
    private long issueBranchID()
    {
        if (fBranchIssuer == null)
        {
            fBranchIssuer = (Issuer)fSession.get(Issuer.class, "branch");
        }
        return fBranchIssuer.issue();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getSession()
     */
    public Session getSession()
    {
        return fSession;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#createContentOutputStream(int)
     */
    public OutputStream createContentOutputStream(String path)
    {
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        return rep.getOutputStream(pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getContentInputStream(int)
     */
    public InputStream getContentInputStream(int version, String path)
    {
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        return rep.getInputStream(version, pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#destroyRepository(java.lang.String)
     */
    public void destroyRepository(String name)
    {
        // TODO Auto-generated method stub
        // Leave this until we have GC in place.
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getIndirectionPath(int, java.lang.String)
     */
    public String getIndirectionPath(int version, String path)
    {
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        return rep.getIndirectionPath(version, pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getLatestVersionID(java.lang.String)
     */
    public int getLatestVersionID(String name)
    {
        Repository rep = getRepositoryByName(name);
        return rep.getLatestVersion();
    }
    
    /**
     * Get a repository by name.
     * @param name The name of the repository.
     * @return The Repository.
     */
    private Repository getRepositoryByName(String name)
    {
/*
        Query query = fSession.createQuery("from RepositoryBeanImpl r where r.name = :name");
        query.setString("name", name);
        return new RepositoryImpl(this, (RepositoryBean)query.uniqueResult());
*/
        return new RepositoryImpl(this, (RepositoryBean)fSession.get(RepositoryBeanImpl.class, name));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#lookup(int, java.lang.String)
     */
    public Lookup lookup(int version, String path)
    {
        String [] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        return rep.lookup(version, pathParts[1]);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#lookupDirectory(int, java.lang.String)
     */
    public Lookup lookupDirectory(int version, String path)
    {
        String[] pathParts = SplitPath(path);
        Repository rep = getRepositoryByName(pathParts[0]);
        return rep.lookupDirectory(version, pathParts[1]);
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
            throw new AlfrescoRuntimeException("Invalid path: " + path);
        }
        return pathParts;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.SuperRepository#getStorageRoot()
     */
    public String getStorageRoot()
    {
        return fStorage;
    }
}
