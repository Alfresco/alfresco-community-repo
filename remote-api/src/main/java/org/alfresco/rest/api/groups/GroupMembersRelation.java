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
import org.alfresco.rest.api.model.GroupMember;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;

/**
 * 
 * @author cturlica
 */
@RelationshipResource(name = "members", entityResource = GroupsEntityResource.class, title = "Group Members")
public class GroupMembersRelation
        implements RelationshipResourceAction.Read<GroupMember>, RelationshipResourceAction.Create<GroupMember>, RelationshipResourceAction.Delete, InitializingBean
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
    @WebApiDescription(title = "A paged list of all the members of the group 'groupId'.")
    public CollectionWithPagingInfo<GroupMember> readAll(String groupId, Parameters params)
    {
        return groups.getGroupMembers(groupId, params);
    }

    @Override
    @WebApiDescription(title = "Create group membership.")
    @WebApiParam(name = "entity", title = "A single group member", description = "A single group member, multiple groups members are not supported.", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple = false, required = true)
    public List<GroupMember> create(String groupId, List<GroupMember> entity, Parameters params)
    {
        List<GroupMember> result = new ArrayList<>(1);
        result.add(groups.createGroupMember(groupId, entity.get(0)));
        return result;
    }

    @Override
    @WebApiDescription(title = "Delete group membership")
    public void delete(String entityResourceId, String id, Parameters parameters)
    {
        groups.deleteGroupMembership(entityResourceId, id);
    }
}
