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

import org.alfresco.service.cmr.transfer.TransferEvent;

/**
 * An abstract implementation of TransferEvent.
 * Also implements RangedTransferEvent.
 * @see TransferEvent
 * @see RangedTransferEvent
 */
public abstract class TransferEventImpl implements TransferEvent
{
    private String message;
    private TransferState state;
    private boolean last = false;
    private long range = 0;
    private long position = 0;
    private Date time = new Date();

    public String getMessage()
    {
        return message;
    }

    public Date getTime()
    {
        return time;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setRange(long range)
    {
        this.range = range;
    }
    
    public void setPosition(long position)
    {
        this.position = position;
    }

    public void setTransferState(TransferState state)
    {
        this.state = state;
    }

    public void setTime(Date time)
    {
        this.time = time;
    }

    public TransferState getTransferState()
    {
        return state;
    }

    public void setLast(boolean last)
    {
        this.last = last;
    }

    public boolean isLast()
    {
        return last;
    }
    
    /**
     * The position in the range
     * @return
     */
    public long getPosition()
    {
       return position; 
    }
    
    /**
     * The maximum range
     * @return
     */
    public long getRange()
    {
        return range;
    }
    
    public String toString()
    {
        return "TransferEventImpl : " + this.getTime() + ", " + this.getTransferState();
    }
    
    public boolean equals(Object obj)
    {
        if(obj instanceof TransferEventImpl)
        {
            TransferEventImpl other = (TransferEventImpl)obj;
            if(other.getTransferState().equals(this.getTransferState()) && 
               other.getPosition() == this.getPosition() &&
               other.getTime().equals(this.getTime()))
            {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode()
    {
        // discard any high bits
        return (int)this.getTime().getTime();
    }

}
