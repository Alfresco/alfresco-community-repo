package org.alfresco.repo.security.authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.StatefulJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Andy
 *
 */
public class TicketCleanupJob implements StatefulJob
{

    public TicketCleanupJob()
    {
    }

    /**
     * Calls the cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content cleaner to use
        Object abstractAuthenticationServiceRef = jobData.get("abstractAuthenticationService");
        if (abstractAuthenticationServiceRef == null || !(abstractAuthenticationServiceRef instanceof AbstractAuthenticationService))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'contentStoreCleaner' reference");
        }
        AbstractAuthenticationService abstractAuthenticationService = (AbstractAuthenticationService) abstractAuthenticationServiceRef;
        abstractAuthenticationService.invalidateTickets(true);
    }

}
