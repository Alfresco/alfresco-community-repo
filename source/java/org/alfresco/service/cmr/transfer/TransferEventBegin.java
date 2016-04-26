package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;

/**
 * TransferEventBegin is produced when a transfer has started.
 */
public class TransferEventBegin extends TransferEventImpl  
{
    private String transferId;
    
    public String toString()
    {   
        return "TransferEventBegin: " + transferId;
    }

    public void setTransferId(String transferId)
    {
        this.transferId = transferId;
    }

    public String getTransferId()
    {
        return transferId;
    }
}
