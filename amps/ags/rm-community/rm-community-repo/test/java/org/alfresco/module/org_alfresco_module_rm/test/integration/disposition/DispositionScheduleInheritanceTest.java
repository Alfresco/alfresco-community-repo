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
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.SEPARATION_EVENT_NAME;
import static org.alfresco.util.GUID.generate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.RetainAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
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
                createDispositionScheduleCutOffAndRetainImmediately(category1);

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

    /**
     * Given a root record category A with a retention schedule set to cut off after 10 days
     * and another root record category B with a retention schedule set to cut off after 5 days containing a
     * subcategory
     * When moving the subcategory into the first root category
     * Then records under the subcategory inherit the retention schedule of the parent record category
     * The cut off date is updated to the new one, since the initial date was before the new one
     * <p>
     * Please see https://alfresco.atlassian.net/browse/RM-7103
     */
    public void testRetentionScheduleInheritance_APPS7103()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef category1;
            NodeRef subcategory2;
            NodeRef record;
            Date asOfDateBeforeMove;

            @Override
            public void given()
            {
                // create root category1
                category1 = filePlanService.createRecordCategory(filePlan, generate());

                // create record level disposition schedule for category1
                createDispositionScheduleCutOff(category1, CutOffAction.NAME, CommonRMTestUtils.PERIOD_TEN_DAYS);

                // create root category2
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, generate());

                // create record level disposition schedule for category2
                createDispositionScheduleCutOff(category2, CutOffAction.NAME, CommonRMTestUtils.PERIOD_FIVE_DAYS);

                // create subcategory2 under category2
                subcategory2 = filePlanService.createRecordCategory(category2, generate());

                // create folder under subcategory2
                folder = recordFolderService.createRecordFolder(subcategory2, generate());

                // file record in folder and complete it
                record = utils.createRecord(folder, generate(), generate());
                utils.completeRecord(record);

                //store the date to check if it was updated
                asOfDateBeforeMove = dispositionService.getNextDispositionAction(record).getAsOfDate();
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
                // check the next disposition action
                DispositionAction dispositionActionAfterMove = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionActionAfterMove);
                assertEquals(CutOffAction.NAME, dispositionActionAfterMove.getName());
                assertNotNull(dispositionActionAfterMove.getAsOfDate());
                assertTrue(dispositionActionAfterMove.getAsOfDate().after(asOfDateBeforeMove));

                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertNull((List<String>) nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_INSTRUCTIONS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_AUTHORITY));
                assertTrue((Boolean) nodeService.getProperty(record, PROP_RS_HAS_DISPOITION_SCHEDULE));
            }
        });
    }

    /**
     * Given a root record category A with a retention schedule set to retain and destroy after 1 day
     * and another root record category B with a retention schedule set to cut off and destroy after 1 day containing a
     * subcategory
     * When moving the subcategory into the first root category
     * Then records under the subcategory inherit the retention schedule of the parent record category
     * The events list contain the retain event step inherited from the new parent category
     * <p>
     * Please see https://alfresco.atlassian.net/browse/APPS-1004
     */
    public void testRetentionScheduleInheritance_APPS_1004()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef category1;
            NodeRef subcategory2;
            NodeRef record;
            Date asOfDateBeforeMove;

            @Override
            public void given()
            {
                // create root category1
                category1 = filePlanService.createRecordCategory(filePlan, generate());

                // create record level disposition schedule for category1
                createDispositionScheduleRetainAndCutOffOneDay(category1);

                // create root category2
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, generate());

                // create record level disposition schedule for category2
                createDispositionScheduleCutOffAndDestroyOneDay(category2);

                // create subcategory2 under category2
                subcategory2 = filePlanService.createRecordCategory(category2, generate());

                // create folder under subcategory2
                folder = recordFolderService.createRecordFolder(subcategory2, generate());

                // file record in folder and complete it
                record = utils.createRecord(folder, generate(), generate());
                utils.completeRecord(record);

                //store the date to check if it was updated
                asOfDateBeforeMove = dispositionService.getNextDispositionAction(record).getAsOfDate();
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
                // check the next disposition action
                DispositionAction dispositionActionAfterMove = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionActionAfterMove);
                assertEquals(RetainAction.NAME, dispositionActionAfterMove.getName());
                assertNotNull(dispositionActionAfterMove.getAsOfDate());
                assertTrue(dispositionActionAfterMove.getAsOfDate().after(asOfDateBeforeMove));

                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(RetainAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertNull((List<String>) nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_INSTRUCTIONS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_AUTHORITY));
                assertTrue((Boolean) nodeService.getProperty(record, PROP_RS_HAS_DISPOITION_SCHEDULE));
            }
        });
    }

    /**
     * Given a root record category A with a retention schedule set to cut off on event 'case closed'
     * and another root record category B with a retention schedule set to cut off on event 'separation'
     * When moving the subcategory into the first root category
     * Then records under the subcategory inherit the retention schedule of the parent record category
     * The events list contain the case closed event step inherited from the new parent category
     * <p>
     * Please see https://alfresco.atlassian.net/browse/APPS-1005
     */
    public void testRetentionScheduleInheritance_APPS_1005()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef category1;
            NodeRef subcategory2;
            NodeRef record;
            Date asOfDateBeforeMove;

            @Override
            public void given()
            {
                // create root category1
                category1 = filePlanService.createRecordCategory(filePlan, generate());

                utils.createDispositionSchedule(category1, DEFAULT_DISPOSITION_INSTRUCTIONS,
                        DEFAULT_DISPOSITION_DESCRIPTION, true, true, false, DEFAULT_EVENT_NAME);

                // create root category2
                NodeRef category2 = filePlanService.createRecordCategory(filePlan, generate());

                // create record level disposition schedule for category2
                utils.createDispositionSchedule(category2, DEFAULT_DISPOSITION_INSTRUCTIONS,
                        DEFAULT_DISPOSITION_DESCRIPTION, true, true, false, SEPARATION_EVENT_NAME);

                // create subcategory2 under category2
                subcategory2 = filePlanService.createRecordCategory(category2, generate());

                // create folder under subcategory2
                folder = recordFolderService.createRecordFolder(subcategory2, generate());

                // file record in folder and complete it
                record = utils.createRecord(folder, generate(), generate());
                utils.completeRecord(record);

                //store the date to check if it was updated
                asOfDateBeforeMove = dispositionService.getNextDispositionAction(record).getAsOfDate();
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
                // check the next disposition action
                DispositionAction dispositionActionAfterMove = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionActionAfterMove);
                assertEquals(CutOffAction.NAME, dispositionActionAfterMove.getName());

                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNotNull((List<String>) nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS));
                assertEquals(((List<String>) ((List<String>) nodeService.getProperty(record,
                        PROP_RS_DISPOSITION_EVENTS))).size(), 1);
                assertEquals(DEFAULT_EVENT_NAME, ((List<String>) ((List<String>) nodeService.getProperty(record,
                        PROP_RS_DISPOSITION_EVENTS))).get(0));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_INSTRUCTIONS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_AUTHORITY));
                assertTrue((Boolean) nodeService.getProperty(record, PROP_RS_HAS_DISPOITION_SCHEDULE));
            }
        });
    }

    private void createDispositionScheduleCutOff(NodeRef category, String action, String period)
    {
        DispositionSchedule ds = utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_DESCRIPTION, true, false, false);

        createDispositionScheduleStep(ds, action, period);
    }

    private void createDispositionScheduleCutOffAndRetainImmediately(NodeRef category)
    {
        DispositionSchedule ds = utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_DESCRIPTION, true, false, false);

        createDispositionScheduleStep(ds, CutOffAction.NAME, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        createDispositionScheduleStep(ds, RetainAction.NAME, CommonRMTestUtils.PERIOD_IMMEDIATELY);
    }

    private void createDispositionScheduleRetainAndCutOffOneDay(NodeRef category)
    {
        DispositionSchedule ds = utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_DESCRIPTION, true, false, false);

        createDispositionScheduleStep(ds, RetainAction.NAME, CommonRMTestUtils.PERIOD_ONE_DAY);
        createDispositionScheduleStep(ds, DestroyAction.NAME, CommonRMTestUtils.PERIOD_ONE_DAY);
    }

    private void createDispositionScheduleCutOffAndDestroyOneDay(NodeRef category)
    {
        DispositionSchedule ds = utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_DESCRIPTION, true, false, false);

        createDispositionScheduleStep(ds, CutOffAction.NAME, CommonRMTestUtils.PERIOD_ONE_DAY);
        createDispositionScheduleStep(ds, DestroyAction.NAME, CommonRMTestUtils.PERIOD_ONE_DAY);
    }

    private void createDispositionScheduleStep(DispositionSchedule ds, String action, String period)
    {
        Map<QName, Serializable> step = new HashMap<QName, Serializable>(3);
        step.put(PROP_DISPOSITION_ACTION_NAME, action);
        step.put(PROP_DISPOSITION_DESCRIPTION, generate());
        step.put(PROP_DISPOSITION_PERIOD, period);
        dispositionService.addDispositionActionDefinition(ds, step);
    }
}
