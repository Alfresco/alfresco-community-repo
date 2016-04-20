package org.alfresco.repo.transfer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestTransferCallback implements TransferCallback
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static Log logger = LogFactory.getLog(TestTransferCallback.class);
    
    
    Queue<TransferEvent> events = new ConcurrentLinkedQueue<TransferEvent>();
    String transferId = null;

    public void processEvent(TransferEvent event)
    {
        logger.debug(event.toString());
        events.add(event);
        
        if(event instanceof TransferEventBegin)
        {
            TransferEventBegin beginEvent = (TransferEventBegin)event;
            transferId = beginEvent.getTransferId();
        }
    }
    

    
    /**
     * Get the thread safe queue of events
     * @return
     */
    public Queue<TransferEvent> getEvents()
    {
        return events;
    }
    
}
