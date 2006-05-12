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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.DirectoryNode;
import org.alfresco.repo.avm.FileContent;
import org.alfresco.repo.avm.FileNode;
import org.alfresco.repo.avm.FolderEntry;
import org.alfresco.repo.avm.LayeredDirectoryNode;
import org.alfresco.repo.avm.LayeredFileNode;
import org.alfresco.repo.avm.Lookup;
import org.alfresco.repo.avm.PlainDirectoryNode;
import org.alfresco.repo.avm.PlainFileNode;
import org.alfresco.repo.avm.Repository;
import org.alfresco.repo.avm.SuperRepository;
import org.alfresco.repo.avm.hibernate.AVMNodeBean;
import org.alfresco.repo.avm.hibernate.DirectoryEntry;
import org.alfresco.repo.avm.hibernate.DirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBeanImpl;
import org.alfresco.repo.avm.hibernate.RepositoryBean;
import org.alfresco.repo.avm.hibernate.RepositoryBeanImpl;

/**
 * @author britt
 *
 */
public class RepositoryImpl implements Repository
{
    /**
     * The data bean.
     */
    private RepositoryBean fData;
    
    /**
     * The super repository.
     */
    private SuperRepository fSuper;
    
    /**
     * Make a brand new repository.
     * @param superRepo The SuperRepository.
     * @param name The name of the Repository.
     */
    public RepositoryImpl(SuperRepository superRepo, String name)
    {
        fSuper = superRepo;
        fData = new RepositoryBeanImpl(name, null);
        fSuper.getSession().save(fData);
        PlainDirectoryNodeBean rootBean = 
            new PlainDirectoryNodeBeanImpl(fSuper.issueID(),
                                           fData.getNextVersionID(),
                                           0L,
                                           null,
                                           null,
                                           null,
                                           fData,
                                           true // is root
                                           );
        fSuper.getSession().save(rootBean);
        fData.setRoot(rootBean);
        fData.getRoots().put(fData.getNextVersionID(), rootBean);
        fData.setNextVersionID(fData.getNextVersionID() + 1);
    }
    
    /**
     * Make one from a data bean.
     * @param data The data.
     */
    public RepositoryImpl(SuperRepository superRepo, RepositoryBean data)
    {
        fData = data;
        fSuper = superRepo;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#setNew(org.alfresco.repo.avm.AVMNode)
     */
    public void setNew(AVMNode node)
    {
        fData.getNewNodes().add(node.getDataBean());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getLatestVersion()
     */
    public long getLatestVersion()
    {
        return fData.getNextVersionID();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#setNewRoot(org.alfresco.repo.avm.DirectoryNode)
     */
    public void setNewRoot(DirectoryNode root)
    {
        fData.setRoot((DirectoryNodeBean)root.getDataBean());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#createSnapshot()
     */
    public void createSnapshot()
    {
        // Walk through the new nodes and mark them not new.
        for (AVMNodeBean newBean : fData.getNewNodes())
        {
            newBean.setIsNew(false);
        }
        // Clear out the new nodes.
        fData.getNewNodes().clear();
        // Add the current root to the root history.
        fData.getRoots().put(fData.getNextVersionID(), fData.getRoot());
        // Increment the version id.
        fData.setNextVersionID(fData.getNextVersionID() + 1);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#createDirectory(java.lang.String, java.lang.String)
     */
    public void createDirectory(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
        {
            throw new AlfrescoRuntimeException("Child exists: " + name);
        }
        DirectoryNode newDir = null;
        if (lPath.isLayered())  // Creating a directory in a layered context creates
                                // a LayeredDirectoryNode that gets its indirection from
                                // its parent.
        {
            newDir = new LayeredDirectoryNode((String)null, this);
            ((LayeredDirectoryNode)newDir).setPrimaryIndirection(false);
        }
        else
        {
            newDir = new PlainDirectoryNode(this);
        }
        // TODO Untangle when you are going to set a new version id.
        newDir.setVersion(getLatestVersion() + 1);
        this.setNew(newDir);
        dir.addChild(name, newDir, lPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#createLayeredDirectory(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(String srcPath, String dstPath,
            String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
        {
            throw new AlfrescoRuntimeException("Child exists: " +  name);
        }
        LayeredDirectoryNode newDir =
            new LayeredDirectoryNode(srcPath, this);
        if (lPath.isLayered())
        {
            LayeredDirectoryNode top = lPath.getTopLayer();
            newDir.setLayerID(top.getLayerID());
        }
        else
        {
            newDir.setLayerID(fSuper.issueLayerID());
        }
        dir.addChild(name, newDir, lPath);
        newDir.setVersion(getLatestVersion() + 1);
        setNew(newDir);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#createFile(java.lang.String, java.lang.String)
     */
    public void createFile(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
        {
            throw new AlfrescoRuntimeException("Child exists: " + name);
        }
        PlainFileNode file = new PlainFileNode(this);
        file.setVersion(getLatestVersion() + 1);
        setNew(file);
        dir.addChild(name, file, lPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(String srcPath, String dstPath, String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) != null)
        {
            throw new AlfrescoRuntimeException("Child exists: " + name);
        }
        LayeredFileNode newFile =
            new LayeredFileNode(srcPath, this);
        dir.addChild(dstPath, newFile, lPath);
        newFile.setVersion(getLatestVersion() + 1);
        setNew(newFile);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getInputStream(int, java.lang.String)
     */
    public InputStream getInputStream(int version, String path)
    {
        Lookup lPath = lookup(version, path);
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AlfrescoRuntimeException("Not a file: " + path + " r " + version);
        }
        FileNode file = (FileNode)node;
        FileContent content = file.getContentForRead(version);
        return content.getInputStream(fSuper);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getListing(int, java.lang.String)
     */
    public List<FolderEntry> getListing(int version, String path)
    {
        Lookup lPath = lookupDirectory(version, path);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        Map<String, DirectoryEntry> listing = dir.getListing(lPath, version);
        ArrayList<FolderEntry> results = new ArrayList<FolderEntry>();
        for (String name : listing.keySet())
        {
            FolderEntry item = new FolderEntry();
            item.setName(name);
            item.setType(listing.get(name).getEntryType());
            results.add(item);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getOutputStream(java.lang.String)
     */
    public OutputStream getOutputStream(String path)
    {
        Lookup lPath = lookup(-1, path);
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AlfrescoRuntimeException("Not a file: " + path);
        }
        FileNode file = (FileNode)node;
        FileContent content = file.getContentForWrite(this);
        return content.getOutputStream(fSuper);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path);
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (dir.lookupChild(lPath, name, -1) == null)
        {
            throw new AlfrescoRuntimeException("Does not exist: " + name);
        }
        dir.removeChild(name, lPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#slide(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void slide(String srcPath, String srcName, String dstPath,
            String dstName)
    {
        Lookup sPath = lookup(-1, srcPath);
        if (!sPath.isLayered())
        {
            throw new AlfrescoRuntimeException("Slide not allowed from non-layered directory.");
        }
        AVMNode node = sPath.getCurrentNode();
        if (!(node instanceof LayeredDirectoryNode))
        {
            throw new AlfrescoRuntimeException("Not a layered directory: " + srcPath);
        }
        LayeredDirectoryNode srcDir = (LayeredDirectoryNode)node;
        AVMNode srcNode = srcDir.lookupChild(sPath, srcName, -1);
        if (srcNode == null)
        {
            throw new AlfrescoRuntimeException("Not found: " + srcName);
        }
        if (!(srcNode instanceof LayeredDirectoryNode))
        {
            throw new AlfrescoRuntimeException("Not a layered directory:" + srcName);
        }
        if (!sPath.isInThisLayer() || !srcDir.directlyContains(srcNode))
        {
            throw new AlfrescoRuntimeException("Not in this layer: " + srcName);
        }
        Lookup dPath = lookupDirectory(-1, dstPath);
        if (!dPath.isLayered() || dPath.getTopLayer() != sPath.getTopLayer())
        {
            throw new AlfrescoRuntimeException("Destination must be in same layer: " + dstPath);
        }
        DirectoryNode dstDir = (DirectoryNode)dPath.getCurrentNode();
        if (dstDir.lookupChild(dPath, dstName, -1) != null)
        {
            throw new AlfrescoRuntimeException("Destination exists: " + dstName);
        }
        // Remove child from src without leaving a ghost.
        LayeredDirectoryNode srcDirCopy =
            (LayeredDirectoryNode)srcDir.copyOnWrite(sPath);
        srcDirCopy.rawRemoveChildNoGhost(srcName);
        // Make a new version of source directly to be slid.
        LayeredDirectoryNode dstNode =
            new LayeredDirectoryNode((LayeredDirectoryNode)srcNode, this);
        // Relookup the destination.
        dPath = lookup(-1, dstPath);
        dstDir = (DirectoryNode)dPath.getCurrentNode();
        dstDir.addChild(dstName, dstNode, dPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getVersions()
     */
    public Set<Integer> getVersions()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getDataBean()
     */
    public RepositoryBean getDataBean()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getSuperRepository()
     */
    public SuperRepository getSuperRepository()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#lookup(int, java.lang.String)
     */
    public Lookup lookup(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#lookupDirectory(int, java.lang.String)
     */
    public Lookup lookupDirectory(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Repository#getIndirectionPath(int, java.lang.String)
     */
    public String getIndirectionPath(int version, String path)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
