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

package org.alfresco.rest.api.aspects;

import org.alfresco.rest.api.Aspects;
import org.alfresco.rest.api.model.Aspect;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@EntityResource(name = "aspect", title = "Aspects")
public class AspectEntityResource implements EntityResourceAction.ReadById<Aspect>, EntityResourceAction.Read<Aspect>, InitializingBean
{

    private Aspects aspects;

    public void setAspects(Aspects aspects)
    {
        this.aspects = aspects;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("aspects", this.aspects);
    }

    @Override
    public CollectionWithPagingInfo<Aspect> readAll(Parameters params)
    {
        return aspects.listAspects(params);
    }

    @Override
    public Aspect readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        return aspects.listAspectById(id);
    }
}
