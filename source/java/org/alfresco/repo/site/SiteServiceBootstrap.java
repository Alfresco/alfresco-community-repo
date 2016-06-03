package org.alfresco.repo.site;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Warms up site zone / authority caches before the first access to a user dashboard
 * 
 * @author dward
 */
public class SiteServiceBootstrap extends AbstractLifecycleBean
{
    private SiteService siteService;

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                siteService.listSites("a");
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
