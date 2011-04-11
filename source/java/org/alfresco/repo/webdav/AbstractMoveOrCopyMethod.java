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

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
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
            NodeRef sourceParentNodeRef,
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

        FileInfo sourceParentInfo = getDAVHelper().getParentNodeForPath(rootNodeRef, sourcePath, servletPath);

        // the destination parent must exist
        String destPath = getDestinationPath();
        FileInfo destParentInfo = null;
        try
        {
            if (destPath.endsWith(WebDAVHelper.PathSeperator))
            {
                destPath = destPath.substring(0, destPath.length() - 1);
            }
            destParentInfo = getDAVHelper().getParentNodeForPath(rootNodeRef, destPath, servletPath);
        }
        catch (FileNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Destination parent folder doesn't exist: " + destPath);
            }
            throw new WebDAVServerException(HttpServletResponse.SC_CONFLICT);
        }
        
        // check for the existence of the destination node
        FileInfo destInfo = null;
        try
        {
            destInfo = getDAVHelper().getNodeForPath(rootNodeRef, destPath, servletPath);
            // ALF-7079 fix, if destInfo is working copy then content will be updated later
			boolean isDestWorkingCopy = getNodeService().hasAspect(destInfo.getNodeRef(), ContentModel.ASPECT_WORKING_COPY);
            if (!hasOverWrite() && !isDestWorkingCopy)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Destination exists but overwrite is not allowed");
                }
                // it exists and we may not overwrite
                throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
            // delete the destination node if it is not the same as the source node and not a working copy
            if (!destInfo.getNodeRef().equals(sourceInfo.getNodeRef()) && !isDestWorkingCopy)
            {
                checkNode(destInfo);

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
        NodeRef sourceParentNodeRef = sourceParentInfo.getNodeRef();
        NodeRef destParentNodeRef = destParentInfo.getNodeRef();
        
        String name = getDAVHelper().splitPath(destPath)[1];

        moveOrCopy(fileFolderService, sourceNodeRef, sourceParentNodeRef, destParentNodeRef, name);

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
