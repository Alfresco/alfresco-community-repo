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

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;

/**
 * Implements the WebDAV PUT method
 * 
 * @author Gavin Cornwell
 */
public class PutMethod extends WebDAVMethod
{
    // Request parameters
    private String m_strLockToken = null;
    private String m_strContentType = null;
    private boolean m_expectHeaderPresent = false;

    /**
     * Default constructor
     */
    public PutMethod()
    {
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        m_strContentType = m_request.getHeader(WebDAV.HEADER_CONTENT_TYPE);
        String strExpect = m_request.getHeader(WebDAV.HEADER_EXPECT);

        if (strExpect != null && strExpect.equals(WebDAV.HEADER_EXPECT_CONTENT))
        {
            m_expectHeaderPresent = true;
        }

        // Get the lock token, if any

        m_strLockToken = parseIfHeader();
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // Nothing to do in this method, the body contains
        // the content it will be dealt with later
    }

    /**
     * Exceute the WebDAV request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        FileFolderService fileFolderService = getFileFolderService();

        // Get the status for the request path
        FileInfo contentNodeInfo = null;
        boolean created = false;
        try
        {
            contentNodeInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), getPath(), getServletPath());
            // make sure that we are not trying to use a folder
            if (contentNodeInfo.isFolder())
            {
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        catch (FileNotFoundException e)
        {
            // the file doesn't exist - create it
            String[] paths = getDAVHelper().splitPath(getPath());
            try
            {
                FileInfo parentNodeInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), paths[0], getServletPath());
                // create file
                contentNodeInfo = fileFolderService.create(parentNodeInfo.getNodeRef(), paths[1], ContentModel.TYPE_CONTENT);
                created = true;
                
                // apply the titled aspect - title and description
                Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
                titledProps.put(ContentModel.PROP_TITLE, paths[1]);
                titledProps.put(ContentModel.PROP_DESCRIPTION, "");
                getNodeService().addAspect(contentNodeInfo.getNodeRef(), ContentModel.ASPECT_TITLED, titledProps);
            }
            catch (FileNotFoundException ee)
            {
                // bad path
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
            }
            catch (FileExistsException ee)
            {
                // bad path
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        
        // Access the content
        ContentWriter writer = fileFolderService.getWriter(contentNodeInfo.getNodeRef());
        // set content properties
        if (m_strContentType != null)
        {
            writer.setMimetype(m_strContentType);
        }
        else
        {
            String guessedMimetype = getServiceRegistry().getMimetypeService().guessMimetype(contentNodeInfo.getName());
            writer.setMimetype(guessedMimetype);
        }
        // use default encoding
        writer.setEncoding("UTF-8");

        // Get the input stream from the request data
        InputStream input = m_request.getInputStream();

        // Write the new data to the content node
        writer.putContent(input);

        // Set the response status, depending if the node existed or not
        m_response.setStatus(created ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_NO_CONTENT);
    }
}
