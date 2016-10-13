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
package org.alfresco.rest.api.sites;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of an Entity Resource for a Site
 *
 * @author Gethin James
 * @author steveglover
 * @author janv
 */
@EntityResource(name="sites", title = "Sites")
public class SiteEntityResource implements EntityResourceAction.Read<Site>,
        EntityResourceAction.ReadById<Site>, EntityResourceAction.Delete,
        EntityResourceAction.Create<Site>, EntityResourceAction.Update<Site>, InitializingBean
{
    private Sites sites;

	public void setSites(Sites sites)
	{
		this.sites = sites;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("sites", this.sites);
    }

    /**
     * Returns a paged list of all sites in the current tenant.
     * 
     */
    @Override
    @WebApiDescription(title="A paged list of visible sites in the network.", description="A site is visible if it is public or if the person is a member")
    public CollectionWithPagingInfo<Site> readAll(Parameters parameters)
    { 
        return sites.getSites(parameters);
    }
    
    /**
     * Returns information regarding the site 'siteId'.
     * 
     */
    @Override
    @WebApiDescription(title="Returns site information for site siteId.")
    public Site readById(String siteId, Parameters parameters)
    {
        return sites.getSite(siteId);
    }

    /**
     * Delete the given site.
     *
     * @param siteId String id of site.
     */
    @Override
    @WebApiDescription(title = "Delete Site", description="Delete the site. This will cascade delete")
    public void delete(String siteId, Parameters parameters)
    {
        sites.deleteSite(siteId, parameters);
    }

    /**
     * Create the given site.
     *
     * @param entity
     * @param parameters
     * @return
     */
    @Override
    @WebApiDescription(title="Create site", description="Create the default/functional Share site")
    @WebApiParam(name="entity", title="A single site", description="A single site, multiple sites are not supported.",
            kind= ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false)
    public List<Site> create(List<Site> entity, Parameters parameters)
    {
        List<Site> result = new ArrayList<>(1);
        result.add(sites.createSite(entity.get(0), parameters));
        return result;
    }

    /**
     * Update the given site. Not all fields are used,
     * only those as defined in the Open API spec.
     *
     * @param siteId       The site ID (aka short name)
     * @param site         Details to use for the update
     * @param parameters
     * @return Updated Site
     */
    @Override
    @WebApiDescription(title="Update site", description="Update the Share site")
    public Site update(String siteId, Site site, Parameters parameters)
    {
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

        // Bind valid fields to a SiteUpdate instance.
        final String title = site.getTitle();
        final String description = site.getDescription();
        final SiteVisibility visibility = site.getVisibility();
        SiteUpdate update = new SiteUpdate(title, description, visibility);

        return sites.updateSite(siteId, update, parameters);
    }
}
