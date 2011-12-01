/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.io.File;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This abstract class handles the progress monitoring functionality as well as providing
 * some utility methods for sub-classes.
 * @author Brian
 */
public abstract class AbstractManifestProcessorBase implements TransferManifestProcessor
{
    private static final Log log = LogFactory.getLog(AbstractManifestProcessorBase.class);
    private static final String MSG_ERROR_WHILE_COMMITTING_TRANSFER = "transfer_service.receiver.error_committing_transfer";

    private TransferReceiver receiver;
    private String transferId;
    private int targetEndProgress;
    private int currProgress;
    
    public AbstractManifestProcessorBase(TransferReceiver receiver, String transferId)
    {
        this.receiver = receiver;
        this.transferId = transferId;
    }
    
    public final void endTransferManifest()
    {
        receiver.getProgressMonitor().updateProgress(transferId, this.targetEndProgress);
        try 
        {
            endManifest();
        }
        catch(Throwable ex)
        {
            handleException(null, ex);
        }
    }

    protected abstract void endManifest();

    public final void processTransferManifestNode(TransferManifestNormalNode node)
    {
        incrementNodeCounter();
        try
        {
            processNode(node);
        }
        catch (Throwable ex)
        {
            handleException(node, ex);
        }
    }

    protected abstract void processNode(TransferManifestNormalNode node) throws TransferProcessingException;

    public final void processTransferManifestNode(TransferManifestDeletedNode node)
    {
        incrementNodeCounter();
        try
        {
            processNode(node);
        }
        catch (Throwable ex)
        {
            handleException(node, ex);
        }
    }

    protected abstract void processNode(TransferManifestDeletedNode node) throws TransferProcessingException;

    public final void processTransferManifiestHeader(TransferManifestHeader header)
    {
        TransferProgressMonitor progressMonitor = receiver.getProgressMonitor();
        TransferProgress progress = progressMonitor.getProgress(transferId);
        int newEndPos = progress.getEndPosition() + header.getNodeCount();
        progressMonitor.updateProgress(transferId, progress.getCurrentPosition(), newEndPos);
        targetEndProgress = newEndPos;
        currProgress = progress.getCurrentPosition();
        try 
        {
            processHeader(header);
        }
        catch (Throwable ex)
        {
            handleException(null, ex);
        }
    }
    
    protected abstract void processHeader(TransferManifestHeader header);

    public final void startTransferManifest()
    {
        try
        {
            startManifest();
        }
        catch (Throwable ex)
        {
            handleException(null, ex);
        }
    }

    protected abstract void startManifest();

    private void incrementNodeCounter()
    {
        currProgress++;
        if (currProgress % 20 == 0)
        {
            receiver.getProgressMonitor().updateProgress(transferId, currProgress);
        }
    }
    
    /**
     * Given the node ref, this method constructs the appropriate ChildAssociationRef that would place this node in the
     * transfer's temporary folder. Useful when handling orphans.
     * 
     * @param nodeRef
     * @return
     */
    protected ChildAssociationRef getTemporaryLocation(NodeRef nodeRef)
    {
        NodeRef parentNodeRef = receiver.getTempFolder(transferId);
        QName parentAssocType = TransferModel.ASSOC_TRANSFER_ORPHAN;
        QName parentAssocName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, nodeRef.getId());
        return new ChildAssociationRef(parentAssocType, parentNodeRef, parentAssocName, nodeRef, true, -1);
    }

    protected File getStagingFolder()
    {
        return receiver.getStagingFolder(transferId);
    }
    
    protected TransferReceiver getReceiver()
    {
        return receiver;
    }
    
    protected String getTransferId()
    {
        return transferId;
    }
    
    private void handleException(TransferManifestNode node, Throwable ex)
    {
        try
        {
            UserTransaction tx = RetryingTransactionHelper.getActiveUserTransaction();
            if (tx != null)
            {
                tx.setRollbackOnly();
                log.debug("Successfully marked transaction for rollback.");
            }
        }
        catch (Throwable e)
        {
            //Nothing really to be done here
            log.warn("Failed to mark transaction as rollback-only in response to an error", e);
        }
        
        try
        {
            TransferProgressMonitor monitor = receiver.getProgressMonitor();
            String message = (node != null) ? "Error while processing incoming node " + node.getNodeRef() :
                "Error processing commit";
            
            monitor.logException(transferId, message, ex);
        }
        catch(Throwable t)
        {
            //Nothing really to be done here
            log.warn("Failed to record exception in transfer log due to an exception", t);
        }
        
        //Any non-fatal transfer exception is logged and then skipped - the transfer continues 
        //(albeit with a guaranteed rollback at the end).
        //A fatal transfer exception is rethrown and causes the transfer to end immediately.
        //Any non-transfer exception is assumed to be fatal, so is wrapped in a fatal exception
        //and thrown.
        if (TransferFatalException.class.isAssignableFrom(ex.getClass()))
        {
            callLocalExceptionHandler(node, ex);
            throw (TransferFatalException)ex;
        } 
        else if (!TransferException.class.isAssignableFrom(ex.getClass()))
        {
            callLocalExceptionHandler(node, ex);
            throw new TransferFatalException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, ex);
        }
    }

    private void callLocalExceptionHandler(TransferManifestNode node, Throwable ex)
    {
        try
        {
            localHandleException(node, ex);
        }
        catch(Throwable t)
        {
            //Nothing really to be done here
            log.warn("Caught and discarded exception thrown from custom exception handler", t);
        }
    }
    
    /**
     * This method is invoked if an exception or error occurs while processing the manifest.
     * By default it does nothing, but classes that extend this class can override this to provide
     * custom clean-up. 
     * @param node
     * @param ex
     */
    protected void localHandleException(TransferManifestNode node, Throwable ex)
    {
        //NO-OP - override to add custom clean-up
    }

    protected void logComment(String message)
    {
        receiver.getProgressMonitor().logComment(transferId, message);
    }
    protected void logCreated(NodeRef sourceNode, NodeRef destNode, NodeRef newParentNode, String parentPath, boolean orphan)
    {
        receiver.getProgressMonitor().logCreated(transferId, sourceNode, destNode, newParentNode, parentPath, orphan);
    }
    protected void logDeleted(NodeRef sourceNode, NodeRef destNode, String parentPath)
    {
        receiver.getProgressMonitor().logDeleted(transferId, sourceNode, destNode, parentPath);
    }
    protected void logUpdated(NodeRef sourceNode, NodeRef destNode, String newPath)
    {
        receiver.getProgressMonitor().logUpdated(transferId, sourceNode, destNode, newPath);
    }
    protected void logMoved(NodeRef sourceNode, NodeRef destNode, String oldPath, NodeRef newParent, String newPath)
    {
        receiver.getProgressMonitor().logMoved(transferId, sourceNode, destNode, oldPath, newParent, newPath);
    }
}
