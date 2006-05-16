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

import java.util.Map;

import org.alfresco.repo.avm.hibernate.DirectoryEntry;

/**
 * Base class for Directories.
 * @author britt
 */
public abstract class DirectoryNode extends AVMNode
{
    /**
     * Does this directory directly contain the specified node.
     * @param node The node to check.
     * @return Whether it does.
     */
    public abstract boolean directlyContains(AVMNode node);
    
    /**
     * Put child into this directory directly.  No copy on write.
     * @param name The name to give it.
     * @param node The child.
     */
    public abstract void putChild(String name, AVMNode node);
    
    /**
     * Lookup a child node.
     * @param lPath The Lookup so far.
     * @param name The name of the child to lookup.
     * @param version The version to look under.
     */
    public abstract AVMNode lookupChild(Lookup lPath, String name, int version);
    
    /**
     * Add a child node.  Fails if child already exists.
     * Copy is possible.
     * @param name The name to give the child.
     * @param child The child to add.
     * @param The lookup path.
     */
    public abstract boolean addChild(String name, AVMNode child,
                                     Lookup lPath);
    
    /**
     * Remove a child node. Fails if child does not exist.
     * Copy is possible.
     * @param name The name of the child to remove.
     * @param lPath The lookup path.
     */
    public abstract boolean removeChild(String name, Lookup lPath);
    
    /**
     * Remove a child directly.  No copy is possible.
     * @param name The name of the child to remove.
     */
    public abstract void rawRemoveChild(String name);
    
    /**
     * Get a directory listing.
     * @param lPath The lookup context.
     * @param version The version to look under.
     * @return A Map of names to DirectoryEntries.
     */
    public abstract Map<String, DirectoryEntry> getListing(Lookup lPath, int version);
}
