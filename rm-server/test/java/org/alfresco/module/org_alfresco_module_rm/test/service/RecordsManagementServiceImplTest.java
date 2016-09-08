/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;


/**
 * Records management service test.
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementServiceImplTest extends BaseRMTestCase
{    
    /********** RM Component methods **********/
    
    /**
     * @see RecordsManagementService#isFilePlanComponent(org.alfresco.service.cmr.repository.NodeRef)
     */
	@SuppressWarnings("deprecation")
    public void testIsFilePlanComponent() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                assertTrue("The rm root container should be a rm component", rmService.isFilePlanComponent(filePlan));
                assertTrue("The rm container should be a rm component", rmService.isFilePlanComponent(rmContainer));
                assertTrue("The rm folder should be a rm component", rmService.isFilePlanComponent(rmFolder));
                
                return null;
            }
        });
    }
    
    /**
     * @see RecordsManagementService#getFilePlanComponentKind(NodeRef)
     */
    @SuppressWarnings("deprecation")
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
                assertEquals(FilePlanComponentKind.FILE_PLAN, rmService.getFilePlanComponentKind(filePlan));
                assertEquals(FilePlanComponentKind.RECORD_CATEGORY, rmService.getFilePlanComponentKind(rmContainer));
                assertEquals(FilePlanComponentKind.RECORD_FOLDER, rmService.getFilePlanComponentKind(rmFolder));
                assertEquals(FilePlanComponentKind.RECORD, rmService.getFilePlanComponentKind(result));     
                // TODO HOLD and TRANSFER              
                assertNull(rmService.getFilePlanComponentKind(folder));
            }
        });        
    }
    
    /**
     * @see RecordsManagementService#isFilePlan(NodeRef)
     */
    @SuppressWarnings("deprecation")
    public void testIsFilePlan() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                assertTrue("This is a records management root", rmService.isFilePlan(filePlan));
                assertFalse("This should not be a records management root", rmService.isFilePlan(rmContainer));
                assertFalse("This should not be a records management root", rmService.isFilePlan(rmFolder));
                
                return null;
            }
        });
    }
    
    /**
     * @see RecordsManagementService#isRecordCategory(NodeRef)
     */
    @SuppressWarnings("deprecation")
    public void testIsRecordCategory() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                assertFalse("This should not be a record category.", rmService.isRecordCategory(filePlan));
                assertTrue("This is a record category.", rmService.isRecordCategory(rmContainer));
                assertFalse("This should not be a record category.", rmService.isRecordCategory(rmFolder));
                
                return null;
            }
        });
    }
    
    /**
     * @see RecordsManagementService#isRecordFolder(NodeRef)
     */
    public void testIsRecordFolder() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                assertFalse("This should not be a record folder", rmService.isRecordFolder(filePlan));
                assertFalse("This should not be a record folder", rmService.isRecordFolder(rmContainer));
                assertTrue("This should be a record folder", rmService.isRecordFolder(rmFolder));
                
                return null;
            }
        });
    }
    
    /**
     * @see RecordsManagementService#getRecordsManagementRoot()
     */
    public void testGetRecordsManagementRoot() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @SuppressWarnings("deprecation")
            @Override
            public NodeRef run()
            {
                assertEquals(filePlan, rmService.getFilePlan(filePlan));
                assertEquals(filePlan, rmService.getFilePlan(rmContainer));
                assertEquals(filePlan, rmService.getFilePlan(rmFolder));
                
                return null;
            }
        });
    }
    
    /********** Record Management Root methods **********/
    
    /**
     * @see RecordsManagementService#getFilePlans()
     */
    public void testGetRecordsManagementRoots() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @SuppressWarnings("deprecation")
            @Override
            public NodeRef run()
            {
                List<NodeRef> roots = rmService.getFilePlans();
                assertNotNull(roots);
                assertTrue(roots.size() != 0);
                assertTrue(roots.contains(filePlan)); 
                
                return null;
            }
        });      
    }
    
    /**
     * @see RecordsManagementService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, String)
     * @see RecordsManagementService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, String, org.alfresco.service.namespace.QName)
     */
    @SuppressWarnings("deprecation")
    public void testCreateFilePlan() throws Exception
    {
        // Create default type of root
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return rmService.createFilePlan(folder, id);
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
                return rmService.createFilePlan(folder, id, TYPE_FILE_PLAN);
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
                rmService.createFilePlan(rmContainer, GUID.generate());                                
            }
        });
        
        // Failure: type no extended from root container
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
                rmService.createFilePlan(folder, GUID.generate(), TYPE_FOLDER);                                
            }
        });
    }
    
    /********** Records Management Container methods **********/
    
    /**
     * @see RecordsManagementService#createRecordCategory(NodeRef, String)
     * @see RecordsManagementService#createRecordCategory(NodeRef, String, org.alfresco.service.namespace.QName)
     */
    @SuppressWarnings("deprecation")
    public void testCreateRecordCategory() throws Exception
    {
        // Create container (in root)
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return rmService.createRecordCategory(filePlan, id);
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
                return rmService.createRecordCategory(rmContainer, id);
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
                rmService.createRecordCategory(folder, GUID.generate());                                
            }
        });
        
        // Fail Test: type is not a sub-type of rm:recordsManagementContainer
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
                rmService.createRecordCategory(filePlan, GUID.generate(), TYPE_FOLDER);                                
            }
        });
    }
    
    /**
     * @see RecordsManagementService#getAllContained(NodeRef)
     * @see RecordsManagementService#getAllContained(NodeRef, boolean)
     */
    @SuppressWarnings("deprecation")
    public void testGetAllContained() throws Exception
    {
        // Get all contained test
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Add to the test data
                NodeRef series = rmService.createRecordCategory(rmContainer, "rmSeries");
                NodeRef seriesChildFolder = rmService.createRecordFolder(series, "seriesRecordFolder");
                NodeRef seriesChildContainer = rmService.createRecordCategory(series, "childContainer");
                
                // Put in model
                setNodeRef("series", series);
                setNodeRef("seriesChildFolder", seriesChildFolder);
                setNodeRef("seriesChildContainer", seriesChildContainer);
                
                return null;
            }
            
            @Override
            public void test(Void result) throws Exception
            {               
                List<NodeRef> nodes = rmService.getAllContained(rmContainer);
                assertNotNull(nodes);
                assertEquals(2, nodes.size());                
                assertTrue(nodes.contains(getNodeRef("series")));
                assertTrue(nodes.contains(rmFolder));
                
                nodes = rmService.getAllContained(rmContainer, false);
                assertNotNull(nodes);
                assertEquals(2, nodes.size());                
                assertTrue(nodes.contains(getNodeRef("series")));
                assertTrue(nodes.contains(rmFolder));
                
                nodes = rmService.getAllContained(rmContainer, true);
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
                rmService.getAllContained(rmFolder);
            }
        });       
    }
    
    /**
     * @see RecordsManagementService#getContainedRecordCategories(NodeRef)
     * @see RecordsManagementService#getContainedRecordCategories(NodeRef, boolean)
     */
    @SuppressWarnings("deprecation")
    public void testGetContainedRecordCategories() throws Exception
    {
        // Test getting all contained containers
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Add to the test data
                NodeRef series = rmService.createRecordCategory(rmContainer, "rmSeries");
                NodeRef seriesChildFolder = rmService.createRecordFolder(series, "seriesRecordFolder");
                NodeRef seriesChildContainer = rmService.createRecordCategory(series, "childContainer");
                
                // Put in model
                setNodeRef("series", series);
                setNodeRef("seriesChildFolder", seriesChildFolder);
                setNodeRef("seriesChildContainer", seriesChildContainer);
                
                return null;
            }
            
            @Override
            public void test(Void result) throws Exception
            {               
                List<NodeRef> nodes = rmService.getContainedRecordCategories(rmContainer);
                assertNotNull(nodes);
                assertEquals(1, nodes.size()); 
                assertTrue(nodes.contains(getNodeRef("series")));      
                
                nodes = rmService.getContainedRecordCategories(rmContainer, false);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());       
                assertTrue(nodes.contains(getNodeRef("series")));
                
                nodes = rmService.getContainedRecordCategories(rmContainer, true);
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
                rmService.getContainedRecordCategories(rmFolder);
            }
        });         
    }
    
    /**
     * @see RecordsManagementService#getContainedRecordFolders(NodeRef)
     * @see RecordsManagementService#getContainedRecordFolders(NodeRef, boolean)
     */
    @SuppressWarnings("deprecation")
    public void testGetContainedRecordFolders() throws Exception
    {
        // Test getting all contained record folders
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Add to the test data
                NodeRef series = rmService.createRecordCategory(rmContainer, "rmSeries");
                NodeRef seriesChildFolder = rmService.createRecordFolder(series, "seriesRecordFolder");
                NodeRef seriesChildContainer = rmService.createRecordCategory(series, "childContainer");
                
                // Put in model
                setNodeRef("series", series);
                setNodeRef("seriesChildFolder", seriesChildFolder);
                setNodeRef("seriesChildContainer", seriesChildContainer);
                
                return null;
            }
            
            @Override
            public void test(Void result) throws Exception
            {               
                List<NodeRef> nodes = rmService.getContainedRecordFolders(rmContainer);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());              
                assertTrue(nodes.contains(rmFolder));           
                
                nodes = rmService.getContainedRecordFolders(rmContainer, false);
                assertNotNull(nodes);
                assertEquals(1, nodes.size());                      
                assertTrue(nodes.contains(rmFolder));   
                
                nodes = rmService.getContainedRecordFolders(rmContainer, true);
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
                rmService.getContainedRecordFolders(rmFolder);
            }
        });       
    }
    
    /********** Record Folder methods **********/    
    
    // TODO void testIsRecordFolderDeclared()
  
    // TODO void testIsRecordFolderClosed()
    
    // TODO void testGetRecords()
    
    /**
     * @see RecordsManagementService#createRecordFolder(NodeRef, String)
     * @see RecordsManagementService#createRecordFolder(NodeRef, String, QName)
     */
    public void testCreateRecordFolder() throws Exception
    {
        // Create record 
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                String id = setString("id", GUID.generate());
                return rmService.createRecordFolder(rmContainer, id);
            }

            @Override
            public void test(NodeRef result)
            {
                assertNotNull("Unable to create record folder", result);
                basicRMContainerCheck(result, getString("id"), TYPE_RECORD_FOLDER);
            }
        });
        
        // TODO Create record of type
        
        // Failure: Create record with invalid type
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
                rmService.createRecordFolder(rmContainer, GUID.generate(), TYPE_FOLDER);                                
            }
        });
        
        // Failure: Create record folder in root
        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run()
            {
                rmService.createRecordFolder(filePlan, GUID.generate());                                
            }
        });
    }
    

    /********** RM2 - Multi-hierarchy record taxonomy's **********/
    
    /**
     * Test to create a simple multi-hierarchy record taxonomy  
     */
    @SuppressWarnings("deprecation")
    public void testCreateSimpleHierarchy()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Create 3 level hierarchy
                NodeRef levelOne = setNodeRef("container1", rmService.createRecordCategory(filePlan, "container1"));    
                assertNotNull("Unable to create container", levelOne);
                NodeRef levelTwo = setNodeRef("container2", rmService.createRecordCategory(levelOne, "container2"));
                assertNotNull("Unable to create container", levelTwo);
                NodeRef levelThree = setNodeRef("container3", rmService.createRecordCategory(levelTwo, "container3"));
                assertNotNull("Unable to create container", levelThree);
                NodeRef levelThreeRecordFolder = setNodeRef("recordFolder3", rmService.createRecordFolder(levelThree, "recordFolder3"));
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
    
}
