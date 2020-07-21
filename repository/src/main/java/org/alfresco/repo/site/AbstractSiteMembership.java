/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.site;

import org.alfresco.service.cmr.site.SiteInfo;

public class AbstractSiteMembership
{
    protected final SiteInfo siteInfo;
    protected final String id;  // contains both userId and authority Id
    protected final String role;

    public AbstractSiteMembership(SiteInfo siteInfo, String id, String role)
    {
        if (siteInfo == null)
        {
            throw new java.lang.IllegalArgumentException();
        }
        if (id == null)
        {
            throw new java.lang.IllegalArgumentException("Id required building site membership");
        }
        if (role == null)
        {
            throw new java.lang.IllegalArgumentException("Role required building site membership");
        }
        this.siteInfo = siteInfo;
        this.id = id;
        this.role = role;
    }

    public String getId()
    {
        return id;
    }

    public SiteInfo getSiteInfo()
    {
        return siteInfo;
    }

    public String getRole()
    {
        return role;
    }
}
