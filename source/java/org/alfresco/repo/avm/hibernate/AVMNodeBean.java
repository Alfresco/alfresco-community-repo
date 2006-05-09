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
package org.alfresco.repo.avm.hibernate;


/**
 * The base Node class for the new versioning model, at least for now.
 * @author britt
 */
public interface AVMNodeBean
{
    /**
     * Set the object id of this node.
     * @param id The Object ID.
     */
    public void setId(long id);

    /**
     * Get the object id of this node.
     * @return The Object ID of this node.
     */
    public long getId();

    /**
     * Set the version id.
     * @param id The version id of the node.
     */
    public void setVersionID(long id);

    /**
     * Get the version id of this node.
     * @return The version id.
     */
    public long getVersionID();

    /**
     * Set the parent of this node.  This is only a canonical parent,
     * the one that this node had at the time of its creation.
     * @param parent The id of the parent node.
     */
    public void setParent(DirectoryNodeBean parent);

    /**
     * Get the parent of this node.
     * @return The parent of this node.
     */
    public DirectoryNodeBean getParent();

    /**
     * Set the node that is this node's direct ancestor.
     * @param ancestor The id of the ancestor node.
     */
    public void setAncestor(AVMNodeBean ancestor);

    /**
     * Get the direct ancestor of this node.
     * @return The id of the direct ancestor of this node.
     */
    public AVMNodeBean getAncestor();

    /**
     * Set the node that this node was merged from.
     * @param mergedFrom The id of the node from which this was merged.
     */
    public void setMergedFrom(AVMNodeBean mergedFrom);

    /**
     * Get the node that this was merged from.
     * @return The id of the node this was merged from.
     */
    public AVMNodeBean getMergedFrom();

    /**
     * Set the branch id.
     * @param branchID The branch id to set.
     */
    public void setBranchID(long branchID);

    /**
     * Get the branch id of this node.
     * @return The branch id of this node.
     */
    public long getBranchID();
    
    /**
     * Set the Repository that owns this node.
     * @param repository The repository that owns this node.
     */
    public void setRepository(RepositoryBean repository);
    
    /**
     * Get the Repository that owns this node.
     * @return The Repository.
     */
    public RepositoryBean getRepository();
    
    /**
     * Set is new.
     * @param isNew
     */
    public void setIsNew(boolean isNew);
    
    /**
     * Get is new.
     * @return Whether this node is new.
     */
    public boolean getIsNew();
    
    /**
     * Set the version (for concurrency management.)
     * @param vers The version.
     */
    public void setVers(long vers);
    
    /**
     * Get the version (for concurrency management.)
     * @return The version.
     */
    public long getVers();
}