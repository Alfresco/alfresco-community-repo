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

/**
 * Base class for all repository file system like objects.
 * @author britt
 */
abstract class AVMNodeImpl implements AVMNode, Serializable
{
    /**
     * The Object ID.
     */
    private long fID;
    
    /**
     * The Version ID.
     */
    private int fVersionID;
    
    /**
     * The ancestor of this.
     */
    private AVMNode fAncestor;
    
    /**
     * The node that was merged into this.
     */
    private AVMNode fMergedFrom;
    
    /**
     * The Repository that owns this.
     */
    private Repository fRepository;
    
    /**
     * The basic attributes of this.  Owner, creator, mod time, etc.
     */
    private BasicAttributes fBasicAttributes;
    
    /**
     * Whether this node is new (and should therefore not be COWed).
     */
    private boolean fIsNew;
    
    /**
     * The version number (for concurrency control).
     */
    private long fVers;
    
    /**
     * The rootness of this node.
     */
    private boolean fIsRoot;
    
    /**
     * Default constructor.
     */
    protected AVMNodeImpl()
    {
    }

    /**
     * Constructor used when creating a new concrete subclass instance.
     * @param id The object id.
     * @param repo The Repository that owns this.
     */
    protected AVMNodeImpl(long id,
                          Repository repo)
    {
        fID = id;
        fVersionID = -1;
        fAncestor = null;
        fMergedFrom = null;
        fRepository = repo;
        fIsRoot = false;
        long time = System.currentTimeMillis();
        fBasicAttributes = new BasicAttributesImpl("britt",
                                                       "britt",
                                                       "britt",
                                                       time,
                                                       time,
                                                       time);
        fIsNew = true;
    }
    
    /**
     * Set the ancestor of this node.
     * @param ancector The ancestor to set.
     */
    public void setAncestor(AVMNode ancestor)
    {
        fAncestor = ancestor;
    }

    /**
     * Get the ancestor of this node.
     * @return The ancestor of this node.
     */
    public AVMNode getAncestor()
    {
        return fAncestor;
    }
    
    /**
     * Set the node that was merged into this.
     * @param mergedFrom The node that was merged into this.
     */
    public void setMergedFrom(AVMNode mergedFrom)
    {
        fMergedFrom = mergedFrom;
    }
    
    /**
     * Get the node that was merged into this.
     * @return The node that was merged into this.
     */
    public AVMNode getMergedFrom()
    {
        return fMergedFrom;
    }
    
    /**
     * Perform a copy on write on this node and recursively
     * up to the repository root.  This is a template method
     * which farms out work to possiblyCopy().
     * @param lPath The Lookup.
     */
    public AVMNode copyOnWrite(Lookup lPath)
    {
        // Call the subclass's copy on write logic.
        AVMNode newMe = possiblyCopy(lPath);
        // No copying needed, so short circuit.
        if (newMe == null)
        {
            return this;
        }
        String myName = lPath.getName();
        lPath.upCurrentNode();
        Repository repos = lPath.getRepository();
        newMe.setVersionID(repos.getNextVersionID());
        // Get our parent directory if we have one.
        DirectoryNode parent = null;
        if (!getIsRoot())
        {
            parent = (DirectoryNode)lPath.getCurrentNode();
        }
        if (parent != null)  
        {
            // Recursive invocation.
            DirectoryNode newParent =
                (DirectoryNode)parent.copyOnWrite(lPath);
            newParent.putChild(myName, newMe);
        }
        else // Null parent means root of repository.
        {
            repos.setNewRoot((DirectoryNode)newMe);
        }
        newMe.setRepository(repos);
        newMe.setIsNew(true);
        return newMe;
    }

    /**
     * Set the owning repository for this.
     * @param repo The owning repository.
     */
    public void setRepository(Repository repo)
    {
        fRepository = repo;
    }

    /**
     * Get the repository that owns this.
     * @return The repository.
     */
    public Repository getRepository()
    {
        return fRepository;
    }
    
    /**
     * Equality based on object ids.
     * @param obj The thing to compare against.
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMNode))
        {
            System.err.println("Failing AVMNodeImpl.equals");
            return false;
        }
        return fID == ((AVMNode)obj).getId();
    }

    /**
     * Get a reasonable hash value.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return (int)fID;
    }
    
    /**
     * Set the object id.  For Hibernate.
     * @param id The id to set.
     */
    protected void setId(long id)
    {
        fID = id;
    }
    
    /**
     * Get the id of this node.
     * @return The object id.
     */
    public long getId()
    {
        return fID;
    }
 
    /**
     * Set the versionID for this node.  
     * @param versionID The id to set.
     */
    public void setVersionID(int versionID)
    {
        fVersionID = versionID;
    }
    
    /**
     * Get the version id of this node.
     * @return The version id.
     */
    public int getVersionID()
    {
        return fVersionID;
    }
    
    /**
     * Set the basic attributes. For Hibernate.
     * @param attrs
     */
    protected void setBasicAttributes(BasicAttributes attrs)
    {
        fBasicAttributes = attrs;
    }
    
    /**
     * Get the basic attributes.
     * @return The basic attributes.
     */
    public BasicAttributes getBasicAttributes()
    {
        return fBasicAttributes;
    }
    
    /**
     * Set whether this is new.
     * @param isNew Whether this is new.
     */
    public void setIsNew(boolean isNew)
    {
        fIsNew = isNew;
    }

    /**
     * Get whether this is a new node.
     * @return Whether this is new.
     */
    public boolean getIsNew()
    {
        return fIsNew;
    }
 
    /**
     * Set the version for concurrency control
     * @param vers
     */
    protected void setVers(long vers)
    {
        fVers = vers;
    }
    
    /**
     * Get the version for concurrency control.
     * @return
     */
    protected long getVers()
    {
        return fVers;
    }

    /**
     * @return
     */
    public boolean getIsRoot()
    {
        return fIsRoot;
    }

    /**
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot)
    {
        fIsRoot = isRoot;
    }
}
