/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.webdav;

import javax.servlet.http.HttpServletResponse;

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
    private static final String CONTENT_LENGTH = "Content-Length";

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
        
        // Indicate no content
        
        m_response.addHeader(CONTENT_LENGTH, "0");
    }
}
