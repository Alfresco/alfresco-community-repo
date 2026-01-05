/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rm.rest.api;

import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * RM Roles API
 */
public interface RMRoles
{

    String PARAM_INCLUDE_ASSIGNED_USERS = "assignedUsers";
    String PARAM_INCLUDE_ASSIGNED_GROUPS = "assignedGroups";
    String PARAM_INCLUDE_SYSTEM_ROLES = "systemRoles";
    String PARAM_CAPABILITY_NAME = "capabilityName";
    String PARAM_PERSON_ID = "personId";

    /**
     * Gets a list of roles.
     *
     * @param filePlan
     *            the file plan node reference
     * @param parameters
     *            the {@link Parameters} object to get the parameters passed into the request including: - filter, sort & paging params (where, orderBy, skipCount, maxItems) - include param (personId, includeSystemRoles)
     * @return a paged list of {@code org.alfresco.rm.rest.api.model.RoleModel} objects
     */
    CollectionWithPagingInfo<RoleModel> getRoles(NodeRef filePlan, Parameters parameters);
}
