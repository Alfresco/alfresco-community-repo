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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.hibernate.BasicAttributesBean;
import org.alfresco.repo.avm.hibernate.BasicAttributesBeanImpl;
import org.alfresco.repo.avm.hibernate.DirectoryEntry;
import org.alfresco.repo.avm.hibernate.LayeredDirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.LayeredDirectoryNodeBeanImpl;

/**
 * A layered directory node.  A layered directory node points at
 * an underlying directory, which may or may not exist.  The visible
 * contents of a layered directory node is the contents of the underlying node
 * pointed at plus those nodes added to or modified in the layered directory node minus
 * those nodes which have been deleted in the layered directory node.
 * @author britt
 */
public class LayeredDirectoryNode extends DirectoryNode implements Layered
{
    /**
     * The underlying bean data.
     */
    private LayeredDirectoryNodeBean fData;
    
    /**
     * Make one up from Bean data.
     * @param data The bean with the persistent data.
     */
    public LayeredDirectoryNode(LayeredDirectoryNodeBean data)
    {
        fData = data;
        setDataBean(data);
    }
    
    /**
     * Make a new one from a specified indirection path.
     * @param indirection The indirection path to set.
     * @param repository The repository that owns this node.
     */
    public LayeredDirectoryNode(String indirection, Repository repos)
    {
        // Set up basic attributes for this node.
        long time = System.currentTimeMillis();
        // TODO We'll fix this up when Britt understands user management.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl("britt",
                                                                "britt",
                                                                "britt",
                                                                time,
                                                                time,
                                                                time);
        fData = new LayeredDirectoryNodeBeanImpl(repos.getSuperRepository().issueID(),
                                                 -1,
                                                 0,
                                                 null,
                                                 null,
                                                 null,
                                                 repos.getDataBean(),
                                                 attrs,
                                                 -1,
                                                 true,
                                                 indirection);
        setDataBean(fData);
        repos.getSuperRepository().getSession().save(fData);
    }
    
    /**
     * Kind of copy constructor, sort of.
     * @param other The LayeredDirectoryNode we are copied from.
     * @param repos The Repository object we use.
     */
    public LayeredDirectoryNode(LayeredDirectoryNode other,
                                Repository repos)
    {
        LayeredDirectoryNodeBean thatBean = (LayeredDirectoryNodeBean)other.getDataBean();
        // Copy the basic attributes and update.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl(thatBean.getBasicAttributes());
        long time = System.currentTimeMillis();
        attrs.setCreateDate(time);
        attrs.setModDate(time);
        attrs.setAccessDate(time);
        attrs.setLastModifier("britt");
        fData = new LayeredDirectoryNodeBeanImpl(repos.getSuperRepository().issueID(),
                                                 -1,
                                                 0,
                                                 null,
                                                 null,
                                                 null,
                                                 repos.getDataBean(),
                                                 attrs,
                                                 -1,
                                                 thatBean.getPrimaryIndirection(),
                                                 other.getUnderlying());
        setDataBean(fData);
        fData.setAdded(new HashMap<String, DirectoryEntry>(thatBean.getAdded()));
        fData.setDeleted(new HashSet<String>(thatBean.getDeleted()));
        // fData.setPrimaryIndirection(thatBean.getPrimaryIndirection());
        repos.getSuperRepository().getSession().save(fData);
    }
    
    /**
     * Construct one from a PlainDirectoryNode.  Called when a COW is performed in a layered
     * context.
     * @param other The PlainDirectoryNode.
     * @param repos The Repository we should belong to.
     * @param lPath The Lookup object.
     */
    public LayeredDirectoryNode(PlainDirectoryNode other,
                                Repository repos,
                                Lookup lPath)
    {
        // TODO Fix this yada, yada.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl(other.getDataBean().getBasicAttributes());
        long time = System.currentTimeMillis();
        attrs.setModDate(time);
        attrs.setAccessDate(time);
        attrs.setLastModifier("britt");
        fData = new LayeredDirectoryNodeBeanImpl(repos.getSuperRepository().issueID(),
                                                 -1,
                                                 0,
                                                 null,
                                                 null,
                                                 null,
                                                 repos.getDataBean(),
                                                 attrs,
                                                 -1,
                                                 false,
                                                 null);
        setDataBean(fData);
        // TODO Is this right?  I don't think so.
        // fData.setAdded(other.getListing(lPath, -1));
        // fData.setPrimaryIndirection(false);
        repos.getSuperRepository().getSession().save(fData);
    }

    /**
     * Create a new layered directory based on a directory we are being named from
     * that is in not in the layer of the source lookup.
     * @param dir The directory
     * @param repo The repository
     * @param srcLookup The source lookup.
     * @param name The name of the target.
     */
    public LayeredDirectoryNode(DirectoryNode dir,
                                Repository repo,
                                Lookup srcLookup,
                                String name)
    {
        // Make BasicAttributes and set them correctly.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl(dir.getDataBean().getBasicAttributes());
        long time = System.currentTimeMillis();
        attrs.setCreateDate(time);
        attrs.setModDate(time);
        attrs.setAccessDate(time);
        attrs.setCreator("britt");
        attrs.setLastModifier("britt");
        fData = new LayeredDirectoryNodeBeanImpl(repo.getSuperRepository().issueID(),
                                                 -1,
                                                 0,
                                                 null,
                                                 null,
                                                 null,
                                                 repo.getDataBean(),
                                                 attrs,
                                                 -1,
                                                 true,
                                                 srcLookup.getIndirectionPath() + "/" + name);
        setDataBean(fData);
        repo.getSuperRepository().getSession().save(fData);
    }   
    
    /**
     * Does this node have a primary indirection.
     * @returns Whether this is a primary indirection.
     */
    public boolean hasPrimaryIndirection()
    {
        return fData.getPrimaryIndirection();
    }
    
    /**
     * Set whether this has a primary indirection.
     * @param has Whether this has a primary indirection.
     */
    public void setPrimaryIndirection(boolean has)
    {
        fData.setPrimaryIndirection(has);
    }
    
    /**
     * Get the raw underlying indirection.  Only meaningful
     * for a node that hasPrimaryIndirection().
     */
    public String getUnderlying()
    {
        return fData.getIndirection();
    }
    
    /**
     * Get the underlying indirection in the context of a Lookup.
     * @param lPath The lookup path.
     */
    public String getUnderlying(Lookup lPath)
    {
        if (fData.getPrimaryIndirection())
        {
            return fData.getIndirection();
        }
        return lPath.getCurrentIndirection();
    }
    
    /**
     * Get the layer id for this node.
     * @return The layer id.
     */
    public long getLayerID()
    {
        return fData.getLayerID();
    }
    
    /**
     * Set the layer id for this node.
     * @param layerID The id to set.
     */
    public void setLayerID(long id)
    {
        fData.setLayerID(id);
    }
    
    /**
     * Handle post copy on write details.
     * @param parent
     */
    public void handlePostCopy(DirectoryNode parent)
    {
        if (parent instanceof LayeredDirectoryNode)
        {
            LayeredDirectoryNode dir = (LayeredDirectoryNode)parent;
            setLayerID(dir.getLayerID());
        }
    }

    /**
     * Copy on write logic.
     * @param lPath
     * @return The copy or null.
     */
    public AVMNode possiblyCopy(Lookup lPath)
    {
        if (!shouldBeCopied())
        {
            return null;
        }
        // Capture the repository.
        Repository repo = lPath.getRepository();
        // Otherwise we do an actual copy.
        LayeredDirectoryNode newMe = null;
        long newBranchID = lPath.getHighestBranch();
        if (!lPath.isInThisLayer())
        {
            if (hasPrimaryIndirection())
            {
                newMe = new LayeredDirectoryNode(lPath.getIndirectionPath(), 
                                                 repo);
            }
            else
            {
                newMe = new LayeredDirectoryNode((String)null, 
                                                 repo);
                newMe.setPrimaryIndirection(false);
            }
        }
        else
        {
            newMe = new LayeredDirectoryNode(this,
                                             repo);
                                             
            newMe.setLayerID(getLayerID());
        }
        newMe.setAncestor(this);
        newMe.setBranchID(newBranchID);
        return newMe;
    }

    // TODO Start around here.
    
    /**
     * Insert a child node without COW.
     * @param name The name to give the child.
     */
    public void putChild(String name, AVMNode node)
    {
        DirectoryEntry entry = new DirectoryEntry(node.getType(), node.getDataBean());
        fData.getAdded().put(name, entry);
        fData.getDeleted().remove(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.DirectoryNode#addChild(java.lang.String, org.alfresco.repo.avm.AVMNode, org.alfresco.repo.avm.Lookup)
     */
    public boolean addChild(String name, AVMNode child, Lookup lPath)
    {
        if (fData.getAdded().containsKey(name))
        {
            return false;
        }
        if (!fData.getDeleted().contains(name))
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
            catch (AlfrescoRuntimeException re)
            {
                // Do nothing.
            }
        }
        DirectoryNode toModify = (DirectoryNode)copyOnWrite(lPath);
        toModify.putChild(name, child);
        child.setParent(toModify);
        child.setRepository(lPath.getRepository());
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.DirectoryNode#directlyContains(org.alfresco.repo.avm.AVMNode)
     */
    public boolean directlyContains(AVMNode node)
    {
        DirectoryEntry entry = new DirectoryEntry(node.getType(),
                                                  node.getDataBean());
        return fData.getAdded().containsValue(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.DirectoryNode#getListing(org.alfresco.repo.avm.Lookup, int)
     */
    public Map<String, DirectoryEntry> getListing(Lookup lPath, int version)
    {
        Map<String, DirectoryEntry> baseListing = null;
        try
        {
            Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(version, getUnderlying(lPath));
            DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
            baseListing = dir.getListing(lookup, version);
        }
        catch (AlfrescoRuntimeException re)
        {
            baseListing = new HashMap<String, DirectoryEntry>();
        }
        Map<String, DirectoryEntry> listing = new TreeMap<String, DirectoryEntry>();
        for (String name : baseListing.keySet())
        {
            if (fData.getDeleted().contains(name))
            {
                continue;
            }
            listing.put(name, baseListing.get(name));
        }
        for (String name : fData.getAdded().keySet())
        {
            listing.put(name, fData.getAdded().get(name));
        }
        return listing;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.DirectoryNode#lookupChild(org.alfresco.repo.avm.Lookup, java.lang.String, int)
     */
    public AVMNode lookupChild(Lookup lPath, String name, int version)
    {
        // TODO revisit the order in this.
        if (fData.getAdded().containsKey(name))
        {
            return AVMNodeFactory.CreateFromBean(fData.getAdded().get(name).getChild());
        }
        AVMNode child = null;
        try
        {
            Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(version, getUnderlying(lPath));
            DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
            child = dir.lookupChild(lookup, name, version);
        }
        catch (AlfrescoRuntimeException re)
        {
            return null;
        }
        if (child ==null)
        {
            return null;
        }
        if (fData.getDeleted().contains(name))
        {
            return null;
        }
        return child;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.DirectoryNode#rawRemoveChild(java.lang.String)
     */
    public void rawRemoveChild(String name)
    {
        fData.getAdded().remove(name);
        fData.getDeleted().add(name);
    }
    
    /**
     * Needed for the slide operation.
     * @param name The name of the child to remove.
     */
    public void rawRemoveChildNoGhost(String name)
    {
        fData.getAdded().remove(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.DirectoryNode#removeChild(java.lang.String, org.alfresco.repo.avm.Lookup)
     */
    public boolean removeChild(String name, Lookup lPath)
    {
        if (fData.getDeleted().contains(name))
        {
            return false;
        }
        if (!fData.getAdded().containsKey(name))
        {
            try
            {
                Lookup lookup = lPath.getRepository().getSuperRepository().lookupDirectory(-1, getUnderlying(lPath));
                DirectoryNode dir = (DirectoryNode)lookup.getCurrentNode();
                if (dir.lookupChild(lookup, name, -1) == null)
                {
                    return false;
                }
            }
            catch (AlfrescoRuntimeException re)
            {
                return false;
            }
        }
        LayeredDirectoryNode toModify =
            (LayeredDirectoryNode)copyOnWrite(lPath);
        toModify.rawRemoveChild(name);
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#getType()
     */
    public int getType()
    {
        return AVMNodeType.LAYERED_DIRECTORY;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(Lookup lPath)
    {
        return "[LD:" + fData.getId() + ":" + getUnderlying(lPath) + "]";
    }
}
