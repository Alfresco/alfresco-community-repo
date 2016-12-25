/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import org.alfresco.rest.rm.community.model.site.RMSiteModel;

/**
 * FIXME!!!
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

    /**
     * FIXME!!!
     *
     * @param compliance FIXME!!!
     * @return FIXME!!!
     */
    private static RMSiteModel createRMSiteModel(RMSiteCompliance compliance)
    {
        RMSiteModel rmSiteModel =  RMSiteModel.builder().compliance(compliance).build();
        rmSiteModel.setTitle(RM_TITLE);
        rmSiteModel.setDescription(RM_DESCRIPTION);
        return rmSiteModel;
    }

    /**
     * FIXME!!!
     *
     * @return FIXME!!!
     */
    public static RMSiteModel createStandardRMSiteModel()
    {
        return createRMSiteModel(STANDARD);
    }

    /**
     * FIXME!!!
     *
     * @return FIXME!!!
     */
    public static RMSiteModel createDOD5015RMSiteModel()
    {
        return createRMSiteModel(DOD5015);
    }

}
