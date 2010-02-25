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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * This class delegates transfer service to the transfer receiver without 
 * using any networking.
 *
 * It is used for unit testing the transfer service without requiring two instance 
 * of the repository to be running.
 *
 * @author Mark Rogers
 */
public class UnitTestInProcessTransmitterImpl implements TransferTransmitter
{
    private TransferReceiver receiver;
    
    private ContentService contentService;
    
    public UnitTestInProcessTransmitterImpl(TransferReceiver receiver, ContentService contentService)
    {
        this.receiver = receiver;
        this.contentService = contentService;
    }
    
    public Transfer begin(TransferTarget target) throws TransferException
    {
        Transfer transfer = new Transfer();
        String transferId = receiver.start();
        transfer.setTransferId(transferId);
        transfer.setTransferTarget(target);
        return transfer;
    }

    public void abort(Transfer transfer) throws TransferException
    {
        String transferId = transfer.getTransferId();
        receiver.abort(transferId);
    }

    public void commit(Transfer transfer) throws TransferException
    {
        String transferId = transfer.getTransferId();
        receiver.commit(transferId);
    }

    public Set<TransferMessage> getMessages(Transfer transfer)
    {
        String transferId = transfer.getTransferId();
        return null;
    }

    public void prepare(Transfer transfer) throws TransferException
    {
        String transferId = transfer.getTransferId();
        receiver.prepare(transferId);
    }

    public void sendContent(Transfer transfer, Set<ContentData> data)
    {
        String transferId = transfer.getTransferId();
        
        for(ContentData content : data)
        {
            String contentUrl = content.getContentUrl();
            String fileName = TransferCommons.URLToPartName(contentUrl);

            InputStream contentStream = getContentService().getRawReader(contentUrl).getContentInputStream();
            receiver.saveContent(transferId, fileName, contentStream);
        }
    }

    public DeltaList sendManifest(Transfer transfer, File manifest) throws TransferException
    {
        try
        {
            String transferId = transfer.getTransferId();
            FileInputStream fs = new FileInputStream(manifest);
            receiver.saveSnapshot(transferId, fs);
        }
        catch (FileNotFoundException error)
        {
            throw new TransferException("test error", error);
        }
        return null;

    }

    public void verifyTarget(TransferTarget target) throws TransferException
    {

    }

    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    public TransferReceiver getReceiver()
    {
        return receiver;
    }

    private void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    private ContentService getContentService()
    {
        return contentService;
    }
}
