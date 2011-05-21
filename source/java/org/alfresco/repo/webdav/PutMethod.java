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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.springframework.dao.ConcurrencyFailureException;

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

        // Parse Lock tokens and ETags, if any

        parseIfHeader();
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

            checkNode(contentNodeInfo);

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
                // ALF-7079 fix, retry: it looks like concurrent access (file not found but file exists) 
                throw new ConcurrencyFailureException("Concurrent access was detected.",  ee);
            }
        }
        
        LockStatus lockSts = getLockService().getLockStatus(contentNodeInfo.getNodeRef());
        String userName = getDAVHelper().getAuthenticationService().getCurrentUserName();
        String owner = (String) getNodeService().getProperty(contentNodeInfo.getNodeRef(), ContentModel.PROP_LOCK_OWNER);

        if (lockSts == LockStatus.LOCKED || (lockSts == LockStatus.LOCK_OWNER && !userName.equals(owner)))
        {
            // Indicate that the resource is locked
            throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
        }

        // Access the content
        ContentWriter writer = fileFolderService.getWriter(contentNodeInfo.getNodeRef());
        // set content properties
        String mimetype = getMimetypeService().guessMimetype(contentNodeInfo.getName());

        writer.setMimetype(mimetype);

        // Get the input stream from the request data
        InputStream is = m_request.getInputStream();
        is = is.markSupported() ? is : new BufferedInputStream(is);
        
        ContentCharsetFinder charsetFinder = getMimetypeService().getContentCharsetFinder();
        Charset encoding = charsetFinder.getCharset(is, mimetype);
        writer.setEncoding(encoding.name());

        // Write the new data to the content node
        writer.putContent(is);

        // Set the response status, depending if the node existed or not
        m_response.setStatus(created ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_NO_CONTENT);
    }
}
