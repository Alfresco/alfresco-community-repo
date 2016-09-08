/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freeze service implementation test.
 * 
 * @author Tuna Aksoy
 * @since 2.1
 */
public class FreezeServiceImplTest extends BaseRMTestCase
{
   @Override
    protected boolean isRecordTest()
    {
        return true;
    }

   /**
    * Test freeze service methods
    */
   public void testFreezeService() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run() throws Exception
         {
            assertTrue(recordService.isRecord(recordOne));
            assertTrue(recordService.isRecord(recordTwo));
            assertTrue(recordService.isRecord(recordThree));
            assertTrue(recordService.isRecord(recordFour));
            assertTrue(filePlanService.isFilePlanComponent(recordOne));
            assertTrue(filePlanService.isFilePlanComponent(recordTwo));
            assertTrue(filePlanService.isFilePlanComponent(recordThree));
            assertTrue(filePlanService.isFilePlanComponent(recordFour));

            // Freeze a record
            freezeService.freeze("FreezeReason", recordOne);

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

            // Test freezing nodes, adding them to an existing hold
            NodeRef hold = freezeService.freeze("AnotherFreezeReason", recordFour);
            freezeService.freeze(hold, recordOne);
            Set<NodeRef> nodes = new HashSet<NodeRef>();
            nodes.add(recordTwo);
            nodes.add(recordThree);
            freezeService.freeze(hold, nodes);

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

            return null;
         }
      });
   }
}
