package org.alfresco.repo.transfer;

import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferReceiver;

public class TransferCommitTransactionListener extends TransactionListenerAdapter
{
    private TransferReceiver receiver;
    private String transferId;

    public TransferCommitTransactionListener(String transferId, TransferReceiver receiver)
    {
        super();
        this.receiver = receiver;
        this.transferId = transferId;
    }

    @Override
    public void afterCommit()
    {
        updateTransferStatus(TransferProgress.Status.COMPLETE);
    }

    @Override
    public void afterRollback()
    {
        updateTransferStatus(TransferProgress.Status.ERROR);
    }

    private void updateTransferStatus(TransferProgress.Status status)
    {
        receiver.getProgressMonitor().updateStatus(transferId, status);
    }
}
