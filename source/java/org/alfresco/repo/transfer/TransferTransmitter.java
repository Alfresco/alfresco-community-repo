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

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * @author brian
 *
 */
public interface TransferTransmitter
{
    /**
     * Verify that the target is available
     * @param target
     * @throws TransferException
     */
    void verifyTarget(TransferTarget target) throws TransferException;
    
    /**
     * Begin a transfer, the transfer object returned will be used by subsequent 
     * calls to the transfer service.
     * 
     * @param target definition of where to transfer to.
     * @return the transfer object or null if the target cannot be locked.
     * @throws TransferException
     */
    Transfer begin(TransferTarget target) throws TransferException;
    
    /**
     * @param manifest, the transfer manifest file
     * @param transfer the transfer object returned by an earlier call to begin
     * @return the delta list.
     * @throws TransferException
     */
    DeltaList sendManifest(Transfer transfer, File manifest) throws TransferException;
    
    /**
     * Send the content of the specified urls
     * 
     * @param transfer the transfer object returned by an earlier call to begin
     * @param data the content to send
     * @throws TransferException
     */
    void sendContent(Transfer transfer, Set<ContentData> data);
    
    /**
     *
     * @param transfer the transfer object returned by an earlier call to begin
     * @throws TransferException
     */
    void prepare(Transfer transfer) throws TransferException;
    
    /**
     * @param transfer the transfer object returned by an earlier call to begin
     * @throws TransferException 
     */
    void commit(Transfer transfer) throws TransferException;
    
    /**
     * Abort the transfer
     * @param transfer the transfer object returned by an earlier call to begin 
     * @throws TransferException 
     */
    void abort(Transfer transfer) throws TransferException;
    
    /**
     * Get Async Messages for a transfer.
     * Server Side Messages.
     * @return messages
     */
    Set<TransferMessage> getMessages(Transfer transfer);
}