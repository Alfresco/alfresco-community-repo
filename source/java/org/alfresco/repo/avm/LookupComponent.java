/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
