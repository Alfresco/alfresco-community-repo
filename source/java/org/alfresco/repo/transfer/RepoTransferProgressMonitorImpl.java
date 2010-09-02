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

import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.reportd.XMLTransferDestinationReportWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
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
    //private Map<String, WritableByteChannel> transferLogWriters = new TreeMap<String, WritableByteChannel>();
    private Map<String, TransferDestinationReportWriter> transferLogWriters = new TreeMap<String, TransferDestinationReportWriter>();

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
    public void logComment(final String transferId, final Object obj)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeComment(obj.toString());
    }

    public void logException(final String transferId, final Object obj, final Throwable ex)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        TransferDestinationReportWriter writer = getLogWriter(transferId);
                        
                        writer.writeComment(obj.toString());
                        
                        if (ex != null)
                        {
                            NodeRef nodeRef = getTransferRecord(transferId);
                            // Write the exception onto the transfer record
                            nodeService.setProperty(nodeRef, TransferModel.PROP_TRANSFER_ERROR, ex);
                            writer.writeException(ex);
                        }

                        return null;
                    }
                }, false, true);
    }
    
    @Override
    public void logCreated(String transferId, 
            NodeRef sourceNode,
            NodeRef destNode,
            NodeRef parentNodeRef,
            Path parentPath, 
            boolean orphan)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeCreated(sourceNode, destNode, parentNodeRef, parentPath);
    }
    
    @Override
    public void logUpdated(String transferId, NodeRef sourceNodeRef,
            NodeRef destNodeRef, Path path)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeUpdated(sourceNodeRef, destNodeRef, path);       
    }
    
    @Override
    public void logMoved(String transferId, NodeRef sourceNodeRef,
            NodeRef destNodeRef, Path oldPath, NodeRef newParentNodeRef, Path newPath)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeMoved(sourceNodeRef, destNodeRef, oldPath, newParentNodeRef, newPath);       
    }
    
    @Override
    public void logDeleted(String transferId, 
            NodeRef sourceNodeRef,
            NodeRef destNodeRef, 
            Path oldPath)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeDeleted(sourceNodeRef, destNodeRef, oldPath);
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
                        
                        TransferDestinationReportWriter writer = getLogWriter(transferId);
                        writer.writeChangeState(status.toString());
                        
                        //If the transfer has already reached a terminal state then we don't allow any further change
                        if (!TransferProgress.getTerminalStatuses().contains(currentStatus))
                        {
                            nodeService.setProperty(nodeRef, TransferModel.PROP_TRANSFER_STATUS, status.toString());
                            //If the transfer has now reached a terminal state then the make sure that the log channel is
                            //closed for it (if one was open).
                            if (TransferProgress.getTerminalStatuses().contains(status))
                            {
                                log.debug("closing destination transfer report");
                                writer.endTransferReport();
                                transferLogWriters.remove(transferId);
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

    private  TransferDestinationReportWriter getLogWriter(String transferId)
    {
        TransferDestinationReportWriter writer = this.transferLogWriters.get(transferId);
        if (writer == null)
        {
            NodeRef node = new NodeRef(transferId);
            ContentWriter contentWriter = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
            contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
            contentWriter.setEncoding("UTF-8");
            
            writer = new  XMLTransferDestinationReportWriter();
            try
            {
                writer.startTransferReport("UTF-8", Channels.newWriter(contentWriter.getWritableChannel(), "UTF-8"));
            } 
            catch (ContentIOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            transferLogWriters.put(transferId, writer);
        }
        return writer;
    }
    
    public InputStream getLogInputStream(String transferId)
            throws TransferException
    {
        NodeRef transferRecord = getTransferRecord(transferId);
        
        ContentReader reader = contentService.getReader(transferRecord, ContentModel.PROP_CONTENT); 
        
        if(reader != null)
        {
            return reader.getContentInputStream();
        }
        else
        {
            return null;
        }
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
