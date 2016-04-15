
package org.alfresco.repo.transfer;

import java.io.InputStream;
import java.io.Writer;
import java.nio.channels.Channels;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;

/**
 * @author brian
 * 
 */
public class RepoTransferProgressMonitorImpl extends AbstractTransferProgressMonitor
{
    private NodeService nodeService;
    private ContentService contentService;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#getProgress(java.lang.String)
     */
    public TransferProgress getProgressInternal(final String transferId)
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

    public void storeError(final String transferId, final Throwable ex)
    {
        NodeRef nodeRef = getTransferRecord(transferId);
        // Write the exception onto the transfer record
        nodeService.setProperty(nodeRef, TransferModel.PROP_TRANSFER_ERROR, ex);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateProgress(java.lang.String, int)
     */
    public void updateProgressInternal(final String transferId, final int currPos)
    {
        NodeRef nodeRef = getTransferRecord(transferId);
        testCancelled(nodeRef);
        nodeService.setProperty(nodeRef, TransferModel.PROP_PROGRESS_POSITION, new Integer(currPos));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateProgress(java.lang.String, int, int)
     */
    public void updateProgressInternal(final String transferId, final int currPos, final int endPos)
    {
        NodeRef nodeRef = getTransferRecord(transferId);
        testCancelled(nodeRef);
        nodeService.setProperty(nodeRef, TransferModel.PROP_PROGRESS_POSITION, new Integer(currPos));
        nodeService.setProperty(nodeRef, TransferModel.PROP_PROGRESS_ENDPOINT, new Integer(endPos));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateStatus(java.lang.String,
     * org.alfresco.service.cmr.transfer.TransferProgress.Status)
     */
    public void updateStatusInternal(final String transferId, final Status status)
    {
        NodeRef nodeRef = getTransferRecord(transferId);
        nodeService.setProperty(nodeRef, TransferModel.PROP_TRANSFER_STATUS, status.toString());
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

    @Override
    protected Writer createUnderlyingLogWriter(String transferId)
    {
        NodeRef node = new NodeRef(transferId);
        ContentWriter contentWriter = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        contentWriter.setEncoding("UTF-8");
        return Channels.newWriter(contentWriter.getWritableChannel(), "UTF-8");
    }
}
