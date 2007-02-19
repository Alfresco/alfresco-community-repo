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
package org.alfresco.repo.webdav;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;

/**
 * Implements the WebDAV OPTIONS method
 * 
 * @author Gavin Cornwell
 */
public class OptionsMethod extends WebDAVMethod
{
    private static final String DAV_HEADER = "DAV";
    private static final String DAV_HEADER_CONTENT = "1,2";
    private static final String ALLOW_HEADER = "Allow";
    private static final String MS_HEADER = "MS-Author-Via";

    private static final String FILE_METHODS = "OPTIONS, GET, HEAD, POST, DELETE, PROPFIND, COPY, MOVE, LOCK, UNLOCK";
    private static final String COLLECTION_METHODS = FILE_METHODS + ", PUT";

    /**
     * Default constructor
     */
    public OptionsMethod()
    {
    }

    /**
     * Parse the request header fields
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Nothing to do in this method
    }

    /**
     * Parse the request main body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // Nothing to do in this method
    }

    /**
     * Perform the main request processing
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException
    {
        boolean isFolder;
        try
        {
            FileInfo fileInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), getPath(), getServletPath());
            isFolder = fileInfo.isFolder();
        }
        catch (FileNotFoundException e)
        {
            // Do nothing; just default to a folder
            isFolder = true;
        }
        // Add the header to advertise the level of support the server has
        m_response.addHeader(DAV_HEADER, DAV_HEADER_CONTENT);

        // Add the proprietary Microsoft header to make Microsoft clients behave
        m_response.addHeader(MS_HEADER, DAV_HEADER);

        // Add the header to show what methods are allowed
        m_response.addHeader(ALLOW_HEADER, isFolder ? COLLECTION_METHODS : FILE_METHODS);
    }
}
