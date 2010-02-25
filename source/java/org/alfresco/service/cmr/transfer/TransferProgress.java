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

package org.alfresco.service.cmr.transfer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author brian
 * 
 */
public class TransferProgress
{
    public enum Status
    {
        PRE_COMMIT, COMMIT_REQUESTED, COMMITTING, COMPLETE, ERROR, CANCELLED
    };

    private static Set<Status> terminalStatuses = Collections.unmodifiableSet(new HashSet<Status>(Arrays.asList(
            Status.COMPLETE, Status.ERROR, Status.CANCELLED)));
    
    private Status status;
    private int currentPosition;
    private int endPosition;
    private Throwable error;

    /**
     * 
     * @return The statuses that mark the end of the transfer. Once a transfer reaches one of these statuses
     * it can never move into a different status.
     */
    public static Set<Status> getTerminalStatuses()
    {
        return terminalStatuses;
    }
    
    /**
     * 
     * @return true if the current status is one of the terminal statuses.
     */
    public boolean isFinished()
    {
        return terminalStatuses.contains(status);
    }
    
    /**
     * @return the status
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Status status)
    {
        this.status = status;
    }

    /**
     * @return the currentPosition
     */
    public int getCurrentPosition()
    {
        return currentPosition;
    }

    /**
     * @param currentPosition
     *            the currentPosition to set
     */
    public void setCurrentPosition(int currentPosition)
    {
        this.currentPosition = currentPosition;
    }

    /**
     * @return the endPosition
     */
    public int getEndPosition()
    {
        return endPosition;
    }

    /**
     * @param endPosition
     *            the endPosition to set
     */
    public void setEndPosition(int endPosition)
    {
        this.endPosition = endPosition;
    }

    /**
     * @return the error
     */
    public Throwable getError()
    {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    public void setError(Throwable error)
    {
        this.error = error;
    }

}
