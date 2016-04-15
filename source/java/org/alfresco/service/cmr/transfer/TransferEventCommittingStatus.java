package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;

/**
 * TransferEventCommittingStatus are produced when a transfer is being committed.
 * 
 * The range can be used to produce a "progress bar"
 * 
 */
public class TransferEventCommittingStatus extends TransferEventImpl implements RangedTransferEvent 
{
    public String toString()
    {   
        return "TransferEventCommittingStatus: " + super.getPosition() + " of " + super.getRange();
    }
}
