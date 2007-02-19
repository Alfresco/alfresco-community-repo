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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.version.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionServiceException;

/**
 * Version History implementation. 
 * 
 * @author Roy Wetherall
 */
public class VersionHistoryImpl implements VersionHistory
{
    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 3257001051558326840L;

    /*
     * Error message(s)
     */
    private static final String ERR_MSG = "The root version must be specified when creating a version history object.";    
    
    /*
     * The root version label
     */
    private String rootVersionLabel = null;
    
    /*
     * Version history tree structure map
     */
    private HashMap<String, String> versionHistory = null;
    
    /*
     * Label to version object map
     */
    private HashMap<String, Version> versions = null;
    
    private Version rootVersion;
    
    /**
     * Constructor, ensures the root version is set.
     * 
     * @param rootVersion  the root version, can not be null.
     */
    public VersionHistoryImpl(Version rootVersion)
    {
        if (rootVersion == null)
        {
            // Exception - a version history can not be created unless
            //             a root version is specified
            throw new VersionServiceException(VersionHistoryImpl.ERR_MSG);
        }
        
        this.versionHistory = new HashMap<String, String>();
        this.versions = new HashMap<String, Version>();
        
        this.rootVersion = rootVersion;
        this.rootVersionLabel = rootVersion.getVersionLabel();
        addVersion(rootVersion, null);        
    }    
    
    /**
     * Gets the root (or initial) version of the version history.
     * 
     * @return  the root version
     */
    public Version getRootVersion()
    {
        return this.rootVersion;
    }
    
    /**
     * Gets a collection containing all the versions within the
     * version history.
     * <p>
     * The order of the versions is not guarenteed.
     * 
     * @return  collection containing all the versions
     */
    public Collection<Version> getAllVersions()
    {
        return this.versions.values();
    }
    
    /**
     * Gets the predecessor of a specified version
     * 
     * @param version  the version object
     * @return         the predeceeding version, null if root version
     */
    public Version getPredecessor(Version version)
    {
        Version result = null;
        if (version != null)
        {
            result = getVersion(this.versionHistory.get(version.getVersionLabel()));
        }
        return result;
    }

    /**
     * Gets the succeeding versions of a specified version.
     * 
     * @param version  the version object
     * @return         a collection containing the succeeding version, empty is none
     */
    public Collection<Version> getSuccessors(Version version)
    {
        ArrayList<Version> result = new ArrayList<Version>();
        
        if (version != null)
        {
            String versionLabel = version.getVersionLabel();
            
            if (this.versionHistory.containsValue(versionLabel) == true)
            {
                for (String key : this.versionHistory.keySet())
                {
                    if (this.versionHistory.get(key) == versionLabel)
                    {
                        result.add(getVersion(key));
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Gets a version with a specified version label.  The version label is guarenteed 
     * unique within the version history.
     * 
     * @param versionLabel                   the version label
     * @return                               the version object
     * @throws VersionDoesNotExistException  indicates requested version does not exisit
     */
    public Version getVersion(String versionLabel)
    {
        Version result = null;
        if (versionLabel != null)
        {
            result = this.versions.get(versionLabel);
            
            if (result == null)
            {
                // Throw exception indicating that the version does not exit
                throw new VersionDoesNotExistException(versionLabel);
            }
        }
        return result;
    }
    
    /**
     * Add a version to the version history.
     * <p>
     * Used internally to build the version history tree.
     * 
     * @param version       the version object
     * @param predecessor   the preceeding version
     */
    public void addVersion(Version version, Version predecessor)
    {
        // TODO cope with exception case where duplicate version labels have been specified
        
        this.versions.put(version.getVersionLabel(), version);
        
        if (predecessor != null)
        {
            this.versionHistory.put(version.getVersionLabel(), predecessor.getVersionLabel());
        }
    }
}
