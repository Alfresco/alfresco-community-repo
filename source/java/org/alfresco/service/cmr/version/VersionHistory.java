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
package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Version history interface.
 * 
 * Collects the versions that make-up a version history.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface VersionHistory extends Serializable
{
    /**
     * Gets the root (initial / least recent) version of the version history.
     * 
     * @return  the root version
     */
    public Version getRootVersion();
    
    /**
     * Gets the head (current / most recent) version of the version history.
     * 
     * @return  the root version
     */
    public Version getHeadVersion();
    
    /**
     * Gets a collection containing all the versions within the
     * version history.
     * <p>
     * Versions are returned in descending create date order (most recent first).
     * 
     * @return  collection containing all the versions
     */
    public Collection<Version> getAllVersions();

    /**
     * Gets the predecessor of a specified version
     * 
     * @param version  the version object
     * @return         the predeceeding version, null if root version
     */
    public Version getPredecessor(Version version);

    /**
     * Gets the succeeding versions of a specified version.
     * 
     * @param version  the version object
     * @return         a collection containing the succeeding version, empty is none
     */
    public Collection<Version> getSuccessors(Version version);
    
    /**
     * Gets a version with a specified version label.  The version label is guarenteed 
     * unique within the version history.
     * 
     * @param versionLabel                   the version label
     * @return                               the version object
     * @throws VersionDoesNotExistException  indicates requested version does not exisit
     */
    public Version getVersion(String versionLabel);

}
