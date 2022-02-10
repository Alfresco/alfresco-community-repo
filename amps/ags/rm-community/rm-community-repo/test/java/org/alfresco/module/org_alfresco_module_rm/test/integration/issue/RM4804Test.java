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

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferCompleteAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.GUID;


/**
 * Integration test for RM-4804
 *
 * Completed records of type pdf can be transferred
 *
 * @author Ramona Popa
 * @since 2.6
 */
public class RM4804Test extends BaseRMTestCase
{
    //Fields required across transactions
    NodeRef record;
    NodeRef transferFolder;

    /**
     * Given a category with disposition schedule applied on folder with Cut Of and Transfer, a record folder and a file PDF document
     * to folder, complete the record
     * When execute disposition schedule steps 
     * Then the Transfer step is successfully finished
     */
    @org.junit.Test
    public void testTransferCompletedRecordOfTypePDF() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                // category is created
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                // create a disposition schedule for category, applied on folder
                Map<QName, Serializable> dsProps = new HashMap<>(3);
                dsProps.put(PROP_DISPOSITION_AUTHORITY, CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY);
                dsProps.put(PROP_DISPOSITION_INSTRUCTIONS, GUID.generate());
                dsProps.put(PROP_RECORD_LEVEL_DISPOSITION, false);

                DispositionSchedule dispositionSchedule = dispositionService.createDispositionSchedule(recordCategory,
                    dsProps);

                // cutoff immediately
                Map<QName, Serializable> dispositionActionCutOff = new HashMap<>(3);
                dispositionActionCutOff.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                dispositionActionCutOff.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                dispositionActionCutOff.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);

                dispositionService.addDispositionActionDefinition(dispositionSchedule, dispositionActionCutOff);

                // transfer immediately
                Map<QName, Serializable> dispositionActionTransfer = new HashMap<>(4);
                dispositionActionTransfer.put(PROP_DISPOSITION_ACTION_NAME, TransferAction.NAME);
                dispositionActionTransfer.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                dispositionActionTransfer.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
                dispositionActionTransfer.put(PROP_DISPOSITION_LOCATION, StringUtils.EMPTY);

                dispositionService.addDispositionActionDefinition(dispositionSchedule, dispositionActionTransfer);

                // add folder under category
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                // add record of type PDF under folder
                Map<QName, Serializable> props = new HashMap<>(1);
                props.put(ContentModel.PROP_TITLE, GUID.generate());
                InputStream inputStream = IOUtils.toInputStream(GUID.generate());
                record = utils.createRecord(recordFolder, GUID.generate(), props, MimetypeMap.MIMETYPE_PDF, inputStream);

                // complete the record
                utils.completeRecord(record);
                // cut off and transfer record
                rmActionService.executeRecordsManagementAction(recordFolder, CutOffAction.NAME, null);
                transferFolder = (NodeRef) rmActionService.executeRecordsManagementAction(recordFolder,
                    TransferAction.NAME)
                    .getValue();
            }

            public void when()
            {
                rmActionService.executeRecordsManagementAction(transferFolder, TransferCompleteAction.NAME);
            }

            public void then()
            {
                // verify record is transferred
                assertTrue(nodeService.hasAspect(record, ASPECT_TRANSFERRED));
            }
        });
    }
}
