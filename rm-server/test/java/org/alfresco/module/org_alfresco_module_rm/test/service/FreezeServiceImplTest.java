package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class FreezeServiceImplTest extends BaseRMTestCase
{
   /** Id of the test data set*/
   private static final String DATA_SET_ID = "testExampleData";

   /** First Record */
   private NodeRef recordOne;

   /** Second Record */
   private NodeRef recordTwo;

   /** Third Record */
   private NodeRef recordThree;

   /** Fourth Record */
   private NodeRef recordFour;

   public void testFreezeService() throws Exception
   {
      doTestInTransaction(new Test<NodeRef>()
      {
         @Override
         public NodeRef run() throws Exception
         {
            NodeRef recordFolder = getRecordFolder();

            recordOne = createRecord(recordFolder, "one.txt", "1");
            recordTwo = createRecord(recordFolder, "two.txt", "22");
            recordThree = createRecord(recordFolder, "three.txt", "333");
            recordFour = createRecord(recordFolder, "four.txt", "4444");

            return recordFolder;
         }

         @Override
         public void test(NodeRef result) throws Exception
         {
            assertTrue(rmService.isRecord(recordOne));
            assertTrue(rmService.isRecord(recordTwo));
            assertTrue(rmService.isRecord(recordThree));
            assertTrue(rmService.isRecord(recordFour));
            assertTrue(rmService.isFilePlanComponent(recordOne));
            assertTrue(rmService.isFilePlanComponent(recordTwo));
            assertTrue(rmService.isFilePlanComponent(recordThree));
            assertTrue(rmService.isFilePlanComponent(recordFour));

            // Freeze a record
            freezeService.freeze("FreezeReason", recordOne);
            assertTrue(freezeService.hasFrozenChildren(result));

            // Check the hold exists
            Set<NodeRef> holdAssocs = freezeService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(1, holdAssocs.size());
            NodeRef holdNodeRef = holdAssocs.iterator().next();
            assertTrue(freezeService.isHold(holdNodeRef));
            assertEquals("FreezeReason", freezeService.getReason(holdNodeRef));
            Set<NodeRef> frozenNodes = freezeService.getFrozen(holdNodeRef);
            assertNotNull(frozenNodes);
            assertEquals(1, frozenNodes.size());

            // Check the nodes are frozen
            assertTrue(freezeService.isFrozen(recordOne));
            assertNotNull(freezeService.getFreezeDate(recordOne));
            assertNotNull(freezeService.getFreezeInitiator(recordOne));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));

            // Update the freeze reason
            freezeService.updateReason(holdNodeRef, "NewFreezeReason");

            // Check the hold has been updated
            assertEquals("NewFreezeReason", freezeService.getReason(holdNodeRef));

            // Freeze a number of records
            Set<NodeRef> records = new HashSet<NodeRef>();
            records.add(recordOne);
            records.add(recordTwo);
            records.add(recordThree);
            NodeRef newHold = freezeService.freeze("Freeze a set of nodes", records);
            assertNotNull(newHold);
            assertTrue(freezeService.isHold(newHold));

            // Check the holds exist
            holdAssocs = freezeService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(2, holdAssocs.size());
            for (NodeRef hold : holdAssocs)
            {
               String reason = freezeService.getReason(hold);
               if (reason.equals("Freeze a set of nodes"))
               {
                  assertEquals(newHold, hold);
                  frozenNodes = freezeService.getFrozen(hold);
                  assertNotNull(frozenNodes);
                  assertEquals(3, frozenNodes.size());
               }
               else if (reason.equals("NewFreezeReason"))
               {
                  frozenNodes = freezeService.getFrozen(hold);
                  assertNotNull(frozenNodes);
                  assertEquals(1, frozenNodes.size());
               }
               else
               {
                  throw new AlfrescoRuntimeException("The reason '" + reason + "' was not found in the existing holds.");
               }
            }

            // Check the nodes are frozen
            final List<NodeRef> testRecords = Arrays.asList(new NodeRef[]{recordOne, recordTwo, recordThree});
            for (NodeRef nr : testRecords)
            {
               assertTrue(freezeService.isFrozen(nr));
               assertNotNull(freezeService.getFreezeDate(nr));
               assertNotNull(freezeService.getFreezeInitiator(nr));
            }

            // Unfreeze a node
            freezeService.unFreeze(recordThree);

            // Check the holds
            holdAssocs = freezeService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(2, holdAssocs.size());
            for (NodeRef hold : holdAssocs)
            {
               String reason = freezeService.getReason(hold);
               if (reason.equals("Freeze a set of nodes"))
               {
                  frozenNodes = freezeService.getFrozen(hold);
                  assertNotNull(frozenNodes);
                  assertEquals(2, frozenNodes.size());
               }
               else if (reason.equals("NewFreezeReason"))
               {
                  frozenNodes = freezeService.getFrozen(hold);
                  assertNotNull(frozenNodes);
                  assertEquals(1, frozenNodes.size());
               }
               else
               {
                  throw new AlfrescoRuntimeException("The reason '" + reason + "' was not found in the existing holds.");
               }
            }

            // Check the nodes are frozen
            assertTrue(freezeService.isFrozen(recordOne));
            assertNotNull(freezeService.getFreezeDate(recordOne));
            assertNotNull(freezeService.getFreezeInitiator(recordOne));
            assertTrue(freezeService.isFrozen(recordTwo));
            assertNotNull(freezeService.getFreezeDate(recordTwo));
            assertNotNull(freezeService.getFreezeInitiator(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));
            assertFalse(freezeService.isFrozen(recordFour));

            // Relinquish the first hold
            holdNodeRef = holdAssocs.iterator().next();
            freezeService.relinquish(holdNodeRef);

            // Check the existing hold
            holdAssocs = freezeService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(1, holdAssocs.size());

            // Relinquish the second hold
            holdNodeRef = holdAssocs.iterator().next();
            freezeService.unFreeze(freezeService.getFrozen(holdNodeRef));

            // All holds should be deleted
            holdAssocs = freezeService.getHolds(filePlan);
            assertEquals(0, holdAssocs.size());

            // Check the nodes are unfrozen
            assertFalse(freezeService.isFrozen(recordOne));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));
            assertFalse(freezeService.isFrozen(recordFour));
            assertFalse(freezeService.hasFrozenChildren(result));

            // Test freezing nodes, adding them to an existing hold
            NodeRef hold = freezeService.freeze("AnotherFreezeReason", recordFour);
            freezeService.freeze(hold, recordOne);
            Set<NodeRef> nodes = new HashSet<NodeRef>();
            nodes.add(recordTwo);
            nodes.add(recordThree);
            freezeService.freeze(hold, nodes);
            assertTrue(freezeService.hasFrozenChildren(result));

            // Check the hold
            holdAssocs = freezeService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(1, holdAssocs.size());

            // Relinquish the first hold
            freezeService.relinquish(holdAssocs.iterator().next());

            // Check the nodes are unfrozen
            assertFalse(freezeService.isFrozen(recordOne));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));
            assertFalse(freezeService.isFrozen(recordFour));
            assertFalse(freezeService.hasFrozenChildren(result));
         }

         /**
          * Helper method for getting a record folder from the test data
          * 
          * @return  NodeRef of a record folder from the test data
          */
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

         /**
          * Helper method for creating a record
          * 
          * @param recordFolder     Record folder in which the record will be created
          * @param name             The name of the record
          * @param someTextContent  The content of the record
          * @return                 NodeRef of the created record
          */
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
}
