/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Unit test for RM-1039 ... can't move a folder into a category with a disposition schedule
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM1039Test extends BaseRMTestCase
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    // try and move a folder from no disposition schedule to a disposition schedule
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

    // move from a disposition schedule to another .. both record folder level

    // move from a disposition schedule to another .. from record to folder level


    // try and move a cutoff folder
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
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(testFolder, CompleteEventAction.NAME, params);

                // cutoff folder
                rmActionService.executeRecordsManagementAction(testFolder, CutOffAction.NAME);

                return testFolder;
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                // take a look at the move capability
                Capability moveCapability = capabilityService.getCapability("Move");
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, moveCapability.evaluate(result, destination));

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
}
