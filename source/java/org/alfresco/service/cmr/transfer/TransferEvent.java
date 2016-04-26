package org.alfresco.service.cmr.transfer;

import java.util.Date;

/**
 * TransferEvents are produced by the Transfer service during an in flight 
 * transfer.
 * 
 * <p>
 * The TransferCallback presents TransferEvents for processing. 
 * 
 * @see TransferCallback
 * @author Mark Rogers
 */
public interface TransferEvent
{
    /**
     * The transfer events will Start with a START event and finish with either SUCCESS or ERROR
     */
    enum TransferState { START, SENDING_SNAPSHOT, SENDING_CONTENT, PREPARING, COMMITTING, SUCCESS, ERROR, CANCELLED };
              
    /**
     * Get the state of this transfer  
     * @return the state of this transfer
     */
    TransferState getTransferState();
        
    /**
     * The time this event occured. 
     * @return the date/time the event
     */
    Date getTime();
        
    /** 
     * Get a human readable message for this event
     * @return String
     */
    String getMessage();
    
    /**
     * Is this the last event for this transfer ?
     */
    boolean isLast();
       
}
