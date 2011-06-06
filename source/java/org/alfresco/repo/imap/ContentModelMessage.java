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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.imap.ImapService.EmailBodyFormat;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.namespace.QName;

public class ContentModelMessage extends AbstractMimeMessage
{

    public ContentModelMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry, boolean generateBody) throws MessagingException
    {
        super(fileInfo, serviceRegistry, generateBody);
    }

    @Override
    public void buildMessageInternal() throws MessagingException
    {
        if (generateBody != false)
        {
            setMessageHeaders();
            buildContentModelMessage();
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
            prop = (prop == null || prop.equals("")) ? messageFileInfo.getName() : prop;
            prop = MimeUtility.encodeText(prop, AlfrescoImapConst.UTF_8, null);
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
        MimeMultipart rootMultipart = new AlfrescoMimeMultipart("alternative", this.messageFileInfo);
        // Cite MOB-395: "email agent will be used to select an appropriate template" - we are not able to
        // detect an email agent so we use a default template for all messages.
        // See AlfrescoImapConst to see the possible templates to use.
        if (isMessageInSitesLibrary)
        {
            String bodyTxt = getEmailBodyText(EmailBodyFormat.SHARE_TEXT_PLAIN);
            rootMultipart.addBodyPart(getTextBodyPart(bodyTxt, EmailBodyFormat.SHARE_TEXT_PLAIN.getSubtype(), EmailBodyFormat.SHARE_TEXT_PLAIN.getMimeType()));
            String bodyHtml = getEmailBodyText(EmailBodyFormat.SHARE_TEXT_HTML);
            rootMultipart.addBodyPart(getTextBodyPart(bodyHtml, EmailBodyFormat.SHARE_TEXT_HTML.getSubtype(), EmailBodyFormat.SHARE_TEXT_HTML.getMimeType()));
        }
        else
        {
            String bodyTxt = getEmailBodyText(EmailBodyFormat.ALFRESCO_TEXT_PLAIN);
            rootMultipart.addBodyPart(getTextBodyPart(bodyTxt, EmailBodyFormat.ALFRESCO_TEXT_PLAIN.getSubtype(), EmailBodyFormat.ALFRESCO_TEXT_PLAIN.getMimeType()));
            String bodyHtml = getEmailBodyText(EmailBodyFormat.ALFRESCO_TEXT_HTML);
            rootMultipart.addBodyPart(getTextBodyPart(bodyHtml, EmailBodyFormat.ALFRESCO_TEXT_HTML.getSubtype(), EmailBodyFormat.ALFRESCO_TEXT_HTML.getMimeType()));
        }
        return rootMultipart;
    }

    private MimeBodyPart getTextBodyPart(String bodyText, String subtype, String mimeType) throws MessagingException
    {
        MimeBodyPart result = new MimeBodyPart();
        result.setText(bodyText, AlfrescoImapConst.UTF_8, subtype);
        result.addHeader(AlfrescoImapConst.CONTENT_TYPE, mimeType + AlfrescoImapConst.CHARSET_UTF8);
        result.addHeader(AlfrescoImapConst.CONTENT_TRANSFER_ENCODING, AlfrescoImapConst.BASE_64_ENCODING);
        return result;
    }

    
    class AlfrescoMimeMultipart extends MimeMultipart
    {
        public AlfrescoMimeMultipart(String subtype, FileInfo messageFileInfo)
        {
            super();
            String boundary = getBoundaryValue(messageFileInfo);
            ContentType cType = new ContentType("multipart", subtype, null);
            cType.setParameter("boundary", boundary);
            contentType = cType.toString();
        }

        public String getBoundaryValue(FileInfo messageFileInfo)
        {
            StringBuffer s = new StringBuffer();
            s.append("----=_Part_").append(messageFileInfo.getNodeRef().getId());
            return s.toString();
        }
    }


}
