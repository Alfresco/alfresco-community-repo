package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Util class for dictionary web services
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public final class RmDictionaryWebServiceUtils
{
    private static final String SITE_ID = "siteId";
    private static final String SITE_PRESET = "rm-site-dashboard";

    private RmDictionaryWebServiceUtils()
    {
        // Will not be called
    }

    public static boolean isRmSite(WebScriptRequest req, SiteService siteService)
    {
        boolean isRmSite = false;
        String siteId = req.getParameter(SITE_ID);
        if (StringUtils.isNotBlank(siteId))
        {
            SiteInfo site = siteService.getSite(siteId);
            if (site != null && site.getSitePreset().equals(SITE_PRESET))
            {
                isRmSite = true;
            }
        }
        return isRmSite;
    }
}
