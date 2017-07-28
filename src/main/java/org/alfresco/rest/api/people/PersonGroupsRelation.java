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
package org.alfresco.rest.api.people;

import org.alfresco.rest.api.Groups;
import org.alfresco.rest.api.model.Group;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Provide access to the groups of which a person is a member.
 * <p>
 * For example, list groups for person jbloggs.
 *
 * @author Matt Ward
 */
@RelationshipResource(
        name = "groups",
        entityResource = PeopleEntityResource.class,
        title = "Person Groups")
public class PersonGroupsRelation implements RelationshipResourceAction.Read<Group>, InitializingBean
{
    private Groups groups;

    public void setGroups(Groups groups)
    {
        this.groups = groups;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("groups", groups);
    }

    @Override
    public CollectionWithPagingInfo<Group> readAll(String personId, Parameters params)
    {
        return groups.getGroupsByPersonId(personId, params);
    }
}
