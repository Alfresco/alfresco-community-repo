package org.alfresco.repo.transfer;

import java.io.Writer;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * @author mrogers
 */
public interface TransferDestinationReportWriter
{

    /**
     * Called at the start of the destination transfer report.
     * 
     * @param encoding the encoding to use, utf-8.
     * @param writer where to write the transfer report
     */
    public void startTransferReport(String encoding, Writer writer);
    
    /**
     * Called at the end of the destination transfer report.
     */
    public void endTransferReport();
    
    /**
     * a change of state 
     */
    public void writeChangeState(String state);
    
    /**
     * An ad-hoc comment
     */
    public void writeComment(String comment);
    
    /**
     * Reporting creation of a new node
     */
    public void writeCreated(NodeRef sourceNodeRef, NodeRef newNodeRef, NodeRef newParentNodeRef, String newPath);
    
    /**
     * Reporting update of an existing node
     */
    public void writeUpdated(NodeRef sourceNodeRef, NodeRef updatedNode, String updatedPath);
    
    /**
     * Reporting a node moved
     */
    public void writeMoved(NodeRef sourceNodeRef, NodeRef movedNodeRef, String oldPath, NodeRef newParentNodeRef, String newPath);
    
    /**
     * Reporting a node deleted
     */
    public void writeDeleted(NodeRef sourceNodeRef, NodeRef deletedNode, String oldPath);
    
    /**
     * Reporting an exception
     */
    public void writeException(Throwable t);
}
