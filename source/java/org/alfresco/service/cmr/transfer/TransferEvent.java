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
     * @return
     */
    String getMessage();
    
    /**
     * Is this the last event for this transfer ?
     */
    boolean isLast();
       
}
