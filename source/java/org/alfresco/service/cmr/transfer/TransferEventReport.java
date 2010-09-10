package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This event is that the destination transfer report has been written.
 * 
 * @author mrogers
 *
 */
public class TransferEventReport extends  TransferEventImpl implements TransferEvent
{
    public enum ReportType { SOURCE, DESTINATION };
    
    private NodeRef nodeRef;
    
    private ReportType reportType;
    
    public String toString()
    {
        return "TransferEventReportWritten: " + nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setReportType(ReportType reportType)
    {
        this.reportType = reportType;
    }

    public ReportType getReportType()
    {
        return reportType;
    }

}
