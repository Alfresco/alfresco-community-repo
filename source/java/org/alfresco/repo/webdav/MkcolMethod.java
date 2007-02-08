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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.webdav;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.w3c.dom.Document;

/**
 * Implements the WebDAV MKCOL method
 * 
 * @author gavinc
 */
public class MkcolMethod extends WebDAVMethod
{
    /**
     * Default constructor
     */
    public MkcolMethod()
    {
    }

    /**
     * Parse the request headers
     * 
     * @Exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Nothing to do in this method
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // There should not be a body with the MKCOL request

        Document body = getRequestBodyAsDocument();

        if (body != null)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /**
     * Exceute the request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        FileFolderService fileFolderService = getFileFolderService();

        // see if it exists
        try
        {
            getDAVHelper().getNodeForPath(getRootNodeRef(), getPath(), getServletPath());
            // already exists
            throw new WebDAVServerException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        catch (FileNotFoundException e)
        {
            // it doesn't exist
        }
        
        // Trim the last path component and check if the parent path exists
        String parentPath = getPath();
        int lastPos = parentPath.lastIndexOf(WebDAVHelper.PathSeperator);
        
        NodeRef parentNodeRef = null;

        if ( lastPos == 0)
        {
            // Create new folder at root

            parentPath = WebDAVHelper.PathSeperator;
            parentNodeRef = getRootNodeRef();
        }
        else if (lastPos != -1)
        {
            // Trim the last path component
            parentPath = parentPath.substring(0, lastPos + 1);
            try
            {
                FileInfo parentFileInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), parentPath, m_request.getServletPath());
                parentNodeRef = parentFileInfo.getNodeRef();
            }
            catch (FileNotFoundException e)
            {
                // parent path is missing
                throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        else
        {
            // Looks like a bad path
            throw new WebDAVServerException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        // Get the new folder name
        String folderName = getPath().substring(lastPos + 1);

        // Create the new folder node
        fileFolderService.create(parentNodeRef, folderName, ContentModel.TYPE_FOLDER);

        // Return a success status
        m_response.setStatus(HttpServletResponse.SC_CREATED);
    }
}
