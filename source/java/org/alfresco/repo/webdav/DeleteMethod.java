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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.FileFilterMode;

/**
 * Implements the WebDAV DELETE method
 * 
 * @author gavinc
 */
public class DeleteMethod extends WebDAVMethod implements ActivityPostProducer
{
    private ActivityPoster activityPoster;
    
    /**
     * Default constructor
     */
    public DeleteMethod()
    {
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        parseIfHeader();
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // Nothing to do in this method
    }

    /**
     * @deprecated MNT-8704: WebDAV:Content does not disappear after being deleted
     */
    @Deprecated
    private static final Timer deleteDelayTimer = new Timer();
    
    /**
     * Execute the request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("WebDAV DELETE: " + getPath());
        }
        
        final FileFolderService fileFolderService = getFileFolderService();
        final PermissionService permissionService = getPermissionService();

        NodeRef rootNodeRef = getRootNodeRef();

        String path = getPath();
        List<String> pathElements = getDAVHelper().splitAllPaths(path);
        FileInfo fileInfo = null;
        try
        {
            // get the node to delete
            fileInfo = fileFolderService.resolveNamePath(rootNodeRef, pathElements);
        }
        catch (FileNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Node not found: " + getPath());
            }
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }

        checkNode(fileInfo);

        final NodeService nodeService = getNodeService();
        final NodeRef nodeRef = fileInfo.getNodeRef();
        
        if (permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED)
        {
        	// As this content will be deleted, we need to extract some info before it's no longer available.
        	String siteId = getSiteId();
        	NodeRef deletedNodeRef = fileInfo.getNodeRef();
        	FileInfo parentFile = getDAVHelper().getParentNodeForPath(getRootNodeRef(), path);

        	// Don't post activity data for hidden files, resource forks etc.
        	if (!getDAVHelper().isRenameShuffle(path))
        	{
        		postActivity(parentFile, fileInfo, siteId);
        	}
            
            // MNT-181: working copies and versioned nodes are hidden rather than deleted
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) || nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            {
                // Mark content as hidden.  This breaks many contracts and will be fixed for "ALF-18619 WebDAV/SPP file shuffles" 
                fileFolderService.setHidden(nodeRef, true);
                {
                    // Workaround for MNT-8704: WebDAV:Content does not disappear after being deleted
                    // Get the current user
                    final String deleteDelayUser = AuthenticationUtil.getFullyAuthenticatedUser();
                    // Add a timed task to really delete the file
                    TimerTask deleteDelayTask = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            RunAsWork<Void> deleteDelayRunAs = new RunAsWork<Void>()
                            {
                                @Override
                                public Void doWork() throws Exception
                                {
                                     // Ignore if it is NOT hidden: the shuffle may have finished; the operation may have failed
                                    if (!nodeService.exists(nodeRef) || !fileFolderService.isHidden(nodeRef))
                                    {
                                        return null;
                                    }
                                    
                                    // Since this will run in a different thread, the client thread-local must be set
                                    // or else unhiding the node will not unhide it for WebDAV.
                                    FileFilterMode.setClient(FileFilterMode.Client.webdav);
                                    
                                    // Unhide the node, e.g. for archiving
                                    fileFolderService.setHidden(nodeRef, false);
                                    
                                    // This is the transaction-aware service
                                    fileFolderService.delete(nodeRef);
                                    return null;
                                }
                            };
                            try
                            {
                                AuthenticationUtil.runAs(deleteDelayRunAs, deleteDelayUser);
                            }
                            catch (Throwable e)
                            {
                                // consume exception to avoid it leaking from the TimerTask and causing the Timer to
                                // no longer accept tasks to be scheduled.
                                logger.info("Exception thrown during WebDAV delete timer task.", e);
                            }
                        }
                    };
                    // Schedule a real delete 5 seconds after the current time
                    deleteDelayTimer.schedule(deleteDelayTask, 5000L);
                }
                getDAVLockService().unlock(nodeRef);
            }
            // We just ensure already-hidden nodes are left unlocked
            else if (fileFolderService.isHidden(nodeRef))
            {
            	getDAVLockService().unlock(nodeRef);            
            }
            // A 'real' delete
            else
            {
                // Delete it
                fileFolderService.delete(deletedNodeRef);
            }
        }
        else
        {
            // access denied
            throw new WebDAVServerException(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    
    /**
     * Create a deletion activity post.
     * 
     * @param parent The FileInfo for the deleted file's parent.
     * @param deletedFile The FileInfo for the deleted file.
     * @throws WebDAVServerException 
     */
    protected void postActivity(FileInfo parent, FileInfo deletedFile, String siteId) throws WebDAVServerException
    {
        WebDavService davService = getDAVHelper().getServiceRegistry().getWebDavService();
        if (!davService.activitiesEnabled())
        {
            // Don't post activities if this behaviour is disabled.
            return;
        }
        
        String tenantDomain = getTenantDomain();
        
        // Check there is enough information to publish site activity.
        if (!siteId.equals(WebDAVHelper.EMPTY_SITE_ID))
        {
            SiteService siteService = getServiceRegistry().getSiteService();
            NodeRef documentLibrary = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
            String parentPath = "/";
            try
            {
                parentPath = getDAVHelper().getPathFromNode(documentLibrary, parent.getNodeRef());
            }
            catch (FileNotFoundException error)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No " + SiteService.DOCUMENT_LIBRARY + " container found.");
                }
            }
            
            activityPoster.postFileFolderDeleted(siteId, tenantDomain, parentPath, parent, deletedFile);
        }  
    }
    
    @Override
    public void setActivityPoster(ActivityPoster activityPoster)
    {
        this.activityPoster = activityPoster;
    }
}
