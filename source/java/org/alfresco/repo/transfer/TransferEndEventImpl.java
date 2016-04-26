package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.RangedTransferEvent;
import org.alfresco.service.cmr.transfer.TransferEndEvent;
import org.alfresco.service.cmr.transfer.TransferEvent;

/**
 * An abstract implementation of TransferEndEvent.

 * @see TransferEvent
 * @see RangedTransferEvent
 */
public class TransferEndEventImpl extends TransferEventImpl implements TransferEndEvent
{
    private NodeRef sourceReport;
    private NodeRef destinationReport;

    public void setSourceReport(NodeRef sourceReport)
    {
        this.sourceReport = sourceReport;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferEndEvent#getSourceReport()
     */
    @Override
    public NodeRef getSourceReport()
    {
        return sourceReport;
    }

    public void setDestinationReport(NodeRef destinationReport)
    {
        this.destinationReport = destinationReport;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferEndEvent#getDestinationReport()
     */
    @Override
    public NodeRef getDestinationReport()
    {
        return destinationReport;
    }

}
