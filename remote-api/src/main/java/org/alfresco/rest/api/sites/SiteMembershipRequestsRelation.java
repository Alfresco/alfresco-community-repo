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
package org.alfresco.rest.api.sites;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.SiteMembershipRequests;
import org.alfresco.rest.api.model.SiteMembershipApproval;
import org.alfresco.rest.api.model.SiteMembershipRejection;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "site-membership-requests", entityResource = SiteEntityResource.class, title = "Site Membership Requests")
public class SiteMembershipRequestsRelation implements InitializingBean
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

    @Operation("approve")
    @WebApiParam(name = "siteMembershipApproval", title = "Site membership approval", description = "Site membership approval", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Approve a site membership request.", description = "Approve a site membership request.", successStatus = HttpServletResponse.SC_OK)
    public void approve(String siteId, String invitationId, SiteMembershipApproval siteMembershipApproval, Parameters parameters, WithResponse withResponse)
    {
        siteMembershipRequests.approveSiteMembershipRequest(siteId, invitationId, siteMembershipApproval);
    }

    @Operation("reject")
    @WebApiParam(name = "siteMembershipRejection", title = "Site membership rejection", description = "Site membership rejection", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Reject a site membership request.", description = "Reject a site membership request.", successStatus = HttpServletResponse.SC_OK)
    public void reject(String siteId, String invitationId, SiteMembershipRejection siteMembershipRejection, Parameters parameters, WithResponse withResponse)
    {
        siteMembershipRequests.rejectSiteMembershipRequest(siteId, invitationId, siteMembershipRejection);
    }
}
