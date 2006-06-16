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
 * Represents a path component in a lookup.
 * @author britt
 */
class LookupComponent
{
    /**
     * The name of this component.
     */
    private String fName;
    
    /**
     * The node of this component.
     */
    private AVMNode fNode;
    
    /**
     * The indirection path (if any) for this node.
     */
    private String fIndirection;
    
    /**
     * The lowest layer index.
     */
    private int fLowestLayerIndex;
    
    /**
     * Whether this node is in a layer.
     */
    private boolean fLayered;
    
    /**
     * Whether this needs copying.
     */
    private boolean fNeedsCopy;
    
    /**
     * Create a new empty lookup component.
     */
    public LookupComponent()
    {
    }
    
    /**
     * Get the indirection.
     * @return the indirection
     */
    public String getIndirection()
    {
        return fIndirection;
    }

    /**
     * Set the indirection.
     * @param indirection the indirection to set
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }

    /**
     * Is this component layered. I.e. has it seen a layer yet in
     * its lookup.
     * @return Whether this component is layered.
     */
    public boolean isLayered()
    {
        return fLayered;
    }

    /**
     * Set whether this node is layered.
     * @param layered
     */
    public void setLayered(boolean layered)
    {
        fLayered = layered;
    }

    /**
     * Get the index of the lowest (in the path lookup sense) layer
     * seen at this component's point in the lookup.
     * @return the lowestLayerIndex
     */
    public int getLowestLayerIndex()
    {
        return fLowestLayerIndex;
    }

    /**
     * Set the index of the lowest (in the path lookup sense) layer
     * seen at this components's point in the lookup.
     * @param lowestLayerIndex the lowestLayerIndex to set
     */
    public void setLowestLayerIndex(int lowestLayerIndex)
    {
        fLowestLayerIndex = lowestLayerIndex;
    }

    /**
     * Get the path component name.
     * @return the name
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Set the path component name.
     * @param name the name to set
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Get the looked up node for this component.
     * @return the node
     */
    public AVMNode getNode()
    {
        return fNode;
    }

    /**
     * Set the node for this component.
     * @param node the node to set
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }

    /**
     * Set the needs copy bit.
     * @param needs Whether this component needs to be copied.
     */
    public void setNeedsCopy(boolean needs)
    {
        fNeedsCopy = needs;
    }
    
    /**
     * Does this component need a copy.
     * @return Whether it does.
     */
    public boolean getNeedsCopy()
    {
        return fNeedsCopy;
    }
}
