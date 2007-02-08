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
