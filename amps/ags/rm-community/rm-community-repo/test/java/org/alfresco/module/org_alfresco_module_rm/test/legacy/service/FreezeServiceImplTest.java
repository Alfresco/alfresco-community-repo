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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private List<NodeRef> holdAssocs;

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

   /**
    * Test freeze service methods.
    *
    * @deprecated as of 2.2
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
             NodeRef hold101 = holdService.createHold(filePlan, "freezename 101", "FreezeReason", null);

           assertNotNull(hold101);
           holdService.addToHold(hold101, recordOne);

           //assertTrue(freezeService.hasFrozenChildren(rmFolder));

           // Check the hold exists
           holdAssocs = holdService.getHolds(filePlan);
           assertNotNull(holdAssocs);
           assertEquals(1, holdAssocs.size());

             NodeRef holdNodeRef = holdAssocs.iterator().next();


            assertEquals(holdNodeRef, hold101);
            assertTrue(holdService.isHold(holdNodeRef));
            assertEquals("FreezeReason", holdService.getHoldReason(holdNodeRef));
             List<NodeRef> frozenNodes = holdService.getHeld(holdNodeRef);

            assertNotNull(frozenNodes);
            assertEquals(1, frozenNodes.size());

            // Check the nodes are frozen
            assertTrue(freezeService.isFrozen(recordOne));
            assertNotNull(freezeService.getFreezeDate(recordOne));
            assertNotNull(freezeService.getFreezeInitiator(recordOne));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));

            // Update the freeze reason
            holdService.setHoldReason(holdNodeRef, "NewFreezeReason");

            // Check the hold has been updated
            assertEquals("NewFreezeReason", holdService.getHoldReason(holdNodeRef));

            // Freeze a number of records
             List<NodeRef> records = new ArrayList<>();
            records.add(recordOne);
            records.add(recordTwo);
            records.add(recordThree);
            NodeRef newHold = holdService.createHold(filePlan, "Hold 102", "Freeze a set of nodes", null);

            holdService.addToHold(newHold, records);
            assertNotNull(newHold);
            assertTrue(holdService.isHold(newHold));

            // Check the holds exist
            holdAssocs = holdService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(2, holdAssocs.size());

            for (NodeRef hold : holdAssocs)
            {
               String reason = holdService.getHoldReason(hold);
               if (reason.equals("Freeze a set of nodes"))
               {
                  assertEquals(newHold, hold);
                  frozenNodes = holdService.getHeld(hold);
                  assertNotNull(frozenNodes);
                  assertEquals(3, frozenNodes.size());
               }
               else if (reason.equals("NewFreezeReason"))
               {
                  frozenNodes = holdService.getHeld(hold);
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
            holdService.removeFromAllHolds(recordThree);
            // Check the holds
            holdAssocs = holdService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(2, holdAssocs.size());
            for (NodeRef hold : holdAssocs)
            {
               String reason = holdService.getHoldReason(hold);
               if (reason.equals("Freeze a set of nodes"))
               {
                  frozenNodes = holdService.getHeld(hold);
                  assertNotNull(frozenNodes);
                  assertEquals(2, frozenNodes.size());
               }
               else if (reason.equals("NewFreezeReason"))
               {
                  frozenNodes = holdService.getHeld(hold);
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
               return null;
           }
       });
        //Splitting transaction to fix onCreateNodePolicy issue where there was a node not found exception
       doTestInTransaction(new Test<Void>()
       {
           @Override
           public Void run() throws Exception
           {
            // Relinquish the first hold
            NodeRef holdNodeRef = holdAssocs.iterator().next();
            holdService.deleteHold(holdNodeRef);

            // Check the existing hold
            holdAssocs = holdService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(1, holdAssocs.size());

            // Relinquish the second hold
            holdNodeRef = holdAssocs.iterator().next();
            holdService.removeFromAllHolds(holdService.getHeld(holdNodeRef));

            // hold is not automatically removed
            holdAssocs = holdService.getHolds(filePlan);
            assertEquals(1, holdAssocs.size());

            // delete hold
            holdService.deleteHold(holdNodeRef);

            holdAssocs = holdService.getHolds(filePlan);
            assertEquals(0, holdAssocs.size());

            // Check the nodes are unfrozen
            assertFalse(freezeService.isFrozen(recordOne));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));
            assertFalse(freezeService.isFrozen(recordFour));
            //assertFalse(freezeService.hasFrozenChildren(rmFolder));

            // Test freezing nodes, adding them to an existing hold
            NodeRef hold = holdService.createHold(filePlan, "hold 1", "AnotherFreezeReason", "description");
            holdService.addToHold(hold, recordFour);
            holdService.addToHold(hold, recordOne);
            List<NodeRef> nodes = new ArrayList<>();
            nodes.add(recordTwo);
            nodes.add(recordThree);
            holdService.addToHold(hold, nodes);
           //assertTrue(freezeService.hasFrozenChildren(rmFolder));
            // Check the hold
            holdAssocs = holdService.getHolds(filePlan);
            assertNotNull(holdAssocs);
            assertEquals(1, holdAssocs.size());
               return null;
           }
       });
       //Splitting transaction to fix onCreateNodePolicy issue where there was a node not found exception
       doTestInTransaction(new Test<Void>()
       {
           @Override
           public Void run() throws Exception
           {
            // Relinquish the first hold
            holdService.deleteHold(holdAssocs.iterator().next());

            // Check the nodes are unfrozen
            assertFalse(freezeService.isFrozen(recordOne));
            assertFalse(freezeService.isFrozen(recordTwo));
            assertFalse(freezeService.isFrozen(recordThree));
            assertFalse(freezeService.isFrozen(recordFour));
           // assertFalse(freezeService.hasFrozenChildren(rmFolder));

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                NodeRef hold101 = holdService.createHold(filePlan, "freezename 103", "FreezeReason", null);
                // Freeze a record folder
                assertNotNull(hold101);
                holdService.addToHold(hold101, rmFolder);
                assertTrue(recordFolderService.isRecordFolder(rmFolder));
                assertTrue(freezeService.isFrozenOrHasFrozenChildren(rmFolder));
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                NodeRef hold101 = holdService.createHold(filePlan, "freezename 104", "FreezeReason", null);
                // Freeze a record inside a record folder
                assertNotNull(hold101);
                holdService.addToHold(hold101, recordThree);
                assertTrue(recordService.isRecord(recordThree));
                assertTrue(freezeService.isFrozenOrHasFrozenChildren(rmFolder));
                return null;
            }
        });
    }
}
