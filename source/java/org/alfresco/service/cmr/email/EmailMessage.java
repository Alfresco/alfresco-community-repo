package org.alfresco.service.cmr.email;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Interface to process email messages. 
 * 
 * @author maxim
 * @since 2.2
 */
public interface EmailMessage extends Serializable
{
    /**
     * @return FROM address.
     */
    public String getFrom();

    /**
     * @return TO address.
     */
    public String getTo();
    
    /**
     * @return CC addresses.
     */
    public List<String> getCC();

    /**
     * @return sent date.
     */
    public Date getSentDate();

    /**
     * Get the subject of the message
     * @return subject of the message or null if there is no subject.
     */
    public String getSubject();

    /**
     * @return part of the mail body.
     */
    public EmailMessagePart getBody();

    /**
     * @return parts of the mail attachments.
     */
    public EmailMessagePart[] getAttachments();

}
