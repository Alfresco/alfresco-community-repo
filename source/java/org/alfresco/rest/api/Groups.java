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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Group;
import org.alfresco.rest.api.model.GroupMember;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Groups API
 *
 * @author cturlica
 */
public interface Groups
{
    String PARAM_ID = "id";
    String PARAM_DISPLAY_NAME = "displayName";
    String PARAM_INCLUDE_PARENT_IDS = "parentIds";
    String PARAM_INCLUDE_ZONES = "zones";
    String PARAM_IS_ROOT = "isRoot";
    String PARAM_MEMBER_TYPE = "memberType";
    String PARAM_MEMBER_TYPE_GROUP = "GROUP";
    String PARAM_MEMBER_TYPE_PERSON = "PERSON";

    /**
     * Create a group.
     *
     * @param group the group to create.
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - include param (parentIds, zones)
     * @return a {@code org.alfresco.rest.api.model.Group} object
     */
    Group create(Group group, Parameters parameters);

    /**
     * Update the given group. Not all fields are used, only those as defined in
     * the Open API spec.
     *
     * @param groupId
     *            the group ID
     * @param group
     *            details to use for the update
     * @param parameters
     *            the {@link Parameters} object to get the parameters passed
     *            into the request including: - include param (parentIds, zones)
     * @return Updated group
     */
    Group update(String groupId, Group group, Parameters parameters);

    /**
     * Get a group by it's id.
     *
     * @param groupId the identifier of a group.
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - include param (parentIds, zones)
     * @return a {@code org.alfresco.rest.api.model.Group} object
     * @throws EntityNotFoundException
     */
    Group getGroup(String groupId, Parameters parameters) throws EntityNotFoundException;

    /**
     * Gets a list of groups.
     * 
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - include param (parentIds, zones)
     * @return a paged list of {@code org.alfresco.rest.api.model.Group} objects
     */
    CollectionWithPagingInfo<Group> getGroups(Parameters parameters);

    /**
     * Gets the list of groups for which the specified person is a member.
     *
     * @param personId the person's ID ("-me-" may be used as an alias for the current user.)
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - sort & paging params (orderBy, skipCount, maxItems)
     * @return a paged list of {@code org.alfresco.rest.api.model.Group} objects
     */
    CollectionWithPagingInfo<Group> getGroupsByPersonId(String personId, Parameters parameters);

    /**
     * Gets a list of groups.
     *
     * @param groupId the identifier of a group.
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - include param (parentIds, zones)
     * @return a paged list of {@code org.alfresco.rest.api.model.GroupMember} objects
     */
    CollectionWithPagingInfo<GroupMember> getGroupMembers(String groupId, Parameters parameters);
}
