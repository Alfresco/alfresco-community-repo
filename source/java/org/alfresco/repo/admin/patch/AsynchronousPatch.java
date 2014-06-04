/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.admin.patch;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
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
 * Base implementation of the asynchronous patch.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AsynchronousPatch extends AbstractPatch
{
    private static final Log logger = LogFactory.getLog(AsynchronousPatch.class);

    private static final String JOB_NAME = "asynchronousPatch";

    private static final String MSG_CHECKING = "patch.asynchrounse.checking";
    private static final String MSG_NO_PATCHES_REQUIRED = "patch.executer.no_patches_required";
    private static final String MSG_SYSTEM_READ_ONLY = "patch.executer.system_readonly";
    private static final String MSG_NOT_EXECUTED = "patch.executer.not_executed";
    private static final String MSG_EXECUTED = "patch.executer.executed";
    private static final String MSG_FAILED = "patch.executer.failed";

    private static final long LOCK_TIME_TO_LIVE = 10000;
    private static final long LOCK_REFRESH_TIME = 5000;

    private JobLockService jobLockService;

    /**
     * @param jobLockService the jobLockService to set
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(jobLockService, "jobLockService");
    }

    public void executeAsynchronously()
    {
        // Lock the push
        QName lockQName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, this.getId());
        String lockToken = jobLockService.getLock(lockQName, LOCK_TIME_TO_LIVE, 0, 1);
        AsyncPatchCallback callback = new AsyncPatchCallback();
        jobLockService.refreshLock(lockToken, lockQName, LOCK_REFRESH_TIME, callback);

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(this.getId() + ": job lock held");
            }
            applyOutstandingPatch(this);
        }
        finally
        {
            if (logger.isTraceEnabled())
            {
                logger.trace(this.getId() + ": job finished");
            }

            // Release the locks on the job and stop refreshing
            callback.isActive = false;
            jobLockService.releaseLock(lockToken, lockQName);
        }
    }

    private void applyOutstandingPatch(Patch patch)
    {
        // Apply the patch even if we are in read only mode. The system may not
        // work safely otherwise.

        if (!patchService.validatePatch(patch))
        {
            logger.warn(I18NUtil.getMessage(MSG_SYSTEM_READ_ONLY));
            return;
        }

        logger.info(I18NUtil.getMessage(MSG_CHECKING));

        AppliedPatch appliedPatch = patchService.getPatch(this.getId());
        // Don't bother if the patch has already been applied successfully
        if (appliedPatch != null && appliedPatch.getSucceeded())
        {
            logger.info(I18NUtil.getMessage(MSG_NO_PATCHES_REQUIRED));
            return;
        }

        patchService.applyOutstandingPatch(this);

        // get the executed patch
        appliedPatch = patchService.getPatch(patch.getId());

        if (!appliedPatch.getWasExecuted())
        {
            // the patch was not executed. E.g. not relevant to the current schema 
            logger.info(I18NUtil.getMessage(MSG_NOT_EXECUTED, appliedPatch.getId(), appliedPatch.getReport()));
        }
        else if (appliedPatch.getSucceeded())
        {
            logger.info(I18NUtil.getMessage(MSG_EXECUTED, appliedPatch.getId(), appliedPatch.getReport()));
        }
        else
        {
            logger.error(I18NUtil.getMessage(MSG_FAILED, appliedPatch.getId(), appliedPatch.getReport()));
            throw new AlfrescoRuntimeException("Not all patches could be applied.");
        }
    }

    /**
     * Job to initiate the {@link AsynchronousPatch} if it has been deferred
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class AsynchronousPatchJob implements Job
    {
        public AsynchronousPatchJob()
        {
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            // extract the object to use
            Object asyncPatchObj = jobData.get(JOB_NAME);
            if (asyncPatchObj == null || !(asyncPatchObj instanceof AsynchronousPatch))
            {
                throw new AlfrescoRuntimeException(JOB_NAME + " data must contain valid 'AsynchronousPatch' reference");
            }
            // Job Lock
            AsynchronousPatch patch = (AsynchronousPatch) asyncPatchObj;
            patch.executeAsynchronously();
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    private class AsyncPatchCallback implements JobLockRefreshCallback
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
    }
}
