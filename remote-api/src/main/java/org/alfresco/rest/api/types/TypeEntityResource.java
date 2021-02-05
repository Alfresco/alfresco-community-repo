/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.types;

import org.alfresco.rest.api.Types;
import org.alfresco.rest.api.model.Type;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@EntityResource(name = "types", title = "types")
public class TypeEntityResource implements EntityResourceAction.ReadById<Type>, EntityResourceAction.Read<Type>, InitializingBean
{

    private Types types;

    public void setTypes(Types types)
    {
        this.types = types;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("types", this.types);
    }

    @Override
    public CollectionWithPagingInfo<Type> readAll(Parameters params)
    {
        return types.listTypes(params);
    }

    @Override
    public Type readById(String id, Parameters parameters)
    {
        return types.getType(id);
    }
}
