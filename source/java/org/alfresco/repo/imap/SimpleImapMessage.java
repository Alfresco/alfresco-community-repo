/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.imap;

import static org.alfresco.repo.imap.AlfrescoImapConst.BASE_64_ENCODING;
import static org.alfresco.repo.imap.AlfrescoImapConst.CONTENT_TRANSFER_ENCODING;
import static org.alfresco.repo.imap.AlfrescoImapConst.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extended MimeMessage to represent a content stored in the Alfresco repository.
 * 
 * @author Arseny Kovalchuk
 */
public class SimpleImapMessage extends AbstractMimeMessage
{
    private static Log logger = LogFactory.getLog(SimpleImapMessage.class);
    
    /**
     * Constructs {@link SimpleImapMessage} object.
     * 
     * @param fileInfo - reference to the {@link FileInfo} object representing the message.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     * @param generateBody - if {@code true} message body will be generated.
     * 
     * @throws MessagingException if generation of the body fails.
     */
    public SimpleImapMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry, boolean generateBody) throws MessagingException
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
        final NodeRef nodeRef = messageFileInfo.getNodeRef();
        if (serviceRegistry.getNodeService().hasAspect(nodeRef, ImapModel.ASPECT_IMAP_CONTENT))
        {
            buildRFC822Message();
        }
        else
        {
            buildContentModelMessage();
        }
    }

    private void buildRFC822Message() throws MessagingException
    {
        ContentService contentService = serviceRegistry.getContentService();
        ContentReader reader = contentService.getReader(messageFileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
        try
        {
            InputStream inputStream = reader.getContentInputStream();
            this.parse(inputStream);
            inputStream.close();
        }
        catch (ContentIOException e)
        {
            //logger.error(e);
            throw new MessagingException("The error occured during message creation from content stream.", e);
        }
        catch (IOException e)
        {
            //logger.error(e);
            throw new MessagingException("The error occured during message creation from content stream.", e);
        }
    }
    
    /**
     * This method builds {@link MimeMessage} based on {@link ContentModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private void buildContentModelMessage() throws MessagingException
    {
        Map<QName, Serializable> properties = messageFileInfo.getProperties();
        String prop = null;
        setSentDate(messageFileInfo.getModifiedDate());
        // Add FROM address
        Address[] addressList = buildSenderFromAddress();
        addFrom(addressList);
        // Add TO address
        addressList = buildRecipientToAddress();
        addRecipients(RecipientType.TO, addressList);
        prop = (String) properties.get(ContentModel.PROP_TITLE);
        try
        {
            prop = (prop == null) ? MimeUtility.encodeText(messageFileInfo.getName(), KOI8R_CHARSET, null) : MimeUtility.encodeText(prop, KOI8R_CHARSET, null);
        }
        catch (UnsupportedEncodingException e)
        {
            // ignore
        }
        setSubject(prop);
        setContent(buildContentModelMultipart());
    }

    /**
     * This method builds {@link Multipart} based on {@link ContentModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private Multipart buildContentModelMultipart() throws MessagingException
    {
        MimeMultipart rootMultipart = new MimeMultipart("alternative");
        // Cite MOB-395: "email agent will be used to select an appropriate template" - we are not able to
        // detect an email agent so we use a default template for all messages.
        // See AlfrescoImapConst to see the possible templates to use.
        String bodyTxt = getEmailBodyText(EmailBodyType.TEXT_PLAIN);
        rootMultipart.addBodyPart(getTextBodyPart(bodyTxt, EmailBodyType.TEXT_PLAIN.getSubtype()));
        String bodyHtml = getEmailBodyText(EmailBodyType.TEXT_HTML);
        rootMultipart.addBodyPart(getTextBodyPart(bodyHtml, EmailBodyType.TEXT_HTML.getSubtype()));
        return rootMultipart;
    }

    
    private MimeBodyPart getTextBodyPart(String bodyText, String subtype) throws MessagingException
    {
        MimeBodyPart result = new MimeBodyPart();
        result.setText(bodyText, UTF_8, subtype);
        result.addHeader(CONTENT_TRANSFER_ENCODING, BASE_64_ENCODING);
        return result;
    }

}
