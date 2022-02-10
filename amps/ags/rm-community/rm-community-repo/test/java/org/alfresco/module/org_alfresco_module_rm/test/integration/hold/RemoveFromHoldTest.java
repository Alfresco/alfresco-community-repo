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
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeRemoveFromHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnRemoveFromHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Remove From Hold integration tests.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RemoveFromHoldTest extends BaseRMTestCase implements BeforeRemoveFromHoldPolicy, OnRemoveFromHoldPolicy
{
    private static final int RECORD_COUNT = 10;

    private boolean beforeRemoveFromHoldFlag = false;
    private boolean onRemoveFromHoldFlag = false;

    public void testRemoveRecordsFromHold()
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

                // add records to hold
                holdService.addToHold(hold, records);
            }

            public void when() throws Exception
            {
                // remove *some* of the records
                holdService.removeFromHold(hold, records.subList(0, 5));
            }

            public void then()
            {
                // check record state (no longer held)   
                for (NodeRef record : records.subList(0, 5))
                {
                    assertFalse(freezeService.isFrozen(record));
                    assertFalse(holdService.getHeld(hold).contains(record));
                    assertFalse(holdService.heldBy(record, true).contains(hold));
                }

                // check record state (still held)   
                for (NodeRef record : records.subList(5, 10))
                {
                    assertTrue(freezeService.isFrozen(record));
                    assertTrue(holdService.getHeld(hold).contains(record));
                    assertTrue(holdService.heldBy(record, true).contains(hold));
                }

                // record folder has frozen children
                assertFalse(freezeService.isFrozen(recordFolder));
                assertTrue(freezeService.hasFrozenChildren(recordFolder));

                // record folder is not held
                assertFalse(holdService.getHeld(hold).contains(recordFolder));
                assertFalse(holdService.heldBy(recordFolder, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(5, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    public void testRemoveAllRecordsFromHold()
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

                // add records to hold
                holdService.addToHold(hold, records);
            }

            public void when() throws Exception
            {
                // remove all of the records
                holdService.removeFromHold(hold, records);
            }

            public void then()
            {
                // check record state (no longer held)   
                for (NodeRef record : records)
                {
                    assertFalse(freezeService.isFrozen(record));
                    assertFalse(holdService.getHeld(hold).contains(record));
                    assertFalse(holdService.heldBy(record, true).contains(hold));
                }

                // record folder has frozen children
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));

                // record folder is not held
                assertFalse(holdService.getHeld(hold).contains(recordFolder));
                assertFalse(holdService.heldBy(recordFolder, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    public void testRemoveRecordFolderFromHold()
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

                // add record folder to hold
                holdService.addToHold(hold, recordFolder);
            }

            public void when() throws Exception
            {
                // remove record folder from hold
                holdService.removeFromHold(hold, recordFolder);
            }

            public void then()
            {
                // check record states   
                for (NodeRef record : records)
                {
                    assertFalse(freezeService.isFrozen(record));
                    assertFalse(holdService.getHeld(hold).contains(record));
                    assertFalse(holdService.heldBy(record, true).contains(hold));
                }

                // record folder has frozen children
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));

                // record folder is not held
                assertFalse(holdService.getHeld(hold).contains(recordFolder));
                assertFalse(holdService.heldBy(recordFolder, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    public void testPolicyNotificationForRemoveFromHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef recordCategory;
            private NodeRef recordFolder;
            BehaviourDefinition<ClassBehaviourBinding> beforeRemoveFromHoldBehaviour;
            BehaviourDefinition<ClassBehaviourBinding> onRemoveFromHoldBehaviour;

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                // create a record category -> record folder
                recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                // add the record folder to hold
                holdService.addToHold(hold, recordFolder);

                beforeRemoveFromHoldBehaviour = policyComponent.bindClassBehaviour(BeforeRemoveFromHoldPolicy.QNAME,
                        RecordsManagementModel.TYPE_HOLD, new JavaBehaviour(RemoveFromHoldTest.this, "beforeRemoveFromHold", NotificationFrequency.EVERY_EVENT));

                onRemoveFromHoldBehaviour = policyComponent.bindClassBehaviour(OnRemoveFromHoldPolicy.QNAME,
                        RecordsManagementModel.TYPE_HOLD, new JavaBehaviour(RemoveFromHoldTest.this, "onRemoveFromHold", NotificationFrequency.EVERY_EVENT));

                assertFalse(beforeRemoveFromHoldFlag);
                assertFalse(onRemoveFromHoldFlag);
            }

            public void when() throws Exception
            {
                // remove the record folder from the hold
                holdService.removeFromHold(hold, recordFolder);
            }

            public void then()
            {
                assertTrue(beforeRemoveFromHoldFlag);
                assertTrue(onRemoveFromHoldFlag);
            }

            public void after()
            {
                policyComponent.removeClassDefinition(beforeRemoveFromHoldBehaviour);
                policyComponent.removeClassDefinition(onRemoveFromHoldBehaviour);
            }
        });
    }

    @Override
    public void beforeRemoveFromHold(NodeRef hold, NodeRef contentNodeRef)
    {
        beforeRemoveFromHoldFlag = true;
    }

    @Override
    public void onRemoveFromHold(NodeRef hold, NodeRef contentNodeRef)
    {
        onRemoveFromHoldFlag = true;
    }
}
