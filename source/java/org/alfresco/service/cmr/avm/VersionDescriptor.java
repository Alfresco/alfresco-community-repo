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

package org.alfresco.service.cmr.avm;

import java.io.Serializable;
import java.util.Date;

import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * All the information about a particular version.
 * @author britt
 */
public class VersionDescriptor implements Serializable
{
    private static final long serialVersionUID = 9045221398461856268L;

    /**
     * The name of the store this version belongs to.
     */
    private String fAVMStoreName;
    
    /**
     * The version id.
     */
    private int fVersionID;
    
    /**
     * The creator of this version.
     */
    private String fCreator;
    
    /**
     * The date of this version's creation.
     */
    private long fCreateDate;
    
    /**
     * The short description.
     */
    private String fTag;

    /**
     * The long description.
     */
    private String fDescription;
    
    /**
     * New one up.
     * @param storeName The store name.
     * @param versionID The version id.
     * @param creator The creator.
     * @param createDate The create date.
     */
    public VersionDescriptor(String storeName,
                             int versionID,
                             String creator,
                             long createDate,
                             String tag,
                             String description)
    {
        fAVMStoreName = storeName;
        fVersionID = versionID;
        fCreator = creator;
        fCreateDate = createDate;
        fTag = tag;
        fDescription = description;
    }
    
    /**
     * Get the store name.
     * @return The store name.
     */
    public String getAVMStoreName()
    {
        return fAVMStoreName;
    }
    
    /**
     * Get the version ID
     * @return The version ID
     */
    public int getVersionID()
    {
        return fVersionID;
    }
    
    /**
     * Get the creator of this version.
     * @return The creator.
     */
    public String getCreator()
    {
        return fCreator;
    }
 
    /**
     * Get the creation date.
     * @return The creation date.
     */
    public long getCreateDate()
    {
        return fCreateDate;
    }
    
    /**
     * Get the short description.
     * @return The short description.
     */
    public String getTag()
    {
        return fTag;
    }
    
    /**
     * Get the long description.
     * @return
     */
    public String getDescription()
    {
        return fDescription;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(fAVMStoreName);
        builder.append(":");
        builder.append("" + fVersionID);
        builder.append(":");
        builder.append(fCreator);
        builder.append(":");
        builder.append(ISO8601DateFormat.format(new Date(fCreateDate)));
        builder.append(":");
        builder.append(fTag);
        builder.append("]");
        return builder.toString();
    }
}
