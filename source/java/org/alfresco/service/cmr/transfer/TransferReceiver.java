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

package org.alfresco.service.cmr.transfer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.repo.transfer.TransferProgressMonitor;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author brian
 * The server side Transfer Receiver.
 */
public interface TransferReceiver
{
    /**
     * 
     * @param transferId
     * @return
     */
    File getStagingFolder(String transferId);
    
    /**
     * 
     * @param transferId
     * @return
     */
    NodeRef getTempFolder(String transferId);

    /**
     * Asks the receiver to setup a new transfer.
     * @return The identifier of the new transfer
     * @throws TransferException if an error occurred while setting up the transfer
     */
    String start() throws TransferException;

    /**
     * Asks the receiver to end (and clean up) the specified transfer
     * @param transferId The transfer to end
     * @throws TransferException If the process of ending the transfer fails
     */
    void end(String transferId) throws TransferException;

    /**
     * Store the specified snapshot file into the transfer staging area.
     * The specified transfer must currently be the holder of the transfer lock, otherwise an exception is thrown.
     * This operation does not close the supplied stream, so the caller must do it as appropriate. The caller 
     * should assume that the supplied stream has been fully read when this operation returns.
     * @param transferId The identifier of the transfer with which this snapshot is associated
     * @param snapshotStream The open stream that holds the snapshot file.
     * @throws TransferException If an error occurs while saving the snapshot file.
     */
    void saveSnapshot(String transferId, InputStream snapshotStream) throws TransferException;
    
    /**
     * Save a content item
     * @param transferId
     * @param contentId
     * @param contentStream
     * @throws TransferException
     */
    void saveContent(String transferId, String contentId, InputStream contentStream) throws TransferException;
    
    /**
     * Write the requsite (the bits required to support the Manifest) to the output stream.
     * @param requsiteStream an open stream to receive the requisite
     * @throws TransferException
     */
    void generateRequsite(String transferId, OutputStream requsiteStream) throws TransferException;
    
    /**
     * Prepare 
     * @param transferId
     * @throws TransferException
     */
    void prepare(String transferId) throws TransferException;
    
    /**
     * Abort
     * @param transferId
     * @throws TransferException
     */
    void cancel(String transferId) throws TransferException;
    
    /**
     * Commit asynchronously
     * @param transferId
     * @throws TransferException
     */
    void commitAsync(String transferId) throws TransferException;

    /**
     * Commit
     * @param transferId
     * @throws TransferException
     */
    void commit(String transferId) throws TransferException;
    
    /**
     * 
     * @param transferId
     * @return the trabsfer progress
     * @throws TransferException
     */
    TransferProgress getStatus(String transferId) throws TransferException;
    
    /**
     * 
     * @return
     */
    TransferProgressMonitor getProgressMonitor();
    
    /**
     * get the transfer report for the specified transfer
     * @param transferId
     */
    InputStream getTransferReport(String transferId);
}