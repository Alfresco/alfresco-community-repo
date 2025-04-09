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
package org.alfresco.rest.api.quicksharelinks;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;

import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.QuickShareLinkEmailRequest;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiNoAuth;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.util.ParameterCheck;

/**
 * An implementation of an Entity Resource for Shared Links.
 *
 * @author janv
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "shared-links", title = "Shared Links")
public class QuickShareLinkEntityResource implements EntityResourceAction.ReadById<QuickShareLink>,
        BinaryResourceAction.Read,
        EntityResourceAction.Delete,
        EntityResourceAction.Create<QuickShareLink>,
        EntityResourceAction.Read<QuickShareLink>,
        InitializingBean
{
    private QuickShareLinks quickShareLinks;

    public void setQuickShareLinks(QuickShareLinks quickShareLinks)
    {
        this.quickShareLinks = quickShareLinks;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("quickShareLinks", this.quickShareLinks);
    }

    /**
     * Returns limited metadata regarding the shared (content) link.
     *
     * Note: does *not* require authenticated access for (public) shared link.
     */
    @Override
    @WebApiDescription(title = "Get shared link info", description = "Return limited metadata for shared link")
    @WebApiNoAuth
    public QuickShareLink readById(String sharedId, Parameters parameters)
    {
        return quickShareLinks.readById(sharedId, parameters);
    }

    /**
     * Download content via shared link.
     *
     * Note: does *not* require authenticated access for (public) shared link.
     *
     * @param sharedId
     * @param parameters
     *            {@link Parameters}
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    @WebApiDescription(title = "Download shared link content", description = "Download content for shared link")
    @WebApiNoAuth
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String sharedId, Parameters parameters) throws EntityNotFoundException
    {
        return quickShareLinks.readProperty(sharedId, null, parameters);
    }

    /**
     * Delete the shared link.
     *
     * Once deleted, the shared link will no longer exist hence get/download will no longer work (ie. return 404). If the link is later re-created then a new unique shared id will be generated.
     *
     * Requires authenticated access.
     *
     * @param sharedId
     *            String id of the quick share
     */
    @Override
    @WebApiDescription(title = "Delete shared link", description = "Delete the shared link")
    public void delete(String sharedId, Parameters parameters)
    {
        quickShareLinks.delete(sharedId, parameters);
    }

    /**
     * Create quick share.
     *
     * Requires authenticated access.
     *
     * @param nodeIds
     * @param parameters
     * @return
     */
    @Override
    @WebApiDescription(title = "Create shared link", description = "Create a new unique system-generated shared (link) id")
    public List<QuickShareLink> create(List<QuickShareLink> nodeIds, Parameters parameters)
    {
        return quickShareLinks.create(nodeIds, parameters);
    }

    @Operation("email")
    @WebApiDescription(title = "Email shared link", successStatus = Status.STATUS_ACCEPTED)
    public void email(String sharedId, QuickShareLinkEmailRequest emailRequest, Parameters parameters, WithResponse response)
    {
        quickShareLinks.emailSharedLink(sharedId, emailRequest, parameters);
    }

    /**
     * Find shared links
     *
     */
    @Override
    @WebApiDescription(title = "Find shared links", description = "Find ('search') & return result set of shared links")
    public CollectionWithPagingInfo<QuickShareLink> readAll(Parameters parameters)
    {
        return quickShareLinks.findLinks(parameters);
    }

}
