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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * The interface for Directory Nodes.
 * @author britt
 */
public interface DirectoryNode extends AVMNode
{
    /**
     * Does this directory directly contain the specified node.
     * @param node The node to check.
     * @return Whether it does.
     */
    public boolean directlyContains(AVMNode node);

    /**
     * Put child into this directory directly.  No copy on write.
     * @param name The name to give it.
     * @param node The child.
     */
    public void putChild(String name, AVMNode node);

    /**
     * Lookup a child node.
     * @param lPath The Lookup so far.
     * @param name The name of the child to lookup.
     * @param includeDeleted Include deleted nodes or not.
     */
    public AVMNode lookupChild(Lookup lPath, String name, boolean includeDeleted);
    
    /**
     * Lookup a child node using an AVMNodeDescriptor as context.
     * @param mine The node descriptor for this.
     * @param name The name of the child to lookup.
     * @return The descriptor for the looked up child.
     */
    public AVMNodeDescriptor lookupChild(AVMNodeDescriptor mine, String name, boolean includeDeleted);

    /**
     * Remove a child directly.  No copy is possible.
     * @param lPath The lookup through which this node was reached.
     * @param name The name of the child to remove.
     */
    public void removeChild(Lookup lPath, String name);

    /**
     * Get a directory listing.
     * @param lPath The lookup context.
     * @return A SortedMap of names to DirectoryEntries.
     */
    public Map<String, AVMNode> getListing(Lookup lPath, boolean includeDeleted);
    
    /**
     * Get a listing of the nodes directly contained by a directory.
     * @param lPath The Lookup to this directory.
     * @return A Map of names to nodes.
     */
    public Map<String, AVMNode> getListingDirect(Lookup lPath, boolean includeDeleted);

    /**
     * Get a listing of nodes directly contained by a directory.
     * @param dir The descriptor for the directory.
     * @param includeDeleted Whether to include deleted nodes.
     * @return A Map of Strings to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListingDirect(AVMNodeDescriptor dir,
                                                                 boolean includeDeleted);    

    /**
     * Get a listing from a directory specified by an AVMNodeDescriptor.
     * @param dir The directory to list.
     * @return A Map of names to node descriptors
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(AVMNodeDescriptor dir, 
                                                           boolean includeDeleted);

    /**
     * Get the names of nodes deleted in this directory.
     * @return A List of names.
     */
    public List<String> getDeletedNames();
    
    /**
     * Set the directory, which must be in a layer, into a primary
     * indirection taking its indirection from the Lookup.
     * @param lPath The Lookup.
     */
    public void turnPrimary(Lookup lPath);

    /**
     * Retarget a layered directory.
     * @param lPath The Lookup.
     * @param target The target path.
     */
    public void retarget(Lookup lPath, String target);
    
    /**
     * Set whether this node is a root node.
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot);
    
    /**
     * Link a node with the given id into this directory.
     * @param lPath The Lookup for this node.
     * @param name The name to give the node.
     * @param toLink The node to link in.
     */
    public void link(Lookup lPath, String name, AVMNodeDescriptor toLink);
    
    /**
     * Dangerous version of link that assumes that a child node of
     * the given name does not already exist.
     * @param name The name to give the child.
     * @param toLink The child to link in.
     */
    public void link(String name, AVMNodeDescriptor toLink);
}