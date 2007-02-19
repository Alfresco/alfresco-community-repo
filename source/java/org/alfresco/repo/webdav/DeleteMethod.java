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

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implements the WebDAV DELETE method
 * 
 * @author gavinc
 */
public class DeleteMethod extends WebDAVMethod
{
    /**
     * Default constructor
     */
    public DeleteMethod()
    {
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
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
        // Nothing to do in this method
    }

    /**
     * Execute the request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("WebDAV DELETE: " + getPath());
        }
        
        FileFolderService fileFolderService = getFileFolderService();

        NodeRef rootNodeRef = getRootNodeRef();

        String path = getPath();
        List<String> pathElements = getDAVHelper().splitAllPaths(path);
        FileInfo fileInfo = null;
        try
        {
            // get the node to delete
            fileInfo = fileFolderService.resolveNamePath(rootNodeRef, pathElements);
        }
        catch (FileNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Node not found: " + getPath());
            }
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }
        // delete it
        fileFolderService.delete(fileInfo.getNodeRef());
    }
}
