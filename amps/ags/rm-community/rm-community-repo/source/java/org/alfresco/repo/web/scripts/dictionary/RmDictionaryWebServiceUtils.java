/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.lang3.StringUtils;
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
