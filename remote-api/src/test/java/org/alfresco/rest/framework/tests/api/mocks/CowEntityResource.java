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

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Status;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@EntityResource(name="cow", title="Cow")
public class CowEntityResource implements EntityResourceAction.ReadByIdWithResponse<Goat>,
                                            EntityResourceAction.ReadWithResponse<Goat>,
                                            EntityResourceAction.CreateWithResponse<Goat>,
                                            EntityResourceAction.UpdateWithResponse<Goat>,
                                            EntityResourceAction.DeleteWithResponse,
        BinaryResourceAction.ReadWithResponse,
        BinaryResourceAction.DeleteWithResponse,
        BinaryResourceAction.UpdateWithResponse<Goat>
{
    public final static Cache CACHE_COW = new Cache(new Description.RequiredCache()
    {
        @Override
        public boolean getNeverCache()
        {
            return false;
        }

        @Override
        public boolean getIsPublic()
        {
            return true;
        }

        @Override
        public boolean getMustRevalidate()
        {
            return false;
        }
    });

    @Override
    public Goat readById(String id, Parameters parameters, WithResponse withResponse)
    {
        withResponse.setCache(CACHE_COW);
        return new Goat("Goat"+id);
    }

    @Override
    public CollectionWithPagingInfo<Goat> readAll(Parameters params, WithResponse withResponse)
    {
        return CollectionWithPagingInfo.asPaged(params.getPaging(), Arrays.asList(new Goat("Cow1")));
    }

    @Override
    public List<Goat> create(List<Goat> entities, Parameters parameters, WithResponse withResponse)
    {
        withResponse.setStatus(Status.STATUS_ACCEPTED);
        return entities;
    }

    @Override
    public void delete(String id, Parameters parameters, WithResponse withResponse)
    {

    }

    @Override
    public Goat update(String id, Goat entity, Parameters parameters, WithResponse withResponse)
    {
        return entity;
    }

    
    @Override
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, Parameters parameters, WithResponse withResponse)
    {

    }

    @Override
    @BinaryProperties("photo")
    public BinaryResource readProperty(String entityId, Parameters parameters, WithResponse withResponse) throws EntityNotFoundException
    {
        return null;
    }

    @Override
    @BinaryProperties("photo")
    public Goat updateProperty(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters params, WithResponse withResponse)
    {
        return null;
    }
}
