package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEndEventImpl;

/**
 * The cancelled event indicates a transfer was aborted
 */
public class TransferEventCancelled extends TransferEndEventImpl
{
    public String toString()
    {
        return "TransferEventCancelled";
    }
}
