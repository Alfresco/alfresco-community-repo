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
package org.alfresco.email.server.impl.subetha;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.MimeMessage.RecipientType;

import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailMessagePart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* 
 * TODO There's a lot of metadata extraction going on in this class that 
 * is duplicated by RFC822MetadataExtracter
 */

/**
 * Concrete representation of an email message as implemented for the SubEtha mail server.
 * 
 * @since 2.2
 */

public class SubethaEmailMessage implements EmailMessage
{
    private static final String ERR_FAILED_TO_CREATE_MIME_MESSAGE = "email.server.err.failed_to_create_mime_message";
    private static final String ERR_EXTRACTING_FROM_ADDRESS = "email.server.err.extracting_from_address";
    private static final String ERR_NO_FROM_ADDRESS = "email.server.err.no_from_address";
    private static final String ERR_EXTRACTING_TO_ADDRESS = "email.server.err.extracting_to_address";
    private static final String ERR_NO_TO_ADDRESS = "email.server.err.no_to_address";
    private static final String ERR_EXTRACTING_SUBJECT = "email.server.err.extracting_subject";
    private static final String ERR_EXTRACTING_SENT_DATE = "email.server.err.extracting_sent_date";
    private static final String ERR_PARSE_MESSAGE = "email.server.err.parse_message";
    
    private static final long serialVersionUID = -3735187524926395261L;

    private static final Log log = LogFactory.getLog(SubethaEmailMessage.class);

    private static final String MIME_PLAIN_TEXT = "text/plain";
    private static final String MIME_HTML_TEXT = "text/html";
    private static final String MIME_XML_TEXT = "text/xml";
    private static final String MIME_APPLICATION = "application/*";
    private static final String MIME_IMAGE = "image/*";
    private static final String MIME_MULTIPART = "multipart/*";
    private static final String MIME_RFC822 = "message/rfc822";
    private static final String FILENAME_ATTACHMENT_PREFIX = "Attachment";

    private String from;
    private String to;
    private String subject;
    private List<String> cc;
    private Date sentDate;
    private EmailMessagePart body;
    private EmailMessagePart[] attachments;
    transient private int bodyNumber = 0;
    transient private int attachmentNumber = 0;
    transient private List<EmailMessagePart> attachmentList = new LinkedList<EmailMessagePart>();

    protected SubethaEmailMessage()
    {
        super();
    }

    public SubethaEmailMessage(MimeMessage mimeMessage)
    {
        processMimeMessage(mimeMessage);
    }

    public SubethaEmailMessage(InputStream dataInputStream)
    {
        MimeMessage mimeMessage = null;
        try
        {
            mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()), dataInputStream);
        }
        catch (MessagingException e)
        {
            throw new EmailMessageException(ERR_FAILED_TO_CREATE_MIME_MESSAGE, e.getMessage());
        }

        processMimeMessage(mimeMessage);
    }

    private void processMimeMessage(MimeMessage mimeMessage)
    {
        if (from == null)
        {
            Address[] addresses = null;
            try
            {
                addresses = mimeMessage.getFrom();
            }
            catch (MessagingException e)
            {
                throw new EmailMessageException(ERR_EXTRACTING_FROM_ADDRESS, e.getMessage());
            }
            if (addresses == null || addresses.length == 0)
            {
                throw new EmailMessageException(ERR_NO_FROM_ADDRESS);
            }
            if(addresses[0] instanceof InternetAddress)
            {
                from = ((InternetAddress)addresses[0]).getAddress();
            }
            else
            {
            from = addresses[0].toString();
        }
         
        }

        if (to == null)
        {
            Address[] addresses = null;
            try
            {
                addresses = mimeMessage.getAllRecipients();
            }
            catch (MessagingException e)
            {
                throw new EmailMessageException(ERR_EXTRACTING_TO_ADDRESS, e.getMessage());
            }
            if (addresses == null || addresses.length == 0)
            {
                throw new EmailMessageException(ERR_NO_TO_ADDRESS);
            }
            if(addresses[0] instanceof InternetAddress)
            {
                to = ((InternetAddress)addresses[0]).getAddress();
            }
            else
            {
            to = addresses[0].toString();
        }

            to = addresses[0].toString();
        }
        
        if (cc == null)
        {
            try
            {
                ArrayList<String> list = new ArrayList<String>();
            
                Address[] cca = mimeMessage.getRecipients(RecipientType.CC);
            
                if(cca != null)
                {
                    for(Address a : cca)
                    {
                        list.add(a.toString());
                    }
                }
                cc = list;
            }
            catch (MessagingException e)
            {
                // Do nothing - this is not a show-stopper.
                cc = null;
            }
        }
            
        try
        {
            subject = encodeSubject(mimeMessage.getSubject());
        }
        catch (MessagingException e)
        {
            throw new EmailMessageException(ERR_EXTRACTING_SUBJECT, e.getMessage());
        }
        if (subject == null)
        {
            subject = ""; // Just anti-null stub :)
        }

        try
        {
            sentDate = mimeMessage.getSentDate();
        }
        catch (MessagingException e)
        {
            throw new EmailMessageException(ERR_EXTRACTING_SENT_DATE, e.getMessage());
        }
        if (sentDate == null)
        {
            sentDate = new Date(); // Just anti-null stub :)
        }

        parseMessagePart(mimeMessage);
        attachments = new EmailMessagePart[attachmentList.size()];
        attachmentList.toArray(attachments);
        attachmentList = null;
    }

    private void parseMessagePart(Part messagePart)
    {
        try
        {
            if (messagePart.isMimeType(MIME_PLAIN_TEXT) || messagePart.isMimeType(MIME_HTML_TEXT))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Text or HTML part was found. ContentType: " + messagePart.getContentType());
                }
                addBody(messagePart);
            }
            else if (messagePart.isMimeType(MIME_XML_TEXT))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("XML part was found.");
                }
                addAttachment(messagePart);
            }
            else if (messagePart.isMimeType(MIME_APPLICATION))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Application part was found.");
                }
                addAttachment(messagePart);
            }
            else if (messagePart.isMimeType(MIME_IMAGE))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Image part was found.");
                }
                addAttachment(messagePart);
            }
            else if (messagePart.isMimeType(MIME_MULTIPART))
            {
                // if multipart, this method will be called recursively
                // for each of its parts
                Multipart mp = (Multipart) messagePart.getContent();
                int count = mp.getCount();

                if (log.isDebugEnabled())
                {
                    log.debug("MULTIPART with " + count + " part(s) found. Processin each part...");
                }
                for (int i = 0; i < count; i++)
                {
                    BodyPart bp = mp.getBodyPart(i);
                    if (bp.getContent() instanceof MimeMultipart)
                    {
                        // It's multipart.  Recurse.
                        parseMessagePart(bp);
                    }
                    else
                    {
                        // It's the body
                        addBody(bp);
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("MULTIPART processed.");
                }

            }
            else if (messagePart.isMimeType(MIME_RFC822))
            {
                // if rfc822, call this method with its content as the part
                if (log.isDebugEnabled())
                {
                    log.debug("MIME_RFC822 part found. Processing inside part...");
                }

                parseMessagePart((Part) messagePart.getContent());

                if (log.isDebugEnabled())
                {
                    log.debug("MIME_RFC822 processed.");
                }

            }
            else
            {
                // if all else fails, put this in the attachments map.
                // Actually we don't know what it is.
                if (log.isDebugEnabled())
                {
                    log.debug("Unrecognized part was found. Put it into attachments.");
                }
                addAttachment(messagePart);
            }
        }
        catch (IOException e)
        {
            throw new EmailMessageException(ERR_PARSE_MESSAGE, e.getMessage());
        }
        catch (MessagingException e)
        {
            throw new EmailMessageException(ERR_PARSE_MESSAGE, e.getMessage());
        }
    }

    private void addBody(Part messagePart) throws MessagingException
    {
        if (body != null)
        {
            attachmentList.add(new SubethaEmailMessagePart(messagePart, getPartFileName(getSubject() + " (part " + ++bodyNumber + ")", messagePart)));
            if (log.isInfoEnabled())
            {
                log.info(String.format("Attachment \"%s\" has been added.", attachmentList.get(attachmentList.size() - 1).getFileName()));
            }
        }
        else
        {
            body = new SubethaEmailMessagePart(messagePart, getPartFileName(getSubject(), messagePart));
            if (log.isDebugEnabled())
            {
                log.debug("Body has been added.");
            }
        }

    }

    /**
     * Method adds a message part to the attachments list
     * 
     * @param messagePart A part of message
     * @throws EmailMessageException
     * @throws MessagingException
     */
    private void addAttachment(Part messagePart) throws MessagingException
    {
        String fileName = getPartFileName(FILENAME_ATTACHMENT_PREFIX + attachmentNumber, messagePart);
        attachmentList.add(new SubethaEmailMessagePart(messagePart, fileName));
        if (log.isDebugEnabled())
        {
            log.debug("Attachment added: " + fileName);
        }
    }

    /**
     * Method extracts file name from a message part for saving its as aa attachment. If the file name can't be extracted, it will be generated based on defaultPrefix parameter.
     * 
     * @param defaultPrefix This prefix fill be used for generating file name.
     * @param messagePart A part of message
     * @return File name.
     * @throws MessagingException
     */
    private String getPartFileName(String defaultPrefix, Part messagePart) throws MessagingException
    {
        String fileName = messagePart.getFileName();
        if (fileName != null)
        {
            try
            {
                fileName = MimeUtility.decodeText(fileName);
            }
            catch (UnsupportedEncodingException ex)
            {
                // Nothing to do :)
            }
        }
        else
        {
            fileName = defaultPrefix;
            if (messagePart.isMimeType(MIME_PLAIN_TEXT))
                fileName += ".txt";
            else if (messagePart.isMimeType(MIME_HTML_TEXT))
                fileName += ".html";
            else if (messagePart.isMimeType(MIME_XML_TEXT))
                fileName += ".xml";
            else if (messagePart.isMimeType(MIME_IMAGE))
                fileName += ".gif";
        }
        return fileName;
    }

    public void setRmiRegistry(String rmiRegistryHost, int rmiRegistryPort)
    {
        if (body instanceof SubethaEmailMessagePart) 
        {
            ((SubethaEmailMessagePart) body).setRmiRegistry(rmiRegistryHost, rmiRegistryPort);
        }
        
        for (EmailMessagePart attachment : attachments)
        {
            if (attachment instanceof SubethaEmailMessagePart)
            {
                ((SubethaEmailMessagePart) attachment).setRmiRegistry(rmiRegistryHost, rmiRegistryPort);
            }
        }
    }
    
    public List<String> getCC()
    {
        return cc;
    }
    
    
    public String getFrom()
    {
        return from;
    }

    public String getTo()
    {
        return to;
    }

    public Date getSentDate()
    {
        return sentDate;
    }

    public String getSubject()
    {
        return subject;
    }

    public EmailMessagePart getBody()
    {
        return body;
    }

    public EmailMessagePart[] getAttachments()
    {
        return attachments;
    }
    
    /**
     * Replaces characters \/*|:"<>?. on their hex values. Subject field is used as name of the content, so we need to replace characters that are forbidden in content names.
     * 
     * @param subject String representing subject
     * @return Encoded string
     */
    static private String encodeSubject(String subject)
    {
        String result = subject.trim();
        String[][] s = new String[][] { { "\\", "%5c" }, { "/", "%2f" }, { "*", "%2a" }, { "|", "%7c" }, { ":", "%3a" }, { "\"", "%22" }, { "<", "%3c" }, { ">", "%3e" },
                { "?", "%3f" },  { ".", "%2e" } };

        for (int i = 0; i < s.length; i++)
        {
            result = result.replace(s[i][0], s[i][1]);
        }

        return result;
    }

}
