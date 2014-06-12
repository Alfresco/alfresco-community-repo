/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.legacy.service.HoldServiceImplTest;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Unit test for https://issues.alfresco.com/jira/browse/RM-1464
 *
 * @author Tuna Aksoy
 * @since 2.2
 * @version 1.0
 */
public class RM1464Test extends HoldServiceImplTest
{
    public void testAddRecordFolderToHoldWithoutFilingPermissionOnHold()
    {
        // Create hold
        final NodeRef hold = createAndCheckHold();

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run()
           {
               // Add the user to the RM Manager role
               filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_NAME_RECORDS_MANAGER, userName);

               // Give the user read permissions on the hold
               permissionService.setPermission(hold, userName, RMPermissionModel.READ_RECORDS, true);

               // Give the user filing permissions on the record folder
               permissionService.setPermission(rmFolder, userName, RMPermissionModel.FILING, true);

               return null;
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
