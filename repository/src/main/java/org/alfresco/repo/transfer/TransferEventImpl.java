/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.transfer;

import java.util.Date;

import org.alfresco.service.cmr.transfer.TransferEvent;

/**
 * An abstract implementation of TransferEvent. Also implements the operations required by RangedTransferEvent.
 * 
 * @see TransferEvent
 * @see org.alfresco.service.cmr.transfer.RangedTransferEvent
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
     * 
     * @return long
     */
    public long getPosition()
    {
        return position;
    }

    /**
     * The maximum range
     * 
     * @return long
     */
    public long getRange()
    {
        return range;
    }

    /**
     * A simple human readable summary of this event, the format of this string is not guaranteed and is liable to change.
     */
    public String toString()
    {
        return this.getClass().getSimpleName() + ", " + this.getTime() + ", " + this.getTransferState();
    }

    public boolean equals(Object obj)
    {
        if (obj != null)
        {
            if (this.getClass().equals(obj.getClass()))
            {
                TransferEventImpl other = (TransferEventImpl) obj;
                if (other.getTransferState().equals(this.getTransferState()) &&
                        other.getPosition() == this.getPosition() &&
                        other.getTime().equals(this.getTime()))
                {
                    return true;
                }
            }
        }
        // not a match
        return false;
    }

    public int hashCode()
    {
        // discard any high bits
        return (int) this.getTime().getTime();
    }

}
