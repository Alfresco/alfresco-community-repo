/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
public class RmDictionaryWebServiceUtils
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
            if (site != null)
            {
                if (site.getSitePreset().equals(SITE_PRESET))
                {
                    isRmSite = true;
                }
            }
        }
        return isRmSite;
    }
}
