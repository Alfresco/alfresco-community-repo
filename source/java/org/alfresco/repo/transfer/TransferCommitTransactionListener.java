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
