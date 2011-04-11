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
package org.alfresco.service.descriptor;

import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.util.VersionNumber;


/**
 * Provides meta-data for the Alfresco stack.
 * 
 * @author David Caruana
 */
public interface Descriptor
{
    /**
     * Gets the id of the item being described
     * 
     * @return  identifier
     */
    public String getId();
    
    /**
     * Gets the name of the item being described
     * 
     * @return  name
     */
    public String getName();
    
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
     * @return Returns the object representing the major-minor-revision numbers
     */
    public VersionNumber getVersionNumber();
    
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
     * Gets LicenseMode
     *  
     * @return the licenseMode
     */
    public LicenseMode getLicenseMode();
    
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
