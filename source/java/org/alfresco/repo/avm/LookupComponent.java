/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

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
     * The indirection version for this node (if any).
     */
    private int fIndirectionVersion;
    
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
     * Get the indirection version for this component.
     * @return The indirection version.
     */
    public int getIndirectionVersion()
    {
        return fIndirectionVersion;
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
     * Set the indirection version for this component.
     * @param version The version to set.
     */
    public void setIndirectionVersion(int version)
    {
        fIndirectionVersion = version;
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
    
    // for debug
    public String toString()
    {
        return (fNode != null ? fNode.toString(null) : "");
    }
}
