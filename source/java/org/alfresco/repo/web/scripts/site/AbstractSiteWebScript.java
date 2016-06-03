package org.alfresco.repo.web.scripts.site;

import java.util.Map;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractSiteWebScript extends DeclarativeWebScript
{
    protected SiteService siteService;
    protected AuthorityService authorityService;
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    
    protected static String buildSiteGroup(SiteInfo site)
    {
        return "GROUP_site_" + site.getShortName();
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
          Status status, Cache cache) 
    {
       // Grab the site
       String siteName = 
           req.getServiceMatch().getTemplateVars().get("shortname");
       SiteInfo site = siteService.getSite(siteName);
       if (site == null)
       {
           throw new WebScriptException(
                   Status.STATUS_NOT_FOUND, 
                   "No Site found with that short name");
       }
       
       // Process
       return executeImpl(site, req, status, cache);
    }
    protected abstract Map<String, Object> executeImpl(SiteInfo site,
          WebScriptRequest req, Status status, Cache cache);
}