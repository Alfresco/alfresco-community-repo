/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferVersion;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for TransferVersionChecker
 * @author mrogers
 */
public class TransferVersionCheckerImplTest
{
    /**
     * Test TransferVersionCheckerImpl
     */
    @Test
    public void testTransferVersionCheckerImpl()
    {
        
        TransferVersionChecker checker = new TransferVersionCheckerImpl();
        
        String EDITION = "Ent";
        
        TransferVersion e = new TransferVersionImpl("3", "3", "0", EDITION);
        TransferVersion e2 = new TransferVersionImpl("3", "3", "0", EDITION);
        TransferVersion e3 = new TransferVersionImpl("3", "3", "1", EDITION);
        
        assertTrue(e.equals(e2));
        assertFalse(e.equals(e3));
        
        assertTrue("same object equals", checker.checkTransferVersions(e, e));
        assertTrue("duplicate object equals", checker.checkTransferVersions(e, e2));
        
        // The revision should be ignored
        assertTrue("not equals", checker.checkTransferVersions(e, new TransferVersionImpl("3", "3", "1", EDITION)));
        assertTrue("not equals", checker.checkTransferVersions(e, new TransferVersionImpl("3", "3", "2", EDITION)));
        
        assertTrue("Checker should not flag minor difference", checker.checkTransferVersions(e, new TransferVersionImpl("3", "4", "0", EDITION)));
        assertFalse("Checker should flag major difference", checker.checkTransferVersions(e, new TransferVersionImpl("4", "3", "0", EDITION)));
        assertTrue("Checker should not flag edition difference", checker.checkTransferVersions(e, new TransferVersionImpl("3", "3", "0", "Whatever")));
        assertTrue("Checker should not flag edition absence", checker.checkTransferVersions(e, new TransferVersionImpl("3", "3", "0", null)));
    }

}
