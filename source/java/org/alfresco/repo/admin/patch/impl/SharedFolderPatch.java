/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * The SharedFolderPatch is a Generic Bootstrap Patch with the extra ability to 
 * rename an existing folder that is in the way (in a different namespace).
 * <p>
 * The first use-case is when there is a child called cm:shared and we want to patch a folder with app:shared
 * 
 * @author mrogers
 */
public class SharedFolderPatch extends GenericBootstrapPatch
{
    private JobLockService jobLockService;
    
    private long LOCK_TIME_TO_LIVE=10000;
    private long LOCK_REFRESH_TIME=5000;
    
    private String renamePath;
    
    private Log logger = LogFactory.getLog(SharedFolderPatch.class);
     
    private static final String MSG_RENAMED = "patch.sharedFolder.result.renamed";
    
    /**
     * Run the Shared Folder Patch asynchronously after bootstrap.
     */
    public void executeAsync()
    {
        // Lock the push
        QName lockQName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "patch.sharedFolder");
        String lockToken = jobLockService.getLock(lockQName, LOCK_TIME_TO_LIVE, 0, 1);
        SharedFolderPatchCallback callback = new SharedFolderPatchCallback();
        jobLockService.refreshLock(lockToken, lockQName, LOCK_REFRESH_TIME, callback);
        
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("SharedFolderPatch: job lock held");
            }
            
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    applyAsync();
                    return null;
                }
            });
        }
        finally
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("PUSH: job finished");
            }
            
            // Release the locks on the job and stop refreshing
            callback.isActive = false;
            jobLockService.releaseLock(lockToken, lockQName);
        }
    }

    @Override
    protected String applyInternal() throws Exception 
    {   
        StoreRef storeRef = importerBootstrap.getStoreRef();
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        if (getRenamePath() != null)
        {
            List<NodeRef> results = searchService.selectNodes(
                    rootNodeRef,
                    getRenamePath(),
                    null,
                    namespaceService,
                    false);
            
            if (results.size() > 1)
            {
                throw new PatchException(ERR_MULTIPLE_FOUND, renamePath);
            }
            else if (results.size() == 1)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("There is an existing node in the way path:" + getRenamePath());
                }
                // A node already exists that we must rename.
                NodeRef existingNodeRef = results.get(0);
                
                // get the path of the parent node e.g. company_home
                LinkedList<String> folderElements = new LinkedList<String>(Arrays.asList(getRenamePath().split("/")));
                folderElements.removeLast();
                
                StringBuffer parentPath = new StringBuffer();
                
                for(String folder : folderElements)
                {
                    parentPath.append("/");
                    parentPath.append(folder);
                }
                
                List<NodeRef> parentResults = searchService.selectNodes(
                        rootNodeRef,
                        parentPath.toString(),
                        null,
                        namespaceService,
                        false);
                
                if(parentResults.size()==1)
                {
                
                    NodeRef parentNodeRef = parentResults.get(0);
                    
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Found the parent node - doing a move parentNodeRef:" + parentNodeRef);
                    }
                
                    // rename the existing node
                    nodeService.moveNode(existingNodeRef, parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName( NamespaceService.APP_MODEL_1_0_URI, "shared"));
                    return I18NUtil.getMessage(MSG_RENAMED, renamePath);
                }
                else
                {
                    // Something has gone horribly wrong if we get here - we have multiple parents, or none despite finding the node earlier
                    throw new PatchException(ERR_MULTIPLE_FOUND, parentPath.toString());
                }
            }
        }
        
        // Else run the normal GenericBootstrapPatch implementation
        
        if(logger.isDebugEnabled())
        {
            logger.debug("Node does not already exist, Running the Generic Bootstrap Patch");
        }
        return super.applyInternal();
    }

    public void setRenamePath(String renamePath)
    {
        this.renamePath = renamePath;
    }

    public String getRenamePath()
    {
        return renamePath;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public JobLockService getJobLockService()
    {
        return jobLockService;
    }

    /**
     * Job to initiate the {@link SharedFolderPatch} if it has been deferred
     * 
     * @author Mark Rogers
     * @since 4.2
     */
    public static class SharedFolderPatchJob implements Job
    {
        public SharedFolderPatchJob()
        {
        }

        /**
         * Calls the cleaner to do its work
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            // extract the content cleaner to use
            Object sharedFolderPatchObj = jobData.get("sharedFolderPatch");
            if (sharedFolderPatchObj == null || !(sharedFolderPatchObj instanceof SharedFolderPatch))
            {
                throw new AlfrescoRuntimeException(
                        "'sharedFolderPatch' data must contain valid 'SharedFolderPatch' reference");
            }
            
            // Job Lock Here - should probably move into the patch service at some time.
            SharedFolderPatch sharedFolderPatch = (SharedFolderPatch) sharedFolderPatchObj;
            sharedFolderPatch.executeAsync();
        }
    }
    
    private class SharedFolderPatchCallback implements JobLockRefreshCallback
    {
        public boolean isActive = true;
    
        @Override
        public boolean isActive()
        {
            return isActive;
        }

        @Override
        public void lockReleased()
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("lock released");
            }
        }
    };
}
