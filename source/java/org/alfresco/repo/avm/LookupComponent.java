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
}
