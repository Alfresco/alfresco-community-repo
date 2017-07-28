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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;

/**
 * Implements Get
 *
 * @author Gethin James
 */
@RelationshipResource(name = "baaahh", entityResource=SheepEntityResource.class, title = "Sheep baaah")
public class SheepBaaaahResource implements RelationshipResourceAction.Read<Sheep>, RelationshipResourceAction.ReadById<Sheep>, RelationshipResourceBinaryAction.Read, RelationshipResourceBinaryAction.Delete,RelationshipResourceBinaryAction.Update
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

    @Override
    @WebApiDescription(title = "Reads a photo")
    @BinaryProperties("photo")
    public BinaryResource readProperty(String entityId, String id, Parameters parameters) throws EntityNotFoundException
    {
        return null;
    }

    @Override
    @WebApiDescription(title = "Deletes a photo")
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, String entityResourceId, Parameters parameters)
    {

    }

    @Override
    @WebApiDescription(title = "Updates a photo")
    @BinaryProperties("photo")
    public Object updateProperty(String entityId, String entityResourceId, BasicContentInfo contentInfo, InputStream stream, Parameters params)
    {
        return null;
    }

    @Operation("chew")
    public String chewTheGrass(String entityId, String id, Void notused, Parameters parameters, WithResponse withResponse) {
        return "All done";
    }
}
