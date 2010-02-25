/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 *
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
    public void log(String transferId, Object obj, Throwable ex) throws TransferException
    {
        localLog(transferId, obj, ex);
        delegate.log(transferId, obj, ex);
    }

    /**
     * @param transferId
     * @param obj
     * @throws TransferException
     * @see org.alfresco.repo.transfer.TransferProgressMonitor#log(java.lang.String, java.lang.Object)
     */
    public void log(String transferId, Object obj) throws TransferException
    {
        localLog(transferId, obj, null);
        delegate.log(transferId, obj);
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
}
