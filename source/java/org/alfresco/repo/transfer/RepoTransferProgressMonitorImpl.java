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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
public class RepoTransferProgressMonitorImpl implements TransferProgressMonitor
{
    private static final Log log = LogFactory.getLog(RepoTransferProgressMonitorImpl.class);

    private static final String MSG_TRANSFER_NOT_FOUND = "transfer_service.receiver.transfer_not_found";
    private static final String MSG_TRANSFER_CANCELLED = "transfer_service.receiver.transfer_cancelled";

    private NodeService nodeService;
    private ContentService contentService;
    private TransactionService transactionService;
    private Map<String, WritableByteChannel> transferLogWriters = new TreeMap<String, WritableByteChannel>();

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#getProgress(java.lang.String)
     */
    public TransferProgress getProgress(final String transferId)
    {
        return transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<TransferProgress>()
                {
                    public TransferProgress execute() throws Throwable
                    {
                        NodeRef nodeRef = getTransferRecord(transferId);

                        TransferProgress progress = new TransferProgress();
                        progress.setStatus(TransferProgress.Status.valueOf((String) nodeService.getProperty(nodeRef,
                                TransferModel.PROP_TRANSFER_STATUS)));
                        progress.setCurrentPosition((Integer) nodeService.getProperty(nodeRef,
                                TransferModel.PROP_PROGRESS_POSITION));
                        progress.setEndPosition((Integer) nodeService.getProperty(nodeRef,
                                TransferModel.PROP_PROGRESS_ENDPOINT));
                        progress.setError((Throwable) nodeService.getProperty(nodeRef,
                                TransferModel.PROP_TRANSFER_ERROR));
                        return progress;
                    }
                }, false, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#log(java.lang.String, java.lang.Object)
     */
    public void log(final String transferId, final Object obj)
    {
        log(transferId, obj, null);
    }

    public void log(final String transferId, final Object obj, final Throwable ex)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        if (ex != null)
                        {
                            NodeRef nodeRef = getTransferRecord(transferId);
                            // Write the exception onto the transfer record
                            nodeService.setProperty(nodeRef, TransferModel.PROP_TRANSFER_ERROR, ex);
                        }
                        WritableByteChannel writer = getLogWriter(transferId);
                        Date now = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        String text = format.format(now) + " - " + obj.toString() + "\n";
                        if (ex != null)
                        {
                            text += ex.getMessage() + "\n";
                            StringWriter stringWriter = new StringWriter(1024);
                            PrintWriter errorWriter = new PrintWriter(stringWriter);
                            ex.printStackTrace(errorWriter);
                            text += stringWriter.toString();
                        }
                        try
                        {
                            writer.write(ByteBuffer.wrap(text.getBytes("UTF-8")));
                        }
                        catch (Exception ex)
                        {
                            if (log.isWarnEnabled())
                            {
                                log.warn("Unable to record transfer log information:\n " + text, ex);
                            }
                        }
                        return null;
                    }
                }, false, true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateProgress(java.lang.String, int)
     */
    public void updateProgress(final String transferId, final int currPos)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef nodeRef = getTransferRecord(transferId);
                        testCancelled(nodeRef);
                        nodeService.setProperty(nodeRef, TransferModel.PROP_PROGRESS_POSITION, new Integer(currPos));
                        return null;
                    }
                }, false, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateProgress(java.lang.String, int, int)
     */
    public void updateProgress(final String transferId, final int currPos, final int endPos)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef nodeRef = getTransferRecord(transferId);
                        testCancelled(nodeRef);
                        nodeService.setProperty(nodeRef, TransferModel.PROP_PROGRESS_POSITION, new Integer(currPos));
                        nodeService.setProperty(nodeRef, TransferModel.PROP_PROGRESS_ENDPOINT, new Integer(endPos));
                        return null;
                    }
                }, false, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateStatus(java.lang.String,
     * org.alfresco.service.cmr.transfer.TransferProgress.Status)
     */
    public void updateStatus(final String transferId, final Status status)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef nodeRef = getTransferRecord(transferId);
                        testCancelled(nodeRef);
                        String currentStatusString = (String)nodeService.getProperty(nodeRef, TransferModel.PROP_TRANSFER_STATUS);
                        Status currentStatus = Status.valueOf(currentStatusString);
                        //If the transfer has already reached a terminal state then we don't allow any further change
                        if (!TransferProgress.getTerminalStatuses().contains(currentStatus))
                        {
                            log(transferId, "Status update: " + status);
                            nodeService.setProperty(nodeRef, TransferModel.PROP_TRANSFER_STATUS, status.toString());
                            //If the transfer has now reached a terminal state then the make sure that the log channel is
                            //closed for it (if one was open).
                            if (TransferProgress.getTerminalStatuses().contains(status))
                            {
                                WritableByteChannel logChannel = transferLogWriters.remove(transferId);
                                if (logChannel != null)
                                {
                                    logChannel.close();
                                }
                            }
                        }
                        return null;
                    }
                }, false, true);
    }

    private void testCancelled(NodeRef transferRecord) throws TransferFatalException
    {
        Status currentStatus = Status.valueOf((String)nodeService.getProperty(transferRecord, TransferModel.PROP_TRANSFER_STATUS));
        if (Status.CANCELLED.equals(currentStatus))
        {
            throw new TransferFatalException(MSG_TRANSFER_CANCELLED, new Object[] { transferRecord.toString() });
        }
    }
    
    private NodeRef getTransferRecord(String transferId) throws TransferException
    {
        NodeRef nodeRef = new NodeRef(transferId);
        if (!nodeService.exists(nodeRef) || !nodeService.getType(nodeRef).equals(TransferModel.TYPE_TRANSFER_RECORD))
        {
            throw new TransferException(MSG_TRANSFER_NOT_FOUND, new Object[] { transferId });
        }
        return nodeRef;
    }

    private WritableByteChannel getLogWriter(String transferId)
    {
        WritableByteChannel channel = this.transferLogWriters.get(transferId);
        if (channel == null)
        {
            NodeRef node = new NodeRef(transferId);
            ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("text/plain");
            channel = writer.getWritableChannel();
            transferLogWriters.put(transferId, channel);
        }
        return channel;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService
     *            the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param transactionService
     *            the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
}
