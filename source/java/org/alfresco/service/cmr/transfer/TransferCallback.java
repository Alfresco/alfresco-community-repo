package org.alfresco.service.cmr.transfer;

/**
 * The transfer callback is called during a transfer, it allows the real-time feedback of 
 * an in progress transfer.  It can be used to populate a deployment report or to display 
 * a user interface.
 *
 * @author Mark Rogers
 */
public interface TransferCallback 
{
   /**
     * processEvent
     * @param event TransferEvent
     */
    public void processEvent(TransferEvent event);    
    
}
