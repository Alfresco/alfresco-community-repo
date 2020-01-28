/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
import static org.alfresco.util.GUID.generate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.RetainAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Inherit disposition schedule on record level when moving categories.
 *
 * @author Roxana Lucanu
 * @since 2.5
 */
public class DispositionScheduleInheritanceTest extends BaseRMTestCase
{
    /**
     * Given a root record category with a retention schedule
     * and another root record category
     * When moving the second record category into the first one (with the disposition)
     * Then records under the second record category inherit the retention schedule of the parent record category
     * <p>
     * relates to https://issues.alfresco.com/jira/browse/RM-7065
     */
    public void testRetentionScheduleInheritance_RM7065()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef category1;
            NodeRef subcategory2;
            NodeRef record;

            @Override
            public void given()
            {
                // create root category1
                category1 = filePlanService.createRecordCategory(filePlan, generate());

                // create record level disposition schedule for category1
                createDispositionSchedule(category1);

                // create subcategory1 under category1
                NodeRef subcategory1 = filePlanService.createRecordCategory(category1, generate());

                // create root category2
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, generate());

                // create subcategory2 under category2
                subcategory2 = filePlanService.createRecordCategory(category2, generate());

                // create folder under subcategory2
                folder = recordFolderService.createRecordFolder(subcategory2, generate());

                // file record in folder and complete it
                record = utils.createRecord(folder, generate(), generate());
                utils.completeRecord(record);
            }

            @Override
            public void when() throws Exception
            {
                // move subcategory2 under category1
                fileFolderService.move(subcategory2, category1, null);
            }

            @Override
            public void then() throws Exception
            {
                dispositionService.getDispositionSchedule(record);
                // check for the lifecycle aspect
                assertTrue("Record " + record + " doesn't have the disposition lifecycle aspect.", nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
            }
        });
    }

    private void createDispositionSchedule(NodeRef category)
    {
        DispositionSchedule ds = utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_DESCRIPTION, true, false, false);

        // CUTOFF immediately
        Map<QName, Serializable> cutOff = new HashMap<QName, Serializable>(3);
        cutOff.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
        cutOff.put(PROP_DISPOSITION_DESCRIPTION, generate());
        cutOff.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(ds, cutOff);

        // RETAIN immediately
        Map<QName, Serializable> retain = new HashMap<QName, Serializable>(3);
        retain.put(PROP_DISPOSITION_ACTION_NAME, RetainAction.NAME);
        retain.put(PROP_DISPOSITION_DESCRIPTION, generate());
        retain.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(ds, retain);
    }
}