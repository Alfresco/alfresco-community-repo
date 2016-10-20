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

package org.alfresco.rm.rest.api.model;

import org.alfresco.rest.api.model.Site;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 *
 * @author Silviu Dinuta
 *
 */
public class RMSite extends Site
{
    private RMSiteCompliance compliance;

    public RMSiteCompliance getCompliance() {
        return compliance;
    }

    public void setCompliance(RMSiteCompliance compliance) {
        this.compliance = compliance;
    }

    public RMSite() {
        super();
    }

    public RMSite(Site site, RMSiteCompliance compliance)
    {
        this.id = site.getId();
        this.guid = site.getGuid();
        this.title = site.getTitle();
        this.description = site.getDescription();
        this.visibility = site.getVisibility();
        this.role = getRole();
        this.compliance = compliance;
    }

    public RMSite(SiteInfo siteInfo, String role, RMSiteCompliance compliance) {
        super(siteInfo, role);
        this.compliance = compliance;
    }
    @Override
    public String toString()
    {
        return "RMSite [id=" + id + ", guid=" + guid + ", title="
                + title + ", description=" + description + ", visibility="
                + visibility + ", role=" + role + ", compliance="+ compliance +"]";
    }

}
