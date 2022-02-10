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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferCompleteAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;

/**
 *
 * @author Ana Bozianu
 * @since 2.3
 */
public class RM1914Test extends BaseRMTestCase
{
    //Fields required across transactions
    NodeRef record1, transferFolder;

    public void testRM1914() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {

            public void given(){

                // create test holds
                NodeRef category1 = filePlanService.createRecordCategory(filePlan, GUID.generate());
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, GUID.generate());

                Map<QName, Serializable> dsProps = new HashMap<>(3);
                dsProps.put(PROP_DISPOSITION_AUTHORITY, CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY);
                dsProps.put(PROP_DISPOSITION_INSTRUCTIONS, GUID.generate());
                dsProps.put(PROP_RECORD_LEVEL_DISPOSITION, true);
                DispositionSchedule dispositionSchedule1 = dispositionService.createDispositionSchedule(category1, dsProps);
                NodeRef folder1 = recordFolderService.createRecordFolder(category1, GUID.generate());

                // - add cutoff after "Related Record Transferred To Inactive Storage" completion event
                Map<QName, Serializable> dispositionAction1 = new HashMap<>(3);
                dispositionAction1.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                dispositionAction1.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());

                List<String> events = new ArrayList<>(1);
                events.add("related_record_trasfered_inactive_storage");
                dispositionAction1.put(PROP_DISPOSITION_EVENT, (Serializable) events);

                dispositionService.addDispositionActionDefinition(dispositionSchedule1, dispositionAction1);

                // 3. Folder1 > Record1 is created inside Category1
                record1 = recordService.createRecordFromContent(folder1, GUID.generate(), TYPE_CONTENT, null, null);

                // 4. Disposition schedule is created for the Category2:
                // applied on Record
                DispositionSchedule dispositionSchedule2 = dispositionService.createDispositionSchedule(category2, dsProps);

                // - cutoff immediatelly
                Map<QName, Serializable> dispositionAction2_1 = new HashMap<>(3);
                dispositionAction2_1.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                dispositionAction2_1.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                dispositionAction2_1.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);

                dispositionService.addDispositionActionDefinition(dispositionSchedule2, dispositionAction2_1);

                // - Transfer Immediatelly
                Map<QName, Serializable> dispositionAction2_2 = new HashMap<>(4);
                dispositionAction2_2.put(PROP_DISPOSITION_ACTION_NAME, TransferAction.NAME);
                dispositionAction2_2.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                dispositionAction2_2.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
                dispositionAction2_2.put(PROP_DISPOSITION_LOCATION, StringUtils.EMPTY);

                dispositionService.addDispositionActionDefinition(dispositionSchedule2, dispositionAction2_2);
                // 5. Folder2 > Record2 is created inside Category2
                NodeRef folder2 = recordFolderService.createRecordFolder(category2, GUID.generate());
                NodeRef record2 = recordService.createRecordFromContent(folder2, GUID.generate(), TYPE_CONTENT, null, null);

                // 6. Record1 and Record2 are completed
                utils.completeRecord(record1);
                utils.completeRecord(record2);
                // 7. Create Cross-Reference link from Record1 to Record2
                relationshipService.addRelationship(CUSTOM_REF_CROSSREFERENCE.getLocalName(), record1, record2);
                // 8. Cut off and transfer Record2
                rmActionService.executeRecordsManagementAction(record2, CutOffAction.NAME, null);

                transferFolder = (NodeRef) rmActionService.executeRecordsManagementAction(record2, TransferAction.NAME)
                    .getValue();
                
            }

            public void when()
            {


                rmActionService.executeRecordsManagementAction(transferFolder, TransferCompleteAction.NAME);
                
            }

            public void then()
            {
                // 9. Verify Record1 
                assertTrue(dispositionService.isNextDispositionActionEligible(record1));
              
            }
        });
    }
}
