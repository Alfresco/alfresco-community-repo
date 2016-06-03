package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEndEventImpl;

/**
 * The success event indicates a successful transfer
 */
public class TransferEventSuccess extends TransferEndEventImpl
{
    public String toString()
    {
        return "TransferEventSuccess";
    }
}
