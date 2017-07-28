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

import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.Status;

/**
 * Implements Get
 * 
 * @author Gethin James
 */
@RelationshipResource(name = "blacksheep",entityResource=SheepEntityResource.class, title = "BlackSheep")
public class SheepBlackSheepResource implements RelationshipResourceAction.Read<Sheep>,
            RelationshipResourceAction.Update<Sheep>, RelationshipResourceAction.Delete,
            RelationshipResourceAction.Create<Sheep>
{


    @Override
    public CollectionWithPagingInfo<Sheep> readAll(String entityResourceId, Parameters params)
    {
        return CollectionWithPagingInfo.asPaged(params.getPaging(),Arrays.asList(new Sheep("D1"), new Sheep("Z2"), new Sheep("4X"), new Sheep("S4")));
    }

    @Override
    @WebApiDescription(title = "Deletes only black Sheep", successStatus = Status.STATUS_CONFLICT)
    public void delete(String entityResourceId, String id, Parameters parameters)
    {
    }

    @Override
    public Sheep update(String entityResourceId, Sheep entity, Parameters parameters)
    {
        return entity;
    }

    @Override
    @WebApiParam(name="entity", title="A single shepp", description="A single sheep, multiples are not supported.", 
    kind=ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false)
    public List<Sheep> create(String entityResourceId, List<Sheep> entity, Parameters parameters)
    {
        return entity;
    }

}
