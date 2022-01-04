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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * File plan service unit test
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanServiceImplTest extends BaseRMTestCase
{
    /**
     * Pull in collaboration test data
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * {@link FilePlanService#isFilePlan(org.alfresco.service.cmr.repository.NodeRef)}
     */
    public void testIsFilePlan()
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl() throws Exception
            {
                assertTrue(filePlanService.isFilePlan(filePlan));
                assertFalse(filePlanService.isFilePlan(rmContainer));
                assertFalse(filePlanService.isFilePlan(dmDocument));
            }
        });
    }

    /**
     * {@link FilePlanService#getFilePlan(org.alfresco.service.cmr.repository.NodeRef)}
     */
    public void testGetFilePlans()
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl() throws Exception
            {
                assertEquals(filePlan, filePlanService.getFilePlan(filePlan));
                assertEquals(filePlan, filePlanService.getFilePlan(rmContainer));
                assertEquals(filePlan, filePlanService.getFilePlan(rmFolder));
                assertNull(filePlanService.getFilePlan(dmDocument));
            }
        });
    }

    /**
     * {@link FilePlanService#getFilePlanBySiteId(String)}
     */
    public void testGetFilePlanBySiteId()
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl() throws Exception
            {
                assertEquals(filePlan, filePlanService.getFilePlanBySiteId(siteId));
                assertNull(filePlanService.getFilePlanBySiteId("rubbish"));

                String siteId = GUID.generate();
                siteService.createSite("anything", siteId, "title", "descrition", SiteVisibility.PUBLIC);
                assertNull(filePlanService.getFilePlanBySiteId(siteId));
            }
        });

    }

    /**
     * @see FilePlanService#isFilePlanComponent(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testIsFilePlanComponent() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                assertTrue("The rm root container should be a rm component", filePlanService.isFilePlanComponent(filePlan));
                assertTrue("The rm container should be a rm component", filePlanService.isFilePlanComponent(rmContainer));
                assertTrue("The rm folder should be a rm component", filePlanService.isFilePlanComponent(rmFolder));

                return null;
            }
        });
    }

    /**
     * @see FilePlanService#getFilePlanComponentKind(NodeRef)
     */
    public void testGetFilePlanComponentKind() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                return utils.createRecord(rmFolder, "testRecord.txt");
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertEquals(FilePlanComponentKind.FILE_PLAN, filePlanService.getFilePlanComponentKind(filePlan));
                assertEquals(FilePlanComponentKind.RECORD_CATEGORY, filePlanService.getFilePlanComponentKind(rmContainer));
                assertEquals(FilePlanComponentKind.RECORD_FOLDER, filePlanService.getFilePlanComponentKind(rmFolder));
                assertEquals(FilePlanComponentKind.RECORD, filePlanService.getFilePlanComponentKind(result));
                // TODO HOLD and TRANSFER
                assertNull(filePlanService.getFilePlanComponentKind(folder));
            }
        });
    }

    /**
     * @see FilePlanService#isRecordCategory(NodeRef)
     */
    public void testIsRecordCategory() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                assertFalse("This should not be a record category.", filePlanService.isRecordCategory(filePlan));
                assertTrue("This is a record category.", filePlanService.isRecordCategory(rmContainer));
                assertFalse("This should not be a record category.", filePlanService.isRecordCategory(rmFolder));

                return null;
            }
        });
    }

    /**
     * @see FilePlanService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, String)
     * @see FilePlanService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, String, org.alfresco.service.namespace.QName)
     */
    public void testCreateFilePlan() throws Exception
    {
        // Create default type of root
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return filePlanService.createFilePlan(folder, id);
            }

            @Override
            public void test(NodeRef result)
            {
                assertNotNull("Unable to create records management root", result);
                basicRMContainerCheck(result, getString("id"), TYPE_FILE_PLAN);
            }
        });

        // Create specific type of root
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return filePlanService.createFilePlan(folder, id, TYPE_FILE_PLAN);
            }

            @Override
            public void test(NodeRef result)
            {
                assertNotNull("Unable to create records management root", result);
                basicRMContainerCheck(result, getString("id"), TYPE_FILE_PLAN);
            }
        });

        // Failure: creating root in existing hierarchy
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.createFilePlan(rmContainer, GUID.generate());
            }
        });

        // Failure: type no extended from root container
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.createFilePlan(folder, GUID.generate(), TYPE_FOLDER);
            }
        });
    }

    /**
     * A basic test of a records management container
     *
     * @param nodeRef   node reference
     * @param name      name of the container
     * @param type      the type of container
     */
    private void basicRMContainerCheck(NodeRef nodeRef, String name, QName type)
    {
        // Check the basic details
        assertEquals(name, nodeService.getProperty(nodeRef, PROP_NAME));
        assertNotNull("RM id has not been set", nodeService.getProperty(nodeRef, PROP_IDENTIFIER));
        assertEquals(type, nodeService.getType(nodeRef));
    }

    /**
     * @see FilePlanService#createRecordCategory(NodeRef, String)
     * @see FilePlanService#createRecordCategory(NodeRef, String, org.alfresco.service.namespace.QName)
     */
    public void testCreateRecordCategory() throws Exception
    {
        // Create container (in root)
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return filePlanService.createRecordCategory(filePlan, id);
            }

            @Override
            public void test(NodeRef result)
            {
                assertNotNull("Unable to create records management container", result);
                basicRMContainerCheck(result, getString("id"), TYPE_RECORD_CATEGORY);
            }
        });

        // Create container (in container)
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return filePlanService.createRecordCategory(rmContainer, id);
            }

            @Override
            public void test(NodeRef result)
            {
                assertNotNull("Unable to create records management container", result);
                basicRMContainerCheck(result, getString("id"), TYPE_RECORD_CATEGORY);
            }
        });

        // TODO need a custom type of container!
        // Create container of a given type
//        doTestInTransaction(new Test<NodeRef>()
//        {
//            @Override
//            public NodeRef run()
//            {
//                String id = setString("id", GUID.generate());
//                return rmService.createRecordCategory(filePlan, id, TYPE_RECORD_SERIES);
//            }
//
//            @Override
//            public void test(NodeRef result)
//            {
//                assertNotNull("Unable to create records management container", result);
//                basicRMContainerCheck(result, getString("id"), TYPE_RECORD_SERIES);
//            }
//        });

        // Fail Test: parent is not a container
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.createRecordCategory(folder, GUID.generate());
            }
        });

        // Fail Test: type is not a sub-type of rm:recordsManagementContainer
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.createRecordCategory(filePlan, GUID.generate(), TYPE_FOLDER);
            }
        });
    }

    /**
     * @see FilePlanService#getAllContained(NodeRef)
     * @see FilePlanService#getAllContained(NodeRef, boolean)
     */
    public void testGetAllContained() throws Exception
    {
        // Get all contained test
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Add to the test data
                NodeRef series = filePlanService.createRecordCategory(rmContainer, "rmSeries");
                NodeRef seriesChildFolder = recordFolderService.createRecordFolder(series, "seriesRecordFolder");
                NodeRef seriesChildContainer = filePlanService.createRecordCategory(series, "childContainer");

                // Put in model
                setNodeRef("series", series);
                setNodeRef("seriesChildFolder", seriesChildFolder);
                setNodeRef("seriesChildContainer", seriesChildContainer);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                List<NodeRef> nodes = filePlanService.getAllContained(rmContainer);
                assertNotNull(nodes);
                assertEquals(2, nodes.size());
                assertTrue(nodes.contains(getNodeRef("series")));
                assertTrue(nodes.contains(rmFolder));

                nodes = filePlanService.getAllContained(rmContainer, false);
                assertNotNull(nodes);
                assertEquals(2, nodes.size());
                assertTrue(nodes.contains(getNodeRef("series")));
                assertTrue(nodes.contains(rmFolder));

                nodes = filePlanService.getAllContained(rmContainer, true);
                assertNotNull(nodes);
                assertEquals(4, nodes.size());
                assertTrue(nodes.contains(getNodeRef("series")));
                assertTrue(nodes.contains(rmFolder));
                assertTrue(nodes.contains(getNodeRef("seriesChildFolder")));
                assertTrue(nodes.contains(getNodeRef("seriesChildContainer")));

            }
        });

        // Failure: call on record folder
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.getAllContained(rmFolder);
            }
        });
    }

    /**
     * @see FilePlanService#getContainedRecordCategories(NodeRef)
     * @see FilePlanService#getContainedRecordCategories(NodeRef, boolean)
     */
    public void testGetContainedRecordCategories() throws Exception
    {
        // Test getting all contained containers
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Add to the test data
                NodeRef series = filePlanService.createRecordCategory(rmContainer, "rmSeries");
                NodeRef seriesChildFolder = recordFolderService.createRecordFolder(series, "seriesRecordFolder");
                NodeRef seriesChildContainer = filePlanService.createRecordCategory(series, "childContainer");

                // Put in model
                setNodeRef("series", series);
                setNodeRef("seriesChildFolder", seriesChildFolder);
                setNodeRef("seriesChildContainer", seriesChildContainer);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                List<NodeRef> nodes = filePlanService.getContainedRecordCategories(rmContainer);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());
                assertTrue(nodes.contains(getNodeRef("series")));

                nodes = filePlanService.getContainedRecordCategories(rmContainer, false);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());
                assertTrue(nodes.contains(getNodeRef("series")));

                nodes = filePlanService.getContainedRecordCategories(rmContainer, true);
                assertNotNull(nodes);
                assertEquals(2, nodes.size());
                assertTrue(nodes.contains(getNodeRef("series")));
                assertTrue(nodes.contains(getNodeRef("seriesChildContainer")));
            }
        });

        // Failure: call on record folder
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.getContainedRecordCategories(rmFolder);
            }
        });
    }

    /**
     * @see FilePlanService#getContainedRecordFolders(NodeRef)
     * @see FilePlanService#getContainedRecordFolders(NodeRef, boolean)
     */
    public void testGetContainedRecordFolders() throws Exception
    {
        // Test getting all contained record folders
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Add to the test data
                NodeRef series = filePlanService.createRecordCategory(rmContainer, "rmSeries");
                NodeRef seriesChildFolder = recordFolderService.createRecordFolder(series, "seriesRecordFolder");
                NodeRef seriesChildContainer = filePlanService.createRecordCategory(series, "childContainer");

                // Put in model
                setNodeRef("series", series);
                setNodeRef("seriesChildFolder", seriesChildFolder);
                setNodeRef("seriesChildContainer", seriesChildContainer);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                List<NodeRef> nodes = filePlanService.getContainedRecordFolders(rmContainer);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());
                assertTrue(nodes.contains(rmFolder));

                nodes = filePlanService.getContainedRecordFolders(rmContainer, false);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());
                assertTrue(nodes.contains(rmFolder));

                nodes = filePlanService.getContainedRecordFolders(rmContainer, true);
                assertNotNull(nodes);
                assertEquals(2, nodes.size());
                assertTrue(nodes.contains(rmFolder));
                assertTrue(nodes.contains(getNodeRef("seriesChildFolder")));
            }
        });

        // Failure: call on record folder
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
            	filePlanService.getContainedRecordFolders(rmFolder);
            }
        });
    }

    /**
     * Test to create a simple multi-hierarchy record taxonomy
     */
    public void testCreateSimpleHierarchy()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Create 3 level hierarchy
                NodeRef levelOne = setNodeRef("container1", filePlanService.createRecordCategory(filePlan, "container1"));
                assertNotNull("Unable to create container", levelOne);
                NodeRef levelTwo = setNodeRef("container2", filePlanService.createRecordCategory(levelOne, "container2"));
                assertNotNull("Unable to create container", levelTwo);
                NodeRef levelThree = setNodeRef("container3", filePlanService.createRecordCategory(levelTwo, "container3"));
                assertNotNull("Unable to create container", levelThree);
                NodeRef levelThreeRecordFolder = setNodeRef("recordFolder3", recordFolderService.createRecordFolder(levelThree, "recordFolder3"));
                assertNotNull("Unable to create record folder", levelThreeRecordFolder);

                return null;
            }

            @Override
            public void test(Void result)
            {
                // Test that the hierarchy has been created correctly
                basicRMContainerCheck(getNodeRef("container1"), "container1", TYPE_RECORD_CATEGORY);
                basicRMContainerCheck(getNodeRef("container2"), "container2", TYPE_RECORD_CATEGORY);
                basicRMContainerCheck(getNodeRef("container3"), "container3", TYPE_RECORD_CATEGORY);
                basicRMContainerCheck(getNodeRef("recordFolder3"), "recordFolder3", TYPE_RECORD_FOLDER);

                // TODO need to check that the parents and children can be retrieved correctly
            }
        });
    }
}
