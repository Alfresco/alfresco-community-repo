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

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Implements the WebDAV PUT method
 * 
 * @author Gavin Cornwell
 */
public class PutMethod extends WebDAVMethod implements ActivityPostProducer
{
    // Request parameters
    private String m_strContentType = null;
    private boolean m_expectHeaderPresent = false;
    // Indicates if a zero byte node was created by a LOCK call.
    // Try to delete the node if the PUT fails
    private boolean noContent = false;
    private boolean created = false;
    private ActivityPoster activityPoster;
    private FileInfo contentNodeInfo;
    private long fileSize;
    
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
                            contentNodeInfo = getNodeForPath(getRootNodeRef(), getPath());
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
        LockInfo nodeLockInfo = null;
        try
        {
            contentNodeInfo = getNodeForPath(getRootNodeRef(), getPath());
            // make sure that we are not trying to use a folder
            if (contentNodeInfo.isFolder())
            {
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
            }

            nodeLockInfo = checkNode(contentNodeInfo);
            
            // 'Unhide' nodes hidden by us and behave as though we created them
            NodeRef contentNodeRef = contentNodeInfo.getNodeRef();
            if (fileFolderService.isHidden(contentNodeRef) && !getDAVHelper().isRenameShuffle(getPath()))
            {
                fileFolderService.setHidden(contentNodeRef, false);
                created = true;
            }
        }
        catch (FileNotFoundException e)
        {
            // the file doesn't exist - create it
            String[] paths = getDAVHelper().splitPath(getPath());
            try
            {
                FileInfo parentNodeInfo = getNodeForPath(getRootNodeRef(), paths[0]);
                // create file
                contentNodeInfo = getDAVHelper().createFile(parentNodeInfo, paths[1]);  
                created = true;
                
            }
            catch (FileNotFoundException ee)
            {
                // bad path
                throw new WebDAVServerException(HttpServletResponse.SC_CONFLICT);
            }
            catch (FileExistsException ee)
            {
                // ALF-7079 fix, retry: it looks like concurrent access (file not found but file exists) 
                throw new ConcurrencyFailureException("Concurrent access was detected.",  ee);
            }
        }
        
        String userName = getDAVHelper().getAuthenticationService().getCurrentUserName();
        LockInfo lockInfo = getDAVLockService().getLockInfo(contentNodeInfo.getNodeRef());
        
        if (lockInfo != null)
        {
            if (lockInfo.isLocked() && !lockInfo.getOwner().equals(userName))
            {
                if (logger.isDebugEnabled())
                {
                    String path = getPath();
                    String owner = lockInfo.getOwner();
                    logger.debug("Node locked: path=["+path+"], owner=["+owner+"], current user=["+userName+"]");
                }
                // Indicate that the resource is locked
                throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
            }
        }
        // ALF-16808: We disable the versionable aspect if we are overwriting
        // empty content because it's probably part of a compound operation to
        // create a new single version
        boolean disabledVersioning = false;
        
        try
        {
            // Disable versioning if we are overwriting an empty file with content
            NodeRef nodeRef = contentNodeInfo.getNodeRef();
            ContentData contentData = (ContentData)getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if ((contentData == null || contentData.getSize() == 0) && getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            {
                getDAVHelper().getPolicyBehaviourFilter().disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                disabledVersioning = true;
            }
            // ALF-16756: To avoid firing inbound rules too early (while a node is still locked) apply the no content aspect
            if (nodeLockInfo != null && nodeLockInfo.isExclusive() && !(ContentData.hasContent(contentData) && contentData.getSize() > 0))
            {
                getNodeService().addAspect(contentNodeInfo.getNodeRef(), ContentModel.ASPECT_NO_CONTENT, null);
            }
            // Access the content
            ContentWriter writer = fileFolderService.getWriter(contentNodeInfo.getNodeRef());
        
            // set content properties
            writer.guessMimetype(contentNodeInfo.getName());
            writer.guessEncoding();
    
            // Get the input stream from the request data
            InputStream is = m_request.getInputStream();
        
    
            // Write the new data to the content node
            writer.putContent(is);
            // Ask for the document metadata to be extracted
            Action extract = getActionService().createAction(ContentMetadataExtracter.EXECUTOR_NAME);
            if(extract != null)
            {
               extract.setExecuteAsynchronously(false);
               getActionService().executeAction(extract, contentNodeInfo.getNodeRef());
            }

            // If the mime-type determined by the repository is different
            // from the original specified in the request, update it.
            if (m_strContentType == null || !m_strContentType.equals(writer.getMimetype()))
            {
                String oldMimeType = m_strContentType;
                m_strContentType = writer.getMimetype();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Mimetype originally specified as " + oldMimeType +
                                ", now guessed to be " + m_strContentType);
                }
            }

            // Record the uploaded file's size
            fileSize = writer.getSize();
            
            // Set the response status, depending if the node existed or not
            m_response.setStatus(created ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_NO_CONTENT);
        }
        catch (AccessDeniedException e)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_FORBIDDEN, e);
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
        finally
        {
            if (disabledVersioning)
            {
                getDAVHelper().getPolicyBehaviourFilter().enableBehaviour(contentNodeInfo.getNodeRef(), ContentModel.ASPECT_VERSIONABLE);
            }
        }

        postActivity();

    }
    
    /**
     * Can be used after a successful {@link #execute()} invocation to
     * check whether the resource was new (created) or over-writing existing
     * content.
     * 
     * @return true if the content was newly created, false if existing.
     */
    protected boolean isCreated()
    {
        return created;
    }
    
    /**
     * Retrieve the mimetype of the content sent for the PUT request. The initial
     * value specified in the request may be updated after the file contents have
     * been uploaded if the repository has determined a different mimetype for the content.
     * 
     * @return content-type
     */
    public String getContentType()
    {
        return m_strContentType;
    }

    /**
     * The FileInfo for the uploaded file, or null if not yet uploaded.
     * 
     * @return FileInfo
     */
    public FileInfo getContentNodeInfo()
    {
        return contentNodeInfo;
    }

    /**
     * Returns the size of the uploaded file, zero if not yet uploaded.
     * 
     * @return the fileSize
     */
    public long getFileSize()
    {
        return fileSize;
    }

    /**
     * Create an activity post.
     * 
     * @throws WebDAVServerException 
     */
    protected void postActivity() throws WebDAVServerException
    {
        WebDavService davService = getDAVHelper().getServiceRegistry().getWebDavService();
        if (!davService.activitiesEnabled())
        {
            // Don't post activities if this behaviour is disabled.
            return;
        }
        
        String path = getPath();
        String siteId = getSiteId();
        String tenantDomain = getTenantDomain();
        
        if (siteId.equals(WebDAVHelper.EMPTY_SITE_ID))
        {
            // There is not enough information to publish site activity.
            return;
        }
        
        FileInfo contentNodeInfo = null;
        try
        {
            contentNodeInfo = getNodeForPath(getRootNodeRef(), path);
            NodeRef nodeRef = contentNodeInfo.getNodeRef();
            // Don't post activity data for hidden files, resource forks etc.
            if (!getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
            {
                if (isCreated())
                {
                    // file added
                    activityPoster.postFileFolderAdded(siteId, tenantDomain, null, contentNodeInfo);
                }
                else
                {
                    // file updated
                    activityPoster.postFileFolderUpdated(siteId, tenantDomain, contentNodeInfo);
                }
            }
        }
        catch (FileNotFoundException error)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    public void setActivityPoster(ActivityPoster activityPoster)
    {
        this.activityPoster = activityPoster;
    }
}
