/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferEndEvent;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventCommittingStatus;
import org.alfresco.service.cmr.transfer.TransferEventEndState;
import org.alfresco.service.cmr.transfer.TransferEventEnterState;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferEventSendingContent;
import org.alfresco.service.cmr.transfer.TransferEventSendingSnapshot;

/**
 * Class to bring together all the transfer event stuff.
 * 
 * One processor instance for each transfer.
 * 
 * Observer
 *
 * @author Mark Rogers
 */
public class TransferEventProcessor
{
    public Set<TransferCallback> observers = new  HashSet<TransferCallback>();
    
    LinkedBlockingQueue<TransferEvent> queue = new LinkedBlockingQueue<TransferEvent>();
    
    public TransferEventProcessor()
    {
    }
    
    public void addObserver(TransferCallback observer)
    {
        observers.add(observer);   
    }
    
    public void deleteObserver(TransferCallback observer)
    {
        observers.remove(observer);
    }
    
    public void begin(String transferId)
    {
        setState(TransferEvent.TransferState.START);  
        TransferEventBegin event = new TransferEventBegin();
        event.setTransferState(TransferEvent.TransferState.START);
        event.setMessage("begin transferId:" + transferId);
        queue.add(event); 
        event.setTransferId(transferId);
        notifyObservers();
    }
      
    public void start()
    {
        setState(TransferEvent.TransferState.START);
        notifyObservers();
    }

    public void end(TransferEndEvent endEvent)
    {
        setState(endEvent.getTransferState());
        queue.add(endEvent); 
        notifyObservers();
    }

    /**
     * 
     * @param data
     * @param range
     * @param position
     */
    public void sendContent(ContentData data, long range, long position)
    {
        setState(TransferEvent.TransferState.SENDING_CONTENT);
        
        TransferEventSendingContent event = new TransferEventSendingContent();
        event.setTransferState(TransferEvent.TransferState.SENDING_CONTENT);
        event.setRange(range);
        event.setPosition(position);
        event.setSize(data.getSize());
        event.setMessage("sending content " + position + " of " + range + ", size: " + event.getSize());
        queue.add(event);
        notifyObservers();
    }
    
    /**
     * 
     * @param data
     * @param range
     * @param position
     */
    public void sendSnapshot(long range, long position)
    {
        setState(TransferEvent.TransferState.SENDING_SNAPSHOT);
        
        TransferEventSendingSnapshot event = new TransferEventSendingSnapshot();
        event.setTransferState(TransferEvent.TransferState.SENDING_SNAPSHOT);
        event.setRange(range);
        event.setPosition(position);
        event.setMessage("sending snapshot");
        queue.add(event);
        notifyObservers();
    }
    
    public void prepare()
    {
        setState(TransferEvent.TransferState.PREPARING);
        notifyObservers();
    }
    
    public void commit()
    {
        setState(TransferEvent.TransferState.COMMITTING);
        notifyObservers();
    }
    
    public void writeReport(NodeRef nodeRef, TransferEventReport.ReportType reportType, TransferEvent.TransferState state)
    {
        setState(state);

        TransferEventReport event = new TransferEventReport();
        event.setTransferState(state);
        event.setNodeRef(nodeRef);
        event.setReportType(reportType); 
        event.setMessage("report nodeRef:" + nodeRef + ", reportType :" + reportType );
        queue.add(event);
        notifyObservers();
    }
    
    /**
     * 
     * @param range
     * @param position
     */
    public void committing(long range, long position)
    {
        setState(TransferEvent.TransferState.COMMITTING);
        
        TransferEventCommittingStatus event = new TransferEventCommittingStatus();
        event.setTransferState(TransferEvent.TransferState.COMMITTING);
        event.setRange(range);
        event.setPosition(position);
        event.setMessage("committing " + position + " of " + range);
        queue.add(event);
        notifyObservers();
    }
    
    
    private TransferEvent.TransferState currentState;
    
    private void setState(TransferEvent.TransferState state)
    {
        if(currentState != state)
        {
            if(currentState != null)
            {
                TransferEventImpl event = new TransferEventEndState();
                event.setMessage("End State: " + currentState);
                event.setTransferState(currentState);
                queue.add(event);
            }
            
            TransferEventImpl event = new TransferEventEnterState();
            event.setMessage("Enter State: " + state);
            event.setTransferState(state);
            queue.add(event);
            currentState = state;
        }
    }

    
    private void notifyObservers()
    {
        TransferEvent event = (TransferEvent)queue.poll();
        while(event != null)
        {
            // call the observers
            for(TransferCallback callback : observers)
            {
                callback.processEvent(event);
            }
            event = (TransferEvent)queue.poll();
        }
    }
}
