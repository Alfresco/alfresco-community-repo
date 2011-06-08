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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
public interface FileInfo extends Serializable
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
    
    /**
     * @return Returns (sub-)type of folder or file
     */
    public QName getType();
}
