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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * A layered directory node.  A layered directory node points at
 * an underlying directory, which may or may not exist.  The visible
 * contents of a layered directory node is the contents of the underlying node
 * pointed at plus those nodes added to or modified in the layered directory node minus
 * those nodes which have been deleted in the layered directory node.
 * @author britt
 */
class LayeredDirectoryNodeImpl extends DirectoryNodeImpl implements LayeredDirectoryNode
{
    static final long serialVersionUID = 4623043057918181724L;

    /**
     * The layer id.
     */
    private long fLayerID;
    
    /**
     * The pointer to the underlying directory.
     */
    private String fIndirection;
    
    /**
     * Whether this is a primary indirection node.
     */
    private boolean fPrimaryIndirection;

    /**
     * Default constructor. Called by Hibernate.
     */
    protected LayeredDirectoryNodeImpl()
    {
    }
    
    /**
     * Make a new one from a specified indirection path.
     * @param indirection The indirection path to set.
     * @param repository The repository that owns this node.
     */
    public LayeredDirectoryNodeImpl(String indirection, Repository repos)
    {
        super(repos.getSuperRepository().issueID(), repos);
        fLayerID = -1;
        fIndirection = indirection;
        fPrimaryIndirection = true;
        repos.getSuperRepository().getSession().save(this);
    }
    
    /**
     * Kind of copy constructor, sort of.
     * @param other The LayeredDirectoryNode we are copied from.
     * @param repos The Repository object we use.
     */
    @SuppressWarnings("unchecked")
    public LayeredDirectoryNodeImpl(LayeredDirectoryNode other,
                                    Repository repos)
    {
        super(repos.getSuperRepository().issueID(), repos);
        Session sess = repos.getSuperRepository().getSession();
        fIndirection = other.getUnderlying();
        fPrimaryIndirection = other.getPrimaryIndirection();
        fLayerID = -1;
        sess.save(this);
        sess.flush();
        for (ChildEntry child : other.getChildren())
        {
            ChildEntryImpl newChild = new ChildEntryImpl(child.getName(),
                                                         this,
                                                         child.getChild());
            repos.getSuperRepository().getSession().save(newChild);
        }
        for (DeletedChild dc : other.getDeleted())
        {
            DeletedChild newDel = new DeletedChildImpl(dc.getName(),
                                                       this);
            sess.save(newDel);
        }
    }
    
    /**
     * Construct one from a PlainDirectoryNode.  Called when a COW is performed in a layered
     * context.
     * @param other The PlainDirectoryNode.
     * @param repos The Repository we should belong to.
     * @param lPath The Lookup object.
     */
    @SuppressWarnings("unchecked")
    public LayeredDirectoryNodeImpl(PlainDirectoryNode other,
                                    Repository repos,
                                    Lookup lPath,
                                    boolean copyContents)
    {
        super(repos.getSuperRepository().issueID(), repos);
        fIndirection = null;
        fPrimaryIndirection = false;
        fLayerID = -1;
        Session sess = repos.getSuperRepository().getSession();
        sess.save(this);
        if (copyContents)
        {
            sess.flush();
            for (ChildEntry child : other.getChildren())
            {
                ChildEntryImpl newChild = new ChildEntryImpl(child.getName(),
                                                             this,
                                                             child.getChild());
                sess.save(newChild);
            }
        }
    }

    /**
     * Create a new layered directory based on a directory we are being named from
     * that is in not in the layer of the source lookup.
     * @param dir The directory
     * @param repo The repository
     * @param srcLookup The source lookup.
     * @param name The name of the target.
     */
    public LayeredDirectoryNodeImpl(DirectoryNode dir,
                                    Repository repo,
                                    Lookup srcLookup,
                                    String name)
    {
        super(repo.getSuperRepository().issueID(), repo);
        fIndirection = srcLookup.getIndirectionPath() + "/" + name;
        fPrimaryIndirection = true;
        fLayerID = -1;
        repo.getSuperRepository().getSession().save(this);
    }   
    
    /**
     * Is this a primary indirection node.
     * @return Whether this is a primary indirection.
     */
    public boolean getPrimaryIndirection()
    {
        return fPrimaryIndirection;
    }
    
    /**
     * Set the primary indirection state of this.
     * @param has Whether this is a primary indirection node.
     */
    public void setPrimaryIndirection(boolean has)
    {
        fPrimaryIndirection = has;
    }
    
    /**
     * Get the indirection path.
     * @return The indirection path.
     */
    public String getIndirection()
    {
        return fIndirection;
    }
    
    public String getUnderlying()
    {
        return fIndirection;
    }
    
    /**
     * Get the underlying path in the Lookup's context.
     * @param lPath The Lookup.
     * @return The underlying path.
     */
    public String getUnderlying(Lookup lPath)
    {
        if (fPrimaryIndirection)
        {
            return fIndirection;
        }
        return lPath.getCurrentIndirection();
    }
    
    /**
     * Get the layer id.
     * @return The layer id.
     */
    public long getLayerID()
    {
        return fLayerID;
    }
    
    /**
     * Set the layer id.
     * @param id The id to set.
     */
    public void setLayerID(long id)
    {
        fLayerID = id;
    }
    
    /**
     * Copy on write logic.
     * @param lPath
     * @return The copy or null.
     */
    public AVMNodeImpl possiblyCopy(Lookup lPath)
    {
        if (!lPath.needsCopying())
        {
            return null;
        }
        // Capture the repository.
        Repository repo = lPath.getRepository();
        // Otherwise we do an actual copy.
        LayeredDirectoryNodeImpl newMe = null;
        if (!lPath.isInThisLayer())
        {
            // This means that this is being seen indirectly through the topmost
            // layer.  The following creates a node that will inherit its
            // indirection from its parent.
            newMe = new LayeredDirectoryNodeImpl((String)null, 
                                                 repo);
            newMe.setPrimaryIndirection(false);
            newMe.setLayerID(lPath.getTopLayer().getLayerID());
        }
        else
        {
            // A simple copy is made.
            newMe = new LayeredDirectoryNodeImpl(this,
                                                 repo);
            newMe.setLayerID(getLayerID());
        }
        newMe.setAncestor(this);
        return newMe;
    }

    /**
     * Insert a child node without COW.
     * @param name The name to give the child.
     */
    public void putChild(String name, AVMNode node)
    {
        Session sess = SuperRepository.GetInstance().getSession();
//        sess.lock(this, LockMode.UPGRADE);
        ChildEntry entry = new ChildEntryImpl(name, this, node);
        ChildEntry existing = (ChildEntry)sess.get(ChildEntryImpl.class, (Serializable)entry);
        if (existing != null)
        {
            existing.setChild(node);
        }
        else
        {
            sess.save(entry);
        }
        DeletedChild dc = getDeleted("name");
        if (dc != null)
        {
            sess.delete(dc);
        }
    }

    /**
     * Add a child to this directory and possibly COW.
     * @param name The name of the child to add.
     * @param child The child to add.
     * @param lPath The Lookup.
     * @return Whether the child was successfully added.
     */
    public boolean addChild(String name, AVMNode child, Lookup lPath)
    {
        if (getChild(name) != null)
        {
            return false;
        }
        if (getDeleted(name) == null)
        {
            try
            {
                Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(-1, getUnderlying(lPath));
                DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
                if (dir.lookupChild(lookup, name, -1) != null)
                {
                    return false;
                }
            }
            catch (AVMException re)
            {
                if (re instanceof AVMCycleException)
                {
                    throw re;
                }
                // Do nothing.
            }
        }
        DirectoryNode toModify = (DirectoryNode)copyOnWrite(lPath);
        toModify.putChild(name, child);
        child.setParent(toModify);
        child.setRepository(lPath.getRepository());
        return true;
    }

    /**
     * Does this node directly contain the indicated node.
     * @param node The node we are checking.
     * @return Whether node is directly contained.
     */
    public boolean directlyContains(AVMNode node)
    {
        return getChild(node) != null;
    }

    /**
     * Get a listing of the virtual contents of this directory.
     * @param lPath The Lookup.
     * @return A Map from names to nodes. This is a sorted Map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, AVMNode> getListing(Lookup lPath)
    {
        // Get the base listing from the thing we indirect to.
        Map<String, AVMNode> baseListing = null;
        try
        {
            Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(-1, getUnderlying(lPath));
            DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
            baseListing = dir.getListing(lookup);
        }
        catch (AVMException re)
        {
            if (re instanceof AVMCycleException)
            {
                throw re;
            }
            // It's OK for an indirection to dangle.
            baseListing = new HashMap<String, AVMNode>();
        }
        // Filter the base listing by taking out anything in the deleted Set.
        Map<String, AVMNode> listing = new TreeMap<String, AVMNode>();
        for (String name : baseListing.keySet())
        {
            if (getDeleted(name) != null)
            {
                continue;
            }
            listing.put(name, baseListing.get(name));
        }
        for (ChildEntry entry : getChildren())
        {
            listing.put(entry.getName(), entry.getChild());
        }
        return listing;
    }

    /**
     * Get a listing from a directory node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public Map<String, AVMNodeDescriptor> getListing(AVMNodeDescriptor dir)
    {
        if (dir.getPath() == null || dir.getIndirection() == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        Map<String, AVMNodeDescriptor> baseListing = new TreeMap<String, AVMNodeDescriptor>();
        try
        {
            Lookup lookup = SuperRepository.GetInstance().lookupDirectory(-1, dir.getIndirection());
            DirectoryNode dirNode = (DirectoryNode)lookup.getCurrentNode();
            Map<String, AVMNode> listing = dirNode.getListing(lookup);
            for (String name : listing.keySet())
            {
                baseListing.put(name,
                                listing.get(name).getDescriptor(dir.getPath(), name,
                                                                lookup.getCurrentIndirection()));
            }
        }
        catch (AVMException e)
        {
            if (e instanceof AVMCycleException)
            {
                throw e;
            }
        }
        List<DeletedChild> deleted = getDeleted();
        for (DeletedChild child : deleted)
        {
            baseListing.remove(child.getName());
        }
        List<ChildEntry> children = getChildren();
        for (ChildEntry child : children)
        {
            baseListing.put(child.getName(),
                            child.getChild().getDescriptor(dir.getPath(),
                                                           child.getName(),
                                                           dir.getIndirection()));
        }
        return baseListing;
    }

    /**
     * Lookup a child by name. 
     * @param lPath The Lookup.
     * @param name The name we are looking.
     * @param version The version in which we are looking.
     * @return The child or null if not found.
     */
    @SuppressWarnings("unchecked")
    public AVMNode lookupChild(Lookup lPath, String name, int version)
    {
        // If the name has been deleted quickly return.
        if (getDeleted(name) != null)
        {
            return null;
        }
        ChildEntry entry = getChild(name);
        if (entry != null)
        {
            return AVMNodeUnwrapper.Unwrap(entry.getChild());
        }
        // Not here so check our indirection.
        try
        {
            Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(-1, getUnderlying(lPath));
            DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
            return dir.lookupChild(lookup, name, -1);
        }
        catch (AVMException re)
        {
            if (re instanceof AVMCycleException)
            {
                throw re;
            }
            return null;
        }
    }

    /**
     * Lookup a child using a node descriptor as context.
     * @param mine The node descriptor for this,
     * @param name The name to lookup,
     * @return The node descriptor.
     */
    public AVMNodeDescriptor lookupChild(AVMNodeDescriptor mine, String name)
    {
        if (mine.getPath() == null || mine.getIndirection() == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        if (getDeleted(name) != null)
        {
            return null;
        }
        ChildEntry entry = getChild(name);
        if (entry != null)
        {
            return entry.getChild().getDescriptor(mine.getPath(),
                                                  name,
                                                  mine.getIndirection());
        }
        try
        {
            Lookup lookup = SuperRepository.GetInstance().lookupDirectory(-1, mine.getIndirection());
            DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
            AVMNode child = dir.lookupChild(lookup, name, -1);
            if (child == null)
            {
                return null;
            }
            return child.getDescriptor(lookup);
        }
        catch (AVMException e)
        {
            if (e instanceof AVMCycleException)
            {
                throw e;
            }
            return null;
        }
    }

    /**
     * Directly remove a child. Do not COW. Do not pass go etc.
     * @param name The name of the child to remove.
     */
    @SuppressWarnings("unchecked")
    public void rawRemoveChild(String name)
    {
        ChildEntry entry = getChild(name);
        if (entry != null)
        {
            SuperRepository.GetInstance().getSession().delete(entry);
        }
        DeletedChild dc = new DeletedChildImpl(name,
                                               this);
        SuperRepository.GetInstance().getSession().save(dc);
    }
    
    /**
     * Remove a child by name.  Possibly COW.
     * @param name The name of the child to remove.
     * @param lPath The Lookup.
     * @return Whether the child was successfully removed.
     */
    @SuppressWarnings("unchecked")
    public boolean removeChild(String name, Lookup lPath)
    {
        // Can't delete something that is already deleted.
        if (getDeleted(name) != null)
        {
            return false;
        }
        ChildEntry entry = getChild(name);
        if (entry == null)
        {
            // See if the name is seen via indirection.
            try
            {
                Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(-1, getUnderlying(lPath));
                DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
                if (dir.lookupChild(lookup, name, -1) == null)
                {
                    return false;
                }
            }
            catch (AVMException re)
            {
                if (re instanceof AVMCycleException)
                {
                    throw re;
                }
                return false;
            }
        }
        LayeredDirectoryNode toModify =
            (LayeredDirectoryNode)copyOnWrite(lPath);
        toModify.rawRemoveChild(name);
        return true;
    }

    /**
     * Get the type of this node.
     * @return The type of this node.
     */
    public int getType()
    {
        return AVMNodeType.LAYERED_DIRECTORY;
    }

    /**
     * For diagnostics. Get a String representation.
     * @param lPath The Lookup.
     * @return A String representation.
     */
    public String toString(Lookup lPath)
    {
        return "[LD:" + getId() + ":" + getUnderlying(lPath) + "]";
    }
    
    /**
     * Set the primary indirection. No COW.
     * @param path The indirection path.
     */
    public void rawSetPrimary(String path)
    {
        fIndirection = path;
        fPrimaryIndirection = true;
    }
    
    /**
     * Make this node become a primary indirection.  COW.
     * @param lPath The Lookup.
     */
    public void turnPrimary(Lookup lPath)
    {
        String path = lPath.getCurrentIndirection();
        LayeredDirectoryNode toModify = (LayeredDirectoryNode)copyOnWrite(lPath);
        toModify.rawSetPrimary(path);
    }

    /**
     * Make this point at a new target.
     * @param lPath The Lookup.
     */
    public void retarget(Lookup lPath, String target)
    {
        LayeredDirectoryNode toModify = (LayeredDirectoryNode)copyOnWrite(lPath);
        toModify.rawSetPrimary(target);
    }
    
    /**
     * Let anything behind name in this become visible.
     * @param lPath The Lookup.
     * @param name The name to uncover.
     */
    public void uncover(Lookup lPath, String name)
    {
        LayeredDirectoryNodeImpl toModify = (LayeredDirectoryNodeImpl)copyOnWrite(lPath);
        DeletedChild dc = toModify.getDeleted(name);
        if (dc != null)
        {
            lPath.getRepository().getSuperRepository().getSession().delete(dc);
        }
    }
    
    /**
     * Get the descriptor for this node.
     * @param The Lookup.
     * @return A descriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath)
    {
        BasicAttributes attrs = getBasicAttributes();
        return new AVMNodeDescriptor(lPath.getRepresentedPath(),
                                     AVMNodeType.LAYERED_DIRECTORY,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getVersionID(),
                                     getUnderlying(lPath),
                                     fPrimaryIndirection,
                                     fLayerID,
                                     -1);
    }

    /**
     * Get a descriptor for this.
     * @param parentPath The parent path.
     * @param name The name this was looked up with.
     * @param parentIndirection The indirection of the parent.
     * @return
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        String indirection = null;
        if (fPrimaryIndirection)
        {
            indirection = fIndirection;
        }
        else
        {
            indirection = parentIndirection.endsWith("/") ? parentIndirection + name : 
                parentIndirection + "/" + name;
        }
        return new AVMNodeDescriptor(path,
                                     AVMNodeType.LAYERED_DIRECTORY,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getVersionID(),
                                     indirection,
                                     fPrimaryIndirection,
                                     fLayerID,
                                     -1);
    }

    /**
     * Set the indirection.
     * @param indirection
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }

    /**
     * Get the deleted child entry for a given name.
     * @param name The name to look for.
     * @return A DeletedChild object.
     */
    @SuppressWarnings("unchecked")
    private DeletedChild getDeleted(String name)
    {
        Query query = SuperRepository.GetInstance().getSession().getNamedQuery("DeletedChild.ByNameParent");
        query.setString("name", name);
        query.setEntity("parent", this);
        query.setCacheable(true);
        query.setCacheRegion("DeletedChild.ByNameParent");
        List<DeletedChild> dc = (List<DeletedChild>)query.list();
        if (dc.size() == 0)
        {
            return null;
        }
        return dc.get(0);
    }
    
    /**
     * Get all the deleted entries in this directory.
     * @return A List of DeletedEntry objects.
     */
    @SuppressWarnings("unchecked")
    public List<DeletedChild> getDeleted()
    {
        Query query = SuperRepository.GetInstance().getSession().getNamedQuery("DeletedChild.ByParent");
        query.setEntity("parent", this);
        query.setCacheable(true);
        query.setCacheRegion("DeletedChild.ByParent");
        return (List<DeletedChild>)query.list();
    }

    /**
     * Does nothing because LayeredDirectoryNodes can't be roots.
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot)
    {
    }
}
