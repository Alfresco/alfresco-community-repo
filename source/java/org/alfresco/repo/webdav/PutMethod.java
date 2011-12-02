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

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
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
    
    // Indicates if a zero byte node was created by a LOCK call.
    // Try to delete the node if the PUT fails
    private boolean noContent = false;

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
     * Clears the aspect added by a LOCK request for a new file, so
     * that the Timer started by the LOCK request will not remove the
     * node now that the PUT request has been received. This is needed
     * for large content.
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // Nothing is done with the body by this method. The body contains
        // the content it will be dealt with later.
        
        // This method is called ONCE just before the FIRST call to executeImpl,
        // which is in a retrying transaction so may be called many times.
        
        // Although this method is called just before the first executeImpl,
        // it is possible that the Thread could be interrupted before the first call
        // or between calls. However the chances are low and the consequence
        // (leaving a zero byte file) is minor.
  
        noContent = getTransactionService().getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Boolean>()
                {
                    public Boolean execute() throws Throwable
                    {
                        FileInfo contentNodeInfo = null;
                        try
                        {
                            contentNodeInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), getPath(), getServletPath());
                            checkNode(contentNodeInfo);
                            final NodeRef nodeRef = contentNodeInfo.getNodeRef();
                            if (getNodeService().hasAspect(contentNodeInfo.getNodeRef(), ContentModel.ASPECT_WEBDAV_NO_CONTENT))
                            {
                                getNodeService().removeAspect(nodeRef, ContentModel.ASPECT_WEBDAV_NO_CONTENT);
                                if (logger.isDebugEnabled())
                                {
                                    String path = getPath();
                                    logger.debug("Put Timer DISABLE " + path);
                                }
                                return Boolean.TRUE;
                            }
                        }
                        catch (FileNotFoundException e)
                        {
                            // Does not exist, so there will be no aspect.
                        }
                        return Boolean.FALSE;
                    }
                }, false, true);
    }

    /**
     * Execute the WebDAV request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        if (logger.isDebugEnabled())
        {
            String path = getPath();
            String userName = getDAVHelper().getAuthenticationService().getCurrentUserName();
            logger.debug("Put node: \n" +
                    "     user: " + userName + "\n" +
                    "     path: " + path + "\n" +
                    "noContent: " + noContent);
        }

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

        try
        {
            // Access the content
            ContentWriter writer = fileFolderService.getWriter(contentNodeInfo.getNodeRef());
        
            // set content properties
            if (m_strContentType != null)
            {
                writer.setMimetype(m_strContentType);
            }
            else
            {
                writer.guessMimetype(contentNodeInfo.getName());
            }
            writer.guessEncoding();
    
            // Get the input stream from the request data
            InputStream is = m_request.getInputStream();
        
    
            // Write the new data to the content node
            writer.putContent(is);
            // Ask for the document metadata to be extracted
            Action extract = getActionService().createAction(ContentMetadataExtracter.EXECUTOR_NAME);
            if(extract != null)
            {
               extract.setExecuteAsynchronously(true);
               getActionService().executeAction(extract, contentNodeInfo.getNodeRef());
            }

    

            // Set the response status, depending if the node existed or not
            m_response.setStatus(created ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_NO_CONTENT);
        }
        catch (Throwable e) 
        {
            // check if the node was marked with noContent aspect previously by lock method AND
            // we are about to give up
            if (noContent && RetryingTransactionHelper.extractRetryCause(e) == null)
            {
                // remove the 0 bytes content if save operation failed or was cancelled
                final NodeRef nodeRef = contentNodeInfo.getNodeRef();
                getTransactionService().getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionCallback<String>()
                        {
                            public String execute() throws Throwable
                            {
                                getNodeService().deleteNode(nodeRef);
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Put failed. DELETE  " + getPath());
                                }
                                return null;
                            }
                        }, false, true);
            }
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
