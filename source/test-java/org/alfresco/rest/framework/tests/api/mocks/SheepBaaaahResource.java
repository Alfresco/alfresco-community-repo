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

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Implements Get
 *
 * @author Gethin James
 */
@RelationshipResource(name = "baaahh", entityResource=SheepEntityResource.class, title = "Sheep baaah")
public class SheepBaaaahResource implements RelationshipResourceAction.Read<Sheep>, RelationshipResourceAction.ReadById<Sheep>
{

    @Override
    public Sheep readById(String entityResourceId, String id, Parameters parameters)
    {
        return new Sheep("Z2");
    }

    @Override
    public CollectionWithPagingInfo<Sheep> readAll(String entityResourceId, Parameters params)
    {
        List<Sheep> toReturn = Arrays.asList(new Sheep("D1"), new Sheep("Z2"), new Sheep("4X"));
        toReturn = toReturn.subList(0, params.getPaging().getMaxItems()>toReturn.size()?toReturn.size():params.getPaging().getMaxItems());
        return CollectionWithPagingInfo.asPaged(params.getPaging(),toReturn,toReturn.size()!=3 ,3);
    }

}
