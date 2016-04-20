package org.alfresco.service.cmr.transfer;

/**
 * A Ranged Transfer event is a detail record for a State that has many smaller steps.   For example when sending content the first 
 * event is 1 of the number of files to send.   The second is 2 of the number of files to send.
 * 
 * These events are intended to support "progress bar" types of interfaces.
 *  
 * @author Mark Rogers
 */
public interface RangedTransferEvent extends TransferEvent
{
    /**
     * The position in the range
     * @return long
     */
    long getPosition();
    
    /**
     * The maximum range
     * @return long
     */
    long getRange();

}
