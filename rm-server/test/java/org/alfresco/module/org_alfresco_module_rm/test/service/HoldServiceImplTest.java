/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold service integration test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class HoldServiceImplTest extends BaseRMTestCase
{
    /** Constants for the holds */
    private static final String HOLD1_NAME = "hold one";
    private static final String HOLD2_NAME = "hold two";
    private static final String HOLD1_REASON = "I have my reasons";
    private static final String HOLD2_REASON = "secrets are everything";
    private static final String HOLD1_DESC = "but I'll not describe them here!";
    private static final String HOLD2_DESC = "no then! that's just not on!";

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * Creates a hold and checks if the hold is null or not
     *
     * @return {@link NodeRef} Node reference of the hold.
     */
    private NodeRef createAndCheckHold()
    {
        NodeRef hold = holdService.createHold(filePlan, HOLD1_NAME, HOLD1_REASON, HOLD1_DESC);
        assertNotNull(hold);
        return hold;
    }

    /**
     * Creates two holds and checks them if they are null or not
     *
     * @return List of {@link NodeRef}s of the holds.
     */
    private List<NodeRef> createAndCheckHolds()
    {
        List<NodeRef> holds = new ArrayList<NodeRef>(2);
        holds.add(createAndCheckHold());
        NodeRef hold2 = holdService.createHold(filePlan, HOLD2_NAME, HOLD2_REASON, HOLD2_DESC);
        assertNotNull(hold2);
        holds.add(hold2);
        assertEquals(2, holds.size());
        return holds;
    }

    public void testDeleteHoldBehaviourForRecordFolder()
    {
        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // create test holds
               NodeRef hold1 = createAndCheckHold();

               // add the record folder to hold1
               holdService.addToHold(hold1, rmFolder);

               // assert that the folder and records are frozen
               assertTrue(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertTrue(freezeService.isFrozen(recordDeclaredOne));

               // check the contents of the hold
               List<NodeRef> frozenNodes = holdService.getHeld(hold1);
               assertNotNull(frozenNodes);
               assertEquals(1, frozenNodes.size());
               assertEquals(rmFolder, frozenNodes.get(0));

               // delete the hold
               holdService.deleteHold(hold1);

               // assert that the folder and records no longer frozen
               assertFalse(freezeService.isFrozen(rmFolder));
               assertFalse(freezeService.isFrozen(recordOne));
               assertFalse(freezeService.isFrozen(recordDeclaredOne));

               // confirm the hold has been deleted
               assertNull(holdService.getHold(filePlan, "hold one"));

               return null;
           }
        });
    }

    public void testDeleteHoldBehaviourForMultipleHolds()
    {
        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // create test holds
               List<NodeRef> holds = createAndCheckHolds();
               NodeRef hold1 = holds.get(0);
               NodeRef hold2 = holds.get(1);

               // add the record folder to hold1
               holdService.addToHold(hold1, rmFolder);

               // assert that the folder and records are frozen
               assertTrue(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertTrue(freezeService.isFrozen(recordDeclaredOne));

               // check the contents of the hold
               List<NodeRef> frozenNodes = holdService.getHeld(hold1);
               assertNotNull(frozenNodes);
               assertEquals(1, frozenNodes.size());
               assertEquals(rmFolder, frozenNodes.get(0));

               holdService.addToHold(hold2, recordOne);

               // assert that the folder and records are frozen
               assertTrue(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertTrue(freezeService.isFrozen(recordDeclaredOne));

               // delete the hold
               holdService.deleteHold(hold1);

               // assert that the folder and records no longer frozen
               assertFalse(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertFalse(freezeService.isFrozen(recordDeclaredOne));

               // confirm the hold has been deleted
               assertNull(holdService.getHold(filePlan, "hold one"));

               // delete the hold
               holdService.deleteHold(hold2);

               // assert that the folder and records no longer frozen
               assertFalse(freezeService.isFrozen(rmFolder));
               assertFalse(freezeService.isFrozen(recordOne));
               assertFalse(freezeService.isFrozen(recordDeclaredOne));

               // confirm the hold has been deleted
               assertNull(holdService.getHold(filePlan, "hold two"));

               return null;
           }
        });
    }

    public void testAddRecordFolderToHoldWithoutFilingPermissionOnRecordFolder()
    {
        // Create hold
        final NodeRef hold = createAndCheckHold();

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // Add the user to the RM Manager role
               filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_NAME_RECORDS_MANAGER, userName);

               // Give the user filing permissions on the hold
               permissionService.setPermission(hold, userName, RMPermissionModel.FILING, true);

               // Give the user only read permissions on the record folder
               permissionService.setPermission(rmFolder, userName, RMPermissionModel.READ_RECORDS, true);

               return null;
           }
        });

        doTestInTransaction(new FailureTest(AlfrescoRuntimeException.class)
        {
            @Override
            public void run() throws Exception
            {
                holdService.addToHold(hold, rmFolder);
            }
        }, userName);
    }

    public void testAddRecordFolderToHoldWithoutFilingPermissionOnHold()
    {
        // Create hold
        final NodeRef hold = createAndCheckHold();

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
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
            public void run() throws Exception
            {
                holdService.addToHold(hold, rmFolder);
            }
        }, userName);
    }

    public void testGettingHolds()
    {
        final List<NodeRef> listWithTwoHolds = new ArrayList<NodeRef>(2);

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
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
               filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_NAME_RECORDS_MANAGER, userName);

               return null;
           }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
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
           public Void run() throws Exception
           {
               // Give the user read permissions on the hold
               permissionService.setPermission(hold2, userName, RMPermissionModel.FILING, true);

               return null;
           }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
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

    public void testHeldByNothing()
    {
        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // Create the test holds
               createAndCheckHolds();

               // Check that the record folder isn't held by anything
               List<NodeRef> holds = new ArrayList<NodeRef>();
               holds.addAll(holdService.heldBy(rmFolder, true));
               assertTrue(holds.isEmpty());
               holds.clear();
               holds.addAll(holdService.heldBy(rmFolder, false));
               assertEquals(2, holds.size());

               // Check that record isn't held by anything (recordOne is a child of the rmFolder)
               holds.clear();
               holds.addAll(holdService.heldBy(recordOne, true));
               assertTrue(holds.isEmpty());
               holds.clear();
               holds.addAll(holdService.heldBy(recordOne, false));
               assertEquals(2, holds.size());

               return null;
           }
        });
    }

    public void testDeleteHoldWithoutPermissionsOnChildren()
    {
        // Create the test hold
        final NodeRef hold = createAndCheckHold();

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // Add the user to the RM Manager role
               filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_NAME_RECORDS_MANAGER, userName);

               // Give the user filing permissions on the hold
               permissionService.setPermission(hold, userName, RMPermissionModel.FILING, true);

               // Give the user read permissions on the record folder
               permissionService.setPermission(rmFolder, userName, RMPermissionModel.READ_RECORDS, true);

               // Add record folder to the hold
               holdService.addToHold(hold, rmFolder);

               return null;
           }
        });

        doTestInTransaction(new FailureTest(AlfrescoRuntimeException.class)
        {
            @Override
            public void run() throws Exception
            {
                holdService.deleteHold(hold);
            }
        }, userName);
    }
}
