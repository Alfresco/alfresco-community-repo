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

package org.alfresco.module.org_alfresco_module_rm.test.integration.hold;

import static org.alfresco.util.GUID.generate;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeDeleteHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnDeleteHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold service integration test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class DeleteHoldTest extends BaseRMTestCase implements BeforeDeleteHoldPolicy, OnDeleteHoldPolicy
{
    /** Constants for the holds */
    protected static final String HOLD1_NAME = "hold one";
    protected static final String HOLD2_NAME = "hold two";
    protected static final String HOLD1_REASON = "I have my reasons";
    protected static final String HOLD2_REASON = "secrets are everything";
    protected static final String HOLD1_DESC = "but I'll not describe them here!";
    protected static final String HOLD2_DESC = "no then! that's just not on!";

    private boolean beforeDeleteHoldFlag = false;
    private boolean onDeleteHoldFlag = false;

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
    protected NodeRef createAndCheckHold()
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
    protected List<NodeRef> createAndCheckHolds()
    {
        List<NodeRef> holds = new ArrayList<>(2);
        holds.add(createAndCheckHold());
        NodeRef hold2 = holdService.createHold(filePlan, HOLD2_NAME, HOLD2_REASON, HOLD2_DESC);
        assertNotNull(hold2);
        holds.add(hold2);
        assertEquals(2, holds.size());
        return holds;
    }

    public void testDeleteHoldBehaviourForRecordFolder()
    {
        NodeRef hold1 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                // create test holds
                NodeRef newHold = createAndCheckHold();
                // add the record folder to hold1
                holdService.addToHold(newHold, rmFolder);

                return newHold;
            }
        });
        //Splitting transaction to fix onCreateNodePolicy issue where there was a node not found exception
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
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
        List<NodeRef> holds = doTestInTransaction(new Test<List<NodeRef>>()
        {
            @Override
            public List<NodeRef> run() throws Exception
            {
                // create test holds
                return createAndCheckHolds();
            }
        });
        //Splitting transaction to fix onCreateNodePolicy issue where there was a node not found exception
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
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

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                NodeRef hold1 = holds.get(0);
                NodeRef hold2 = holds.get(1);
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
               List<NodeRef> holds = new ArrayList<>();
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

    public void testPolicyNotificationForDeleteHold() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            BehaviourDefinition<ClassBehaviourBinding> beforeDeleteHoldBehaviour;
            BehaviourDefinition<ClassBehaviourBinding> onDeleteHoldBehaviour;
            NodeRef hold;

            public void given()
            {
                beforeDeleteHoldBehaviour = policyComponent.bindClassBehaviour(BeforeDeleteHoldPolicy.QNAME,
                            RecordsManagementModel.TYPE_HOLD,
                            new JavaBehaviour(DeleteHoldTest.this, "beforeDeleteHold", NotificationFrequency.EVERY_EVENT));

                onDeleteHoldBehaviour = policyComponent.bindClassBehaviour(OnDeleteHoldPolicy.QNAME, RecordsManagementModel.TYPE_HOLD,
                            new JavaBehaviour(DeleteHoldTest.this, "onDeleteHold", NotificationFrequency.EVERY_EVENT));

                // Create a hold
                hold = holdService.createHold(filePlan, generate(), generate(), generate());

                assertFalse(beforeDeleteHoldFlag);
                assertFalse(beforeDeleteHoldFlag);
            }

            public void when()
            {
                // Delete the hold
                holdService.deleteHold(hold);

            }

            public void then()
            {
                assertTrue(beforeDeleteHoldFlag);
                assertTrue(onDeleteHoldFlag);
            }

            public void after()
            {
                policyComponent.removeClassDefinition(beforeDeleteHoldBehaviour);
                policyComponent.removeClassDefinition(onDeleteHoldBehaviour);
            }
        });

    }

    @Override
    public void beforeDeleteHold(NodeRef hold)
    {
        beforeDeleteHoldFlag = true;
    }

    @Override
    public void onDeleteHold(String holdName)
    {
        onDeleteHoldFlag = true;
    }
}
