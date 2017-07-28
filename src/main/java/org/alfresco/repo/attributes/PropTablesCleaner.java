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
package org.alfresco.repo.attributes;

import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
    private static final long LOCK_TTL = 360000L;
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, PropTablesCleaner.class.getName());

    private static Log logger = LogFactory.getLog(PropTablesCleaner.class);

    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void checkProperties()
    {
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "propertyValueDAO", propertyValueDAO);
    }

    /**
     * Get {@link #LOCK_QNAME a lock} for {@link #LOCK_TTL a long-running job} and {@link PropertyValueDAO#cleanupUnusedValues() call through}
     * to get the unused data cleaned up.
     */
    public void execute()
    {
        checkProperties();
        
        String lockToken = null;
        try
        {
            // Get a lock
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            propertyValueDAO.cleanupUnusedValues();
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping prop tables cleaning (could not get lock): " + e.getMessage());
            }
        }
        finally
        {
            if (lockToken != null)
            {
                try
                {
                    jobLockService.releaseLock(lockToken, LOCK_QNAME);
                }
                catch (LockAcquisitionException e)
                {
                    // Ignore
                }
            }
        }
    }
}
