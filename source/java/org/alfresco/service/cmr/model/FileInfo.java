/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Common file information.  The implementations may store the properties for the lifetime
 * of this instance; i.e. the values are transient and can be used as read-only values for
 * a short time only.
 * 
 * @author Derek Hulley
 */
public interface FileInfo
{
    /**
     * @return Returns a reference to the low-level node representing this file
     */
    public NodeRef getNodeRef();
    
    /**
     * @return Return true if this instance represents a folder, false if this represents a file
     */
    public boolean isFolder();
    
    /**
     * @return true if this instance represents a link to a node
     */
    public boolean isLink();
    
    /**
     * @return Return the reference to the node that this node is linked to
     */
    public NodeRef getLinkNodeRef();
    
    /**
     * @return Returns the name of the file or folder within the parent folder
     */
    public String getName();
    
    /**
     * @return Returns the date the node was created
     */
    public Date getCreatedDate();
    
    /**
     * @return Returns the modified date
     */
    public Date getModifiedDate();
    
    /**
     * Get the content data.  This is only valid for {@link #isFolder() files}.
     * 
     * @return Returns the content data
     */
    public ContentData getContentData();
    
    /**
     * @return Returns all the node properties
     */
    public Map<QName, Serializable> getProperties();
}
