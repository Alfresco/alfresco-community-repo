/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link RecordsManagementPermissionPostProcessor}.
 *
 * @author David Webster
 * @since 2.4.1
 */
public class RecordsManagementPermissionPostProcessorUnitTest
{

    private @InjectMocks
    RecordsManagementPermissionPostProcessor recordsManagementPermissionPostProcessor = new RecordsManagementPermissionPostProcessor();

    private @Mock NodeService nodeService;
    private @Mock PermissionService permissionService;

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

        when(nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            .thenReturn(true);
        when(permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS))
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

        when(nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            .thenReturn(true);
        when(permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);

        AccessStatus result = recordsManagementPermissionPostProcessor.process(accessStatus, nodeRef, perm, configuredReadPermissions, configuredFilePermissions);

        assertEquals(AccessStatus.DENIED, result);
    }
}
