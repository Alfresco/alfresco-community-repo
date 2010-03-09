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
package org.alfresco.repo.imap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

/**
 * This class is used to serve incoming IMAP message. E.g. when message is copied /moved into some IMAP older.
 * 
 * @author Arseny Kovalchuk
 */
public class IncomingImapMessage extends AbstractMimeMessage
{
    private Log logger = LogFactory.getLog(IncomingImapMessage.class);
    private ContentReader contentReader;
    /**
     * Constructs {@link IncomingImapMessage} object based on {@link MimeMessage}
     * 
     * @param fileInfo - reference to the {@link FileInfo} object representing the message.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     * @param message - {@link MimeMessage}
     * @throws MessagingException
     */
    public IncomingImapMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry, MimeMessage message) throws MessagingException
    {
        super(Session.getDefaultInstance(new Properties()));
        this.wrappedMessage = message; // temporary save it and then destroyed in writeContent() (to avoid memory leak with byte[] MimeMessage.content field)
        this.buildMessage(fileInfo, serviceRegistry);
    }

    @Override
    public void buildMessageInternal() throws MessagingException
    {
        setMessageHeaders();
        // Add Imap Content Aspect with properties
        NodeService nodeService = serviceRegistry.getNodeService();
        nodeService.addAspect(this.messageFileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT, null);
        imapService.setFlags(messageFileInfo, flags, true);
        // Write content
        writeContent();
    }

    /**
     * Writes the content of incoming message into Alfresco repository.
     * 
     * @throws MessagingException
     */
    private void writeContent() throws MessagingException
    {
        ContentWriter writer = serviceRegistry.getContentService().getWriter(messageFileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        try
        {
            OutputStream outputStream = writer.getContentOutputStream();
            wrappedMessage.writeTo(outputStream);
            outputStream.close();
            wrappedMessage = null; // it is not used any more and it is available to GC (to avoid memory leak with byte[] MimeMessage.content field)
            this.contentReader = serviceRegistry.getContentService().getReader(messageFileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
        }
        catch (ContentIOException e)
        {
            throw new MessagingException(e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    @Override
    protected InputStream getContentStream() throws MessagingException
    {
        try
        {
            if (this.contentStream == null)
            {
                this.contentStream = this.contentReader.getContentInputStream();
            }
            return this.contentStream;
        }
        catch (Exception e)
        {
            throw new MessagingException(e.getMessage(),e);
        }
    }

}
