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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.job.publish.DispositionActionDefinitionPublishExecutor;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.extensions.webscripts.GUID;

/**
 * Integration tests for updating the disposition schedule.
 *
 * @author Tom Page
 * @since 2.3.1
 */
public class UpdateDispositionScheduleTest extends BaseRMTestCase
{
    /** A unique prefix for the constants in this test. */
    protected static final String TEST_PREFIX = UpdateDispositionScheduleTest.class.getName() + GUID.generate() + "_";
    /** The name to use for the category. */
    protected static final String CATEGORY_NAME = TEST_PREFIX + "Category";
    /** The name to use for the folder. */
    protected static final String FOLDER_NAME = TEST_PREFIX + "Folder";
    /** The name to use for the record. */
    protected static final String RECORD_NAME = TEST_PREFIX + "Record";

    /** The executor for the disposition update job. */
    private DispositionActionDefinitionPublishExecutor dispositionActionDefinitionPublishExecutor;
    /** The internal disposition service is used to avoid permissions issues when updating the record. */
    private DispositionService internalDispositionService;

    /** The category node. */
    private NodeRef category;
    /** The folder node. */
    private NodeRef folder;
    /** The record node. */
    private NodeRef record;
    /** The 'disposition as of' date from before the 'when' step. */
    private Date originalAsOfDate;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        BehaviourTest.initBehaviourTests(retryingTransactionHelper);

        // Get the application context
        applicationContext = ApplicationContextHelper.getApplicationContext(getConfigLocations());
        dispositionActionDefinitionPublishExecutor = applicationContext.getBean(DispositionActionDefinitionPublishExecutor.class);
        internalDispositionService = (DispositionService) applicationContext.getBean("dispositionService");
    }

    /**
     * <a href="https://issues.alfresco.com/jira/browse/RM-3386">RM-3386</a>
     * <p><pre>
     * Given a record subject to a disposition schedule
     * And the next step is due to run at some period after the date the content was created
     * When I update the period of the next step (and wait for this to be processed)
     * Then the "as of" date is updated to be at the new period after the creation date.
     * </pre>
     */
    public void testUpdatePeriod()
    {
        test()
            .given(() -> {
                // Create a category.
                category = filePlanService.createRecordCategory(filePlan, CATEGORY_NAME);
                // Create a disposition schedule for the category (Cut off immediately, then Destroy 1 year after the creation date).
                DispositionSchedule dispSched = utils.createBasicDispositionSchedule(category, "instructions", "authority", true, false);
                Map<QName, Serializable> cutOffParams = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME,
                                PROP_DISPOSITION_DESCRIPTION, "description",
                                PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
                dispositionService.addDispositionActionDefinition(dispSched, cutOffParams);
                Map<QName, Serializable> destroyParams = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME,
                PROP_DISPOSITION_DESCRIPTION, "description",
                PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_ONE_YEAR,
                PROP_DISPOSITION_PERIOD_PROPERTY, ContentModel.PROP_CREATED);
                dispositionService.addDispositionActionDefinition(dispSched, destroyParams);
                // Create a folder containing a record within the category.
                folder = recordFolderService.createRecordFolder(category, FOLDER_NAME);
                record = fileFolderService.create(folder, RECORD_NAME, ContentModel.TYPE_CONTENT).getNodeRef();

                dispositionService.cutoffDisposableItem(record);
                // Ensure the update has been applied to the record.
                internalDispositionService.updateNextDispositionAction(record);

                originalAsOfDate = dispositionService.getNextDispositionAction(record).getAsOfDate();
            })
            .when(() -> {
                // Update the Destroy step to be 3 years after the creation date.
                DispositionSchedule dispSched = dispositionService.getDispositionSchedule(category);
                DispositionActionDefinition destroy = dispSched.getDispositionActionDefinitionByName(DestroyAction.NAME);
                Map<QName, Serializable> destroyParams = ImmutableMap.of(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME,
                                PROP_DISPOSITION_DESCRIPTION, "description",
                                PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_THREE_YEARS,
                                PROP_DISPOSITION_PERIOD_PROPERTY, ContentModel.PROP_CREATED);
                dispositionService.updateDispositionActionDefinition(destroy, destroyParams);

                // Make the disposition action definition update job run.
                dispositionActionDefinitionPublishExecutor.publish(destroy.getNodeRef());
            })
            .then()
                .expect(true)
                    .from(() -> aboutTwoYearsApart(originalAsOfDate, dispositionService.getNextDispositionAction(record).getAsOfDate()))
                    .because("Increasing the destroy period by two years should increase the 'as of' date by two years.");
    }

    /**
     * Check that the two given dates are approximately two years apart.
     * <p>
     * This actually just checks that they're more than one and less than three years apart, because leap years make
     * things hard to calculate.
     *
     * @return true if the two dates are about two years apart.
     */
    private boolean aboutTwoYearsApart(Date start, Date end)
    {
        long days = daysBetween(start, end);
        long yearInDays = 365;
        return (yearInDays < days) && (days < 3 * yearInDays);
    }

    /** Find the number of days between the two dates. */
    private long daysBetween(Date start, Date end)
    {
        return TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
    }
}
