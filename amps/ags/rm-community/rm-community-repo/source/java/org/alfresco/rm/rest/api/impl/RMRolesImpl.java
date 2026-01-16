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
package org.alfresco.rm.rest.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.rm.rest.api.RMRoles;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.service.cmr.repository.NodeRef;

public class RMRolesImpl implements RMRoles
{
    private ApiNodesModelFactory nodesModelFactory;
    private FilePlanRoleService filePlanRoleService;

    private static final Set<String> LIST_ROLES_QUERY_PROPERTIES = new HashSet<>(List.of(PARAM_PERSON_ID, PARAM_INCLUDE_SYSTEM_ROLES, PARAM_CAPABILITY_NAME));

    @Override
    public CollectionWithPagingInfo<RoleModel> getRoles(NodeRef filePlan, Parameters parameters)
    {
        var rolesFilter = getRolesFilter(parameters.getQuery());
        var roles = getRolesByFilter(filePlan, rolesFilter);

        var filteredRoles = roles.stream()
                .map(role -> createRoleModel(filePlan, role, parameters.getInclude()))
                .filter(hasRoleCapabilities(rolesFilter.getCapabilities()))
                .toList();
        var page = filteredRoles
                .stream()
                .sorted(Comparator.comparing(RoleModel::name))
                .skip(parameters.getPaging().getSkipCount())
                .limit(parameters.getPaging().getMaxItems())
                .collect(Collectors.toCollection(LinkedList::new));

        int totalItems = filteredRoles.size();
        boolean hasMore = parameters.getPaging().getSkipCount() + parameters.getPaging().getMaxItems() < totalItems;
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, hasMore, totalItems);
    }

    private Predicate<RoleModel> hasRoleCapabilities(List<String> capabilities)
    {
        return role -> capabilities == null ||
                capabilities.isEmpty() ||
                role.capabilities().stream().anyMatch(capability -> capabilities.contains(capability.name()));
    }

    private Set<Role> getRolesByFilter(NodeRef filePlan, RolesFilter rolesFilter)
    {
        if (rolesFilter.getPersonId() != null)
        {
            return filePlanRoleService.getRolesByUser(filePlan, rolesFilter.getPersonId(), rolesFilter.includeSystemRoles());
        }
        else
        {
            return filePlanRoleService.getRoles(filePlan, rolesFilter.includeSystemRoles());
        }
    }

    private RoleModel createRoleModel(NodeRef filePlan, Role role, List<String> include)
    {
        List<String> assignedUsers = getAssignedUsers(filePlan, role, include);
        List<String> assignedGroups = getAssignedGroups(filePlan, role, include);

        return nodesModelFactory.createRoleModel(role, assignedUsers, assignedGroups);
    }

    private List<String> getAssignedUsers(NodeRef filePlan, Role role, List<String> include)
    {
        if (include != null && include.contains(PARAM_INCLUDE_ASSIGNED_USERS))
        {
            return new ArrayList<>(filePlanRoleService.getAllAssignedToRole(filePlan, role.getName()));
        }
        return null;
    }

    private List<String> getAssignedGroups(NodeRef filePlan, Role role, List<String> include)
    {
        if (include != null && include.contains(PARAM_INCLUDE_ASSIGNED_GROUPS))
        {
            return new ArrayList<>(filePlanRoleService.getGroupsAssignedToRole(filePlan, role.getName()));
        }
        return null;
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
                    .withCapabilities(propertyWalker.getCapabilitiesNames())
                    .withIncludeSystemRoles(propertyWalker.includeSystemRoles());
        }
        return rolesFilterBuilder.build();
    }

    private static class RolesQueryWalker extends MapBasedQueryWalker
    {
        private List<String> capabilitiesNames;

        public RolesQueryWalker()
        {
            super(LIST_ROLES_QUERY_PROPERTIES, null);
        }

        @Override
        public void in(String propertyName, boolean negated, String... propertyValues)
        {
            if (negated)
            {
                throw new InvalidArgumentException("Cannot use NOT for " + propertyName);
            }

            if (PARAM_CAPABILITY_NAME.equalsIgnoreCase(propertyName))
            {
                capabilitiesNames = Arrays.asList(propertyValues);
            }
        }

        @Override
        public void and()
        {
            // allow AND, e.g. personId='123' AND includeSystemRoles=true
        }

        public List<String> getCapabilitiesNames()
        {
            return this.capabilitiesNames;
        }

        public String getPersonId()
        {
            return getProperty(PARAM_PERSON_ID, WhereClauseParser.EQUALS, String.class);
        }

        public Boolean includeSystemRoles()
        {
            return getProperty(PARAM_INCLUDE_SYSTEM_ROLES, WhereClauseParser.EQUALS, Boolean.class);
        }
    }
}

class RolesFilter
{
    private String personId;
    private boolean includeSystemRoles;
    private List<String> capabilities;

    private RolesFilter()
    {}

    public static RolesFilterBuilder builder()
    {
        return new RolesFilterBuilder();
    }

    public String getPersonId()
    {
        return personId;
    }

    public boolean includeSystemRoles()
    {
        return includeSystemRoles;
    }

    public List<String> getCapabilities()
    {
        return capabilities;
    }

    public static class RolesFilterBuilder
    {
        private String personId;
        private boolean includeSystemRoles = true;
        private List<String> capabilities;

        public RolesFilterBuilder withPersonId(String personId)
        {
            this.personId = personId;
            return this;
        }

        public RolesFilterBuilder withIncludeSystemRoles(Boolean includeSystemRoles)
        {
            if (includeSystemRoles != null)
            {
                this.includeSystemRoles = includeSystemRoles;
            }
            return this;
        }

        public RolesFilterBuilder withCapabilities(List<String> capabilities)
        {
            this.capabilities = capabilities;
            return this;
        }

        public RolesFilter build()
        {
            RolesFilter rolesFilter = new RolesFilter();
            rolesFilter.personId = this.personId;
            rolesFilter.includeSystemRoles = this.includeSystemRoles;
            rolesFilter.capabilities = this.capabilities;
            return rolesFilter;
        }
    }
}
