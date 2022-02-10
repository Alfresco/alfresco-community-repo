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
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSet;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Dataset Service Test
 * 
 * @author Tuna Aksoy
 * @since 2.1
 */
public class DataSetServiceImplTest extends BaseRMTestCase
{
   /** Id of the test data set*/
   private static final String DATA_SET_ID = "testExampleData"; 

   /**
    * @see DataSetService#getDataSets()
    */
   public void testGetDataSets() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            Map<String, DataSet> dataSets = dataSetService.getDataSets();
            // Test the data sets
            assertNotNull(dataSets);
            // At least the test data set must exist
            assertTrue(dataSets.size() >= 1);
            // The test data set must be in the list of available data sets
            assertNotNull(dataSets.get(DATA_SET_ID));

            for (Map.Entry<String, DataSet> entry : dataSets.entrySet())
            {
               // Test the key
               String key = entry.getKey();
               assertNotNull(key);

               // Test the value
               DataSet dataSet = entry.getValue();
               assertNotNull(dataSet);

               // Test the id
               String id = dataSet.getId();
               assertNotNull(id);
               assertEquals(id, key);

               // Test the label
               String label = dataSet.getLabel();
               assertNotNull(label);

               // Test the path
               String path = dataSet.getPath();
               assertNotNull(path);
            }

            return null;
         }
      });
   }

   /**
    * @see DataSetService#loadDataSet(String, org.alfresco.service.cmr.repository.NodeRef)
    */
   public void testLoadDataSet() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            // Test the file plan before importing the data sets
            testFilePlanBeforeImportingDataSet();

            // Load the data set into the specified file plan
            dataSetService.loadDataSet(filePlan, DATA_SET_ID);

            // Test the file plan after importing the data sets
            testFilePlanAfterImportingDataSet();

            return null;
         }

         /**
          * Helper method for testing the test file plan before importing the data
          */
         private void testFilePlanBeforeImportingDataSet()
         {
            // There should not be any categories before importing
            assertNull(nodeService.getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, "TestRecordCategory1"));
            assertNull(nodeService.getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, "TestRecordCategory2"));

            // The aspect should exist before loading a data set
            assertNull(nodeService.getProperty(filePlan, PROP_LOADED_DATA_SET_IDS));

            // At the beginning the file plan is empty. So there should not be any data sets
            assertTrue(dataSetService.getLoadedDataSets(filePlan).size() == 0);
            assertFalse(dataSetService.isLoadedDataSet(filePlan, DATA_SET_ID));
            assertTrue(dataSetService.getDataSets(filePlan, true).size() > 0);
            assertTrue(dataSetService.getDataSets(filePlan, false).size() > 0);
         }

         /**
          * Helper method for testing the test file plan after importing the data
          */
         private void testFilePlanAfterImportingDataSet()
         {
            // Test the "first level" categories after importing if they exist
            // TestRecordCategory1
            NodeRef recCat1 = nodeService.getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, "TestRecordCategory1");
            assertNotNull(recCat1);
            List<NodeRef> recCat1ImmediateChildren = filePlanService.getAllContained(recCat1);
            assertTrue(recCat1ImmediateChildren.size() == 3);
            List<NodeRef> recCat1AllChildren = filePlanService.getAllContained(recCat1, true);
            assertTrue(recCat1AllChildren.size() == 6);
            DispositionSchedule recCat1DispositionSchedule = dispositionService.getDispositionSchedule(recCat1);
            assertNull(recCat1DispositionSchedule);

            // TestRecordCategory2
            NodeRef recCat2 = nodeService.getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, "TestRecordCategory2");
            assertNotNull(recCat2);
            List<NodeRef> recCat2ImmediateChildren = filePlanService.getAllContained(recCat2);
            assertTrue(recCat2ImmediateChildren.size() == 2);
            List<NodeRef> recCat2AllChildren = filePlanService.getAllContained(recCat2, true);
            assertTrue(recCat2AllChildren.size() == 4);
            DispositionSchedule recCat2DispositionSchedule = dispositionService.getDispositionSchedule(recCat2);
            assertNull(recCat2DispositionSchedule);

            // TestRecordCategory1: Test the "second level" categories and record folders
            NodeRef recCat11 = nodeService.getChildByName(recCat1, ContentModel.ASSOC_CONTAINS, "TestRecordCategory11");
            assertNotNull(recCat11);
            List<NodeRef> recCat11ImmediateChilderen = filePlanService.getAllContained(recCat11);
            assertTrue(recCat11ImmediateChilderen.size() == 2);
            List<NodeRef> recCat11Childeren = filePlanService.getAllContained(recCat11, true);
            assertTrue(recCat11Childeren.size() == 2);
            assertNotNull(nodeService.getChildByName(recCat11, ContentModel.ASSOC_CONTAINS, "TestRecordFolder1"));
            assertNotNull(nodeService.getChildByName(recCat11, ContentModel.ASSOC_CONTAINS, "TestRecordFolder2"));
            VitalRecordDefinition recCat11VitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(recCat11);
            assertNotNull(recCat11VitalRecordDefinition);
            assertTrue(recCat11VitalRecordDefinition.getReviewPeriod().getExpression().equals("1"));
            assertNotNull(recCat11VitalRecordDefinition.getNextReviewDate());

            NodeRef recCat12 = nodeService.getChildByName(recCat1, ContentModel.ASSOC_CONTAINS, "TestRecordCategory12");
            assertNotNull(recCat12);
            List<NodeRef> recCat12ImmediateChildren = filePlanService.getAllContained(recCat12);
            assertTrue(recCat12ImmediateChildren.size() == 1);
            List<NodeRef> recCat12Children = filePlanService.getAllContained(recCat12, true);
            assertTrue(recCat12Children.size() == 1);
            assertNotNull(nodeService.getChildByName(recCat12, ContentModel.ASSOC_CONTAINS, "TestRecordFolder3"));
            DispositionSchedule recCat12DispositionSchedule = dispositionService.getDispositionSchedule(recCat12);
            assertNotNull(recCat12DispositionSchedule);
            assertTrue(recCat12DispositionSchedule.getDispositionInstructions().equals("Cut off every 3 months, hold 3 months, then destroy."));
            assertTrue(recCat12DispositionSchedule.getDispositionAuthority().equals("T0-000-00-1 item 002"));
            assertTrue(recCat12DispositionSchedule.getDispositionActionDefinitions().size() == 2);
            assertNotNull(recCat12DispositionSchedule.getDispositionActionDefinitionByName("cutoff"));
            assertNotNull(recCat12DispositionSchedule.getDispositionActionDefinitionByName("destroy"));

            NodeRef recCat13 = nodeService.getChildByName(recCat1, ContentModel.ASSOC_CONTAINS, "TestRecordCategory13");
            assertNotNull(recCat13);
            List<NodeRef> recCat13ImmediateChildren = filePlanService.getAllContained(recCat13);
            assertTrue(recCat13ImmediateChildren.size() == 0);
            DispositionSchedule recCat13DispositionSchedule = dispositionService.getDispositionSchedule(recCat13);
            assertNotNull(recCat13DispositionSchedule);

            // TestRecordCategory2: Test the "second level" categories and record folders
            NodeRef recCat21 = nodeService.getChildByName(recCat2, ContentModel.ASSOC_CONTAINS, "TestRecordCategory21");
            assertNotNull(recCat21);
            List<NodeRef> recCat21ImmediateChildren = filePlanService.getAllContained(recCat21);
            assertTrue(recCat21ImmediateChildren.size() == 0);
            DispositionSchedule recCat21DispositionSchedule = dispositionService.getDispositionSchedule(recCat21);
            assertNotNull(recCat21DispositionSchedule);

            NodeRef recCat22 = nodeService.getChildByName(recCat2, ContentModel.ASSOC_CONTAINS, "TestRecordCategory22");
            assertNotNull(recCat22);
            List<NodeRef> recCat22ImmediateChildren = filePlanService.getAllContained(recCat22);
            assertTrue(recCat22ImmediateChildren.size() == 2);
            List<NodeRef> recCat22Children = filePlanService.getAllContained(recCat22, true);
            assertTrue(recCat22Children.size() == 2);
            assertNotNull(nodeService.getChildByName(recCat22, ContentModel.ASSOC_CONTAINS, "TestRecordFolder4"));
            assertNotNull(nodeService.getChildByName(recCat22, ContentModel.ASSOC_CONTAINS, "TestRecordFolder5"));
            DispositionSchedule recCat22DispositionSchedule = dispositionService.getDispositionSchedule(recCat22);
            assertNotNull(recCat22DispositionSchedule);

            // After loading the data set into the file plan the custom aspect should contain the id of the loaded data set 
            Serializable nodeProperty = nodeService.getProperty(filePlan, PROP_LOADED_DATA_SET_IDS);
            assertNotNull(nodeProperty);
            @SuppressWarnings("unchecked")
            ArrayList<String> loadedDataSetIds = (ArrayList<String>)nodeProperty;
            assertTrue(loadedDataSetIds.size() == 1);
            assertTrue(loadedDataSetIds.contains(DATA_SET_ID));

            // The data set has been loaded into the file plan, so the file plan should contain the data set id
            Map<String, DataSet> loadedDataSets = dataSetService.getLoadedDataSets(filePlan);
            assertTrue(loadedDataSets.size() == 1);
            assertTrue(loadedDataSets.containsKey(DATA_SET_ID));
            assertTrue(dataSetService.isLoadedDataSet(filePlan, DATA_SET_ID));
            assertTrue(dataSetService.getDataSets(filePlan, true).size() > 0);
            assertTrue(dataSetService.getDataSets(filePlan, false).size() > 1);
         }
      });
   }

   /**
    * @see DataSetService#existsDataSet(String)
    */
   public void testExistsDataSet() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            // Test if a data set with the specified data set id exists
            assertTrue(dataSetService.existsDataSet(DATA_SET_ID));
            assertFalse(dataSetService.existsDataSet("AnotherDataSetId"));

            return null;
         }
      });
   }

   /*
    * INFO:
    * 
    * The tests for
    * 
    * DataSetService#getDataSets(NodeRef, boolean)
    * DataSetService#getLoadedDataSets(NodeRef)
    * DataSetService#isLoadedDataSet(NodeRef, String)
    * 
    * will be executed in testFilePlanBeforeImportingDataSet() and testFilePlanAfterImportingDataSet().
    */

}
