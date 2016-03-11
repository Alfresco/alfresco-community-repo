/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.attributes;

import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cleaner of unused values from the alf_prop_xxx tables.
 *
 * @author alex.mukha
 */
public class PropTablesCleaner
{
    private PropertyValueDAO propertyValueDAO;
    private JobLockService jobLockService;

    /* 1 minute */
    private static final long LOCK_TTL = 60000L;
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, PropTablesCleaner.class.getName());
    private static ThreadLocal<Pair<Long, String>> lockThreadLocal = new ThreadLocal<Pair<Long, String>>();

    private static Log logger = LogFactory.getLog(PropTablesCleaner.class);

    public void checkProperties()
    {
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "propertyValueDAO", propertyValueDAO);
    }

    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * Lazily update the job lock
     */
    private void refreshLock()
    {
        Pair<Long, String> lockPair = lockThreadLocal.get();
        if (lockPair == null)
        {
            String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            Long lastLock = new Long(System.currentTimeMillis());
            // We have not locked before
            lockPair = new Pair<Long, String>(lastLock, lockToken);
            lockThreadLocal.set(lockPair);
        }
        else
        {
            long now = System.currentTimeMillis();
            long lastLock = lockPair.getFirst().longValue();
            String lockToken = lockPair.getSecond();
            // Only refresh the lock if we are past a threshold
            if (now - lastLock > (long)(LOCK_TTL/2L))
            {
                jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL);
                lastLock = System.currentTimeMillis();
                lockPair = new Pair<Long, String>(lastLock, lockToken);
                lockThreadLocal.set(lockPair);
            }
        }
    }

    /**
     * Release the lock after the job completes
     */
    private void releaseLock()
    {
        Pair<Long, String> lockPair = lockThreadLocal.get();
        if (lockPair != null)
        {
            // We can't release without a token
            try
            {
                jobLockService.releaseLock(lockPair.getSecond(), LOCK_QNAME);
            }
            finally
            {
                // Reset
                lockThreadLocal.set(null);
            }
        }
        // else: We can't release without a token
    }

    public void execute()
    {
        checkProperties();
        try
        {
            refreshLock();
            propertyValueDAO.cleanupUnusedValues();
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping prop tables cleaning.  " + e.getMessage());
            }
        }
        finally
        {
            releaseLock();
        }
    }
}
