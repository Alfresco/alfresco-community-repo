/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.permission;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.model.PermissionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;

/**
 * Unit tests for {@link RecordsManagementPermissionPostProcessor}.
 *
 * @author David Webster
 * @author Tom Page
 * @since 2.4.1
 */
public class RecordsManagementPermissionPostProcessorUnitTest
{
    @InjectMocks
    private RecordsManagementPermissionPostProcessor recordsManagementPermissionPostProcessor = new RecordsManagementPermissionPostProcessor();

    @Mock
    private NodeService mockNodeService;
    @Mock
    private PermissionService mockPermissionService;
    @Mock
    private PermissionModel mockPermissionModel;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Given the configured permissions are set
     * When process is called
     * Then access is allowed
     */
    @Test
    public void configurePermissionsAllowed()
    {
        AccessStatus accessStatus = AccessStatus.DENIED;
        NodeRef nodeRef = new NodeRef("node://ref/");
        String perm = AlfMock.generateText();
        // permissions includes the perm created above
        List<String> configuredReadPermissions = asList("ReadProperties", "ReadChildren", perm);
        List<String> configuredFilePermissions = asList("WriteProperties", "AddChildren");

        when(mockNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            .thenReturn(true);
        when(mockPermissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);

        AccessStatus result = recordsManagementPermissionPostProcessor.process(accessStatus, nodeRef, perm, configuredReadPermissions, configuredFilePermissions);

        assertEquals(AccessStatus.ALLOWED, result);
    }

    /**
     * Given the configured permissions are not set
     * When process is called
     * Then access is denied
     */
    @Test
    public void configurePermissionsDenied()
    {
        AccessStatus accessStatus = AccessStatus.DENIED;
        NodeRef nodeRef = new NodeRef("node://ref/");
        String perm = AlfMock.generateText();
        // permissions do not include perm created above
        List<String> configuredReadPermissions = asList("ReadProperties", "ReadChildren");
        List<String> configuredFilePermissions = asList("WriteProperties", "AddChildren");

        when(mockNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            .thenReturn(true);
        when(mockPermissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);

        AccessStatus result = recordsManagementPermissionPostProcessor.process(accessStatus, nodeRef, perm, configuredReadPermissions, configuredFilePermissions);

        assertEquals(AccessStatus.DENIED, result);
    }

    /**
     * Test that the permission groups configured in the global properties file imply descendant permission groups.
     * <p>
     * Given a configured permission is an ancestor of another permission P
     * And the post processor checks if the user has P
     * Then the post processor says that they do.
     */
    @Test
    public void permissionInherittedFromConfiguredGroup()
    {
        NodeRef nodeRef = new NodeRef("node://ref/");
        // permissions do not include perm created above
        List<String> configuredReadPermissions = asList();
        List<String> configuredFilePermissions = asList("WriteProperties");

        when(mockNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            .thenReturn(true);
        when(mockPermissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);

        // Set up "WriteProperties" to imply three other permission groups.
        PermissionReference mockWritePropsPermRef = mock(PermissionReference.class);
        when(mockPermissionModel.getPermissionReference(null, "WriteProperties")).thenReturn(mockWritePropsPermRef);
        PermissionReference childOne = mock(PermissionReference.class);
        when(childOne.getName()).thenReturn("Not this one");
        PermissionReference childTwo = mock(PermissionReference.class);
        when(childTwo.getName()).thenReturn("This is the requested permission");
        PermissionReference childThree = mock(PermissionReference.class);
        when(childThree.getName()).thenReturn("Not this one either");
        when(mockPermissionModel.getGranteePermissions(mockWritePropsPermRef)).thenReturn(Sets.newHashSet(childOne, childTwo, childThree));

        // Call the method under test.
        AccessStatus result = recordsManagementPermissionPostProcessor.process(AccessStatus.DENIED, nodeRef,
                    "This is the requested permission", configuredReadPermissions, configuredFilePermissions);

        assertEquals(AccessStatus.ALLOWED, result);
    }
}
