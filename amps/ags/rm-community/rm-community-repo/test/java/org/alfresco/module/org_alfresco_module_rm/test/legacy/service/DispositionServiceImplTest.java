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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.job.PublishUpdatesJobExecuter;
import org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutor;
import org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutorRegistry;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Disposition service implementation unit test.
 *
 * @author Roy Wetherall
 */
public class DispositionServiceImplTest extends BaseRMTestCase
{
    @Override
    protected boolean isMultiHierarchyTest()
    {
        return true;
    }

    public void testGetDispositionProperties() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Collection<DispositionProperty> properties = dispositionService.getDispositionProperties();

                assertNotNull(properties);
                assertEquals(5, properties.size());

                for (DispositionProperty property : properties)
                {
                    assertNotNull(property.getQName());
                    assertNotNull(property.getPropertyDefinition());
                }

                return null;
            }
        });
    }

    /**
     * @see DispositionService#getDispositionSchedule(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testGetDispositionSchedule() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Check for null lookup's
                assertNull(dispositionService.getDispositionSchedule(filePlan));

                // Get the containers disposition schedule
                DispositionSchedule ds = dispositionService.getDispositionSchedule(rmContainer);
                assertNotNull(ds);
                checkDispositionSchedule(ds, false);

                // Get the folders disposition schedule
                ds = dispositionService.getDispositionSchedule(rmContainer);
                assertNotNull(ds);
                checkDispositionSchedule(ds, false);

                return null;
            }

        });

        // Failure: Root node
        doTestInTransaction(new FailureTest
        (
        	"Should not be able to get adisposition schedule for the root node",
        	AlfrescoRuntimeException.class
        )
        {
            @Override
            public void run()
            {
                dispositionService.getDispositionSchedule(rootNodeRef);
            }
        });

        // Failure: Non-rm node
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
                dispositionService.getDispositionSchedule(folder);
            }
        });
    }

    /**
     * @see DispositionService#getDispositionSchedule(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testGetDispositionScheduleMultiHier() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertNull(dispositionService.getDispositionSchedule(mhContainer));

                // Level 1
                doCheck(mhContainer11, "ds11", false);
                doCheck(mhContainer12, "ds12", false);

                // Level 2
                doCheck(mhContainer21, "ds11", false);
                doCheck(mhContainer22, "ds12", false);
                doCheck(mhContainer23, "ds23", false);

                // Level 3
                doCheck(mhContainer31, "ds11", false);
                doCheck(mhContainer32, "ds12", false);
                doCheck(mhContainer33, "ds33", true);
                doCheck(mhContainer34, "ds23", false);
                doCheck(mhContainer35, "ds35", true);

                // Folders
                doCheckFolder(mhRecordFolder41, "ds11", false);
                doCheckFolder(mhRecordFolder42, "ds12", false);
                doCheckFolder(mhRecordFolder43, "ds33", true);
                doCheckFolder(mhRecordFolder44, "ds23", false);
                doCheckFolder(mhRecordFolder45, "ds35", true);

                return null;
            }

            private void doCheck(NodeRef container, String dispositionInstructions, boolean isRecordLevel)
            {
                DispositionSchedule ds = dispositionService.getDispositionSchedule(container);
                assertNotNull(ds);
                checkDispositionSchedule(ds, dispositionInstructions, CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, isRecordLevel);
            }

            private void doCheckFolder(NodeRef container, String dispositionInstructions, boolean isRecordLevel)
            {
                doCheck(container, dispositionInstructions, isRecordLevel);
                if (!isRecordLevel)
                {
                    assertNotNull(dispositionService.getNextDispositionAction(container));
                }
            }
        });

    }

    /**
     * Checks a disposition schedule
     *
     * @param ds    disposition scheduleS
     */
    private void checkDispositionSchedule(DispositionSchedule ds, String dispositionInstructions, String dispositionAuthority, boolean isRecordLevel)
    {
        assertEquals(dispositionAuthority, ds.getDispositionAuthority());
        assertEquals(dispositionInstructions, ds.getDispositionInstructions());
        assertEquals(isRecordLevel, ds.isRecordLevelDisposition());

        List<DispositionActionDefinition> defs = ds.getDispositionActionDefinitions();
        assertNotNull(defs);
        assertEquals(2, defs.size());

        DispositionActionDefinition defCutoff = ds.getDispositionActionDefinitionByName("cutoff");
        assertNotNull(defCutoff);
        assertEquals("cutoff", defCutoff.getName());

        DispositionActionDefinition defDestroy = ds.getDispositionActionDefinitionByName("destroy");
        assertNotNull(defDestroy);
        assertEquals("destroy", defDestroy.getName());
    }

    /**
     *
     * @param ds
     */
    private void checkDispositionSchedule(DispositionSchedule ds, boolean isRecordLevel)
    {
        checkDispositionSchedule(ds, CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, isRecordLevel);
    }

    /**
     * @see DispositionService#getAssociatedDispositionSchedule(NodeRef)
     */
    public void testGetAssociatedDispositionSchedule() throws Exception
    {
        // Get associated disposition schedule for rmContainer
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Get the containers disposition schedule
                DispositionSchedule ds = dispositionService.getAssociatedDispositionSchedule(rmContainer);
                assertNotNull(ds);
                checkDispositionSchedule(ds, false);

                // Show the null disposition schedules
                assertNull(dispositionService.getAssociatedDispositionSchedule(filePlan));
                assertNull(dispositionService.getAssociatedDispositionSchedule(rmFolder));

                return null;
            }
        });

        // Failure: associated disposition schedule for non-rm node
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
                dispositionService.getAssociatedDispositionSchedule(folder);
            }
        });
    }

    /**
     * @see DispositionService#getAssociatedDispositionSchedule(NodeRef)
     */
    public void testGetAssociatedDispositionScheduleMultiHier() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertNull(dispositionService.getAssociatedDispositionSchedule(mhContainer));

                // Level 1
                doCheck(mhContainer11, "ds11", false);
                doCheck(mhContainer12, "ds12", false);

                // Level 2
                assertNull(dispositionService.getAssociatedDispositionSchedule(mhContainer21));
                assertNull(dispositionService.getAssociatedDispositionSchedule(mhContainer22));
                doCheck(mhContainer23, "ds23", false);

                // Level 3
                assertNull(dispositionService.getAssociatedDispositionSchedule(mhContainer31));
                assertNull(dispositionService.getAssociatedDispositionSchedule(mhContainer32));
                doCheck(mhContainer33, "ds33", true);
                assertNull(dispositionService.getAssociatedDispositionSchedule(mhContainer34));
                doCheck(mhContainer35, "ds35", true);

                return null;
            }

            private void doCheck(NodeRef container, String dispositionInstructions, boolean isRecordLevel)
            {
                DispositionSchedule ds = dispositionService.getAssociatedDispositionSchedule(container);
                assertNotNull(ds);
                checkDispositionSchedule(ds, dispositionInstructions, CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, isRecordLevel);
            }
        });
    }

    /**
     * @see DispositionService#hasDisposableItems(DispositionSchedule)
     */
    public void testHasDisposableItems() throws Exception
    {
    	doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
            	// Add a new disposition schedule
            	NodeRef container = filePlanService.createRecordCategory(rmContainer, "hasDisposableTest");
            	DispositionSchedule ds = utils.createBasicDispositionSchedule(container);

                assertTrue(dispositionService.hasDisposableItems(dispositionSchedule));
                assertFalse(dispositionService.hasDisposableItems(ds));

                return null;
            }
        });
    }

    /**
     * @see DispositionService#hasDisposableItems(DispositionSchedule)
     */
    public void testHasDisposableItemsMultiHier() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
            	assertTrue(dispositionService.hasDisposableItems(mhDispositionSchedule11));
            	assertTrue(dispositionService.hasDisposableItems(mhDispositionSchedule12));
            	assertTrue(dispositionService.hasDisposableItems(mhDispositionSchedule23));
            	assertFalse(dispositionService.hasDisposableItems(mhDispositionSchedule33));
            	assertFalse(dispositionService.hasDisposableItems(mhDispositionSchedule35));

                return null;
            }
        });
    }

    /**
     * @see DispositionService#getDisposableItems(DispositionSchedule)
     */
    public void testGetDisposableItems() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                List<NodeRef> nodeRefs = dispositionService.getDisposableItems(dispositionSchedule);
                assertNotNull(nodeRefs);
                assertEquals(1, nodeRefs.size());
                assertTrue(nodeRefs.contains(rmFolder));

                return null;
            }
        });
    }

    /**
     * @see DispositionService#getDisposableItems(DispositionSchedule)
     */
    public void testGetDisposableItemsMultiHier() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                List<NodeRef> nodeRefs = dispositionService.getDisposableItems(mhDispositionSchedule11);
                assertNotNull(nodeRefs);
                assertEquals(1, nodeRefs.size());
                assertTrue(nodeRefs.contains(mhRecordFolder41));

                nodeRefs = dispositionService.getDisposableItems(mhDispositionSchedule12);
                assertNotNull(nodeRefs);
                assertEquals(1, nodeRefs.size());
                assertTrue(nodeRefs.contains(mhRecordFolder42));

                nodeRefs = dispositionService.getDisposableItems(mhDispositionSchedule23);
                assertNotNull(nodeRefs);
                assertEquals(1, nodeRefs.size());
                assertTrue(nodeRefs.contains(mhRecordFolder44));

                nodeRefs = dispositionService.getDisposableItems(mhDispositionSchedule33);
                assertNotNull(nodeRefs);
                assertEquals(0, nodeRefs.size());

                nodeRefs = dispositionService.getDisposableItems(mhDispositionSchedule35);
                assertNotNull(nodeRefs);
                assertEquals(0, nodeRefs.size());

                return null;
            }
        });
    }

    /**
     * @see DispositionService#createDispositionSchedule(NodeRef, Map)
     */
    public void testCreateDispositionSchedule() throws Exception
    {
    	// Test: simple disposition create
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
            	// Create a new container
            	NodeRef container = filePlanService.createRecordCategory(filePlan, "testCreateDispositionSchedule");

            	// Create a new disposition schedule
            	utils.createBasicDispositionSchedule(container, "testCreateDispositionSchedule", "testCreateDispositionSchedule", false, true);

            	return container;
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
            	// Get the created disposition schedule
            	DispositionSchedule ds = dispositionService.getAssociatedDispositionSchedule(result);
            	assertNotNull(ds);

            	// Check the disposition schedule
            	checkDispositionSchedule(ds, "testCreateDispositionSchedule", "testCreateDispositionSchedule", false);
            }
        });

        // Failure: create disposition schedule on container with existing disposition schedule
        doTestInTransaction(new FailureTest
        (
        	"Can not create a disposition schedule on a container with an existing disposition schedule"
        )
        {
            @Override
            public void run()
            {
            	utils.createBasicDispositionSchedule(rmContainer);
            }
        });
    }

    /**
     * @see DispositionService#createDispositionSchedule(NodeRef, Map)
     */
    public void testCreateDispositionScheduleMultiHier() throws Exception
    {
    	// Test: simple disposition create
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
            	// Create a new structure container
            	NodeRef testA = filePlanService.createRecordCategory(mhContainer, "testA");
            	NodeRef testB = filePlanService.createRecordCategory(testA, "testB");

            	// Create new disposition schedules
            	utils.createBasicDispositionSchedule(testA, "testA", "testA", false, true);
            	utils.createBasicDispositionSchedule(testB, "testB", "testB", false, true);

            	// Add created containers to model
            	setNodeRef("testA", testA);
            	setNodeRef("testB", testB);

            	return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
            	// Get the created disposition schedule
            	DispositionSchedule testA = dispositionService.getAssociatedDispositionSchedule(getNodeRef("testA"));
            	assertNotNull(testA);
            	DispositionSchedule testB = dispositionService.getAssociatedDispositionSchedule(getNodeRef("testB"));
            	assertNotNull(testB);

            	// Check the disposition schedule
            	checkDispositionSchedule(testA, "testA", "testA", false);
            	checkDispositionSchedule(testB, "testB", "testB", false);
            }
        });

        // Failure: create disposition schedule on container with existing disposition schedule
        doTestInTransaction(new FailureTest
        (
        	"Can not create a disposition schedule on container with an existing disposition schedule"
        )
        {
            @Override
            public void run()
            {
            	utils.createBasicDispositionSchedule(mhContainer11);
            }
        });

        // Failure: create disposition schedule on a container where there are disposable items under management
        doTestInTransaction(new FailureTest
        (
        	"Can not create a disposition schedule on a container where there are already disposable items under management"
        )
        {
            @Override
            public void run()
            {
            	utils.createBasicDispositionSchedule(mhContainer21);
            }
        });
    }

    /**
     * @see DispositionService#getAssociatedRecordsManagementContainer(DispositionSchedule)
     */
    public void testGetAssociatedRecordsManagementContainer() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef nodeRef = dispositionService.getAssociatedRecordsManagementContainer(dispositionSchedule);
                assertNotNull(nodeRef);
                assertEquals(rmContainer, nodeRef);

                return null;
            }
        });
    }

    /**
     * @see DispositionService#getAssociatedRecordsManagementContainer(DispositionSchedule)
     */
    public void testGetAssociatedRecordsManagementContainerMultiHier() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef nodeRef = dispositionService.getAssociatedRecordsManagementContainer(mhDispositionSchedule11);
                assertNotNull(nodeRef);
                assertEquals(mhContainer11, nodeRef);

                nodeRef = dispositionService.getAssociatedRecordsManagementContainer(mhDispositionSchedule12);
                assertNotNull(nodeRef);
                assertEquals(mhContainer12, nodeRef);

                nodeRef = dispositionService.getAssociatedRecordsManagementContainer(mhDispositionSchedule23);
                assertNotNull(nodeRef);
                assertEquals(mhContainer23, nodeRef);

                nodeRef = dispositionService.getAssociatedRecordsManagementContainer(mhDispositionSchedule33);
                assertNotNull(nodeRef);
                assertEquals(mhContainer33, nodeRef);

                nodeRef = dispositionService.getAssociatedRecordsManagementContainer(mhDispositionSchedule35);
                assertNotNull(nodeRef);
                assertEquals(mhContainer35, nodeRef);

                return null;
            }
        });
    }

    // TODO DispositionActionDefinition addDispositionActionDefinition

    // TODO void removeDispositionActionDefinition(

    private NodeRef record43;
    private NodeRef record45;

    public void testUpdateDispositionActionDefinitionMultiHier() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                record43 = utils.createRecord(mhRecordFolder43, "record1.txt");
                record45 = utils.createRecord(mhRecordFolder45, "record2.txt");

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // Check all the current record folders first
                checkDisposableItemUnchanged(mhRecordFolder41);
                checkDisposableItemUnchanged(mhRecordFolder42);
                checkDisposableItemUnchanged(record43);
                checkDisposableItemUnchanged(mhRecordFolder44);
                checkDisposableItemUnchanged(record45);

                updateDispositionScheduleOnContainer(mhContainer11);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                // Check all the current record folders first
                checkDisposableItemChanged(mhRecordFolder41);
                checkDisposableItemUnchanged(mhRecordFolder42);
                checkDisposableItemUnchanged(record43);
                checkDisposableItemUnchanged(mhRecordFolder44);
                checkDisposableItemUnchanged(record45);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                updateDispositionScheduleOnContainer(mhContainer12);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                // Check all the current record folders first
                checkDisposableItemChanged(mhRecordFolder41);
                checkDisposableItemChanged(mhRecordFolder42);
                checkDisposableItemUnchanged(record43);
                checkDisposableItemUnchanged(mhRecordFolder44);
                checkDisposableItemUnchanged(record45);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                updateDispositionScheduleOnContainer(mhContainer33);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                // Check all the current record folders first
                checkDisposableItemChanged(mhRecordFolder41);
                checkDisposableItemChanged(mhRecordFolder42);
                checkDisposableItemChanged(record43);
                checkDisposableItemUnchanged(mhRecordFolder44);
                checkDisposableItemUnchanged(record45);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                updateDispositionScheduleOnContainer(mhContainer23);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                // Check all the current record folders first
                checkDisposableItemChanged(mhRecordFolder41);
                checkDisposableItemChanged(mhRecordFolder42);
                checkDisposableItemChanged(record43);
                checkDisposableItemChanged(mhRecordFolder44);
                checkDisposableItemUnchanged(record45);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                updateDispositionScheduleOnContainer(mhContainer35);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                // Check all the current record folders first
                checkDisposableItemChanged(mhRecordFolder41);
                checkDisposableItemChanged(mhRecordFolder42);
                checkDisposableItemChanged(record43);
                checkDisposableItemChanged(mhRecordFolder44);
                checkDisposableItemChanged(record45);
            }
        });
    }

    private void publishDispositionActionDefinitionChange(DispositionActionDefinition dad)
    {
        PublishExecutorRegistry reg = (PublishExecutorRegistry)applicationContext.getBean("publishExecutorRegistry");
        PublishExecutor pub = reg.get(RecordsManagementModel.UPDATE_TO_DISPOSITION_ACTION_DEFINITION);
        assertNotNull(pub);
        pub.publish(dad.getNodeRef());
    }

    private void checkDisposableItemUnchanged(NodeRef recordFolder)
    {
        checkDispositionAction(
                dispositionService.getNextDispositionAction(recordFolder),
                "cutoff",
                new String[]{CommonRMTestUtils.DEFAULT_EVENT_NAME},
                			 CommonRMTestUtils.PERIOD_NONE);
    }

    private void checkDisposableItemChanged(NodeRef recordFolder) throws Exception
    {
        checkDispositionAction(
                dispositionService.getNextDispositionAction(recordFolder),
                "cutoff",
                new String[]{CommonRMTestUtils.DEFAULT_EVENT_NAME, "abolished"},
                "week|1");
    }

    private void updateDispositionScheduleOnContainer(NodeRef nodeRef)
    {
        Map<QName, Serializable> updateProps = new HashMap<>(3);
        updateProps.put(PROP_DISPOSITION_PERIOD, "week|1");
        updateProps.put(PROP_DISPOSITION_EVENT, (Serializable)Arrays.asList(CommonRMTestUtils.DEFAULT_EVENT_NAME, "abolished"));

        DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
        DispositionActionDefinition dad = ds.getDispositionActionDefinitionByName("cutoff");
        dispositionService.updateDispositionActionDefinition(dad, updateProps);
        publishDispositionActionDefinitionChange(dad);
    }

    /**
     *
     * @param da
     * @param name
     * @param arrEventNames
     * @param strPeriod
     */
    private void checkDispositionAction(DispositionAction da, String name, String[] arrEventNames, String strPeriod)
    {
        assertNotNull(da);
        assertEquals(name, da.getName());

        List<EventCompletionDetails> events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(arrEventNames.length, events.size());

        List<String> origEvents = new ArrayList<>(events.size());
        for (EventCompletionDetails event : events)
        {
            origEvents.add(event.getEventName());
        }

        List<String> expectedEvents = Arrays.asList(arrEventNames);
        Collection<String> copy = new ArrayList<>(origEvents);

        for (Iterator<String> i = origEvents.iterator(); i.hasNext(); )
        {
            String origEvent = i.next();

            if (expectedEvents.contains(origEvent))
            {
                i.remove();
                copy.remove(origEvent);
            }
        }

        if (copy.size() != 0 && expectedEvents.size() != 0)
        {
            StringBuffer buff = new StringBuffer(255);
            if (copy.size() != 0)
            {
                buff.append("The following events where found, but not expected: (");
                for (String eventName : copy)
                {
                    buff.append(eventName).append(", ");
                }
                buff.append(").  ");
            }
            if (expectedEvents.size() != 0)
            {
                buff.append("The following events where not found, but expected: (");
                for (String eventName : expectedEvents)
                {
                    buff.append(eventName).append(", ");
                }
                buff.append(").");
            }
            fail(buff.toString());
        }

        if (CommonRMTestUtils.PERIOD_NONE.equals(strPeriod))
        {
            assertNull(da.getAsOfDate());
        }
        else
        {
            assertNotNull(da.getAsOfDate());
        }
    }

    // TODO boolean isNextDispositionActionEligible(NodeRef nodeRef);

    // TODO DispositionAction getNextDispositionAction(NodeRef nodeRef);

    // TODO List<DispositionAction> getCompletedDispositionActions(NodeRef nodeRef);

    // TODO DispositionAction getLastCompletedDispostionAction(NodeRef nodeRef);

    // TODO List<QName> getDispositionPeriodProperties();

    /* === Issues === */

    private NodeRef testRM263RecordCategory;
    private DispositionSchedule testRM263DispositionSchedule;
    private NodeRef testRM263Record;

    /**
     * https://issues.alfresco.com/jira/browse/RM-263
     */
    public void testRM_263() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                testRM263RecordCategory = filePlanService.createRecordCategory(rmContainer, "rm263");
                testRM263DispositionSchedule = utils.createBasicDispositionSchedule(
                        testRM263RecordCategory,
                        "test",
                        "test",
                        true,
                        false);

                Map<QName, Serializable> adParams = new HashMap<>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, "cutoff");
                adParams.put(PROP_DISPOSITION_DESCRIPTION, "test");
                adParams.put(PROP_DISPOSITION_PERIOD, "week|1");
                adParams.put(PROP_DISPOSITION_PERIOD_PROPERTY, DOD5015Model.PROP_PUBLICATION_DATE.toString());

                dispositionService.addDispositionActionDefinition(testRM263DispositionSchedule, adParams);

                NodeRef recordFolder = recordFolderService.createRecordFolder(testRM263RecordCategory, "testRM263RecordFolder");
                testRM263Record = utils.createRecord(recordFolder, "testRM263Record", "testRM263Record");

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            private final QName PROP_SEARCH_ASOF = QName.createQName(RM_URI, "recordSearchDispositionActionAsOf");

            @Override
            public Void run() throws Exception
            {
                Date pubDate = (Date)nodeService.getProperty(testRM263Record, DOD5015Model.PROP_PUBLICATION_DATE);
                assertNull(pubDate);
                Date asOfDate = (Date)nodeService.getProperty(testRM263Record, PROP_SEARCH_ASOF);
                assertNull(asOfDate);

                DispositionAction da = dispositionService.getNextDispositionAction(testRM263Record);
                assertNotNull(da);
                assertNull(da.getAsOfDate());

                //rma:recordSearchDispositionActionAsOf"
                nodeService.setProperty(testRM263Record, DOD5015Model.PROP_PUBLICATION_DATE, new Date());

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                Date pubDate = (Date)nodeService.getProperty(testRM263Record, DOD5015Model.PROP_PUBLICATION_DATE);
                assertNotNull(pubDate);
                Date asOfDate = (Date)nodeService.getProperty(testRM263Record, PROP_SEARCH_ASOF);
                assertNotNull(asOfDate);

                DispositionAction da = dispositionService.getNextDispositionAction(testRM263Record);
                assertNotNull(da);
                assertNotNull(da.getAsOfDate());
            }
        });
    }

    private NodeRef testRM386RecordCategory;
    private DispositionSchedule testRM386DispositionSchedule;
    private NodeRef testRM386Record;

    /**
     * Test to make sure all the search rollups are correct after schedule is updated
     * @throws Exception
     */
    public void testRM386() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                testRM386RecordCategory = filePlanService.createRecordCategory(rmContainer, "RM386");
                testRM386DispositionSchedule = utils.createBasicDispositionSchedule(
                        testRM386RecordCategory,
                        "disposition instructions",
                        "disposition authority",
                        true,   // record level
                        true);  // set the default actions

                NodeRef recordFolder = recordFolderService.createRecordFolder(testRM386RecordCategory, "testRM386RecordFolder");
                testRM386Record = utils.createRecord(recordFolder, "testRM386Record", "testRM386Record");

                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void test(Void result) throws Exception
            {
                // Test the rollups for the record
                Map<QName, Serializable> properties = nodeService.getProperties(testRM386Record);

                assertEquals(Boolean.TRUE, properties.get(PROP_RS_HAS_DISPOITION_SCHEDULE));
                assertEquals(CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, properties.get(PROP_RS_DISPOITION_AUTHORITY));
                assertEquals(CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, properties.get(PROP_RS_DISPOITION_INSTRUCTIONS));

                assertEquals("none", properties.get(PROP_RS_DISPOSITION_PERIOD));
                assertEquals("0", properties.get(PROP_RS_DISPOSITION_PERIOD_EXPRESSION));

                List<String> events = (List<String>)properties.get(PROP_RS_DISPOSITION_EVENTS);
                assertNotNull(events);
                assertEquals(1, events.size());
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, events.get(0));
                assertEquals(Boolean.FALSE, properties.get(PROP_RS_DISPOSITION_EVENTS_ELIGIBLE));

                assertEquals("cutoff", properties.get(PROP_RS_DISPOSITION_ACTION_NAME));
                assertNull(properties.get(PROP_RS_DISPOSITION_ACTION_AS_OF));
            }
        });

        doTestInTransaction(new Test<DispositionActionDefinition>()
        {
            @Override
            public DispositionActionDefinition run() throws Exception
            {
                DispositionActionDefinition actionDefinition = testRM386DispositionSchedule.getDispositionActionDefinitionByName("cutoff");
                assertNotNull( "Expected an action definition", actionDefinition);

                Map<QName, Serializable> adParams = new HashMap<>(3);

                List<String> events = new ArrayList<>(1);
                events.add(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                events.add("obsolete");
                adParams.put(PROP_DISPOSITION_EVENT, (Serializable)events);
                adParams.put(PROP_DISPOSITION_PERIOD, "week|1");

                dispositionService.updateDispositionActionDefinition(
                        actionDefinition,
                        adParams);

                return actionDefinition;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void test(DispositionActionDefinition result) throws Exception
            {
                DispositionActionDefinition actionDefinition = testRM386DispositionSchedule.getDispositionActionDefinitionByName("cutoff");
                assertNotNull(actionDefinition);
                assertTrue(nodeService.hasAspect(actionDefinition.getNodeRef(), ASPECT_UNPUBLISHED_UPDATE));

                // Publish the updates
                PublishUpdatesJobExecuter updater = (PublishUpdatesJobExecuter)applicationContext.getBean("publishUpdatesJobExecuter");
                updater.executeImpl();

                assertFalse(nodeService.hasAspect(actionDefinition.getNodeRef(), ASPECT_UNPUBLISHED_UPDATE));

                // Check the record has been updated
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(testRM386Record);
                assertNotNull(dispositionAction);
                assertEquals("cutoff", dispositionAction.getName());
                assertNotNull(dispositionAction.getAsOfDate());
                assertEquals(2, dispositionAction.getEventCompletionDetails().size());

                // Test the rollups for the record
                Map<QName, Serializable> properties = nodeService.getProperties(testRM386Record);

                assertEquals(Boolean.TRUE, properties.get(PROP_RS_HAS_DISPOITION_SCHEDULE));
                assertEquals(CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, properties.get(PROP_RS_DISPOITION_AUTHORITY));
                assertEquals(CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, properties.get(PROP_RS_DISPOITION_INSTRUCTIONS));

                assertEquals("week", properties.get(PROP_RS_DISPOSITION_PERIOD));
                assertEquals("1", properties.get(PROP_RS_DISPOSITION_PERIOD_EXPRESSION));

                List<String> events = (List<String>)properties.get(PROP_RS_DISPOSITION_EVENTS);
                assertNotNull(events);
                assertEquals(2, events.size());
                assertEquals(Boolean.FALSE, properties.get(PROP_RS_DISPOSITION_EVENTS_ELIGIBLE));

                assertEquals("cutoff", properties.get(PROP_RS_DISPOSITION_ACTION_NAME));
                assertNotNull(properties.get(PROP_RS_DISPOSITION_ACTION_AS_OF));
            }
        });

    }

}
