/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.framework.core;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Extends ResourceMetaData to give more information about an Operation
 *
 * @author Gethin James
 */
public class OperationResourceMetaData extends ResourceMetadata
{
    private final Method operationMethod;
    private final boolean noAuthRequired;

    /**
     * Use this constructor to create the resource metadata
     * @param uniqueId
     * @param operations
     * @param api
     * @param operationMethod
     * @param noAuthRequired
     */
    public OperationResourceMetaData(String uniqueId, List<ResourceOperation> operations, Api api, Method operationMethod, boolean noAuthRequired)
    {
        super(uniqueId, RESOURCE_TYPE.OPERATION, operations, api, null, null, null);
        if (operations.size()!= 1)
        {
            throw new IllegalArgumentException("Only 1 operation per url is supported for an entity");
        }
        this.operationMethod = operationMethod;
        this.noAuthRequired = noAuthRequired;
    }

    /**
     * Constructor to use when it has been deleted
     * @param uniqueId
     * @param api
     * @param apiDeleted
     * @param noAuthRequired
     */
    public OperationResourceMetaData(String uniqueId, Api api, Set<Class<? extends ResourceAction>> apiDeleted, boolean noAuthRequired)
    {
        super(uniqueId, RESOURCE_TYPE.OPERATION, null, api, apiDeleted, null, null);
        this.operationMethod = null;
        this.noAuthRequired = noAuthRequired;
    }

    public Method getOperationMethod()
    {
        return operationMethod;
    }

    @Override
    public boolean isNoAuth(Class<? extends ResourceAction> resourceAction)
    {
        return this.noAuthRequired;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("OperationResourceMetaData [api=");
        builder.append(this.getApi());
        builder.append(", uniqueId=");
        builder.append(this.getUniqueId());
        builder.append(", type=");
        builder.append(this.getType());
        builder.append(", parent=");
        builder.append(this.getParentResource());
        builder.append(", operations=");
        builder.append(this.getOperations());
        builder.append(", apiDeleted=");
        builder.append(this.getApiDeleted());
        builder.append(", operationMethod=").append(operationMethod);
        builder.append(", noAuthRequired=").append(noAuthRequired);
        builder.append("]");
        return builder.toString();
    }
}
