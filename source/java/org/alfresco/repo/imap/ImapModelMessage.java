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

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimePartDataSource;
import javax.mail.util.SharedByteArrayInputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;

/**
 * Extended MimeMessage to represent a content stored in the Alfresco repository.
 * 
 * @author Arseny Kovalchuk
 */
public class ImapModelMessage extends AbstractMimeMessage
{
    /**
     * Constructs {@link ImapModelMessage} object.
     * 
     * @param fileInfo - reference to the {@link FileInfo} object representing the message.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     * @param generateBody - if {@code true} message body will be generated.
     * 
     * @throws MessagingException if generation of the body fails.
     */
    public ImapModelMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry, boolean generateBody) throws MessagingException
    {
        super(fileInfo, serviceRegistry, generateBody);
    }

    @Override
    public void buildMessageInternal() throws MessagingException
    {
        if (generateBody != false)
        {
            setMessageHeaders();
            buildImapMessage();
        }
    }

    /**
     * This method builds MimeMessage based on either ImapModel or ContentModel type.
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private void buildImapMessage() throws MessagingException
    {
        modified = false;
        saved = false;
        buildRFC822Message();
        saved = true;
    }

    private void buildRFC822Message() throws MessagingException
    {
        ContentService contentService = serviceRegistry.getContentService();
        ContentReader reader = contentService.getReader(messageFileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            this.parse(is);
        }
        catch (ContentIOException e)
        {
            //logger.error(e);
            throw new MessagingException("The error occured during message creation from content stream.", e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    throw new MessagingException("The error occured during message creation from content stream.", e);
                }
                is = null;
            }
        }
    }

    @Override
    protected InputStream getContentStream() throws MessagingException
    {
        try
        {
            if (this.contentStream == null)
            {
                if (content != null)
                {
                    return new SharedByteArrayInputStream(content);
                }
                else
                {
                    throw new MessagingException("No content");
                }
            }
            return this.contentStream;
        }
        catch (Exception e)
        {
            throw new MessagingException(e.getMessage(),e);
        }
    }
    
    /*
    protected void parse(InputStream inputstream) throws MessagingException
    {
        headers = createInternetHeaders(inputstream);
        contentStream = inputstream;
    }
    */
}
