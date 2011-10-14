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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 */
public class LoggingTransferProgressMonitorImpl implements TransferProgressMonitor
{
    private static final Log log = LogFactory.getLog(LoggingTransferProgressMonitorImpl.class);
    private TransferProgressMonitor delegate;
    
    /**
     * @param delegate the delegate to set
     */
    public void setDelegate(TransferProgressMonitor delegate)
    {
        this.delegate = delegate;
    }

    /**
     * @param transferId
     * @return
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#getProgress(java.lang.String)
     */
    public TransferProgress getProgress(String transferId) throws TransferException
    {
        return delegate.getProgress(transferId);
    }

    /**
     * @param transferId
     * @param obj
     * @param ex
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#log(java.lang.String, java.lang.Object, java.lang.Throwable)
     */
    public void logException(String transferId, Object obj, Throwable ex) throws TransferException
    {
        localLog(transferId, obj, ex);
        delegate.logException(transferId, obj, ex);
    }

    /**
     * @param transferId
     * @param obj
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#log(java.lang.String, java.lang.Object)
     */
    public void logComment(String transferId, Object obj) throws TransferException
    {
        localLog(transferId, obj, null);
        delegate.logComment(transferId, obj);
    }
    
    @Override
    public void logCreated(String transferId, NodeRef sourceNode,
            NodeRef destNode, NodeRef parentNode, String parentPath, boolean orphan)
    {
        delegate.logCreated(transferId, sourceNode, destNode, parentNode, parentPath, orphan);
    }
    
    @Override
    public void logUpdated(String transferId, NodeRef sourceNode,
            NodeRef destNode, String parentPath)
    {
        delegate.logUpdated(transferId, sourceNode, destNode, parentPath);
    }
    
    @Override
    public void logMoved(String transferId, NodeRef sourceNode,
            NodeRef destNode, String oldPath, NodeRef parentNodeRef, String parentPath)
    {
        delegate.logMoved(transferId, sourceNode, destNode, oldPath, parentNodeRef, parentPath);
    }
    
    @Override
    public void logDeleted(String transferId, NodeRef sourceNode,
            NodeRef destNode, String parentPath)
    {
        delegate.logDeleted(transferId, sourceNode, destNode, parentPath);
        
    }

    /**
     * @param transferId
     * @param currPos
     * @param endPos
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateProgress(java.lang.String, int, int)
     */
    public void updateProgress(String transferId, int currPos, int endPos) throws TransferException
    {
        if (loggingEnabled())
        {
            localLog(transferId, "Progress update: " + currPos + " out of " + endPos, null);
        }
        delegate.updateProgress(transferId, currPos, endPos);
    }

    /**
     * @param transferId
     * @param currPos
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateProgress(java.lang.String, int)
     */
    public void updateProgress(String transferId, int currPos) throws TransferException
    {
        if (loggingEnabled())
        {
            localLog(transferId, "Progress update: current position = " + currPos, null);
        }
        delegate.updateProgress(transferId, currPos);
    }

    /**
     * @param transferId
     * @param status
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#updateStatus(java.lang.String, org.alfresco.service.cmr.transfer.TransferProgress.Status)
     */
    public void updateStatus(String transferId, Status status) throws TransferException
    {
        if (loggingEnabled())
        {
            localLog(transferId, "Status update: " + status, null);
        }
        delegate.updateStatus(transferId, status);
    }

    private boolean loggingEnabled() 
    {
        return log.isInfoEnabled();
    }
    
    private void localLog(String transferId, Object obj, Throwable ex)
    {
        if (loggingEnabled()) 
        {
            String message = "Transfer Log (" + transferId +"): " + obj.toString(); 
            if (ex == null)
            {
                log.info(message);
            }
            else
            {
                log.info(message, ex);
            }
        }
    }

    public InputStream getLogInputStream(String transferId)
            throws TransferException
    {
        return delegate.getLogInputStream(transferId);
    }






}
