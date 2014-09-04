/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.webdav.WebDavService;

/**
 * Implements the WebDAV MKCOL method
 * 
 * @author gavinc
 */
public class MkcolMethod extends WebDAVMethod implements ActivityPostProducer
{
    private WebDAVActivityPoster activityPoster;
    
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

        if (m_request.getContentLength() > 0)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /**
     * Execute the request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        FileFolderService fileFolderService = getFileFolderService();

        // see if it exists
        try
        {
            getDAVHelper().getNodeForPath(getRootNodeRef(), getPath());
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
                FileInfo parentFileInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), parentPath);
                parentNodeRef = parentFileInfo.getNodeRef();
            }
            catch (FileNotFoundException e)
            {
                // parent path is missing
                throw new WebDAVServerException(HttpServletResponse.SC_CONFLICT);
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
        FileInfo fileInfo = fileFolderService.create(parentNodeRef, folderName, ContentModel.TYPE_FOLDER);
        
        // Don't post activity data for hidden folder
        if (!fileInfo.isHidden())
        {
             postActivity(fileInfo);
        }
        
        // Return a success status
        m_response.setStatus(HttpServletResponse.SC_CREATED);
    }
    
    /**
     * Create a folder added activity post.
     * 
     * @throws WebDAVServerException 
     */
    private void postActivity(FileInfo fileInfo) throws WebDAVServerException
    {
        WebDavService davService = getDAVHelper().getServiceRegistry().getWebDavService();
        if (!davService.activitiesEnabled())
        {
            // Don't post activities if this behaviour is disabled.
            return;
        }
        
        String siteId = getSiteId();
        String tenantDomain = getTenantDomain();
        
        // Check there is enough information to publish site activity.
        if (!siteId.equals(WebDAVHelper.EMPTY_SITE_ID))
        {
            SiteService siteService = getServiceRegistry().getSiteService();
            NodeRef documentLibrary = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
            String path = "/";
            try
            {
                path = getDAVHelper().getPathFromNode(documentLibrary, fileInfo.getNodeRef());
            }
            catch (FileNotFoundException error)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No " + SiteService.DOCUMENT_LIBRARY + " container found.");
                }
            }
            
            activityPoster.postFileFolderAdded(siteId, tenantDomain, path, fileInfo);
        }
    }
    
    @Override
    public void setActivityPoster(WebDAVActivityPoster activityPoster)
    {
        this.activityPoster = activityPoster;
    }
}
