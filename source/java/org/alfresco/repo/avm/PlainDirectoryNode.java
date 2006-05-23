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
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.avm.hibernate.BasicAttributesBean;
import org.alfresco.repo.avm.hibernate.BasicAttributesBeanImpl;
import org.alfresco.repo.avm.hibernate.DirectoryEntry;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBeanImpl;

/**
 * A plain directory.  No monkey tricks except for possiblyCopy.
 * @author britt
 */
public class PlainDirectoryNode extends DirectoryNode
{
    /**
     * The Bean data.
     */
    private PlainDirectoryNodeBean fData;
    
    /**
     * Make up a new directory with nothing in it.
     * @param repo
     */
    public PlainDirectoryNode(Repository repo)
    {
        // Make up initial BasicAttributes.
        long time = System.currentTimeMillis();
        // TODO figure out how to get user information from context.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl("britt",
                                                                "britt",
                                                                "britt",
                                                                time,
                                                                time,
                                                                time);
        fData = new PlainDirectoryNodeBeanImpl(repo.getSuperRepository().issueID(),
                                               repo.getLatestVersion(),
                                               0L,
                                               null,
                                               null,
                                               null,
                                               repo.getDataBean(),
                                               attrs,
                                               false);
        repo.getSuperRepository().getSession().save(fData);
        setDataBean(fData);
    }
    
    /**
     * Make one up from its bean data.  Used when a PlainDirectory is
     * restored from the database.
     * @param data The bean data.
     */
    public PlainDirectoryNode(PlainDirectoryNodeBean data)
    {
        fData = data;
        setDataBean(data);
    }
    
    /**
     * Copy like constructor.
     * @param other The other directory.
     * @param repos The Repository Object that will own us.
     */
    public PlainDirectoryNode(PlainDirectoryNode other,
                              Repository repos)
    {
        // Make up appropriate BasicAttributes.
        long time = System.currentTimeMillis();
        // TODO Need to figure out how to get user information from context.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl(other.getDataBean().getBasicAttributes());
        attrs.setModDate(time);
        attrs.setCreateDate(time);
        attrs.setAccessDate(time);
        attrs.setCreator("britt");
        attrs.setLastModifier("britt");
        fData = new PlainDirectoryNodeBeanImpl(repos.getSuperRepository().issueID(),
                                               -1,
                                               0,
                                               null,
                                               null,
                                               null,
                                               repos.getDataBean(),
                                               attrs,
                                               false);
        setDataBean(fData);
        fData.setChildren(new HashMap<String, DirectoryEntry>(((PlainDirectoryNodeBean)other.getDataBean()).getChildren()));
    }

    /**
     * Add a child to this directory, possibly doing a copy. 
     * @param name The name of the child.
     * @param child The child node.
     * @param lPath The lookup path to this directory.
     * @return Success or failure.
     */   
    public boolean addChild(String name, AVMNode child, Lookup lPath)
    {
        // No, if a child with the given name exists. Note that uniqueness
        // of names is built into the AVM, as opposed to being configurable.
        if (fData.getChildren().containsKey(name))
        {
            return false;
        }
        DirectoryNode toModify = (DirectoryNode)copyOnWrite(lPath);
        toModify.putChild(name, child);
        child.setParent(toModify);
        child.setRepository(lPath.getRepository());
        return true;
    }

    /**
     * Does this directory directly contain the given node. 
     * @param node The node to check.
     * @return Whether it was found.
     */
    public boolean directlyContains(AVMNode node)
    {
        // TODO This is inefficient; maybe use a two way map.
        DirectoryEntry entry = new DirectoryEntry(node.getType(), node.getDataBean()); 
        return fData.getChildren().containsValue(entry);
    }

    /**
     * Get a directory listing.
     * @param lPath The lookup path.
     * @param version Which version.
     * @return The listing.
     */
    public Map<String, DirectoryEntry> getListing(Lookup lPath, int version)
    {
        // Maybe this is pointless, but it's nice to be able to iterate
        // over entries in a defined order.
        return new TreeMap<String, DirectoryEntry>(fData.getChildren());
    }

    /**
     * Lookup a child by name.
     * @param lPath The lookup path so far.
     * @param name The name to lookup.
     * @param version The version to look under.
     * @return The child or null.
     */
    public AVMNode lookupChild(Lookup lPath, String name, int version)
    {
        DirectoryEntry child = fData.getChildren().get(name);
        if (child == null)
        {
            return null;
        }
        return AVMNodeFactory.CreateFromBean(child.getChild());
    }

    /**
     * Remove a child, no copying.
     * @param name The name of the child to remove.
     */
    public void rawRemoveChild(String name)
    {
        fData.getChildren().remove(name);
    }

    /**
     * Remove a child. Possibly copy.
     * @param name The name of the child to remove.
     * @param lPath The lookup path.
     * @return Success or failure.
     */
    public boolean removeChild(String name, Lookup lPath)
    {
        // Can't remove it if it's not there.
        if (!fData.getChildren().containsKey(name))
        {
            return false;
        }
        DirectoryNode toModify = (DirectoryNode)copyOnWrite(lPath);
        toModify.rawRemoveChild(name);
        return true;
    }

    /**
     * Put a new child node into this directory.  No copy.
     * @param name The name of the child.
     * @param node The node to add.
     */
    public void putChild(String name, AVMNode node)
    {
        fData.getChildren().put(name, new DirectoryEntry(node.getType(), node.getDataBean()));
    }

    // TODO I don't think this is at all necessary in the world without
    // mounted VirtualRepositories.
    /**
     * Set repository after copy on write. 
     * @param parent The parent after copy on write.
     */
    public void handlePostCopy(DirectoryNode parent)
    {
    }

    /**
     * Copy on write logic.
     * @param lPath The lookup path.
     * @return
     */
    public AVMNode possiblyCopy(Lookup lPath)
    {
        if (!shouldBeCopied())
        {
            return null;
        }
        // Otherwise do an actual copy.
        DirectoryNode newMe = null;
        long newBranchID = lPath.getHighestBranch();
        // In a layered context a copy on write creates a new 
        // layered directory.
        if (lPath.isLayered())
        {
            newMe = new LayeredDirectoryNode(this, lPath.getRepository(), lPath);
        }
        else
        {
            newMe = new PlainDirectoryNode(this, lPath.getRepository());
        }
        newMe.setAncestor(this);
        newMe.setBranchID(newBranchID);
        return newMe;
    }

    /**
     * Get the type of this node. 
     * @return The type of this node.
     */
    public int getType()
    {
        return AVMNodeType.PLAIN_DIRECTORY;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#toString(org.alfresco.repo.avm.Lookup)
     */
    @Override
    public String toString(Lookup lPath)
    {
        return "[PD:" + fData.getId() + "]";
    }    
}
