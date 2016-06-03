package org.alfresco.repo.security.sync;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A scheduled job that regularly invokes a {@link UserRegistrySynchronizer}. Supports a
 * <code>synchronizeChangesOnly</code> string parameter. When <code>"false"</code> means that the
 * {@link UserRegistrySynchronizer#synchronize(boolean, boolean)} method will be called with a <code>true</code> forceUpdate
 * argument rather than the default <code>false</code>.
 * 
 * @author dward
 */
public class UserRegistrySynchronizerJob implements Job
{
    public void execute(JobExecutionContext executionContext) throws JobExecutionException
    {
        final UserRegistrySynchronizer userRegistrySynchronizer = (UserRegistrySynchronizer) executionContext
                .getJobDetail().getJobDataMap().get("userRegistrySynchronizer");
        final String synchronizeChangesOnly = (String) executionContext.getJobDetail().getJobDataMap().get("synchronizeChangesOnly");
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                userRegistrySynchronizer.synchronize(synchronizeChangesOnly == null || !Boolean.parseBoolean(synchronizeChangesOnly), true);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
