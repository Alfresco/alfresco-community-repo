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
package org.alfresco.rest.api.groups;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.rest.api.Groups;
import org.alfresco.rest.api.model.Group;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;

/**
 * An implementation of an Entity Resource for a Group
 * 
 * @author cturlica
 */
@EntityResource(name = "groups", title = "Groups")
public class GroupsEntityResource implements EntityResourceAction.Read<Group>, EntityResourceAction.ReadById<Group>, EntityResourceAction.Create<Group>,
        EntityResourceAction.Update<Group>, EntityResourceAction.Delete, InitializingBean
{
    private Groups groups;

    public void setGroups(Groups groups)
    {
        this.groups = groups;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("groups", this.groups);
    }

    @Override
    @WebApiDescription(title = "Get List of Groups", description = "Get List of Groups")
    public CollectionWithPagingInfo<Group> readAll(Parameters params)
    {
        return groups.getGroups(params);
    }

    @Override
    @WebApiDescription(title = "Returns group information for group id")
    public Group readById(String groupId, Parameters parameters) throws EntityNotFoundException
    {
        return groups.getGroup(groupId, parameters);
    }

    @Override
    @WebApiDescription(title = "Create group", description = "Create group")
    @WebApiParam(name = "entity", title = "A single group", description = "A single group, multiple groups are not supported.",
            kind = ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple = false, required = true)
    public List<Group> create(List<Group> entity, Parameters parameters)
    {
        List<Group> result = new ArrayList<>(1);
        result.add(groups.create(entity.get(0), parameters));
        return result;
    }

    @Override
    @WebApiDescription(title = "Update group", description = "Update group")
    public Group update(String groupId, Group group, Parameters parameters)
    {
        return groups.update(groupId, group, parameters);
    }

    @Override
    @WebApiDescription(title = "Delete group", description = "Delete group")
    public void delete(String groupId, Parameters parameters)
    {
        groups.delete(groupId, parameters);
    }
}
