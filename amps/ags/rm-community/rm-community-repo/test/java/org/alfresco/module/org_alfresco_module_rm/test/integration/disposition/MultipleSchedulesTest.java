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

import static org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest.test;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.extensions.webscripts.GUID;

import com.google.common.collect.ImmutableMap;

/**
 * Integration tests for records linked to multiple disposition schedules.
 *
 * @author Tom Page
 * @since 2.3.1
 */
public class MultipleSchedulesTest extends BaseRMTestCase
{
    /** A unique prefix for the constants in this test. */
    protected static final String TEST_PREFIX = MultipleSchedulesTest.class.getName() + GUID.generate() + "_";
    /** The name to use for the first category. */
    protected static final String CATEGORY_A_NAME = TEST_PREFIX + "CategoryA";
    /** The name to use for the folder within the first category. */
    protected static final String FOLDER_A_NAME = TEST_PREFIX + "FolderA";
    /** The name to use for the second category. */
    protected static final String CATEGORY_B_NAME = TEST_PREFIX + "CategoryB";
    /** The name to use for the folder within the second category. */
    protected static final String FOLDER_B_NAME = TEST_PREFIX + "FolderB";
    /** The name to use for the third category. */
    protected static final String CATEGORY_C_NAME = TEST_PREFIX + "CategoryC";
    /** The name to use for the folder within the third category. */
    protected static final String FOLDER_C_NAME = TEST_PREFIX + "FolderC";
    /** The name to use for the record. */
    protected static final String RECORD_NAME = TEST_PREFIX + "Record";

    /** The internal disposition service is used to avoid permissions issues when updating the record. */
    private DispositionService internalDispositionService;

    /** The first category node. */
    private NodeRef categoryA;
    /** The folder node within the first category. */
    private NodeRef folderA;
    /** The second category node. */
    private NodeRef categoryB;
    /** The folder node within the second category. */
    private NodeRef folderB;
    /** The third category node. */
    private NodeRef categoryC;
    /** The folder node within the third category. */
    private NodeRef folderC;
    /** The record node. */
    private NodeRef record;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        BehaviourTest.initBehaviourTests(retryingTransactionHelper);

        // Get the application context
        applicationContext = ApplicationContextHelper.getApplicationContext(getConfigLocations());
        internalDispositionService = (DispositionService) applicationContext.getBean("dispositionService");

        // Ensure different records are used for each test.
        record = null;
    }

    /**
     * Create two categories each containing a folder. Set up a schedule on category A that applies to records (cutoff
     * immediately, destroy immediately). Set up a schedule on category B that is the same, but with a week delay before
     * destroy becomes eligible.
     */
    private void setUpFilePlan()
    {
        // Only set up the file plan if it hasn't already been done.
        if (categoryA != null)
        {
            return;
        }

        // Create two categories.
        categoryA = filePlanService.createRecordCategory(filePlan, CATEGORY_A_NAME);
        categoryB = filePlanService.createRecordCategory(filePlan, CATEGORY_B_NAME);
        categoryC = filePlanService.createRecordCategory(filePlan, CATEGORY_C_NAME);
        // Create a disposition schedule for category A (Cut off immediately, then Destroy immediately).
        DispositionSchedule dispSchedA = utils.createBasicDispositionSchedule(categoryA, "instructions", "authority", true, false);
        Map<QName, Serializable> cutOffParamsA = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME,
                        PROP_DISPOSITION_DESCRIPTION, "description",
                        PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(dispSchedA, cutOffParamsA);
        Map<QName, Serializable> destroyParamsA = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME,
                        PROP_DISPOSITION_DESCRIPTION, "description",
                        PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(dispSchedA, destroyParamsA);
        // Create a disposition schedule for category B (Cut off immediately, then Destroy one week after cutoff).
        DispositionSchedule dispSchedB = utils.createBasicDispositionSchedule(categoryB, "instructions", "authority", true, false);
        Map<QName, Serializable> cutOffParamsB = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME,
                        PROP_DISPOSITION_DESCRIPTION, "description",
                        PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(dispSchedB, cutOffParamsB);
        Map<QName, Serializable> destroyParamsB = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME,
                        PROP_DISPOSITION_DESCRIPTION, "description",
                        PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_ONE_WEEK,
                        PROP_DISPOSITION_PERIOD_PROPERTY, PROP_CUT_OFF_DATE);
        dispositionService.addDispositionActionDefinition(dispSchedB, destroyParamsB);
        // Create a disposition schedule for category C (Cut off immediately, then Destroy one year after cutoff).
        DispositionSchedule dispSchedC = utils.createBasicDispositionSchedule(categoryC, "instructions", "authority", true, false);
        Map<QName, Serializable> cutOffParamsC = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME,
                        PROP_DISPOSITION_DESCRIPTION, "description",
                        PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
        dispositionService.addDispositionActionDefinition(dispSchedC, cutOffParamsC);
        Map<QName, Serializable> destroyParamsC = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME,
                        PROP_DISPOSITION_DESCRIPTION, "description",
                        PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_ONE_YEAR,
                        PROP_DISPOSITION_PERIOD_PROPERTY, PROP_CUT_OFF_DATE);
        dispositionService.addDispositionActionDefinition(dispSchedC, destroyParamsC);
        // Create a folder within each category.
        folderA = recordFolderService.createRecordFolder(categoryA, FOLDER_A_NAME);
        folderB = recordFolderService.createRecordFolder(categoryB, FOLDER_B_NAME);
        folderC = recordFolderService.createRecordFolder(categoryC, FOLDER_C_NAME);
    }

    /**
     * <a href="https://issues.alfresco.com/jira/browse/RM-2526">RM-2526</a>
     * <p><pre>
     * Given a record subject to a disposition schedule
     * And it is linked to a disposition schedule with the same step order, but a longer destroy step
     * When the record is moved onto the destroy step
     * Then the "as of" date is calculated using the longer period.
     * </pre>
     */
    public void testLinkedToLongerSchedule()
    {
        Calendar calendar = Calendar.getInstance();
        test()
            .given(() -> {
                setUpFilePlan();
                
                // Create a record filed under category A and linked to category B.
                record = fileFolderService.create(folderA, RECORD_NAME, ContentModel.TYPE_CONTENT).getNodeRef();
                recordService.link(record, folderB);
            })
            .when(() -> {
                // Cut off the record.
                dispositionService.cutoffDisposableItem(record);
                // Ensure the update has been applied to the record.
                internalDispositionService.updateNextDispositionAction(record);
                calendar.setTime((Date) nodeService.getProperty(record, PROP_CUT_OFF_DATE));            
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            })
            .then()
                .expect(calendar.getTime())
                        .from(() -> dispositionService.getNextDispositionAction(record).getAsOfDate())
                    .because("Record should follow largest rentention schedule period, which is one week.");
    }

    /**
     * <a href="https://issues.alfresco.com/jira/browse/RM-2526">RM-2526</a>
     * <p><pre>
     * Given a record subject to a disposition schedule
     * And it is linked to a disposition schedule with the same step order, but a shorter destroy step
     * When the record is moved onto the destroy step
     * Then the "as of" date is calculated using the longer period.
     * </pre>
     */
    public void testLinkedToShorterSchedule()
    {
        Calendar calendar = Calendar.getInstance();
        test()
            .given(() -> {
                setUpFilePlan();
                // Create a record filed under category B and linked to category A.
                record = fileFolderService.create(folderB, RECORD_NAME, ContentModel.TYPE_CONTENT).getNodeRef();
                recordService.link(record, folderA);
            })
            .when(() -> {
                // Cut off the record.
                dispositionService.cutoffDisposableItem(record);
                // Ensure the update has been applied to the record.
                internalDispositionService.updateNextDispositionAction(record);
                calendar.setTime((Date) nodeService.getProperty(record, PROP_CUT_OFF_DATE));            
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            })
            .then()
                .expect(calendar.getTime())
                        .from(() -> dispositionService.getNextDispositionAction(record).getAsOfDate())
                    .because("Record should follow largest rentention schedule period, which is one week.");
    }
    
    /**
     * <a href="https://issues.alfresco.com/jira/browse/RM-4292">RM-4292</a>
     * <p><pre>
     * Given a record subject to a mixed disposition schedule
     * When the record is unlinked from one of its secondary parents
     * Then the next disposition action is recalculated.
     * </pre>
     */
    public void testRecalculateDispositionWhenUnlinking()
    {
        test()
            .given(() -> {
                setUpFilePlan();
                // Create a record filed under category A and linked to category B and C.
                record = fileFolderService.create(folderA, RECORD_NAME, ContentModel.TYPE_CONTENT).getNodeRef();
                recordService.link(record, folderB);
                recordService.link(record, folderC);
                // Cut off the record.
                dispositionService.cutoffDisposableItem(record);
                // Ensure the update has been applied to the record.
                internalDispositionService.updateNextDispositionAction(record);
            })
            .when(() -> {
                // Unlink the record from folder B.
                recordService.unlink(record, folderB);
            })
            .then()
                .expect(false)
                        .from(() -> dispositionService.isNextDispositionActionEligible(record))
                    .because("Destroy action shouldn't be available, as the record should follow disposition schedule from category C.");
    }
}
