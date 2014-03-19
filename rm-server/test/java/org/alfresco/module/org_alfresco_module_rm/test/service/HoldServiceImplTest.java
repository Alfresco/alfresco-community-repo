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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold service integration test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class HoldServiceImplTest extends BaseRMTestCase
{    
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }
    
    public void testDeleteHoldBehaviourForRecordFolder()
    {
        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // create test holds
               NodeRef hold1 = holdService.createHold(filePlan, "hold one", "I have my reasons", "but I'll not describe them here!");
               assertNotNull(hold1);
               
               // add the record folder to hold1
               holdService.addToHold(hold1, rmFolder);
               
               // assert that the folder and records are frozen
               assertTrue(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertTrue(freezeService.isFrozen(recordDeclaredOne));
               
               // check the contents of the hold
               List<NodeRef> frozenNodes = holdService.getHeld(hold1);
               assertNotNull(frozenNodes);
               assertEquals(1, frozenNodes.size());
               assertEquals(rmFolder, frozenNodes.get(0));
               
               // delete the hold
               holdService.deleteHold(hold1);
               
               // assert that the folder and records no longer frozen
               assertFalse(freezeService.isFrozen(rmFolder));
               assertFalse(freezeService.isFrozen(recordOne));
               assertFalse(freezeService.isFrozen(recordDeclaredOne));
               
               // confirm the hold has been deleted
               assertNull(holdService.getHold(filePlan, "hold one"));               
               
               return null;
           }
        });
    }
    
    public void testDeleteHoldBehaviourForMultipleHolds()
    {
        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run() throws Exception
           {
               // create test holds
               NodeRef hold1 = holdService.createHold(filePlan, "hold one", "I have my reasons", "but I'll not describe them here!");
               assertNotNull(hold1);
               NodeRef hold2 = holdService.createHold(filePlan, "hold two", "secrets are everything", "no then! that's just not on!");
               assertNotNull(hold2);
               
               // add the record folder to hold1
               holdService.addToHold(hold1, rmFolder);
               
               // assert that the folder and records are frozen
               assertTrue(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertTrue(freezeService.isFrozen(recordDeclaredOne));
               
               // check the contents of the hold
               List<NodeRef> frozenNodes = holdService.getHeld(hold1);
               assertNotNull(frozenNodes);
               assertEquals(1, frozenNodes.size());
               assertEquals(rmFolder, frozenNodes.get(0));
               
               holdService.addToHold(hold2, recordOne);
               
               // assert that the folder and records are frozen
               assertTrue(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertTrue(freezeService.isFrozen(recordDeclaredOne));
               
               // delete the hold
               holdService.deleteHold(hold1);
               
               // assert that the folder and records no longer frozen
               assertFalse(freezeService.isFrozen(rmFolder));
               assertTrue(freezeService.isFrozen(recordOne));
               assertFalse(freezeService.isFrozen(recordDeclaredOne));
               
               // confirm the hold has been deleted
               assertNull(holdService.getHold(filePlan, "hold one"));   
               
               // delete the hold
               holdService.deleteHold(hold2);
               
               // assert that the folder and records no longer frozen
               assertFalse(freezeService.isFrozen(rmFolder));
               assertFalse(freezeService.isFrozen(recordOne));
               assertFalse(freezeService.isFrozen(recordDeclaredOne));
               
               // confirm the hold has been deleted
               assertNull(holdService.getHold(filePlan, "hold two"));
               
               return null;
           }
        });
    }
   
}
