/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.email;

import java.util.List;

/**
 * Bootstrap bean that indicates that the specified mappings are customisable
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class CustomisableEmailMappingKeyBootstrap
{
    /** List of mappings to register as customisable */
    private List<String> customisable;

    /** Custom email mapping service */
    private CustomEmailMappingService customEmailMappingService;

    /**
     * @param customizable  list of mappings to register as customisable
     */
    public void setCustomisable(List<String> customisable)
    {
        this.customisable = customisable;
    }

    /**
     * Custom email mapping service
     *
     * @param customEmailMappingService the custom email mapping service
     */
    public void setCustomEmailMappingService(CustomEmailMappingService customEmailMappingService)
    {
        this.customEmailMappingService = customEmailMappingService;
    }

    /**
     * Bean initialisation method
     */
    public void init()
    {
        for (String customEmailMappingKey : customisable)
        {
            customEmailMappingService.registerEMailMappingKey(customEmailMappingKey);
        }
    }
}
