package org.alfresco.repo.template;

import org.alfresco.repo.admin.SysAdminParamsImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.UrlUtil;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Site support in FreeMarker templates.
 * 
 * @author Mike Hatfield
 * @author Kevin Roast
 */
public class Site extends BaseTemplateProcessorExtension
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    private SiteService siteService;
    
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry	the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

    /**
     * Set the site service
     * 
     * @param siteService The siteService to set.
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Gets the SiteInfo given the shortName
     * 
     * @param shortName  the shortName of the Site to get
     * @return the Site or null if no such site exists 
     */
    public SiteInfo getSiteInfo(String shortName)
    {
        ParameterCheck.mandatoryString("shortName", shortName);
        return siteService.getSite(shortName);
    }
    
    /**
     * This method returns a URL stem which resolves to the configured Alfresco Share URL.
     * <p>
     * @see SysAdminParamsImpl#setAlfrescoHost(String)
     * @see SysAdminParamsImpl#setShareHost(String)
     */
    public String getShareUrl()
    {
        return UrlUtil.getShareUrl(services.getSysAdminParams());
    }
    
    /**
     * This method returns a URL which resolves to the configured Alfresco Share site URL.
     * <p>
     * @see SysAdminParamsImpl#setAlfrescoHost(String)
     * @see SysAdminParamsImpl#setShareHost(String)
     * @param siteShortName  the shortName of the Site to build URL for
     * @return the Site or null if no such site exists 
     */
    public String getShareSiteUrl(String siteShortName)
    {
        StringBuilder result = new StringBuilder(UrlUtil.getShareUrl(services.getSysAdminParams()));
        result.append("/page");
        if (siteShortName != null)
        {
            result.append("/site/").append(siteShortName);
        }
        
        return result.toString();
    }
}
