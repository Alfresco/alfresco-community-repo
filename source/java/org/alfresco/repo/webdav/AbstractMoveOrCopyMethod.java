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

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implements the WebDAV COPY and MOVE methods
 * 
 * @author Derek Hulley
 */
public abstract class AbstractMoveOrCopyMethod extends HierarchicalMethod
{
    /**
     * Default constructor
     */
    public AbstractMoveOrCopyMethod()
    {
    }
    
    /**
     * Implement the move or copy, depending on the implementation
     * 
     * @param fileFolderService the service to do the work
     * @param sourceNodeRef the node to copy or move
     * @param destParentNodeRef the destination parent
     * @param name the name of the file or folder
     * @throws Exception
     */
    protected abstract void moveOrCopy(
            FileFolderService fileFolderService,
            NodeRef sourceNodeRef,
            NodeRef destParentNodeRef,
            String name) throws Exception;

    /**
     * Exceute the request
     * 
     * @exception WebDAVServerException
     */
    protected final void executeImpl() throws WebDAVServerException, Exception
    {
        FileFolderService fileFolderService = getFileFolderService();

        NodeRef rootNodeRef = getRootNodeRef();
        String servletPath = getServletPath();

        // Debug
        if (logger.isDebugEnabled())
        {
            logger.debug("Copy from " + getPath() + " to " + getDestinationPath());
        }

        // the source must exist
        String sourcePath = getPath();
        FileInfo sourceInfo = null;
        try
        {
            sourceInfo = getDAVHelper().getNodeForPath(rootNodeRef, sourcePath, servletPath);
        }
        catch (FileNotFoundException e)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }

        // the destination parent must exist
        String destPath = getDestinationPath();
        FileInfo destParentInfo = null;
        try
        {
            destParentInfo = getDAVHelper().getParentNodeForPath(rootNodeRef, destPath, servletPath);
        }
        catch (FileNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Destination parent folder doesn't exist: " + destPath);
            }
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }
        
        // check for the existence of the destination node
        FileInfo destInfo = null;
        try
        {
            destInfo = getDAVHelper().getNodeForPath(rootNodeRef, destPath, servletPath);
            if (!hasOverWrite())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Destination exists but overwrite is not allowed");
                }
                // it exists and we may not overwrite
                throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
            // delete the destination node if it is not the same as the source node
            if (!destInfo.getNodeRef().equals(sourceInfo.getNodeRef()))
            {
                // attempting to move or copy onto another node
                fileFolderService.delete(destInfo.getNodeRef());
            }
            else
            {
                // it is a copy or move onto itself
            }
        }
        catch (FileNotFoundException e)
        {
            // destination doesn't exist
        }

        NodeRef sourceNodeRef = sourceInfo.getNodeRef();
        NodeRef destParentNodeRef = destParentInfo.getNodeRef();
        String name = getDAVHelper().splitPath(destPath)[1];
        moveOrCopy(fileFolderService, sourceNodeRef, destParentNodeRef, name);

        // Set the response status
        if (destInfo == null)
        {
            m_response.setStatus(HttpServletResponse.SC_CREATED);
        }
        else
        {
            m_response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
