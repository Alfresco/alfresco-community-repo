package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;

/**
 * Event for sending the properties snapshot.
 *
 */
public class TransferEventSendingSnapshot extends TransferEventImpl implements RangedTransferEvent
{
    public String toString()
    {
        return "TransferEventSendingSnapshot: " + super.getPosition() + " of " + super.getRange();
    }

}
