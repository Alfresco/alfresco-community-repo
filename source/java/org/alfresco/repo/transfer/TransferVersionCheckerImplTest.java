package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferVersion;
import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * Unit test for TransferVersionChecker
 * @author mrogers
 */
public class TransferVersionCheckerImplTest extends BaseAlfrescoSpringTest 
{
    /**
     * Test TransferVersionCheckerImpl
     */
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
        
        // These should not match
        assertFalse("not equals minor different", checker.checkTransferVersions(e, new TransferVersionImpl("3", "4", "0", EDITION)));     
        assertFalse("not equals major different", checker.checkTransferVersions(e, new TransferVersionImpl("4", "3", "0", EDITION)));     
        assertFalse("not equals edition different", checker.checkTransferVersions(e, new TransferVersionImpl("3", "3", "0", "Whatever")));     
        assertFalse("not equals edition null ", checker.checkTransferVersions(e, new TransferVersionImpl("3", "3", "0", null)));                        
    }

}
