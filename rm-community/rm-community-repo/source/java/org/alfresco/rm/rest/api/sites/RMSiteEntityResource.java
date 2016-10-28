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

package org.alfresco.rm.rest.api.sites;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMSites;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.SiteUpdate;
import org.alfresco.service.cmr.site.SiteVisibility;

/**
 * RM Site operations
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
@EntityResource(name = "sites", title = "IG Sites")
public class RMSiteEntityResource implements EntityResourceAction.Delete, EntityResourceAction.Create<RMSite>,
            EntityResourceAction.Update<RMSite>, EntityResourceAction.ReadById<RMSite>
{
    private static final String RM_SITE_ID = "rm";
    private RMSites sites;
    private String PARAM_PERMANENT = "permanent";

    public void setSites(RMSites sites)
    {
        this.sites = sites;
    }

    @Override
    public List<RMSite> create(List<RMSite> entity, Parameters parameters)
    {
        List<RMSite> result = new ArrayList<>(1);
        result.add(sites.createRMSite(entity.get(0), parameters));
        return result;
    }

    @Override
    public void delete(String siteId, Parameters parameters)
    {
        if (!RM_SITE_ID.equals(siteId))
        {
            throw new InvalidParameterException("The Deletion is supported only for siteId = rm.");
        }
        String permanentParameter = parameters.getParameter(PARAM_PERMANENT);
        if(permanentParameter != null)
        {
            throw new InvalidArgumentException("DELETE does not support parameter: permanent");
        }
        sites.deleteSite(siteId, parameters);
    }

    @Override
    public RMSite update(String siteId, RMSite site, Parameters parameters)
    {
        if (!RM_SITE_ID.equals(siteId))
        {
            throw new InvalidParameterException("The Update is supported only for siteId = rm.");
        }

        // Until REPO-110 is solved, we need to explicitly test for the presence of fields
        // on the Site object that aren't valid SiteUpdate fields. Once REPO-110 is solved,
        // the update method will take a SiteUpdate as a parameter rather than a Site
        // and only the correct fields will be exposed. Any attempt to access illegal fields
        // should then result in the framework returning a 400 automatically.
        if (site.getId() != null)
        {
            throw new InvalidArgumentException("Site update does not support field: id");
        }
        if (site.getGuid() != null)
        {
            throw new InvalidArgumentException("Site update does not support field: guid");
        }
        if (site.getRole() != null)
        {
            throw new InvalidArgumentException("Site update does not support field: role");
        }
        if (site.getCompliance() != null)
        {
            throw new InvalidArgumentException("Site update does not support field: compliance");
        }
        if (site.getVisibility() != null)
        {
            throw new InvalidArgumentException("Site update does not support field: visibility");
        }

        // Bind valid fields to a SiteUpdate instance.
        final String title = site.getTitle();
        final String description = site.getDescription();
        SiteUpdate update = new SiteUpdate(title, description, SiteVisibility.PUBLIC);

        return sites.updateRMSite(siteId, update, parameters);
    }

    @Override
    public RMSite readById(String siteId, Parameters parameters)
    {
        if (!RM_SITE_ID.equals(siteId))
        {
            throw new InvalidParameterException("GET is supported only for siteId = rm.");
        }
        return sites.getRMSite(siteId);
    }
}
