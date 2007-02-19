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

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * A Property attached to an AVMNode.
 * @author britt
 */
class AVMNodePropertyImpl implements AVMNodeProperty, Serializable
{
    private static final long serialVersionUID = -7194228119659288619L;

    /**
     * The primary key.
     */
    private Long fID;
    
    /**
     * The node that owns this.
     */
    private AVMNode fNode;
    
    /**
     * The QName of this property.
     */
    private QName fName;
    
    /**
     * The PropertyValue.
     */
    private PropertyValue fValue;

    /**
     * Default constructor.
     */
    public AVMNodePropertyImpl()
    {
    }
    
    /**
     * Get the owning node.
     * @return The AVMNode.
     */
    public AVMNode getNode()
    {
        return fNode;
    }

    /**
     * Set the owning node.
     * @param node The AVMNode to set.
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }

    /**
     * Get the name, a QName
     * @return A QName.
     */
    public QName getName()
    {
        return fName;
    }

    /**
     * Set the name, a QName.
     * @param name The QName.
     */
    public void setName(QName name)
    {
        fName = name;
    }

    /**
     * Get the value.
     * @return A PropertyValue
     */
    public PropertyValue getValue()
    {
        return fValue;
    }

    /**
     * Set the value.
     * @param value A PropertyValue.
     */
    public void setValue(PropertyValue value)
    {
        fValue = value;
    }

    /**
     * Set the primary key. (For Hibernate)
     * @param id The primary key.
     */
    protected void setId(Long id)
    {
        fID = id;
    }
    
    /**
     * Get the primary key. (For Hibernate)
     * @return The primary key.
     */
    protected Long getId()
    {
        return fID;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof AVMNodeProperty))
        {
            return false;
        }
        AVMNodeProperty o = (AVMNodeProperty)other;
        return fNode.equals(o.getNode()) && fName.equals(o.getName());
    }
    
    @Override
    public int hashCode()
    {
        return fNode.hashCode() + fName.hashCode();
    }
}
