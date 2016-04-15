package org.alfresco.repo.activities.feed;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * Executes scheduled feed email notifier quartz-job - refer to scheduled-jobs-context.xml
 * 
 * @since 3.5
 */
public class FeedNotifierJob implements Job
{
    private static final String KEY_FEED_NOTIFIER = "feedNotifier";
    private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
    
    /**
     * Calls the feed notifier to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        
        final FeedNotifier feedNotifier = (FeedNotifier)jobData.get(KEY_FEED_NOTIFIER);
        final TenantAdminService tenantAdminService = (TenantAdminService)jobData.get(KEY_TENANT_ADMIN_SERVICE);
        
        Long repeatInterval = null;
        Trigger trigger = context.getTrigger();
        if (trigger instanceof SimpleTrigger)
        {
            repeatInterval = ((SimpleTrigger)trigger).getRepeatInterval();
        }
        
        final int repeatIntervalMins = new Long(repeatInterval == null ? 0L : repeatInterval / 1000 / 60).intValue();
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                feedNotifier.execute(repeatIntervalMins);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        if ((tenantAdminService != null) && tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        feedNotifier.execute(repeatIntervalMins);
                        return null;
                    }
                }, tenant.getTenantDomain());
            }
        }
    }
}
