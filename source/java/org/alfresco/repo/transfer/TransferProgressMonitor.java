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

/**
 * @author brian
 * 
 * The transfer progress monitor monitors each transfer
 * <p>
 * It contains a status, current position, end position, and a log.  
 * It can also store an exception. 
 * 
 */
public interface TransferProgressMonitor
{
    /**
     * log an ad-hoc message
     * @param transferId
     * @param obj
     * @throws TransferException
     */
    void logComment(String transferId, Object obj) throws TransferException;
    /**
     * log an ad-hoc message and an exception
     * @param transferId
     * @param obj
     * @param ex
     * @throws TransferException
     */
    void logException(String transferId, Object obj, Throwable ex) throws TransferException;
    
    /**
     * Log the creation of a new node
     * @param transferId
     * @param sourceNode
     * @param destNode
     * @param newPath
     * @param orphan
     */
    void logCreated(String transferId, NodeRef sourceNode, NodeRef destNode, NodeRef newParent, String newPath, boolean orphan);
    
    /**
     * Log the creation of a new node
     * @param transferId
     * @param sourceNode
     * @param destNode
     * @param path The path of the updated node
     * @param orphan
     */
    void logUpdated(String transferId, NodeRef sourceNode, NodeRef destNode, String path);
  
    
    /**
     * Log the deletion of a node
     * @param transferId
     * @param sourceNode
     * @param destNode
     * @param path The path of the deleted node
     * @param orphan
     */
    void logDeleted(String transferId, NodeRef sourceNode, NodeRef destNode, String path);
    
    /**
     * After the transfer has completed this method reads the log.
     * @param transferId
     * @return the log
     */

    void logMoved(String transferId, NodeRef sourceNodeRef,
            NodeRef destNodeRef, String oldPath, NodeRef newParent, String newPath);
    
    /**
     * update the progress of the specified transfer 
     * @param transferId
     * @param currPos
     * @throws TransferException
     */
    void updateProgress(String transferId, int currPos) throws TransferException;
    
    /** 
     * update the progress of the specified transfer and possibly change the end position.
     * @param transferId
     * @param currPos
     * @param endPos
     * @throws TransferException
     */
    void updateProgress(String transferId, int currPos, int endPos) throws TransferException;
    
    /**
     * update the startus of the transfer
     * @param transferId
     * @param status
     * @throws TransferException
     */
    void updateStatus(String transferId, TransferProgress.Status status) throws TransferException;
    
    /**
     * Read the progress of the 
     * @param transferId
     * @return the progress of the transfer
     * @throws TransferException
     */
    TransferProgress getProgress(String transferId) throws TransferException;
    
    InputStream getLogInputStream(String transferId) throws TransferException;
    

}
