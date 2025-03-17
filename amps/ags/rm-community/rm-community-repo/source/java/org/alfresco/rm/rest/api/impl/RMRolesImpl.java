/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.rm.rest.api.impl;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.rm.rest.api.RMRoles;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RMRolesImpl implements RMRoles
{
    private ApiNodesModelFactory nodesModelFactory;
    private FilePlanRoleService filePlanRoleService;

    private final static Set<String> LIST_ROLES_EQUALS_QUERY_PROPERTIES = new HashSet<>(List.of(PARAM_PERSON_ID, PARAM_INCLUDE_SYSTEM_ROLES));

    @Override
    public CollectionWithPagingInfo<RoleModel> getRoles(NodeRef filePlan, Parameters parameters)
    {
        RolesFilter rolesFilter = getRolesFilter(parameters.getQuery());

        Set<Role> roles = null;
        if (rolesFilter.getPersonId() != null)
        {
            roles = filePlanRoleService.getRolesByUser(filePlan, rolesFilter.getPersonId(), rolesFilter.getIncludeSystemRoles());
        }
        else
        {
            roles = filePlanRoleService.getRoles(filePlan, rolesFilter.getIncludeSystemRoles());
        }

        var page = roles.stream()
                .map(nodesModelFactory::createRoleModel)
                .sorted()
                .skip(parameters.getPaging().getSkipCount())
                .limit(parameters.getPaging().getMaxItems())
                .collect(Collectors.toCollection(LinkedList::new));

        int totalItems = roles.size();
        boolean hasMore = parameters.getPaging().getSkipCount() + parameters.getPaging().getMaxItems() < totalItems;
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, hasMore, totalItems);
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    private RolesFilter getRolesFilter(Query queryParameters)
    {
        var rolesFilterBuilder = RolesFilter.builder();

        if (queryParameters != null)
        {
            var propertyWalker = new RolesQueryWalker();
            QueryHelper.walk(queryParameters, propertyWalker);

            rolesFilterBuilder
                    .withPersonId(propertyWalker.getPersonId())
                    .withIncludeSystemRoles(propertyWalker.getIncludeSystemRoles());
        }
        return rolesFilterBuilder.build();
    }

    private static class RolesQueryWalker extends MapBasedQueryWalker
    {
        public RolesQueryWalker()
        {
            super(LIST_ROLES_EQUALS_QUERY_PROPERTIES, null);
        }

        @Override
        public void and()
        {
            // allow AND, e.g. personId='123' AND includeSystemRoles=true
        }

        public String getPersonId()
        {
            return getProperty(PARAM_PERSON_ID, WhereClauseParser.EQUALS, String.class);
        }

        public Boolean getIncludeSystemRoles()
        {
            return getProperty(PARAM_INCLUDE_SYSTEM_ROLES, WhereClauseParser.EQUALS, Boolean.class);
        }
    }
}

class RolesFilter
{
    private String personId;
    private Boolean includeSystemRoles;

    private RolesFilter()
    {
    }

    public static RolesFilterBuilder builder()
    {
        return new RolesFilterBuilder();
    }

    public String getPersonId()
    {
        return personId;
    }

    public Boolean getIncludeSystemRoles()
    {
        return includeSystemRoles;
    }

    public static class RolesFilterBuilder
    {
        private String personId;
        private Boolean includeSystemRoles;

        public RolesFilterBuilder withPersonId(String personId)
        {
            this.personId = personId;
            return this;
        }

        public RolesFilterBuilder withIncludeSystemRoles(Boolean includeSystemRoles)
        {
            this.includeSystemRoles = includeSystemRoles;
            return this;
        }

        public RolesFilter build()
        {
            RolesFilter rolesFilter = new RolesFilter();
            rolesFilter.personId = this.personId;
            rolesFilter.includeSystemRoles = this.includeSystemRoles;
            return rolesFilter;
        }
    }
}
