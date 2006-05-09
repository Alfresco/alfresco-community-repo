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
public class LookupComponent
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
     * The highest branch seen by this component.
     */
    private long fHighestBranch;
    
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
     * @return the highestBranch
     */
    public long getHighestBranch()
    {
        return fHighestBranch;
    }
    
    /**
     * Create a new empty lookup component.
     */
    public LookupComponent()
    {
    }
    
    /**
     * @param highestBranch the highestBranch to set
     */
    public void setHighestBranch(long highestBranch)
    {
        fHighestBranch = highestBranch;
    }

    /**
     * @return the indirection
     */
    public String getIndirection()
    {
        return fIndirection;
    }

    /**
     * @param indirection the indirection to set
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }

    /**
     * @return the layered
     */
    public boolean isLayered()
    {
        return fLayered;
    }

    /**
     * @param layered the layered to set
     */
    public void setLayered(boolean layered)
    {
        fLayered = layered;
    }

    /**
     * @return the lowestLayerIndex
     */
    public int getLowestLayerIndex()
    {
        return fLowestLayerIndex;
    }

    /**
     * @param lowestLayerIndex the lowestLayerIndex to set
     */
    public void setLowestLayerIndex(int lowestLayerIndex)
    {
        fLowestLayerIndex = lowestLayerIndex;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return fName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * @return the node
     */
    public AVMNode getNode()
    {
        return fNode;
    }

    /**
     * @param node the node to set
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }
}
