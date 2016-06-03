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
