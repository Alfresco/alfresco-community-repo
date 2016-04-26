
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

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("TransferProgress{status=");
        sb.append(status);
        sb.append("; currentPosition=");
        sb.append(currentPosition);
        sb.append("; endPosition=");
        sb.append(endPosition);
        sb.append("; error={");
        sb.append(error);
        sb.append("}}");
        return sb.toString();
    }
}
