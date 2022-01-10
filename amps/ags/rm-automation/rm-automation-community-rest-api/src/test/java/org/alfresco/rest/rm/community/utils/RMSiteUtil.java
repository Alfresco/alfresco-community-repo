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
package org.alfresco.rest.rm.community.utils;

import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.DOD5015;
import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.STANDARD;

import org.alfresco.rest.rm.community.model.site.RMSiteCompliance;
import org.alfresco.rest.rm.community.model.site.RMSite;

/**
 * Utility class for the RM Site
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RMSiteUtil
{
    private RMSiteUtil()
    {
        // Intentionally blank
    }

    /** Constants */
    public static final String RM_ID = "rm";
    public static final String RM_TITLE = "Records Management";
    public static final String RM_DESCRIPTION = "Records Management Site";
    public static final String FILE_PLAN_PATH = "/rm/documentLibrary";

    /**
     * Creates an RM Site model for the given compliance, title and description
     *
     * @param compliance The RM site compliance
     * @param title The site title
     * @param description The site description
     * @return The {@link RMSite} with the given details
     */
    public static RMSite createRMSiteModel(RMSiteCompliance compliance, String title, String description)
    {
        return RMSite.builder().compliance(compliance).title(title).description(description).build();
    }

    /**
     * Creates an RM Site for the given compliance and default title and description
     *
     * @param compliance The RM site compliance
     * @return The {@link RMSite} with the given details
     */
    private static RMSite createRMSiteModel(RMSiteCompliance compliance)
    {
        return createRMSiteModel(compliance, RM_TITLE, RM_DESCRIPTION);
    }

    /**
     * Creates a standard RM site with the default title and description
     *
     * @return The standard RM site
     */
    public static RMSite createStandardRMSiteModel()
    {
        return createRMSiteModel(STANDARD);
    }

    /**
     * Creates a DOD5015 compliance RM site with the default title and description
     *
     * @return The DOD5015 compliance RM site
     */
    public static RMSite createDOD5015RMSiteModel()
    {
        return createRMSiteModel(DOD5015);
    }
}
