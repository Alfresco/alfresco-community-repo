/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.site.SiteInfo;

/**
 * Represents a user's favourite site.
 * 
 * Represented by a separate class in order to allow other attributes to be added.
 * 
 * @author steveglover
 *
 */
public class FavouriteSite extends Site
{
    public FavouriteSite()
    {}

    public FavouriteSite(SiteInfo siteInfo, String role)
    {
        super(siteInfo, role);
    }

    @Override
    public String toString()
    {
        return "FavouriteSite [id=" + id + ", guid=" + guid + ", title="
                + title + ", description=" + description + ", visibility="
                + visibility + ", role=" + role + "]";
    }
}
