/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.api.sitesmembershipsrequests;

import org.alfresco.rest.api.SiteMembershipRequests;
import org.alfresco.rest.api.model.SiteMembershipRequest;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Site Membership Request
 *
 */
@EntityResource(name = "site-membership-requests", title = "Site Membership Request")
public class SiteMembershipRequestEntityResource implements EntityResourceAction.Read<SiteMembershipRequest>, InitializingBean
{
    private SiteMembershipRequests siteMembershipRequests;

    public void setSiteMembershipRequests(SiteMembershipRequests siteMembershipRequests)
    {
        this.siteMembershipRequests = siteMembershipRequests;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("siteMembershipRequests", this.siteMembershipRequests);
    }

    /**
     * Returns a paged list of all site membership request the user can action.
     * 
     */
    @Override
    @WebApiDescription(title = "A paged list of visible site membership requests in the network.")
    public CollectionWithPagingInfo<SiteMembershipRequest> readAll(Parameters parameters)
    {
        return siteMembershipRequests.getPagedSiteMembershipRequests(parameters);
    }
}
