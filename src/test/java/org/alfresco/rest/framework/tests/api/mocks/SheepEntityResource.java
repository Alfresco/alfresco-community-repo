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
package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.Status;

@EntityResource(name=SheepEntityResource.ENTITY_KEY,title="Sheep")
public class SheepEntityResource implements EntityResourceAction.Read<Sheep>,EntityResourceAction.ReadById<Sheep>, EntityResourceAction.Update<Sheep>, EntityResourceAction.Delete
{
    public static final String ENTITY_KEY = "sheep";

    @Override
    public void delete(String id, Parameters parameters)
    {
    }

    @Override
    public Sheep update(String id, Sheep entity, Parameters parameters)
    {
        return entity;
    }

    @Override
    public Sheep readById(String id, Parameters parameters)
    {
        return new Sheep(id);
    }

    @Override
    @WebApiDescription(title = "Gets all the Sheep", successStatus = Status.STATUS_ACCEPTED)
    @WebApiParameters({
                @WebApiParam(name = "siteId", title = "Site id", description="What ever."),
                @WebApiParam(name = "who", title = "Who", kind=ResourceParameter.KIND.HTTP_HEADER),
                @WebApiParam(name = "body", title = "aintnobody", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
                @WebApiParam(name = "requiredParam", title = "",required=true, kind=ResourceParameter.KIND.QUERY_STRING)})
    public CollectionWithPagingInfo<Sheep> readAll(Parameters params)
    {
        return CollectionWithPagingInfo.asPagedCollection(new Sheep("paged"));
    }

}
