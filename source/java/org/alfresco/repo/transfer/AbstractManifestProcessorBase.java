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
        catch(Exception ex)
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
        catch (Exception ex)
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
        catch (Exception ex)
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
        catch (Exception ex)
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
        catch (Exception ex)
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
    
    private void handleException(TransferManifestNode node, Exception ex)
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
        catch (Exception e)
        {
            //Nothing really to be done here
            log.warn("Failed to mark transaction as rollback-only in response to an error", e);
        }
        TransferProgressMonitor monitor = receiver.getProgressMonitor();
        String message = (node != null) ? "Error while processing incoming node " + node.getNodeRef() :
            "Error processing commit";
        
        monitor.log(transferId, message, ex);
        //Any non-fatal transfer exception is logged and then skipped - the transfer continues 
        //(albeit with a guaranteed rollback at the end).
        //A fatal transfer exception is rethrown and causes the transfer to end immediately.
        //Any non-transfer exception is assumed to be fatal, so is wrapped in a fatal exception
        //and thrown.
        if (TransferFatalException.class.isAssignableFrom(ex.getClass()))
        {
            throw (TransferFatalException)ex;
        } 
        else if (!TransferException.class.isAssignableFrom(ex.getClass()))
        {
            throw new TransferFatalException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, ex);
        }
    }
    
    protected void logProgress(String message)
    {
        receiver.getProgressMonitor().log(transferId, message);
    }
}
