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
package org.alfresco.util;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Immutable class to encapsulate a version number string.
 * 
 * A valid version number string can be made up of any number of numberical parts 
 * all delimited by '.'.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public final class VersionNumber implements Comparable<VersionNumber>, Serializable
{
    private static final long serialVersionUID = -1570247769786810251L;

    /** A convenient '0' version */
    public static final VersionNumber VERSION_ZERO = new VersionNumber("0");
    /** A convenient '999' version */
    public static final VersionNumber VERSION_BIG = new VersionNumber("999");
    
    /** Version delimeter */
    private static final String DELIMITER = "\\.";
    
    /** Version parts */
    private final int[] parts;

    /**
     * Constructror, expects a valid version string.
     * 
     * A AlfrescoRuntimeException will be throw if an invalid version is encountered.
     * 
     * @param version   the version string
     */
    public VersionNumber(String version)
    {
        // Split the version into its component parts
        String[] versions = version.split(DELIMITER);
        if (versions.length < 1)
        {
            throw new AlfrescoRuntimeException("The version string '" + version + "' is invalid.");
        }
        
        try
        {
            // Set the parts of the version
            int index = 0;
            this.parts = new int[versions.length];
            for (String versionPart : versions)
            {
                int part = Integer.parseInt(versionPart);
                this.parts[index] = part;
                index++;
            }
        }
        catch (NumberFormatException e)
        {
            throw new AlfrescoRuntimeException("The version string '" + version + "' is invalid.");   
        }
    }
    
    /**
     * Get the various parts of the version
     * 
     * @return  array containing the parts of the version
     */
    public int[] getParts()
    {
        return this.parts.clone();
    }
    
    /**
     * Compares the passed version to this.  Determines whether they are equal, greater or less than this version.
     * 
     * @param obj  the other version number
     * @return  -1 if the passed version is less that this, 0 if they are equal, 1 if the passed version is greater
     */
    public int compareTo(VersionNumber obj)
    {
        int result = 0;

        VersionNumber that = (VersionNumber)obj;
        int length = 0;
        if (this.parts.length > that.parts.length)
        {
            length = this.parts.length;
        }
        else
        {
            length = that.parts.length;
        }
        
        for (int index = 0; index < length; index++)
        {
            int thisPart = this.getPart(index);
            int thatPart = that.getPart(index);
            
            if (thisPart > thatPart)
            {
                result = 1;
                break;
            }
            else if (thisPart < thatPart)
            {
                result = -1;
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Helper method to the the part based on the index, if an invalid index is supplied 0 is returned.
     * 
     * @param index     the index
     * @return          the part value, 0 if the index is invalid
     */
    public int getPart(int index)
    {
        int result = 0;
        if (index < this.parts.length)
        {
            result = this.parts[index];
        }
        return result;
    }
    
    /**
     * Hash code implementation
     */
    @Override
    public int hashCode()
    {
        if (parts == null || parts.length == 0)
        {
            return 0;
        }
        else if (parts.length >= 2)
        {
            return parts[0] * 17 + parts[1];
        }
        else
        {
            return parts[0];
        }
    }
    
    /**
     * Equals implementation
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (false == obj instanceof VersionNumber)
        {
            return false;
        }
        VersionNumber that = (VersionNumber) obj;
        return this.compareTo(that) == 0;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int part : parts)
        {
            if (!first)
            {
                sb.append(".");
            }
            first = false;
            sb.append(part);
        }
        return sb.toString();
    }
}
