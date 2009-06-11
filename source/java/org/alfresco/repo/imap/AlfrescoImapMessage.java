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
import static org.alfresco.repo.imap.AlfrescoImapConst.CONTENT_ID;
import static org.alfresco.repo.imap.AlfrescoImapConst.CONTENT_TRANSFER_ENCODING;
import static org.alfresco.repo.imap.AlfrescoImapConst.CONTENT_TYPE;
import static org.alfresco.repo.imap.AlfrescoImapConst.MIME_VERSION;
import static org.alfresco.repo.imap.AlfrescoImapConst.UTF_8;
import static org.alfresco.repo.imap.AlfrescoImapConst.X_ALF_NODEREF_ID;
import static org.alfresco.repo.imap.AlfrescoImapConst.X_ALF_SERVER_UID;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.ImapHelper.EmailBodyType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extended MimeMessage to represent a content stored in the Alfresco repository.
 * 
 * @author Arseny Kovalchuk
 */
public class AlfrescoImapMessage extends MimeMessage
{
    /** Used if imapHelper.getDefaultFromAddress is not set */
    private static final String DEFAULT_EMAIL_FROM = "alfresco@alfresco.org";
    private static final String DEFAULT_EMAIL_TO = DEFAULT_EMAIL_FROM;
    private static final String KOI8R_CHARSET = "koi8-r";

    private static Log logger = LogFactory.getLog(AlfrescoImapMessage.class);

    private ImapHelper imapHelper;
    private FileInfo messageInfo;

    /**
     * Constructs {@link AlfrescoImapMessage} object.
     * 
     * @param fileInfo - reference to the {@link FileInfo} object representing the message.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     * @param generateBody - if {@code true} message body will be generated.
     * 
     * @throws MessagingException if generation of the body fails.
     */
    public AlfrescoImapMessage(FileInfo fileInfo, ImapHelper imapHelper, boolean generateBody) throws MessagingException
    {
        super(Session.getDefaultInstance(new Properties()));
        this.messageInfo = fileInfo;
        this.imapHelper = imapHelper;
        if (generateBody)
        {
            setMessageHeaders();
            buildMessage();
        }
    }

    /**
     * Constructs {@link AlfrescoImapMessage} object.
     * 
     * @param fileInfo - reference to the {@link FileInfo} object representing the message.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     * @param message - {@link MimeMessage}
     * @throws MessagingException
     */
    public AlfrescoImapMessage(FileInfo fileInfo, ImapHelper imapHelper, MimeMessage message) throws MessagingException
    {
        super(message);
        this.messageInfo = fileInfo;
        this.imapHelper = imapHelper;

        setMessageHeaders();
        final NodeRef nodeRef = fileInfo.getNodeRef();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ImapModel.PROP_MESSAGE_FROM, InternetAddress.toString(message.getFrom()));
        props.put(ImapModel.PROP_MESSAGE_TO, InternetAddress.toString(message.getRecipients(RecipientType.TO)));
        props.put(ImapModel.PROP_MESSAGE_CC, InternetAddress.toString(message.getRecipients(RecipientType.CC)));

        String[] subj = message.getHeader("Subject");
        if (subj.length > 0)
        {
            props.put(ImapModel.PROP_MESSAGE_SUBJECT, subj[0]);
            imapHelper.getNodeService().setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, subj[0]);
        }

        Map<QName, Serializable> allprops = imapHelper.getNodeService().getProperties(fileInfo.getNodeRef());
        allprops.putAll(props);
        imapHelper.getNodeService().setProperties(nodeRef, allprops);
        // setContent(buildMultipart(fileInfo)); - disabled for better performance.
    }

    /**
     * Returns message flags.
     * 
     * @return {@link Flags}
     */
    @Override
    public synchronized Flags getFlags()
    {
        return imapHelper.getFlags(messageInfo);
    }

    
    /**
     * Sets message flags.
     * 
     * @param flags - {@link Flags} object.
     * @param value - flags value.
     */
    @Override
    public synchronized void setFlags(Flags flags, boolean value) throws MessagingException
    {
        imapHelper.setFlags(messageInfo, flags, value);
    }

    
    /**
     * Returns {@link FileInfo} object representing message in Alfresco.
     * 
     * @return reference to the {@link FileInfo} object.
     */
    public FileInfo getMessageInfo()
    {
        return messageInfo;
    }

    private void setMessageHeaders() throws MessagingException
    {
        setHeader(MIME_VERSION, "1.0");
        // Optional headers for further implementation of multiple Alfresco server support.
        setHeader(X_ALF_NODEREF_ID, messageInfo.getNodeRef().getId());
        setHeader(X_ALF_SERVER_UID, imapHelper.getAlfrescoServerUID());
    }

    /**
     * This method builds MimeMessage based on either ImapModel or ContentModel type.
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private void buildMessage() throws MessagingException
    {
        final NodeRef nodeRef = messageInfo.getNodeRef();
        if (ImapModel.TYPE_IMAP_CONTENT.equals(imapHelper.getNodeService().getType(nodeRef)))
        {
            buildImapModelMessage();
        }
        else
        {
            buildContentModelMessage();
        }
    }

    /**
     * This method builds MimeMessage based on {@link ImapModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private void buildImapModelMessage() throws MessagingException
    {
        Map<QName, Serializable> properties = messageInfo.getProperties();
        setSentDate(messageInfo.getModifiedDate());
        String prop = (String) properties.get(ImapModel.PROP_MESSAGE_FROM);
        addFromInternal(prop);
        prop = (String) properties.get(ImapModel.PROP_MESSAGE_TO);

        if (prop != null && prop.length() > 0)
        {
            addRecipients(RecipientType.TO, InternetAddress.parse(prop));
        }
        else
        {
            addRecipients(RecipientType.TO, DEFAULT_EMAIL_TO);
        }

        prop = (String) properties.get(ImapModel.PROP_MESSAGE_CC);
        if (prop != null && prop.length() > 0)
        {
            addRecipients(RecipientType.CC, InternetAddress.parse(prop));
        }

        prop = (String) properties.get(ImapModel.PROP_MESSAGE_SUBJECT);
        setSubject(prop == null ? messageInfo.getName() : prop);

        setContent(buildImapModelMultipart());

    }

    /**
     * This method builds {@link MimeMessage} based on {@link ContentModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private void buildContentModelMessage() throws MessagingException
    {
        Map<QName, Serializable> properties = messageInfo.getProperties();
        String prop = null;
        setSentDate(messageInfo.getModifiedDate());
        // Add FROM address
        Address[] addressList = buildSenderFromAddress(properties);
        addFrom(addressList);
        // Add TO address
        addressList = buildRecipientToAddress();
        addRecipients(RecipientType.TO, addressList);
        prop = (String) properties.get(ContentModel.PROP_TITLE);
        try
        {
            prop = (prop == null) ? MimeUtility.encodeText(messageInfo.getName(), KOI8R_CHARSET, null) : MimeUtility.encodeText(prop, KOI8R_CHARSET, null);
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
        String bodyTxt = imapHelper.getEmailBodyText(messageInfo.getNodeRef(), EmailBodyType.TEXT_PLAIN);
        rootMultipart.addBodyPart(getTextBodyPart(bodyTxt, EmailBodyType.TEXT_PLAIN.getSubtype()));
        String bodyHtml = imapHelper.getEmailBodyText(messageInfo.getNodeRef(), EmailBodyType.TEXT_HTML);
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

    /**
     * This method builds {@link Multipart} based on {@link ImapModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private Multipart buildImapModelMultipart() throws MessagingException
    {
        DataSource source = null;
        String errorMessage = null;

        // Root multipart - multipart/mixed
        MimeMultipart rootMultipart = new MimeMultipart("mixed");
        // Message body - multipart/alternative - consists of two parts: text/plain and text/html
        MimeMultipart messageBody = new MimeMultipart("alternative");
        // <------------------------ text html body part ------------------------>
        List<FileInfo> bodyHtmls = imapHelper.searchFiles(messageInfo.getNodeRef(), "*.html", ImapModel.TYPE_IMAP_BODY, false);
        ContentType contentType = null;
        MimeBodyPart textHtmlBodyPart = null;
        if (bodyHtmls != null && bodyHtmls.size() > 0)
        {
            textHtmlBodyPart = new MimeBodyPart();
            FileInfo bodyHtml = bodyHtmls.get(0);
            contentType = new ContentType(bodyHtml.getContentData().getMimetype());
            ContentReader reader = imapHelper.getFileFolderService().getReader(bodyHtml.getNodeRef());
            try
            {
                source = new ByteArrayDataSource(reader.getContentInputStream(), contentType.toString());
            }
            catch (IOException e)
            {
                logger.error(e);
                errorMessage = e.getMessage();
            }
            if (source != null)
            {
                textHtmlBodyPart.setDataHandler(new DataHandler(source));
                textHtmlBodyPart.addHeader(CONTENT_TYPE, bodyHtml.getContentData().getMimetype());
                // textHtmlBodyPart.addHeader(CONTENT_TRANSFER_ENCODING, EIGHT_BIT_ENCODING);
                textHtmlBodyPart.addHeader(CONTENT_TRANSFER_ENCODING, BASE_64_ENCODING);
            }
            else
            {
                textHtmlBodyPart.setText(errorMessage, UTF_8);
            }
            messageBody.addBodyPart(textHtmlBodyPart);
        }
        // </------------------------ text html body part ------------------------>
        // <------------------------ text plain body part ------------------------>
        List<FileInfo> results = imapHelper.searchFiles(messageInfo.getNodeRef(), "*.txt", ImapModel.TYPE_IMAP_BODY, false);
        MimeBodyPart textPlainBodyPart = null;
        String text = null;
        if (results != null && results.size() > 0)
        {
            textPlainBodyPart = new MimeBodyPart();
            FileInfo bodyTxt = results.get(0);
            text = imapHelper.getFileFolderService().getReader(bodyTxt.getNodeRef()).getContentString();
            contentType = new ContentType(bodyTxt.getContentData().getMimetype());
        }
        else if (textHtmlBodyPart == null)
        {
            text = I18NUtil.getMessage("imap.server.info.message_body_not_found");
            contentType = new ContentType(EmailBodyType.TEXT_PLAIN.getMimeType() + "; charset=UTF-8");
        }

        textPlainBodyPart.setText(text, contentType.getParameter("charset"), contentType.getSubType());
        textPlainBodyPart.addHeader(CONTENT_TYPE, contentType.toString());
        messageBody.addBodyPart(textPlainBodyPart);
        // </------------------------ text plain body part ------------------------>

        // Body part for multipart/alternative
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(messageBody);
        // Add multipart/alternative into root multipart/mixed...
        rootMultipart.addBodyPart(messageBodyPart);

        // Process attachments
        List<FileInfo> attaches = imapHelper.searchFiles(messageInfo.getNodeRef(), "*", ImapModel.TYPE_IMAP_ATTACH, false);

        for (FileInfo attach : attaches)
        {
            try
            {

                errorMessage = null;
                messageBodyPart = new MimeBodyPart();
                ContentReader reader = imapHelper.getFileFolderService().getReader(attach.getNodeRef());
                source = new ByteArrayDataSource(reader.getContentInputStream(), attach.getContentData().getMimetype());
            }
            catch (IOException e)
            {
                logger.error(e);
                errorMessage = e.getMessage();
            }
            if (source != null)
            {
                String attachID = (String) imapHelper.getNodeService().getProperty(attach.getNodeRef(), ImapModel.PROP_ATTACH_ID);
                if (attachID != null)
                {
                    messageBodyPart.addHeader(CONTENT_ID, attachID);
                }
                StringBuilder ct = new StringBuilder(attach.getContentData().getMimetype()).append("; name=\"").append(attach.getName()).append("\"");
                messageBodyPart.addHeader(CONTENT_TYPE, ct.toString());
                messageBodyPart.addHeader(CONTENT_TRANSFER_ENCODING, BASE_64_ENCODING);
                messageBodyPart.setDataHandler(new DataHandler(source));
                try
                {
                    messageBodyPart.setFileName(MimeUtility.encodeText(attach.getName(), KOI8R_CHARSET, null));
                }
                catch (UnsupportedEncodingException e)
                {
                    // ignore
                }
            }
            else
            {
                messageBodyPart.setText(errorMessage, UTF_8);
            }
            rootMultipart.addBodyPart(messageBodyPart);
        }
        return rootMultipart;
    }

    private void addFromInternal(String addressesString) throws MessagingException
    {
        if (addressesString != null)
        {
            addFrom(InternetAddress.parse(addressesString));
        }
        else
        {
            addFrom(new Address[] { new InternetAddress(DEFAULT_EMAIL_FROM) });
        }
    }

    /**
     * TODO USE CASE 2: "The To/addressee will be the first email alias found in the parent folders or a default one (TBD)". It seems to be more informative as alike
     * {@code <user>@<current.domain>}...
     * 
     * @return Generated TO address {@code <user>@<current.domain>}
     * @throws AddressException
     */
    private InternetAddress[] buildRecipientToAddress() throws AddressException
    {
        InternetAddress[] result = null;
        String defaultEmailTo = null;
        // TODO : search first email alias found in the parent folders
        // if (found) defaultEmailTo = foundAlias
        // else
        final String escapedUserName = imapHelper.getCurrentUser().replaceAll("[/,\\,@]", ".");
        final String userDomain = DEFAULT_EMAIL_TO.split("@")[1];
        defaultEmailTo = escapedUserName + "@" + userDomain;
        try
        {
            result = InternetAddress.parse(defaultEmailTo);
        }
        catch (AddressException e)
        {
            logger.error(String.format("Wrong email address '%s'.", defaultEmailTo), e);
            result = InternetAddress.parse(DEFAULT_EMAIL_TO);
        }
        return result;
    }

    /**
     * Builds the InternetAddress from the Content Author name if provided. If name not specified, it takes Content Creator name. If content creator does not exists, the default
     * from address will be returned.
     * 
     * @param contentAuthor The content author full name.
     * @return Generated InternetAddress[] array.
     * @throws AddressException
     */
    private InternetAddress[] buildSenderFromAddress(Map<QName, Serializable> properties) throws AddressException
    {
        // Generate FROM address (Content author)
        InternetAddress[] addressList = null;
        String prop = (String) properties.get(ContentModel.PROP_AUTHOR);
        String defaultFromAddress = imapHelper.getDefaultFromAddress();
        defaultFromAddress = defaultFromAddress == null ? DEFAULT_EMAIL_FROM : defaultFromAddress;
        try
        {

            if (prop != null)
            {
                StringBuilder contentAuthor = new StringBuilder();
                contentAuthor.append("\"").append(prop).append("\" <").append(defaultFromAddress).append(">");
                addressList = InternetAddress.parse(contentAuthor.toString());
            }
            else
            {
                prop = (String) properties.get(ContentModel.PROP_CREATOR);
                if (prop != null)
                {
                    StringBuilder creator = new StringBuilder();
                    creator.append("\"").append(prop).append("\" <").append(defaultFromAddress).append(">");
                    addressList = InternetAddress.parse(creator.toString());
                }
                else
                {
                    throw new AddressException(I18NUtil.getMessage("imap.server.error.properties_dont_exist"));
                }
            }
        }
        catch (AddressException e)
        {
            addressList = InternetAddress.parse(DEFAULT_EMAIL_FROM);
        }
        return addressList;
    }

}
