/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.rest.api.sites;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteImpl;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter.KIND;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Site
 * 
 * @author Gethin James
 * @author steveglover
 */
@EntityResource(name = "sites", title = "Sites")
public class SiteEntityResource implements EntityResourceAction.Read<Site>, EntityResourceAction.ReadById<Site>,
            EntityResourceAction.Delete, EntityResourceAction.Update<SiteImpl>, InitializingBean
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
     * @see org.alfresco.rest.framework.resource.actions.interfaces.CollectionResourceAction.Get#get()
     */
    @Override
    @WebApiDescription(title = "A paged list of visible sites in the network.", description = "A site is visible if it is public or if the person is a member")
    @WebApiParam(name = "admin", title = "Admin", description = "An optional filter that when is set to true and the user has ‘site-admin’ permission, a paged list of all sites in the network will be returned.", kind = KIND.QUERY_STRING)
    public CollectionWithPagingInfo<Site> readAll(Parameters parameters)
    {
        return sites.getSites(parameters);
    }

    /**
     * Returns information regarding the site 'siteId'.
     */
    @Override
    @WebApiDescription(title = "Returns site information for site siteId.")
    public Site readById(String siteId, Parameters parameters)
    {
        return sites.getSite(siteId);
    }

    /**
     * PUT sites/{@literal <siteId>}
     * <p>
     * Updates the <i>visibility</i> of the site.
     */
    @Override
    @WebApiDescription(title = "Updates the visibility of the site.")
    public SiteImpl update(String siteId, SiteImpl entity, Parameters parameters)
    {
        return sites.updateSite(siteId, entity);
    }

    /**
     * Delete sites/{@literal <siteId>}
     * <p>
     * Deletes the site.
     */
    @Override
    @WebApiDescription(title = "Deletes the site.")
    public void delete(String siteId, Parameters parameters)
    {
        sites.deleteSite(siteId);
    }
}
