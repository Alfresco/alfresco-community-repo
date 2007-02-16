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
package org.alfresco.service.cmr.repository;

import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;


/**
 * This service interface provides support for Mimetypes.
 * 
 * @author Derek Hulley
 *
 */
@PublicService
public interface MimetypeService
{
    /**
     * Get the extension for the specified mimetype  
     * 
     * @param mimetype a valid mimetype
     * @return Returns the default extension for the mimetype
     * @throws AlfrescoRuntimeException if the mimetype doesn't exist
     */
    @NotAuditable
    public String getExtension(String mimetype);

    /**
     * Get all human readable mimetype descriptions indexed by mimetype extension
     * 
     * @return the map of displays indexed by extension
     */
    @NotAuditable
    public Map<String, String> getDisplaysByExtension();

    /**
     * Get all human readable mimetype descriptions indexed by mimetype
     *
     * @return the map of displays indexed by mimetype
     */
    @NotAuditable
    public Map<String, String> getDisplaysByMimetype();

    /**
     * Get all mimetype extensions indexed by mimetype
     * 
     * @return the map of extension indexed by mimetype
     */
    @NotAuditable
    public Map<String, String> getExtensionsByMimetype();

    /**
     * Get all mimetypes indexed by extension
     * 
     * @return the map of mimetypes indexed by extension
     */
    @NotAuditable
    public Map<String, String> getMimetypesByExtension();

    /**
     * Get all mimetypes
     * 
     * @return all mimetypes
     */
    @NotAuditable
    public List<String> getMimetypes();

    /**
     * Provides a non-null best guess of the appropriate mimetype given a
     * filename.
     * 
     * @param filename the name of the file with an optional file extension
     * @return Returns the best guess mimetype or the mimetype for
     *      straight binary files if no extension could be found.
     */
    @NotAuditable
    public String guessMimetype(String filename);
}
