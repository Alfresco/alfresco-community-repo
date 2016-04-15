package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.cmr.transfer.TransferVersion;

/**
 * Information about a transfer which is in progress.
 *
 * @author Mark Rogers
 */
public class Transfer
{
    private String transferId;
    private TransferTarget transferTarget;
    private TransferVersion toVersion;
    

    public void setTransferId(String transferId)
    {
        this.transferId = transferId;
    }

    public String getTransferId()
    {
        return transferId;
    }
    
    // may also have capabilities of the remote system here (for when we are 
    // transfering accross versions)
    
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this == obj)
        {
            return true;
        }
        else if (obj instanceof Transfer == false)
        {
            return false;
        }
        Transfer that = (Transfer) obj;
        return (this.transferId.equals(that.getTransferId()));
    }
    
    public int hashCode()
    {
        return transferId.hashCode();
    }

    /**
     * @param target TransferTarget
     */
    public void setTransferTarget(TransferTarget target)
    {
        this.transferTarget = target;
    }

    /**
     * @return the transferTarget
     */
    public TransferTarget getTransferTarget()
    {
        return transferTarget;
    }
    
    public String toString()
    {
        return "TransferId" + transferId + ", target:" + transferTarget ;
    }

    public void setToVersion(TransferVersion toVersion)
    {
        this.toVersion = toVersion;
    }

    public TransferVersion getToVersion()
    {
        return toVersion;
    }
}
