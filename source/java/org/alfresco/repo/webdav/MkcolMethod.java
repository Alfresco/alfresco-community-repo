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
