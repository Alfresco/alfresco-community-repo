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

/**
 * The Interface for versionable objects.
 * @author britt
 */
interface AVMNode
{
    /**
     * Set the ancestor of this node.
     * @param ancestor The ancestor to set.
     */
    public void setAncestor(AVMNode ancestor);

    /**
     * Get the ancestor of this node.
     * @return The ancestor of this node.
     */
    public AVMNode getAncestor();

    /**
     * Set the merged from node.
     * @param mergedFrom The merged from node.
     */
    public void setMergedFrom(AVMNode mergedFrom);

    /**
     * Get the node this was merged from.
     * @return The node this was merged from.
     */
    public AVMNode getMergedFrom();

    /**
     * Get the version number.
     * @return The version number.
     */
    public int getVersionID();

    /**
     * Set the version number.
     * @param version The version number to set.
     */
    public void setVersionID(int version);

    /**
     * Possibly copy ourselves.
     * @param lPath The Lookup for this node.
     * @return A copy of ourself or null if no copy was necessary.
     */
    public AVMNode copy(Lookup lPath);

    /**
     * Set the repository for a node.
     * @param repo The Repository to set.
     */
    public void setRepository(Repository repo);

    /**
     * Get the type of this node.
     */
    public int getType();


    /**
     * Get the descriptor for this node.
     * @param The Lookup.
     * @return The descriptor for this node.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath, String name);

    /**
     * Get the descriptor for this node.
     * @param The Lookup.
     * @return The descriptor for this node.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath);

    /**
     * Get a node descriptor for this node.
     * @param parentPath The parent path.
     * @param name The name looked up as.
     * @param parentIndirection The indirection of the parent.
     * @return The descriptor for this node.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection);
    
    /**
     * Get the object id.
     * @return The object id.
     */
    public long getId();
    
    /**
     * Set this node's newness.
     * @param isNew The newness.
     */
    public void setIsNew(boolean isNew);
    
    /**
     * Get the newnews.
     * @return Whether the node is new.
     */
    public boolean getIsNew();
    
    /**
     * Get a string representation for debugging.
     * @param lPath The Lookup.
     * @return A String representation.
     */
    public String toString(Lookup lPath);
    
    /**
     * Set whether this node to be a root of a Repository.
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot);

    /**
     * Get whether this node is a root of a Repository.
     * @return Whether this node is a root.
     */
    public boolean getIsRoot();
}