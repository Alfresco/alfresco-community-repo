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

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * Simple bean that implements AVMAspectName.
 * @author britt
 */
class AVMAspectNameImpl implements AVMAspectName, Serializable
{
    private static final long serialVersionUID = -6282415309583571934L;

    /**
     * The Primary Key.
     */
    private Long fID;
    
    /**
     * The Node that has the named aspect.
     */
    private AVMNode fNode;
    
    /**
     * The name of the Aspect.
     */
    private QName fName;

    /**
     * Default constructor.
     */
    public AVMAspectNameImpl()
    {
    }
    
    /**
     * Set the node that has the Aspect. 
     * @param node The node.
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }
    
    /**
     * Get the node that has this Aspect name.
     * @return The AVM Node.
     */
    public AVMNode getNode()
    {
        return fNode;
    }
    
    /**
     * Set the name of the Aspect.
     * @param name The QName of the Aspect.
     */
    public void setName(QName name)
    {
        fName = name;
    }
    
    /**
     * Get the name of this Aspect.
     * @return The QName of this aspect.
     */
    public QName getName()
    {
        return fName;
    }

    /**
     * Set the primary key (For Hibernate)
     * @param id The primary key.
     */
    protected void setId(Long id)
    {
        fID = id;
    }
    
    /**
     * Get the primary key (For Hibernate)
     * @return The primary key.
     */
    protected Long getId()
    {
        return fID;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMAspectName))
        {
            return false;
        }
        AVMAspectName o = (AVMAspectName)obj;
        return fNode.equals(o.getNode()) && fName.equals(o.getName());
    }

    @Override
    public int hashCode()
    {
        return fNode.hashCode() + fName.hashCode();
    }
}
