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

    private static final String FILE_METHODS = "OPTIONS, GET, HEAD, POST, DELETE, PROPFIND, PROPPATCH, COPY, MOVE, LOCK, UNLOCK";
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
     * @return          Returns <tt>true</tt> always
     */
    @Override
    protected boolean isReadOnly()
    {
        return true;
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
