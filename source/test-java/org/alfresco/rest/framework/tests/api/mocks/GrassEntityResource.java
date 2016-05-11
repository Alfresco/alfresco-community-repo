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

import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.springframework.extensions.webscripts.Status;

import java.util.List;

@EntityResource(name = "grass", title="Grass")
public class GrassEntityResource implements EntityResourceAction.ReadById<Grass>, EntityResourceAction.Create<Grass>, EntityResourceAction.Delete {

    @Override
    @WebApiDescription(title = "Gets grass by id")
    @WebApiParam(name = "justone", title = "Only 1 param and its required.",required=true)
    public Grass readById(String id, Parameters parameters)
    {
        return new Grass(id);
    }

    @Operation("cut")
    @WebApiDescription(title = "Cut the grass",successStatus = Status.STATUS_NOT_IMPLEMENTED)
    public String cutLawn(String id, Void notused, Parameters parameters, WithResponse withResponse) {
        return "All done";
    }

    @Operation("grow")
    @WebApiDescription(title = "Grow the grass",successStatus = Status.STATUS_ACCEPTED)
    @WebApiParam(name = "Grass", title = "The grass.",required=true, kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    public String growTheLawn(String id, Grass grass, Parameters parameters, WithResponse withResponse) {
        return "Growing well";
    }

    @Override
    @WebApiDescription(title = "Create some grass")
    public List<Grass> create(List<Grass> entity, Parameters parameters)
    {
        return entity;
    }

    @Override
    public void delete(String id, Parameters parameters)
    {
        //I did a delete
    }
}
