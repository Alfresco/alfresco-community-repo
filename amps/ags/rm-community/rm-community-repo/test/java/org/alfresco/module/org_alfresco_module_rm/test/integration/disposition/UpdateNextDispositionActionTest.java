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
package org.alfresco.module.org_alfresco_module_rm.test.integration.disposition;

import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_DISPOSITION_DESCRIPTION;
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS;
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_EVENT_NAME;
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.PERIOD_ONE_WEEK;
import static org.alfresco.util.GUID.generate;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
* Update next disposition step integration tests.
*
* @author Roxana Lucanu
* @since 2.3.1
*/
public class UpdateNextDispositionActionTest extends BaseRMTestCase
{
    /**
     * Given a record with multiple dispositions
     * When updating the next step
     * Then the action is available
     * <p>
     * relates to https://issues.alfresco.com/jira/browse/RM-3060
     */
    public void testUpdateNextDispositionAction_RM3060() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef record;
            NodeRef folder2;

            @Override
            public void given()
            {
                // create category1
                NodeRef category1 = filePlanService.createRecordCategory(filePlan, generate());

                // create disposition schedule for category1
                createDispositionSchedule(category1);

                // create category2
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, generate());

                // create disposition schedule for category2
                createDispositionSchedule(category2);

                // create folder2 inside category2
                folder2 = recordFolderService.createRecordFolder(category2, generate());

                // create folder1 inside category1
                NodeRef folder1 = recordFolderService.createRecordFolder(category1, generate());

                // create record inside folder1
                record = utils.createRecord(folder1, generate(), generate());

            }
            @Override
            public void when() throws Exception
            {
                // link the record to folder2
                recordService.link(record, folder2);

                // complete record
                utils.completeRecord(record);

                // cut off
                rmActionService.executeRecordsManagementAction(record, CutOffAction.NAME, null); 
            }

            @Override
            public void then() throws Exception
            {
                assertTrue("Record " + record + " doesn't have the cutOff aspect.", nodeService.hasAspect(record, ASPECT_CUT_OFF));
            }
        });
    }

    private void createDispositionSchedule(NodeRef category)
    {
        DispositionSchedule ds = utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_DESCRIPTION, true, false, false);

        // create the properties for CUTOFF action and add it to the disposition action definition
        Map<QName, Serializable> cutOff = new HashMap<>(3);
        cutOff.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
        cutOff.put(PROP_DISPOSITION_DESCRIPTION, generate());
        cutOff.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(ds, cutOff);

        // create the properties for TRANSFER action and add it to the disposition action definition
        Map<QName, Serializable> transfer = new HashMap<>(3);
        transfer.put(PROP_DISPOSITION_ACTION_NAME, TransferAction.NAME);
        transfer.put(PROP_DISPOSITION_DESCRIPTION, generate());
        transfer.put(PROP_DISPOSITION_EVENT, (Serializable)Collections.singletonList(DEFAULT_EVENT_NAME));
        dispositionService.addDispositionActionDefinition(ds, transfer);

        // create the properties for DESTROY action and add it to the disposition action definition
        Map<QName, Serializable> destroy = new HashMap<>(3);
        destroy.put(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME);
        destroy.put(PROP_DISPOSITION_DESCRIPTION, generate());
        destroy.put(PROP_DISPOSITION_PERIOD, PERIOD_ONE_WEEK);
        dispositionService.addDispositionActionDefinition(ds, destroy);
    }
}
