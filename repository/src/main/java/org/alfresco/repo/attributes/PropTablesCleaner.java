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
import org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizer;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * Cleaner of unused values from the alf_prop_xxx tables.
 *
 * @author alex.mukha
 */
public class PropTablesCleaner
{
    private static final String PROPERTY_PROP_TABLE_CLEANER_ALG = "system.prop_table_cleaner.algorithm";
    private static final String PROP_TABLE_CLEANER_ALG_V2 = "V2";

    private PropertyValueDAO propertyValueDAO;
    private JobLockService jobLockService;
    private Properties globalProperties;

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

    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }

    public void checkProperties()
    {
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "propertyValueDAO", propertyValueDAO);
        PropertyCheck.mandatory(this, "globalProperties", globalProperties);
    }

    /**
     * Get {@link #LOCK_QNAME a lock} for {@link #LOCK_TTL a long-running job} and {@link PropertyValueDAO#cleanupUnusedValues() call through}
     * to get the unused data cleaned up.
     */
    public void execute()
    {
        checkProperties();

        String propCleanUplockToken = null;
        String ldapSyncLockTocken = null;
        try
        {
            // Get a lock for cleanup
            propCleanUplockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            // Get a lock for LDAP sync as well, see REPO-4556
            ldapSyncLockTocken = jobLockService.getLock(ChainingUserRegistrySynchronizer.LOCK_QNAME, LOCK_TTL);

            if (PROP_TABLE_CLEANER_ALG_V2.equalsIgnoreCase(getAlgorithm()))
            {
                propertyValueDAO.cleanupUnusedValuesV2();
            }
            else
            {
                propertyValueDAO.cleanupUnusedValues();
            }
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
            if (propCleanUplockToken != null)
            {
                try
                {
                    jobLockService.releaseLock(propCleanUplockToken, LOCK_QNAME);
                }
                catch (LockAcquisitionException e)
                {
                    // Ignore
                }
            }
            if (ldapSyncLockTocken != null)
            {
                try
                {
                    jobLockService.releaseLock(ldapSyncLockTocken, ChainingUserRegistrySynchronizer.LOCK_QNAME);
                }
                catch (LockAcquisitionException e)
                {
                    // Ignore
                }
            }
        }
    }

    private String getAlgorithm()
    {
        return globalProperties.getProperty(PROPERTY_PROP_TABLE_CLEANER_ALG);
    }
}
