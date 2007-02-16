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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.descriptor;


/**
 * Provides meta-data for the Alfresco stack.
 * 
 * @author David Caruana
 */
public interface Descriptor
{
    /**
     * Gets the major version number, e.g. <u>1</u>.2.3
     * 
     * @return  major version number
     */
    public String getVersionMajor();

    /**
     * Gets the minor version number, e.g. 1.<u>2</u>.3
     * 
     * @return  minor version number
     */
    public String getVersionMinor();
    
    /**
     * Gets the version revision number, e.g. 1.2.<u>3</u>
     * 
     * @return  revision number
     */
    public String getVersionRevision();
    
    /**
     * Gets the version label
     * 
     * @return  the version label
     */
    public String getVersionLabel();
    
    /**
     * Gets the build number 
     * 
     * @return  the build number i.e. build-1
     */
    public String getVersionBuild();
    
    /**
     * Gets the full version number
     * 
     * @return  full version number as major.minor.revision (label)
     */
    public String getVersion();

    /**
     * Gets the edition
     *  
     * @return  the edition
     */
    public String getEdition();
    
    /**
     * Gets the schema number
     * 
     * @return a positive integer
     */
    public int getSchema();
    
    /**
     * Gets the list available descriptors
     *  
     * @return  descriptor keys
     */
    public String[] getDescriptorKeys();
    
    /**
     * Get descriptor value
     * 
     * @param key  the descriptor key
     * @return  descriptor value (or null, if one not provided)
     */
    public String getDescriptor(String key);
    
}
