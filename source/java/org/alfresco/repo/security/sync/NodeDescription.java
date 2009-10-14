/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.sync;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.util.PropertyMap;

/**
 * An 'off-line' description of an Alfresco node.
 * 
 * @author dward
 */
public class NodeDescription
{
    /**
     * An identifier for the node for monitoring purposes. Should help trace where the node originated from.
     */
    private String sourceId;

    /** The properties. */
    private final PropertyMap properties = new PropertyMap(19);

    /** The child associations. */
    private final Set<String> childAssociations = new TreeSet<String>();

    /** The last modification date. */
    private Date lastModified;

    /**
     * Instantiates a new node description.
     * 
     * @param sourceId
     *            An identifier for the node for monitoring purposes. Should help trace where the node originated from.
     */
    public NodeDescription(String sourceId)
    {
        this.sourceId = sourceId;
    }        

    /**
     * Gets an identifier for the node for monitoring purposes. Should help trace where the node originated from.
     * 
     * @return an identifier for the node for monitoring purposes
     */
    public String getSourceId()
    {
        return sourceId;
    }

    /**
     * Gets the last modification date.
     * 
     * @return the last modification date
     */
    public Date getLastModified()
    {
        return lastModified;
    }

    /**
     * Sets the last modification date.
     * 
     * @param lastModified
     *            the last modification date
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * Gets the properties.
     * 
     * @return the properties
     */
    public PropertyMap getProperties()
    {
        return properties;
    }

    /**
     * Gets the child associations.
     * 
     * @return the child associations
     */
    public Set<String> getChildAssociations()
    {
        return childAssociations;
    }
}
