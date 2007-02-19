/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
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
               fName.equals(o.getName());
    }
    
    /**
     * Override of hashCode.
     */
    public int hashCode()
    {
        return fParent.hashCode() + fName.hashCode();
    }
}
