/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.transfer;

import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.reportd.XMLTransferDestinationReportWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 */
public abstract class AbstractTransferProgressMonitor implements TransferProgressMonitor
{
    private static final Log log = LogFactory.getLog(AbstractTransferProgressMonitor.class);

    protected static final String MSG_TRANSFER_NOT_FOUND = "transfer_service.receiver.transfer_not_found";
    protected static final String MSG_TRANSFER_CANCELLED = "transfer_service.receiver.transfer_cancelled";

    private Map<String, TransferDestinationReportWriter> transferLogWriters = new TreeMap<String, TransferDestinationReportWriter>();
    private TransactionService transactionService;
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    
    
    @Override
    public TransferProgress getProgress(final String transferId) throws TransferException
    {
        return transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<TransferProgress>()
                {
                    public TransferProgress execute() throws Throwable
                    {
                        return getProgressInternal(transferId);
                    }
                }, false, true);
    }

    protected abstract TransferProgress getProgressInternal(String transferId);

    @Override
    public void updateProgress(final String transferId, final int currPos, final int endPos) throws TransferException
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        updateProgressInternal(transferId, currPos, endPos);
                        return null;
                    }
                }, false, true);
    }


    protected abstract void updateProgressInternal(String transferId, int currPos, int endPos);


    @Override
    public void updateProgress(final String transferId, final int currPos) throws TransferException
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        updateProgressInternal(transferId, currPos);
                        return null;
                    }
                }, false, true);
    }

    protected abstract void updateProgressInternal(String transferId, int currPos);

    @Override
    public final void updateStatus(final String transferId, final Status status) throws TransferException
    {
        Status currentStatus = getProgress(transferId).getStatus();
        
        //If the transfer has already reached a terminal state then we don't allow any further change
        if (!TransferProgress.getTerminalStatuses().contains(currentStatus))
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            TransferDestinationReportWriter writer = getLogWriter(transferId);
                            writer.writeChangeState(status.toString());
                            updateStatusInternal(transferId, status);
                            //If the transfer has now reached a terminal state then the make sure that the log channel is
                            //closed for it (if one was open).
                            if (TransferProgress.getTerminalStatuses().contains(status))
                            {
                                log.debug("closing destination transfer report");
                                writer.endTransferReport();
                                transferLogWriters.remove(transferId);
                            }
                            return null;
                        }
                    }, false, true);
        }
    }

    protected abstract void updateStatusInternal(String transferId, Status status);
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#log(java.lang.String, java.lang.Object)
     */
    public void logComment(final String transferId, final Object obj)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeComment(obj.toString());
    }

    public void logException(final String transferId, final Object obj, final Throwable ex)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeComment(obj.toString());
        if (ex != null)
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            storeError(transferId, ex);
                            return null;
                        }
                    }, false, true);
            writer.writeException(ex);
        }
    }
    
    protected abstract void storeError(String transferId, Throwable error);
    
    @Override
    public void logCreated(String transferId, 
            NodeRef sourceNode,
            NodeRef destNode,
            NodeRef parentNodeRef,
            String parentPath, 
            boolean orphan)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeCreated(sourceNode, destNode, parentNodeRef, parentPath);
    }
    
    @Override
    public void logUpdated(String transferId, NodeRef sourceNodeRef,
            NodeRef destNodeRef, String path)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeUpdated(sourceNodeRef, destNodeRef, path);       
    }
    
    @Override
    public void logMoved(String transferId, NodeRef sourceNodeRef,
            NodeRef destNodeRef, String oldPath, NodeRef newParentNodeRef, String newPath)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeMoved(sourceNodeRef, destNodeRef, oldPath, newParentNodeRef, newPath);       
    }
    
    @Override
    public void logDeleted(String transferId, 
            NodeRef sourceNodeRef,
            NodeRef destNodeRef, 
            String oldPath)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeDeleted(sourceNodeRef, destNodeRef, oldPath);
    }

    private TransferDestinationReportWriter getLogWriter(String transferId)
    {
        TransferDestinationReportWriter writer = this.transferLogWriters.get(transferId);
        if (writer == null)
        {
            writer = new  XMLTransferDestinationReportWriter();
            writer.startTransferReport("UTF-8", createUnderlyingLogWriter(transferId));
            transferLogWriters.put(transferId, writer);
        }
        return writer;
    }
    
    protected abstract Writer createUnderlyingLogWriter(String transferId);
}
