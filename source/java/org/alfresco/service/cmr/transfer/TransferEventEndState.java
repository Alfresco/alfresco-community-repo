package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;

/**
 * An end state is produces when a transfer ends a state.
 */
public class TransferEventEndState extends TransferEventImpl implements TransferEvent
{

    public String toString()
    {
        return  ("TransferEventEndState: " + super.toString());
    }
}
