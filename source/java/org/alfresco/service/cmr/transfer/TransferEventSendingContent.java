package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;

/**
 * Ranged Transfer event for sending content (e.g. sending content 1 of 64) 
 */
public class TransferEventSendingContent extends TransferEventImpl implements RangedTransferEvent 
{
    private long size;
    
    public String toString()
    {
        return "TransferEventSendingContent: " + super.getPosition() + " of " + super.getRange();
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getSize()
    {
        return size;
    }

}
