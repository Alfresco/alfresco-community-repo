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

package org.alfresco.module.org_alfresco_module_rm.test.integration.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Test automatic disposition via scheduled job.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class AutomaticDispositionTest extends BaseRMTestCase
{
    @SuppressWarnings("unused")
    private RecordsManagementAuditService auditService;

    /** additional job context to override job frequency */
    protected String[] getConfigLocations()
    {
        return ArrayUtils.add(super.getConfigLocations(), "classpath:test-job-context.xml");
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        auditService = (RecordsManagementAuditService)applicationContext.getBean("recordsManagementAuditService");
    }

    /**
     * Given there is a complete record eligible for cut off, when the correct frequency of time passes, then
     * the record will be automatically cut off
     */
    public void testAutomaticCutOff()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef sourceCategory;
            NodeRef sourceRecordFolder;
            NodeRef record;

            public void given()
            {
                // create test data
                sourceCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                DispositionSchedule dis = utils.createBasicDispositionSchedule(sourceCategory, GUID.generate(), GUID.generate(), true, false);
                Map<QName, Serializable> adParams = new HashMap<QName, Serializable>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                adParams.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
                dispositionService.addDispositionActionDefinition(dis, adParams);
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceCategory, GUID.generate());

                // create and complete record
                record = utils.createRecord(sourceRecordFolder, GUID.generate());
                utils.completeRecord(record);

                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertTrue(dispositionService.isNextDispositionActionEligible(record));
            }

            public void when() throws Exception
            {
                // sleep .. allowing the job time to execute
                Thread.sleep(30000);
            }

            public void then()
            {
                // record should now be cut off
                assertTrue(dispositionService.isDisposableItemCutoff(record));

                // TODO .. automatic dispoistion does not log entry in audit
                //      .. the following test checks for this, but is currently commented out
                //      .. because it doesn't work!
//                RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
//                params.setEvent(CutOffAction.NAME);
//                params.setMaxEntries(1);
//                List<RecordsManagementAuditEntry> entries = auditService.getAuditTrail(params);
//                assertNotNull(entries);
//                assertEquals(1, entries.size());
//
//                RecordsManagementAuditEntry entry = entries.get(0);
//                assertEquals(record, entry.getNodeRef());
            }
        });
    }

    // TODO automatic retain

    // TODO automatic destroy
}
