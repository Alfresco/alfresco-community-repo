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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;

/**
 * A delegating implementation of the {@link TransferProgressMonitor} interface that
 * keeps a record of the nodes that have been changed during the course of processing
 * an incoming transfer. Used by the {@link RepoTransferReceiverImpl}.
 * @author Brian
 * @since 3.4
 */
/*package*/ final class ChangeCapturingProgressMonitor implements TransferProgressMonitor
{
    private final TransferProgressMonitor delegate;

    public static class TransferChangesRecord
    {
        private final List<NodeRef> createdNodes = new LinkedList<NodeRef>();
        private final List<NodeRef> updatedNodes = new LinkedList<NodeRef>();
        private final List<NodeRef> deletedNodes = new LinkedList<NodeRef>();

        public List<NodeRef> getCreatedNodes()
        {
            return createdNodes;
        }

        public List<NodeRef> getUpdatedNodes()
        {
            return updatedNodes;
        }

        public List<NodeRef> getDeletedNodes()
        {
            return deletedNodes;
        }

        public void addDeletedNode(NodeRef nodeRef)
        {
            deletedNodes.add(nodeRef);
        }

        public void addUpdatedNode(NodeRef nodeRef)
        {
            updatedNodes.add(nodeRef);
        }

        public void addCreatedNode(NodeRef nodeRef)
        {
            createdNodes.add(nodeRef);
        }
        
        public void reset()
        {
            createdNodes.clear();
            updatedNodes.clear();
            deletedNodes.clear();
        }
    }

    private final TreeMap<String, TransferChangesRecord> changeRecords = new TreeMap<String, TransferChangesRecord>();
    
    public ChangeCapturingProgressMonitor(TransferProgressMonitor delegatedProgressMonitor)
    {
        delegate = delegatedProgressMonitor;
    }

    public InputStream getLogInputStream(String transferId) throws TransferException
    {
        return delegate.getLogInputStream(transferId);
    }

    public TransferProgress getProgress(String transferId) throws TransferException
    {
        return delegate.getProgress(transferId);
    }

    public void logComment(String transferId, Object obj) throws TransferException
    {
        delegate.logComment(transferId, obj);
    }

    public void logCreated(String transferId, NodeRef sourceNode, NodeRef destNode, NodeRef newParent, String newPath,
            boolean orphan)
    {
        delegate.logCreated(transferId, sourceNode, destNode, newParent, newPath, orphan);
        getChangesRecord(transferId).addCreatedNode(destNode);
    }

    public void logDeleted(String transferId, NodeRef sourceNode, NodeRef destNode, String parentPath)
    {
        delegate.logDeleted(transferId, sourceNode, destNode, parentPath);
        getChangesRecord(transferId).addDeletedNode(destNode);
    }

    public void logException(String transferId, Object obj, Throwable ex) throws TransferException
    {
        delegate.logException(transferId, obj, ex);
    }

    public void logMoved(String transferId, NodeRef sourceNodeRef, NodeRef destNodeRef, String oldPath,
            NodeRef newParent, String newPath)
    {
        delegate.logMoved(transferId, sourceNodeRef, destNodeRef, oldPath, newParent, newPath);
    }

    public void logUpdated(String transferId, NodeRef sourceNode, NodeRef destNode, String parentPath)
    {
        delegate.logUpdated(transferId, sourceNode, destNode, parentPath);
        getChangesRecord(transferId).addUpdatedNode(destNode);
    }

    public void updateProgress(String transferId, int currPos, int endPos) throws TransferException
    {
        delegate.updateProgress(transferId, currPos, endPos);
    }

    public void updateProgress(String transferId, int currPos) throws TransferException
    {
        delegate.updateProgress(transferId, currPos);
    }

    public void updateStatus(String transferId, Status status) throws TransferException
    {
        delegate.updateStatus(transferId, status);
        //If we are entering a "don't commit" state then reset the changes record
        //for this transfer, since the effective result is "no change".
        if (status == Status.CANCELLED || status == Status.ERROR)
        {
            getChangesRecord(transferId).reset();
        }
    }

    public TransferChangesRecord removeChangeRecord(String transferId)
    {
        return changeRecords.remove(transferId);
    }
    
    private TransferChangesRecord getChangesRecord(String transferId)
    {
        TransferChangesRecord record = changeRecords.get(transferId);
        if (record == null)
        {
            record = new TransferChangesRecord();
            changeRecords.put(transferId, record);
        }
        return record;
    }
}
