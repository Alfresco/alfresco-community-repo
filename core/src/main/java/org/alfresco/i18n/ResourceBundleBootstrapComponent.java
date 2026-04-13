/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.i18n;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Resource bundle bootstrap component.
 * <p>
 * Provides a convenient way to make resource bundles available via Spring config.
 * 
 * @author Roy Wetherall
 */
public class ResourceBundleBootstrapComponent
{
    /**
     * Set the resource bundles to be registered.  This should be a list of resource
     * bundle base names whose content will be made available across the repository.
     * 
     * @param resourceBundles   the resource bundles
     */
    public void setResourceBundles(List<String> resourceBundles)
    {
        for (String resourceBundle : resourceBundles)
        {
            I18NUtil.registerResourceBundle(resourceBundle);
        }
    }
}
