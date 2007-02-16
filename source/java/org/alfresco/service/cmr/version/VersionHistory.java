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
package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Collection;



/**
 * Version history interface.
 * 
 * Collects the versions that make-up a version history.
 * 
 * @author Roy Wetherall
 */
public interface VersionHistory extends Serializable
{
    /**
     * Gets the root (or initial) version of the version history.
     * 
     * @return  the root version
     */
    public Version getRootVersion();
    
    /**
     * Gets a collection containing all the versions within the
     * version history.
     * <p>
     * The order of the versions is not guarenteed.
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
