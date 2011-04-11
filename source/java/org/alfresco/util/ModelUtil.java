/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.util;

import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;


/**
 * Model related utility functions.
 * 
 * @since 3.5
 */
public class ModelUtil
{
    private static final String SHARE = "Share";
    private static final String TEAM = "Team";
    
    /**
     * Returns the name of the product currently running, determined
     * by the current license.
     * 
     * @param repoAdminService The RepoAdminService
     * @return "Share" or "Team"
     */
    public static String getProductName(RepoAdminService repoAdminService)
    {
        // the product name is never localised so it's safe to
        // return a hard-coded string but if we ever need to it's
        // centralised here.
        
        String productName = SHARE;
        
        if (repoAdminService != null && 
            repoAdminService.getRestrictions().getLicenseMode().equals(LicenseMode.TEAM))
        {
            productName = TEAM;
        }
        
        return productName;
    }
}
