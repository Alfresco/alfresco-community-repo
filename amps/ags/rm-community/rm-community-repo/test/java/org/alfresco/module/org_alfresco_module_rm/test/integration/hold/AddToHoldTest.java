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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeAddToHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnAddToHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Add To Hold Integration Tests
 *
 * @author Roy Wetherall
 * @since 2.2
 */

public class AddToHoldTest extends BaseRMTestCase implements BeforeAddToHoldPolicy, OnAddToHoldPolicy
{
    private static final int RECORD_COUNT = 10;

    private boolean beforeAddToHoldFlag = false;
    private boolean onAddToHoldFlag = false;

    public void testAddRecordToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef recordCategory;
            private NodeRef recordFolder;
            private NodeRef record;

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // create a record folder that contains records
                recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                record = recordService.createRecordFromContent(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT, null, null);

                // assert current states
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.isFrozen(record));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }

            public void when() throws Exception
            {
                // add the record to hold
                holdService.addToHold(hold, record);
            }

            public void then()
            {
                // record is held
                assertTrue(freezeService.isFrozen(record));

                // record folder has frozen children
                assertFalse(freezeService.isFrozen(recordFolder));
                assertTrue(freezeService.hasFrozenChildren(recordFolder));

                // record folder is not held
                assertFalse(holdService.getHeld(hold).contains(recordFolder));
                assertFalse(holdService.heldBy(recordFolder, true).contains(hold));

                // hold contains record
                assertTrue(holdService.getHeld(hold).contains(record));
                assertTrue(holdService.heldBy(record, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(1, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });

    }

    public void testAddRecordsToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef recordCategory;
            private NodeRef recordFolder;
            private List<NodeRef> records = new ArrayList<>(RECORD_COUNT);

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // create a record folder that contains records
                recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                for (int i = 0; i < RECORD_COUNT; i++)
                {
                    records.add(recordService.createRecordFromContent(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT, null, null));
                }

                // assert current states
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));
                for (NodeRef record : records)
                {
                    assertFalse(freezeService.isFrozen(record));
                }

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }

            public void when() throws Exception
            {
                // add the record to hold
                holdService.addToHold(hold, records);
            }

            public void then()
            {
                // record is held
                for (NodeRef record : records)
                {
                    assertTrue(freezeService.isFrozen(record));
                }

                // record folder has frozen children
                assertFalse(freezeService.isFrozen(recordFolder));
                assertTrue(freezeService.hasFrozenChildren(recordFolder));

                // record folder is not held
                assertFalse(holdService.getHeld(hold).contains(recordFolder));
                assertFalse(holdService.heldBy(recordFolder, true).contains(hold));

                for (NodeRef record : records)
                {
                    // hold contains record
                    assertTrue(holdService.getHeld(hold).contains(record));
                    assertTrue(holdService.heldBy(record, true).contains(hold));
                }

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(RECORD_COUNT, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    public void testAddRecordFolderToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef recordCategory;
            private NodeRef recordFolder;
            private List<NodeRef> records = new ArrayList<>(RECORD_COUNT);

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // create a record folder that contains records
                recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                for (int i = 0; i < RECORD_COUNT; i++)
                {
                    records.add(recordService.createRecordFromContent(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT, null, null));
                }

                // assert current states
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));
                for (NodeRef record : records)
                {
                    assertFalse(freezeService.isFrozen(record));
                }

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }

            public void when() throws Exception
            {
                // add the record to hold
                holdService.addToHold(hold, recordFolder);
            }

            public void then()
            {
                for (NodeRef record : records)
                {
                    // record is held
                    assertTrue(freezeService.isFrozen(record));
                    assertFalse(holdService.getHeld(hold).contains(record));
                    assertTrue(holdService.heldBy(record, true).contains(hold));
                }

                // record folder has frozen children
                assertTrue(freezeService.isFrozen(recordFolder));
                assertTrue(freezeService.hasFrozenChildren(recordFolder));

                // hold contains record folder
                assertTrue(holdService.getHeld(hold).contains(recordFolder));
                assertTrue(holdService.heldBy(recordFolder, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(RECORD_COUNT, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });

    }

    public void testPolicyNotificationForAddToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef recordCategory;
            private NodeRef recordFolder;
            BehaviourDefinition<ClassBehaviourBinding> beforeAddToHoldBehaviour;
            BehaviourDefinition<ClassBehaviourBinding> onAddToHoldBehaviour;

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                // create a record category -> record folder
                recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());

                beforeAddToHoldBehaviour = policyComponent.bindClassBehaviour(BeforeAddToHoldPolicy.QNAME,
                        RecordsManagementModel.TYPE_HOLD, new JavaBehaviour(AddToHoldTest.this, "beforeAddToHold", NotificationFrequency.EVERY_EVENT));

                onAddToHoldBehaviour = policyComponent.bindClassBehaviour(OnAddToHoldPolicy.QNAME,
                        RecordsManagementModel.TYPE_HOLD, new JavaBehaviour(AddToHoldTest.this, "onAddToHold", NotificationFrequency.EVERY_EVENT));

                assertFalse(beforeAddToHoldFlag);
                assertFalse(onAddToHoldFlag);
            }

            public void when() throws Exception
            {
                // add the record folder to hold
                holdService.addToHold(hold, recordFolder);
            }

            public void then()
            {
                assertTrue(beforeAddToHoldFlag);
                assertTrue(onAddToHoldFlag);
            }

            public void after()
            {
                policyComponent.removeClassDefinition(beforeAddToHoldBehaviour);
                policyComponent.removeClassDefinition(onAddToHoldBehaviour);
            }
        });
    }

    @Override
    public void beforeAddToHold(NodeRef hold, NodeRef contentNodeRef)
    {
        beforeAddToHoldFlag = true;
    }

    @Override
    public void onAddToHold(NodeRef hold, NodeRef contentNodeRef)
    {
        onAddToHoldFlag = true;
    }
}
