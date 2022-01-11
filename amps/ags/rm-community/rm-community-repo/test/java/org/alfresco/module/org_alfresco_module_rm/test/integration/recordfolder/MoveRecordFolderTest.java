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

package org.alfresco.module.org_alfresco_module_rm.test.integration.recordfolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.GUID;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Move record folder tests.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class MoveRecordFolderTest extends BaseRMTestCase
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    /**
     * Given two categories, both with cut off immediately schedules, when the record is move then all the parts of the
     * record should be correct based on the new schedule.
     *
     * @see https://issues.alfresco.com/jira/browse/RM-1345
     */
    public void testMoveRecordFolderBeforeCutOffFolderLevelDisposition() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(null, false)
        {
            NodeRef recordFolder;
            NodeRef destinationRecordCategory;

            public void given()
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        NodeRef rcOne = createRecordCategory(false);
                        destinationRecordCategory = createRecordCategory(false);
                        recordFolder = recordFolderService.createRecordFolder(rcOne, GUID.generate());

                        // check for the lifecycle aspect
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));

                        // check the disposition action details
                        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                        assertNotNull(dispositionAction);
                        assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                        assertNotNull(dispositionAction.getAsOfDate());
                        assertTrue(dispositionService.isNextDispositionActionEligible(recordFolder));
                    }
                });

                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // check the search aspect properties
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_RM_SEARCH));
                        assertEquals(CutOffAction.NAME,
                                    nodeService.getProperty(recordFolder, PROP_RS_DISPOSITION_ACTION_NAME));
                        assertNotNull(nodeService.getProperty(recordFolder, PROP_RS_DISPOSITION_ACTION_AS_OF));
                    }
                });
            }

            public void when() throws Exception
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // move record folder
                        fileFolderService.move(recordFolder, destinationRecordCategory, GUID.generate());
                    }
                });
            }

            public void then()
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // check for the lifecycle aspect
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));

                        // check the disposition action details
                        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                        assertNotNull(dispositionAction);
                        assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                        assertNotNull(dispositionAction.getAsOfDate());
                        assertTrue(dispositionService.isNextDispositionActionEligible(recordFolder));

                        // check the search aspect properties
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_RM_SEARCH));
                        assertEquals(CutOffAction.NAME,
                                    nodeService.getProperty(recordFolder, PROP_RS_DISPOSITION_ACTION_NAME));
                        assertNotNull(nodeService.getProperty(recordFolder, PROP_RS_DISPOSITION_ACTION_AS_OF));
                    }
                });
            }
        });
    }

    /**
     *
     */
    public void testMoveRecordFolderBeforeCutOffIntoAFolderWithNoDisposition() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(null, false)
        {
            NodeRef recordFolder;
            NodeRef destinationRecordCategory;

            public void given()
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        NodeRef rcOne = createRecordCategory(false);
                        destinationRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                        recordFolder = recordFolderService.createRecordFolder(rcOne, GUID.generate());

                        // check for the lifecycle aspect
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));

                        // check the disposition action details
                        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                        assertNotNull(dispositionAction);
                        assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                        assertNotNull(dispositionAction.getAsOfDate());
                        assertTrue(dispositionService.isNextDispositionActionEligible(recordFolder));
                    }
                });

                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // check the search aspect properties
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_RM_SEARCH));
                        assertEquals(CutOffAction.NAME,
                                    nodeService.getProperty(recordFolder, PROP_RS_DISPOSITION_ACTION_NAME));
                        assertNotNull(nodeService.getProperty(recordFolder, PROP_RS_DISPOSITION_ACTION_AS_OF));
                    }
                });
            }

            public void when() throws Exception
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // move record folder
                        fileFolderService.move(recordFolder, destinationRecordCategory, GUID.generate());
                    }
                });
            }

            public void then()
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // check for the lifecycle aspect
                        assertFalse(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));

                        // check the disposition action details
                        assertNull(dispositionService.getNextDispositionAction(recordFolder));

                        // check the search aspect properties
                        assertTrue(nodeService.hasAspect(recordFolder, ASPECT_RM_SEARCH));
                    }
                });
            }
        });
    }

    /**
     *
     */
    public void testMoveRecordFolderWithRecordsBeforeCutOffRecordLevelDisposition() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(null, false)
        {
            NodeRef record;
            NodeRef recordFolder;
            NodeRef destinationRecordCategory;

            public void given()
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        NodeRef rcOne = createRecordCategory(true);
                        destinationRecordCategory = createRecordCategory(true);
                        recordFolder = recordFolderService.createRecordFolder(rcOne, GUID.generate());
                        record = utils.createRecord(recordFolder, GUID.generate());

                        // check for the lifecycle aspect
                        assertFalse(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                        assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));

                        // check the disposition action details
                        assertNull(dispositionService.getNextDispositionAction(recordFolder));
                        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                        assertNotNull(dispositionAction);
                        assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                        assertNotNull(dispositionAction.getAsOfDate());
                        assertTrue(dispositionService.isNextDispositionActionEligible(record));
                    }
                });

                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // check the search aspect properties
                        assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                        assertEquals(CutOffAction.NAME,
                                    nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                        assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                    }
                });
            }

            public void when() throws Exception
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // move record folder
                        fileFolderService.move(recordFolder, destinationRecordCategory, GUID.generate());
                    }
                });
            }

            public void then()
            {
                doTestInTransaction(new VoidTest()
                {
                    public void runImpl() throws Exception
                    {
                        // check for the lifecycle aspect
                        assertFalse(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                        assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));

                        // check the disposition action details
                        assertNull(dispositionService.getNextDispositionAction(recordFolder));
                        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                        assertNotNull(dispositionAction);
                        assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                        assertNotNull(dispositionAction.getAsOfDate());
                        assertTrue(dispositionService.isNextDispositionActionEligible(record));

                        // check the search aspect properties
                        assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                        assertEquals(CutOffAction.NAME,
                                    nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                        assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                    }
                });
            }
        });
    }

    /**
     * Try and move a folder from no disposition schedule to a disposition schedule
     *
     * @see https://issues.alfresco.com/jira/browse/RM-1039
     */
    public void testMoveRecordFolderFromNoDisToDis() throws Exception
    {
        final NodeRef recordFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a record category (no disposition schedule)
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, "Caitlin Reed");

                // create a record folder
                return recordFolderService.createRecordFolder(recordCategory, "Grace Wetherall");
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertNotNull(result);
                assertNull(dispositionService.getDispositionSchedule(result));
                assertFalse(nodeService.hasAspect(result, ASPECT_DISPOSITION_LIFECYCLE));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(result, FILING));
            }
        });

        final NodeRef record = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a record
                return fileFolderService.create(recordFolder, "mytest.txt", ContentModel.TYPE_CONTENT).getNodeRef();
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertNotNull(result);
                assertNull(dispositionService.getDispositionSchedule(result));
                assertFalse(nodeService.hasAspect(result, ASPECT_DISPOSITION_LIFECYCLE));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(result, FILING));
            }
        });

        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                Capability capability = capabilityService.getCapability("CreateModifyDestroyFolders");
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(recordFolder));
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(recordFolder, rmContainer));

                // take a look at the move capability
                Capability moveCapability = capabilityService.getCapability("Move");
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, moveCapability.evaluate(recordFolder, rmContainer));

                // move the node
                return fileFolderService.move(recordFolder, rmContainer, null).getNodeRef();
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertNotNull(result);
                assertNotNull(dispositionService.getDispositionSchedule(result));
                assertTrue(nodeService.hasAspect(result, ASPECT_DISPOSITION_LIFECYCLE));

                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(result);
                assertNotNull(dispositionAction);

                assertNull(dispositionAction.getAsOfDate());
                assertEquals("cutoff", dispositionAction.getName());
                assertEquals(1, dispositionAction.getEventCompletionDetails().size());

                // take a look at the record and check things are as we would expect
                assertFalse(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
            }
        });
    }
    
    /**
     * Try and move a cutoff folder
     * 
     * @see https://issues.alfresco.com/jira/browse/RM-1039
     */
    public void testMoveCutoffRecordFolder() throws Exception
    {
        final NodeRef destination = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a record category (no disposition schedule)
                return filePlanService.createRecordCategory(filePlan, "Caitlin Reed");
            }
        });

        final NodeRef testFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create folder
                NodeRef testFolder = recordFolderService.createRecordFolder(rmContainer, "Peter Edward Francis");

                // complete event
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(testFolder, CompleteEventAction.NAME, params);

                // cutoff folder
                rmActionService.executeRecordsManagementAction(testFolder, CutOffAction.NAME);

                return testFolder;
            }

            @Override
            public void test(NodeRef testFolder) throws Exception
            {
                // take a look at the move capability
                Capability moveCapability = capabilityService.getCapability("MoveRecordFolder");
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, moveCapability.evaluate(testFolder, destination));
            }
        });

        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run() throws Exception
            {
                fileFolderService.move(testFolder, destination, null).getNodeRef();
            }
        });
    }

    // try and move a destroyed folder
    public void testMoveDestroyedRecordFolder() throws Exception
    {
        final NodeRef destination = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a record category (no disposition schedule)
                return filePlanService.createRecordCategory(filePlan, "Caitlin Reed");
            }
        });

        final NodeRef testFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create folder
                NodeRef testFolder = recordFolderService.createRecordFolder(rmContainer, "Peter Edward Francis");

                // complete event
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(testFolder, CompleteEventAction.NAME, params);

                // cutoff & destroy folder
                rmActionService.executeRecordsManagementAction(testFolder, CutOffAction.NAME);
                rmActionService.executeRecordsManagementAction(testFolder, DestroyAction.NAME);

                return testFolder;
            }

        });

        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                Capability moveCapability = capabilityService.getCapability("MoveRecordFolder");
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, moveCapability.evaluate(testFolder, destination));

                return fileFolderService.move(testFolder, destination, null).getNodeRef();
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertNotNull(result);
            }
        });

    }

    /**
     * Given a closed folder
     * When we evaluate the move capability on it
     * The access is denied
     */
    public void testMoveClosedFolder()
    {
        final NodeRef destination = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a record category
                return filePlanService.createRecordCategory(filePlan, GUID.generate());
            }
        });

        final NodeRef testFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create folder
                NodeRef testFolder = recordFolderService.createRecordFolder(rmContainer, GUID.generate());

                // close folder
                recordFolderService.closeRecordFolder(testFolder);

                return testFolder;
            }

            @Override
            public void test(NodeRef testFolder) throws Exception
            {
                Capability moveCapability = capabilityService.getCapability("MoveRecordFolder");
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, moveCapability.evaluate(testFolder, destination));
            }
        });
    }

    private NodeRef createRecordCategory(boolean recordLevel)
    {
        NodeRef rc = filePlanService.createRecordCategory(filePlan, GUID.generate());
        DispositionSchedule dis = utils.createBasicDispositionSchedule(rc, GUID.generate(), GUID.generate(),
                    recordLevel, false);
        Map<QName, Serializable> adParams = new HashMap<>(3);
        adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
        adParams.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
        adParams.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(dis, adParams);
        return rc;
    }
}
