package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSet;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;

public class DataSetServiceImplTest extends BaseRMTestCase
{
   /** Services */
   protected DataSetService dataSetService;
   protected ImporterService importerService;

   /** List of data set ids */
   private List<String> dataSetIds;

   /** Constants for data set ids */
   private static final String DATA_SET_ID_DOD5015 = "dod5015";

   /** Enum for checking the condition */
   private enum Condition
   {
      BEFORE, // Test a file plan before a data set has been imported
      AFTER; // Test a file plan after a data set has been imported
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      // Add the id's of the data sets to the list
      dataSetIds = new ArrayList<String>();
      dataSetIds.add(DATA_SET_ID_DOD5015);
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
    */
   @Override
   protected void initServices()
   {
      super.initServices();

      // Get Services
      dataSetService = (DataSetService) applicationContext.getBean("DataSetService");
      importerService = (ImporterService) applicationContext.getBean("ImporterService");
   }

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
            // Test the data sets
            Map<String, DataSet> dataSets = dataSetService.getDataSets();
            assertNotNull(dataSets);

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
            // Test filePlan
            assertNotNull(filePlan);

            for (String dataSetId : dataSetIds)
            {
               // Get a data set and test it
               DataSet dataSet = dataSetService.getDataSets().get(dataSetId);
               assertNotNull(dataSet);

               // Import the RM test data ACP into the the provided file plan node reference
               String path = dataSet.getPath();
               assertNotNull(path);
               InputStream is = null;
               is = getClass().getClassLoader().getResourceAsStream(path);
               assertNotNull(is);

               // Get view reader and test it
               Reader viewReader = new InputStreamReader(is);
               assertNotNull(viewReader);

               // Get location and test it
               Location location = new Location(filePlan);
               assertNotNull(location);

               // Test the file plan before importing the data sets
               testFilePlan(filePlan, dataSetId, Condition.BEFORE);

               // Import view
               importerService.importView(viewReader, location, null, null);

               // Test the file plan after importing the data sets
               testFilePlan(filePlan, dataSetId, Condition.AFTER);

               // Close the input stream
               is.close();
               is = null;
            }

            return null;
         }

         /**
          * Helper method for testing the file plan
          * 
          * @param filePlan The NodeRef of the filePlan which is to test
          * @param dataSetId The id of the data set which is to import / has been imported
          * @param condition Indicates whether the filePlan should be tested before or after importing the test data
          */
         private void testFilePlan(NodeRef filePlan, String dataSetId, Condition condition)
         {
            if (dataSetId.equals(DATA_SET_ID_DOD5015))
            {
               switch (condition)
               {
                  case BEFORE:
                     testFilePlanBeforeImportingDOD5015DataSet(filePlan);
                     break;

                  case AFTER:
                     testFilePlanAfterImportingDOD5015DataSet(filePlan);
                     break;
               }
            }
         }

         /**
          * Helper method for testing the file plan before importing the DOD5015 data
          * 
          * @param filePlan The NodeRef of the filePlan which is to test
          */
         private void testFilePlanBeforeImportingDOD5015DataSet(NodeRef filePlan)
         {
            // There should not be any categories before importing
            assertNull(getRecordCategory(filePlan, "Civilian Files"));
            assertNull(getRecordCategory(filePlan, "Military Files"));
            assertNull(getRecordCategory(filePlan, "Miscellaneous Files"));
            assertNull(getRecordCategory(filePlan, "Reports"));
         }

         /**
          * Helper method for testing the file plan after importing the DOD5015 data
          * 
          * @param filePlan The NodeRef of the filePlan which is to test
          */
         private void testFilePlanAfterImportingDOD5015DataSet(NodeRef filePlan)
         {
            // Test the "first level" categories after importing if they exist
            NodeRef civFil = getRecordCategory(filePlan, "Civilian Files");
            assertNotNull(civFil);
            NodeRef milFil = getRecordCategory(filePlan, "Military Files");
            assertNotNull(milFil);
            NodeRef misFil = getRecordCategory(filePlan, "Miscellaneous Files");
            assertNotNull(misFil);
            NodeRef rep = getRecordCategory(filePlan, "Reports");
            assertNotNull(rep);


            // Civilian Files: Test the "second level" categories and record folders
            NodeRef civFil1 = getRecordCategory(civFil, "Case Files and Papers");
            assertNotNull(civFil1);
            assertNotNull(getRecordFolder(civFil1, "Gilbert Competency Hearing"));

            NodeRef civFil2 = getRecordCategory(civFil, "Employee Performance File System Records");
            assertNotNull(civFil2);

            NodeRef civFil3 = getRecordCategory(civFil, "Foreign Employee Award Files");
            assertNotNull(civFil3);
            assertNotNull(getRecordFolder(civFil3, "Christian Bohr"));
            assertNotNull(getRecordFolder(civFil3, "Karl Planck"));

            NodeRef civFil4 = getRecordCategory(civFil, "Payroll Differential and Allowances");
            assertNotNull(civFil4);
            assertNotNull(getRecordFolder(civFil4, "Martin Payroll Differential and Allowances"));

            NodeRef civFil5 = getRecordCategory(civFil, "Withholding of Within-Grade Increase (WGI) Records");
            assertNotNull(civFil5);
            assertNotNull(getRecordFolder(civFil5, "Gilbert WGI Records"));


            // Military Files: Test the "second level" categories and record folders
            NodeRef milFil1 = getRecordCategory(milFil, "Military Assignment Documents");
            assertNotNull(milFil1);

            NodeRef milFil2 = getRecordCategory(milFil, "Official Military Personnel Privilege Card Applications");
            assertNotNull(milFil2);
            assertNotNull(getRecordFolder(milFil2, "COL Bob Johnson"));
            assertNotNull(getRecordFolder(milFil2, "PFC Alan Murphy"));

            NodeRef milFil3 = getRecordCategory(milFil, "Personnel Security Program Records");
            assertNotNull(milFil3);
            assertNotNull(getRecordFolder(milFil3, "Commander's Administrative Assistant"));
            assertNotNull(getRecordFolder(milFil3, "Equal Opportunity Coordinator"));


            // Miscellaneous Files: Test the "second level" categories and record folders
            NodeRef misFil1 = getRecordCategory(misFil, "Civilian Employee Training Program Records");
            assertNotNull(misFil1);
            assertNotNull(getRecordFolder(misFil1, "Beth Tanaka Training Records (2008)"));
            assertNotNull(getRecordFolder(misFil1, "Bob Prentice Training Records (2008)"));
            assertNotNull(getRecordFolder(misFil1, "Chuck Stevens Training Records (2008)"));

            NodeRef misFil2 = getRecordCategory(misFil, "Monthly Cockpit Crew Training");
            assertNotNull(misFil2);
            assertNotNull(getRecordFolder(misFil2, "February Cockpit Crew Training"));
            assertNotNull(getRecordFolder(misFil2, "January Cockpit Crew Training"));

            NodeRef misFil3 = getRecordCategory(misFil, "Purchase of Foreign Award Medals and Decorations");
            assertNotNull(misFil3);

            NodeRef misFil4 = getRecordCategory(misFil, "Science Advisor Records");
            assertNotNull(misFil4);
            assertNotNull(getRecordFolder(misFil4, "Phoenix Mars Mission"));


            // Reports: Test the "second level" categories and record folders
            NodeRef rep1 = getRecordCategory(rep, "AIS Audit Records");
            assertNotNull(rep1);
            assertNotNull(getRecordFolder(rep1, "January AIS Audit Records"));

            NodeRef rep2 = getRecordCategory(rep, "Bi-Weekly Cost Reports");
            assertNotNull(rep2);
            assertNotNull(getRecordFolder(rep2, "CY08 Unit Manning Documents"));

            NodeRef rep3 = getRecordCategory(rep, "Overtime Reports");
            assertNotNull(rep3);
            assertNotNull(getRecordFolder(rep3, "FY08 Overtime Reports"));

            NodeRef rep4 = getRecordCategory(rep, "Unit Manning Documents");
            assertNotNull(rep4);
            assertNotNull(getRecordFolder(rep4, "1st Quarter Unit Manning Documents"));
         }

         /**
          * Helper method for getting the record category with the given category name within the context of the parent node
          *
          * @param parentNode the parent node
          * @param recordCategoryName the record category name
          * @return Returns the record category nodeRef or null if not found
          */
         private NodeRef getRecordCategory(NodeRef parentNode, String recordCategoryName)
         {
            return nodeService.getChildByName(parentNode, ContentModel.ASSOC_CONTAINS, recordCategoryName);
         }

         /**
          * Helper method for getting the record folder with the given folder name within the context of the record category
          *
          * @param recordCategory the record category node
          * @param recordFolderName the folder name
          * @return Returns the record folder nodeRef or null if not found
          */
         private NodeRef getRecordFolder(NodeRef recordCategory, String recordFolderName)
         {
            return nodeService.getChildByName(recordCategory, ContentModel.ASSOC_CONTAINS, recordFolderName);
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
            for (String dataSetId : dataSetIds)
            {
               assertTrue(dataSetService.getDataSets().containsKey(dataSetId));
            }

            return null;
         }
      });
   }

}
