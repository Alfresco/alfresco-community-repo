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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.integration.hold.DeleteHoldTest;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Unit test for https://issues.alfresco.com/jira/browse/RM-1424
 *
 * @author Tuna Aksoy
 * @since 2.2
 * @version 1.0
 */
public class RM1424Test extends DeleteHoldTest
{
    public void testGettingHolds()
    {
        final List<NodeRef> listWithTwoHolds = new ArrayList<>(2);

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run()
           {
               // No holds
               List<NodeRef> emptyHoldList = holdService.getHolds(filePlan);
               assertNotNull(emptyHoldList);
               assertTrue(emptyHoldList.isEmpty());

               // Create 2 holds
               createAndCheckHolds();

               // Check the list of holds
               listWithTwoHolds.addAll(holdService.getHolds(filePlan));
               assertNotNull(listWithTwoHolds);
               assertEquals(2, listWithTwoHolds.size());

               // Check the first hold
               NodeRef hold1 = listWithTwoHolds.get(0);
               assertEquals(RecordsManagementModel.TYPE_HOLD, nodeService.getType(hold1));
               assertEquals(HOLD1_NAME, (String) nodeService.getProperty(hold1, PROP_NAME));
               assertEquals(HOLD1_REASON, (String) nodeService.getProperty(hold1, PROP_HOLD_REASON));
               assertEquals(HOLD1_DESC, (String) nodeService.getProperty(hold1, PROP_DESCRIPTION));

               // Add the user to the RM Manager role
               filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, userName);

               return null;
           }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Get the holds the test user without having any permissions on the holds
                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(0, holds.size());

                return null;
            }
        }, userName);

        final NodeRef hold2 = listWithTwoHolds.get(1);
        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run()
           {
               // Give the user read permissions on the hold
               permissionService.setPermission(hold2, userName, RMPermissionModel.FILING, true);

               return null;
           }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(1, holds.size());
                assertEquals(RecordsManagementModel.TYPE_HOLD, nodeService.getType(hold2));
                assertEquals(HOLD2_NAME, (String) nodeService.getProperty(hold2, PROP_NAME));
                assertEquals(HOLD2_REASON, (String) nodeService.getProperty(hold2, PROP_HOLD_REASON));
                assertEquals(HOLD2_DESC, (String) nodeService.getProperty(hold2, PROP_DESCRIPTION));

                return null;
            }
        }, userName);
    }
}
