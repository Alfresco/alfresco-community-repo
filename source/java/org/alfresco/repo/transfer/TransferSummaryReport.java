package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;

/**
 * Used to log relevant changes to transfered files into a summary report of the
 * entire replication job;
 * 
 * It should log: created files, deleted, moved, *modified files and relevant
 * errors while handling the files;
 * 
 * *modified: each implementation would chose the definition of modified(actual
 * content modification/ new version/ some properties updated)
 * 
 */
public interface TransferSummaryReport
{

    /**
     * Log the creation of a new node
     *
     * @param sourceNode NodeRef
     * @param destNode NodeRef
     * @param newParent NodeRef
     * @param newPath String
     * @param orphan boolean
     */
    void logSummaryCreated(NodeRef sourceNode, NodeRef destNode, NodeRef newParent, String newPath, boolean orphan);

    /**
     * Log the creation of a new node
     *
     * @param sourceNode NodeRef
     * @param destNode NodeRef
     * @param path The path of the updated node
     */
    void logSummaryUpdated(NodeRef sourceNode, NodeRef destNode, String path);

    /**
     * Log the deletion of a node
     * 
     * @param sourceNode NodeRef
     * @param destNode NodeRef
     * @param path The path of the updated node
     */
    void logSummaryDeleted(NodeRef sourceNode, NodeRef destNode, String path);

    /**
     * After the transfer has completed this method reads the log.
     *
     * @param sourceNodeRef NodeRef
     * @param destNodeRef NodeRef
     * @param oldPath String
     * @param newParent NodeRef
     * @param newPath String
     *
     */
    void logSummaryMoved(NodeRef sourceNodeRef, NodeRef destNodeRef, String oldPath, NodeRef newParent, String newPath);

    /**
     * log an ad-hoc message
     * 
     * @param obj Object
     * @throws TransferException
     */
    void logSummaryComment(Object obj) throws TransferException;

    /**
     * log an ad-hoc message and an exception
     * 
     * @param obj Object
     * @param ex Throwable
     * @throws TransferException
     */
    void logSummaryException(Object obj, Throwable ex) throws TransferException;

    /**
     * update the status of the transfer
     * 
     * @param status TransferProgress.Status
     * @throws TransferException
     */
    void logSummaryUpdateStatus(TransferProgress.Status status) throws TransferException;

    /**
     * called to close the report file
     */
    void finishSummaryReport();

}
