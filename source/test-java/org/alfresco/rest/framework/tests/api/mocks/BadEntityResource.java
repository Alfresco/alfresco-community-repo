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

import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.Status;

@EntityResource(name= BadEntityResource.ENTITY_KEY,title="bad resource that does bad things")
public class BadEntityResource implements EntityResourceAction.Read<Sheep>,EntityResourceAction.ReadById<Sheep>
{
    public static final String ENTITY_KEY = "bad";


    @Override
    public Sheep readById(String id, Parameters parameters)
    {
        throw new IntegrityException("bad integrity", null);
    }

    @Override
    @WebApiDescription(title = "Gets all the Sheep", successStatus = Status.STATUS_ACCEPTED)
    public CollectionWithPagingInfo<Sheep> readAll(Parameters params)
    {
        throw new RuntimeException("read all");
    }

}
