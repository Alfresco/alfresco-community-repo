package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class FreezeServiceImplTest extends BaseRMTestCase
{
   /** Data Set Service */
   private DataSetService dataSetService;

   /** Freeze Service */
   private FreezeService freezeService;

   /** Id of the test data set*/
   private static final String DATA_SET_ID = "testExampleData"; 

   /** First Record */
   private NodeRef recordOne; 

   /** Second Record */
   private NodeRef recordTwo; 

   /** Third Record */
   private NodeRef recordThree; 

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
    */
   @Override
   protected void initServices()
   {
      super.initServices();

      // Get Data Set Service
      dataSetService = (DataSetService) applicationContext.getBean("DataSetService");
      // Get Freeze Service
      freezeService = (FreezeService) applicationContext.getBean("freezeService");
   }

   /**
    * @see FreezeService#freeze(String, NodeRef)
    */
   public void testFreeze() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            NodeRef recordFolder = getRecordFolder();

            recordOne = createRecord(recordFolder, "one.txt", "1");
            recordTwo = createRecord(recordFolder, "two.txt", "22");
            recordThree = createRecord(recordFolder, "three.txt", "333");

            return null;
         }

         @Override
         public void test(Void result) throws Exception
         {
            assertTrue(nodeService.hasAspect(recordOne, ASPECT_RECORD));
            assertTrue(nodeService.hasAspect(recordOne, ASPECT_FILE_PLAN_COMPONENT));

            freezeService.freeze("FreezeReason", recordOne);

            // Check the hold exists 
            List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(filePlan, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
            assertNotNull(holdAssocs);
            assertEquals(1, holdAssocs.size());
            NodeRef holdNodeRef = holdAssocs.get(0).getChildRef();
            assertTrue(freezeService.isHold(holdNodeRef));
            assertEquals("FreezeReason", freezeService.getReason(holdNodeRef));
            Set<NodeRef> freezeAssocs = freezeService.getFrozen(holdNodeRef);
            assertNotNull(freezeAssocs);
            assertEquals(1, freezeAssocs.size());

            // Check the nodes are frozen
            assertTrue(freezeService.isFrozen(recordOne));
            assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_AT));
            assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_BY));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));

            // Update the freeze reason
            freezeService.updateReason(holdNodeRef, "NewFreezeReason");

            // Check the hold has been updated
            assertEquals("NewFreezeReason", freezeService.getReason(holdNodeRef));
         }

         private NodeRef getRecordFolder()
         {
            // Load the data set into the specified file plan
            dataSetService.loadDataSet(filePlan, DATA_SET_ID);

            // Get the record category
            NodeRef recCat1 = nodeService.getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, "TestRecordCategory1");
            assertNotNull(recCat1);
            assertEquals("TestRecordCategory1", nodeService.getProperty(recCat1, ContentModel.PROP_NAME));

            NodeRef recCat12 = nodeService.getChildByName(recCat1, ContentModel.ASSOC_CONTAINS, "TestRecordCategory12");
            assertNotNull(recCat12);
            assertEquals("TestRecordCategory12", nodeService.getProperty(recCat12, ContentModel.PROP_NAME));

            // Get the record folder
            NodeRef recFol13 = nodeService.getChildByName(recCat12, ContentModel.ASSOC_CONTAINS, "TestRecordFolder3");
            assertNotNull(recFol13);
            assertEquals("TestRecordFolder3", nodeService.getProperty(recFol13, ContentModel.PROP_NAME));

            return recFol13;
         }

         private NodeRef createRecord(NodeRef recordFolder, String name, String someTextContent)
         {
            // Create the document
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
            props.put(ContentModel.PROP_NAME, name);
            NodeRef record = nodeService.createNode(recordFolder,
                  ContentModel.ASSOC_CONTAINS,
                  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                  ContentModel.TYPE_CONTENT, props).getChildRef();

            // Set the content
            ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(someTextContent);

            return record;
         }
      });
   }

   /**
    * @see FreezeService#unFreeze(NodeRef)
    */
   public void testUnFreeze() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            // FIXME
            return null;
         }
      });
   }

   /**
    * @see FreezeService#relinquish(NodeRef)
    */
   public void testRelinquish() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            // FIXME
            return null;
         }
      });
   }
}
