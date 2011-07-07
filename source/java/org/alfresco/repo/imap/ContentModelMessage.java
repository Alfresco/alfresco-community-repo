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
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.imap.ImapService.EmailBodyFormat;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class ContentModelMessage extends AbstractMimeMessage
{
    private Log logger = LogFactory.getLog(ContentModelMessage.class);
    
    protected static final String DEFAULT_EMAIL_FROM = "alfresco@alfresco.org";
    protected static final String DEFAULT_EMAIL_TO = "alfresco@alfresco.org";

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
    
    /**
     * Generate the "to" address.
     * 
     * Step 1: Use PROP_ADDRESSEE
     * 
     * Last Step: Use the default address
     * 
     * @return Generated TO address {@code <user>@<current.domain>}
     * @throws AddressException
     */
    private InternetAddress[] buildRecipientToAddress() throws AddressException
    {
        InternetAddress[] result = null;
       
        
        Map<QName, Serializable> properties = messageFileInfo.getProperties();
        
        /**
         * Step 1 : Get the ADDRESSEE if it exists
         */
        if(properties.containsKey(ContentModel.PROP_ADDRESSEE))
        {
            String addressee = (String)properties.get(ContentModel.PROP_ADDRESSEE);
            try
            { 
                result = InternetAddress.parse(addressee);
                return result;
            }
            catch (AddressException e)
            {
                // try next step
            }
        }
        
//      final String escapedUserName = AuthenticationUtil.getFullyAuthenticatedUser().replaceAll("[/,\\,@]", ".");
//      final String userDomain = DEFAULT_EMAIL_TO.split("@")[1];
//      String userName = escapedUserName + "@" + userDomain;
//      try
//      {
//          result = InternetAddress.parse(userName);
//          return result;        
//      }
//      catch (AddressException e)
//      {
//      }
        
        /**
         * Last Step : Get the Default address
         */
        String defaultToAddress = imapService.getDefaultToAddress();
      
        try
        { 
            result = InternetAddress.parse(defaultToAddress);
            return result;
        }
        catch (AddressException e)
        {
            logger.warn(String.format("Wrong email address '%s'.", defaultToAddress), e);
        }
        result = InternetAddress.parse(DEFAULT_EMAIL_TO);
        return result;
       
    }
    
    /**
     * Builds the InternetAddress for the sender (from) 
     * 
     * Step 1: use PROP_ORIGINATOR
     * 
     * Last Step : Use the default address.
     * 
     * Content Author name if provided. If name not specified, it takes Content Creator name. 
     * If content creator does not exists, the default from address will be returned.
     * 
     * @param contentAuthor The content author full name.
     * @return Generated InternetAddress[] array.
     * @throws AddressException
     */
    private InternetAddress[] buildSenderFromAddress() throws AddressException
    {
        // Generate FROM address (Content author)
        InternetAddress[] result = null;
        Map<QName, Serializable> properties = messageFileInfo.getProperties();
        String defaultFromAddress = imapService.getDefaultFromAddress();
        
        /**
         * Step 1 : Get the ORIGINATOR if it exists
         */
        if(properties.containsKey(ContentModel.PROP_ORIGINATOR))
        {
            String addressee = (String)properties.get(ContentModel.PROP_ORIGINATOR);
            try
            { 
                result = InternetAddress.parse(addressee);
                return result;
            }
            catch (AddressException e)
            {
                // try next step
            }
        }

        /**
         * Go for the author property
         */
        if(properties.containsKey(ContentModel.PROP_AUTHOR))
        {
            String author = (String) properties.get(ContentModel.PROP_AUTHOR);
            try
            {

              StringBuilder contentAuthor = new StringBuilder();
              contentAuthor.append("\"").append(author).append("\" <").append(defaultFromAddress).append(">");
              result = InternetAddress.parse(contentAuthor.toString());
              return result;
            
            }
            catch (AddressException e)
            {
                // try next step
            }
        }
        
        if(properties.containsKey(ContentModel.PROP_CREATOR))
        {
            String author = (String) properties.get(ContentModel.PROP_CREATOR);
            try
            {

              StringBuilder contentAuthor = new StringBuilder();
              contentAuthor.append("\"").append(author).append("\" <").append(defaultFromAddress).append(">");
              result = InternetAddress.parse(contentAuthor.toString());
              return result;
            
            }
            catch (AddressException e)
            {
                // try next step
            }
        }
        
        /**
         * Last Step : Get the Default address
         */
        try
        { 
            result = InternetAddress.parse(defaultFromAddress);
            return result;
        }
        catch (AddressException e)
        {
            logger.warn(String.format("Wrong email address '%s'.", defaultFromAddress), e);
        }
        result = InternetAddress.parse(DEFAULT_EMAIL_FROM);
        return result;
        
    }
}
