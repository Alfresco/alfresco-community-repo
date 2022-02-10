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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.util.GUID;

/**
 * Vital record service implementation unit test.
 *
 * @author Roy Wetherall
 */
public class VitalRecordServiceImplTest extends BaseRMTestCase
{
    /** Test periods */
    protected static final Period PERIOD_NONE = new Period("none|0");
    protected static final Period PERIOD_WEEK = new Period("week|1");
    protected static final Period PERIOD_MONTH = new Period("month|1");

    /** Test records */
    private NodeRef mhRecord51;
    private NodeRef mhRecord52;
    private NodeRef mhRecord53;
    private NodeRef mhRecord54;
    private NodeRef mhRecord55;

    /**
     * Indicate this test uses the collaboration site test data
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /** Indicate this is a multi hierarchy test */
    @Override
    protected boolean isMultiHierarchyTest()
    {
        return true;
    }

    /** vital record multi-hierarchy test data
    *
    *   |--rmRootContainer (no vr def)
    *      |
    *      |--mhContainer  (no vr def)
    *         |
    *         |--mhContainer-1-1 (has schedule - folder level) (no vr def)
    *         |  |
    *         |  |--mhContainer-2-1 (vr def)
    *         |     |
    *         |     |--mhContainer-3-1 (no vr def)
    *         |
    *         |--mhContainer-1-2 (has schedule - folder level) (no vr def)
    *            |
    *            |--mhContainer-2-2 (no vr def)
    *            |  |
    *            |  |--mhContainer-3-2 (vr def disabled)
    *            |  |
    *            |  |--mhContainer-3-3 (has schedule - record level) (vr def)
    *            |
    *            |--mhContainer-2-3 (has schedule - folder level) (vr def)
    *               |
    *               |--mhContainer-3-4 (no vr def)
    *               |
    *               |--mhContainer-3-5 (has schedule- record level) (vr def)
    */
    @Override
    protected void setupMultiHierarchyTestData()
    {
        // Load core test data
        super.setupMultiHierarchyTestData();

        // Setup vital record definitions
        setupVitalRecordDefinition(mhContainer21, true, PERIOD_WEEK);
        setupVitalRecordDefinition(mhContainer32, false, PERIOD_WEEK);
        setupVitalRecordDefinition(mhContainer33, true, PERIOD_WEEK);
        setupVitalRecordDefinition(mhContainer23, true, PERIOD_WEEK);
        setupVitalRecordDefinition(mhContainer35, true, PERIOD_MONTH);

        // Create records
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                mhRecord51 = utils.createRecord(mhRecordFolder41, "record51.txt");
                mhRecord52 = utils.createRecord(mhRecordFolder42, "record52.txt");
                mhRecord53 = utils.createRecord(mhRecordFolder43, "record53.txt");
                mhRecord54 = utils.createRecord(mhRecordFolder44, "record54.txt");
                mhRecord55 = utils.createRecord(mhRecordFolder45, "record55.txt");

                return null;
            }
        });
    }

    /**
     * Helper to set up the vital record definition data in a transactional manner.
     *
     * @param nodeRef
     * @param enabled
     * @param period
     */
    private void setupVitalRecordDefinition(final NodeRef nodeRef, final boolean enabled, final Period period)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                vitalRecordService.setVitalRecordDefintion(nodeRef, enabled, period);
                return null;
            }
        });
    }

    /**
     * Based on the initial data:
     *  - check category, folder and record raw values.
     *  - check search aspect values.
     */
    public void testInit()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertHasVitalRecordDefinition(mhContainer, false, null);
                assertHasVitalRecordDefinition(mhContainer11, false, null);
                assertHasVitalRecordDefinition(mhContainer12, false, null);
                assertHasVitalRecordDefinition(mhContainer21, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer22, false, null);
                assertHasVitalRecordDefinition(mhContainer23, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer31, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer32, false, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer33, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer34, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer35, true, PERIOD_MONTH);

                assertHasVitalRecordDefinition(mhRecordFolder41, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder42, false, null);
                assertHasVitalRecordDefinition(mhRecordFolder43, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder44, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder45, true, PERIOD_MONTH);

                assertVitalRecord(mhRecord51, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord52, false, null);
                assertVitalRecord(mhRecord53, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord54, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord55, true, PERIOD_MONTH);

                return null;
            }
        });
    }

    /**
     * Test that when new record categories and record folders are created in an existing file plan
     * structure that they correctly inherit the correct vital record property values
     */
    public void testValueInheritance() throws Exception
    {
        // Test record category value inheritance
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                return filePlanService.createRecordCategory(mhContainer35, GUID.generate());
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertHasVitalRecordDefinition(result, true, PERIOD_MONTH);
            }
        });

        // Test record folder value inheritance
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                return recordFolderService.createRecordFolder(mhContainer32, GUID.generate());
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertHasVitalRecordDefinition(result, false, PERIOD_WEEK);
            }
        });
    }

    /** Filling tests */

    public void testFileNewContent() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                NodeRef record = fileFolderService.create(mhRecordFolder41, "test101.txt" , TYPE_CONTENT).getNodeRef();

                ContentWriter writer = contentService.getWriter(record, PROP_CONTENT, true);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("hello world this is some test content");

                return record;
            }

            @Override
            public void test(NodeRef record) throws Exception
            {
                assertVitalRecord(record, true, PERIOD_WEEK);
            }
        });
    }

//    public void testFileUnfiledrecord() throws Exception
//    {
//        doTestInTransaction(new Test<NodeRef>()
//        {
//            @Override
//            public NodeRef run() throws Exception
//            {
//                recordService.createRecord(filePlan, dmDocument);
//                fileFolderService.move(dmDocument, mhRecordFolder41, "record.txt");
//
//                return dmDocument;
//            }
//
//            @Override
//            public void test(NodeRef record) throws Exception
//            {
//                assertVitalRecord(record, true, PERIOD_WEEK);
//            }
//        });
//    }
//
//    public void testFileDirectlyFromCollab() throws Exception
//    {
//        doTestInTransaction(new Test<NodeRef>()
//        {
//            @Override
//            public NodeRef run() throws Exception
//            {
//                fileFolderService.move(dmDocument, mhRecordFolder41, "record.txt");
//                return dmDocument;
//            }
//
//            @Override
//            public void test(NodeRef record) throws Exception
//            {
//                assertVitalRecord(record, true, PERIOD_WEEK);
//            }
//        });
//    }

    /** Helper Methods */

    /**
     * Test to ensure that changes made to vital record definitions are reflected down the hierarchy.
     */
    public void testChangesToVitalRecordDefinitions() throws Exception
    {
        // Override vital record definition
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                setupVitalRecordDefinition(mhContainer31, true, PERIOD_MONTH);
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertHasVitalRecordDefinition(mhContainer, false, null);
                assertHasVitalRecordDefinition(mhContainer11, false, null);
                assertHasVitalRecordDefinition(mhContainer12, false, null);
                assertHasVitalRecordDefinition(mhContainer21, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer22, false, null);
                assertHasVitalRecordDefinition(mhContainer23, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer31, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer32, false, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer33, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer34, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer35, true, PERIOD_MONTH);

                assertHasVitalRecordDefinition(mhRecordFolder41, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhRecordFolder42, false, null);
                assertHasVitalRecordDefinition(mhRecordFolder43, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder44, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder45, true, PERIOD_MONTH);

                assertVitalRecord(mhRecord51, true, PERIOD_MONTH);
                assertVitalRecord(mhRecord52, false, null);
                assertVitalRecord(mhRecord53, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord54, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord55, true, PERIOD_MONTH);
            }
        });

        // 'turn off' vital record def
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                setupVitalRecordDefinition(mhContainer31, false, PERIOD_NONE);
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertHasVitalRecordDefinition(mhContainer, false, null);
                assertHasVitalRecordDefinition(mhContainer11, false, null);
                assertHasVitalRecordDefinition(mhContainer12, false, null);
                assertHasVitalRecordDefinition(mhContainer21, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer22, false, null);
                assertHasVitalRecordDefinition(mhContainer23, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer31, false, null);
                assertHasVitalRecordDefinition(mhContainer32, false, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer33, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer34, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer35, true, PERIOD_MONTH);

                assertHasVitalRecordDefinition(mhRecordFolder41, false, null);
                assertHasVitalRecordDefinition(mhRecordFolder42, false, null);
                assertHasVitalRecordDefinition(mhRecordFolder43, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder44, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhRecordFolder45, true, PERIOD_MONTH);

                assertVitalRecord(mhRecord51, false, null);
                assertVitalRecord(mhRecord52, false, null);
                assertVitalRecord(mhRecord53, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord54, true, PERIOD_WEEK);
                assertVitalRecord(mhRecord55, true, PERIOD_MONTH);
            }
        });

        // Test parent change overrites existing
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                setupVitalRecordDefinition(mhContainer12, true, PERIOD_MONTH);
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertHasVitalRecordDefinition(mhContainer, false, null);
                assertHasVitalRecordDefinition(mhContainer11, false, null);
                assertHasVitalRecordDefinition(mhContainer12, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer21, true, PERIOD_WEEK);
                assertHasVitalRecordDefinition(mhContainer22, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer23, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer31, false, null);
                assertHasVitalRecordDefinition(mhContainer32, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer33, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer34, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhContainer35, true, PERIOD_MONTH);

                assertHasVitalRecordDefinition(mhRecordFolder41, false, null);
                assertHasVitalRecordDefinition(mhRecordFolder42, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhRecordFolder43, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhRecordFolder44, true, PERIOD_MONTH);
                assertHasVitalRecordDefinition(mhRecordFolder45, true, PERIOD_MONTH);

                assertVitalRecord(mhRecord51, false, null);
                assertVitalRecord(mhRecord52, true, PERIOD_MONTH);
                assertVitalRecord(mhRecord53, true, PERIOD_MONTH);
                assertVitalRecord(mhRecord54, true, PERIOD_MONTH);
                assertVitalRecord(mhRecord55, true, PERIOD_MONTH);
            }
        });

    }

    @SuppressWarnings("deprecation")
    private void assertHasVitalRecordDefinition(NodeRef nodeRef, boolean enabled, Period period)
    {
        assertTrue(nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD_DEFINITION));

        VitalRecordDefinition def = vitalRecordService.getVitalRecordDefinition(nodeRef);
        assertNotNull(def);

        Boolean vitalRecordIndicator = (Boolean)nodeService.getProperty(nodeRef, PROP_VITAL_RECORD_INDICATOR);
        assertNotNull(vitalRecordIndicator);
        assertEquals(enabled, vitalRecordIndicator.booleanValue());
        assertEquals(enabled, def.isEnabled());

        if (enabled)
        {
            Period reviewPeriod = (Period)nodeService.getProperty(nodeRef, PROP_REVIEW_PERIOD);
            assertNotNull(reviewPeriod);
            assertEquals(period, reviewPeriod);
            assertEquals(period, def.getReviewPeriod());
            assertEquals(period.getNextDate(new Date()).getDate(), def.getNextReviewDate().getDate());
        }
    }

    @SuppressWarnings("deprecation")
    private void assertVitalRecord(NodeRef nodeRef, boolean enabled, Period period)
    {
        assertEquals(enabled, nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD));
        if (enabled)
        {
            Date reviewAsOf = (Date)nodeService.getProperty(nodeRef, PROP_REVIEW_AS_OF);
            assertNotNull(reviewAsOf);
            assertEquals(period.getNextDate(new Date()).getDate(), reviewAsOf.getDate());

            assertEquals(period.getPeriodType(), nodeService.getProperty(nodeRef, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD));
            assertEquals(period.getExpression(), nodeService.getProperty(nodeRef, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION));
        }
        else
        {
            assertNull(nodeService.getProperty(nodeRef, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD));
            assertNull(nodeService.getProperty(nodeRef, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION));
        }
    }
}
