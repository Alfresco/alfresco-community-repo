/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
