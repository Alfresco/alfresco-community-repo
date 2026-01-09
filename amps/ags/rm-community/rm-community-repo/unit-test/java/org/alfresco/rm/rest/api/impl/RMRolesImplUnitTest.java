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

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rm.rest.api.model.CapabilityModel;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RMRolesImplUnitTest extends BaseUnitTest {

    private final RecognizedParamsExtractor queryExtractor = new RecognizedParamsExtractor() {};

    private RMRolesImpl rmRolesImpl;
    private FilePlanRoleService mockedFilePlanRoleService;
    private ApiNodesModelFactory mockedNodesModelFactory;

    private final Capability viewRecordsCapability = mock(Capability.class);
    private final Capability editMetadataCapability = mock(Capability.class);

    private final Role role1 = new Role("Role1", "Role 1", Set.of(viewRecordsCapability), "Group1");
    private final Role role2 = new Role("Role2", "Role 2", Set.of(editMetadataCapability), "Group2");

    private final RoleModel roleModel1 = new RoleModel("Role1", "Role 1", List.of(new CapabilityModel("ViewRecords", "", "", null, 0)), "Group1", null, List.of("User1"), List.of("Group1"));
    private final RoleModel roleModel2 = new RoleModel("Role2", "Role 2", List.of(new CapabilityModel("EditMetadata", "", "", null, 0)), "Group2", null, List.of("User2"), List.of("Group2"));

    private final NodeRef filePlan = new NodeRef("workspace://SpacesStore/testFilePlan");

    private final Parameters parameters = mock(Parameters.class);
    private final Paging paging = mock(Paging.class);

    @Before
    public void setUp() {
        mockedFilePlanRoleService = mock(FilePlanRoleService.class);
        mockedNodesModelFactory = mock(ApiNodesModelFactory.class);

        rmRolesImpl = new RMRolesImpl();
        rmRolesImpl.setFilePlanRoleService(mockedFilePlanRoleService);
        rmRolesImpl.setNodesModelFactory(mockedNodesModelFactory);

        when(mockedFilePlanRoleService.getRoles(filePlan, true)).thenReturn(Set.of(role1, role2));
        when(mockedNodesModelFactory.createRoleModel(eq(role1), any(), any())).thenReturn(roleModel1);
        when(mockedNodesModelFactory.createRoleModel(eq(role2), any(), any())).thenReturn(roleModel2);

        when(viewRecordsCapability.getName()).thenReturn("ViewRecords");
        when(editMetadataCapability.getName()).thenReturn("EditMetadata");

        when(parameters.getPaging()).thenReturn(paging);
        when(paging.getSkipCount()).thenReturn(0);
        when(paging.getMaxItems()).thenReturn(10);
    }

    @Test
    public void testGetRoles_NoFilters() {
        // when
        CollectionWithPagingInfo<RoleModel> result = rmRolesImpl.getRoles(filePlan, parameters);

        // then
        List<RoleModel> roleModelList = (List<RoleModel>) result.getCollection();
        assertEquals(2, (int) result.getTotalItems());
        assertEquals(List.of(roleModel1, roleModel2), roleModelList);
        verify(mockedFilePlanRoleService).getRoles(filePlan, true);
        verify(mockedNodesModelFactory).createRoleModel(eq(role1), any(), any());
        verify(mockedNodesModelFactory).createRoleModel(eq(role2), any(), any());
    }

    @Test
    public void testGetRoles_WithPersonId() {
        // given
        String personId = "testUser";
        when(mockedFilePlanRoleService.getRolesByUser(filePlan, personId, true)).thenReturn(Set.of(role1));
        when(parameters.getQuery()).thenReturn(queryExtractor.getWhereClause("(personId='" + personId + "')"));

        // when
        CollectionWithPagingInfo<RoleModel> result = rmRolesImpl.getRoles(filePlan, parameters);

        // then
        assertEquals(1, (int) result.getTotalItems());
        assertEquals(List.of(roleModel1), result.getCollection());
        verify(mockedFilePlanRoleService).getRolesByUser(filePlan, personId, true);
        verify(mockedNodesModelFactory).createRoleModel(eq(role1), any(), any());
    }

    @Test
    public void testGetNonSystemRoles() {
        //given
        when(mockedFilePlanRoleService.getRoles(filePlan, false)).thenReturn(Set.of(role2));
        when(parameters.getQuery()).thenReturn(queryExtractor.getWhereClause("(systemRoles=false)"));

        // when
        CollectionWithPagingInfo<RoleModel> result = rmRolesImpl.getRoles(filePlan, parameters);

        // then
        assertEquals(1, (int) result.getTotalItems());
        assertEquals(List.of(roleModel2), result.getCollection());
        verify(mockedFilePlanRoleService).getRoles(filePlan, false);
        verify(mockedNodesModelFactory).createRoleModel(eq(role2), any(), any());
    }

    @Test
    public void testGetRoles_WithCapabilitiesFilter() {
        // given
        when(parameters.getQuery()).thenReturn(queryExtractor.getWhereClause("(capabilityName IN ('ViewRecords'))"));

        // when
        CollectionWithPagingInfo<RoleModel> result = rmRolesImpl.getRoles(filePlan, parameters);

        // then
        assertEquals(1, (int) result.getTotalItems());
        assertEquals(List.of(roleModel1), result.getCollection());
        verify(mockedFilePlanRoleService).getRoles(filePlan, true);
        verify(mockedNodesModelFactory).createRoleModel(eq(role1), any(), any());
    }

    @Test
    public void testGetRoles_IncludeAssignedUsersAndGroups() {
        // given
        when(mockedFilePlanRoleService.getRoles(filePlan, true)).thenReturn(Set.of(role1));
        when(mockedFilePlanRoleService.getAllAssignedToRole(filePlan, "Role1")).thenReturn(Set.of("User1"));
        when(mockedFilePlanRoleService.getGroupsAssignedToRole(filePlan, "Role1")).thenReturn(Set.of("Group1"));

        when(parameters.getInclude()).thenReturn(List.of("assignedUsers", "assignedGroups"));

        // when
        CollectionWithPagingInfo<RoleModel> result = rmRolesImpl.getRoles(filePlan, parameters);

        // then
        List<RoleModel> roleModelList = (List<RoleModel>) result.getCollection();
        assertEquals(1, (int) result.getTotalItems());
        assertEquals(List.of(roleModel1), roleModelList);
        assertEquals(List.of("User1"), roleModelList.get(0).assignedUsers());
        assertEquals(List.of("Group1"), roleModelList.get(0).assignedGroups());
        verify(mockedFilePlanRoleService).getRoles(filePlan, true);
        verify(mockedFilePlanRoleService).getAllAssignedToRole(filePlan, "Role1");
        verify(mockedFilePlanRoleService).getGroupsAssignedToRole(filePlan, "Role1");
        verify(mockedNodesModelFactory).createRoleModel(role1, List.of("User1"), List.of("Group1"));
    }
}
