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

import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Unit test for RM-1030 .. can't freeze a record folder that already has a frozen record contained within
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM1030Test extends BaseRMTestCase
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    public void testRM1030() throws Exception
    {
        final NodeRef recordHold = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                Set<String> auths = filePlanRoleService.getAllAssignedToRole(filePlan, FilePlanRoleService.ROLE_ADMIN);
                for (String auth : auths)
                {
                    System.out.println(auth);
                }
                
                // show there are no holds when we start
                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(0, holds.size());

                // freeze record contained within the record folder
                NodeRef hold = holdService.createHold(filePlan, "my hold 2", "in true life for serious", "my decription");
                holdService.addToHold(hold, recordOne);
                assertNotNull(hold);

                return hold;
            }

            @Override
            public void test(NodeRef hold) throws Exception
            {
                // show the record is frozen
                assertTrue(freezeService.isFrozen(recordOne));

                // count the number of holds
                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(1, holds.size());
            }

        });

        final NodeRef recordFolderHold = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // freeze the record folder that contains the frozen record
                NodeRef folderHold = holdService.createHold(filePlan, "my hold 3", "innit but", "my decription");
                holdService.addToHold(folderHold, rmFolder);
                assertNotNull(folderHold);

                return folderHold;
            }

            @Override
            public void test(NodeRef hold) throws Exception
            {
             // show that the record and the record folder are frozen
                assertTrue(freezeService.isFrozen(recordOne));
                assertTrue(freezeService.isFrozen(rmFolder));

                // count the number of holds
                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(2, holds.size());
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // relinquish the record folder hold
                holdService.deleteHold(recordFolderHold);
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertTrue(freezeService.isFrozen(recordOne));
                assertFalse(freezeService.isFrozen(rmFolder));

                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(1, holds.size());
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // relinquish the record hold
                holdService.deleteHold(recordHold);
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertFalse(freezeService.isFrozen(recordOne));
                assertFalse(freezeService.isFrozen(rmFolder));

                List<NodeRef> holds = holdService.getHolds(filePlan);
                assertNotNull(holds);
                assertEquals(0, holds.size());
            }
        });

    }
}
