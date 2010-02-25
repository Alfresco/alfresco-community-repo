/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventCommittingStatus;
import org.alfresco.service.cmr.transfer.TransferEventEndState;
import org.alfresco.service.cmr.transfer.TransferEventEnterState;
import org.alfresco.service.cmr.transfer.TransferEventError;
import org.alfresco.service.cmr.transfer.TransferEventSendingContent;
import org.alfresco.service.cmr.transfer.TransferEventSendingManifest;
import org.alfresco.service.cmr.transfer.TransferEventSuccess;

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
    

    
    public void addObserver(TransferCallback observer)
    {
        observers.add(observer);   
    }
    
    public void deleteObserver(TransferCallback observer)
    {
        observers.remove(observer);
    }
    
    /**
     * 
     */
    public TransferEventProcessor()
    {
        
    }
      
    public void start()
    {
        setState(TransferEvent.TransferState.START);        
        notifyObservers();
    }
    
    public void success()
    {
        setState(TransferEvent.TransferState.SUCCESS);
        
        /**
         * Write the success event
         */
        TransferEventSuccess event = new TransferEventSuccess();
        event.setTransferState(TransferEvent.TransferState.SUCCESS);
        event.setLast(true);
        queue.add(event); 
        notifyObservers();
    }
    
    public void error(Exception exception)
    {
        setState(TransferEvent.TransferState.ERROR);
        
        /**
         * Write the error event
         */
        TransferEventError event = new TransferEventError();
        event.setTransferState(TransferEvent.TransferState.ERROR);
        event.setLast(true);
        event.setException(exception);
        queue.add(event); 
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
    public void sendManifest(long range, long position)
    {
        setState(TransferEvent.TransferState.SENDING_MANIFEST);
        
        TransferEventSendingManifest event = new TransferEventSendingManifest();
        event.setTransferState(TransferEvent.TransferState.SENDING_MANIFEST);
        event.setRange(range);
        event.setPosition(position);
        event.setMessage("sending manifest");
        queue.add(event);
        notifyObservers();
    }
    
    public void prepare()
    {
        setState(TransferEvent.TransferState.PREPARING);
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
    
    public void abort()
    {

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
                event.setTransferState(state);
                queue.add(event); 
            }
            {
                TransferEventImpl event = new TransferEventEnterState();
                event.setMessage("Enter State: " + state);
                event.setTransferState(state);
                queue.add(event); 
            }
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
