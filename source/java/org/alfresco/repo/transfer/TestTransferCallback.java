/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
