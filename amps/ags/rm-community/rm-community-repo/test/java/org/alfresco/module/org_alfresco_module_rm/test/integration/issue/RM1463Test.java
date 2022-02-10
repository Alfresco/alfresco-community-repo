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

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.integration.hold.DeleteHoldTest;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Unit test for https://issues.alfresco.com/jira/browse/RM-1463
 *
 * @author Tuna Aksoy
 * @since 2.2
 * @version 1.0
 */
public class RM1463Test extends DeleteHoldTest
{
    public void testAddRecordFolderToHoldWithoutFilingPermissionOnRecordFolder()
    {
        final NodeRef hold = doTestInTransaction(new Test<NodeRef>()
        {
           @Override
           public NodeRef run()
           {
               // Create hold
               NodeRef hold = createAndCheckHold();
               
               // Add the user to the RM Manager role
               filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, userName);

               // Give the user filing permissions on the hold
               permissionService.setPermission(hold, userName, RMPermissionModel.FILING, true);

               // Give the user only read permissions on the record folder
               permissionService.setPermission(rmFolder, userName, RMPermissionModel.READ_RECORDS, true);

               return hold;
           }
        });

        doTestInTransaction(new FailureTest(AlfrescoRuntimeException.class)
        {
            @Override
            public void run()
            {
                holdService.addToHold(hold, rmFolder);
            }
        }, userName);
    }
}
