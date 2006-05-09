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

import org.alfresco.repo.avm.hibernate.AVMNodeBean;
import org.alfresco.repo.avm.hibernate.DirectoryNodeBean;

/**
 * Base class for all repository file system like objects.
 * @author britt
 */
public abstract class AVMNode
{
    /**
     * The AVMNodeBean that contains our data.
     */
    private AVMNodeBean fData;
    
    /**
     * Default constructor.
     */
    protected AVMNode()
    {
        fData = null;
    }
    
    /**
     * Set the ancestor of this node.
     * @param ancestor The ancestor to set.
     */
    public void setAncestor(AVMNode ancestor)
    {
        fData.setAncestor(ancestor.getDataBean());
    }

    /**
     * Get the ancestor of this node.
     * @return The ancestor of this node.
     */
    public AVMNode getAncestor()
    {
        return AVMNodeFactory.CreateFromBean(fData.getAncestor());
    }
    
    /**
     * Set the merged from node.
     * @param mergedFrom The merged from node.
     */
    public void setMergedFrom(AVMNode mergedFrom)
    {
        fData.setMergedFrom(mergedFrom.getDataBean());
    }
    
    /**
     * Get the node this was merged from.
     * @return The node this was merged from.
     */
    public AVMNode getMergedFrom()
    {
        return AVMNodeFactory.CreateFromBean(fData.getMergedFrom());
    }
    
    /**
     * Should this be copied on modification.
     */
    public boolean shouldBeCopied()
    {
        return !fData.getIsNew();
    }
   
    /**
     * Set to need copying or not.
     * @param copyable Whether this should be copied.
     */
    public void setShouldBeCopied(boolean copyable)
    {
        fData.setIsNew(!copyable);
    }
    
    /**
     * Get the version number.
     * @return The version number.
     */
    public long getVersion()
    {
        return fData.getVersionID();
    }
    
    /**
     * Set the version number.
     * @param version The version number to set.
     */
    public void setVersion(long version)
    {
        fData.setVersionID(version);
    }
    
    /**
     * Get the branch id of this node.
     * @return The branch id.
     */
    public long getBranchID()
    {
        return fData.getBranchID();
    }
    
    /**
     * Set the branch id on this node.
     * @param branchID The id to set.
     */
    public void setBranchID(long branchID)
    {
        fData.setBranchID(branchID);
    }
    
    /**
     * Get the (possibly null) parent.
     * @return The parent or null.
     */
    public DirectoryNode getParent()
    {
        return (DirectoryNode)AVMNodeFactory.CreateFromBean(fData.getParent());
    }

    /**
     * Set the parent of this node.
     * @param parent The DirectoryNode to set.
     */
    public void setParent(DirectoryNode parent)
    {
        fData.setParent((DirectoryNodeBean)parent.getDataBean());
    }
    
    /**
     * Perform a COW if required.
     * @param lPath The lookup path.
     * @return A 'copied' version of this node.
     */
    public AVMNode copyOnWrite(Lookup lPath)
    {
        AVMNode newMe = possiblyCopy(lPath);
        String myName = lPath.getName();
        lPath.upCurrentNode();
        if (newMe == null)
        {
            return this;
        }
        Repository repos = getRepository();
        newMe.setVersion(repos.getLatestVersion() + 1);
        DirectoryNode parent = null;
        if (getParent() != null)
        {
            parent = (DirectoryNode)lPath.getCurrentNode();
        }
        if (parent != null)  
        {
            DirectoryNode newParent =
                (DirectoryNode)parent.copyOnWrite(lPath);
            newParent.putChild(myName, newMe);
            newMe.setParent(newParent);
        }
        else // Null parent means root of repository.
        {
            newMe.setRepository(getRepository());
            repos.setNewRoot((DirectoryNode)newMe);
        }
        newMe.setShouldBeCopied(false);
        repos.setNew(newMe);
        return newMe;
    }


    /**
     * Possibly copy ourselves.
     * @param lPath The Lookup for this node.
     * @return A copy of ourself or null if no copy was necessary.
     */
    public abstract AVMNode possiblyCopy(Lookup lPath);
    
    /**
     * Handle any after recursive copy processing.
     * 
     * @param parent The DirectoryNode that is the parent of 
     * this copied node, after recursive copying.
     */
    public abstract void handlePostCopy(DirectoryNode parent);
    
    /**
     * Set the repository for a node.
     * @param repo The Repository to set.
     */
    public void setRepository(Repository repo)
    {
        fData.setRepository(repo.getDataBean());
    }

    /**
     * Get the Repository we 'belong' to.
     * @return The Repository.
     */
    public Repository getRepository()
    {
        return RepositoryFactory.GetInstance().createFromBean(fData.getRepository());
    }
    
    /**
     * Get the data bean in this node.
     * @return The data bean.
     */
    public AVMNodeBean getDataBean()
    {
        return fData;
    }
    
    /**
     * Set the data bean in this node.
     * @param bean The data bean to set.
     */
    public void setDataBean(AVMNodeBean bean)
    {
        fData = bean;
    }
    
    /**
     * Get the type of this node.
     */
    public abstract AVMNodeType getType();
}
