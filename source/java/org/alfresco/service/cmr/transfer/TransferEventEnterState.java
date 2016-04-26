package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;

/**
 * An enter state is produced when a transfer enters a new state.
 */ 
public class TransferEventEnterState extends TransferEventImpl implements TransferEvent
{

    public String toString()
    {
        return  ("TransferEventEnterState: " + super.toString());
    }
}
