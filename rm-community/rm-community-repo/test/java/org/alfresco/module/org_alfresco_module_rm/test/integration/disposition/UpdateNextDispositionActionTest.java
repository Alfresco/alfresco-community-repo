/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS;
import static org.alfresco.util.GUID.generate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.GUID;

/**
* Update next disposition step integration tests.
*
* @author Roxana Lucanu
* @since 2.4.1
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

            @Override
            public void given()
            {
                // create first category 
                NodeRef category1 = filePlanService.createRecordCategory(filePlan, generate());
                // create disposition schedule for category1
                DispositionSchedule ds1 = utils.createDispositionSchedule(category1, DEFAULT_DISPOSITION_INSTRUCTIONS, 
                        "ds1", true, false, false);

                // create the properties for CUTOFF action 
                Map<QName, Serializable> adCutOff = new HashMap<QName, Serializable>(3);
                adCutOff.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adCutOff.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "CutOffDesc");
                adCutOff.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);

                dispositionService.addDispositionActionDefinition(ds1, adCutOff);

                // create the properties for TRANSFER action
                Map<QName, Serializable> adTransfer = new HashMap<QName, Serializable>(3);
                adTransfer.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, TransferAction.NAME);
                adTransfer.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "TransferDesc");
                List<String> eventsList = new ArrayList<String>(1);
                eventsList.add("study_complete");
                adTransfer.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable)eventsList);

                dispositionService.addDispositionActionDefinition(ds1, adTransfer);

                // create the properties for DESTROY action
                Map<QName, Serializable> adDestroy = new HashMap<QName, Serializable>(3);
                adDestroy.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME);
                adDestroy.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "DestroyDesc");
                adDestroy.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "week|1");

                dispositionService.addDispositionActionDefinition(ds1, adDestroy);

                // create folder1 > record inside category1
                NodeRef firstFolder = recordFolderService.createRecordFolder(category1, GUID.generate());
                record = utils.createRecord(firstFolder, GUID.generate(), "title");

                // create second category 
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, generate());
                // create disposition schedule for category2
                DispositionSchedule ds2 = utils.createDispositionSchedule(category2, DEFAULT_DISPOSITION_INSTRUCTIONS, 
                        "ds2", true, false, true);
                // add disposition actions
                dispositionService.addDispositionActionDefinition(ds2, adCutOff);
                dispositionService.addDispositionActionDefinition(ds2, adTransfer);
                dispositionService.addDispositionActionDefinition(ds2, adDestroy);

                // create folder2 inside category2
                NodeRef folder2 = recordFolderService.createRecordFolder(category2, GUID.generate());

                // link the record to folder2
                recordService.link(record, folder2);

                // complete record
                utils.completeRecord(record);
            }

            @Override
            public void when()
            {
                // complete event
                rmActionService.executeRecordsManagementAction(record, CutOffAction.NAME, null);
            }

            @Override
            public void then()
            {
                // ensure the record folder is cut off
                assertTrue(dispositionService.isDisposableItemCutoff(record));
            }
        });

    }
}
