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

import java.io.Serializable;

/**
 * The key to a ChildEntry, a Parent and a name.
 * @author britt
 */
public class ChildKey implements Serializable
{
    private static final long serialVersionUID = 2033634095972856432L;

    /**
     * The Parent.
     */
    private DirectoryNode fParent;
    
    /**
     * The child's name.
     */
    private String fName;
    
    /**
     * Construct one with parameters.
     * @param parent The parent directory.
     * @param name The name of the child.
     */
    public ChildKey(DirectoryNode parent, String name)
    {
        fParent = parent;
        fName = name;
    }

    /**
     * A Default Constructor.
     */
    public ChildKey()
    {
    }
    
    /**
     * Set the parent.
     */
    public void setParent(DirectoryNode parent)
    {
        fParent = parent;
    }
    
    /**
     * Get the parent.
     * @return A DirectoryNode.
     */
    public DirectoryNode getParent()
    {
        return fParent;
    }
    
    /**
     * Set the name.
     */
    public void setName(String name)
    {
        fName = name;
    }
    
    /**
     * Get the name.
     */
    public String getName()
    {
        return fName;
    }
    
    /**
     * Override of equals.
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ChildKey))
        {
            return false;
        }
        ChildKey o = (ChildKey)other;
        return fParent.equals(o.getParent()) &&
               fName.equalsIgnoreCase(o.getName());
    }
    
    /**
     * Override of hashCode.
     */
    public int hashCode()
    {
        return fParent.hashCode() + fName.toLowerCase().hashCode();
    }
}
