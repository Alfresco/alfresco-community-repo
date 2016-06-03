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
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
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
        EntityResourceAction.Create<Site>, InitializingBean
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
    public List<Site> create(List<Site> entity, Parameters parameters)
    {
        if (entity.size() != 1)
        {
            throw new InvalidArgumentException("Please specify one site entity only");
        }

        List<Site> result = new ArrayList<>(1);
        result.add(sites.createSite(entity.get(0), parameters));
        return result;
    }
}
